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
package org.miaixz.bus.core.math;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 将浮点数类型的number转换成英语的表达方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EnglishNumberFormatter {

    /**
     * 简洁计数单位
     */
    private static final String[] NUMBER_SUFFIX = new String[] { "k", "w", "", "m", "", "", "b", "", "", "t", "", "",
            "p", "", "", "e" };

    /**
     * 将阿拉伯数字转为英文表达式
     *
     * @param x 阿拉伯数字，可以为{@link Number}对象，也可以是普通对象，最后会使用字符串方式处理
     * @return 英文表达式
     */
    public static String format(final Object x) {
        if (x != null) {
            return format(x.toString());
        } else {
            return Normal.EMPTY;
        }
    }

    /**
     * 将阿拉伯数字转化为简洁计数单位，例如 2100 = 2.1k 范围默认只到w
     *
     * @param value 被格式化的数字
     * @return 格式化后的数字
     */
    public static String formatSimple(final long value) {
        return formatSimple(value, true);
    }

    /**
     * 将阿拉伯数字转化为简介计数单位，例如 2100 = 2.1k
     *
     * @param value 对应数字的值
     * @param isTwo 控制是否为只为k、w，例如当为{@code false}时返回4.38m，{@code true}返回438.43w
     * @return 格式化后的数字
     */
    public static String formatSimple(final long value, final boolean isTwo) {
        if (value < 1000) {
            return String.valueOf(value);
        }
        int index = -1;
        double res = value;
        while (res > 10 && (!isTwo || index < 1)) {
            if (res >= 1000) {
                res = res / 1000;
                index++;
            }
            if (res > 10) {
                res = res / 10;
                index++;
            }
        }
        return String.format("%s%s", MathKit.format("#.##", res), NUMBER_SUFFIX[index]);
    }

    /**
     * 将阿拉伯数字转为英文表达式
     *
     * @param x 阿拉伯数字字符串
     * @return 英文表达式
     */
    private static String format(final String x) {
        final int z = x.indexOf("."); // 取小数点位置
        final String lstr;
        String rstr = "";
        if (z > -1) { // 看是否有小数，如果有，则分别取左边和右边
            lstr = x.substring(0, z);
            rstr = x.substring(z + 1);
        } else {
            // 否则就是全部
            lstr = x;
        }

        String lstrrev = StringKit.reverse(lstr); // 对左边的字串取反
        final String[] a = new String[5]; // 定义5个字串变量来存放解析出来的叁位一组的字串

        switch (lstrrev.length() % 3) {
        case 1:
            lstrrev += "00";
            break;
        case 2:
            lstrrev += "0";
            break;
        }
        StringBuilder lm = new StringBuilder(); // 用来存放转换后的整数部分
        for (int i = 0; i < lstrrev.length() / 3; i++) {
            a[i] = StringKit.reverse(lstrrev.substring(3 * i, 3 * i + 3)); // 截取第一个三位
            if (!"000".equals(a[i])) { // 用来避免这种情况：1000000 = one million
                // thousand only
                if (i != 0) {
                    lm.insert(0, transThree(a[i]) + Symbol.SPACE + parseMore(i) + Symbol.SPACE); // 加:
                    // thousand、million、billion
                } else {
                    // 防止i=0时， 在多加两个空格.
                    lm = new StringBuilder(transThree(a[i]));
                }
            } else {
                lm.append(transThree(a[i]));
            }
        }

        String xs = lm.length() == 0 ? "ZERO " : Symbol.SPACE; // 用来存放转换后小数部分
        if (z > -1) {
            xs += "AND CENTS " + transTwo(rstr) + Symbol.SPACE; // 小数部分存在时转换小数
        }

        return lm.toString().trim() + xs + "ONLY";
    }

    private static String parseTeen(final String x) {
        return Normal.EN_NUMBER_TEEN[Integer.parseInt(x) - 10];
    }

    private static String parseTen(final String x) {
        return Normal.EN_NUMBER_TEN[Integer.parseInt(x.substring(0, 1)) - 1];
    }

    private static String parseMore(final int i) {
        return Normal.EN_NUMBER_MORE[i];
    }

    /**
     * 两位
     *
     * @param x 字符
     * @return the string
     */
    private static String transTwo(String x) {
        final String value;
        // 判断位数
        if (x.length() > 2) {
            x = x.substring(0, 2);
        } else if (x.length() < 2) {
            // 单位数出现于小数部分，按照分对待
            x = x + "0";
        }

        if (x.startsWith("0")) {// 07 - seven 是否小於10
            value = parseLast(x);
        } else if (x.startsWith("1")) {// 17 seventeen 是否在10和20之间
            value = parseTeen(x);
        } else if (x.endsWith("0")) {// 是否在10与100之间的能被10整除的数
            value = parseTen(x);
        } else {
            value = parseTen(x) + Symbol.SPACE + parseLast(x);
        }
        return value;
    }

    /**
     * 三位数
     *
     * @param x 字符
     * @return the string
     */
    private static String transThree(final String x) {
        final String value;
        if (x.startsWith("0")) {// 是否小于100
            value = transTwo(x.substring(1));
        } else if ("00".equals(x.substring(1))) {// 是否被100整除
            value = parseLast(x.substring(0, 1)) + " HUNDRED";
        } else {
            value = parseLast(x.substring(0, 1)) + " HUNDRED AND " + transTwo(x.substring(1));
        }
        return value;
    }

    private static String parseLast(final String s) {
        return Normal.EN_NUMBER[Integer.parseInt(s.substring(s.length() - 1))];
    }

}
