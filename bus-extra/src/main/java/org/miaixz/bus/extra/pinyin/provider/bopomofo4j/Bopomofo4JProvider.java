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
package org.miaixz.bus.extra.pinyin.provider.bopomofo4j;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.extra.pinyin.PinyinProvider;

import com.rnkrsoft.bopomofo4j.Bopomofo4j;
import com.rnkrsoft.bopomofo4j.ToneType;

/**
 * 封装了Bopomofo4j的引擎。
 *
 * <p>
 * Bopomofo4j封装，项目：https://gitee.com/rnkrsoft/Bopomofo4j。
 * </p>
 *
 * <p>
 * 引入：
 * 
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;com.rnkrsoft.bopomofo4j&lt;/groupId&gt;
 *     &lt;artifactId&gt;bopomofo4j&lt;/artifactId&gt;
 *     &lt;version&gt;1.0.0&lt;/version&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Bopomofo4JProvider implements PinyinProvider {

    /**
     * 构造
     */
    public Bopomofo4JProvider() {
        Bopomofo4j.local();
    }

    @Override
    public String getPinyin(final char c, final boolean tone) {
        return Bopomofo4j.pinyin(String.valueOf(c), tone ? ToneType.WITH_VOWEL_TONE : ToneType.WITHOUT_TONE, false,
                false, Normal.EMPTY);
    }

    @Override
    public String getPinyin(final String str, final String separator, final boolean tone) {
        return Bopomofo4j.pinyin(str, tone ? ToneType.WITH_VOWEL_TONE : ToneType.WITHOUT_TONE, false, false, separator);
    }

}
