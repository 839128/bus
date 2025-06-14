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
package org.miaixz.bus.core.data.id;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Random;

import org.miaixz.bus.core.codec.No128;
import org.miaixz.bus.core.codec.binary.Crockford;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.RandomKit;

/**
 * ULID（Universally Unique Lexicographically Sortable Identifier）通用唯一词典分类标识符，特性：
 * <ul>
 * <li>与UUID的128位兼容性</li>
 * <li>每毫秒1.21e + 24个唯一ULID</li>
 * <li>按字典顺序(也就是字母顺序)排序</li>
 * <li>规范地编码为26个字符串，而不是UUID的36个字符</li>
 * <li>使用Crockford的base32获得更好的效率和可读性（每个字符5位）</li>
 * <li>不区分大小写</li>
 * <li>没有特殊字符（URL安全）</li>
 * <li>单调排序顺序（正确检测并处理相同的毫秒）</li>
 * </ul>
 * <p>
 * 参考：https://github.com/zjcscut/framework-mesh/blob/master/ulid4j/src/main/java/cn/vlts/ulid/ULID.java
 * 
 * <pre>{@code
 *   01AN4Z07BY      79KA1307SR9X4MV3
 *  |----------|    |----------------|
 *   Timestamp          Randomness
 *    48bits             80bits
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ULID implements Comparable<ULID>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852275998965L;

    /**
     * Timestamp component mask
     */
    private static final long TIMESTAMP_MASK = 0xffff000000000000L;
    /**
     * The length of randomness component of ULID
     */
    private static final int RANDOMNESS_BYTE_LEN = 10;
    /**
     * The least significant 64 bits increase overflow, 0xffffffffffffffffL + 1
     */
    private static final long OVERFLOW = 0x0000000000000000L;

    private final No128 idValue;

    /**
     * 构造
     *
     * @param no128 16位数字
     */
    public ULID(final No128 no128) {
        this.idValue = no128;
    }

    /**
     * 创建一个新的ULID，使用当前系统时间戳和随机数
     *
     * @return ULID
     */
    public static ULID of() {
        return of(System.currentTimeMillis());
    }

    /**
     * 创建一个新的ULID，使用指定系统时间戳和随机数
     *
     * @param timestamp 时间戳
     * @return ULID
     */
    public static ULID of(final long timestamp) {
        return of(timestamp, RandomKit.getRandom());
    }

    /**
     * 创建一个新的ULID，使用指定系统时间戳和指定随机对象
     *
     * @param timestamp 时间戳
     * @param random    {@link Random}
     * @return ULID
     */
    public static ULID of(final long timestamp, final Random random) {
        return of(timestamp, RandomKit.randomBytes(RANDOMNESS_BYTE_LEN, random));
    }

    /**
     * 创建一个新的ULID，使用指定系统时间戳和指定填充数
     *
     * @param timestamp  时间戳
     * @param randomness 指定填充数
     * @return ULID
     */
    public static ULID of(final long timestamp, final byte[] randomness) {
        // 时间戳最多为48 bit(6 bytes)
        checkTimestamp(timestamp);
        Assert.notNull(randomness);
        // 随机数部分长度必须为80 bit(10 bytes)
        Assert.isTrue(RANDOMNESS_BYTE_LEN == randomness.length, "Invalid randomness");

        long msb = 0;
        // 时间戳左移16位，低位补零准备填入部分随机数位，即16_bit_uint_random
        msb |= timestamp << 16;
        // randomness[0]左移0位填充到16_bit_uint_random的高8位，randomness[1]填充到16_bit_uint_random的低8位
        msb |= (long) (randomness[0x0] & 0xff) << 8;
        // randomness[1]填充到16_bit_uint_random的低8位
        msb |= randomness[0x1] & 0xff;

        return new ULID(new No128(msb, ByteKit.toLong(randomness, 2, ByteOrder.BIG_ENDIAN)));
    }

    /**
     * 解析一个Crockford`s Base32的ULID
     *
     * @param ulidString Crockford`s Base32的ULID
     * @return ULID
     */
    public static ULID of(final String ulidString) {
        Objects.requireNonNull(ulidString, "ulidString must not be null!");
        if (ulidString.length() != 26) {
            throw new IllegalArgumentException("ulidString must be exactly 26 chars long.");
        }

        final String timeString = ulidString.substring(0, 10);
        final long time = Crockford.parseCrockford(timeString);
        checkTimestamp(time);

        final String part1String = ulidString.substring(10, 18);
        final String part2String = ulidString.substring(18);
        final long part1 = Crockford.parseCrockford(part1String);
        final long part2 = Crockford.parseCrockford(part2String);

        final long most = (time << 16) | (part1 >>> 24);
        final long least = part2 | (part1 << 40);
        return new ULID(new No128(most, least));
    }

    /**
     * 从bytes解析ULID
     *
     * @param data bytes,16位，0-7为mostSignificantBits，8-15为leastSignificantBits
     * @return ULID
     */
    public static ULID of(final byte[] data) {
        Objects.requireNonNull(data, "data must not be null!");
        if (data.length != 16) {
            throw new IllegalArgumentException("data must be 16 bytes in length!");
        }
        long mostSignificantBits = 0;
        long leastSignificantBits = 0;
        for (int i = 0; i < 8; i++) {
            mostSignificantBits = (mostSignificantBits << 8) | (data[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            leastSignificantBits = (leastSignificantBits << 8) | (data[i] & 0xff);
        }
        return new ULID(new No128(mostSignificantBits, leastSignificantBits));
    }

    /**
     * 检查日期
     *
     * @param timestamp 时间戳
     */
    private static void checkTimestamp(final long timestamp) {
        Assert.isTrue((timestamp & TIMESTAMP_MASK) == 0,
                "ULID does not support timestamps after +10889-08-02T05:31:50.655Z!");
    }

    /**
     * 获取最高有效位（Most Significant Bit），64 bit（8 bytes）
     *
     * @return 最高有效位（Most Significant Bit），64 bit（8 bytes）
     */
    public long getMostSignificantBits() {
        return this.idValue.getMostSigBits();
    }

    /**
     * 获取最低有效位（Least Significant Bit），64 bit（8 bytes）
     *
     * @return 最低有效位（Least Significant Bit），64 bit（8 bytes）
     */
    public long getLeastSignificantBits() {
        return this.idValue.getLeastSigBits();
    }

    /**
     * 获取ULID的时间戳部分
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return this.idValue.getMostSigBits() >>> 16;
    }

    /**
     * 获取ULID的随机数部分
     *
     * @return 随机数部分
     */
    public byte[] getRandomness() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        final byte[] randomness = new byte[RANDOMNESS_BYTE_LEN];
        // 这里不需要& 0xff，因为多余的位会被截断
        randomness[0x0] = (byte) (msb >>> 8);
        randomness[0x1] = (byte) msb;

        ByteKit.fill(lsb, 2, ByteOrder.BIG_ENDIAN, randomness);
        return randomness;
    }

    /**
     * 自增ULID
     *
     * @return 返回自增ULID
     */
    public ULID increment() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        long newMsb = msb;
        final long newLsb = lsb + 1;
        if (newLsb == OVERFLOW) {
            newMsb += 1;
        }
        return new ULID(new No128(newMsb, newLsb));
    }

    /**
     * 获取下一个有序的ULID
     *
     * @param timestamp 时间戳
     * @return 如果给定时间戳与当前ULID相同，则返回自增ULID，否则返回一个新的ULID
     */
    public ULID nextMonotonic(final long timestamp) {
        if (getTimestamp() == timestamp) {
            return increment();
        }
        return of(timestamp);
    }

    /**
     * 转为bytes值,16位，0-7为mostSignificantBits，8-15为leastSignificantBits
     *
     * @return bytes值
     */
    public byte[] toBytes() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        final byte[] result = new byte[16];
        for (int i = 0; i < 8; i++) {
            result[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xFF);
        }
        for (int i = 8; i < 16; i++) {
            result[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xFF);
        }

        return result;
    }

    /**
     * 转为UUID
     *
     * @return UUID
     */
    public UUID toUUID() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        return new UUID(msb, lsb);
    }

    /**
     * 转为JDK的UUID
     *
     * @return UUID
     */
    public java.util.UUID toJdkUUID() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        return new java.util.UUID(msb, lsb);
    }

    @Override
    public int compareTo(final ULID o) {
        return this.idValue.compareTo(o.idValue);
    }

    @Override
    public boolean equals(final Object object) {
        if ((Objects.isNull(object)) || (object.getClass() != ULID.class)) {
            return false;
        }
        final ULID id = (ULID) object;
        return this.idValue.equals(id.idValue);
    }

    @Override
    public int hashCode() {
        return this.idValue.hashCode();
    }

    @Override
    public String toString() {
        final long msb = this.idValue.getMostSigBits();
        final long lsb = this.idValue.getLeastSigBits();
        final char[] buffer = new char[26];

        Crockford.writeCrockford(buffer, getTimestamp(), 10, 0);
        long value = ((msb & 0xFFFFL) << 24);
        final long interim = (lsb >>> 40);
        value = value | interim;
        Crockford.writeCrockford(buffer, value, 8, 10);
        Crockford.writeCrockford(buffer, lsb, 8, 18);

        return new String(buffer);
    }

}
