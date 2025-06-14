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
package org.miaixz.bus.health.unix.platform.solaris.driver.disk;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWPartition;

/**
 * Utility to query iostat
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Prtvtoc {

    private static final String PRTVTOC_DEV_DSK = "prtvtoc /dev/dsk/";

    public static List<HWPartition> queryPartitions(String mount, int major) {
        List<HWPartition> partList = new ArrayList<>();
        // This requires sudo permissions; will result in "permission denied"
        // otherwise in which case we return empty partition list
        List<String> prtvotc = Executor.runNative(PRTVTOC_DEV_DSK + mount);
        // Sample output - see man prtvtoc
        if (prtvotc.size() > 1) {
            int bytesPerSector = 0;
            String[] split;
            // We have a result, parse partition table
            for (String line : prtvotc) {
                // If line starts with asterisk we ignore except for the one
                // specifying bytes per sector
                if (line.startsWith(Symbol.STAR)) {
                    if (line.endsWith("bytes/sector")) {
                        split = Pattern.SPACES_PATTERN.split(line);
                        if (split.length > 0) {
                            bytesPerSector = Parsing.parseIntOrDefault(split[1], 0);
                        }
                    }
                } else if (bytesPerSector > 0) {
                    // If bytes/sector is still 0, these are not real partitions so
                    // ignore.
                    // Lines without asterisk have 6 or 7 whitespaces-split values
                    // representing (last field optional):
                    // Partition Tag Flags Sector Count Sector Mount
                    split = Pattern.SPACES_PATTERN.split(line.trim());
                    // Partition 2 is always the whole disk so we ignore it
                    if (split.length >= 6 && !"2".equals(split[0])) {
                        // First field is partition number
                        String identification = mount + "s" + split[0];
                        // major already defined as method param
                        int minor = Parsing.parseIntOrDefault(split[0], 0);
                        // Second field is tag. Parse:
                        String name;
                        switch (Parsing.parseIntOrDefault(split[1], 0)) {
                        case 0x01:
                        case 0x18:
                            name = "boot";
                            break;
                        case 0x02:
                            name = "root";
                            break;
                        case 0x03:
                            name = "swap";
                            break;
                        case 0x04:
                            name = "usr";
                            break;
                        case 0x05:
                            name = "backup";
                            break;
                        case 0x06:
                            name = "stand";
                            break;
                        case 0x07:
                            name = "var";
                            break;
                        case 0x08:
                            name = "home";
                            break;
                        case 0x09:
                            name = "altsctr";
                            break;
                        case 0x0a:
                            name = "cache";
                            break;
                        case 0x0b:
                            name = "reserved";
                            break;
                        case 0x0c:
                            name = "system";
                            break;
                        case 0x0e:
                            name = "public region";
                            break;
                        case 0x0f:
                            name = "private region";
                            break;
                        default:
                            name = Normal.UNKNOWN;
                            break;
                        }
                        // Third field is flags.
                        String type;
                        // First character writable, second is mountable
                        switch (split[2]) {
                        case "00":
                            type = "wm";
                            break;
                        case "10":
                            type = "rm";
                            break;
                        case "01":
                            type = "wu";
                            break;
                        default:
                            type = "ru";
                            break;
                        }
                        // Fifth field is sector count
                        long partSize = bytesPerSector * Parsing.parseLongOrDefault(split[4], 0L);
                        // Seventh field (if present) is mount point
                        String mountPoint = Normal.EMPTY;
                        if (split.length > 6) {
                            mountPoint = split[6];
                        }
                        partList.add(new HWPartition(identification, name, type, Normal.EMPTY, partSize, major, minor,
                                mountPoint));
                    }
                }
            }
        }
        return partList;
    }

}
