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
package org.miaixz.bus.core.lang.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.TypeKit;

/**
 * 泛型变量和泛型实际类型映射关系缓存
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ActualTypeMapper {

    private static final WeakConcurrentMap<Type, Map<Type, Type>> CACHE = new WeakConcurrentMap<>();

    /**
     * 获取泛型变量和泛型实际类型的对应关系Map
     *
     * @param type 被解析的包含泛型参数的类
     * @return 泛型对应关系Map
     */
    public static Map<Type, Type> get(final Type type) {
        return CACHE.computeIfAbsent(type, (key) -> createTypeMap(type));
    }

    /**
     * 获取泛型变量名（字符串）和泛型实际类型的对应关系Map
     *
     * @param type 被解析的包含泛型参数的类
     * @return 泛型对应关系Map
     */
    public static Map<String, Type> getStringKeyMap(final Type type) {
        return Convert.toMap(String.class, Type.class, get(type));
    }

    /**
     * 获得泛型变量对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
     *
     * @param type         类
     * @param typeVariable 泛型变量，例如T等
     * @return 实际类型，可能为Class等
     */
    public static Type getActualType(final Type type, final TypeVariable<?> typeVariable) {
        final Map<Type, Type> typeTypeMap = get(type);
        Type result = typeTypeMap.get(typeVariable);
        while (result instanceof TypeVariable) {
            result = typeTypeMap.get(result);
        }
        return result;
    }

    /**
     * 获得泛型变量对应的泛型实际类型，如果此变量没有对应的实际类型，返回null
     *
     * @param type             类
     * @param genericArrayType 泛型数组
     * @return 实际类型，可能为Class等
     */
    public static Type getActualType(final Type type, final GenericArrayType genericArrayType) {
        final Map<Type, Type> typeTypeMap = get(type);
        Type actualType = typeTypeMap.get(genericArrayType);
        if (actualType == null) {
            // 获取泛型数组元素泛型对应的确切类型
            final Type componentType = typeTypeMap.get(genericArrayType.getGenericComponentType());
            if (componentType instanceof Class) {
                actualType = ArrayKit.getArrayType((Class<?>) componentType);
                typeTypeMap.put(genericArrayType, actualType);
            }
        }

        return actualType;
    }

    /**
     * 获取指定泛型变量对应的真实类型 由于子类中泛型参数实现和父类（接口）中泛型定义位置是一一对应的，因此可以通过对应关系找到泛型实现类型
     *
     * @param type          真实类型所在类，此类中记录了泛型参数对应的实际类型
     * @param typeVariables 泛型变量，需要的实际类型对应的泛型参数
     * @return 给定泛型参数对应的实际类型，如果无对应类型，对应位置返回null
     */
    public static Type[] getActualTypes(final Type type, final Type... typeVariables) {
        // 查找方法定义所在类或接口中此泛型参数的位置
        final Type[] result = new Type[typeVariables.length];
        for (int i = 0; i < typeVariables.length; i++) {
            result[i] = (typeVariables[i] instanceof TypeVariable)
                    ? getActualType(type, (TypeVariable<?>) typeVariables[i])
                    : typeVariables[i];
        }
        return result;
    }

    /**
     * 创建类中所有的泛型变量和泛型实际类型的对应关系Map
     *
     * @param type 被解析的包含泛型参数的类
     * @return 泛型对应关系Map
     */
    private static Map<Type, Type> createTypeMap(Type type) {
        final Map<Type, Type> typeMap = new HashMap<>();

        // 按继承层级寻找泛型变量和实际类型的对应关系
        // 在类中，对应关系分为两类：
        // 1. 父类定义变量，子类标注实际类型
        // 2. 父类定义变量，子类继承这个变量，让子类的子类去标注，以此类推
        // 此方法中我们将每一层级的对应关系全部加入到Map中，查找实际类型的时候，根据传入的泛型变量，
        // 找到对应关系，如果对应的是继承的泛型变量，则递归继续找，直到找到实际或返回null为止。
        // 如果传入的非Class，例如TypeReference，获取到泛型参数中实际的泛型对象类，继续按照类处理
        while (null != type) {
            final ParameterizedType parameterizedType = TypeKit.toParameterizedType(type);
            if (null == parameterizedType) {
                break;
            }
            final Type[] typeArguments = parameterizedType.getActualTypeArguments();
            final Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            final Type[] typeParameters = rawType.getTypeParameters();

            Type value;
            for (int i = 0; i < typeParameters.length; i++) {
                value = typeArguments[i];
                // 跳过泛型变量对应泛型变量的情况
                if (!(value instanceof TypeVariable)) {
                    typeMap.put(typeParameters[i], value);
                }
            }

            type = rawType;
        }

        return typeMap;
    }

}
