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
package org.miaixz.bus.core.xyz;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Assert;

/**
 * 枚举工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EnumKit {

    /**
     * 指定类是否为Enum类
     *
     * @param type 类
     * @return 是否为Enum类
     */
    public static boolean isEnum(final Type type) {
        return Assert.notNull(TypeKit.getClass(type)).isEnum();
    }

    /**
     * 指定类是否为Enum类
     *
     * @param object 类
     * @return 是否为Enum类
     */
    public static boolean isEnum(final Object object) {
        return Assert.notNull(object).getClass().isEnum();
    }

    /**
     * Enum对象转String，调用{@link Enum#name()} 方法
     *
     * @param e Enum
     * @return name值
     */
    public static String toString(final Enum<?> e) {
        return null != e ? e.name() : null;
    }

    /**
     * 获取给定位置的枚举值
     *
     * @param <E>       枚举类型泛型
     * @param enumClass 枚举类
     * @param index     枚举索引
     * @return 枚举值，null表示无此对应枚举
     */
    public static <E extends Enum<E>> E getEnumAt(final Class<E> enumClass, int index) {
        if (null == enumClass) {
            return null;
        }
        final E[] enumConstants = enumClass.getEnumConstants();
        if (index < 0) {
            index = enumConstants.length + index;
        }
        return index >= 0 && index < enumConstants.length ? enumConstants[index] : null;
    }

    /**
     * 字符串转枚举，调用{@link Enum#valueOf(Class, String)}
     *
     * @param <E>       枚举类型泛型
     * @param enumClass 枚举类
     * @param value     值
     * @return 枚举值
     */
    public static <E extends Enum<E>> E fromString(final Class<E> enumClass, final String value) {
        if (null == enumClass || StringKit.isBlank(value)) {
            return null;
        }
        return Enum.valueOf(enumClass, value);
    }

    /**
     * 字符串转枚举，调用{@link Enum#valueOf(Class, String)} 如果无枚举值，返回默认值
     *
     * @param <E>          枚举类型泛型
     * @param enumClass    枚举类
     * @param value        值
     * @param defaultValue 无对应枚举值返回的默认值
     * @return 枚举值
     */
    public static <E extends Enum<E>> E fromString(final Class<E> enumClass, final String value, final E defaultValue) {
        return ObjectKit.defaultIfNull(fromStringQuietly(enumClass, value), defaultValue);
    }

    /**
     * 字符串转枚举，调用{@link Enum#valueOf(Class, String)}，转换失败返回{@code null} 而非报错
     *
     * @param <E>       枚举类型泛型
     * @param enumClass 枚举类
     * @param value     值
     * @return 枚举值
     */
    public static <E extends Enum<E>> E fromStringQuietly(final Class<E> enumClass, final String value) {
        if (null == enumClass || StringKit.isBlank(value)) {
            return null;
        }

        try {
            return fromString(enumClass, value);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 模糊匹配转换为枚举，给定一个值，匹配枚举中定义的所有字段名（包括name属性），一旦匹配到返回这个枚举对象，否则返回null
     *
     * @param <E>       枚举类型
     * @param enumClass 枚举类
     * @param value     值
     * @return 匹配到的枚举对象，未匹配到返回null
     */
    public static <E extends Enum<E>> E likeValueOf(final Class<E> enumClass, Object value) {
        if (null == enumClass || null == value) {
            return null;
        }
        if (value instanceof CharSequence) {
            value = value.toString().trim();
        }

        final Field[] fields = FieldKit.getFields(enumClass);
        final Enum<?>[] enums = enumClass.getEnumConstants();
        String fieldName;
        for (final Field field : fields) {
            fieldName = field.getName();
            if (field.getType().isEnum() || StringKit.equalsAny("ENUM$VALUES", "ordinal", fieldName)) {
                // 跳过一些特殊字段
                continue;
            }
            for (final Enum<?> enumObj : enums) {
                if (ObjectKit.equals(value, FieldKit.getFieldValue(enumObj, field))) {
                    return (E) enumObj;
                }
            }
        }
        return null;
    }

    /**
     * 枚举类中所有枚举对象的name列表
     *
     * @param clazz 枚举类
     * @return name列表
     */
    public static List<String> getNames(final Class<? extends Enum<?>> clazz) {
        if (null == clazz) {
            return null;
        }
        final Enum<?>[] enums = clazz.getEnumConstants();
        if (null == enums) {
            return null;
        }
        final List<String> list = new ArrayList<>(enums.length);
        for (final Enum<?> e : enums) {
            list.add(e.name());
        }
        return list;
    }

    /**
     * 获得枚举类中各枚举对象下指定字段的值
     *
     * @param clazz     枚举类
     * @param fieldName 字段名，最终调用getXXX方法
     * @return 字段值列表
     */
    public static List<Object> getFieldValues(final Class<? extends Enum<?>> clazz, final String fieldName) {
        if (null == clazz || StringKit.isBlank(fieldName)) {
            return null;
        }
        final Enum<?>[] enums = clazz.getEnumConstants();
        if (null == enums) {
            return null;
        }
        final List<Object> list = new ArrayList<>(enums.length);
        for (final Enum<?> e : enums) {
            list.add(FieldKit.getFieldValue(e, fieldName));
        }
        return list;
    }

    /**
     * 获得枚举类中所有的字段名 除用户自定义的字段名，也包括“name”字段，例如：
     *
     * <pre>
     *   EnumKit.getFieldNames(Ansi4BitColor.class) == ["name", "index"]
     * </pre>
     *
     * @param clazz 枚举类
     * @return 字段名列表
     */
    public static List<String> getFieldNames(final Class<? extends Enum<?>> clazz) {
        if (null == clazz) {
            return null;
        }
        final List<String> names = new ArrayList<>();
        final Field[] fields = FieldKit.getFields(clazz);
        String name;
        for (final Field field : fields) {
            name = field.getName();
            if (field.getType().isEnum() || name.contains("$VALUES") || "ordinal".equals(name)) {
                continue;
            }
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }

    /**
     * 通过 某字段对应值 获取 枚举，获取不到时为 {@code null}
     *
     * @param condition 条件字段
     * @param value     条件字段值
     * @param <E>       枚举类型
     * @param <C>       字段类型
     * @return 对应枚举 ，获取不到时为 {@code null}
     */
    public static <E extends Enum<E>, C> E getBy(final FunctionX<E, C> condition, final C value) {
        return getBy(condition, value, null);
    }

    /**
     * 通过 某字段对应值 获取 枚举，获取不到时为 {@code defaultEnum}
     *
     * @param condition   条件字段
     * @param value       条件字段值
     * @param defaultEnum 条件找不到则返回结果使用这个
     * @param <C>         值类型
     * @param <E>         枚举类型
     * @return 对应枚举 ，获取不到时为 {@code null}
     */
    public static <E extends Enum<E>, C> E getBy(final FunctionX<E, C> condition, final C value, final E defaultEnum) {
        if (null == condition) {
            return null;
        }
        final Class<E> implClass = LambdaKit.getRealClass(condition);
        return getBy(implClass, condition, value, defaultEnum);
    }

    /**
     * 通过 某字段对应值 获取 枚举，获取不到时为 {@code defaultEnum}
     *
     * @param enumClass   枚举类
     * @param condition   条件字段
     * @param value       条件字段值
     * @param defaultEnum 条件找不到则返回结果使用这个
     * @param <C>         值类型
     * @param <E>         枚举类型
     * @return 对应枚举 ，获取不到时为 {@code null}
     */
    public static <E extends Enum<E>, C> E getBy(final Class<E> enumClass, final FunctionX<E, C> condition,
            final C value, final E defaultEnum) {
        if (null == condition) {
            return null;
        }
        return getBy(enumClass, constant -> ObjectKit.equals(condition.apply(constant), value), defaultEnum);
    }

    /**
     * 通过 某字段对应值 获取 枚举，获取不到时为 {@code null}
     *
     * @param enumClass 枚举类
     * @param predicate 条件
     * @param <E>       枚举类型
     * @return 对应枚举 ，获取不到时为 {@code null}
     */
    public static <E extends Enum<E>> E getBy(final Class<E> enumClass, final Predicate<? super E> predicate) {
        return getBy(enumClass, predicate, null);
    }

    /**
     * 通过 某字段对应值 获取 枚举，获取不到时为 {@code defaultEnum}
     *
     * @param enumClass   枚举类
     * @param predicate   条件
     * @param defaultEnum 获取不到时的默认枚举值
     * @param <E>         枚举类型
     * @return 对应枚举 ，获取不到时为 {@code defaultEnum}
     */
    public static <E extends Enum<E>> E getBy(final Class<E> enumClass, final Predicate<? super E> predicate,
            final E defaultEnum) {
        if (null == enumClass || null == predicate) {
            return null;
        }
        return Arrays.stream(enumClass.getEnumConstants()).filter(predicate).findAny().orElse(defaultEnum);
    }

    /**
     * 通过 某字段对应值 获取 枚举中另一字段值，获取不到时为 {@code null}
     *
     * @param field     你想要获取的字段
     * @param condition 条件字段
     * @param value     条件字段值
     * @param <E>       枚举类型
     * @param <F>       想要获取的字段类型
     * @param <C>       条件字段类型
     * @return 对应枚举中另一字段值 ，获取不到时为 {@code null}
     */
    public static <E extends Enum<E>, F, C> F getFieldBy(final FunctionX<E, F> field, final Function<E, C> condition,
            final C value) {
        if (null == field || null == condition) {
            return null;
        }
        Class<E> implClass = LambdaKit.getRealClass(field);
        if (Enum.class.equals(implClass)) {
            implClass = LambdaKit.getRealClass(field);
        }
        return Arrays.stream(implClass.getEnumConstants())
                // 过滤
                .filter(constant -> ObjectKit.equals(condition.apply(constant), value))
                // 获取第一个并转换为结果
                .findFirst().map(field).orElse(null);
    }

    /**
     * 获取枚举字符串值和枚举对象的Map对应，使用LinkedHashMap保证有序 结果中键为枚举名，值为枚举对象
     *
     * @param <E>       枚举类型
     * @param enumClass 枚举类
     * @return 枚举字符串值和枚举对象的Map对应，使用LinkedHashMap保证有序
     */
    public static <E extends Enum<E>> LinkedHashMap<String, E> getEnumMap(final Class<E> enumClass) {
        if (null == enumClass) {
            return null;
        }
        final LinkedHashMap<String, E> map = new LinkedHashMap<>();
        for (final E e : enumClass.getEnumConstants()) {
            map.put(e.name(), e);
        }
        return map;
    }

    /**
     * 获得枚举名对应指定字段值的Map 键为枚举名，值为字段值
     *
     * @param clazz     枚举类
     * @param fieldName 字段名，最终调用getXXX方法
     * @return 枚举名对应指定字段值的Map
     */
    public static Map<String, Object> getNameFieldMap(final Class<? extends Enum<?>> clazz, final String fieldName) {
        if (null == clazz || StringKit.isBlank(fieldName)) {
            return null;
        }
        final Enum<?>[] enums = clazz.getEnumConstants();
        Assert.notNull(enums, "Class [{}] is not an Enum type!", clazz);
        final Map<String, Object> map = MapKit.newHashMap(enums.length, true);
        for (final Enum<?> e : enums) {
            map.put(e.name(), FieldKit.getFieldValue(e, fieldName));
        }
        return map;
    }

    /**
     * 判断指定名称的枚举是否存在
     *
     * @param <E>       枚举类型
     * @param enumClass 枚举类
     * @param name      需要查找的枚举名
     * @return 是否存在
     */
    public static <E extends Enum<E>> boolean contains(final Class<E> enumClass, final String name) {
        final LinkedHashMap<String, E> enumMap = getEnumMap(enumClass);
        if (CollKit.isEmpty(enumMap)) {
            return false;
        }
        return enumMap.containsKey(name);
    }

    /**
     * 判断某个值是不存在枚举中
     *
     * @param <E>       枚举类型
     * @param enumClass 枚举类
     * @param val       需要查找的值
     * @return 是否不存在
     */
    public static <E extends Enum<E>> boolean notContains(final Class<E> enumClass, final String val) {
        return !contains(enumClass, val);
    }

    /**
     * 忽略大小检查某个枚举值是否匹配指定值
     *
     * @param e   枚举值
     * @param val 需要判断的值
     * @return 是非匹配
     */
    public static boolean equalsIgnoreCase(final Enum<?> e, final String val) {
        return StringKit.equalsIgnoreCase(toString(e), val);
    }

    /**
     * 检查某个枚举值是否匹配指定值
     *
     * @param e   枚举值
     * @param val 需要判断的值
     * @return 是非匹配
     */
    public static boolean equals(final Enum<?> e, final String val) {
        return StringKit.equals(toString(e), val);
    }

}
