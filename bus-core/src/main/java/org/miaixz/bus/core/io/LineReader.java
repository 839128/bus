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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.miaixz.bus.core.center.iterator.ComputeIterator;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 行读取器，类似于BufferedInputStream，支持多行转义，规则如下：
 * <ul>
 * <li>支持'\n'和'\r\n'两种换行符，不支持'\r'换行符</li>
 * <li>如果想读取转义符，必须定义为'\\'</li>
 * <li>多行转义后的换行符和空格都会被忽略</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineReader extends ReaderWrapper implements Iterable<String> {

    /**
     * 构造
     *
     * @param in      {@link InputStream}
     * @param charset 编码
     */
    public LineReader(final InputStream in, final Charset charset) {
        this(IoKit.toReader(in, charset));
    }

    /**
     * 构造
     *
     * @param reader {@link Reader}
     */
    public LineReader(final Reader reader) {
        super(IoKit.toBuffered(reader));
    }

    /**
     * 读取一行
     *
     * @return 内容
     * @throws IOException IO异常
     */
    public String readLine() throws IOException {
        StringBuilder text = null;
        // 换行符前是否为转义符
        boolean precedingBackslash = false;
        int c;
        while ((c = read()) > 0) {
            if (null == text) {
                // 只有有字符的情况下才初始化行，否则为行结束
                text = StringKit.builder(1024);
            }
            if (Symbol.C_BACKSLASH == c) {
                // 转义符转义，行尾需要使用'\'时，使用转义符转义，即`\\`
                if (!precedingBackslash) {
                    // 转义符，添加标识，但是不加入字符
                    precedingBackslash = true;
                    continue;
                } else {
                    precedingBackslash = false;
                }
            } else {
                if (precedingBackslash) {
                    // 转义模式下，跳过转义符后的所有空白符
                    if (CharKit.isBlankChar(c)) {
                        continue;
                    }
                    // 遇到普通字符，关闭转义
                    precedingBackslash = false;
                } else if (Symbol.C_LF == c) {
                    // 非转义状态下，表示行的结束
                    // 如果换行符是`\r\n`，删除末尾的`\r`
                    final int lastIndex = text.length() - 1;
                    if (lastIndex >= 0 && Symbol.C_CR == text.charAt(lastIndex)) {
                        text.deleteCharAt(lastIndex);
                    }
                    break;
                }
            }

            text.append((char) c);
        }

        return StringKit.toStringOrNull(text);
    }

    @Override
    public Iterator<String> iterator() {
        return new ComputeIterator<>() {
            @Override
            protected String computeNext() {
                try {
                    return readLine();
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            }
        };
    }

}
