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
package org.miaixz.bus.core.io.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.core.io.buffer.FastByteBuffer;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * 基于快速缓冲FastByteBuffer的OutputStream，随着数据的增长自动扩充缓冲区 可以通过{@link #toByteArray()}和 {@link #toString()}来获取数据
 * {@link #close()}方法无任何效果，当流被关闭后不会抛出IOException 这种设计避免重新分配内存块而是分配新增的缓冲区，缓冲区不会被GC，数据也不会被拷贝到其他缓冲区。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastByteArrayOutputStream extends OutputStream {

    private final FastByteBuffer buffer;

    /**
     * 构造
     */
    public FastByteArrayOutputStream() {
        this(Normal._1024);
    }

    /**
     * 构造
     *
     * @param size 预估大小
     */
    public FastByteArrayOutputStream(final int size) {
        buffer = new FastByteBuffer(size);
    }

    /**
     * 根据输入流的总长度创建一个{@code FastByteArrayOutputStream}对象 如果输入流的长度不确定，且
     *
     * @param in    输入流
     * @param limit 限制大小
     * @return {@code FastByteArrayOutputStream}
     */
    public static FastByteArrayOutputStream of(final InputStream in, final int limit) {
        int length = IoKit.length(in);
        if (length < 0 || length > limit) {
            length = limit;
        }
        if (length < 0) {
            length = Normal._1024;
        }
        return new FastByteArrayOutputStream(length);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        buffer.append(b, off, len);
    }

    @Override
    public void write(final int b) {
        buffer.append((byte) b);
    }

    /**
     * 长度
     *
     * @return 长度
     */
    public int size() {
        return buffer.length();
    }

    /**
     * 此方法无任何效果，当流被关闭后不会抛出IOException
     */
    @Override
    public void close() {
        // nop
    }

    /**
     * 复位
     */
    public void reset() {
        buffer.reset();
    }

    /**
     * 写出
     *
     * @param out 输出流
     * @throws InternalException IO异常
     */
    public void writeTo(final OutputStream out) throws InternalException {
        final int index = buffer.index();
        if (index < 0) {
            // 无数据写出
            return;
        }
        byte[] buf;
        try {
            for (int i = 0; i < index; i++) {
                buf = buffer.array(i);
                out.write(buf);
            }
            out.write(buffer.array(index), 0, buffer.offset());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 转为Byte数组
     *
     * @return Byte数组
     */
    public byte[] toByteArray() {
        return buffer.toArray();
    }

    /**
     * 转为Byte数组
     *
     * @param start 起始位置（包含）
     * @param len   长度
     * @return Byte数组
     */
    public byte[] toByteArray(final int start, final int len) {
        return buffer.toArray(start, len);
    }

    /**
     * 转为Byte数组，如果缓冲区中的数据长度固定，则直接返回原始数组 注意此方法共享数组，不能修改数组内容！
     *
     * @return Byte数组
     */
    public byte[] toByteArrayZeroCopyIfPossible() {
        return buffer.toArrayZeroCopyIfPossible();
    }

    /**
     * 获取指定位置的字节
     * 
     * @param index 位置
     * @return 字节
     */
    public byte get(final int index) {
        return buffer.get(index);
    }

    @Override
    public String toString() {
        return toString(Charset.defaultCharset());
    }

    /**
     * 转为字符串
     *
     * @param charset 编码,null表示默认编码
     * @return 字符串
     */
    public String toString(final java.nio.charset.Charset charset) {
        return new String(toByteArray(), ObjectKit.defaultIfNull(charset, Charset::defaultCharset));
    }

}
