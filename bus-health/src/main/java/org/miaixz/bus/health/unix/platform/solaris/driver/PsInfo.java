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
package org.miaixz.bus.health.unix.platform.solaris.driver;

import java.util.*;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.unix.jna.SolarisLibc;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.LibCAPI.size_t;
import com.sun.jna.platform.unix.LibCAPI.ssize_t;

/**
 * Utility to query /proc/psinfo
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class PsInfo {

    private static final SolarisLibc LIBC = SolarisLibc.INSTANCE;

    private static final long PAGE_SIZE = Parsing.parseLongOrDefault(Executor.getFirstAnswer("pagesize"), 4096L);

    /**
     * Reads /proc/pid/psinfo and returns data in a structure
     *
     * @param pid The process ID
     * @return A structure containing information for the requested process
     */
    public static SolarisLibc.SolarisPsInfo queryPsInfo(int pid) {
        return new SolarisLibc.SolarisPsInfo(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/psinfo", pid)));
    }

    /**
     * Reads /proc/pid/lwp/tid/lwpsinfo and returns data in a structure
     *
     * @param pid The process ID
     * @param tid The thread ID (lwpid)
     * @return A structure containing information for the requested thread
     */
    public static SolarisLibc.SolarisLwpsInfo queryLwpsInfo(int pid, int tid) {
        return new SolarisLibc.SolarisLwpsInfo(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/lwp/%d/lwpsinfo", pid, tid)));
    }

    /**
     * Reads /proc/pid/usage and returns data in a structure
     *
     * @param pid The process ID
     * @return A structure containing information for the requested process
     */
    public static SolarisLibc.SolarisPrUsage queryPrUsage(int pid) {
        return new SolarisLibc.SolarisPrUsage(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/usage", pid)));
    }

    /**
     * Reads /proc/pid/lwp/tid/usage and returns data in a structure
     *
     * @param pid The process ID
     * @param tid The thread ID (lwpid)
     * @return A structure containing information for the requested thread
     */
    public static SolarisLibc.SolarisPrUsage queryPrUsage(int pid, int tid) {
        return new SolarisLibc.SolarisPrUsage(
                Builder.readAllBytesAsBuffer(String.format(Locale.ROOT, "/proc/%d/lwp/%d/usage", pid, tid)));
    }

    /**
     * Reads the pr_argc, pr_argv, pr_envp, and pr_dmodel fields from /proc/pid/psinfo
     *
     * @param pid    The process ID
     * @param psinfo A populated {@link SolarisLibc.SolarisPsInfo} structure containing the offset pointers for these
     *               fields
     * @return A quartet containing the argc, argv, envp and dmodel values, or null if unable to read
     */
    public static Tuple queryArgsEnvAddrs(int pid, SolarisLibc.SolarisPsInfo psinfo) {
        if (psinfo != null) {
            int argc = psinfo.pr_argc;
            // Must have at least one argc (the command itself) so failure here means exit
            if (argc > 0) {
                long argv = Pointer.nativeValue(psinfo.pr_argv);
                long envp = Pointer.nativeValue(psinfo.pr_envp);
                // Process data model 1 = 32 bit, 2 = 64 bit
                byte dmodel = psinfo.pr_dmodel;
                // Sanity check
                if (dmodel * 4 == (envp - argv) / (argc + 1)) {
                    return new Tuple(argc, argv, envp, dmodel);
                }
                Logger.trace("Failed data model and offset increment sanity check: dm={} diff={}", dmodel, envp - argv);
                return null;
            }
            Logger.trace("Failed argc sanity check: argc={}", argc);
            return null;
        }
        Logger.trace("Failed to read psinfo file for pid: {} ", pid);
        return null;
    }

    /**
     * Read the argument and environment strings from process address space
     *
     * @param pid    the process id
     * @param psinfo A populated {@link SolarisLibc.SolarisPsInfo} structure containing the offset pointers for these
     *               fields
     * @return A pair containing a list of the arguments and a map of environment variables
     */
    public static Pair<List<String>, Map<String, String>> queryArgsEnv(int pid, SolarisLibc.SolarisPsInfo psinfo) {
        List<String> args = new ArrayList<>();
        Map<String, String> env = new LinkedHashMap<>();

        // Get the arg count and list of env vars
        Tuple addrs = queryArgsEnvAddrs(pid, psinfo);
        if (addrs != null) {
            // Open a file descriptor to the address space
            String procas = "/proc/" + pid + "/as";
            int fd = LIBC.open(procas, 0);
            if (fd < 0) {
                Logger.trace("No permission to read file: {} ", procas);
                return Pair.of(args, env);
            }
            try {
                // Non-null addrs means argc > 0
                int argc = addrs.get(0);
                long argv = addrs.get(1);
                long envp = addrs.get(2);
                long increment = ((byte) addrs.get(3)) * 4L;

                // Reusable buffer
                long bufStart = 0;
                try (Memory buffer = new Memory(PAGE_SIZE * 2)) {
                    size_t bufSize = new size_t(buffer.size());

                    // Read the pointers to the arg strings
                    // We know argc so we can count them
                    long[] argp = new long[argc];
                    long offset = argv;
                    for (int i = 0; i < argc; i++) {
                        bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, offset);
                        argp[i] = bufStart == 0 ? 0 : getOffsetFromBuffer(buffer, offset - bufStart, increment);
                        offset += increment;
                    }

                    // Also read the pointers to the env strings
                    // We don't know how many, so stop when we get to null pointer
                    List<Long> envPtrList = new ArrayList<>();
                    offset = envp;
                    long addr = 0;
                    int limit = 500; // sane max env strings to stop at
                    do {
                        bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, offset);
                        addr = bufStart == 0 ? 0 : getOffsetFromBuffer(buffer, offset - bufStart, increment);
                        if (addr != 0) {
                            envPtrList.add(addr);
                        }
                        offset += increment;
                    } while (addr != 0 && --limit > 0);

                    // Now read the arg strings from the buffer
                    for (int i = 0; i < argp.length && argp[i] != 0; i++) {
                        bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, argp[i]);
                        if (bufStart != 0) {
                            String argStr = buffer.getString(argp[i] - bufStart);
                            if (!argStr.isEmpty()) {
                                args.add(argStr);
                            }
                        }
                    }

                    // And now read the env strings from the buffer
                    for (Long envPtr : envPtrList) {
                        bufStart = conditionallyReadBufferFromStartOfPage(fd, buffer, bufSize, bufStart, envPtr);
                        if (bufStart != 0) {
                            String envStr = buffer.getString(envPtr - bufStart);
                            int idx = envStr.indexOf(Symbol.C_EQUAL);
                            if (idx > 0) {
                                env.put(envStr.substring(0, idx), envStr.substring(idx + 1));
                            }
                        }
                    }
                }
            } finally {
                LIBC.close(fd);
            }
        }
        return Pair.of(args, env);
    }

    /**
     * Reads the page containing addr into buffer, unless the buffer already contains that page (as indicated by the
     * bufStart address), in which case nothing is changed.
     *
     * @param fd       The file descriptor for the address space
     * @param buffer   An allocated buffer, possibly with data reread from bufStart
     * @param bufSize  The size of the buffer
     * @param bufStart The start of data currently in bufStart, or 0 if uninitialized
     * @param addr     THe address whose page to read into the buffer
     * @return The new starting pointer for the buffer
     */
    private static long conditionallyReadBufferFromStartOfPage(int fd, Memory buffer, size_t bufSize, long bufStart,
            long addr) {
        // If we don't have the right buffer, update it
        if (addr < bufStart || addr - bufStart > PAGE_SIZE) {
            long newStart = Math.floorDiv(addr, PAGE_SIZE) * PAGE_SIZE;
            ssize_t result = LIBC.pread(fd, buffer, bufSize, new NativeLong(newStart));
            // May return less than asked but should be at least a full page
            if (result.longValue() < PAGE_SIZE) {
                Logger.debug("Failed to read page from address space: {} bytes read", result.longValue());
                return 0;
            }
            return newStart;
        }
        return bufStart;
    }

    private static long getOffsetFromBuffer(Memory buffer, long offset, long increment) {
        return increment == 8 ? buffer.getLong(offset) : buffer.getInt(offset);
    }

}
