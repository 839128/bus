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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.OSSession;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.Wtsapi32.WTSINFO;
import com.sun.jna.platform.win32.Wtsapi32.WTS_CLIENT_ADDRESS;
import com.sun.jna.platform.win32.Wtsapi32.WTS_SESSION_INFO;

/**
 * Utility to read process data from HKEY_PERFORMANCE_DATA information with backup from Performance Counters or WMI
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class SessionWtsData {

    private static final int WTS_ACTIVE = 0;
    private static final int WTS_CLIENTADDRESS = 14;
    private static final int WTS_SESSIONINFO = 24;
    private static final int WTS_CLIENTPROTOCOLTYPE = 16;

    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    private static final Wtsapi32 WTS = Wtsapi32.INSTANCE;

    private SessionWtsData() {
    }

    public static List<OSSession> queryUserSessions() {
        List<OSSession> sessions = new ArrayList<>();
        if (IS_VISTA_OR_GREATER) {
            try (ByRef.CloseablePointerByReference ppSessionInfo = new ByRef.CloseablePointerByReference();
                    ByRef.CloseableIntByReference pCount = new ByRef.CloseableIntByReference();
                    ByRef.CloseablePointerByReference ppBuffer = new ByRef.CloseablePointerByReference();
                    ByRef.CloseableIntByReference pBytes = new ByRef.CloseableIntByReference()) {
                if (WTS.WTSEnumerateSessions(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, 0, 1, ppSessionInfo, pCount)) {
                    Pointer pSessionInfo = ppSessionInfo.getValue();
                    if (pCount.getValue() > 0) {
                        WTS_SESSION_INFO sessionInfoRef = new WTS_SESSION_INFO(pSessionInfo);
                        WTS_SESSION_INFO[] sessionInfo = (WTS_SESSION_INFO[]) sessionInfoRef.toArray(pCount.getValue());
                        for (WTS_SESSION_INFO session : sessionInfo) {
                            if (session.State == WTS_ACTIVE) {
                                // Use session id to fetch additional session information
                                WTS.WTSQuerySessionInformation(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, session.SessionId,
                                        WTS_CLIENTPROTOCOLTYPE, ppBuffer, pBytes);
                                Pointer pBuffer = ppBuffer.getValue(); // pointer to USHORT
                                short protocolType = pBuffer.getShort(0); // 0 = console, 2 = RDP
                                WTS.WTSFreeMemory(pBuffer);
                                // We've already got console from registry, only test RDP
                                if (protocolType > 0) {
                                    // DEVICE
                                    String device = session.pWinStationName;
                                    // USER and LOGIN TIME
                                    WTS.WTSQuerySessionInformation(Wtsapi32.WTS_CURRENT_SERVER_HANDLE,
                                            session.SessionId, WTS_SESSIONINFO, ppBuffer, pBytes);
                                    pBuffer = ppBuffer.getValue(); // returns WTSINFO
                                    WTSINFO wtsInfo = new WTSINFO(pBuffer);
                                    // Temporary due to broken LARGE_INTEGER, remove in JNA 5.6.0
                                    long logonTime = new WinBase.FILETIME(
                                            new WinNT.LARGE_INTEGER(wtsInfo.LogonTime.getValue())).toTime();
                                    String userName = wtsInfo.getUserName();
                                    WTS.WTSFreeMemory(pBuffer);
                                    // HOST
                                    WTS.WTSQuerySessionInformation(Wtsapi32.WTS_CURRENT_SERVER_HANDLE,
                                            session.SessionId, WTS_CLIENTADDRESS, ppBuffer, pBytes);
                                    pBuffer = ppBuffer.getValue(); // returns WTS_CLIENT_ADDRESS
                                    WTS_CLIENT_ADDRESS addr = new WTS_CLIENT_ADDRESS(pBuffer);
                                    WTS.WTSFreeMemory(pBuffer);
                                    String host = "::";
                                    if (addr.AddressFamily == IPHlpAPI.AF_INET) {
                                        try {
                                            host = InetAddress.getByAddress(Arrays.copyOfRange(addr.Address, 2, 6))
                                                    .getHostAddress();
                                        } catch (UnknownHostException e) {
                                            // If array is not length of 4, shouldn't happen
                                            host = "Illegal length IP Array";
                                        }
                                    } else if (addr.AddressFamily == IPHlpAPI.AF_INET6) {
                                        // Get ints for address parsing
                                        int[] ipArray = convertBytesToInts(addr.Address);
                                        host = Parsing.parseUtAddrV6toIP(ipArray);
                                    }
                                    sessions.add(new OSSession(userName, device, logonTime, host));
                                }
                            }
                        }
                    }
                    WTS.WTSFreeMemory(pSessionInfo);
                }
            }
        }
        return sessions;
    }

    /**
     * Per WTS_INFO_CLASS docs, the IP address is offset by two bytes from the start of the Address member of the
     * WTS_CLIENT_ADDRESS structure. Also contrary to docs, IPv4 is not a null terminated string. This method converts
     * the byte[20] to an int[4] parseable by existing code
     *
     * @param address The 20-byte array from the WTS_CLIENT_ADDRESS structure
     * @return A 4-int array for {@link Parsing#parseUtAddrV6toIP}
     */
    private static int[] convertBytesToInts(byte[] address) {
        IntBuffer intBuf = ByteBuffer.wrap(Arrays.copyOfRange(address, 2, 18)).order(ByteOrder.BIG_ENDIAN)
                .asIntBuffer();
        int[] array = new int[intBuf.remaining()];
        intBuf.get(array);
        return array;
    }

}
