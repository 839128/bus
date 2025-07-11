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
package org.miaixz.bus.core.convert;

import java.io.Serial;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.xyz.TypeKit;

/**
 * {@link AtomicReference}转换器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AtomicReferenceConverter extends AbstractConverter {

    @Serial
    private static final long serialVersionUID = 2852265266938L;

    private final Converter converter;

    /**
     * 构造
     *
     * @param converter 用于转换AtomicReference包装的对象类型
     */
    public AtomicReferenceConverter(final Converter converter) {
        this.converter = converter;
    }

    @Override
    protected AtomicReference<?> convertInternal(final Class<?> targetClass, final Object value) {
        // 尝试将值转换为Reference泛型的类型
        Object targetValue = null;
        final Type paramType = TypeKit.getTypeArgument(AtomicReference.class);
        if (!TypeKit.isUnknown(paramType)) {
            targetValue = converter.convert(paramType, value);
        }
        if (null == targetValue) {
            targetValue = value;
        }

        return new AtomicReference<>(targetValue);
    }

}
