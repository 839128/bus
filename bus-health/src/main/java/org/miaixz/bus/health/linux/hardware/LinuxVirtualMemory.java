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
package org.miaixz.bus.health.linux.hardware;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;
import org.miaixz.bus.health.linux.ProcPath;

/**
 * Memory obtained by /proc/meminfo and /proc/vmstat
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class LinuxVirtualMemory extends AbstractVirtualMemory {

    private final LinuxGlobalMemory global;

    private final Supplier<Triplet<Long, Long, Long>> usedTotalCommitLim = Memoizer
            .memoize(LinuxVirtualMemory::queryMemInfo, Memoizer.defaultExpiration());

    private final Supplier<Pair<Long, Long>> inOut = Memoizer.memoize(LinuxVirtualMemory::queryVmStat,
            Memoizer.defaultExpiration());

    /**
     * Constructor for LinuxVirtualMemory.
     *
     * @param linuxGlobalMemory The parent global memory class instantiating this
     */
    LinuxVirtualMemory(LinuxGlobalMemory linuxGlobalMemory) {
        this.global = linuxGlobalMemory;
    }

    private static Triplet<Long, Long, Long> queryMemInfo() {
        long swapFree = 0L;
        long swapTotal = 0L;
        long commitLimit = 0L;

        List<String> procMemInfo = Builder.readFile(ProcPath.MEMINFO);
        for (String checkLine : procMemInfo) {
            String[] memorySplit = Pattern.SPACES_PATTERN.split(checkLine);
            if (memorySplit.length > 1) {
                switch (memorySplit[0]) {
                case "SwapTotal:":
                    swapTotal = parseMeminfo(memorySplit);
                    break;
                case "SwapFree:":
                    swapFree = parseMeminfo(memorySplit);
                    break;
                case "CommitLimit:":
                    commitLimit = parseMeminfo(memorySplit);
                    break;
                default:
                    // do nothing with other lines
                    break;
                }
            }
        }
        return Triplet.of(swapTotal - swapFree, swapTotal, commitLimit);
    }

    private static Pair<Long, Long> queryVmStat() {
        long swapPagesIn = 0L;
        long swapPagesOut = 0L;
        List<String> procVmStat = Builder.readFile(ProcPath.VMSTAT);
        for (String checkLine : procVmStat) {
            String[] memorySplit = Pattern.SPACES_PATTERN.split(checkLine);
            if (memorySplit.length > 1) {
                switch (memorySplit[0]) {
                case "pswpin":
                    swapPagesIn = Parsing.parseLongOrDefault(memorySplit[1], 0L);
                    break;
                case "pswpout":
                    swapPagesOut = Parsing.parseLongOrDefault(memorySplit[1], 0L);
                    break;
                default:
                    // do nothing with other lines
                    break;
                }
            }
        }
        return Pair.of(swapPagesIn, swapPagesOut);
    }

    /**
     * Parses lines from the display of /proc/meminfo
     *
     * @param memorySplit Array of Strings representing the 3 columns of /proc/meminfo
     * @return value, multiplied by 1024 if kB is specified
     */
    private static long parseMeminfo(String[] memorySplit) {
        if (memorySplit.length < 2) {
            return 0L;
        }
        long memory = Parsing.parseLongOrDefault(memorySplit[1], 0L);
        if (memorySplit.length > 2 && "kB".equals(memorySplit[2])) {
            memory *= 1024;
        }
        return memory;
    }

    @Override
    public long getSwapUsed() {
        return usedTotalCommitLim.get().getLeft();
    }

    @Override
    public long getSwapTotal() {
        return usedTotalCommitLim.get().getMiddle();
    }

    @Override
    public long getVirtualMax() {
        return usedTotalCommitLim.get().getRight();
    }

    @Override
    public long getVirtualInUse() {
        return this.global.getTotal() - this.global.getAvailable() + getSwapUsed();
    }

    @Override
    public long getSwapPagesIn() {
        return inOut.get().getLeft();
    }

    @Override
    public long getSwapPagesOut() {
        return inOut.get().getRight();
    }

}
