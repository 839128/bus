/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac.software;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.builtin.software.OSThread;
import org.miaixz.bus.health.builtin.software.common.AbstractOSProcess;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.health.mac.driver.ThreadInfo;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.Group;
import com.sun.jna.platform.mac.SystemB.Passwd;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.Resource;

/**
 * OSProcess implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class MacOSProcess extends AbstractOSProcess {

    private static final int ARGMAX = SysctlKit.sysctl("kern.argmax", 0);
    private static final long TICKS_PER_MS;
    private static final boolean LOG_MAC_SYSCTL_WARNING = Config.get(Config._MAC_SYSCTL_LOGWARNING, false);
    private static final int MAC_RLIMIT_NOFILE = 8;
    // 64-bit flag
    private static final int P_LP64 = 0x4;
    /*
     * macOS States:
     */
    private static final int SSLEEP = 1; // sleeping on high priority
    private static final int SWAIT = 2; // sleeping on low priority
    private static final int SRUN = 3; // running
    private static final int SIDL = 4; // intermediate state in process creation
    private static final int SZOMB = 5; // intermediate state in process termination
    private static final int SSTOP = 6; // process being traced

    static {
        // default to 1 tick per nanosecond
        long ticksPerSec = 1_000_000_000L;
        IOIterator iter = IOKitUtil.getMatchingServices("IOPlatformDevice");
        if (iter != null) {
            IORegistryEntry cpu = iter.next();
            while (cpu != null) {
                try {
                    String s = cpu.getName().toLowerCase(Locale.ROOT);
                    if (s.startsWith("cpu") && s.length() > 3) {
                        byte[] data = cpu.getByteArrayProperty("timebase-frequency");
                        if (data != null) {
                            ticksPerSec = Parsing.byteArrayToLong(data, 4, false);
                            break;
                        }
                    }
                } finally {
                    cpu.release();
                }
                cpu = iter.next();
            }
            iter.release();
        }
        // Convert to ticks per millisecond
        TICKS_PER_MS = ticksPerSec / 1000L;
    }

    private final MacOperatingSystem os;
    private final Supplier<Pair<List<String>, Map<String, String>>> argsEnviron = Memoizer
            .memoize(this::queryArgsAndEnvironment);
    private final Supplier<String> commandLine = Memoizer.memoize(this::queryCommandLine);
    private int majorVersion;
    private int minorVersion;
    private String name = Normal.EMPTY;
    private String path = Normal.EMPTY;
    private String currentWorkingDirectory;
    private String user;
    private String userID;
    private String group;
    private String groupID;
    private State state = State.INVALID;
    private int parentProcessID;
    private int threadCount;
    private int priority;
    private long virtualSize;
    private long residentSetSize;
    private long kernelTime;
    private long userTime;
    private long startTime;
    private long upTime;
    private long bytesRead;
    private long bytesWritten;
    private long openFiles;
    private int bitness;
    private long minorFaults;
    private long majorFaults;
    private long contextSwitches;

    public MacOSProcess(int pid, int major, int minor, MacOperatingSystem os) {
        super(pid);
        this.majorVersion = major;
        this.minorVersion = minor;
        this.os = os;
        updateAttributes();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getCommandLine() {
        return this.commandLine.get();
    }

    private String queryCommandLine() {
        return String.join(Symbol.SPACE, getArguments());
    }

    @Override
    public List<String> getArguments() {
        return argsEnviron.get().getLeft();
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return argsEnviron.get().getRight();
    }

    private Pair<List<String>, Map<String, String>> queryArgsAndEnvironment() {
        int pid = getProcessID();
        // Set up return objects
        List<String> args = new ArrayList<>();
        // API does not specify any particular order of entries, but it is reasonable to
        // maintain whatever order the OS provided to the end user
        Map<String, String> env = new LinkedHashMap<>();

        // Get command line via sysctl
        int[] mib = new int[3];
        mib[0] = 1; // CTL_KERN
        mib[1] = 49; // KERN_PROCARGS2
        mib[2] = pid;
        // Allocate memory for arguments
        try (Memory procargs = new Memory(ARGMAX)) {
            procargs.clear();
            size_t.ByReference size = new size_t.ByReference(ARGMAX);
            // Fetch arguments
            if (0 == SystemB.INSTANCE.sysctl(mib, mib.length, procargs, size, null, size_t.ZERO)) {
                // Procargs contains an int representing total # of args, followed by a
                // null-terminated execpath string and then the arguments, each
                // null-terminated (possible multiple consecutive nulls),
                // The execpath string is also the first arg.
                // Following this is an int representing total # of env, followed by
                // null-terminated envs in similar format
                int nargs = procargs.getInt(0);
                // Sanity check
                if (nargs > 0 && nargs <= 1024) {
                    // Skip first int (containing value of nargs)
                    long offset = SystemB.INT_SIZE;
                    // Skip exec_command, as
                    offset += procargs.getString(offset).length();
                    // Iterate character by character using offset
                    // Build each arg and add to list
                    while (offset < size.longValue()) {
                        // Advance through additional nulls
                        while (procargs.getByte(offset) == 0) {
                            if (++offset >= size.longValue()) {
                                break;
                            }
                        }
                        // Grab a string. This should go until the null terminator
                        String arg = procargs.getString(offset);
                        if (nargs-- > 0) {
                            // If we havent found nargs yet, it's an arg
                            args.add(arg);
                        } else {
                            // otherwise it's an env
                            int idx = arg.indexOf(Symbol.C_EQUAL);
                            if (idx > 0) {
                                env.put(arg.substring(0, idx), arg.substring(idx + 1));
                            }
                        }
                        // Advance offset to next null
                        offset += arg.length();
                    }
                }
            } else {
                // Don't warn for pid 0
                if (pid > 0 && LOG_MAC_SYSCTL_WARNING) {
                    Logger.warn(
                            "Failed sysctl call for process arguments (kern.procargs2), process {} may not exist. Error code: {}",
                            pid, Native.getLastError());
                }
            }
        }
        return Pair.of(Collections.unmodifiableList(args), Collections.unmodifiableMap(env));
    }

    @Override
    public String getCurrentWorkingDirectory() {
        return this.currentWorkingDirectory;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public String getUserID() {
        return this.userID;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public String getGroupID() {
        return this.groupID;
    }

    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public int getParentProcessID() {
        return this.parentProcessID;
    }

    @Override
    public int getThreadCount() {
        return this.threadCount;
    }

    @Override
    public List<OSThread> getThreadDetails() {
        long now = System.currentTimeMillis();
        return ThreadInfo.queryTaskThreads(getProcessID()).stream().parallel().map(stat -> {
            // For long running threads the start time calculation can overestimate
            long start = Math.max(now - stat.getUpTime(), getStartTime());
            return new MacOSThread(getProcessID(), stat.getThreadId(), stat.getState(), stat.getSystemTime(),
                    stat.getUserTime(), start, now - start, stat.getPriority());
        }).collect(Collectors.toList());
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public long getVirtualSize() {
        return this.virtualSize;
    }

    @Override
    public long getResidentSetSize() {
        return this.residentSetSize;
    }

    @Override
    public long getKernelTime() {
        return this.kernelTime;
    }

    @Override
    public long getUserTime() {
        return this.userTime;
    }

    @Override
    public long getUpTime() {
        return this.upTime;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public long getBytesRead() {
        return this.bytesRead;
    }

    @Override
    public long getBytesWritten() {
        return this.bytesWritten;
    }

    @Override
    public long getOpenFiles() {
        return this.openFiles;
    }

    @Override
    public long getSoftOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            SystemB.INSTANCE.getrlimit(MAC_RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_cur;
        } else {
            return -1L; // not supported
        }
    }

    @Override
    public long getHardOpenFileLimit() {
        if (getProcessID() == this.os.getProcessId()) {
            final Resource.Rlimit rlimit = new Resource.Rlimit();
            SystemB.INSTANCE.getrlimit(MAC_RLIMIT_NOFILE, rlimit);
            return rlimit.rlim_max;
        } else {
            return -1L; // not supported
        }
    }

    @Override
    public int getBitness() {
        return this.bitness;
    }

    @Override
    public long getAffinityMask() {
        // macOS doesn't do affinity. Return a bitmask of the current processors.
        int logicalProcessorCount = SysctlKit.sysctl("hw.logicalcpu", 1);
        return logicalProcessorCount < 64 ? (1L << logicalProcessorCount) - 1 : -1L;
    }

    @Override
    public long getMinorFaults() {
        return this.minorFaults;
    }

    @Override
    public long getMajorFaults() {
        return this.majorFaults;
    }

    @Override
    public long getContextSwitches() {
        return this.contextSwitches;
    }

    @Override
    public boolean updateAttributes() {
        long now = System.currentTimeMillis();
        try (Struct.CloseableProcTaskAllInfo taskAllInfo = new Struct.CloseableProcTaskAllInfo()) {
            if (0 > SystemB.INSTANCE.proc_pidinfo(getProcessID(), SystemB.PROC_PIDTASKALLINFO, 0, taskAllInfo,
                    taskAllInfo.size()) || taskAllInfo.ptinfo.pti_threadnum < 1) {
                this.state = State.INVALID;
                return false;
            }
            try (Memory buf = new Memory(SystemB.PROC_PIDPATHINFO_MAXSIZE)) {
                if (0 < SystemB.INSTANCE.proc_pidpath(getProcessID(), buf, SystemB.PROC_PIDPATHINFO_MAXSIZE)) {
                    this.path = buf.getString(0).trim();
                    // Overwrite name with last part of path
                    String[] pathSplit = this.path.split("/");
                    if (pathSplit.length > 0) {
                        this.name = pathSplit[pathSplit.length - 1];
                    }
                }
            }
            if (this.name.isEmpty()) {
                // pbi_comm contains first 16 characters of name
                this.name = Native.toString(taskAllInfo.pbsd.pbi_comm, Charset.UTF_8);
            }

            switch (taskAllInfo.pbsd.pbi_status) {
            case SSLEEP:
                this.state = State.SLEEPING;
                break;
            case SWAIT:
                this.state = State.WAITING;
                break;
            case SRUN:
                this.state = State.RUNNING;
                break;
            case SIDL:
                this.state = State.NEW;
                break;
            case SZOMB:
                this.state = State.ZOMBIE;
                break;
            case SSTOP:
                this.state = State.STOPPED;
                break;
            default:
                this.state = State.OTHER;
                break;
            }
            this.parentProcessID = taskAllInfo.pbsd.pbi_ppid;
            this.userID = Integer.toString(taskAllInfo.pbsd.pbi_uid);
            Passwd pwuid = SystemB.INSTANCE.getpwuid(taskAllInfo.pbsd.pbi_uid);
            this.user = pwuid == null ? Integer.toString(taskAllInfo.pbsd.pbi_uid) : pwuid.pw_name;
            this.groupID = Integer.toString(taskAllInfo.pbsd.pbi_gid);
            Group grgid = SystemB.INSTANCE.getgrgid(taskAllInfo.pbsd.pbi_gid);
            this.group = grgid == null ? Integer.toString(taskAllInfo.pbsd.pbi_gid) : grgid.gr_name;
            this.threadCount = taskAllInfo.ptinfo.pti_threadnum;
            this.priority = taskAllInfo.ptinfo.pti_priority;
            this.virtualSize = taskAllInfo.ptinfo.pti_virtual_size;
            this.residentSetSize = taskAllInfo.ptinfo.pti_resident_size;
            this.kernelTime = taskAllInfo.ptinfo.pti_total_system / TICKS_PER_MS;
            this.userTime = taskAllInfo.ptinfo.pti_total_user / TICKS_PER_MS;
            this.startTime = taskAllInfo.pbsd.pbi_start_tvsec * 1000L + taskAllInfo.pbsd.pbi_start_tvusec / 1000L;
            this.upTime = now - this.startTime;
            this.openFiles = taskAllInfo.pbsd.pbi_nfiles;
            this.bitness = (taskAllInfo.pbsd.pbi_flags & P_LP64) == 0 ? 32 : 64;
            this.majorFaults = taskAllInfo.ptinfo.pti_pageins;
            // testing using getrusage confirms pti_faults includes both major and minor
            this.minorFaults = taskAllInfo.ptinfo.pti_faults - taskAllInfo.ptinfo.pti_pageins; // NOSONAR squid:S2184
            this.contextSwitches = taskAllInfo.ptinfo.pti_csw;
        }
        if (this.majorVersion > 10 || this.minorVersion >= 9) {
            try (Struct.CloseableRUsageInfoV2 rUsageInfoV2 = new Struct.CloseableRUsageInfoV2()) {
                if (0 == SystemB.INSTANCE.proc_pid_rusage(getProcessID(), SystemB.RUSAGE_INFO_V2, rUsageInfoV2)) {
                    this.bytesRead = rUsageInfoV2.ri_diskio_bytesread;
                    this.bytesWritten = rUsageInfoV2.ri_diskio_byteswritten;
                }
            }
        }
        try (Struct.CloseableVnodePathInfo vpi = new Struct.CloseableVnodePathInfo()) {
            if (0 < SystemB.INSTANCE.proc_pidinfo(getProcessID(), SystemB.PROC_PIDVNODEPATHINFO, 0, vpi, vpi.size())) {
                this.currentWorkingDirectory = Native.toString(vpi.pvi_cdir.vip_path, Charset.US_ASCII);
            }
        }
        return true;
    }

}
