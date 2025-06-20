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
package org.miaixz.bus.health.linux;

import java.io.File;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.health.Config;

/**
 * Provides constants for paths in the {@code /sys} filesystem on Linux. If the user desires to configure a custom
 * {@code /sys} path, it must be declared in the configuration file or updated in the {@link Config} class prior to
 * initializing this class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class SysPath {

    /**
     * The /sys filesystem location.
     */
    public static final String SYS = querySysConfig() + "/";

    public static final String CPU = SYS + "devices/system/cpu/";
    public static final String DMI_ID = SYS + "devices/virtual/dmi/id/";
    public static final String NET = SYS + "class/net/";
    public static final String MODEL = SYS + "firmware/devicetree/base/model";
    public static final String POWER_SUPPLY = SYS + "class/power_supply";
    public static final String HWMON = SYS + "class/hwmon/";
    public static final String THERMAL = SYS + "class/thermal/";

    private static String querySysConfig() {
        String sysPath = Config.get(Config._UTIL_SYS_PATH, "/sys");
        // Ensure prefix begins with path separator, but doesn't end with one
        sysPath = '/' + sysPath.replaceAll("/$|^/", Normal.EMPTY);
        if (!new File(sysPath).exists()) {
            throw new NotFoundException("The path does not exist " + Config._UTIL_SYS_PATH);
        }
        return sysPath;
    }

}
