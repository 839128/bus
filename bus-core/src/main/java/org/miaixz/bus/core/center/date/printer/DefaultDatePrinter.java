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
package org.miaixz.bus.core.center.date.printer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 默认日期基本信息类，提供默认的日期格式、时区和地域设置。
 * <ul>
 * <li>{@link #getPattern()} 返回 {@code null}</li>
 * <li>{@link #getTimeZone()} 返回 {@link TimeZone#getDefault()}</li>
 * <li>{@link #getLocale()} 返回 {@link Locale#getDefault()}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultDatePrinter implements DatePrinter, Serializable {

    @Serial
    private static final long serialVersionUID = 2852257378058L;

    /**
     * 获取日期格式模式。
     *
     * @return 始终返回 null
     */
    @Override
    public String getPattern() {
        return null;
    }

    /**
     * 获取时区。
     *
     * @return 默认时区
     */
    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    /**
     * 获取地域设置。
     *
     * @return 默认地域
     */
    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

}
