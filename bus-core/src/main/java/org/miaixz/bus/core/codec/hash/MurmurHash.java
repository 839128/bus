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
package org.miaixz.bus.core.codec.hash;

import java.nio.ByteOrder;

import org.miaixz.bus.core.codec.No128;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;

/**
 * Murmur3 32bit、64bit、128bit 哈希算法实现 此算法来自于：<a href=
 * "https://github.com/xlturing/Simhash4J/blob/master/src/main/java/bee/simhash/main/Murmur3.java">...</a>
 *
 * <p>
 * 32-bit Java port of https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#94 128-bit Java port of
 * https://code.google.com/p/smhasher/source/browse/trunk/MurmurHash3.cpp#255
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MurmurHash implements Hash32<byte[]>, Hash64<byte[]>, Hash128<byte[]> {

    /**
     * 单例
     */
    public static final MurmurHash INSTANCE = new MurmurHash();

    // Constants for 32 bit variant
    private static final int C1_32 = 0xcc9e2d51;
    private static final int C2_32 = 0x1b873593;
    private static final int R1_32 = 15;
    private static final int R2_32 = 13;
    private static final int M_32 = 5;
    private static final int N_32 = 0xe6546b64;

    // Constants for 128 bit variant
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int R3 = 33;
    private static final int M = 5;
    private static final int N1 = 0x52dce729;
    private static final int N2 = 0x38495ab5;

    private static final int DEFAULT_SEED = 0;
    private static final java.nio.charset.Charset DEFAULT_CHARSET = Charset.UTF_8;
    private static final ByteOrder DEFAULT_ORDER = ByteOrder.LITTLE_ENDIAN;

    private static int mix32(int k, int hash) {
        k *= C1_32;
        k = Integer.rotateLeft(k, R1_32);
        k *= C2_32;
        hash ^= k;
        return Integer.rotateLeft(hash, R2_32) * M_32 + N_32;
    }

    private static int fmix32(int hash) {
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);
        return hash;
    }

    private static long fmix64(long h) {
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= (h >>> 33);
        return h;
    }

    @Override
    public Number encode(final byte[] bytes) {
        return hash128(bytes);
    }

    /**
     * Murmur3 32-bit Hash值计算
     *
     * @param data 数据
     * @return Hash值
     */
    public int hash32(final CharSequence data) {
        return hash32(ByteKit.toBytes(data, DEFAULT_CHARSET));
    }

    /**
     * Murmur3 32-bit Hash值计算
     *
     * @param data 数据
     * @return Hash值
     */
    @Override
    public int hash32(final byte[] data) {
        return hash32(data, data.length, DEFAULT_SEED);
    }

    /**
     * Murmur3 32-bit Hash值计算
     *
     * @param data   数据
     * @param length 长度
     * @param seed   种子，默认0
     * @return Hash值
     */
    public int hash32(final byte[] data, final int length, final int seed) {
        return hash32(data, 0, length, seed);
    }

    /**
     * Murmur3 32-bit Hash值计算
     *
     * @param data   数据
     * @param offset 数据开始位置
     * @param length 长度
     * @param seed   种子，默认0
     * @return Hash值
     */
    public int hash32(final byte[] data, final int offset, final int length, final int seed) {
        int hash = seed;
        final int nblocks = length >> 2;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int i4 = offset + (i << 2);
            final int k = ByteKit.toInt(data, i4, DEFAULT_ORDER);
            // mix functions
            hash = mix32(k, hash);
        }

        // tail
        final int idx = offset + (nblocks << 2);
        int k1 = 0;
        switch (offset + length - idx) {
        case 3:
            k1 ^= (data[idx + 2] & 0xff) << 16;
        case 2:
            k1 ^= (data[idx + 1] & 0xff) << 8;
        case 1:
            k1 ^= (data[idx] & 0xff);

            // mix functions
            k1 *= C1_32;
            k1 = Integer.rotateLeft(k1, R1_32);
            k1 *= C2_32;
            hash ^= k1;
        }

        // finalization
        hash ^= length;
        return fmix32(hash);
    }

    /**
     * Murmur3 64-bit Hash值计算
     *
     * @param data 数据
     * @return Hash值
     */
    public long hash64(final CharSequence data) {
        return hash64(ByteKit.toBytes(data, DEFAULT_CHARSET));
    }

    /**
     * Murmur3 64-bit 算法 This is essentially MSB 8 bytes of Murmur3 128-bit variant.
     *
     * @param data 数据
     * @return Hash值
     */
    @Override
    public long hash64(final byte[] data) {
        return hash64(data, data.length, DEFAULT_SEED);
    }

    /**
     * 类Murmur3 64-bit 算法 This is essentially MSB 8 bytes of Murmur3 128-bit variant.
     *
     * @param data   数据
     * @param length 长度
     * @param seed   种子，默认0
     * @return Hash值
     */
    public long hash64(final byte[] data, final int length, final int seed) {
        long hash = seed;
        final int nblocks = length >> 3;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int i8 = i << 3;
            long k = ByteKit.toLong(data, i8, DEFAULT_ORDER);

            // mix functions
            k *= C1;
            k = Long.rotateLeft(k, R1);
            k *= C2;
            hash ^= k;
            hash = Long.rotateLeft(hash, R2) * M + N1;
        }

        // tail
        long k1 = 0;
        final int tailStart = nblocks << 3;
        switch (length - tailStart) {
        case 7:
            k1 ^= ((long) data[tailStart + 6] & 0xff) << 48;
        case 6:
            k1 ^= ((long) data[tailStart + 5] & 0xff) << 40;
        case 5:
            k1 ^= ((long) data[tailStart + 4] & 0xff) << 32;
        case 4:
            k1 ^= ((long) data[tailStart + 3] & 0xff) << 24;
        case 3:
            k1 ^= ((long) data[tailStart + 2] & 0xff) << 16;
        case 2:
            k1 ^= ((long) data[tailStart + 1] & 0xff) << 8;
        case 1:
            k1 ^= ((long) data[tailStart] & 0xff);
            k1 *= C1;
            k1 = Long.rotateLeft(k1, R1);
            k1 *= C2;
            hash ^= k1;
        }

        // finalization
        hash ^= length;
        hash = fmix64(hash);

        return hash;
    }

    /**
     * Murmur3 128-bit Hash值计算
     *
     * @param data 数据
     * @return Hash值 (2 longs)
     */
    public No128 hash128(final CharSequence data) {
        return hash128(ByteKit.toBytes(data, DEFAULT_CHARSET));
    }

    /**
     * Murmur3 128-bit 算法.
     *
     * @param data -数据
     * @return Hash值 (2 longs)
     */
    @Override
    public No128 hash128(final byte[] data) {
        return hash128(data, data.length, DEFAULT_SEED);
    }

    /**
     * Murmur3 128-bit variant.
     *
     * @param data   数据
     * @param length 长度
     * @param seed   种子，默认0
     * @return Hash值(2 longs)
     */
    public No128 hash128(final byte[] data, final int length, final int seed) {
        return hash128(data, 0, length, seed);
    }

    /**
     * Murmur3 128-bit variant.
     *
     * @param data   数据
     * @param offset 数据开始位置
     * @param length 长度
     * @param seed   种子，默认0
     * @return Hash值(2 longs)
     */
    public No128 hash128(final byte[] data, final int offset, final int length, int seed) {
        // 避免负数的种子
        seed &= 0xffffffffL;

        long h1 = seed;
        long h2 = seed;
        final int nblocks = length >> 4;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int i16 = offset + (i << 4);
            long k1 = ByteKit.toLong(data, i16, DEFAULT_ORDER);
            long k2 = ByteKit.toLong(data, i16 + 8, DEFAULT_ORDER);

            // mix functions for k1
            k1 *= C1;
            k1 = Long.rotateLeft(k1, R1);
            k1 *= C2;
            h1 ^= k1;
            h1 = Long.rotateLeft(h1, R2);
            h1 += h2;
            h1 = h1 * M + N1;

            // mix functions for k2
            k2 *= C2;
            k2 = Long.rotateLeft(k2, R3);
            k2 *= C1;
            h2 ^= k2;
            h2 = Long.rotateLeft(h2, R1);
            h2 += h1;
            h2 = h2 * M + N2;
        }

        // tail
        long k1 = 0;
        long k2 = 0;
        final int tailStart = offset + (nblocks << 4);
        switch (offset + length - tailStart) {
        case 15:
            k2 ^= (long) (data[tailStart + 14] & 0xff) << 48;
        case 14:
            k2 ^= (long) (data[tailStart + 13] & 0xff) << 40;
        case 13:
            k2 ^= (long) (data[tailStart + 12] & 0xff) << 32;
        case 12:
            k2 ^= (long) (data[tailStart + 11] & 0xff) << 24;
        case 11:
            k2 ^= (long) (data[tailStart + 10] & 0xff) << 16;
        case 10:
            k2 ^= (long) (data[tailStart + 9] & 0xff) << 8;
        case 9:
            k2 ^= data[tailStart + 8] & 0xff;
            k2 *= C2;
            k2 = Long.rotateLeft(k2, R3);
            k2 *= C1;
            h2 ^= k2;

        case 8:
            k1 ^= (long) (data[tailStart + 7] & 0xff) << 56;
        case 7:
            k1 ^= (long) (data[tailStart + 6] & 0xff) << 48;
        case 6:
            k1 ^= (long) (data[tailStart + 5] & 0xff) << 40;
        case 5:
            k1 ^= (long) (data[tailStart + 4] & 0xff) << 32;
        case 4:
            k1 ^= (long) (data[tailStart + 3] & 0xff) << 24;
        case 3:
            k1 ^= (long) (data[tailStart + 2] & 0xff) << 16;
        case 2:
            k1 ^= (long) (data[tailStart + 1] & 0xff) << 8;
        case 1:
            k1 ^= data[tailStart] & 0xff;
            k1 *= C1;
            k1 = Long.rotateLeft(k1, R1);
            k1 *= C2;
            h1 ^= k1;
        }

        // finalization
        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new No128(h2, h1);
    }

}
