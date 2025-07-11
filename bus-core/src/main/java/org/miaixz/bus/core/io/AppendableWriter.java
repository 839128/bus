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
package org.miaixz.bus.core.io;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 * 同时继承{@link Writer}和实现{@link Appendable}的聚合类，用于适配两种接口操作
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AppendableWriter extends Writer implements Appendable {

    private final Appendable appendable;
    private final boolean flushable;
    private boolean closed;

    /**
     * 构造
     *
     * @param appendable {@link Appendable}
     */
    public AppendableWriter(final Appendable appendable) {
        this.appendable = appendable;
        this.flushable = appendable instanceof Flushable;
        this.closed = false;
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len) throws IOException {
        checkNotClosed();
        appendable.append(CharBuffer.wrap(cbuf), off, off + len);
    }

    @Override
    public void write(final int c) throws IOException {
        checkNotClosed();
        appendable.append((char) c);
    }

    @Override
    public Writer append(final char c) throws IOException {
        checkNotClosed();
        appendable.append(c);
        return this;
    }

    @Override
    public Writer append(final CharSequence csq, final int start, final int end) throws IOException {
        checkNotClosed();
        appendable.append(csq, start, end);
        return this;
    }

    @Override
    public Writer append(final CharSequence csq) throws IOException {
        checkNotClosed();
        appendable.append(csq);
        return this;
    }

    @Override
    public void write(final String text, final int off, final int len) throws IOException {
        checkNotClosed();
        appendable.append(text, off, off + len);
    }

    @Override
    public void write(final String text) throws IOException {
        appendable.append(text);
    }

    @Override
    public void write(final char[] c) throws IOException {
        appendable.append(CharBuffer.wrap(c));
    }

    @Override
    public void flush() throws IOException {
        checkNotClosed();
        if (flushable) {
            ((Flushable) appendable).flush();
        }
    }

    /**
     * 检查Writer是否已经被关闭
     *
     * @throws IOException IO异常
     */
    private void checkNotClosed() throws IOException {
        if (closed) {
            throw new IOException("Writer is closed!" + this);
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            flush();
            if (appendable instanceof Closeable) {
                ((Closeable) appendable).close();
            }
            closed = true;
        }
    }

}
