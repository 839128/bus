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
package org.miaixz.bus.core.bean.desc;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.miaixz.bus.core.lang.reflect.Invoker;

/**
 * Bean信息描述做为BeanInfo替代方案，此对象持有JavaBean中的setters和getters等相关信息描述 查找Getter和Setter方法时会：
 *
 * <pre>
 * 1. 忽略字段和方法名的大小写
 * 2. Getter查找getXXX、isXXX、getIsXXX
 * 3. Setter查找setXXX、setIsXXX
 * 4. Setter忽略参数值与字段值不匹配的情况，因此有多个参数类型的重载时，会调用首次匹配的
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface BeanDesc extends Serializable {

    /**
     * 获取字段名-字段属性Map
     *
     * @param ignoreCase 是否忽略大小写，true为忽略，false不忽略
     * @return 字段名-字段属性Map
     */
    Map<String, PropDesc> getPropMap(final boolean ignoreCase);

    /**
     * 获取Bean属性数量
     *
     * @return 字段数量
     */
    default int size() {
        return getPropMap(false).size();
    }

    /**
     * 是否为空
     *
     * @return 是否为空
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * 是否有可读字段，即有getter方法或public字段
     *
     * @param checkTransient 是否检查transient字段，true表示检查，false表示不检查
     * @return 是否有可读字段
     */
    default boolean isReadable(final boolean checkTransient) {
        for (final Map.Entry<String, PropDesc> entry : getPropMap(false).entrySet()) {
            if (entry.getValue().isReadable(checkTransient)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否有可写字段，即有setter方法或public字段
     *
     * @param checkTransient 是否检查transient字段，true表示检查，false表示不检查
     * @return 是否有可写字段
     */
    default boolean isWritable(final boolean checkTransient) {
        for (final Map.Entry<String, PropDesc> entry : getPropMap(false).entrySet()) {
            if (entry.getValue().isWritable(checkTransient)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取字段属性列表
     *
     * @return {@link PropDesc} 列表
     */
    default Collection<PropDesc> getProps() {
        return getPropMap(false).values();
    }

    /**
     * 获取属性，如果不存在返回null
     *
     * @param fieldName 字段名
     * @return {@link PropDesc}
     */
    default PropDesc getProp(final String fieldName) {
        return getPropMap(false).get(fieldName);
    }

    /**
     * 获取Getter方法，如果不存在返回null
     *
     * @param fieldName 字段名
     * @return Getter方法
     */
    default Invoker getGetter(final String fieldName) {
        final PropDesc desc = getProp(fieldName);
        return null == desc ? null : desc.getGetter();
    }

    /**
     * 获取Setter方法，如果不存在返回null
     *
     * @param fieldName 字段名
     * @return Setter方法
     */
    default Invoker getSetter(final String fieldName) {
        final PropDesc desc = getProp(fieldName);
        return null == desc ? null : desc.getSetter();
    }

}
