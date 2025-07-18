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
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.PowerSource;
import org.miaixz.bus.health.builtin.hardware.common.AbstractPowerSource;

/**
 * A Power Source
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class OpenBsdPowerSource extends AbstractPowerSource {

    public OpenBsdPowerSource(String psName, String psDeviceName, double psRemainingCapacityPercent,
            double psTimeRemainingEstimated, double psTimeRemainingInstant, double psPowerUsageRate, double psVoltage,
            double psAmperage, boolean psPowerOnLine, boolean psCharging, boolean psDischarging,
            PowerSource.CapacityUnits psCapacityUnits, int psCurrentCapacity, int psMaxCapacity, int psDesignCapacity,
            int psCycleCount, String psChemistry, LocalDate psManufactureDate, String psManufacturer,
            String psSerialNumber, double psTemperature) {
        super(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated, psTimeRemainingInstant,
                psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging, psDischarging, psCapacityUnits,
                psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate,
                psManufacturer, psSerialNumber, psTemperature);
    }

    /**
     * Gets Battery Information
     *
     * @return An array of PowerSource objects representing batteries, etc.
     */
    public static List<PowerSource> getPowerSources() {
        Set<String> psNames = new HashSet<>();
        for (String line : Executor.runNative("systat -ab sensors")) {
            if (line.contains(".amphour") || line.contains(".watthour")) {
                int dot = line.indexOf('.');
                psNames.add(line.substring(0, dot));
            }
        }
        List<PowerSource> psList = new ArrayList<>();
        for (String name : psNames) {
            psList.add(getPowerSource(name));
        }
        return psList;
    }

    private static OpenBsdPowerSource getPowerSource(String name) {
        String psName = name.startsWith("acpi") ? name.substring(4) : name;
        double psRemainingCapacityPercent = 1d;
        double psTimeRemainingEstimated = -1d; // -1 = unknown, -2 = unlimited
        double psPowerUsageRate = 0d;
        double psVoltage = -1d;
        double psAmperage = 0d;
        boolean psPowerOnLine = false;
        boolean psCharging = false;
        boolean psDischarging = false;
        PowerSource.CapacityUnits psCapacityUnits = PowerSource.CapacityUnits.RELATIVE;
        int psCurrentCapacity = 0;
        int psMaxCapacity = 1;
        int psDesignCapacity = 1;
        int psCycleCount = -1;
        LocalDate psManufactureDate = null;

        double psTemperature = 0d;

        for (String line : Executor.runNative("systat -ab sensors")) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length > 1 && split[0].startsWith(name)) {
                if (split[0].contains("volt0") || split[0].contains("volt") && line.contains("current")) {
                    psVoltage = Parsing.parseDoubleOrDefault(split[1], -1d);
                } else if (split[0].contains("current0")) {
                    psAmperage = Parsing.parseDoubleOrDefault(split[1], 0d);
                } else if (split[0].contains("temp0")) {
                    psTemperature = Parsing.parseDoubleOrDefault(split[1], 0d);
                } else if (split[0].contains("watthour") || split[0].contains("amphour")) {
                    psCapacityUnits = split[0].contains("watthour") ? PowerSource.CapacityUnits.MWH
                            : PowerSource.CapacityUnits.MAH;
                    if (line.contains("remaining")) {
                        psCurrentCapacity = (int) (1000d * Parsing.parseDoubleOrDefault(split[1], 0d));
                    } else if (line.contains("full")) {
                        psMaxCapacity = (int) (1000d * Parsing.parseDoubleOrDefault(split[1], 0d));
                    } else if (line.contains("new") || line.contains("design")) {
                        psDesignCapacity = (int) (1000d * Parsing.parseDoubleOrDefault(split[1], 0d));
                    }
                }
            }
        }

        int state = Parsing.parseIntOrDefault(Executor.getFirstAnswer("apm -b"), 255);
        // state 0=high, 1=low, 2=critical, 3=charging, 4=absent, 255=unknown
        if (state < 4) {
            psPowerOnLine = true;
            if (state == 3) {
                psCharging = true;
            } else {
                int time = Parsing.parseIntOrDefault(Executor.getFirstAnswer("apm -m"), -1);
                // time is in minutes
                psTimeRemainingEstimated = time < 0 ? -1d : 60d * time;
                psDischarging = true;
            }
        }
        // life is in percent
        int life = Parsing.parseIntOrDefault(Executor.getFirstAnswer("apm -l"), -1);
        if (life > 0) {
            psRemainingCapacityPercent = life / 100d;
        }
        if (psMaxCapacity < psDesignCapacity && psMaxCapacity < psCurrentCapacity) {
            psMaxCapacity = psDesignCapacity;
        } else if (psDesignCapacity < psMaxCapacity && psDesignCapacity < psCurrentCapacity) {
            psDesignCapacity = psMaxCapacity;
        }

        String psDeviceName = Normal.UNKNOWN;
        String psSerialNumber = Normal.UNKNOWN;
        String psChemistry = Normal.UNKNOWN;
        String psManufacturer = Normal.UNKNOWN;

        double psTimeRemainingInstant = psTimeRemainingEstimated;
        if (psVoltage > 0) {
            if (psAmperage > 0 && psPowerUsageRate == 0) {
                psPowerUsageRate = psAmperage * psVoltage;
            } else if (psAmperage == 0 && psPowerUsageRate > 0) {
                psAmperage = psPowerUsageRate / psVoltage;
            }
        }

        return new OpenBsdPowerSource(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated,
                psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging,
                psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount,
                psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }

}
