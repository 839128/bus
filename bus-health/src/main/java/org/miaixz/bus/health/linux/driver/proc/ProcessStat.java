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
package org.miaixz.bus.health.linux.driver.proc;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.linux.ProcPath;

/**
 * Utility to read process statistics from {@code /proc/[pid]/stat}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class ProcessStat {

    /**
     * Constant defining the number of integer values in {@code /proc/pid/stat}. 2.6 Kernel has 44 elements, 3.3 has 47,
     * and 3.5 has 52.
     */
    public static final int PROC_PID_STAT_LENGTH;
    private static final java.util.regex.Pattern SOCKET = java.util.regex.Pattern.compile("socket:\\[(\\d+)\\]");

    static {
        String stat = Builder.getStringFromFile(ProcPath.SELF_STAT);
        if (stat.contains(Symbol.PARENTHESE_RIGHT)) {
            // add 3 to account for pid, process name in prarenthesis, and state
            PROC_PID_STAT_LENGTH = Parsing.countStringToLongArray(stat, Symbol.C_SPACE) + 3;
        } else {
            // Default assuming recent kernel
            PROC_PID_STAT_LENGTH = 52;
        }
    }

    private ProcessStat() {
    }

    /**
     * Reads the statistics in {@code /proc/[pid]/stat} and returns the results.
     *
     * @param pid The process ID for which to fetch stats
     * @return A triplet containing the process name as the first element, a character representing the process state as
     *         the second element, and an EnumMap as the third element, where the numeric values in {@link PidStat} are
     *         mapped to a {@link Long} value.
     *         <p>
     *         If the process doesn't exist, returns null.
     */
    public static Triplet<String, Character, Map<PidStat, Long>> getPidStats(int pid) {
        String stat = Builder.getStringFromFile(String.format(Locale.ROOT, ProcPath.PID_STAT, pid));
        if (stat.isEmpty()) {
            // If pid doesn't exist
            return null;
        }
        // Get process name from between parentheses and state immediately after
        int nameStart = stat.indexOf(Symbol.C_PARENTHESE_LEFT) + 1;
        int nameEnd = stat.indexOf(Symbol.C_PARENTHESE_RIGHT);
        String name = stat.substring(nameStart, nameEnd);
        Character state = stat.charAt(nameEnd + 2);
        // Split everything after the state
        String[] split = Pattern.SPACES_PATTERN.split(stat.substring(nameEnd + 4).trim());

        Map<PidStat, Long> statMap = new EnumMap<>(PidStat.class);
        PidStat[] enumArray = PidStat.class.getEnumConstants();
        for (int i = 3; i < enumArray.length && i - 3 < split.length; i++) {
            statMap.put(enumArray[i], Parsing.parseLongOrDefault(split[i - 3], 0L));
        }
        return Triplet.of(name, state, statMap);
    }

    /**
     * Reads the statistics in {@code /proc/[pid]/statm} and returns the results.
     *
     * @param pid The process ID for which to fetch stats
     * @return An EnumMap where the numeric values in {@link PidStatM} are mapped to a {@link Long} value.
     *         <p>
     *         If the process doesn't exist, returns null.
     */
    public static Map<PidStatM, Long> getPidStatM(int pid) {
        String statm = Builder.getStringFromFile(String.format(Locale.ROOT, ProcPath.PID_STATM, pid));
        if (statm.isEmpty()) {
            // If pid doesn't exist
            return null;
        }
        // Split the fields
        String[] split = Pattern.SPACES_PATTERN.split(statm);

        Map<PidStatM, Long> statmMap = new EnumMap<>(PidStatM.class);
        PidStatM[] enumArray = PidStatM.class.getEnumConstants();
        for (int i = 0; i < enumArray.length && i < split.length; i++) {
            statmMap.put(enumArray[i], Parsing.parseLongOrDefault(split[i], 0L));
        }
        return statmMap;
    }

    /**
     * Gets an array of files in the /proc/{pid}/fd directory.
     *
     * @param pid id of process to read file descriptors for
     * @return An array of File objects representing opened file descriptors of the process
     */
    public static File[] getFileDescriptorFiles(int pid) {
        return listNumericFiles(String.format(Locale.ROOT, ProcPath.PID_FD, pid));
    }

    /**
     * Gets an array of files in the /proc directory with only numeric digit filenames, corresponding to processes
     *
     * @return An array of File objects for the process files
     */
    public static File[] getPidFiles() {
        return listNumericFiles(ProcPath.PROC);
    }

    /**
     * Gets a map of sockets and their corresponding process ID
     *
     * @return a map with socket as the key and pid as the value
     */
    public static Map<Long, Integer> querySocketToPidMap() {
        Map<Long, Integer> pidMap = new HashMap<>();
        for (File f : getPidFiles()) {
            int pid = Parsing.parseIntOrDefault(f.getName(), -1);
            File[] fds = getFileDescriptorFiles(pid);
            for (File fd : fds) {
                String symLink = Builder.readSymlinkTarget(fd);
                if (symLink != null) {
                    Matcher m = SOCKET.matcher(symLink);
                    if (m.matches()) {
                        pidMap.put(Parsing.parseLongOrDefault(m.group(1), -1L), pid);
                    }
                }
            }
        }
        return pidMap;
    }

    /**
     * Gets a List of thread ids for a process from the {@code /proc/[pid]/task/} directory with only numeric digit
     * filenames, corresponding to the threads.
     *
     * @param pid process id
     * @return A list of thread id.
     */
    public static List<Integer> getThreadIds(int pid) {
        File[] threads = listNumericFiles(String.format(Locale.ROOT, ProcPath.TASK_PATH, pid));
        return Arrays.stream(threads).map(thread -> Parsing.parseIntOrDefault(thread.getName(), 0))
                .filter(threadId -> threadId != pid).collect(Collectors.toList());
    }

    private static File[] listNumericFiles(String path) {
        File directory = new File(path);
        File[] numericFiles = directory.listFiles(file -> Pattern.NUMBERS_PATTERN.matcher(file.getName()).matches());
        return numericFiles == null ? new File[0] : numericFiles;
    }

    /***
     * Returns Enum STATE for the state value obtained from status file of any process/thread.
     *
     * @param stateValue state value from the status file
     * @return OSProcess.State
     */
    public static OSProcess.State getState(char stateValue) {
        OSProcess.State state;
        switch (stateValue) {
        case 'R':
            state = OSProcess.State.RUNNING;
            break;
        case 'S':
            state = OSProcess.State.SLEEPING;
            break;
        case 'D':
            state = OSProcess.State.WAITING;
            break;
        case 'Z':
            state = OSProcess.State.ZOMBIE;
            break;
        case 'T':
            state = OSProcess.State.STOPPED;
            break;
        default:
            state = OSProcess.State.OTHER;
            break;
        }
        return state;
    }

    /**
     * Enum corresponding to the fields in the output of {@code /proc/[pid]/stat}
     */
    public enum PidStat {
        /**
         * The process ID.
         */
        PID,
        /**
         * The filename of the executable.
         */
        COMM,
        /**
         * One of the following characters, indicating process state:
         * <p>
         * R Running
         * <p>
         * S Sleeping in an interruptible wait
         * <p>
         * D Waiting in uninterruptible disk sleep
         * <p>
         * Z Zombie
         * <p>
         * T Stopped (on a signal) or (before Linux 2.6.33) trace stopped
         * <p>
         * t Tracing stop (Linux 2.6.33 onward)
         * <p>
         * W Paging (only before Linux 2.6.0)
         * <p>
         * X Dead (from Linux 2.6.0 onward)
         * <p>
         * x Dead (Linux 2.6.33 to 3.13 only)
         * <p>
         * K Wakekill (Linux 2.6.33 to 3.13 only)
         * <p>
         * W Waking (Linux 2.6.33 to 3.13 only)
         * <p>
         * P Parked (Linux 3.9 to 3.13 only)
         */
        STATE,
        /**
         * The PID of the parent of this process.
         */
        PPID,
        /**
         * The process group ID of the process.
         */
        PGRP,
        /**
         * The session ID of the process.
         */
        SESSION,
        /**
         * The controlling terminal of the process. (The minor device number is contained in the combination of bits 31
         * to 20 and 7 to 0; the major device number is in bits 15 to 8.)
         */
        TTY_NR,
        /**
         * The ID of the foreground process group of the controlling terminal of the process.
         */
        PTGID,
        /**
         * The kernel flags word of the process. For bit meanings, see the PF_* defines in the Linux kernel source file
         * include/linux/sched.h. Details depend on the kernel version.
         */
        FLAGS,
        /**
         * The number of minor faults the process has made which have not required loading a memory page from disk.
         */
        MINFLT,
        /**
         * The number of minor faults that the process's waited-for children have made.
         */
        CMINFLT,
        /**
         * The number of major faults the process has made which have required loading a memory page from disk.
         */
        MAJFLT,
        /**
         * The number of major faults that the process's waited-for children have made.
         */
        CMAJFLT,
        /**
         * Amount of time that this process has been scheduled in user mode, measured in clock ticks. This includes
         * guest time, cguest_time (time spent running a virtual CPU), so that applications that are not aware of the
         * guest time field do not lose that time from their calculations.
         */
        UTIME,
        /**
         * Amount of time that this process has been scheduled in kernel mode, measured in clock ticks.
         */
        STIME,
        /**
         * Amount of time that this process's waited-for children have been scheduled in user mode, measured in clock
         * ticks. This includes guest time, cguest_time (time spent running a virtual CPU).
         */
        CUTIME,
        /**
         * Amount of time that this process's waited-for children have been scheduled in kernel mode, measured in clock
         * ticks.
         */
        CSTIME,
        /**
         * For processes running a real-time scheduling policy (policy below; see sched_setscheduler(2)), this is the
         * negated scheduling priority, minus one; that is, a number in the range -2 to -100, corresponding to real-time
         * priorities 1 to 99. For processes running under a non-real-time scheduling policy, this is the raw nice value
         * (setpriority(2)) as represented in the kernel. The kernel stores nice values as numbers in the range 0 (high)
         * to 39 (low), corresponding to the user-visible nice range of -20 to 19.
         */
        PRIORITY,
        /**
         * The nice value (see setpriority(2)), a value in the range 19 (low priority) to -20 (high priority).
         */
        NICE,
        /**
         * Number of threads in this process.
         */
        NUM_THREADS,
        /**
         * The time in jiffies before the next SIGALRM is sent to the process due to an interval timer. Since ker‐nel
         * 2.6.17, this field is no longer maintained, and is hard coded as 0.
         */
        ITREALVALUE,
        /**
         * The time the process started after system boot, in clock ticks.
         */
        STARTTIME,
        /**
         * Virtual memory size in bytes.
         */
        VSIZE,
        /**
         * Resident Set Size: number of pages the process has in real memory. This is just the pages which count toward
         * text, data, or stack space. This does not include pages which have not been demand-loaded in, or which are
         * swapped out.
         */
        RSS,
        /**
         * Current soft limit in bytes on the rss of the process; see the description of RLIMIT_RSS in getrlimit(2).
         */
        RSSLIM,
        /**
         * The address above which program text can run.
         */
        STARTCODE,

        /**
         * The address below which program text can run.
         */
        ENDCODE,
        /**
         * The address of the start (i.e., bottom) of the stack.
         */
        STARTSTACK,
        /**
         * The current value of ESP (stack pointer), as found in the kernel stack page for the process.
         */
        KSTKESP,
        /**
         * The current EIP (instruction pointer).
         */
        KSTKEIP,
        /**
         * The bitmap of pending signals, displayed as a decimal number. Obsolete, because it does not provide
         * information on real-time signals; use /proc/[pid]/status instead.
         */
        SIGNAL,
        /**
         * The bitmap of blocked signals, displayed as a decimal number. Obsolete, because it does not provide
         * information on real-time signals; use /proc/[pid]/status instead.
         */
        BLOCKED,
        /**
         * The bitmap of ignored signals, displayed as a decimal number. Obsolete, because it does not provide
         * information on real-time signals; use /proc/[pid]/status instead.
         */
        SIGIGNORE,
        /**
         * The bitmap of caught signals, displayed as a decimal number. Obsolete, because it does not provide
         * information on real-time signals; use /proc/[pid]/status instead.
         */
        SIGCATCH,
        /**
         * This is the "channel" in which the process is waiting. It is the address of a location in the kernel where
         * the process is sleeping. The corresponding symbolic name can be found in /proc/[pid]/wchan.
         */
        WCHAN,
        /**
         * Number of pages swapped (not maintained).
         */
        NSWAP,
        /**
         * Cumulative nswap for child processes (not maintained).
         */
        CNSWAP,
        /**
         * Signal to be sent to parent when we die.
         */
        EXIT_SIGNAL,
        /**
         * CPU number last executed on.
         */
        PROCESSOR,
        /**
         * Real-time scheduling priority, a number in the range 1 to 99 for processes scheduled under a real-time
         * policy, or 0, for non-real-time processes (see sched_setscheduler(2)).
         */
        RT_PRIORITY,
        /**
         * Scheduling policy (see sched_setscheduler(2)). Decode using the SCHED_* constants in linux/sched.h.
         */
        POLICY,
        /**
         * Aggregated block I/O delays, measured in clock ticks (centiseconds).
         */
        DELAYACCT_BLKIO_TICKS,
        /**
         * Guest time of the process (time spent running a vir‐ tual CPU for a guest operating system), measured in
         * clock ticks.
         */
        GUEST_TIME,
        /**
         * Guest time of the process's children, measured in clock ticks.
         */
        CGUEST_TIME,
        /**
         * Address above which program initialized and uninitialized (BSS) data are placed.
         */
        START_DATA,
        /**
         * Address below which program initialized and uninitialized (BSS) data are placed.
         */
        END_DATA,
        /**
         * Address above which program heap can be expanded with brk(2).
         */
        START_BRK,
        /**
         * Address above which program command-line arguments (argv) are placed.
         */
        ARG_START,

        /**
         * Address below program command-line arguments (argv) are placed.
         */
        ARG_END,

        /**
         * Address above which program environment is placed.
         */
        ENV_START,

        /**
         * Address below which program environment is placed.
         */
        ENV_END,

        /**
         * The thread's exit status in the form reported by waitpid(2).
         */
        EXIT_CODE
    }

    /**
     * Enum corresponding to the fields in the output of {@code /proc/[pid]/statm}
     */
    public enum PidStatM {
        /**
         * Total program size
         */
        SIZE,
        /**
         * Resident set size
         */
        RESIDENT,
        /**
         * Number of resident shared pages (i.e., backed by a file)
         */
        SHARED,
        /**
         * Text (code)
         */
        TEXT,
        /**
         * Library (unused since Linux 2.6; always 0)
         */
        LIB,
        /**
         * Data + stack
         */
        DATA,
        /**
         * Dirty pages (unused since Linux 2.6; always 0)
         */
        DT
    }

}
