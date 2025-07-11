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
package org.miaixz.bus.health.windows.jna;

import org.miaixz.bus.health.Builder;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Power profile stats. This class should be considered non-API as it may be removed if/when its code is incorporated
 * into the JNA project.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface PowrProf extends com.sun.jna.platform.win32.PowrProf {

    /**
     * Constant <code>INSTANCE</code>
     */
    PowrProf INSTANCE = Native.load("PowrProf", PowrProf.class);

    enum BATTERY_QUERY_INFORMATION_LEVEL {
        BatteryInformation, BatteryGranularityInformation, BatteryTemperature, BatteryEstimatedTime, BatteryDeviceName,
        BatteryManufactureDate, BatteryManufactureName, BatteryUniqueID, BatterySerialNumber
    }

    /**
     * Contains information about the current state of the system battery.
     */
    @FieldOrder({ "acOnLine", "batteryPresent", "charging", "discharging", "spare1", "tag", "maxCapacity",
            "remainingCapacity", "rate", "estimatedTime", "defaultAlert1", "defaultAlert2" })
    class SystemBatteryState extends Structure implements AutoCloseable {
        public byte acOnLine;
        public byte batteryPresent;
        public byte charging;
        public byte discharging;
        public byte[] spare1 = new byte[3];
        public byte tag;
        public int maxCapacity;
        public int remainingCapacity;
        public int rate;
        public int estimatedTime;
        public int defaultAlert1;
        public int defaultAlert2;

        public SystemBatteryState(Pointer p) {
            super(p);
            read();
        }

        public SystemBatteryState() {
            super();
        }

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /**
     * Contains information about a processor.
     */
    @FieldOrder({ "number", "maxMhz", "currentMhz", "mhzLimit", "maxIdleState", "currentIdleState" })
    class ProcessorPowerInformation extends Structure {
        public int number;
        public int maxMhz;
        public int currentMhz;
        public int mhzLimit;
        public int maxIdleState;
        public int currentIdleState;

        public ProcessorPowerInformation(Pointer p) {
            super(p);
            read();
        }

        public ProcessorPowerInformation() {
            super();
        }
    }

    // MOVE?
    @FieldOrder({ "BatteryTag", "InformationLevel", "AtRate" })
    class BATTERY_QUERY_INFORMATION extends Structure implements AutoCloseable {
        public int BatteryTag;
        public int InformationLevel;
        public int AtRate;

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    @FieldOrder({ "Capabilities", "Technology", "Reserved", "Chemistry", "DesignedCapacity", "FullChargedCapacity",
            "DefaultAlert1", "DefaultAlert2", "CriticalBias", "CycleCount" })
    class BATTERY_INFORMATION extends Structure implements AutoCloseable {
        public int Capabilities;
        public byte Technology;
        public byte[] Reserved = new byte[3];
        public byte[] Chemistry = new byte[4];
        public int DesignedCapacity;
        public int FullChargedCapacity;
        public int DefaultAlert1;
        public int DefaultAlert2;
        public int CriticalBias;
        public int CycleCount;

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    @FieldOrder({ "BatteryTag", "Timeout", "PowerState", "LowCapacity", "HighCapacity" })
    class BATTERY_WAIT_STATUS extends Structure implements AutoCloseable {
        public int BatteryTag;
        public int Timeout;
        public int PowerState;
        public int LowCapacity;
        public int HighCapacity;

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    @FieldOrder({ "PowerState", "Capacity", "Voltage", "Rate" })
    class BATTERY_STATUS extends Structure implements AutoCloseable {
        public int PowerState;
        public int Capacity;
        public int Voltage;
        public int Rate;

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    @FieldOrder({ "Day", "Month", "Year" })
    class BATTERY_MANUFACTURE_DATE extends Structure implements AutoCloseable {
        public byte Day;
        public byte Month;
        public short Year;

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

}
