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

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractBaseboard;
import org.miaixz.bus.health.linux.driver.Sysfs;
import org.miaixz.bus.health.linux.driver.proc.CpuInfo;

/**
 * Baseboard data obtained by sysfs
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class LinuxBaseboard extends AbstractBaseboard {

    private final Supplier<Tuple> manufacturerModelVersionSerial = Memoizer.memoize(CpuInfo::queryBoardInfo);
    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);
    private final Supplier<String> model = Memoizer.memoize(this::queryModel);
    private final Supplier<String> version = Memoizer.memoize(this::queryVersion);
    private final Supplier<String> serialNumber = Memoizer.memoize(this::querySerialNumber);

    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    @Override
    public String getModel() {
        return model.get();
    }

    @Override
    public String getVersion() {
        return version.get();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber.get();
    }

    private String queryManufacturer() {
        String result = null;
        if ((result = Sysfs.queryBoardVendor()) == null
                && (result = manufacturerModelVersionSerial.get().get(0)) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryModel() {
        String result;
        if ((result = Sysfs.queryBoardModel()) == null
                && (result = manufacturerModelVersionSerial.get().get(1)) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryVersion() {
        String result;
        if ((result = Sysfs.queryBoardVersion()) == null
                && (result = manufacturerModelVersionSerial.get().get(2)) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String querySerialNumber() {
        String result;
        if ((result = Sysfs.queryBoardSerial()) == null
                && (result = manufacturerModelVersionSerial.get().get(3)) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

}
