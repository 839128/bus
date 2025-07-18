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
package org.miaixz.bus.logger.metric.slf4j;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.miaixz.bus.logger.Provider;
import org.miaixz.bus.logger.magic.AbstractFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

/**
 * slf4j and logback
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Slf4jLoggingFactory extends AbstractFactory {

    /**
     * 构造
     */
    public Slf4jLoggingFactory() {
        this(true);
    }

    /**
     * 构造
     *
     * @param fail 如果未找到桥接包是否报错
     */
    public Slf4jLoggingFactory(final boolean fail) {
        super("Slf4j");
        exists(LoggerFactory.class);
        if (!fail) {
            return;
        }
        final StringBuilder buf = new StringBuilder();
        final PrintStream err = System.err;
        try {
            System.setErr(new PrintStream(new OutputStream() {
                @Override
                public void write(final int b) {
                    buf.append((char) b);
                }
            }, true, "US-ASCII"));
        } catch (final UnsupportedEncodingException e) {
            throw new Error(e);
        }

        try {
            if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
                throw new NoClassDefFoundError(buf.toString());
            } else {
                err.print(buf);
                err.flush();
            }
        } finally {
            System.setErr(err);
        }
    }

    @Override
    public Provider create(final String name) {
        return new Slf4jLoggingProvider(name);
    }

    @Override
    public Provider create(final Class<?> clazz) {
        return new Slf4jLoggingProvider(clazz);
    }

}
