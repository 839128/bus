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
package org.miaixz.bus.core.io.buffer;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * 
 * 快速缓冲，将数据存放在缓冲集中，取代以往的单一数组
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastByteBuffer extends FastBuffer {

    /**
     * 缓冲集
     */
    private byte[][] buffers = new byte[16][];
    /**
     * 当前缓冲
     */
    private byte[] currentBuffer;

    /**
     * 构造
     */
    public FastByteBuffer() {
        this(Normal._8192);
    }

    /**
     * 构造
     *
     * @param size 一个缓冲区的最小字节数
     */
    public FastByteBuffer(final int size) {
        super(size);
    }

    /**
     * 向快速缓冲加入数据
     *
     * @param array 数据
     * @param off   偏移量
     * @param len   字节数
     * @return 快速缓冲自身 @see FastByteBuffer
     */
    public FastByteBuffer append(final byte[] array, final int off, final int len) {
        final int end = off + len;
        if ((off < 0) || (len < 0) || (end > array.length)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return this;
        }
        final int newSize = size + len;
        int remaining = len;

        if (currentBuffer != null) {
            // first try to fill current buffer
            final int part = Math.min(remaining, currentBuffer.length - offset);
            System.arraycopy(array, end - remaining, currentBuffer, offset, part);
            remaining -= part;
            offset += part;
            size += part;
        }

        if (remaining > 0) {
            // still some data left
            // ask for new buffer
            ensureCapacity(newSize);

            // then copy remaining
            // but this time we are sure that it will fit
            final int part = Math.min(remaining, currentBuffer.length - offset);
            System.arraycopy(array, end - remaining, currentBuffer, offset, part);
            offset += part;
            size += part;
        }

        return this;
    }

    /**
     * 向快速缓冲加入数据
     *
     * @param array 数据
     * @return 快速缓冲自身 @see FastByteBuffer
     */
    public FastByteBuffer append(final byte[] array) {
        return append(array, 0, array.length);
    }

    /**
     * 向快速缓冲加入一个字节
     *
     * @param element 一个字节的数据
     * @return 快速缓冲自身 @see FastByteBuffer
     */
    public FastByteBuffer append(final byte element) {
        if ((currentBuffer == null) || (offset == currentBuffer.length)) {
            ensureCapacity(size + 1);
        }

        currentBuffer[offset] = element;
        offset++;
        this.size++;

        return this;
    }

    /**
     * 将另一个快速缓冲加入到自身
     *
     * @param buff 快速缓冲
     * @return 快速缓冲自身 @see FastByteBuffer
     */
    public FastByteBuffer append(final FastByteBuffer buff) {
        if (buff.size == 0) {
            return this;
        }
        for (int i = 0; i < buff.currentBufferIndex; i++) {
            append(buff.buffers[i]);
        }
        append(buff.currentBuffer, 0, buff.offset);
        return this;
    }

    /**
     * 根据索引位返回缓冲集中的缓冲
     *
     * @param index 索引位
     * @return 缓冲
     */
    public byte[] array(final int index) {
        return buffers[index];
    }

    @Override
    public void reset() {
        super.reset();
        currentBuffer = null;
    }

    @Override
    public int length() {
        return this.size;
    }

    /**
     * 返回快速缓冲中的数据，如果缓冲区中的数据长度固定，则直接返回原始数组<br>
     * 注意此方法共享数组，不能修改数组内容！
     *
     * @return 快速缓冲中的数据
     */
    public byte[] toArrayZeroCopyIfPossible() {
        if (1 == currentBufferIndex) {
            final int len = buffers[0].length;
            if (len == size) {
                return buffers[0];
            }
        }

        return toArray();
    }

    /**
     * 返回快速缓冲中的数据
     *
     * @return 快速缓冲中的数据
     */
    public byte[] toArray() {
        return toArray(0, this.size);
    }

    /**
     * 返回快速缓冲中的数据
     *
     * @param start 逻辑起始位置
     * @param len   逻辑字节长
     * @return 快速缓冲中的数据
     */
    public byte[] toArray(int start, int len) {
        Assert.isTrue(start >= 0, "Start must be greater than zero!");
        Assert.isTrue(len >= 0, "Length must be greater than zero!");

        if (start >= this.size || len == 0) {
            return new byte[0];
        }
        if (len > (this.size - start)) {
            len = this.size - start;
        }
        int remaining = len;
        int pos = 0;
        final byte[] result = new byte[len];

        int i = 0;
        while (start >= buffers[i].length) {
            start -= buffers[i].length;
            i++;
        }

        while (i < buffersCount) {
            final byte[] buf = buffers[i];
            final int bufLen = Math.min(buf.length - start, remaining);
            System.arraycopy(buf, start, result, pos, bufLen);
            pos += bufLen;
            remaining -= bufLen;
            if (remaining == 0) {
                break;
            }
            start = 0;
            i++;
        }
        return result;
    }

    /**
     * 根据索引位返回一个字节
     *
     * @param index 索引位
     * @return 一个字节
     */
    public byte get(int index) {
        if ((index >= this.size) || (index < 0)) {
            throw new IndexOutOfBoundsException();
        }
        int ndx = 0;
        while (true) {
            final byte[] b = buffers[ndx];
            if (index < b.length) {
                return b[index];
            }
            ndx++;
            index -= b.length;
        }
    }

    @Override
    protected void ensureCapacity(final int capacity) {
        final int delta = capacity - this.size;
        final int newBufferSize = Math.max(minChunkLen, delta);

        currentBufferIndex++;
        currentBuffer = new byte[newBufferSize];
        offset = 0;

        // add buffer
        if (currentBufferIndex >= buffers.length) {
            final int newLen = buffers.length << 1;
            final byte[][] newBuffers = new byte[newLen][];
            System.arraycopy(buffers, 0, newBuffers, 0, buffers.length);
            buffers = newBuffers;
        }
        buffers[currentBufferIndex] = currentBuffer;
        buffersCount++;
    }

}
