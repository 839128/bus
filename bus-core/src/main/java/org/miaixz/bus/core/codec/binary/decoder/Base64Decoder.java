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
package org.miaixz.bus.core.codec.binary.decoder;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.mutable.MutableInt;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Base64解码实现 此解码保留的原因是，JDK提供的解码需要指定是否为URL安全的或是否换行，此解码无需区分此解码对非Base64字符忽略
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base64Decoder implements Decoder<byte[], byte[]>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852282603562L;

    /**
     * 单例对象
     */
    public static Base64Decoder INSTANCE = new Base64Decoder();
    /**
     * Base64解码表，共128位，-1表示非base64字符，-2表示padding
     */
    private final byte[] DECODE_TABLE = {
            // 0 1 2 3 4 5 6 7 8 9 A B C D E F
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 00-0f
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, // 10-1f
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, // 20-2f + - /
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, // 30-3f 0-9，-2的位置是'='
            -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, // 40-4f A-O
            15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, // 50-5f P-Z _
            -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, // 60-6f a-o
            41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 // 70-7a p-z
    };

    /**
     * 解码Base64
     *
     * @param in 输入
     * @return 解码后的bytes
     */
    @Override
    public byte[] decode(final byte[] in) {
        if (ArrayKit.isEmpty(in)) {
            return in;
        }
        return decode(in, 0, in.length);
    }

    /**
     * 解码Base64
     *
     * @param in     输入
     * @param pos    开始位置
     * @param length 长度
     * @return 解码后的bytes
     */
    public byte[] decode(final byte[] in, final int pos, final int length) {
        if (ArrayKit.isEmpty(in)) {
            return in;
        }

        final MutableInt offset = new MutableInt(pos);

        byte sestet0;
        byte sestet1;
        byte sestet2;
        byte sestet3;
        final int maxPos = pos + length - 1;
        int octetId = 0;
        final byte[] octet = new byte[length * 3 / 4];// over-estimated if non-base64 characters present
        while (offset.intValue() <= maxPos) {
            sestet0 = getNextValidDecodeByte(in, offset, maxPos);
            sestet1 = getNextValidDecodeByte(in, offset, maxPos);
            sestet2 = getNextValidDecodeByte(in, offset, maxPos);
            sestet3 = getNextValidDecodeByte(in, offset, maxPos);

            if (Normal.__2 != sestet1) {
                octet[octetId++] = (byte) ((sestet0 << 2) | (sestet1 >>> 4));
            }
            if (Normal.__2 != sestet2) {
                octet[octetId++] = (byte) (((sestet1 & 0xf) << 4) | (sestet2 >>> 2));
            }
            if (Normal.__2 != sestet3) {
                octet[octetId++] = (byte) (((sestet2 & 3) << 6) | sestet3);
            }
        }

        if (octetId == octet.length) {
            return octet;
        } else {
            // 如果有非Base64字符混入，则实际结果比解析的要短，截取之
            return ArrayKit.copy(octet, new byte[octetId], octetId);
        }
    }

    /**
     * 给定的字符是否为Base64字符
     *
     * @param octet 被检查的字符
     * @return 是否为Base64字符
     */
    public boolean isBase64Code(final byte octet) {
        return octet == Symbol.C_EQUAL || (octet >= 0 && octet < DECODE_TABLE.length && DECODE_TABLE[octet] != -1);
    }

    /**
     * 获取下一个有效的byte字符
     *
     * @param in     输入
     * @param pos    当前位置，调用此方法后此位置保持在有效字符的下一个位置
     * @param maxPos 最大位置
     * @return 有效字符，如果达到末尾返回
     */
    private byte getNextValidDecodeByte(final byte[] in, final MutableInt pos, final int maxPos) {
        byte base64Byte;
        byte decodeByte;
        while (pos.intValue() <= maxPos) {
            base64Byte = in[pos.intValue()];
            pos.increment();
            if (base64Byte > -1) {
                decodeByte = DECODE_TABLE[base64Byte];
                if (decodeByte > -1) {
                    return decodeByte;
                }
            }
        }
        // padding if reached max position
        return Normal.__2;
    }

}
