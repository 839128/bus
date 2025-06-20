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
package org.miaixz.bus.core.codec;

import org.miaixz.bus.core.lang.Assert;

/**
 * RotN（rotate by N places），回转N位密码，是一种简易的替换式密码，也是过去在古罗马开发的凯撒加密的一种变体。
 * 代码来自：<a href="https://github.com/orclight/jencrypt">https://github.com/orclight/jencrypt</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RotateN {

    private static final char aCHAR = 'a';
    private static final char zCHAR = 'z';
    private static final char ACHAR = 'A';
    private static final char ZCHAR = 'Z';
    private static final char CHAR0 = '0';
    private static final char CHAR9 = '9';

    /**
     * RotateN-13编码，同时编码数字
     *
     * @param message 被编码的消息
     * @return 编码后的字符串
     */
    public static String encode13(final String message) {
        return encode13(message, true);
    }

    /**
     * RotateN-13编码
     *
     * @param message        被编码的消息
     * @param isEncodeNumber 是否编码数字
     * @return 编码后的字符串
     */
    public static String encode13(final String message, final boolean isEncodeNumber) {
        return encode(message, 13, isEncodeNumber);
    }

    /**
     * RotN编码
     *
     * @param message        被编码的消息
     * @param offset         位移，常用位移13
     * @param isEncodeNumber 是否编码数字
     * @return 编码后的字符串
     */
    public static String encode(final String message, final int offset, final boolean isEncodeNumber) {
        final int len = message.length();
        final char[] chars = new char[len];

        for (int i = 0; i < len; i++) {
            chars[i] = encodeChar(message.charAt(i), offset, isEncodeNumber);
        }
        return new String(chars);
    }

    /**
     * RotateN-13解码，同时解码数字
     *
     * @param rot 被解码的消息密文
     * @return 解码后的字符串
     */
    public static String decode13(final String rot) {
        return decode13(rot, true);
    }

    /**
     * RotateN-13解码
     *
     * @param rot            被解码的消息密文
     * @param isDecodeNumber 是否解码数字
     * @return 解码后的字符串
     */
    public static String decode13(final String rot, final boolean isDecodeNumber) {
        return decode(rot, 13, isDecodeNumber);
    }

    /**
     * RotN解码
     *
     * @param rot            被解码的消息密文
     * @param offset         位移，常用位移13
     * @param isDecodeNumber 是否解码数字
     * @return 解码后的字符串
     */
    public static String decode(final String rot, final int offset, final boolean isDecodeNumber) {
        Assert.notNull(rot, "rot must not be null");
        final int len = rot.length();
        final char[] chars = new char[len];

        for (int i = 0; i < len; i++) {
            chars[i] = decodeChar(rot.charAt(i), offset, isDecodeNumber);
        }
        return new String(chars);
    }

    /**
     * 解码字符
     *
     * @param c              字符
     * @param offset         位移
     * @param isDecodeNumber 是否解码数字
     * @return 解码后的字符串
     */
    private static char encodeChar(char c, final int offset, final boolean isDecodeNumber) {
        if (isDecodeNumber) {
            if (c >= CHAR0 && c <= CHAR9) {
                c -= CHAR0;
                c = (char) ((c + offset) % 10);
                c += CHAR0;
            }
        }

        // A == 65, Z == 90
        if (c >= ACHAR && c <= ZCHAR) {
            c -= ACHAR;
            c = (char) ((c + offset) % 26);
            c += ACHAR;
        }
        // a == 97, z == 122.
        else if (c >= aCHAR && c <= zCHAR) {
            c -= aCHAR;
            c = (char) ((c + offset) % 26);
            c += aCHAR;
        }
        return c;
    }

    /**
     * 编码字符
     *
     * @param c              字符
     * @param offset         位移
     * @param isDecodeNumber 是否编码数字
     * @return 编码后的字符串
     */
    private static char decodeChar(final char c, final int offset, final boolean isDecodeNumber) {
        int temp = c;
        // if converting numbers is enabled
        if (isDecodeNumber) {
            if (temp >= CHAR0 && temp <= CHAR9) {
                temp -= CHAR0;
                temp = temp - offset;
                while (temp < 0) {
                    temp += 10;
                }
                temp += CHAR0;
            }
        }

        // A == 65, Z == 90
        if (temp >= ACHAR && temp <= ZCHAR) {
            temp -= ACHAR;

            temp = temp - offset;
            while (temp < 0) {
                temp = 26 + temp;
            }
            temp += ACHAR;
        } else if (temp >= aCHAR && temp <= zCHAR) {
            temp -= aCHAR;

            temp = temp - offset;
            if (temp < 0)
                temp = 26 + temp;

            temp += aCHAR;
        }
        return (char) temp;
    }

}
