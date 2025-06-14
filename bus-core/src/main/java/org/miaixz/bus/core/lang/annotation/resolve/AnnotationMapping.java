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
package org.miaixz.bus.core.lang.annotation.resolve;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.annotation.Alias;

/**
 * 用于增强注解的包装器
 *
 * @param <T> 注解类型
 * @author Kimi Liu
 * @see AnnotationMappingProxy
 * @since Java 17+
 */
public interface AnnotationMapping<T extends Annotation> extends Annotation {

    /**
     * 当前注解是否为根注解
     *
     * @return 是否
     */
    boolean isRoot();

    /**
     * 获取注解对象
     *
     * @return 注解对象
     */
    T getAnnotation();

    /**
     * 根据当前映射对象，通过动态代理生成一个类型与被包装注解对象一致地合成注解，该注解相对原生注解：
     * <ul>
     * <li>支持同注解内通过{@link Alias}构建的别名机制；</li>
     * <li>支持子注解对元注解的同名同类型属性覆盖机制；</li>
     * </ul>
     * 当{@link #isResolved()}为{@code false}时，则该方法应当被包装的原始注解对象， 即返回值应当与{@link #getAnnotation()}相同。
     *
     * @return 所需的注解，若{@link #isResolved()}为{@code false}则返回的是原始的注解对象
     */
    T getResolvedAnnotation();

    /**
     * 获取注解类型
     *
     * @return 注解类型
     */
    @Override
    default Class<? extends Annotation> annotationType() {
        return getAnnotation().annotationType();
    }

    /**
     * 当前注解是否存在被解析的属性，当该值为{@code false}时， 通过{@code getResolvedAttributeValue}获得的值皆为注解的原始属性值，
     * 通过{@link #getResolvedAnnotation()}获得注解对象为原始的注解对象。
     *
     * @return 是否
     */
    boolean isResolved();

    /**
     * 获取注解原始属性
     *
     * @return 注解属性
     */
    Method[] getAttributes();

    /**
     * 获取属性值
     *
     * @param attributeName 属性名称
     * @param attributeType 属性类型
     * @param <R>           返回值类型
     * @return 属性值
     */
    <R> R getAttributeValue(final String attributeName, final Class<R> attributeType);

    /**
     * 获取解析后的属性值
     *
     * @param attributeName 属性名称
     * @param attributeType 属性类型
     * @param <R>           返回值类型
     * @return 属性值
     */
    <R> R getResolvedAttributeValue(final String attributeName, final Class<R> attributeType);

}
