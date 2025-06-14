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
package org.miaixz.bus.health.builtin.hardware.common;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GlobalMemory;
import org.miaixz.bus.health.builtin.hardware.PhysicalMemory;

/**
 * Memory info.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public abstract class AbstractGlobalMemory implements GlobalMemory {

    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        // dmidecode requires sudo permission but is the only option on Linux
        // and Unix
        List<PhysicalMemory> pmList = new ArrayList<>();
        List<String> dmi = Executor.runNative("dmidecode --type 17");
        int bank = 0;
        String bankLabel = Normal.UNKNOWN;
        String locator = Normal.EMPTY;
        long capacity = 0L;
        long speed = 0L;
        String manufacturer = Normal.UNKNOWN;
        String memoryType = Normal.UNKNOWN;
        String partNumber = Normal.UNKNOWN;
        String serialNumber = Normal.UNKNOWN;
        for (String line : dmi) {
            if (line.trim().contains("DMI type 17")) {
                // Save previous bank
                if (bank++ > 0) {
                    if (capacity > 0) {
                        pmList.add(new PhysicalMemory(bankLabel + locator, capacity, speed, manufacturer, memoryType,
                                partNumber, serialNumber));
                    }
                    bankLabel = Normal.UNKNOWN;
                    locator = Normal.EMPTY;
                    capacity = 0L;
                    speed = 0L;
                }
            } else if (bank > 0) {
                String[] split = line.trim().split(Symbol.COLON);
                if (split.length == 2) {
                    switch (split[0]) {
                    case "Bank Locator":
                        bankLabel = split[1].trim();
                        break;
                    case "Locator":
                        locator = "/" + split[1].trim();
                        break;
                    case "Size":
                        capacity = Parsing.parseDecimalMemorySizeToBinary(split[1].trim());
                        break;
                    case "Type":
                        memoryType = split[1].trim();
                        break;
                    case "Speed":
                        speed = Parsing.parseSpeed(split[1]);
                        break;
                    case "Manufacturer":
                        manufacturer = split[1].trim();
                        break;
                    case "PartNumber":
                    case "Part Number":
                        partNumber = split[1].trim();
                        break;
                    case "Serial Number":
                        serialNumber = split[1].trim();
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        if (capacity > 0) {
            pmList.add(new PhysicalMemory(bankLabel + locator, capacity, speed, manufacturer, memoryType, partNumber,
                    serialNumber));
        }
        return pmList;
    }

    @Override
    public String toString() {
        String sb = "Available: " + Formats.formatBytes(getAvailable()) + "/" + Formats.formatBytes(getTotal());
        return sb;
    }

}
