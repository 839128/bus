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
package org.miaixz.bus.health.mac.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractFirmware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

/**
 * Firmware data obtained from ioreg.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class MacFirmware extends AbstractFirmware {

    private final Supplier<Tuple> manufNameDescVersRelease = Memoizer.memoize(MacFirmware::queryEfi);

    private static Tuple queryEfi() {
        String manufacturer = null;
        String name = null;
        String description = null;
        String version = null;
        String releaseDate = null;

        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        byte[] data;
        if (platformExpert != null) {
            IOIterator iter = platformExpert.getChildIterator("IODeviceTree");
            if (iter != null) {
                IORegistryEntry entry = iter.next();
                while (entry != null) {
                    switch (entry.getName()) {
                    case "rom":
                        data = entry.getByteArrayProperty("vendor");
                        if (data != null) {
                            manufacturer = Native.toString(data, Charset.UTF_8);
                        }
                        data = entry.getByteArrayProperty("version");
                        if (data != null) {
                            version = Native.toString(data, Charset.UTF_8);
                        }
                        data = entry.getByteArrayProperty("release-date");
                        if (data != null) {
                            releaseDate = Native.toString(data, Charset.UTF_8);
                        }
                        break;
                    case "chosen":
                        data = entry.getByteArrayProperty("booter-name");
                        if (data != null) {
                            name = Native.toString(data, Charset.UTF_8);
                        }
                        break;
                    case "efi":
                        data = entry.getByteArrayProperty("firmware-abi");
                        if (data != null) {
                            description = Native.toString(data, Charset.UTF_8);
                        }
                        break;
                    default:
                        if (StringKit.isBlank(name)) {
                            name = entry.getStringProperty("IONameMatch");
                        }
                        break;
                    }
                    entry.release();
                    entry = iter.next();
                }
                iter.release();
            }
            if (StringKit.isBlank(manufacturer)) {
                data = platformExpert.getByteArrayProperty("manufacturer");
                if (data != null) {
                    manufacturer = Native.toString(data, Charset.UTF_8);
                }
            }
            if (StringKit.isBlank(version)) {
                data = platformExpert.getByteArrayProperty("target-type");
                if (data != null) {
                    version = Native.toString(data, Charset.UTF_8);
                }
            }
            if (StringKit.isBlank(name)) {
                data = platformExpert.getByteArrayProperty("device_type");
                if (data != null) {
                    name = Native.toString(data, Charset.UTF_8);
                }
            }
            platformExpert.release();
        }
        return new Tuple(StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(name) ? Normal.UNKNOWN : name,
                StringKit.isBlank(description) ? Normal.UNKNOWN : description,
                StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(releaseDate) ? Normal.UNKNOWN : releaseDate);
    }

    @Override
    public String getManufacturer() {
        return manufNameDescVersRelease.get().get(0);
    }

    @Override
    public String getName() {
        return manufNameDescVersRelease.get().get(1);
    }

    @Override
    public String getDescription() {
        return manufNameDescVersRelease.get().get(2);
    }

    @Override
    public String getVersion() {
        return manufNameDescVersRelease.get().get(3);
    }

    @Override
    public String getReleaseDate() {
        return manufNameDescVersRelease.get().get(4);
    }

}
