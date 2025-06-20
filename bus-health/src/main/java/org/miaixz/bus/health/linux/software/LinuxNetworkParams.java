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
package org.miaixz.bus.health.linux.software;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.software.common.AbstractNetworkParams;
import org.miaixz.bus.health.linux.jna.LinuxLibc;
import org.miaixz.bus.health.unix.jna.CLibrary;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import com.sun.jna.platform.unix.LibCAPI;

/**
 * LinuxNetworkParams class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class LinuxNetworkParams extends AbstractNetworkParams {

    private static final LinuxLibc LIBC = LinuxLibc.INSTANCE;

    private static final String IPV4_DEFAULT_DEST = "0.0.0.0"; // NOSONAR
    private static final String IPV6_DEFAULT_DEST = "::/0";

    @Override
    public String getDomainName() {
        try (CLibrary.Addrinfo hint = new CLibrary.Addrinfo()) {
            hint.ai_flags = CLibrary.AI_CANONNAME;
            String hostname;
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                Logger.warn("Unknown host exception when getting address of local host: {}", e.getMessage());
                return Normal.EMPTY;
            }
            try (ByRef.CloseablePointerByReference ptr = new ByRef.CloseablePointerByReference()) {
                int res = LIBC.getaddrinfo(hostname, null, hint, ptr);
                if (res > 0) {
                    if (Logger.isErrorEnabled()) {
                        Logger.error("Failed getaddrinfo(): {}", LIBC.gai_strerror(res));
                    }
                    return Normal.EMPTY;
                }
                try (CLibrary.Addrinfo info = new CLibrary.Addrinfo(ptr.getValue())) {
                    return info.ai_canonname == null ? hostname : info.ai_canonname.trim();
                }
            }
        }
    }

    @Override
    public String getHostName() {
        byte[] hostnameBuffer = new byte[LibCAPI.HOST_NAME_MAX + 1];
        if (0 != LibC.INSTANCE.gethostname(hostnameBuffer, hostnameBuffer.length)) {
            return super.getHostName();
        }
        return Native.toString(hostnameBuffer);
    }

    @Override
    public String getIpv4DefaultGateway() {
        List<String> routes = Executor.runNative("route -A inet -n");
        if (routes.size() <= 2) {
            return Normal.EMPTY;
        }

        String gateway = Normal.EMPTY;
        int minMetric = Integer.MAX_VALUE;

        for (int i = 2; i < routes.size(); i++) {
            String[] fields = Pattern.SPACES_PATTERN.split(routes.get(i));
            if (fields.length > 4 && fields[0].equals(IPV4_DEFAULT_DEST)) {
                boolean isGateway = fields[3].indexOf('G') != -1;
                int metric = Parsing.parseIntOrDefault(fields[4], Integer.MAX_VALUE);
                if (isGateway && metric < minMetric) {
                    minMetric = metric;
                    gateway = fields[1];
                }
            }
        }
        return gateway;
    }

    @Override
    public String getIpv6DefaultGateway() {
        List<String> routes = Executor.runNative("route -A inet6 -n");
        if (routes.size() <= 2) {
            return Normal.EMPTY;
        }

        String gateway = Normal.EMPTY;
        int minMetric = Integer.MAX_VALUE;

        for (int i = 2; i < routes.size(); i++) {
            String[] fields = Pattern.SPACES_PATTERN.split(routes.get(i));
            if (fields.length > 3 && fields[0].equals(IPV6_DEFAULT_DEST)) {
                boolean isGateway = fields[2].indexOf('G') != -1;
                int metric = Parsing.parseIntOrDefault(fields[3], Integer.MAX_VALUE);
                if (isGateway && metric < minMetric) {
                    minMetric = metric;
                    gateway = fields[1];
                }
            }
        }
        return gateway;
    }

}
