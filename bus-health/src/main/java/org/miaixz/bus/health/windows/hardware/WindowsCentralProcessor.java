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
package org.miaixz.bus.health.windows.hardware;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.common.AbstractCentralProcessor;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.LogicalProcessorInformation;
import org.miaixz.bus.health.windows.driver.perfmon.LoadAverage;
import org.miaixz.bus.health.windows.driver.perfmon.ProcessorInformation;
import org.miaixz.bus.health.windows.driver.perfmon.ProcessorInformation.InterruptsProperty;
import org.miaixz.bus.health.windows.driver.perfmon.ProcessorInformation.ProcessorFrequencyProperty;
import org.miaixz.bus.health.windows.driver.perfmon.ProcessorInformation.ProcessorTickCountProperty;
import org.miaixz.bus.health.windows.driver.perfmon.ProcessorInformation.ProcessorUtilityTickCountProperty;
import org.miaixz.bus.health.windows.driver.perfmon.SystemInformation;
import org.miaixz.bus.health.windows.driver.perfmon.SystemInformation.ContextSwitchProperty;
import org.miaixz.bus.health.windows.driver.wmi.Win32Processor;
import org.miaixz.bus.health.windows.driver.wmi.Win32Processor.ProcessorIdProperty;
import org.miaixz.bus.health.windows.jna.Kernel32;
import org.miaixz.bus.health.windows.jna.PowrProf;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.PowrProf.POWER_INFORMATION_LEVEL;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * A CPU, representing all of a system's processors. It may contain multiple individual Physical and Logical processors.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class WindowsCentralProcessor extends AbstractCentralProcessor {

    // Whether to use Processor counters rather than sum up Processor Information counters
    private static final boolean USE_LEGACY_SYSTEM_COUNTERS = Config.get(Config._WINDOWS_LEGACY_SYSTEM_COUNTERS, false);

    // Whether to match task manager using Processor Utility ticks
    private static final boolean USE_CPU_UTILITY = VersionHelpers.IsWindows8OrGreater()
            && Config.get(Config._WINDOWS_CPU_UTILITY, false);

    // Whether to start a daemon thread to calculate load average
    private static final boolean USE_LOAD_AVERAGE = Config.get(Config._WINDOWS_LOADAVERAGE, false);

    static {
        if (USE_LOAD_AVERAGE) {
            LoadAverage.startDaemon();
        }
    }

    // This tick query is memoized to enforce a minimum elapsed time for determining
    // the capacity base multiplier
    private final Supplier<Pair<List<String>, Map<ProcessorUtilityTickCountProperty, List<Long>>>> processorUtilityCounters = USE_CPU_UTILITY
            ? Memoizer.memoize(WindowsCentralProcessor::queryProcessorUtilityCounters,
                    TimeUnit.MILLISECONDS.toNanos(300L))
            : null;
    // populated by initProcessorCounts called by the parent constructor
    private Map<String, Integer> numaNodeProcToLogicalProcMap;
    // Store the initial query and start the memoizer expiration
    private Map<ProcessorUtilityTickCountProperty, List<Long>> initialUtilityCounters = USE_CPU_UTILITY
            ? processorUtilityCounters.get().getRight()
            : null;
    // Lazily initialized
    private Long utilityBaseMultiplier = null;

    /**
     * Parses identifier string
     *
     * @param identifier the full identifier string
     * @param key        the key to retrieve
     * @return the string following id
     */
    private static String parseIdentifier(String identifier, String key) {
        String[] idSplit = Pattern.SPACES_PATTERN.split(identifier);
        boolean found = false;
        for (String s : idSplit) {
            // If key string found, return next value
            if (found) {
                return s;
            }
            found = s.equals(key);
        }
        // If key string not found, return empty string
        return Normal.EMPTY;
    }

    private static Pair<List<String>, Map<ProcessorUtilityTickCountProperty, List<Long>>> queryProcessorUtilityCounters() {
        return ProcessorInformation.queryProcessorCapacityCounters();
    }

    /**
     * Initializes Class variables
     */
    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String cpuVendor = Normal.EMPTY;
        String cpuName = Normal.EMPTY;
        String cpuIdentifier = Normal.EMPTY;
        String cpuFamily = Normal.EMPTY;
        String cpuModel = Normal.EMPTY;
        String cpuStepping = Normal.EMPTY;
        long cpuVendorFreq = 0L;
        String processorID;
        boolean cpu64bit = false;

        final String cpuRegistryRoot = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\";
        String[] processorIds = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryRoot);
        if (processorIds.length > 0) {
            String cpuRegistryPath = cpuRegistryRoot + processorIds[0];
            cpuVendor = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                    "VendorIdentifier");
            cpuName = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                    "ProcessorNameString");
            cpuIdentifier = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                    "Identifier");
            try {
                cpuVendorFreq = Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath, "~MHz")
                        * 1_000_000L;
            } catch (Win32Exception e) {
                // Leave as 0, parse the identifier as backup
            }
        }
        if (!cpuIdentifier.isEmpty()) {
            cpuFamily = parseIdentifier(cpuIdentifier, "Family");
            cpuModel = parseIdentifier(cpuIdentifier, "Model");
            cpuStepping = parseIdentifier(cpuIdentifier, "Stepping");
        }
        try (Struct.CloseableSystemInfo sysinfo = new Struct.CloseableSystemInfo()) {
            Kernel32.INSTANCE.GetNativeSystemInfo(sysinfo);
            int processorArchitecture = sysinfo.processorArchitecture.pi.wProcessorArchitecture.intValue();
            if (processorArchitecture == 9 // PROCESSOR_ARCHITECTURE_AMD64
                    || processorArchitecture == 12 // PROCESSOR_ARCHITECTURE_ARM64
                    || processorArchitecture == 6) { // PROCESSOR_ARCHITECTURE_IA64
                cpu64bit = true;
            }
        }
        WmiResult<ProcessorIdProperty> processorId = Win32Processor.queryProcessorId();
        if (processorId.getResultCount() > 0) {
            processorID = WmiKit.getString(processorId, ProcessorIdProperty.PROCESSORID, 0);
        } else {
            processorID = createProcessorID(cpuStepping, cpuModel, cpuFamily,
                    cpu64bit ? new String[] { "ia64" } : new String[0]);
        }
        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping,
                processorID, cpu64bit, cpuVendorFreq);
    }

    @Override
    protected Tuple initProcessorCounts() {
        Triplet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>> lpi;
        if (VersionHelpers.IsWindows7OrGreater()) {
            lpi = LogicalProcessorInformation.getLogicalProcessorInformationEx();
            // Save numaNode,Processor lookup for future PerfCounter instance lookup
            // The processor number is based on the Processor Group, so we keep a separate
            // index by NUMA node.
            int curNode = -1;
            int procNum = 0;
            // 0-indexed list of all lps for array lookup
            int lp = 0;
            this.numaNodeProcToLogicalProcMap = new HashMap<>();
            for (CentralProcessor.LogicalProcessor logProc : lpi.getLeft()) {
                int node = logProc.getNumaNode();
                // This list is grouped by NUMA node so a change in node will reset this counter
                if (node != curNode) {
                    curNode = node;
                    procNum = 0;
                }
                numaNodeProcToLogicalProcMap.put(String.format(Locale.ROOT, "%d,%d", logProc.getNumaNode(), procNum++),
                        lp++);
            }
        } else {
            lpi = LogicalProcessorInformation.getLogicalProcessorInformation();
        }
        List<String> featureFlags = Arrays.stream(Kernel32.ProcessorFeature.values())
                .filter(f -> Kernel32.INSTANCE.IsProcessorFeaturePresent(f.value()))
                .map(Kernel32.ProcessorFeature::name).collect(Collectors.toList());
        return new Tuple(lpi.getLeft(), lpi.getMiddle(), lpi.getRight(), featureFlags);
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[TickType.values().length];
        if (USE_LEGACY_SYSTEM_COUNTERS) {
            WinBase.FILETIME lpIdleTime = new WinBase.FILETIME();
            WinBase.FILETIME lpKernelTime = new WinBase.FILETIME();
            WinBase.FILETIME lpUserTime = new WinBase.FILETIME();
            if (!Kernel32.INSTANCE.GetSystemTimes(lpIdleTime, lpKernelTime, lpUserTime)) {
                Logger.error("Failed to update system idle/kernel/user times. Error code: {}", Native.getLastError());
                return ticks;
            }
            // IOwait:
            // Windows does not measure IOWait.

            // IRQ and ticks:
            // Percent time raw value is cumulative 100NS-ticks
            // Divide by 10_000 to get milliseconds
            Map<ProcessorInformation.SystemTickCountProperty, Long> valueMap = ProcessorInformation
                    .querySystemCounters();
            ticks[TickType.IRQ.getIndex()] = valueMap
                    .getOrDefault(ProcessorInformation.SystemTickCountProperty.PERCENTINTERRUPTTIME, 0L) / 10_000L;
            ticks[TickType.SOFTIRQ.getIndex()] = valueMap
                    .getOrDefault(ProcessorInformation.SystemTickCountProperty.PERCENTDPCTIME, 0L) / 10_000L;

            ticks[TickType.IDLE.getIndex()] = lpIdleTime.toDWordLong().longValue() / 10_000L;
            ticks[TickType.SYSTEM.getIndex()] = lpKernelTime.toDWordLong().longValue() / 10_000L
                    - ticks[TickType.IDLE.getIndex()];
            ticks[TickType.USER.getIndex()] = lpUserTime.toDWordLong().longValue() / 10_000L;
            // Additional decrement to avoid double counting in the total array
            ticks[TickType.SYSTEM.getIndex()] -= ticks[TickType.IRQ.getIndex()] + ticks[TickType.SOFTIRQ.getIndex()];
            return ticks;
        }
        // To get load in processor group scenario, we need perfmon counters, but the
        // _Total instance is an average rather than total (scaled) number of ticks
        // which matches GetSystemTimes() results. We can just query the per-processor
        // ticks and add them up. Calling the get() method gains the benefit of
        // synchronizing this output with the memoized result of per-processor ticks as
        // well.
        // Sum processor ticks
        long[][] procTicks = getProcessorCpuLoadTicks();
        for (int i = 0; i < ticks.length; i++) {
            for (long[] procTick : procTicks) {
                ticks[i] += procTick[i];
            }
        }
        return ticks;
    }

    @Override
    public long queryMaxFreq() {
        long[] freqs = queryNTPower(1); // Max is field index 1
        return Arrays.stream(freqs).max().orElse(-1L);
    }

    @Override
    public long[] queryCurrentFreq() {
        if (VersionHelpers.IsWindows7OrGreater()) {
            Pair<List<String>, Map<ProcessorFrequencyProperty, List<Long>>> instanceValuePair = ProcessorInformation
                    .queryFrequencyCounters();
            List<String> instances = instanceValuePair.getLeft();
            Map<ProcessorFrequencyProperty, List<Long>> valueMap = instanceValuePair.getRight();
            List<Long> percentMaxList = valueMap.get(ProcessorFrequencyProperty.PERCENTOFMAXIMUMFREQUENCY);
            if (!instances.isEmpty()) {
                long maxFreq = this.getMaxFreq();
                long[] freqs = new long[getLogicalProcessorCount()];
                for (String instance : instances) {
                    int cpu = instance.contains(Symbol.COMMA) ? numaNodeProcToLogicalProcMap.getOrDefault(instance, 0)
                            : Parsing.parseIntOrDefault(instance, 0);
                    if (cpu >= getLogicalProcessorCount()) {
                        continue;
                    }
                    freqs[cpu] = percentMaxList.get(cpu) * maxFreq / 100L;
                }
                return freqs;
            }
        }
        // If <Win7 or anything failed in PDH/WMI, use the native call
        return queryNTPower(2); // Current is field index 2
    }

    /**
     * Call CallNTPowerInformation for Processor information and return an array of the specified index
     *
     * @param fieldIndex The field
     * @return The array of values.
     */
    private long[] queryNTPower(int fieldIndex) {
        PowrProf.ProcessorPowerInformation ppi = new PowrProf.ProcessorPowerInformation();
        PowrProf.ProcessorPowerInformation[] ppiArray = (PowrProf.ProcessorPowerInformation[]) ppi
                .toArray(getLogicalProcessorCount());
        long[] freqs = new long[getLogicalProcessorCount()];
        if (0 != PowrProf.INSTANCE.CallNtPowerInformation(POWER_INFORMATION_LEVEL.ProcessorInformation, null, 0,
                ppiArray[0].getPointer(), ppi.size() * ppiArray.length)) {
            Logger.error("Unable to get Processor Information");
            Arrays.fill(freqs, -1L);
            return freqs;
        }
        for (int i = 0; i < freqs.length; i++) {
            if (fieldIndex == 1) { // Max
                freqs[i] = ppiArray[i].maxMhz * 1_000_000L;
            } else if (fieldIndex == 2) { // Current
                freqs[i] = ppiArray[i].currentMhz * 1_000_000L;
            } else {
                freqs[i] = -1L;
            }
            // In Win11 23H2 CallNtPowerInformation returns all 0's so use vendor freq
            if (freqs[i] == 0) {
                freqs[i] = getProcessorIdentifier().getVendorFreq();
            }
        }
        return freqs;
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        return LoadAverage.queryLoadAverage(nelem);
    }

    /**
     * Lazily calculate the capacity tick multiplier once.
     *
     * @param deltaBase The difference in base ticks.
     * @param deltaT    The difference in elapsed 100NS time
     * @return The ratio of elapsed time to base ticks
     */
    private synchronized long lazilyCalculateMultiplier(long deltaBase, long deltaT) {
        if (utilityBaseMultiplier == null) {
            // If too much time has elapsed from class instantiation, re-initialize the
            // ticks and return without calculating. Approx 7 minutes for 100NS counter to
            // exceed max unsigned int.
            if (deltaT >> 32 > 0) {
                initialUtilityCounters = processorUtilityCounters.get().getRight();
                return 0L;
            }
            // Base counter wraps approximately every 115 minutes
            // If deltaBase is nonpositive assume it has wrapped
            if (deltaBase <= 0) {
                deltaBase += 1L << 32;
            }
            long multiplier = Math.round((double) deltaT / deltaBase);
            // If not enough time has elapsed, return the value this one time but don't
            // persist. 5000 ms = 50 million 100NS ticks
            if (deltaT < 50_000_000L) {
                return multiplier;
            }
            utilityBaseMultiplier = multiplier;
        }
        return utilityBaseMultiplier;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        // These are used in all cases
        List<String> instances;
        List<Long> systemList;
        List<Long> userList;
        List<Long> irqList;
        List<Long> softIrqList;
        List<Long> idleList;
        // These are only used with USE_CPU_UTILITY
        List<Long> baseList = null;
        List<Long> systemUtility = null;
        List<Long> processorUtility = null;
        List<Long> processorUtilityBase = null;
        List<Long> initSystemList = null;
        List<Long> initUserList = null;
        List<Long> initBase = null;
        List<Long> initSystemUtility = null;
        List<Long> initProcessorUtility = null;
        List<Long> initProcessorUtilityBase = null;
        if (USE_CPU_UTILITY) {
            Pair<List<String>, Map<ProcessorUtilityTickCountProperty, List<Long>>> instanceValuePair = processorUtilityCounters
                    .get();
            instances = instanceValuePair.getLeft();
            Map<ProcessorUtilityTickCountProperty, List<Long>> valueMap = instanceValuePair.getRight();
            systemList = valueMap.get(ProcessorUtilityTickCountProperty.PERCENTPRIVILEGEDTIME);
            userList = valueMap.get(ProcessorUtilityTickCountProperty.PERCENTUSERTIME);
            irqList = valueMap.get(ProcessorUtilityTickCountProperty.PERCENTINTERRUPTTIME);
            softIrqList = valueMap.get(ProcessorUtilityTickCountProperty.PERCENTDPCTIME);
            // % Processor Time is actually Idle time
            idleList = valueMap.get(ProcessorUtilityTickCountProperty.PERCENTPROCESSORTIME);
            baseList = valueMap.get(ProcessorUtilityTickCountProperty.TIMESTAMP_SYS100NS);
            // Utility ticks, if configured
            systemUtility = valueMap.get(ProcessorUtilityTickCountProperty.PERCENTPRIVILEGEDUTILITY);
            processorUtility = valueMap.get(ProcessorUtilityTickCountProperty.PERCENTPROCESSORUTILITY);
            processorUtilityBase = valueMap.get(ProcessorUtilityTickCountProperty.PERCENTPROCESSORUTILITY_BASE);

            initSystemList = initialUtilityCounters.get(ProcessorUtilityTickCountProperty.PERCENTPRIVILEGEDTIME);
            initUserList = initialUtilityCounters.get(ProcessorUtilityTickCountProperty.PERCENTUSERTIME);
            initBase = initialUtilityCounters.get(ProcessorUtilityTickCountProperty.TIMESTAMP_SYS100NS);
            // Utility ticks, if configured
            initSystemUtility = initialUtilityCounters.get(ProcessorUtilityTickCountProperty.PERCENTPRIVILEGEDUTILITY);
            initProcessorUtility = initialUtilityCounters
                    .get(ProcessorUtilityTickCountProperty.PERCENTPROCESSORUTILITY);
            initProcessorUtilityBase = initialUtilityCounters
                    .get(ProcessorUtilityTickCountProperty.PERCENTPROCESSORUTILITY_BASE);
        } else {
            Pair<List<String>, Map<ProcessorTickCountProperty, List<Long>>> instanceValuePair = ProcessorInformation
                    .queryProcessorCounters();
            instances = instanceValuePair.getLeft();
            Map<ProcessorTickCountProperty, List<Long>> valueMap = instanceValuePair.getRight();
            systemList = valueMap.get(ProcessorTickCountProperty.PERCENTPRIVILEGEDTIME);
            userList = valueMap.get(ProcessorTickCountProperty.PERCENTUSERTIME);
            irqList = valueMap.get(ProcessorTickCountProperty.PERCENTINTERRUPTTIME);
            softIrqList = valueMap.get(ProcessorTickCountProperty.PERCENTDPCTIME);
            // % Processor Time is actually Idle time
            idleList = valueMap.get(ProcessorTickCountProperty.PERCENTPROCESSORTIME);
        }

        int ncpu = getLogicalProcessorCount();
        long[][] ticks = new long[ncpu][CentralProcessor.TickType.values().length];
        if (instances.isEmpty() || systemList == null || userList == null || irqList == null || softIrqList == null
                || idleList == null
                || (USE_CPU_UTILITY && (baseList == null || systemUtility == null || processorUtility == null
                        || processorUtilityBase == null || initSystemList == null || initUserList == null
                        || initBase == null || initSystemUtility == null || initProcessorUtility == null
                        || initProcessorUtilityBase == null))) {
            return ticks;
        }
        for (String instance : instances) {
            int cpu = instance.contains(Symbol.COMMA) ? numaNodeProcToLogicalProcMap.getOrDefault(instance, 0)
                    : Parsing.parseIntOrDefault(instance, 0);
            if (cpu >= ncpu) {
                continue;
            }
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = systemList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = userList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()] = irqList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.SOFTIRQ.getIndex()] = softIrqList.get(cpu);
            ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = idleList.get(cpu);

            // If users want Task Manager output we have to do some math to get there
            if (USE_CPU_UTILITY) {
                // We have two new capacity numbers, processor (all but idle) and system
                // (included in processor). To further complicate matters, these are in percent
                // units so must be divided by 100.

                // The 100NS elapsed time counter is a constant multiple of the capacity base
                // counter. By enforcing a memoized pause we'll either have zero elapsed time or
                // sufficient delay to determine this offset reliably once and not have to
                // recalculate it

                // Get elapsed time in 100NS
                long deltaT = baseList.get(cpu) - initBase.get(cpu);
                if (deltaT > 0) {
                    // Get elapsed utility base
                    long deltaBase = processorUtilityBase.get(cpu) - initProcessorUtilityBase.get(cpu);
                    // The ratio of elapsed clock to elapsed utility base is an integer constant.
                    // We can calculate a conversion factor to ensure a consistent application of
                    // the correction. Since Utility is in percent, this is actually 100x the true
                    // multiplier but is at the level where the integer calculation is precise
                    long multiplier = lazilyCalculateMultiplier(deltaBase, deltaT);

                    // 0 multiplier means we just re-initialized ticks
                    if (multiplier > 0) {
                        // Get utility delta
                        long deltaProc = processorUtility.get(cpu) - initProcessorUtility.get(cpu);
                        long deltaSys = systemUtility.get(cpu) - initSystemUtility.get(cpu);

                        // Calculate new target ticks
                        // Correct for the 100x multiplier at the end
                        long newUser = initUserList.get(cpu) + multiplier * (deltaProc - deltaSys) / 100;
                        long newSystem = initSystemList.get(cpu) + multiplier * deltaSys / 100;

                        // Adjust user to new, saving the delta
                        long delta = newUser - ticks[cpu][CentralProcessor.TickType.USER.getIndex()];
                        ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = newUser;
                        // Do the same for system
                        delta += newSystem - ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()];
                        ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = newSystem;
                        // Subtract delta from idle
                        ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] -= delta;
                    }
                }
            }

            // Decrement IRQ from system to avoid double counting in the total array
            ticks[cpu][CentralProcessor.TickType.SYSTEM
                    .getIndex()] -= ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()]
                            + ticks[cpu][CentralProcessor.TickType.SOFTIRQ.getIndex()];

            // Raw value is cumulative 100NS-ticks
            // Divide by 10_000 to get milliseconds
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] /= 10_000L;
            ticks[cpu][CentralProcessor.TickType.USER.getIndex()] /= 10_000L;
            ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()] /= 10_000L;
            ticks[cpu][CentralProcessor.TickType.SOFTIRQ.getIndex()] /= 10_000L;
            ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] /= 10_000L;
        }
        // Skipping nice and IOWait, they'll stay 0
        return ticks;
    }

    @Override
    public long queryContextSwitches() {
        return SystemInformation.queryContextSwitchCounters().getOrDefault(ContextSwitchProperty.CONTEXTSWITCHESPERSEC,
                0L);
    }

    @Override
    public long queryInterrupts() {
        return ProcessorInformation.queryInterruptCounters().getOrDefault(InterruptsProperty.INTERRUPTSPERSEC, 0L);
    }

}
