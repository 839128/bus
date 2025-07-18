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
package org.miaixz.bus.core.lang.getter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.miaixz.bus.core.convert.Convert;

/**
 * 基于分组的Get接口
 *
 * @param <K> 键类型
 * @param <G> 分组键类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface GroupedTypeGetter<K, G> {

    /**
     * 获取Object属性值，最原始的对象获取，没有任何转换或类型判断
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    Object getObjByGroup(K key, G group, Object defaultValue);

    /**
     * 获取Object属性值，最原始的对象获取，没有任何转换或类型判断
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default Object getObjByGroup(final K key, final G group) {
        return getObjByGroup(key, group, null);
    }

    /**
     * 获取指定类型的值，默认自动转换值类型
     *
     * @param <T>   目标类型
     * @param key   键
     * @param group 分组
     * @param type  目标类型
     * @return 结果值
     */
    default <T> T getByGroup(final K key, final G group, final Type type) {
        return getByGroup(key, group, type, null);
    }

    /**
     * 获取指定类型的值，默认自动转换值类型
     *
     * @param <T>          目标类型
     * @param key          键
     * @param group        分组
     * @param type         目标类型
     * @param defaultValue 默认值
     * @return 结果值
     */
    default <T> T getByGroup(final K key, final G group, final Type type, final T defaultValue) {
        return Convert.convert(type, getObjByGroup(key, group), defaultValue);
    }

    /**
     * 获取字符串型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default String getStrByGroup(final K key, final G group, final String defaultValue) {
        return getByGroup(key, group, String.class, defaultValue);
    }

    /**
     * 获取字符串型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default String getStrByGroup(final K key, final G group) {
        return getStrByGroup(key, group, null);
    }

    /**
     * 获取int型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default Integer getIntByGroup(final K key, final G group, final Integer defaultValue) {
        return getByGroup(key, group, Integer.class, defaultValue);
    }

    /**
     * 获取int型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default Integer getIntByGroup(final K key, final G group) {
        return getIntByGroup(key, group, null);
    }

    /**
     * 获取short型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default Short getShortByGroup(final K key, final G group, final Short defaultValue) {
        return getByGroup(key, group, Short.class, defaultValue);
    }

    /**
     * 获取short型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default Short getShortByGroup(final K key, final G group) {
        return getShortByGroup(key, group, null);
    }

    /**
     * 获取boolean型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default Boolean getBoolByGroup(final K key, final G group, final Boolean defaultValue) {
        return getByGroup(key, group, Boolean.class, defaultValue);
    }

    /**
     * 获取boolean型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default Boolean getBoolByGroup(final K key, final G group) {
        return getBoolByGroup(key, group, null);
    }

    /**
     * 获取Long型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default Long getLongByGroup(final K key, final G group, final Long defaultValue) {
        return getByGroup(key, group, Long.class, defaultValue);
    }

    /**
     * 获取Long型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default Long getLongByGroup(final K key, final G group) {
        return getLongByGroup(key, group, null);
    }

    /**
     * 获取char型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default Character getCharByGroup(final K key, final G group, final Character defaultValue) {
        return getByGroup(key, group, Character.class, defaultValue);
    }

    /**
     * 获取char型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default Character getCharByGroup(final K key, final G group) {
        return getCharByGroup(key, group, null);
    }

    /**
     * 获取double型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default Double getDoubleByGroup(final K key, final G group, final Double defaultValue) {
        return getByGroup(key, group, Double.class, defaultValue);
    }

    /**
     * 获取double型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default Double getDoubleByGroup(final K key, final G group) {
        return getDoubleByGroup(key, group, null);
    }

    /**
     * 获取byte型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default Byte getByteByGroup(final K key, final G group, final Byte defaultValue) {
        return getByGroup(key, group, Byte.class, defaultValue);
    }

    /**
     * 获取byte型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default Byte getByteByGroup(final K key, final G group) {
        return getByteByGroup(key, group, null);
    }

    /**
     * 获取BigDecimal型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default BigDecimal getBigDecimalByGroup(final K key, final G group, final BigDecimal defaultValue) {
        return getByGroup(key, group, BigDecimal.class, defaultValue);
    }

    /**
     * 获取BigDecimal型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default BigDecimal getBigDecimalByGroup(final K key, final G group) {
        return getBigDecimalByGroup(key, group, null);
    }

    /**
     * 获取BigInteger型属性值
     *
     * @param key          属性名
     * @param group        分组
     * @param defaultValue 默认值
     * @return 属性值
     */
    default BigInteger getBigIntegerByGroup(final K key, final G group, final BigInteger defaultValue) {
        return getByGroup(key, group, BigInteger.class, defaultValue);
    }

    /**
     * 获取BigInteger型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    default BigInteger getBigIntegerByGroup(final K key, final G group) {
        return getBigIntegerByGroup(key, group, null);
    }

}
