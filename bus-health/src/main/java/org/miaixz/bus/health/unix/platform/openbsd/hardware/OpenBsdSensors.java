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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractSensors;

/**
 * Sensors
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class OpenBsdSensors extends AbstractSensors {

    private final Supplier<Triplet<Double, int[], Double>> tempFanVolts = Memoizer.memoize(OpenBsdSensors::querySensors,
            Memoizer.defaultExpiration());

    private static Triplet<Double, int[], Double> querySensors() {
        double volts = 0d;
        List<Double> cpuTemps = new ArrayList<>();
        List<Double> allTemps = new ArrayList<>();
        List<Integer> fanRPMs = new ArrayList<>();
        for (String line : Executor.runNative("systat -ab sensors")) {
            String[] split = Pattern.SPACES_PATTERN.split(line);
            if (split.length > 1) {
                if (split[0].contains("cpu")) {
                    if (split[0].contains("temp0")) {
                        cpuTemps.add(Parsing.parseDoubleOrDefault(split[1], Double.NaN));
                    } else if (split[0].contains("volt0")) {
                        volts = Parsing.parseDoubleOrDefault(split[1], 0d);
                    }
                } else if (split[0].contains("temp0")) {
                    allTemps.add(Parsing.parseDoubleOrDefault(split[1], Double.NaN));
                } else if (split[0].contains("fan")) {
                    fanRPMs.add(Parsing.parseIntOrDefault(split[1], 0));
                }
            }
        }
        // Prefer cpu temps
        double temp = cpuTemps.isEmpty() ? listAverage(allTemps) : listAverage(cpuTemps);
        // Collect all fans
        int[] fans = new int[fanRPMs.size()];
        for (int i = 0; i < fans.length; i++) {
            fans[i] = fanRPMs.get(i);
        }
        return Triplet.of(temp, fans, volts);
    }

    private static double listAverage(List<Double> doubles) {
        double sum = 0d;
        int count = 0;
        for (Double d : doubles) {
            if (!d.isNaN()) {
                sum += d;
                count++;
            }
        }
        return count > 0 ? sum / count : 0d;
    }

    @Override
    public double queryCpuTemperature() {
        return tempFanVolts.get().getLeft();
    }

    @Override
    public int[] queryFanSpeeds() {
        return tempFanVolts.get().getMiddle();
    }

    @Override
    public double queryCpuVoltage() {
        return tempFanVolts.get().getRight();
    }

}
