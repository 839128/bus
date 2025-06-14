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
package org.miaixz.bus.health.builtin.hardware;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * Sensors include hardware sensors to monitor temperature, fan speed, and other information.
 * <p>
 * Drivers may or may not exist to collect this data depending on the installed hardware and Operating System. In
 * addition, software-hardware communication may suffer intermittent errors when attempting to access this information.
 * Users should expect, test for, and handle zero values and/or empty arrays which will result if the OS is unable to
 * provide the information.
 * <p>
 * Windows information is generally retrieved via Windows Management Instrumentation (WMI). Unfortunately, most hardware
 * providers do not publish sensor values to WMI. OSHI attempts to retrieve values from
 * <a href="https://github.com/LibreHardwareMonitor/LibreHardwareMonitor">LibreHardwareMonitor</a> if the optional
 * <a href="https://github.com/pandalxb/jLibreHardwareMonitor">jLibreHardwareMonitor</a> dependency is included.
 * Otherwise, OSHI attempts to retrieve values from <a href="https://openhardwaremonitor.org/">Open Hardware Monitor</a>
 * if it is running. Otherwise, OSHI retrieves via the Microsoft API, which may require elevated permissions and still
 * may provide no results or unchanging results depending on the motherboard manufacturer.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public interface Sensors {
    /**
     * CPU Temperature
     *
     * @return CPU Temperature in degrees Celsius if available, 0 or {@link Double#NaN} otherwise.
     *         <p>
     *         See notes on {@link Sensors}.
     */
    double getCpuTemperature();

    /**
     * Fan speeds
     *
     * @return Speed in rpm for all fans. May return empty array if no fans detected or 0 fan speed if unable to measure
     *         fan speed.
     */
    int[] getFanSpeeds();

    /**
     * CPU Voltage
     *
     * @return CPU Voltage in Volts if available, 0 otherwise.
     */
    double getCpuVoltage();

}
