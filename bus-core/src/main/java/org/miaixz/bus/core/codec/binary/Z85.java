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
package org.miaixz.bus.core.codec.binary;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;

/**
 * ZeroMQ Z85实现， 定义见：<a href="https://rfc.zeromq.org/spec:32/Z85/">ZeroMQ Z85</a>
 * <p>
 * 参考实现：https://github.com/cometd/cometd/blob/6.0.x/cometd-java/cometd-java-common/src/main/java/org/cometd/common/Z85.java
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Z85 implements Encoder<byte[], String>, Decoder<String, byte[]>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852282097928L;

    private static final char[] encodeTable = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
            'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '.', '-', ':', '+', '=', '^', '!', '/', '*', '?', '&', '<', '>',
            '(', ')', '[', ']', '{', '}', '@', '%', '$', '#' };
    private static final int[] decodeTable = new int[] { 0x00, 0x44, 0x00, 0x54, 0x53, 0x52, 0x48, 0x00, 0x4B, 0x4C,
            0x46, 0x41, 0x00, 0x3F, 0x3E, 0x45, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x40, 0x00,
            0x49, 0x42, 0x4A, 0x47, 0x51, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30,
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x4D, 0x00, 0x4E, 0x43, 0x00,
            0x00, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A,
            0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20, 0x21, 0x22, 0x23, 0x4F, 0x00, 0x50, 0x00, 0x00 };

    @Override
    public String encode(final byte[] data) {
        // SPEC: The binary frame SHALL have a length that is divisible by 4 with no remainder.
        final int length = data.length;
        final int remainder = length & 3; // Equivalent to length % 4.
        // Support inputs that are not divisible by 4 with no remainder.
        final int padding = 4 - (remainder == 0 ? 4 : remainder);

        // SPEC: 4 octets convert into 5 printable characters.
        // char.length = length + 1/4 length == 5/4 length.
        final char[] chars = new char[length + (length >>> 2) + (remainder == 0 ? 0 : 1)];
        int idx = 0;
        long value = 0;
        for (int i = 0; i < length + padding; ++i) {
            // Accumulate the octets into a long.
            final boolean isPadBlock = i >= length;
            value = (value << 8) + (isPadBlock ? 0 : data[i] & 0xFF);

            // Decode every 4 octets into 5 characters.
            if (((i + 1) & 3) == 0) {
                for (int j = 0; j < 5; ++j) {
                    if (j > 0) {
                        value /= 85;
                    }
                    if (!isPadBlock || j >= padding) {
                        final int code = (int) (value % 85);
                        chars[idx + 4 - j] = encodeTable[code];
                    }
                }
                idx += 5;
                value = 0;
            }
        }
        return new String(chars);
    }

    @Override
    public byte[] decode(String encoded) {
        final int remainder = encoded.length() % 5;
        final int padding = 5 - (remainder == 0 ? 5 : remainder);
        final StringBuilder encodedBuilder = new StringBuilder(encoded);
        for (int p = 0; p < padding; ++p) {
            encodedBuilder.append(encodeTable[encodeTable.length - 1]);
        }
        encoded = encodedBuilder.toString();
        final int length = encoded.length();
        final byte[] bytes = new byte[((length << 2) / 5) - padding];
        long value = 0;
        int index = 0;
        for (int i = 0; i < length; ++i) {
            // Accumulate the characters into a long.
            final int code = encoded.charAt(i) - 32;
            value = value * 85 + decodeTable[code];

            // Encode every 5 characters into 4 octets.
            if ((i + 1) % 5 == 0) {
                for (int j = 0; j < 4; ++j) {
                    final int idx = index + 3 - j;
                    if (idx < bytes.length) {
                        bytes[idx] = (byte) (value & 0xFF);
                    }
                    value >>>= 8;
                }
                index += 4;
                value = 0;
            }
        }
        return bytes;
    }

}
