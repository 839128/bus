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

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.function.Consumer3X;
import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.center.map.*;
import org.miaixz.bus.core.center.map.Dictionary;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;

/**
 * Map相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapKit extends MapGets {

    /**
     * 如果给定Map为空，返回默认Map
     *
     * @param <T>        集合类型
     * @param <K>        键类型
     * @param <V>        值类型
     * @param map        Map
     * @param defaultMap 默认Map
     * @return 非空（empty）的原Map或默认Map
     */
    public static <T extends Map<K, V>, K, V> T defaultIfEmpty(final T map, final T defaultMap) {
        return isEmpty(map) ? defaultMap : map;
    }

    /**
     * 新建一个HashMap
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param size     初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75 + 1
     * @param isLinked Map的Key是否有序，有序返回 {@link LinkedHashMap}，否则返回 {@link HashMap}
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(final int size, final boolean isLinked) {
        final int initialCapacity = (int) (size / Normal.DEFAULT_LOAD_FACTOR) + 1;
        return isLinked ? new LinkedHashMap<>(initialCapacity) : new HashMap<>(initialCapacity);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param size 初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75 + 1
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(final int size) {
        return newHashMap(size, false);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>      Key类型
     * @param <V>      Value类型
     * @param isLinked Map的Key是否有序，有序返回 {@link LinkedHashMap}，否则返回 {@link HashMap}
     * @return HashMap对象
     */
    public static <K, V> HashMap<K, V> newHashMap(final boolean isLinked) {
        return newHashMap(Normal._16, isLinked);
    }

    /**
     * 新建TreeMap，Key有序的Map
     *
     * @param <K>        key的类型
     * @param <V>        value的类型
     * @param comparator Key比较器
     * @return TreeMap
     */
    public static <K, V> TreeMap<K, V> newTreeMap(final Comparator<? super K> comparator) {
        return new TreeMap<>(comparator);
    }

    /**
     * 新建TreeMap，Key有序的Map
     *
     * @param <K>        key的类型
     * @param <V>        value的类型
     * @param map        Map
     * @param comparator Key比较器
     * @return TreeMap
     */
    public static <K, V> TreeMap<K, V> newTreeMap(final Map<K, V> map, final Comparator<? super K> comparator) {
        final TreeMap<K, V> treeMap = new TreeMap<>(comparator);
        if (isNotEmpty(map)) {
            treeMap.putAll(map);
        }
        return treeMap;
    }

    /**
     * 创建键不重复Map
     *
     * @param <K>  key的类型
     * @param <V>  value的类型
     * @param size 初始容量
     * @return {@link IdentityHashMap}
     */
    public static <K, V> Map<K, V> newIdentityMap(final int size) {
        return new IdentityHashMap<>(size);
    }

    /**
     * 创建Map 传入抽象Map{@link AbstractMap}和{@link Map}类将默认创建{@link HashMap}
     *
     * @param <K>     map键类型
     * @param <V>     map值类型
     * @param mapType map类型
     * @return {@link Map}实例
     */
    public static <K, V> Map<K, V> createMap(final Class<?> mapType) {
        return createMap(mapType, HashMap::new);
    }

    /**
     * 创建Map 传入抽象Map{@link AbstractMap}和{@link Map}类将默认创建{@link HashMap}
     *
     * @param <K>        map键类型
     * @param <V>        map值类型
     * @param mapType    map类型
     * @param defaultMap 如果通过反射创建失败或提供的是抽象Map，则创建的默认Map
     * @return {@link Map}实例
     */
    public static <K, V> Map<K, V> createMap(final Class<?> mapType, final Supplier<Map<K, V>> defaultMap) {
        Map<K, V> result = null;
        if (null != mapType && !mapType.isAssignableFrom(AbstractMap.class)) {
            try {
                result = (Map<K, V>) ReflectKit.newInstanceIfPossible(mapType);
            } catch (final Exception ignore) {
                // JDK9+抛出java.lang.reflect.InaccessibleObjectException
                // 跳过
            }
        }

        if (null == result) {
            result = defaultMap.get();
        }

        if (!result.isEmpty()) {
            // 在构造中put值，会导致新建map带有值内容，此处清空
            result.clear();
        }

        return result;
    }

    /**
     * 将单一键值对转换为Map
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param key   键
     * @param value 值
     * @return {@link HashMap}
     */
    public static <K, V> HashMap<K, V> of(final K key, final V value) {
        return of(key, value, false);
    }

    /**
     * 将单一键值对转换为Map
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param key     键
     * @param value   值
     * @param isOrder 是否有序
     * @return {@link HashMap}
     */
    public static <K, V> HashMap<K, V> of(final K key, final V value, final boolean isOrder) {
        final HashMap<K, V> map = newHashMap(isOrder);
        map.put(key, value);
        return map;
    }

    /**
     * 根据给定的键值对数组创建HashMap对象，传入参数必须为key,value,data,value...
     *
     * <p>
     * 奇数参数必须为key，key最后会转换为String类型。
     * </p>
     * <p>
     * 偶数参数必须为value，可以为任意类型。
     * </p>
     *
     * <pre>
     * LinkedHashMap map = MapKit.ofKvs(false, "RED", "#FF0000", "GREEN", "#00FF00", "BLUE", "#0000FF");
     * </pre>
     *
     * @param isLinked      是否使用{@link LinkedHashMap}
     * @param keysAndValues 键值对列表，必须奇数参数为key，偶数参数为value
     * @param <K>           键类型
     * @param <V>           值类型
     * @return LinkedHashMap
     * @see org.miaixz.bus.core.center.map.Dictionary#ofKvs(Object...)
     * @see Dictionary#ofKvs(Object...)
     */
    public static <K, V> Map<K, V> ofKvs(final boolean isLinked, final Object... keysAndValues) {
        if (ArrayKit.isEmpty(keysAndValues)) {
            return newHashMap(0, isLinked);
        }

        Assert.isTrue(keysAndValues.length % 2 == 0, "keysAndValues not in pairs!");

        final Map<K, V> map = newHashMap(keysAndValues.length / 2, isLinked);
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put((K) keysAndValues[i], (V) keysAndValues[i + 1]);
        }
        return map;
    }

    /**
     * 根据给定的Pair数组创建Map对象
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param entries 键值对
     * @return Map
     * @see #entry(Object, Object)
     */
    @SafeVarargs
    public static <K, V> Map<K, V> ofEntries(final Map.Entry<K, V>... entries) {
        return ofEntries((Iterator<Entry<K, V>>) new ArrayIterator<>(entries));
    }

    /**
     * 将Entry集合转换为HashMap
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param entryIter entry集合
     * @return Map
     */
    public static <K, V> HashMap<K, V> ofEntries(final Iterable<Entry<K, V>> entryIter) {
        return ofEntries(IteratorKit.getIter(entryIter));
    }

    /**
     * 将Entry集合转换为HashMap
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param entryIter entry集合
     * @return Map
     */
    public static <K, V> HashMap<K, V> ofEntries(final Iterator<Entry<K, V>> entryIter) {
        final HashMap<K, V> map = new HashMap<>();
        if (IteratorKit.isNotEmpty(entryIter)) {
            Entry<K, V> entry;
            while (entryIter.hasNext()) {
                entry = entryIter.next();
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    /**
     * 将数组转换为Map（HashMap），支持数组元素类型为：
     *
     * <pre>
     * Map.Entry
     * 长度大于1的数组（取前两个值），如果不满足跳过此元素
     * Iterable 长度也必须大于1（取前两个值），如果不满足跳过此元素
     * Iterator 长度也必须大于1（取前两个值），如果不满足跳过此元素
     * </pre>
     *
     * <pre>
     * Map&lt;Object, Object&gt; colorMap = MapKit
     *         .of(new String[][] { { "RED", "#FF0000" }, { "GREEN", "#00FF00" }, { "BLUE", "#0000FF" } });
     * </pre>
     * <p>
     * 参考：commons-lang
     *
     * @param array 数组。元素类型为Map.Entry、数组、Iterable、Iterator
     * @return {@link HashMap}
     */
    public static HashMap<Object, Object> of(final Object[] array) {
        if (array == null) {
            return null;
        }
        final HashMap<Object, Object> map = new HashMap<>((int) (array.length * 1.5));
        for (int i = 0; i < array.length; i++) {
            final Object object = array[i];
            if (object instanceof Map.Entry entry) {
                map.put(entry.getKey(), entry.getValue());
            } else if (object instanceof Object[] entry) {
                if (entry.length > 1) {
                    map.put(entry[0], entry[1]);
                }
            } else if (object instanceof Iterable) {
                final Iterator iter = ((Iterable) object).iterator();
                if (iter.hasNext()) {
                    final Object key = iter.next();
                    if (iter.hasNext()) {
                        final Object value = iter.next();
                        map.put(key, value);
                    }
                }
            } else if (object instanceof Iterator iter) {
                if (iter.hasNext()) {
                    final Object key = iter.next();
                    if (iter.hasNext()) {
                        final Object value = iter.next();
                        map.put(key, value);
                    }
                }
            } else {
                throw new IllegalArgumentException(StringKit.format(
                        "Array element {}, '{}', is not type of Map.Entry or Array or Iterable or Iterator", i,
                        object));
            }
        }
        return map;
    }

    /**
     * 行转列，合并相同的键，值合并为列表 将Map列表中相同key的值组成列表做为Map的value 是{@link #toMapList(Map)}的逆方法 比如传入数据：
     *
     * <pre>
     * [
     *  {a: 1, b: 1, c: 1}
     *  {a: 2, b: 2}
     *  {a: 3, b: 3}
     *  {a: 4}
     * ]
     * </pre>
     * <p>
     * 结果是：
     *
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param mapList Map列表
     * @return Map
     */
    public static <K, V> Map<K, List<V>> toListMap(final Iterable<? extends Map<K, V>> mapList) {
        final Map<K, List<V>> resultMap = new HashMap<>();
        if (CollKit.isEmpty(mapList)) {
            return resultMap;
        }

        for (final Map<K, V> map : mapList) {
            for (final Entry<K, V> entry : map.entrySet()) {
                resultMap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }

        return resultMap;
    }

    /**
     * 列转行。将Map中值列表分别按照其位置与key组成新的map。 是{@link #toListMap(Iterable)}的逆方法 比如传入数据：
     *
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     * <p>
     * 结果是：
     *
     * <pre>
     * [
     *  {a: 1, b: 1, c: 1}
     *  {a: 2, b: 2}
     *  {a: 3, b: 3}
     *  {a: 4}
     * ]
     * </pre>
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param listMap 列表Map
     * @return Map列表
     */
    public static <K, V> List<Map<K, V>> toMapList(final Map<K, ? extends Iterable<V>> listMap) {
        if (isEmpty(listMap)) {
            return ListKit.zero();
        }

        final List<Map<K, V>> resultList = new ArrayList<>();
        for (final Entry<K, ? extends Iterable<V>> entry : listMap.entrySet()) {
            final Iterator<V> iterator = IteratorKit.getIter(entry.getValue());
            if (IteratorKit.isEmpty(iterator)) {
                continue;
            }
            final K key = entry.getKey();
            // 对已经存在的map添加元素
            for (final Map<K, V> map : resultList) {
                // 还可以继续添加元素
                if (iterator.hasNext()) {
                    map.put(key, iterator.next());
                } else {
                    break;
                }
            }
            // entry的value的个数 大于 当前列表的size, 直接新增map
            while (iterator.hasNext()) {
                resultList.add(MapKit.of(key, iterator.next()));
            }
        }

        return resultList;
    }

    /**
     * 将已知Map转换为key为驼峰风格的Map 如果KEY为非String类型，保留原值
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map 原Map
     * @return 驼峰风格Map
     */
    public static <K, V> Map<K, V> toCamelCaseMap(final Map<K, V> map) {
        return (map instanceof LinkedHashMap) ? new CamelCaseLinkedMap<>(map) : new CamelCaseMap<>(map);
    }

    /**
     * 将键值对转换为二维数组，第一维是key，第二维是value
     *
     * @param map map
     * @return 数组
     */
    public static Object[][] toObjectArray(final Map<?, ?> map) {
        if (map == null) {
            return null;
        }
        final Object[][] result = new Object[map.size()][2];
        if (map.isEmpty()) {
            return result;
        }
        int index = 0;
        for (final Entry<?, ?> entry : map.entrySet()) {
            result[index][0] = entry.getKey();
            result[index][1] = entry.getValue();
            index++;
        }
        return result;
    }

    /**
     * 将map转成字符串
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param otherParams       其它附加参数字符串（例如密钥）
     * @return 连接字符串
     */
    public static <K, V> String join(final Map<K, V> map, final String separator, final String keyValueSeparator,
            final String... otherParams) {
        return join(map, separator, keyValueSeparator, false, otherParams);
    }

    /**
     * 根据参数排序后拼接为字符串，常用于签名
     *
     * @param params            参数
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param otherParams       其它附加参数字符串（例如密钥）
     * @return 签名字符串
     */
    public static String sortJoin(final Map<?, ?> params, final String separator, final String keyValueSeparator,
            final boolean isIgnoreNull, final String... otherParams) {
        return join(sort(params), separator, keyValueSeparator, isIgnoreNull, otherParams);
    }

    /**
     * 将map转成字符串，忽略null的键和值
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param otherParams       其它附加参数字符串（例如密钥）
     * @return 连接后的字符串
     */
    public static <K, V> String joinIgnoreNull(final Map<K, V> map, final String separator,
            final String keyValueSeparator, final String... otherParams) {
        return join(map, separator, keyValueSeparator, true, otherParams);
    }

    /**
     * 将map转成字符串
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map，为空返回otherParams拼接
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param isIgnoreNull      是否忽略null的键和值
     * @param otherParams       其它附加参数字符串（例如密钥）
     * @return 连接后的字符串，map和otherParams为空返回""
     */
    public static <K, V> String join(final Map<K, V> map, final String separator, final String keyValueSeparator,
            final boolean isIgnoreNull, final String... otherParams) {
        return join(map, separator, keyValueSeparator,
                (entry) -> !isIgnoreNull || entry.getKey() != null && entry.getValue() != null, otherParams);
    }

    /**
     * 将map转成字符串
     *
     * @param <K>               键类型
     * @param <V>               值类型
     * @param map               Map，为空返回otherParams拼接
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @param predicate         键值对过滤
     * @param otherParams       其它附加参数字符串（例如密钥）
     * @return 连接后的字符串，map和otherParams为空返回""
     */
    public static <K, V> String join(final Map<K, V> map, final String separator, final String keyValueSeparator,
            final Predicate<Entry<K, V>> predicate, final String... otherParams) {
        return MapJoiner.of(separator, keyValueSeparator).append(map, predicate).append(otherParams).toString();
    }

    /**
     * 编辑Map 编辑过程通过传入的Editor实现来返回需要的元素内容，这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，如果返回{@code
     * null
     * }表示这个元素对象抛弃
     * 2、修改元素对象，返回集合中为修改后的对象
     * </pre>
     *
     * @param <K>    Key类型
     * @param <V>    Value类型
     * @param map    Map
     * @param editor 编辑器接口
     * @return 编辑后的Map
     */
    public static <K, V> Map<K, V> edit(final Map<K, V> map, final UnaryOperator<Entry<K, V>> editor) {
        if (null == map || null == editor) {
            return map;
        }

        final Map<K, V> map2 = createMap(map.getClass(), () -> new HashMap<>(map.size(), 1f));
        if (isEmpty(map)) {
            return map2;
        }

        Entry<K, V> modified;
        for (final Entry<K, V> entry : map.entrySet()) {
            modified = editor.apply(entry);
            if (null != modified) {
                map2.put(modified.getKey(), modified.getValue());
            }
        }
        return map2;
    }

    /**
     * 过滤 过滤过程通过传入的Editor实现来返回需要的元素内容，这个Filter实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，如果返回null表示这个元素对象抛弃
     * </pre>
     *
     * @param <K>       Key类型
     * @param <V>       Value类型
     * @param map       Map
     * @param predicate 过滤器接口，{@link Predicate#test(Object)}为{@code true}保留，{@code null}返回原Map
     * @return 过滤后的Map
     */
    public static <K, V> Map<K, V> filter(final Map<K, V> map, final Predicate<Entry<K, V>> predicate) {
        if (null == map || null == predicate) {
            return map;
        }
        return edit(map, t -> predicate.test(t) ? t : null);
    }

    /**
     * 通过biFunction自定义一个规则，此规则将原Map中的元素转换成新的元素，生成新的Map返回 变更过程通过传入的 {@link BiFunction} 实现来返回一个值可以为不同类型的 {@link Map}
     *
     * @param map        原有的map
     * @param biFunction {@code lambda}，参数包含{@code data},{@code value}，返回值会作为新的{@code value}
     * @param <K>        {@code data}的类型
     * @param <V>        {@code value}的类型
     * @param <R>        新的，修改后的{@code value}的类型
     * @return 值可以为不同类型的 {@link Map}
     */
    public static <K, V, R> Map<K, R> map(final Map<K, V> map, final BiFunction<K, V, R> biFunction) {
        if (null == map || null == biFunction) {
            return MapKit.newHashMap();
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, m -> biFunction.apply(m.getKey(), m.getValue())));
    }

    /**
     * 过滤Map保留指定键值对，如果键不存在跳过
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param map  原始Map
     * @param keys 键列表，{@code null}返回原Map
     * @return Map 结果，结果的Map类型与原Map保持一致
     */
    public static <K, V> Map<K, V> filter(final Map<K, V> map, final K... keys) {
        if (null == map || null == keys) {
            return map;
        }

        final Map<K, V> map2 = createMap(map.getClass(), () -> new HashMap<>(map.size(), 1f));
        if (isEmpty(map)) {
            return map2;
        }

        for (final K key : keys) {
            if (map.containsKey(key)) {
                map2.put(key, map.get(key));
            }
        }
        return map2;
    }

    /**
     * Map的键和值互换 互换键值对不检查值是否有重复，如果有则后加入的元素替换先加入的元素 值的顺序在HashMap中不确定，所以谁覆盖谁也不确定，在有序的Map中按照先后顺序覆盖，保留最后的值
     *
     * @param <T> 键和值类型
     * @param map Map对象，键值类型必须一致
     * @return 互换后的Map
     * @see #inverse(Map)
     */
    public static <T> Map<T, T> reverse(final Map<T, T> map) {
        return edit(map, t -> new Entry<T, T>() {

            @Override
            public T getKey() {
                return t.getValue();
            }

            @Override
            public T getValue() {
                return t.getKey();
            }

            @Override
            public T setValue(final T value) {
                throw new UnsupportedOperationException("Unsupported setValue method !");
            }
        });
    }

    /**
     * Map的键和值互换 互换键值对不检查值是否有重复，如果有则后加入的元素替换先加入的元素 值的顺序在HashMap中不确定，所以谁覆盖谁也不确定，在有序的Map中按照先后顺序覆盖，保留最后的值
     *
     * @param <K> 键和值类型
     * @param <V> 键和值类型
     * @param map Map对象
     * @return 互换后的Map
     */
    public static <K, V> Map<V, K> inverse(final Map<K, V> map) {
        final Map<V, K> result = createMap(map.getClass());
        map.forEach((key, value) -> result.put(value, key));
        return result;
    }

    /**
     * 排序已有Map，Key有序的Map，使用默认Key排序方式（字母顺序）
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map Map
     * @return TreeMap
     * @see #newTreeMap(Map, Comparator)
     */
    public static <K, V> TreeMap<K, V> sort(final Map<K, V> map) {
        return sort(map, null);
    }

    /**
     * 排序已有Map，Key有序的Map
     *
     * @param <K>        key的类型
     * @param <V>        value的类型
     * @param map        Map，为null返回null
     * @param comparator Key比较器
     * @return TreeMap，map为null返回null
     * @see #newTreeMap(Map, Comparator)
     */
    public static <K, V> TreeMap<K, V> sort(final Map<K, V> map, final Comparator<? super K> comparator) {
        if (null == map) {
            return null;
        }

        if (map instanceof TreeMap<K, V> result) {
            // 已经是可排序Map，此时只有比较器一致才返回原map
            if (null == comparator || comparator.equals(result.comparator())) {
                return result;
            }
        }

        return newTreeMap(map, comparator);
    }

    /**
     * 按照值排序，可选是否倒序
     *
     * @param map    需要对值排序的map
     * @param <K>    键类型
     * @param <V>    值类型
     * @param isDesc 是否倒序
     * @return 排序后新的Map
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(final Map<K, V> map,
            final boolean isDesc) {
        final Map<K, V> result = new LinkedHashMap<>();
        Comparator<Entry<K, V>> entryComparator = Entry.comparingByValue();
        if (isDesc) {
            entryComparator = entryComparator.reversed();
        }
        map.entrySet().stream().sorted(entryComparator).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    /**
     * 创建代理Map {@link MapProxy}对Map做一次包装，提供各种getXXX方法
     *
     * @param map 被代理的Map
     * @return {@link MapProxy}
     */
    public static MapProxy createProxy(final Map<?, ?> map) {
        return MapProxy.of(map);
    }

    /**
     * 创建Map包装类MapWrapper {@link MapWrapper}对Map做一次包装
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map 被代理的Map
     * @return {@link MapWrapper}
     */
    public static <K, V> MapWrapper<K, V> wrap(final Map<K, V> map) {
        return new MapWrapper<>(map);
    }

    /**
     * 将对应Map转换为不可修改的Map
     *
     * @param map Map
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 不修改Map
     */
    public static <K, V> Map<K, V> view(final Map<K, V> map) {
        return Collections.unmodifiableMap(map);
    }

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder() {
        return builder(new HashMap<>());
    }

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param map 实际使用的map
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder(final Map<K, V> map) {
        return new MapBuilder<>(map);
    }

    /**
     * 创建链接调用map
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @param k   data
     * @param v   value
     * @return map创建类
     */
    public static <K, V> MapBuilder<K, V> builder(final K k, final V v) {
        return (builder(new HashMap<K, V>())).put(k, v);
    }

    /**
     * 获取Map的部分key生成新的Map
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param map  Map
     * @param keys 键列表
     * @return 新Map，只包含指定的key
     */
    public static <K, V> Map<K, V> getAny(final Map<K, V> map, final K... keys) {
        return filter(map, entry -> ArrayKit.contains(keys, entry.getKey()));
    }

    /**
     * 去掉Map中指定key的键值对，修改原Map
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param <T>  Map类型
     * @param map  Map
     * @param keys 键列表
     * @return 修改后的key
     */
    public static <K, V, T extends Map<K, V>> T removeAny(final T map, final K... keys) {
        for (final K key : keys) {
            map.remove(key);
        }
        return map;
    }

    /**
     * 重命名键 实现方式为移除然后重新put，当旧的key不存在直接返回 当新的key存在，抛出{@link IllegalArgumentException} 异常
     *
     * @param <K>    key的类型
     * @param <V>    value的类型
     * @param map    Map
     * @param oldKey 原键
     * @param newKey 新键
     * @return map
     * @throws IllegalArgumentException 新key存在抛出此异常
     */
    public static <K, V> Map<K, V> renameKey(final Map<K, V> map, final K oldKey, final K newKey) {
        if (isNotEmpty(map) && map.containsKey(oldKey)) {
            if (map.containsKey(newKey)) {
                throw new IllegalArgumentException(StringKit.format("The data '{}' exist !", newKey));
            }
            map.put(newKey, map.remove(oldKey));
        }
        return map;
    }

    /**
     * 去除Map中值为{@code null}的键值对 注意：此方法在传入的Map上直接修改。
     *
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map Map
     * @return map
     */
    public static <K, V> Map<K, V> removeNullValue(final Map<K, V> map) {
        return removeIf(map, entry -> null == entry.getValue());
    }

    /**
     * 去除Map中值为指定值的键值对 注意：此方法在传入的Map上直接修改。
     *
     * @param <K>   key的类型
     * @param <V>   value的类型
     * @param map   Map
     * @param value 给定值
     * @return map
     */
    public static <K, V> Map<K, V> removeByValue(final Map<K, V> map, final V value) {
        return removeIf(map, entry -> ObjectKit.equals(value, entry.getValue()));
    }

    /**
     * 去除Map中值为{@code null}的键值对 注意：此方法在传入的Map上直接修改。
     *
     * @param <K>       key的类型
     * @param <V>       value的类型
     * @param map       Map
     * @param predicate 移除条件，当{@link Predicate#test(Object)}为{@code true}时移除
     * @return map
     */
    public static <K, V> Map<K, V> removeIf(final Map<K, V> map, final Predicate<Entry<K, V>> predicate) {
        if (isEmpty(map)) {
            return map;
        }
        map.entrySet().removeIf(predicate);
        return map;
    }

    /**
     * 返回一个空Map
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 空Map
     * @see Collections#emptyMap()
     */
    public static <K, V> Map<K, V> empty() {
        return Collections.emptyMap();
    }

    /**
     * 返回一个初始大小为0的HashMap(初始为0，可加入元素)
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 初始大小为0的HashMap
     */
    public static <K, V> Map<K, V> zero() {
        return new HashMap<>(0, 1);
    }

    /**
     * 返回一个只包含一个键值对的Map，不可变
     *
     * @param key   键
     * @param value 值
     * @param <K>   键类型
     * @param <V>   值类型
     * @return Map
     */
    public static <K, V> Map<K, V> singleton(final K key, final V value) {
        return Collections.singletonMap(key, value);
    }

    /**
     * 根据传入的Map类型不同，返回对应类型的空Map，支持类型包括：
     *
     * <pre>
     *     1. NavigableMap
     *     2. SortedMap
     *     3. Map
     * </pre>
     *
     * @param <K>      键类型
     * @param <V>      值类型
     * @param <T>      Map类型
     * @param mapClass Map类型，null返回默认的Map
     * @return 空Map
     */
    public static <K, V, T extends Map<K, V>> T empty(final Class<?> mapClass) {
        if (null == mapClass) {
            return (T) Collections.emptyMap();
        }
        if (NavigableMap.class == mapClass) {
            return (T) Collections.emptyNavigableMap();
        } else if (SortedMap.class == mapClass) {
            return (T) Collections.emptySortedMap();
        } else if (Map.class == mapClass) {
            return (T) Collections.emptyMap();
        }

        // 不支持空集合的集合类型
        throw new IllegalArgumentException(StringKit.format("[{}] is not support to get empty!", mapClass));
    }

    /**
     * 清除一个或多个Map集合内的元素，每个Map调用clear()方法
     *
     * @param maps 一个或多个Map
     */
    public static void clear(final Map<?, ?>... maps) {
        for (final Map<?, ?> map : maps) {
            if (isNotEmpty(map)) {
                map.clear();
            }
        }
    }

    /**
     * 从Map中获取指定键列表对应的值列表 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     */
    public static <K, V> List<V> valuesOfKeys(final Map<K, V> map, final K... keys) {
        return valuesOfKeys(map, (Iterator<K>) new ArrayIterator<>(keys));
    }

    /**
     * 从Map中获取指定键列表对应的值列表 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     */
    public static <K, V> List<V> valuesOfKeys(final Map<K, V> map, final Iterable<K> keys) {
        return valuesOfKeys(map, keys.iterator());
    }

    /**
     * 从Map中获取指定键列表对应的值列表 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     */
    public static <K, V> List<V> valuesOfKeys(final Map<K, V> map, final Iterator<K> keys) {
        final List<V> list = new ArrayList<>();
        while (keys.hasNext()) {
            list.add(map.get(keys.next()));
        }
        return list;
    }

    /**
     * 将键和值转换为{@link AbstractMap.SimpleImmutableEntry} 返回的Entry不可变
     *
     * @param key   键
     * @param value 值
     * @param <K>   键类型
     * @param <V>   值类型
     * @return {@link AbstractMap.SimpleImmutableEntry}
     */
    public static <K, V> Map.Entry<K, V> entry(final K key, final V value) {
        return entry(key, value, true);
    }

    /**
     * 将键和值转换为{@link AbstractMap.SimpleEntry} 或者 {@link AbstractMap.SimpleImmutableEntry}
     *
     * @param key         键
     * @param value       值
     * @param <K>         键类型
     * @param <V>         值类型
     * @param isImmutable 是否不可变Entry
     * @return {@link AbstractMap.SimpleEntry} 或者 {@link AbstractMap.SimpleImmutableEntry}
     */
    public static <K, V> Map.Entry<K, V> entry(final K key, final V value, final boolean isImmutable) {
        return isImmutable ? new AbstractMap.SimpleImmutableEntry<>(key, value)
                : new AbstractMap.SimpleEntry<>(key, value);
    }

    /**
     * 将列表按照给定的键生成器规则和值生成器规则，加入到给定的Map中
     *
     * @param resultMap 结果Map，通过传入map对象决定结果的Map类型，如果为{@code null}，默认使用HashMap
     * @param iterable  值列表
     * @param keyMapper Map的键映射
     * @param <K>       键类型
     * @param <V>       值类型
     * @return HashMap
     */
    public static <K, V> Map<K, V> putAll(final Map<K, V> resultMap, final Iterable<V> iterable,
            final Function<V, K> keyMapper) {
        return putAll(resultMap, iterable, keyMapper, Function.identity());
    }

    /**
     * 将列表按照给定的键生成器规则和值生成器规则，加入到给定的Map中
     *
     * @param resultMap   结果Map，通过传入map对象决定结果的Map类型
     * @param iterable    值列表
     * @param keyMapper   Map的键映射
     * @param valueMapper Map的值映射
     * @param <T>         列表值类型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return HashMap
     */
    public static <T, K, V> Map<K, V> putAll(final Map<K, V> resultMap, final Iterable<T> iterable,
            final Function<T, K> keyMapper, final Function<T, V> valueMapper) {
        return putAll(resultMap, IteratorKit.getIter(iterable), keyMapper, valueMapper);
    }

    /**
     * 将列表按照给定的键生成器规则和值生成器规则，加入到给定的Map中
     *
     * @param resultMap 结果Map，通过传入map对象决定结果的Map类型，如果为{@code null}，默认使用HashMap
     * @param iterator  值列表
     * @param keyMapper Map的键映射
     * @param <K>       键类型
     * @param <V>       值类型
     * @return HashMap
     */
    public static <K, V> Map<K, V> putAll(final Map<K, V> resultMap, final Iterator<V> iterator,
            final Function<V, K> keyMapper) {
        return putAll(resultMap, iterator, keyMapper, Function.identity());
    }

    /**
     * 将列表按照给定的键生成器规则和值生成器规则，加入到给定的Map中
     *
     * @param resultMap   结果Map，通过传入map对象决定结果的Map类型，如果为{@code null}，默认使用HashMap
     * @param iterator    值列表
     * @param keyMapper   Map的键映射
     * @param valueMapper Map的值映射
     * @param <T>         列表值类型
     * @param <K>         键类型
     * @param <V>         值类型
     * @return HashMap
     */
    public static <T, K, V> Map<K, V> putAll(Map<K, V> resultMap, final Iterator<T> iterator,
            final Function<T, K> keyMapper, final Function<T, V> valueMapper) {
        if (null == resultMap) {
            resultMap = MapKit.newHashMap();
        }
        if (ObjectKit.isNull(iterator)) {
            return resultMap;
        }

        T value;
        while (iterator.hasNext()) {
            value = iterator.next();
            resultMap.put(keyMapper.apply(value), valueMapper.apply(value));
        }
        return resultMap;
    }

    /**
     * 根据给定的entry列表，根据entry的key进行分组;
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param entries entry列表
     * @return entries
     */
    public static <K, V> Map<K, List<V>> grouping(final Iterable<Map.Entry<K, V>> entries) {
        if (CollKit.isEmpty(entries)) {
            return zero();
        }

        final Map<K, List<V>> map = new HashMap<>();
        for (final Map.Entry<K, V> pair : entries) {
            final List<V> values = map.computeIfAbsent(pair.getKey(), k -> new ArrayList<>());
            values.add(pair.getValue());
        }
        return map;
    }

    /**
     * 将一个Map按照固定大小拆分成多个子Map
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  Map
     * @param size 子Map的大小
     * @return 子Map列表
     */
    public static <K, V> List<Map<K, V>> partition(final Map<K, V> map, final int size) {
        Assert.notNull(map);
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        final List<Map<K, V>> list = new ArrayList<>();
        final Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map<K, V> subMap = new HashMap<>(size);
            for (int i = 0; i < size && iterator.hasNext(); i++) {
                final Map.Entry<K, V> entry = iterator.next();
                subMap.put(entry.getKey(), entry.getValue());
            }
            list.add(subMap);
        }
        return list;
    }

    /**
     * 遍历Map，返回第一个匹配的value值
     *
     * @param map       map
     * @param predicate 匹配条件
     * @param <K>       键类型
     * @param <V>       值类型
     * @return value值
     */
    public static <K, V> V firstMatchValue(final Map<K, V> map, final Predicate<Entry<K, V>> predicate) {
        final Entry<K, V> kvEntry = firstMatch(map, predicate);
        if (null != kvEntry) {
            return kvEntry.getValue();
        }
        return null;
    }

    /**
     * 遍历Map，返回第一个匹配的entry值
     *
     * @param map       map
     * @param predicate 匹配条件
     * @param <K>       键类型
     * @param <V>       值类型
     * @return entry
     */
    public static <K, V> Entry<K, V> firstMatch(final Map<K, V> map, final Predicate<Entry<K, V>> predicate) {
        if (isNotEmpty(map)) {
            for (final Entry<K, V> entry : map.entrySet()) {
                if (predicate.test(entry)) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * 循环遍历Map，使用{@link Consumer3X} 接受遍历的每条数据，并针对每条数据做处理, 和JDK8中的map.forEach不同的是，此方法支持index
     *
     * @param <K>        Key类型
     * @param <V>        Value类型
     * @param map        {@link Map}
     * @param kvConsumer {@link Consumer3X} 遍历的每条数据处理器
     */
    public static <K, V> void forEach(final Map<K, V> map, final Consumer3X<Integer, K, V> kvConsumer) {
        if (map == null) {
            return;
        }
        int index = 0;
        for (final Entry<K, V> entry : map.entrySet()) {
            kvConsumer.accept(index, entry.getKey(), entry.getValue());
            index++;
        }
    }

}
