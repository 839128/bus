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
package org.miaixz.bus.health.unix.platform.aix.software;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats;
import org.miaixz.bus.health.builtin.software.common.AbstractInternetProtocolStats;
import org.miaixz.bus.health.unix.platform.aix.driver.perfstat.PerfstatProtocol;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_protocol_t;

/**
 * Internet Protocol Stats implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class AixInternetProtocolStats extends AbstractInternetProtocolStats {

    private final Supplier<perfstat_protocol_t[]> ipstats = Memoizer.memoize(PerfstatProtocol::queryProtocols,
            Memoizer.defaultExpiration());

    @Override
    public InternetProtocolStats.TcpStats getTCPv4Stats() {
        for (perfstat_protocol_t stat : ipstats.get()) {
            if ("tcp".equals(Native.toString(stat.name))) {
                return new InternetProtocolStats.TcpStats(stat.u.tcp.established, stat.u.tcp.initiated,
                        stat.u.tcp.accepted, stat.u.tcp.dropped, stat.u.tcp.dropped, stat.u.tcp.opackets,
                        stat.u.tcp.ipackets, 0L, stat.u.tcp.ierrors, 0L);
            }
        }
        return new InternetProtocolStats.TcpStats(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);
    }

    @Override
    public InternetProtocolStats.UdpStats getUDPv4Stats() {
        for (perfstat_protocol_t stat : ipstats.get()) {
            if ("udp".equals(Native.toString(stat.name))) {
                return new InternetProtocolStats.UdpStats(stat.u.udp.opackets, stat.u.udp.ipackets,
                        stat.u.udp.no_socket, stat.u.udp.ierrors);
            }
        }
        return new InternetProtocolStats.UdpStats(0L, 0L, 0L, 0L);
    }

}
