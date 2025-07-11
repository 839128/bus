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
package org.miaixz.bus.core.codec.hash.metro;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.miaixz.bus.core.codec.hash.Hash64;

/**
 * Apache 发布的MetroHash算法的64位实现，是一组用于非加密用例的最先进的哈希函数。 除了卓越的性能外，他们还以算法生成而著称。
 *
 * <p>
 * 官方实现：https://github.com/jandrewrogers/MetroHash 官方文档：http://www.jandrewrogers.com/2015/05/27/metrohash/
 * 来自：https://github.com/postamar/java-metrohash/
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MetroHash64 extends AbstractMetroHash<MetroHash64> implements Hash64<byte[]> {

    private static final long K0 = 0xD6D018F5L;
    private static final long K1 = 0xA2AA033BL;
    private static final long K2 = 0x62992FC1L;
    private static final long K3 = 0x30BC5B29L;
    private long hash;

    /**
     * 使用指定种子构造
     *
     * @param seed 种子
     */
    public MetroHash64(final long seed) {
        super(seed);
    }

    /**
     * 创建 {@code MetroHash64}对象
     *
     * @param seed 种子
     * @return {@code MetroHash64}对象
     */
    public static MetroHash64 of(final long seed) {
        return new MetroHash64(seed);
    }

    @Override
    public MetroHash64 reset() {
        v0 = v1 = v2 = v3 = hash = (seed + K2) * K0;
        nChunks = 0;
        return this;
    }

    /**
     * 获取计算结果hash值
     *
     * @return hash值
     */
    public long get() {
        return hash;
    }

    @Override
    public long hash64(final byte[] bytes) {
        return apply(ByteBuffer.wrap(bytes)).get();
    }

    @Override
    public MetroHash64 write(final ByteBuffer output, final ByteOrder byteOrder) {
        if (ByteOrder.LITTLE_ENDIAN == byteOrder) {
            writeLittleEndian(hash, output);
        } else {
            output.asLongBuffer().put(hash);
        }
        return this;
    }

    @Override
    MetroHash64 partialApply32ByteChunk(final ByteBuffer partialInput) {
        assert partialInput.remaining() >= 32;
        v0 += grab(partialInput, 8) * K0;
        v0 = Long.rotateRight(v0, 29) + v2;
        v1 += grab(partialInput, 8) * K1;
        v1 = Long.rotateRight(v1, 29) + v3;
        v2 += grab(partialInput, 8) * K2;
        v2 = Long.rotateRight(v2, 29) + v0;
        v3 += grab(partialInput, 8) * K3;
        v3 = Long.rotateRight(v3, 29) + v1;
        ++nChunks;
        return this;
    }

    @Override
    MetroHash64 partialApplyRemaining(final ByteBuffer partialInput) {
        assert partialInput.remaining() < 32;
        if (nChunks > 0) {
            metroHash64_32();
        }
        if (partialInput.remaining() >= 16) {
            metroHash64_16(partialInput);
        }
        if (partialInput.remaining() >= 8) {
            metroHash64_8(partialInput);
        }
        if (partialInput.remaining() >= 4) {
            metroHash64_4(partialInput);
        }
        if (partialInput.remaining() >= 2) {
            metroHash64_2(partialInput);
        }
        if (partialInput.remaining() >= 1) {
            metroHash64_1(partialInput);
        }
        hash ^= Long.rotateRight(hash, 28);
        hash *= K0;
        hash ^= Long.rotateRight(hash, 29);
        return this;
    }

    private void metroHash64_32() {
        v2 ^= Long.rotateRight(((v0 + v3) * K0) + v1, 37) * K1;
        v3 ^= Long.rotateRight(((v1 + v2) * K1) + v0, 37) * K0;
        v0 ^= Long.rotateRight(((v0 + v2) * K0) + v3, 37) * K1;
        v1 ^= Long.rotateRight(((v1 + v3) * K1) + v2, 37) * K0;
        hash += v0 ^ v1;
    }

    private void metroHash64_16(final ByteBuffer bb) {
        v0 = hash + grab(bb, 8) * K2;
        v0 = Long.rotateRight(v0, 29) * K3;
        v1 = hash + grab(bb, 8) * K2;
        v1 = Long.rotateRight(v1, 29) * K3;
        v0 ^= Long.rotateRight(v0 * K0, 21) + v1;
        v1 ^= Long.rotateRight(v1 * K3, 21) + v0;
        hash += v1;
    }

    private void metroHash64_8(final ByteBuffer bb) {
        hash += grab(bb, 8) * K3;
        hash ^= Long.rotateRight(hash, 55) * K1;
    }

    private void metroHash64_4(final ByteBuffer bb) {
        hash += grab(bb, 4) * K3;
        hash ^= Long.rotateRight(hash, 26) * K1;
    }

    private void metroHash64_2(final ByteBuffer bb) {
        hash += grab(bb, 2) * K3;
        hash ^= Long.rotateRight(hash, 48) * K1;
    }

    private void metroHash64_1(final ByteBuffer bb) {
        hash += grab(bb, 1) * K3;
        hash ^= Long.rotateRight(hash, 37) * K1;
    }

}
