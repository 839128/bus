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
package org.miaixz.bus.core.net.url;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import org.miaixz.bus.core.io.stream.FastByteArrayOutputStream;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * URL解码，数据内容的类型是 application/x-www-form-urlencoded。
 *
 * <pre>
 * 1. 将%20转换为空格 ;
 * 2. 将"%xy"转换为文本形式,xy是两位16进制的数值;
 * 3. 跳过不符合规范的%形式，直接输出
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UrlDecoder implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852231876163L;

    /**
     * 解码，不对+解码
     *
     * <ol>
     * <li>将%20转换为空格</li>
     * <li>将 "%xy"转换为文本形式,xy是两位16进制的数值</li>
     * <li>跳过不符合规范的%形式，直接输出</li>
     * </ol>
     *
     * @param text    包含URL编码后的字符串
     * @param charset 解码的编码，{@code null}表示不做解码
     * @return 解码后的字符串
     */
    public static String decodeForPath(final String text, final java.nio.charset.Charset charset) {
        return decode(text, charset, false);
    }

    /**
     * 解码
     * 规则见：<a href="https://url.spec.whatwg.org/#urlencoded-parsing">https://url.spec.whatwg.org/#urlencoded-parsing</a>
     *
     * <pre>
     *   1. 将+和%20转换为空格(" ");
     *   2. 将"%xy"转换为文本形式,xy是两位16进制的数值;
     *   3. 跳过不符合规范的%形式，直接输出
     * </pre>
     *
     * @param text 包含URL编码后的字符串
     * @return 解码后的字符串
     */
    public static String decode(final String text) {
        return decode(text, Charset.UTF_8);
    }

    /**
     * 解码
     * 规则见：<a href="https://url.spec.whatwg.org/#urlencoded-parsing">https://url.spec.whatwg.org/#urlencoded-parsing</a>
     *
     * <pre>
     *   1. 将+和%20转换为空格(" ");
     *   2. 将"%xy"转换为文本形式,xy是两位16进制的数值;
     *   3. 跳过不符合规范的%形式，直接输出
     * </pre>
     *
     * @param text    包含URL编码后的字符串
     * @param charset 编码
     * @return 解码后的字符串
     */
    public static String decode(final String text, final java.nio.charset.Charset charset) {
        return decode(text, charset, true);
    }

    /**
     * 解码
     *
     * <pre>
     *   1. 将%20转换为空格 ;
     *   2. 将"%xy"转换为文本形式,xy是两位16进制的数值;
     *   3. 跳过不符合规范的%形式，直接输出
     * </pre>
     *
     * @param text          包含URL编码后的字符串
     * @param isPlusToSpace 是否+转换为空格
     * @return 解码后的字符串
     */
    public static String decode(final String text, final boolean isPlusToSpace) {
        return decode(text, Charset.UTF_8, isPlusToSpace);
    }

    /**
     * 解码
     *
     * <pre>
     *   1. 将%20转换为空格 ;
     *   2. 将"%xy"转换为文本形式,xy是两位16进制的数值;
     *   3. 跳过不符合规范的%形式，直接输出
     * </pre>
     *
     * @param text          包含URL编码后的字符串
     * @param isPlusToSpace 是否+转换为空格
     * @param charset       编码，{@code null}表示不做解码
     * @return 解码后的字符串
     */
    public static String decode(final String text, final java.nio.charset.Charset charset,
            final boolean isPlusToSpace) {
        if (null == charset) {
            return text;
        }
        if (null == text) {
            return null;
        }
        final int length = text.length();
        if (0 == length) {
            return Normal.EMPTY;
        }

        final StringBuilder result = new StringBuilder(length / 3);

        int begin = 0;
        char c;
        for (int i = 0; i < length; i++) {
            c = text.charAt(i);
            if (Symbol.C_PERCENT == c || CharKit.isHexChar(c)) {
                continue;
            }

            // 遇到非需要处理的字符跳过
            // 处理之前的hex字符
            if (i > begin) {
                result.append(decodeSub(text, begin, i, charset, isPlusToSpace));
            }

            // 非Hex字符，忽略本字符
            if (Symbol.C_PLUS == c && isPlusToSpace) {
                c = Symbol.C_SPACE;
            }

            result.append(c);
            begin = i + 1;
        }

        // 处理剩余字符
        if (begin < length) {
            result.append(decodeSub(text, begin, length, charset, isPlusToSpace));
        }

        return result.toString();
    }

    /**
     * 解码
     *
     * <pre>
     *   1. 将+和%20转换为空格 ;
     *   2. 将"%xy"转换为文本形式,xy是两位16进制的数值;
     *   3. 跳过不符合规范的%形式，直接输出
     * </pre>
     *
     * @param bytes url编码的bytes
     * @return 解码后的bytes
     */
    public static byte[] decode(final byte[] bytes) {
        return decode(bytes, true);
    }

    /**
     * 解码
     *
     * <pre>
     *   1. 将%20转换为空格 ;
     *   2. 将"%xy"转换为文本形式,xy是两位16进制的数值;
     *   3. 跳过不符合规范的%形式，直接输出
     * </pre>
     *
     * @param bytes         url编码的bytes
     * @param isPlusToSpace 是否+转换为空格
     * @return 解码后的bytes
     */
    public static byte[] decode(final byte[] bytes, final boolean isPlusToSpace) {
        if (bytes == null) {
            return null;
        }
        final FastByteArrayOutputStream buffer = new FastByteArrayOutputStream(bytes.length / 3);
        int b;
        for (int i = 0; i < bytes.length; i++) {
            b = bytes[i];
            if (b == Symbol.C_PLUS) {
                buffer.write(isPlusToSpace ? Symbol.C_SPACE : b);
            } else if (b == Symbol.C_PERCENT) {
                if (i + 1 < bytes.length) {
                    final int u = CharKit.digit16(bytes[i + 1]);
                    if (u >= 0 && i + 2 < bytes.length) {
                        final int l = CharKit.digit16(bytes[i + 2]);
                        if (l >= 0) {
                            buffer.write((char) ((u << 4) + l));
                            i += 2;
                            continue;
                        }
                    }
                }
                // 跳过不符合规范的%形式
                buffer.write(b);
            } else {
                buffer.write(b);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * 解码子串
     *
     * @param text          字符串
     * @param begin         开始位置（包含）
     * @param end           结束位置（不包含）
     * @param charset       编码
     * @param isPlusToSpace 是否+转换为空格
     * @return 解码后的字符串
     */
    private static String decodeSub(final String text, final int begin, final int end,
            final java.nio.charset.Charset charset, final boolean isPlusToSpace) {
        return new String(decode(
                // 截取需要decode的部分
                text.substring(begin, end).getBytes(Charset.ISO_8859_1), isPlusToSpace), charset);
    }

    /**
     * 将URL参数解析为Map(也可以解析Post中的键值对参数)
     *
     * @param params  参数字符串(或者带参数的Path)
     * @param charset 字符集
     * @return 参数Map
     */
    public static Map<String, String> decodeMap(String params, String charset) {
        final Map<String, List<String>> paramsMap = decodeObject(params, charset);
        final Map<String, String> result = MapKit.newHashMap(paramsMap.size());
        List<String> list;
        for (Map.Entry<String, List<String>> entry : paramsMap.entrySet()) {
            list = entry.getValue();
            result.put(entry.getKey(), CollKit.isEmpty(list) ? null : list.get(0));
        }
        return result;
    }

    /**
     * 将URL参数解析为Map(也可以解析Post中的键值对参数)
     *
     * @param params  参数字符串(或者带参数的Path)
     * @param charset 字符集
     * @return 参数Map
     */
    public static Map<String, List<String>> decodeObject(String params, String charset) {
        if (StringKit.isBlank(params)) {
            return Collections.emptyMap();
        }

        // 去掉Path部分
        int pathEndPos = params.indexOf(Symbol.C_QUESTION_MARK);
        if (pathEndPos > -1) {
            params = StringKit.subSuf(params, pathEndPos + 1);
        }

        final Map<String, List<String>> map = new LinkedHashMap<>();
        final int len = params.length();
        String name = null;
        int pos = 0; // 未处理字符开始位置
        int i; // 未处理字符结束位置
        char c; // 当前字符
        for (i = 0; i < len; i++) {
            c = params.charAt(i);
            if (c == Symbol.C_EQUAL) { // 键值对的分界点
                if (null == name) {
                    // name可以是""
                    name = params.substring(pos, i);
                }
                pos = i + 1;
            } else if (c == Symbol.C_AND) { // 参数对的分界点
                if (null == name && pos != i) {
                    // 对于像&a&这类无参数值的字符串,我们将name为a的值设为""
                    addParam(map, params.substring(pos, i), Normal.EMPTY, charset);
                } else if (null != name) {
                    addParam(map, name, params.substring(pos, i), charset);
                    name = null;
                }
                pos = i + 1;
            }
        }

        // 处理结尾
        if (pos != i) {
            if (null == name) {
                addParam(map, params.substring(pos, i), Normal.EMPTY, charset);
            } else {
                addParam(map, name, params.substring(pos, i), charset);
            }
        } else if (null != name) {
            addParam(map, name, Normal.EMPTY, charset);
        }

        return map;
    }

    /**
     * 将键值对加入到值为List类型的Map中
     *
     * @param params  参数
     * @param name    key
     * @param value   value
     * @param charset 编码
     */
    private static void addParam(Map<String, List<String>> params, String name, String value, String charset) {
        name = decode(name, Charset.parse(charset));
        value = decode(value, Charset.parse(charset));
        List<String> values = params.get(name);
        if (null == values) {
            values = new ArrayList<>(1);
            params.put(name, values);
        }
        values.add(value);
    }

}
