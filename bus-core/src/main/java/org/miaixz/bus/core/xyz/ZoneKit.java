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
package org.miaixz.bus.core.xyz;

import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * {@link ZoneId}和{@link TimeZone}相关封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZoneKit {

    /**
     * UTC 的 ZoneID
     */
    public static final TimeZone ZONE_UTC = TimeZone.getTimeZone("UTC");
    /**
     * UTC 的 TimeZone
     */
    public static final ZoneId ZONE_ID_UTC = ZONE_UTC.toZoneId();

    /**
     * {@link ZoneId}转换为{@link TimeZone}，{@code null}则返回系统默认值
     *
     * @param zoneId {@link ZoneId}，{@code null}则返回系统默认值
     * @return {@link TimeZone}
     */
    public static TimeZone toTimeZone(final ZoneId zoneId) {
        if (null == zoneId) {
            return TimeZone.getDefault();
        }

        return TimeZone.getTimeZone(zoneId);
    }

    /**
     * {@link TimeZone}转换为{@link ZoneId}，{@code null}则返回系统默认值
     *
     * @param timeZone {@link TimeZone}，{@code null}则返回系统默认值
     * @return {@link ZoneId}
     */
    public static ZoneId toZoneId(final TimeZone timeZone) {
        if (null == timeZone) {
            return ZoneId.systemDefault();
        }

        return timeZone.toZoneId();
    }

    /**
     * 获取指定偏移量的可用时区，如果有多个时区匹配，使用第一个
     *
     * @param rawOffset 偏移量
     * @param timeUnit  偏移量单位
     * @return 时区
     */
    public static TimeZone getTimeZoneByOffset(final int rawOffset, final TimeUnit timeUnit) {
        final String id = getAvailableID(rawOffset, timeUnit);
        return null == id ? null : TimeZone.getTimeZone(id);
    }

    /**
     * 获取指定偏移量的可用时区ID，如果有多个时区匹配，使用第一个
     *
     * @param rawOffset 偏移量
     * @param timeUnit  偏移量单位
     * @return 时区ID，未找到返回{@code null}
     */
    public static String getAvailableID(final int rawOffset, final TimeUnit timeUnit) {
        final String[] availableIDs = TimeZone
                .getAvailableIDs((int) ObjectKit.defaultIfNull(timeUnit, TimeUnit.MILLISECONDS).toMillis(rawOffset));
        return ArrayKit.isEmpty(availableIDs) ? null : availableIDs[0];
    }

}
