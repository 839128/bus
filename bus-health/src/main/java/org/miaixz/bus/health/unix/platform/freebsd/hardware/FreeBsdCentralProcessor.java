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
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import java.util.*;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.common.AbstractCentralProcessor;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.unix.jna.FreeBsdLibc;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.LibCAPI.size_t;

/**
 * A CPU
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class FreeBsdCentralProcessor extends AbstractCentralProcessor {

    // Capture the CSV of hex values as group(1), clients should split on ','
    private static final java.util.regex.Pattern CPUMASK = java.util.regex.Pattern
            .compile(".*<cpu\\s.*mask=\"(\\p{XDigit}+(,\\p{XDigit}+)*)\".*>.*</cpu>.*");

    private static final long CPTIME_SIZE;

    static {
        try (FreeBsdLibc.CpTime cpTime = new FreeBsdLibc.CpTime()) {
            CPTIME_SIZE = cpTime.size();
        }
    }

    private static List<CentralProcessor.LogicalProcessor> parseTopology() {
        String[] topology = BsdSysctlKit.sysctl("kern.sched.topology_spec", Normal.EMPTY).split("[\\n\\r]");
        /*-
         * Sample output:
         *
        <groups>
        <group level="1" cache-level="0">
         <cpu count="24" mask="ffffff">0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23</cpu>
         <children>
          <group level="2" cache-level="2">
           <cpu count="12" mask="fff">0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11</cpu>
           <children>
            <group level="3" cache-level="1">
             <cpu count="2" mask="3">0, 1</cpu>
             <flags><flag name="THREAD">THREAD group</flag><flag name="SMT">SMT group</flag></flags>
            </group>
            ...
        * On FreeBSD 13.1, the output may contain a csv value for the mask:
        <groups>
         <group level="1" cache-level="3">
          <cpu count="8" mask="ff,0,0,0">0, 1, 2, 3, 4, 5, 6, 7</cpu>
          <children>
           <group level="2" cache-level="2">
            <cpu count="2" mask="3,0,0,0">0, 1</cpu>
            ...
        *
        * Opens with <groups>
        * <group> level 1 identifies all the processors via bitmask, should only be one
        * <group> level 2 separates by physical package
        * <group> level 3 puts hyperthreads together: if THREAD or SMT or HTT all the CPUs are one physical
        * If there is no level 3, then all logical processors are physical
        */
        // Create lists of the group bitmasks
        long group1 = 1L;
        List<Long> group2 = new ArrayList<>();
        List<Long> group3 = new ArrayList<>();
        int groupLevel = 0;
        for (String topo : topology) {
            if (topo.contains("<group level=")) {
                groupLevel++;
            } else if (topo.contains("</group>")) {
                groupLevel--;
            } else if (topo.contains("<cpu")) {
                // Find <cpu> tag and extract bits
                Matcher m = CPUMASK.matcher(topo);
                if (m.matches()) {
                    // If csv of hex values like "f,0,0,0", parse the first value
                    String csvMatch = m.group(1);
                    String[] csvTokens = csvMatch.split(Symbol.COMMA);
                    String firstVal = csvTokens[0];

                    // Regex guarantees parsing digits so we won't get a
                    // NumberFormatException
                    long parsedVal = Parsing.hexStringToLong(firstVal, 0);
                    switch (groupLevel) {
                    case 1:
                        group1 = parsedVal;
                        break;
                    case 2:
                        group2.add(parsedVal);
                        break;
                    case 3:
                        group3.add(parsedVal);
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        return matchBitmasks(group1, group2, group3);
    }

    private static List<CentralProcessor.LogicalProcessor> matchBitmasks(long group1, List<Long> group2,
            List<Long> group3) {
        List<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<>();
        // Lowest and Highest set bits, indexing from 0
        int lowBit = Long.numberOfTrailingZeros(group1);
        int hiBit = 63 - Long.numberOfLeadingZeros(group1);
        // Create logical processors for this core
        for (int i = lowBit; i <= hiBit; i++) {
            if ((group1 & (1L << i)) > 0) {
                int numaNode = 0;
                CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(i,
                        getMatchingBitmask(group3, i), getMatchingBitmask(group2, i), numaNode);
                logProcs.add(logProc);
            }
        }
        return logProcs;
    }

    /**
     * Fetches the ProcessorID from dmidecode (if possible with root permissions), otherwise uses the values from
     * /var/run/dmesg.boot
     *
     * @param processorID The processorID as a long
     * @return The ProcessorID string
     */
    private static String getProcessorIDfromDmiDecode(long processorID) {
        boolean procInfo = false;
        String marker = "Processor Information";
        for (String checkLine : Executor.runNative("dmidecode -t system")) {
            if (!procInfo && checkLine.contains(marker)) {
                marker = "ID:";
                procInfo = true;
            } else if (procInfo && checkLine.contains(marker)) {
                return checkLine.split(marker)[1].trim();
            }
        }
        // If we've gotten this far, dmidecode failed. Used the passed-in values
        return String.format(Locale.ROOT, "%016X", processorID);
    }

    private static int getMatchingBitmask(List<Long> bitmasks, int lp) {
        for (int j = 0; j < bitmasks.size(); j++) {
            if ((bitmasks.get(j).longValue() & (1L << lp)) != 0) {
                return j;
            }
        }
        return 0;
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        final java.util.regex.Pattern identifierPattern = java.util.regex.Pattern
                .compile("Origin=\"([^\"]*)\".*Id=(\\S+).*Family=(\\S+).*Model=(\\S+).*Stepping=(\\S+).*");
        final java.util.regex.Pattern featuresPattern = java.util.regex.Pattern.compile("Features=(\\S+)<.*");

        String cpuVendor = Normal.EMPTY;
        String cpuName = BsdSysctlKit.sysctl("hw.model", Normal.EMPTY);
        String cpuFamily = Normal.EMPTY;
        String cpuModel = Normal.EMPTY;
        String cpuStepping = Normal.EMPTY;
        String processorID;
        long cpuFreq = BsdSysctlKit.sysctl("hw.clockrate", 0L) * 1_000_000L;

        boolean cpu64bit;

        // Parsing dmesg.boot is apparently the only reliable source for processor
        // identification in FreeBSD
        long processorIdBits = 0L;
        List<String> cpuInfo = Builder.readFile("/var/run/dmesg.boot");
        for (String line : cpuInfo) {
            line = line.trim();
            // Prefer hw.model to this one
            if (line.startsWith("CPU:") && cpuName.isEmpty()) {
                cpuName = line.replace("CPU:", Normal.EMPTY).trim();
            } else if (line.startsWith("Origin=")) {
                Matcher m = identifierPattern.matcher(line);
                if (m.matches()) {
                    cpuVendor = m.group(1);
                    processorIdBits |= Long.decode(m.group(2));
                    cpuFamily = Integer.decode(m.group(3)).toString();
                    cpuModel = Integer.decode(m.group(4)).toString();
                    cpuStepping = Integer.decode(m.group(5)).toString();
                }
            } else if (line.startsWith("Features=")) {
                Matcher m = featuresPattern.matcher(line);
                if (m.matches()) {
                    processorIdBits |= Long.decode(m.group(1)) << 32;
                }
                // No further interest in this file
                break;
            }
        }
        cpu64bit = Executor.getFirstAnswer("uname -m").trim().contains("64");
        processorID = getProcessorIDfromDmiDecode(processorIdBits);

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping,
                processorID, cpu64bit, cpuFreq);
    }

    @Override
    protected Tuple initProcessorCounts() {
        List<CentralProcessor.LogicalProcessor> logProcs = parseTopology();
        // Force at least one processor
        if (logProcs.isEmpty()) {
            logProcs.add(new CentralProcessor.LogicalProcessor(0, 0, 0));
        }
        Map<Integer, String> dmesg = new HashMap<>();
        // cpu0: <Open Firmware CPU> on cpulist0
        java.util.regex.Pattern normal = java.util.regex.Pattern.compile("cpu(\\\\d+): (.+) on .*");
        // CPU 0: ARM Cortex-A53 r0p4 affinity: 0 0
        java.util.regex.Pattern hybrid = java.util.regex.Pattern.compile("CPU\\\\s*(\\\\d+): (.+) affinity:.*");
        List<String> featureFlags = new ArrayList<>();
        boolean readingFlags = false;
        for (String s : Builder.readFile("/var/run/dmesg.boot")) {
            Matcher h = hybrid.matcher(s);
            if (h.matches()) {
                int coreId = Parsing.parseIntOrDefault(h.group(1), 0);
                // This always takes priority, overwrite if needed
                dmesg.put(coreId, h.group(2).trim());
            } else {
                Matcher n = normal.matcher(s);
                if (n.matches()) {
                    int coreId = Parsing.parseIntOrDefault(n.group(1), 0);
                    // Don't overwrite if h matched earlier
                    dmesg.putIfAbsent(coreId, n.group(2).trim());
                }
            }
            if (s.contains("Origin=")) {
                readingFlags = true;
            } else if (readingFlags) {
                if (s.startsWith("  ")) {
                    featureFlags.add(s.trim());
                } else {
                    readingFlags = false;
                }
            }
        }
        List<CentralProcessor.PhysicalProcessor> physProcs = dmesg.isEmpty() ? null
                : createProcListFromDmesg(logProcs, dmesg);
        List<CentralProcessor.ProcessorCache> caches = getCacheInfoFromLscpu();
        return new Tuple(logProcs, physProcs, caches, featureFlags);
    }

    private List<CentralProcessor.ProcessorCache> getCacheInfoFromLscpu() {
        Set<CentralProcessor.ProcessorCache> caches = new HashSet<>();
        for (String checkLine : Executor.runNative("lscpu")) {
            if (checkLine.contains("L1d cache:")) {
                caches.add(new CentralProcessor.ProcessorCache(1, 0, 0,
                        Parsing.parseDecimalMemorySizeToBinary(checkLine.split(Symbol.COLON)[1].trim()),
                        CentralProcessor.ProcessorCache.Type.DATA));
            } else if (checkLine.contains("L1i cache:")) {
                caches.add(new CentralProcessor.ProcessorCache(1, 0, 0,
                        Parsing.parseDecimalMemorySizeToBinary(checkLine.split(Symbol.COLON)[1].trim()),
                        CentralProcessor.ProcessorCache.Type.INSTRUCTION));
            } else if (checkLine.contains("L2 cache:")) {
                caches.add(new CentralProcessor.ProcessorCache(2, 0, 0,
                        Parsing.parseDecimalMemorySizeToBinary(checkLine.split(Symbol.COLON)[1].trim()),
                        CentralProcessor.ProcessorCache.Type.UNIFIED));
            } else if (checkLine.contains("L3 cache:")) {
                caches.add(new CentralProcessor.ProcessorCache(3, 0, 0,
                        Parsing.parseDecimalMemorySizeToBinary(checkLine.split(Symbol.COLON)[1].trim()),
                        CentralProcessor.ProcessorCache.Type.UNIFIED));
            }
        }
        return orderedProcCaches(caches);
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        try (FreeBsdLibc.CpTime cpTime = new FreeBsdLibc.CpTime()) {
            BsdSysctlKit.sysctl("kern.cp_time", cpTime);
            ticks[CentralProcessor.TickType.USER.getIndex()] = cpTime.cpu_ticks[FreeBsdLibc.CP_USER];
            ticks[CentralProcessor.TickType.NICE.getIndex()] = cpTime.cpu_ticks[FreeBsdLibc.CP_NICE];
            ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = cpTime.cpu_ticks[FreeBsdLibc.CP_SYS];
            ticks[CentralProcessor.TickType.IRQ.getIndex()] = cpTime.cpu_ticks[FreeBsdLibc.CP_INTR];
            ticks[CentralProcessor.TickType.IDLE.getIndex()] = cpTime.cpu_ticks[FreeBsdLibc.CP_IDLE];
        }
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        long[] freq = new long[1];
        freq[0] = BsdSysctlKit.sysctl("dev.cpu.0.freq", -1L);
        if (freq[0] > 0) {
            // If success, value is in MHz
            freq[0] *= 1_000_000L;
        } else {
            freq[0] = BsdSysctlKit.sysctl("machdep.tsc_freq", -1L);
        }
        return freq;
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = FreeBsdLibc.INSTANCE.getloadavg(average, nelem);
        if (retval < nelem) {
            for (int i = Math.max(retval, 0); i < average.length; i++) {
                average[i] = -1d;
            }
        }
        return average;
    }

    @Override
    public long queryMaxFreq() {
        long max = -1L;
        String freqLevels = BsdSysctlKit.sysctl("dev.cpu.0.freq_levels", Normal.EMPTY);
        // MHz/Watts pairs like: 2501/32000 2187/27125 2000/24000
        for (String s : Pattern.SPACES_PATTERN.split(freqLevels)) {
            long freq = Parsing.parseLongOrDefault(s.split("/")[0], -1L);
            if (max < freq) {
                max = freq;
            }
        }
        if (max > 0) {
            // If success, value is in MHz
            max *= 1_000_000;
        } else {
            max = BsdSysctlKit.sysctl("machdep.tsc_freq", -1L);
        }
        return max;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = new long[getLogicalProcessorCount()][CentralProcessor.TickType.values().length];

        // Allocate memory for array of CPTime
        long arraySize = CPTIME_SIZE * getLogicalProcessorCount();
        try (Memory p = new Memory(arraySize);
                ByRef.CloseableSizeTByReference oldlenp = new ByRef.CloseableSizeTByReference(arraySize)) {
            String name = "kern.cp_times";
            // Fetch
            if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, p, oldlenp, null, size_t.ZERO)) {
                Logger.error("Failed sysctl call: {}, Error code: {}", name, Native.getLastError());
                return ticks;
            }
            // p now points to the data; need to copy each element
            for (int cpu = 0; cpu < getLogicalProcessorCount(); cpu++) {
                ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = p
                        .getLong(CPTIME_SIZE * cpu + FreeBsdLibc.CP_USER * FreeBsdLibc.UINT64_SIZE); // lgtm
                ticks[cpu][CentralProcessor.TickType.NICE.getIndex()] = p
                        .getLong(CPTIME_SIZE * cpu + FreeBsdLibc.CP_NICE * FreeBsdLibc.UINT64_SIZE); // lgtm
                ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = p
                        .getLong(CPTIME_SIZE * cpu + FreeBsdLibc.CP_SYS * FreeBsdLibc.UINT64_SIZE); // lgtm
                ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()] = p
                        .getLong(CPTIME_SIZE * cpu + FreeBsdLibc.CP_INTR * FreeBsdLibc.UINT64_SIZE); // lgtm
                ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = p
                        .getLong(CPTIME_SIZE * cpu + FreeBsdLibc.CP_IDLE * FreeBsdLibc.UINT64_SIZE); // lgtm
            }
        }
        return ticks;
    }

    @Override
    public long queryContextSwitches() {
        String name = "vm.stats.sys.v_swtch";
        size_t.ByReference size = new size_t.ByReference(new size_t(FreeBsdLibc.INT_SIZE));
        try (Memory p = new Memory(size.longValue())) {
            if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, p, size, null, size_t.ZERO)) {
                return 0L;
            }
            return Parsing.unsignedIntToLong(p.getInt(0));
        }
    }

    @Override
    public long queryInterrupts() {
        String name = "vm.stats.sys.v_intr";
        size_t.ByReference size = new size_t.ByReference(new size_t(FreeBsdLibc.INT_SIZE));
        try (Memory p = new Memory(size.longValue())) {
            if (0 != FreeBsdLibc.INSTANCE.sysctlbyname(name, p, size, null, size_t.ZERO)) {
                return 0L;
            }
            return Parsing.unsignedIntToLong(p.getInt(0));
        }
    }

}
