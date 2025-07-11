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
package org.miaixz.bus.core.center.date.culture.en;

import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;

/**
 * 月份枚举 与Calendar中的月份int值对应
 *
 * @author Kimi Liu
 * @see Calendar#JANUARY
 * @see Calendar#FEBRUARY
 * @see Calendar#MARCH
 * @see Calendar#APRIL
 * @see Calendar#MAY
 * @see Calendar#JUNE
 * @see Calendar#JULY
 * @see Calendar#AUGUST
 * @see Calendar#SEPTEMBER
 * @see Calendar#OCTOBER
 * @see Calendar#NOVEMBER
 * @see Calendar#DECEMBER
 * @see Calendar#UNDECIMBER
 * @since Java 17+
 */
public enum Month {
    /**
     * 一月
     */
    JANUARY(Calendar.JANUARY),
    /**
     * 二月
     */
    FEBRUARY(Calendar.FEBRUARY),
    /**
     * 三月
     */
    MARCH(Calendar.MARCH),
    /**
     * 四月
     */
    APRIL(Calendar.APRIL),
    /**
     * 五月
     */
    MAY(Calendar.MAY),
    /**
     * 六月
     */
    JUNE(Calendar.JUNE),
    /**
     * 七月
     */
    JULY(Calendar.JULY),
    /**
     * 八月
     */
    AUGUST(Calendar.AUGUST),
    /**
     * 九月
     */
    SEPTEMBER(Calendar.SEPTEMBER),
    /**
     * 十月
     */
    OCTOBER(Calendar.OCTOBER),
    /**
     * 十一月
     */
    NOVEMBER(Calendar.NOVEMBER),
    /**
     * 十二月
     */
    DECEMBER(Calendar.DECEMBER),
    /**
     * 十三月，仅用于农历
     */
    UNDECIMBER(Calendar.UNDECIMBER);

    private static final Month[] ENUMS = Month.values();

    /**
     * 对应值，见{@link Calendar}
     */
    private final int value;

    /**
     * 构造
     *
     * @param value 对应值，见{@link Calendar}
     */
    Month(final int value) {
        this.value = value;
    }

    /**
     * 将 {@link Calendar}月份相关值转换为Month枚举对象 未找到返回{@code null}
     *
     * @param calendarMonthIntValue Calendar中关于Month的int值，从0开始
     * @return Month
     * @see Calendar#JANUARY
     * @see Calendar#FEBRUARY
     * @see Calendar#MARCH
     * @see Calendar#APRIL
     * @see Calendar#MAY
     * @see Calendar#JUNE
     * @see Calendar#JULY
     * @see Calendar#AUGUST
     * @see Calendar#SEPTEMBER
     * @see Calendar#OCTOBER
     * @see Calendar#NOVEMBER
     * @see Calendar#DECEMBER
     * @see Calendar#UNDECIMBER
     */
    public static Month of(final int calendarMonthIntValue) {
        if (calendarMonthIntValue >= ENUMS.length || calendarMonthIntValue < 0) {
            return null;
        }
        return ENUMS[calendarMonthIntValue];
    }

    /**
     * 解析别名为Month对象，别名如：jan或者JANUARY，不区分大小写
     *
     * @param name 别名值
     * @return 月份枚举Month，非空
     * @throws IllegalArgumentException 如果别名无对应的枚举，抛出此异常
     */
    public static Month of(final String name) throws IllegalArgumentException {
        if (null != name && name.length() > 1) {
            switch (Character.toLowerCase(name.charAt(0))) {
            case 'a':
                switch (Character.toLowerCase(name.charAt(1))) {
                case 'p':
                    return APRIL; // april
                case 'u':
                    return AUGUST; // august
                }
                break;
            case 'j':
                if (Character.toLowerCase(name.charAt(1)) == 'a') {
                    return JANUARY; // january
                }
                switch (Character.toLowerCase(name.charAt(2))) {
                case 'n':
                    return JUNE; // june
                case 'l':
                    return JULY; // july
                }
                break;
            case 'f':
                return FEBRUARY; // february
            case 'm':
                switch (Character.toLowerCase(name.charAt(2))) {
                case 'r':
                    return MARCH; // march
                case 'y':
                    return MAY; // may
                }
                break;
            case 's':
                return SEPTEMBER; // september
            case 'o':
                return OCTOBER; // october
            case 'n':
                return NOVEMBER; // november
            case 'd':
                return DECEMBER; // december
            case '一':
                return JANUARY;
            case '二':
                return FEBRUARY;
            case '三':
                return MARCH;
            case '四':
                return APRIL;
            case '五':
                return MAY;
            case '六':
                return JUNE;
            case '七':
                return JULY;
            case '八':
                return AUGUST;
            case '九':
                return SEPTEMBER;
            case '十':
                switch (Character.toLowerCase(name.charAt(1))) {
                case '一':
                    return NOVEMBER;
                case '二':
                    return DECEMBER;
                }
                return OCTOBER;
            }
        }

        throw new IllegalArgumentException("Invalid Month name: " + name);
    }

    /**
     * {@link java.time.Month}转换为Month对象
     *
     * @param month {@link java.time.Month}
     * @return Month
     */
    public static Month of(final java.time.Month month) {
        return of(month.ordinal());
    }

    /**
     * 获得指定月的最后一天
     *
     * @param month      月份，从0开始
     * @param isLeapYear 是否为闰年，闰年只对二月有影响
     * @return 最后一天，可能为28,29,30,31
     */
    public static int getLastDay(final int month, final boolean isLeapYear) {
        final Month of = of(month);
        Assert.notNull(of, "Invalid Month base 0: " + month);
        return of.getLastDay(isLeapYear);
    }

    /**
     * 获取{@link Calendar}中的对应值 此值从0开始，即0表示一月
     *
     * @return {@link Calendar}中的对应月份值，从0开始计数
     */
    public int getValue() {
        return this.value;
    }

    /**
     * 获取月份值，此值与{@link java.time.Month}对应 此值从1开始，即1表示一月
     *
     * @return 月份值，对应{@link java.time.Month}，从1开始计数
     */
    public int getIsoValue() {
        Assert.isFalse(this == UNDECIMBER, "Unsupported Undecimber field");
        return getValue() + 1;
    }

    /**
     * 获取此月份最后一天的值 不支持 {@link #UNDECIMBER}
     *
     * @param isLeapYear 是否闰年
     * @return 此月份最后一天的值
     */
    public int getLastDay(final boolean isLeapYear) {
        switch (this) {
        case FEBRUARY:
            return isLeapYear ? 29 : 28;
        case APRIL:
        case JUNE:
        case SEPTEMBER:
        case NOVEMBER:
            return 30;
        default:
            return 31;
        }
    }

    /**
     * 转换为{@link java.time.Month}
     *
     * @return {@link java.time.Month}
     */
    public java.time.Month toJdkMonth() {
        return java.time.Month.of(getIsoValue());
    }

    /**
     * 获取显示名称
     *
     * @param style 名称风格
     * @return 显示名称
     */
    public String getDisplayName(final TextStyle style) {
        return getDisplayName(style, Locale.getDefault());
    }

    /**
     * 获取显示名称
     *
     * @param style  名称风格
     * @param locale {@link Locale}
     * @return 显示名称
     */
    public String getDisplayName(final TextStyle style, final Locale locale) {
        return toJdkMonth().getDisplayName(style, locale);
    }

}
