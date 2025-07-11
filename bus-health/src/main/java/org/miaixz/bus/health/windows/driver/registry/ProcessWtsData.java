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
package org.miaixz.bus.health.windows.driver.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.wmi.Win32Process;
import org.miaixz.bus.health.windows.driver.wmi.Win32Process.ProcessXPProperty;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.VersionHelpers;
import com.sun.jna.platform.win32.Wtsapi32;
import com.sun.jna.platform.win32.Wtsapi32.WTS_PROCESS_INFO_EX;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Utility to read process data from HKEY_PERFORMANCE_DATA information with backup from Performance Counters or WMI
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class ProcessWtsData {

    private static final boolean IS_WINDOWS7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();

    /**
     * Query the registry for process performance counters
     *
     * @param pids An optional collection of process IDs to filter the list to. May be null for no filtering.
     * @return A map with Process ID as the key and a {@link WtsInfo} object populated with data.
     */
    public static Map<Integer, WtsInfo> queryProcessWtsMap(Collection<Integer> pids) {
        if (IS_WINDOWS7_OR_GREATER) {
            // Get processes from WTS
            return queryProcessWtsMapFromWTS(pids);
        }
        // Pre-Win7 we can't use WTSEnumerateProcessesEx so we'll grab the
        // same info from WMI and fake the array
        return queryProcessWtsMapFromPerfMon(pids);
    }

    private static Map<Integer, WtsInfo> queryProcessWtsMapFromWTS(Collection<Integer> pids) {
        Map<Integer, WtsInfo> wtsMap = new HashMap<>();
        try (ByRef.CloseableIntByReference pCount = new ByRef.CloseableIntByReference(0);
                ByRef.CloseablePointerByReference ppProcessInfo = new ByRef.CloseablePointerByReference();
                ByRef.CloseableIntByReference infoLevel1 = new ByRef.CloseableIntByReference(
                        Wtsapi32.WTS_PROCESS_INFO_LEVEL_1)) {
            if (!Wtsapi32.INSTANCE.WTSEnumerateProcessesEx(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, infoLevel1,
                    Wtsapi32.WTS_ANY_SESSION, ppProcessInfo, pCount)) {
                Logger.error("Failed to enumerate Processes. Error code: {}", Kernel32.INSTANCE.GetLastError());
                return wtsMap;
            }
            // extract the pointed-to pointer and create array
            Pointer pProcessInfo = ppProcessInfo.getValue();
            final WTS_PROCESS_INFO_EX processInfoRef = new WTS_PROCESS_INFO_EX(pProcessInfo);
            WTS_PROCESS_INFO_EX[] processInfo = (WTS_PROCESS_INFO_EX[]) processInfoRef.toArray(pCount.getValue());
            for (WTS_PROCESS_INFO_EX info : processInfo) {
                if (pids == null || pids.contains(info.ProcessId)) {
                    wtsMap.put(info.ProcessId,
                            new WtsInfo(info.pProcessName, Normal.EMPTY, info.NumberOfThreads,
                                    info.PagefileUsage & 0xffff_ffffL, info.KernelTime.getValue() / 10_000L,
                                    info.UserTime.getValue() / 10_000, info.HandleCount));
                }
            }
            // Clean up memory
            if (!Wtsapi32.INSTANCE.WTSFreeMemoryEx(Wtsapi32.WTS_PROCESS_INFO_LEVEL_1, pProcessInfo,
                    pCount.getValue())) {
                Logger.warn("Failed to Free Memory for Processes. Error code: {}", Kernel32.INSTANCE.GetLastError());
            }
        }
        return wtsMap;
    }

    private static Map<Integer, WtsInfo> queryProcessWtsMapFromPerfMon(Collection<Integer> pids) {
        Map<Integer, WtsInfo> wtsMap = new HashMap<>();
        WmiResult<ProcessXPProperty> processWmiResult = Win32Process.queryProcesses(pids);
        for (int i = 0; i < processWmiResult.getResultCount(); i++) {
            wtsMap.put(WmiKit.getUint32(processWmiResult, ProcessXPProperty.PROCESSID, i), new WtsInfo(
                    WmiKit.getString(processWmiResult, ProcessXPProperty.NAME, i),
                    WmiKit.getString(processWmiResult, ProcessXPProperty.EXECUTABLEPATH, i),
                    WmiKit.getUint32(processWmiResult, ProcessXPProperty.THREADCOUNT, i),
                    // WMI Pagefile usage is in KB
                    1024 * (WmiKit.getUint32(processWmiResult, ProcessXPProperty.PAGEFILEUSAGE, i) & 0xffff_ffffL),
                    WmiKit.getUint64(processWmiResult, ProcessXPProperty.KERNELMODETIME, i) / 10_000L,
                    WmiKit.getUint64(processWmiResult, ProcessXPProperty.USERMODETIME, i) / 10_000L,
                    WmiKit.getUint32(processWmiResult, ProcessXPProperty.HANDLECOUNT, i)));
        }
        return wtsMap;
    }

    /**
     * Class to encapsulate data from WTS Process Info
     */
    @Immutable
    public static class WtsInfo {
        private final String name;
        private final String path;
        private final int threadCount;
        private final long virtualSize;
        private final long kernelTime;
        private final long userTime;
        private final long openFiles;

        public WtsInfo(String name, String path, int threadCount, long virtualSize, long kernelTime, long userTime,
                long openFiles) {
            this.name = name;
            this.path = path;
            this.threadCount = threadCount;
            this.virtualSize = virtualSize;
            this.kernelTime = kernelTime;
            this.userTime = userTime;
            this.openFiles = openFiles;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * @return the threadCount
         */
        public int getThreadCount() {
            return threadCount;
        }

        /**
         * @return the virtualSize
         */
        public long getVirtualSize() {
            return virtualSize;
        }

        /**
         * @return the kernelTime
         */
        public long getKernelTime() {
            return kernelTime;
        }

        /**
         * @return the userTime
         */
        public long getUserTime() {
            return userTime;
        }

        /**
         * @return the openFiles
         */
        public long getOpenFiles() {
            return openFiles;
        }
    }

}
