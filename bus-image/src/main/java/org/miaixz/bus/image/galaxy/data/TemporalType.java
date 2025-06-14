/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.galaxy.data;

import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.miaixz.bus.image.Format;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
enum TemporalType {
    DA {
        @Override
        public Temporal parseTemporal(String s, DatePrecision precision) {
            precision.lastField = Calendar.DAY_OF_MONTH;
            return Format.parseLocalDA(s);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            precision.lastField = Calendar.DAY_OF_MONTH;
            return Format.parseDA(null, s, ceil);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return Format.formatDA(tz, date);
        }
    },
    DT {
        public Temporal parseTemporal(String s, DatePrecision precision) {
            return Format.parseTemporalDT(s, precision);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            return Format.parseDT(tz, s, ceil, precision);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return Format.formatDT(tz, date, precision);
        }
    },
    TM {
        @Override
        public Temporal parseTemporal(String s, DatePrecision precision) {
            return Format.parseLocalTM(s, precision);
        }

        @Override
        public Date parse(TimeZone tz, String s, boolean ceil, DatePrecision precision) {
            return Format.parseTM(null, s, ceil, precision);
        }

        @Override
        public String format(TimeZone tz, Date date, DatePrecision precision) {
            return Format.formatTM(tz, date, precision);
        }
    };

    public abstract Temporal parseTemporal(String s, DatePrecision precision);

    public abstract Date parse(TimeZone tz, String val, boolean ceil, DatePrecision precision);

    public abstract String format(TimeZone tz, Date date, DatePrecision precision);

}
