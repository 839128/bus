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

import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.CollectionOperation;
import org.miaixz.bus.core.center.CollectionStream;
import org.miaixz.bus.core.center.TransCollection;
import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.function.Consumer3X;
import org.miaixz.bus.core.center.iterator.ArrayIterator;
import org.miaixz.bus.core.center.iterator.IteratorEnumeration;
import org.miaixz.bus.core.center.set.UniqueKeySet;
import org.miaixz.bus.core.codec.hash.Hash32;
import org.miaixz.bus.core.compare.PinyinCompare;
import org.miaixz.bus.core.compare.PropertyCompare;
import org.miaixz.bus.core.convert.CompositeConverter;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.convert.Converter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * 集合相关工具类 此工具方法针对{@link Collection}或{@link Iterable}及其实现类封装的工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollKit extends CollectionStream {

    /**
     * 去重集合
     *
     * @param <T> 集合元素类型
     * @param key 属性名
     * @return {@link List}
     */
    public static <T> Predicate<T> distinct(final Function<? super T, ?> key) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(key.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 去重集合
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link List}
     */
    public static <T> List<T> distinct(final Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        } else if (collection instanceof Set) {
            return new ArrayList<>(collection);
        } else {
            return new ArrayList<>(new LinkedHashSet<>(collection));
        }
    }

    /**
     * 根据函数生成的KEY去重集合，如根据Bean的某个或者某些字段完成去重。 去重可选是保留最先加入的值还是后加入的值
     *
     * @param <T>        集合元素类型
     * @param <K>        唯一键类型
     * @param collection 集合
     * @param key        唯一标识
     * @param override   是否覆盖模式，如果为{@code true}，加入的新值会覆盖相同key的旧值，否则会忽略新加值
     * @return {@link List}
     */
    public static <T, K> List<T> distinct(final Collection<T> collection, final Function<T, K> key,
            final boolean override) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        }

        final UniqueKeySet<K, T> set = new UniqueKeySet<>(true, key);
        if (override) {
            set.addAll(collection);
        } else {
            set.addAllIfAbsent(collection);
        }
        return new ArrayList<>(set);
    }

    /**
     * 多个集合的并集 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留最多的个数 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c, c,
     * c]，此结果中只保留了三个c
     *
     * @param <T>   集合元素类型
     * @param colls 集合数组
     * @return 并集的集合，返回 {@link ArrayList}
     */
    @SafeVarargs
    public static <T> Collection<T> union(final Collection<? extends T>... colls) {
        return CollectionOperation.of(colls).union();
    }

    /**
     * 多个集合的非重复并集，类似于SQL中的“UNION DISTINCT” 针对一个集合中存在多个相同元素的情况，只保留一个 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b,
     * c]，此结果中只保留了一个c
     *
     * @param <T>   集合元素类型
     * @param colls 列表集合
     * @return 并集的集合，返回 {@link LinkedHashSet}
     */
    @SafeVarargs
    public static <T> Set<T> unionDistinct(final Collection<? extends T>... colls) {
        return CollectionOperation.of(colls).unionDistinct();
    }

    /**
     * 多个集合的完全并集，类似于SQL中的“UNION ALL” 针对一个集合中存在多个相同元素的情况，保留全部元素 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c, c,
     * c, a, b, c, c]
     *
     * @param <T>   集合元素类型
     * @param colls 集合数组
     * @return 并集的集合，返回 {@link ArrayList}
     */
    @SafeVarargs
    public static <T> List<T> unionAll(final Collection<? extends T>... colls) {
        return CollectionOperation.of(colls).unionAll();
    }

    /**
     * 多个集合的交集 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留最少的个数 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c,
     * c]，此结果中只保留了两个c
     *
     * @param <T>   集合元素类型
     * @param colls 集合列表
     * @return 交集的集合，返回 {@link ArrayList}
     */
    @SafeVarargs
    public static <T> Collection<T> intersection(final Collection<T>... colls) {
        return CollectionOperation.of(colls).intersection();
    }

    /**
     * 多个集合的交集 针对一个集合中存在多个相同元素的情况，只保留一个 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c]，此结果中只保留了一个c
     *
     * @param <T>   集合元素类型
     * @param colls 集合列表
     * @return 交集的集合，返回 {@link LinkedHashSet}
     */
    @SafeVarargs
    public static <T> Set<T> intersectionDistinct(final Collection<T>... colls) {
        return CollectionOperation.of(colls).intersectionDistinct();
    }

    /**
     * 两个集合的对称差集 (A-B)∪(B-A) 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留两个集合中此元素个数差的个数 例如：
     * 
     * <pre>
     *     disjunction([a, b, c, c, c], [a, b, c, c]) - [c]
     *     disjunction([a, b], [])                    - [a, b]
     *     disjunction([a, b, c], [b, c, d])          - [a, d]
     * </pre>
     * 
     * 任意一个集合为空，返回另一个集合 两个集合无差集则返回空集合
     *
     * @param <T>   集合元素类型
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 差集的集合，返回 {@link ArrayList}
     */
    public static <T> Collection<T> disjunction(final Collection<T> coll1, final Collection<T> coll2) {
        return CollectionOperation.of(coll1, coll2).disjunction();
    }

    /**
     * 计算集合的单差集，即只返回【集合1】中有，但是【集合2】中没有的元素，例如：
     *
     * <pre>
     *     subtract([1,2,3,4],[2,3,4,5]) - [1]
     * </pre>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   元素类型
     * @return 单差集
     */
    public static <T> Collection<T> subtract(final Collection<T> coll1, final Collection<T> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return coll1;
        }
        Collection<T> result = ObjectKit.clone(coll1);
        try {
            if (null == result) {
                result = create(coll1.getClass());
                result.addAll(coll1);
            }
            result.removeAll(coll2);
        } catch (UnsupportedOperationException e) {
            // 针对 coll1 为只读集合的补偿
            result = create(AbstractCollection.class);
            result.addAll(coll1);
            result.removeAll(coll2);
        }
        return result;
    }

    /**
     * 计算集合的单差集，即只返回【集合1】中有，但是【集合2】中没有的元素，例如：
     *
     * <pre>
     *     subtractToList([1,2,3,4],[2,3,4,5]) -》 [1]
     * </pre>
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <T>   元素类型
     * @return 单差集
     * @since 5.3.5
     */
    public static <T> List<T> subtractToList(Collection<T> coll1, Collection<T> coll2) {
        return subtractToList(coll1, coll2, true);
    }

    /**
     * 计算集合的单差集，即只返回【集合1】中有，但是【集合2】中没有的元素 只要【集合1】中的某个元素在【集合2】中存在（equals和hashcode），就会被排除
     *
     * <pre>
     * 示例：
     * 1. subtractToList([null, null, null, null], [null, null]) → []
     * 2. subtractToList([null, null, null, null], [null, null, "c"]) → []
     * 3. subtractToList(["a", "b", "c"], ["a", "b", "c"]) → []
     * 4. subtractToList([], ["a", "b", "c"]) → []
     * 5. subtractToList(["a", "b", "c"], []) → ["a", "b", "c"]
     * 6. subtractToList(["a", "a", "b", "b", "c", "c", "d"], ["b", "c"]) → ["a", "a", "d"]
     * 7. subtractToList(["a", null, "b"], ["a", "c"]) → [null, "b"]
     * 8. subtractToList(["a", "b", "c"], ["d", "e", "f"]) → ["a", "b", "c"]
     * 9. subtractToList(["a", "a", "b", "b", "c"], ["d", "e", "f"]) → ["a", "a", "b", "b", "c"]
     * </pre>
     *
     * @param coll1    集合1，需要计算差集的源集合
     * @param coll2    集合2，需要从集合1中排除的元素所在集合
     * @param isLinked 返回的集合类型是否是LinkedList，{@code true}返回{@link LinkedList}，{@code false}返回{@link ArrayList}
     * @param <T>      元素类型
     * @return 单差集结果。当【集合1】为空时返回空列表；当【集合2】为空时返回集合1的拷贝；否则返回【集合1】中排除【集合2】中所有元素后的结果
     */
    public static <T> List<T> subtractToList(Collection<T> coll1, Collection<T> coll2, boolean isLinked) {
        if (isEmpty(coll1)) {
            return ListKit.empty();
        }

        if (isEmpty(coll2)) {
            // TODO
            // return ListKit.list(isLinked, coll1);
        }

        /*
         * 返回的集合最大不会超过 coll1.size 所以这里创建 ArrayList 时 initialCapacity 给的是 coll1.size 这样做可以避免频繁扩容
         */
        final List<T> result = isLinked ? new LinkedList<>() : new ArrayList<>(coll1.size());

        Set<T> set = new HashSet<>(coll2);
        for (T t : coll1) {
            if (!set.contains(t)) {
                result.add(t);
            }
        }

        return result;
    }

    /**
     * 判断指定集合是否包含指定值，如果集合为空（null或者空），返回{@code false}，否则找到元素返回{@code true}
     *
     * @param collection 集合
     * @param value      需要查找的值
     * @return 如果集合为空（null或者空），返回{@code false}，否则找到元素返回{@code true}
     * @throws ClassCastException   如果类型不一致会抛出转换异常
     * @throws NullPointerException 当指定的元素 值为 null ,或集合类不支持null 时抛出该异常
     * @see Collection#contains(Object)
     */
    public static boolean contains(final Collection<?> collection, final Object value) {
        return isNotEmpty(collection) && collection.contains(value);
    }

    /**
     * 判断指定集合是否包含指定值，如果集合为空（null或者空），返回{@code false}，否则找到元素返回{@code true}
     *
     * @param collection 集合
     * @param value      需要查找的值
     * @return 果集合为空（null或者空），返回{@code false}，否则找到元素返回{@code true}
     */
    public static boolean safeContains(final Collection<?> collection, final Object value) {
        try {
            return contains(collection, value);
        } catch (final ClassCastException | NullPointerException e) {
            return false;
        }
    }

    /**
     * 自定义函数判断集合是否包含某类值
     *
     * @param collection  集合
     * @param containFunc 自定义判断函数
     * @param <T>         值类型
     * @return 是否包含自定义规则的值
     */
    public static <T> boolean contains(final Collection<T> collection, final Predicate<? super T> containFunc) {
        if (isEmpty(collection)) {
            return false;
        }
        for (final T t : collection) {
            if (containFunc.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 其中一个集合在另一个集合中是否至少包含一个元素，即是两个集合是否至少有一个共同的元素
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 两个集合是否至少有一个共同的元素
     * @see #intersection
     */
    public static boolean containsAny(final Collection<?> coll1, final Collection<?> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return false;
        }
        final boolean isFirstSmaller = coll1.size() <= coll2.size();
        // 用元素较少的集合来遍历
        final Collection<?> smallerColl = isFirstSmaller ? coll1 : coll2;
        // 用元素较多的集合构造Set, 用于快速判断是否有相同元素
        final Set<?> biggerSet = isFirstSmaller ? new HashSet<>(coll2) : new HashSet<>(coll1);
        for (final Object object : smallerColl) {
            if (biggerSet.contains(object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 集合1中是否包含集合2中所有的元素。 当集合1和集合2都为空时，返回{@code true} 当集合2为空时，返回{@code true}
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 集合1中是否包含集合2中所有的元素
     */
    public static boolean containsAll(final Collection<?> coll1, final Collection<?> coll2) {
        if (isEmpty(coll1)) {
            return isEmpty(coll2);
        }

        if (isEmpty(coll2)) {
            return true;
        }

        // Set直接判定
        if (coll1 instanceof Set) {
            return coll1.containsAll(coll2);
        }

        // 参考Apache commons collection4
        // 将时间复杂度降低到O(n + m)
        final Iterator<?> it = coll1.iterator();
        final Set<Object> elementsAlreadySeen = new HashSet<>(coll1.size(), 1);
        for (final Object nextElement : coll2) {
            if (elementsAlreadySeen.contains(nextElement)) {
                continue;
            }

            boolean foundCurrentElement = false;
            while (it.hasNext()) {
                final Object p = it.next();
                elementsAlreadySeen.add(p);
                if (Objects.equals(nextElement, p)) {
                    foundCurrentElement = true;
                    break;
                }
            }

            if (!foundCurrentElement) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据集合返回一个元素计数的 {@link Map} 所谓元素计数就是假如这个集合中某个元素出现了n次，那将这个元素做为key，n做为value 例如：[a,b,c,c,c] 得到： a: 1 b: 1 c: 3
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link Map}
     * @see IteratorKit#countMap(Iterator)
     */
    public static <T> Map<T, Integer> countMap(final Iterable<T> collection) {
        return IteratorKit.countMap(IteratorKit.getIter(collection));
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串
     *
     * @param <T>         集合元素类型
     * @param iterable    {@link Iterable}
     * @param conjunction 分隔符
     * @param func        集合元素转换器，将元素转换为字符串
     * @return 连接后的字符串
     * @see IteratorKit#join(Iterator, CharSequence, Function)
     */
    public static <T> String join(final Iterable<T> iterable, final CharSequence conjunction,
            final Function<T, ? extends CharSequence> func) {
        return IteratorKit.join(IteratorKit.getIter(iterable), conjunction, func);
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串 如果集合元素为数组、{@link Iterable}或{@link Iterator}，则递归组合其为字符串
     *
     * @param <T>         集合元素类型
     * @param iterable    {@link Iterable}
     * @param conjunction 分隔符
     * @return 连接后的字符串
     * @see IteratorKit#join(Iterator, CharSequence)
     */
    public static <T> String join(final Iterable<T> iterable, final CharSequence conjunction) {
        if (null == iterable) {
            return null;
        }
        return IteratorKit.join(iterable.iterator(), conjunction);
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串
     *
     * @param <T>         集合元素类型
     * @param iterable    {@link Iterable}
     * @param conjunction 分隔符
     * @param prefix      每个元素添加的前缀，null表示不添加
     * @param suffix      每个元素添加的后缀，null表示不添加
     * @return 连接后的字符串
     */
    public static <T> String join(final Iterable<T> iterable, final CharSequence conjunction, final String prefix,
            final String suffix) {
        if (null == iterable) {
            return null;
        }
        return IteratorKit.join(iterable.iterator(), conjunction, prefix, suffix);
    }

    /**
     * 切取部分数据 切取后的栈将减少这些元素
     *
     * @param <T>             集合元素类型
     * @param surplusAlaDatas 原数据
     * @param partSize        每部分数据的长度
     * @return 切取出的数据或null
     */
    public static <T> List<T> popPart(final Stack<T> surplusAlaDatas, final int partSize) {
        if (isEmpty(surplusAlaDatas)) {
            return ListKit.empty();
        }

        final int size = surplusAlaDatas.size();
        // 需要切割的数量
        final int popSize = Math.min(partSize, size);
        final List<T> resultList = new ArrayList<>(popSize);
        // 切割
        for (int i = 0; i < popSize; i++) {
            resultList.add(surplusAlaDatas.pop());
        }
        return resultList;
    }

    /**
     * 切取部分数据 切取后的栈将减少这些元素
     *
     * @param <T>             集合元素类型
     * @param surplusAlaDatas 原数据
     * @param partSize        每部分数据的长度
     * @return 切取出的数据或null
     */
    public static <T> List<T> popPart(final Deque<T> surplusAlaDatas, final int partSize) {
        if (isEmpty(surplusAlaDatas)) {
            return ListKit.empty();
        }

        final int size = surplusAlaDatas.size();
        // 需要切割的数量
        final int popSize = Math.min(partSize, size);
        final List<T> resultList = new ArrayList<>(popSize);
        // 切割
        for (int i = 0; i < popSize; i++) {
            resultList.add(surplusAlaDatas.pop());
        }
        return resultList;
    }

    /**
     * 新建{@link BlockingQueue} 在队列为空时，获取元素的线程会等待队列变为非空。当队列满时，存储元素的线程会等待队列可用。
     *
     * @param <T>      集合类型
     * @param capacity 容量
     * @param isLinked 是否为链表形式
     * @return {@link BlockingQueue}
     */
    public static <T> BlockingQueue<T> newBlockingQueue(final int capacity, final boolean isLinked) {
        final BlockingQueue<T> queue;
        if (isLinked) {
            queue = new LinkedBlockingDeque<>(capacity);
        } else {
            queue = new ArrayBlockingQueue<>(capacity);
        }
        return queue;
    }

    /**
     * 根据给定的集合类型，返回对应的空集合，支持类型包括：
     * 
     * <pre>
     *     1. NavigableSet
     *     2. SortedSet
     *     3. Set
     *     4. List
     * </pre>
     *
     * @param <E>             元素类型
     * @param <T>             集合类型
     * @param collectionClass 集合类型
     * @return 空集合
     */
    public static <E, T extends Collection<E>> T empty(final Class<?> collectionClass) {
        if (null == collectionClass) {
            return (T) Collections.emptyList();
        }

        if (Set.class.isAssignableFrom(collectionClass)) {
            if (NavigableSet.class == collectionClass) {
                return (T) Collections.emptyNavigableSet();
            } else if (SortedSet.class == collectionClass) {
                return (T) Collections.emptySortedSet();
            } else {
                return (T) Collections.emptySet();
            }
        } else if (List.class.isAssignableFrom(collectionClass)) {
            return (T) Collections.emptyList();
        }

        // 不支持空集合的集合类型
        throw new IllegalArgumentException(StringKit.format("[{}] is not support to get empty!", collectionClass));
    }

    /**
     * 创建新的集合对象，返回具体的泛型集合
     *
     * @param <T>            集合元素类型，rawtype 如 ArrayList.class, EnumMap.class ...
     * @param collectionType 集合类型
     * @return 集合类型对应的实例
     */
    public static <T> Collection<T> create(final Class<?> collectionType) {
        final Collection<T> list;
        if (collectionType.isAssignableFrom(AbstractCollection.class)) {
            // 抽象集合默认使用ArrayList
            list = new ArrayList<>();
        }

        // Set
        else if (collectionType.isAssignableFrom(HashSet.class)) {
            list = new HashSet<>();
        } else if (collectionType.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet<>();
        } else if (collectionType.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet<>((o1, o2) -> {
                // 优先按照对象本身比较，如果没有实现比较接口，默认按照toString内容比较
                if (o1 instanceof Comparable) {
                    return ((Comparable<T>) o1).compareTo(o2);
                }
                return CompareKit.compare(o1.toString(), o2.toString());
            });
        }

        // List
        else if (collectionType.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList<>();
        } else if (collectionType.isAssignableFrom(LinkedList.class)) {
            list = new LinkedList<>();
        }

        // Others，直接实例化
        else {
            try {
                list = (Collection<T>) ReflectKit.newInstance(collectionType);
            } catch (final Exception e) {
                // 无法创建当前类型的对象，尝试创建父类型对象
                final Class<?> superclass = collectionType.getSuperclass();
                if (null != superclass && collectionType != superclass) {
                    return create(superclass);
                }
                throw ExceptionKit.wrapRuntime(e);
            }
        }
        return list;
    }

    /**
     * 创建新的集合对象，返回具体的泛型集合
     *
     * @param <T>            集合元素类型，rawtype 如 ArrayList.class, EnumMap.class ...
     * @param collectionType 集合类型
     * @param elementType    集合元素类，只用于EnumSet创建，如果创建EnumSet，则此参数必须非空
     * @return 集合类型对应的实例
     */
    public static <T> Collection<T> create(final Class<?> collectionType, final Class<T> elementType) {
        if (EnumSet.class.isAssignableFrom(collectionType)) {
            return (Collection<T>) EnumSet.noneOf((Class<Enum>) Assert.notNull(elementType));
        }
        return create(collectionType);
    }

    /**
     * 截取列表的部分
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置（包含）
     * @param end   结束位置（不包含）
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     * @see ListKit#sub(List, int, int)
     */
    public static <T> List<T> sub(final List<T> list, final int start, final int end) {
        return ListKit.sub(list, start, end);
    }

    /**
     * 截取列表的部分
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置（包含）
     * @param end   结束位置（不包含）
     * @param step  步进
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     * @see ListKit#sub(List, int, int, int)
     */
    public static <T> List<T> sub(final List<T> list, final int start, final int end, final int step) {
        return ListKit.sub(list, start, end, step);
    }

    /**
     * 截取集合的部分
     *
     * @param <T>        集合元素类型
     * @param collection 被截取的数组
     * @param start      开始位置（包含）
     * @param end        结束位置（不包含）
     * @return 截取后的数组，当开始位置超过最大时，返回null
     */
    public static <T> List<T> sub(final Collection<T> collection, final int start, final int end) {
        return sub(collection, start, end, 1);
    }

    /**
     * 截取集合的部分
     *
     * @param <T>        集合元素类型
     * @param collection 被截取的数组
     * @param start      开始位置（包含）
     * @param end        结束位置（不包含）
     * @param step       步进
     * @return 截取后的数组，当开始位置超过最大时，返回空集合
     */
    public static <T> List<T> sub(final Collection<T> collection, final int start, final int end, final int step) {
        if (isEmpty(collection)) {
            return ListKit.empty();
        }

        final List<T> list = collection instanceof List ? (List<T>) collection : ListKit.of(collection);
        return sub(list, start, end, step);
    }

    /**
     * 对集合按照指定长度分段，每一个段为单独的集合，返回这个集合的列表
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param size       每个段的长度
     * @return 分段列表
     */
    public static <T> List<List<T>> partition(final Collection<T> collection, final int size) {
        final List<List<T>> result = new ArrayList<>();
        if (CollKit.isEmpty(collection)) {
            return result;
        }

        final int initSize = Math.min(collection.size(), size);
        ArrayList<T> subList = new ArrayList<>(initSize);
        for (final T t : collection) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new ArrayList<>(initSize);
            }
            subList.add(t);
        }
        result.add(subList);
        return result;
    }

    /**
     * 编辑，此方法产生一个新集合 编辑过程通过传入的Editor实现来返回需要的元素内容，这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，如果返回null表示这个元素对象抛弃
     * 2、修改元素对象，返回集合中为修改后的对象
     * </pre>
     *
     * @param <T>        集合类型
     * @param <E>        集合元素类型
     * @param collection 集合
     * @param editor     编辑器接口，{@code null}返回原集合
     * @return 过滤后的集合
     */
    public static <T extends Collection<E>, E> T edit(final T collection, final UnaryOperator<E> editor) {
        if (null == collection || null == editor) {
            return collection;
        }

        final T collection2 = (T) create(collection.getClass());
        if (isEmpty(collection)) {
            return collection2;
        }

        E modified;
        for (final E t : collection) {
            modified = editor.apply(t);
            if (null != modified) {
                collection2.add(modified);
            }
        }
        return collection2;
    }

    /**
     * 过滤 过滤过程通过传入的{@link Predicate}实现来过滤返回需要的元素内容，可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，{@link Predicate#test(Object)}方法返回true的对象将被加入结果集合中
     * </pre>
     *
     * @param <T>        集合类型
     * @param <E>        集合元素类型
     * @param collection 集合
     * @param predicate  过滤器，{@code null}返回原集合
     * @return 过滤后的数组
     */
    public static <T extends Collection<E>, E> T filter(final T collection, final Predicate<E> predicate) {
        if (null == collection || null == predicate) {
            return collection;
        }
        return edit(collection, t -> predicate.test(t) ? t : null);
    }

    /**
     * 去掉集合中的多个元素，此方法直接修改原集合
     *
     * @param <T>         集合类型
     * @param <E>         集合元素类型
     * @param collection  集合
     * @param elesRemoved 需要删除的元素
     * @return 原集合
     */
    public static <T extends Collection<E>, E> T removeAny(final T collection, final E... elesRemoved) {
        collection.removeAll(SetKit.of(elesRemoved));
        return collection;
    }

    /**
     * 移除集合中满足条件的所有元素，此方法在原集合上直接修改 通过实现{@link Predicate}接口，完成元素的移除，可以实现以下功能：
     *
     * <pre>
     * 1、移除指定对象，{@link Predicate#test(Object)}方法返回{@code
     * true
     * }的对象将被使用{@link Iterator#remove()}方法移除。
     * </pre>
     *
     * @param <T>       集合类型
     * @param <E>       集合元素类型
     * @param iter      集合
     * @param predicate 过滤器接口
     * @return 编辑后的集合
     */
    public static <T extends Iterable<E>, E> T remove(final T iter, final Predicate<E> predicate) {
        if (null == iter) {
            return null;
        }
        IteratorKit.remove(iter.iterator(), predicate);
        return iter;
    }

    /**
     * 去除{@code null} 元素，此方法直接修改原集合
     *
     * @param <T>        集合类型
     * @param <E>        集合元素类型
     * @param collection 集合
     * @return 处理后的集合
     */
    public static <T extends Collection<E>, E> T removeNull(final T collection) {
        return remove(collection, Objects::isNull);
    }

    /**
     * 去除{@code null}或者"" 元素，此方法直接修改原集合
     *
     * @param <T>        集合类型
     * @param <E>        集合元素类型
     * @param collection 集合
     * @return 处理后的集合
     */
    public static <T extends Collection<E>, E extends CharSequence> T removeEmpty(final T collection) {
        return remove(collection, StringKit::isEmpty);
    }

    /**
     * 去除{@code null}或者""或者空白字符串 元素，此方法直接修改原集合
     *
     * @param <T>        集合类型
     * @param <E>        集合元素类型
     * @param collection 集合
     * @return 处理后的集合
     */
    public static <T extends Collection<E>, E extends CharSequence> T removeBlank(final T collection) {
        return remove(collection, StringKit::isBlank);
    }

    /**
     * 移除集合中的多个元素，并将结果存放到指定的集合 此方法直接修改原集合
     *
     * @param <T>              集合类型
     * @param <E>              集合元素类型
     * @param resultCollection 存放移除结果的集合
     * @param targetCollection 被操作移除元素的集合
     * @param predicate        用于是否移除判断的过滤器
     * @return 移除结果的集合
     */
    public static <T extends Collection<E>, E> T removeWithAddIf(final T targetCollection, final T resultCollection,
            final Predicate<? super E> predicate) {
        Objects.requireNonNull(predicate);
        final Iterator<E> each = targetCollection.iterator();
        while (each.hasNext()) {
            final E next = each.next();
            if (predicate.test(next)) {
                resultCollection.add(next);
                each.remove();
            }
        }
        return resultCollection;
    }

    /**
     * 移除集合中的多个元素，并将结果存放到生成的新集合中后返回 此方法直接修改原集合
     *
     * @param <T>              集合类型
     * @param <E>              集合元素类型
     * @param targetCollection 被操作移除元素的集合
     * @param predicate        用于是否移除判断的过滤器
     * @return 移除结果的集合
     */
    public static <T extends Collection<E>, E> List<E> removeWithAddIf(final T targetCollection,
            final Predicate<? super E> predicate) {
        final List<E> removed = new ArrayList<>();
        removeWithAddIf(targetCollection, removed, predicate);
        return removed;
    }

    /**
     * 通过func自定义一个规则，此规则将原集合中的元素转换成新的元素，生成新的列表返回 例如提供的是一个Bean列表，通过Function接口实现获取某个字段值，返回这个字段值组成的新列表
     * 默认忽略映射后{@code null}的情况
     *
     * @param <T>        集合元素类型
     * @param <R>        返回集合元素类型
     * @param collection 原集合
     * @param func       编辑函数
     * @return 抽取后的新列表
     */
    public static <T, R> List<R> map(final Iterable<T> collection, final Function<? super T, ? extends R> func) {
        return map(collection, func, true);
    }

    /**
     * 通过func自定义一个规则，此规则将原集合中的元素转换成新的元素，生成新的列表返回 例如提供的是一个Bean列表，通过Function接口实现获取某个字段值，返回这个字段值组成的新列表
     *
     * @param <T>        集合元素类型
     * @param <R>        返回集合元素类型
     * @param collection 原集合
     * @param mapper     编辑函数
     * @param ignoreNull 是否忽略空值，这里的空值包括函数处理前和处理后的null值
     * @return 抽取后的新列表
     * @see java.util.stream.Stream#map(Function)
     */
    public static <T, R> List<R> map(final Iterable<T> collection, final Function<? super T, ? extends R> mapper,
            final boolean ignoreNull) {
        if (ignoreNull) {
            return StreamKit.of(collection)
                    // 检查映射前的结果
                    .filter(Objects::nonNull).map(mapper)
                    // 检查映射后的结果
                    .filter(Objects::nonNull).collect(Collectors.toList());
        }
        return StreamKit.of(collection).map(mapper).collect(Collectors.toList());
    }

    /**
     * 获取给定Bean列表中指定字段名对应字段值的列表 列表元素支持Bean与Map
     *
     * @param collection Bean集合或Map集合
     * @param fieldName  字段名或map的键
     * @return 字段值列表
     */
    public static Collection<Object> getFieldValues(final Iterable<?> collection, final String fieldName) {
        return getFieldValues(collection, fieldName, false);
    }

    /**
     * 获取给定Bean列表中指定字段名对应字段值的列表 列表元素支持Bean与Map
     *
     * @param collection Bean集合或Map集合
     * @param fieldName  字段名或map的键
     * @param ignoreNull 是否忽略值为{@code null}的字段
     * @return 字段值列表
     */
    public static List<Object> getFieldValues(final Iterable<?> collection, final String fieldName,
            final boolean ignoreNull) {
        return map(collection, bean -> {
            if (bean instanceof Map) {
                return ((Map<?, ?>) bean).get(fieldName);
            } else {
                return FieldKit.getFieldValue(bean, fieldName);
            }
        }, ignoreNull);
    }

    /**
     * 获取给定Bean列表中指定字段名对应字段值的列表 列表元素支持Bean与Map
     *
     * @param <T>         元素类型
     * @param collection  Bean集合或Map集合
     * @param fieldName   字段名或map的键
     * @param elementType 元素类型类
     * @return 字段值列表
     */
    public static <T> List<T> getFieldValues(final Iterable<?> collection, final String fieldName,
            final Class<T> elementType) {
        final Collection<Object> fieldValues = getFieldValues(collection, fieldName);
        return Convert.toList(elementType, fieldValues);
    }

    /**
     * 字段值与列表值对应的Map，常用于元素对象中有唯一ID时需要按照这个ID查找对象的情况 例如：车牌号 = 车
     *
     * @param <K>       字段名对应值得类型，不确定请使用Object
     * @param <V>       对象类型
     * @param iterable  对象列表
     * @param fieldName 字段名（会通过反射获取其值）
     * @return 某个字段值与对象对应Map
     */
    public static <K, V> Map<K, V> fieldValueMap(final Iterable<V> iterable, final String fieldName) {
        return IteratorKit.fieldValueMap(IteratorKit.getIter(iterable), fieldName);
    }

    /**
     * 两个字段值组成新的Map
     *
     * @param <K>               字段名对应值得类型，不确定请使用Object
     * @param <V>               值类型，不确定使用Object
     * @param iterable          对象列表
     * @param fieldNameForKey   做为键的字段名（会通过反射获取其值）
     * @param fieldNameForValue 做为值的字段名（会通过反射获取其值）
     * @return 某个字段值与对象对应Map
     */
    public static <K, V> Map<K, V> fieldValueAsMap(final Iterable<?> iterable, final String fieldNameForKey,
            final String fieldNameForValue) {
        return IteratorKit.fieldValueAsMap(IteratorKit.getIter(iterable), fieldNameForKey, fieldNameForValue);
    }

    /**
     * 获取集合的第一个元素，如果集合为空（null或者空集合），返回{@code null}
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @return 第一个元素，为空返回{@code null}
     */
    public static <T> T getFirst(final Iterable<T> iterable) {
        if (iterable instanceof List) {
            final List<T> list = (List<T>) iterable;
            return CollKit.isEmpty(list) ? null : list.get(0);
        }
        return IteratorKit.getFirst(IteratorKit.getIter(iterable));
    }

    /**
     * 获取集合的第一个非空元素
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @return 第一个元素
     */
    public static <T> T getFirstNoneNull(final Iterable<T> iterable) {
        return IteratorKit.getFirstNoneNull(IteratorKit.getIter(iterable));
    }

    /**
     * 查找第一个匹配元素对象
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param predicate  过滤器，满足过滤条件的第一个元素将被返回
     * @return 满足过滤条件的第一个元素
     */
    public static <T> T getFirst(final Iterable<T> collection, final Predicate<T> predicate) {
        return IteratorKit.getFirst(IteratorKit.getIter(collection), predicate);
    }

    /**
     * 查找第一个匹配元素对象 如果集合元素是Map，则比对键和值是否相同，相同则返回 如果为普通Bean，则通过反射比对元素字段名对应的字段值是否相同，相同则返回 如果给定字段值参数是{@code null}
     * 且元素对象中的字段值也为{@code null}则认为相同
     *
     * @param <T>        集合元素类型
     * @param collection 集合，集合元素可以是Bean或者Map
     * @param fieldName  集合元素对象的字段名或map的键
     * @param fieldValue 集合元素对象的字段值或map的值
     * @return 满足条件的第一个元素
     */
    public static <T> T getFirstByField(final Iterable<T> collection, final String fieldName, final Object fieldValue) {
        return getFirst(collection, t -> {
            if (t instanceof Map) {
                final Map<?, ?> map = (Map<?, ?>) t;
                final Object value = map.get(fieldName);
                return ObjectKit.equals(value, fieldValue);
            }

            // 普通Bean
            final Object value = FieldKit.getFieldValue(t, fieldName);
            return ObjectKit.equals(value, fieldValue);
        });
    }

    /**
     * 集合中匹配规则的数量
     *
     * @param <T>       集合元素类型
     * @param iterable  {@link Iterable}
     * @param predicate 匹配器，为空则全部匹配
     * @return 匹配数量
     */
    public static <T> int count(final Iterable<T> iterable, final Predicate<T> predicate) {
        int count = 0;
        if (null != iterable) {
            for (final T t : iterable) {
                if (null == predicate || predicate.test(t)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 获取匹配规则定义中匹配到元素的第一个位置 此方法对于某些无序集合的位置信息，以转换为数组后的位置为准。
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param predicate  匹配器，为空则全部匹配
     * @return 第一个位置
     */
    public static <T> int indexOf(final Collection<T> collection, final Predicate<T> predicate) {
        if (isNotEmpty(collection)) {
            int index = 0;
            for (final T t : collection) {
                if (null == predicate || predicate.test(t)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    /**
     * 获取匹配规则定义中匹配到元素的最后位置 此方法对于某些无序集合的位置信息，以转换为数组后的位置为准。
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param predicate  匹配器，为空则全部匹配
     * @return 最后一个位置
     */
    public static <T> int lastIndexOf(final Collection<T> collection, final Predicate<? super T> predicate) {
        if (collection instanceof List) {
            // List的查找最后一个有优化算法
            return ListKit.lastIndexOf((List<T>) collection, predicate);
        }
        int matchIndex = -1;
        if (isNotEmpty(collection)) {
            int index = 0;
            for (final T t : collection) {
                if (null == predicate || predicate.test(t)) {
                    matchIndex = index;
                }
                index++;
            }
        }
        return matchIndex;
    }

    /**
     * 获取匹配规则定义中匹配到元素的所有位置 此方法对于某些无序集合的位置信息，以转换为数组后的位置为准。
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param predicate  匹配器，为空则全部匹配
     * @return 位置数组
     */
    public static <T> int[] indexOfAll(final Collection<T> collection, final Predicate<T> predicate) {
        return Convert.convert(int[].class, indexListOfAll(collection, predicate));
    }

    /**
     * 获取匹配规则定义中匹配到元素的所有位置 此方法对于某些无序集合的位置信息，以转换为数组后的位置为准。
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param predicate  匹配器，为空则全部匹配
     * @return 位置数组
     */
    public static <T> List<Integer> indexListOfAll(final Collection<T> collection, final Predicate<T> predicate) {
        final List<Integer> indexList = new ArrayList<>();
        if (null != collection) {
            int index = 0;
            for (final T t : collection) {
                if (null == predicate || predicate.test(t)) {
                    indexList.add(index);
                }
                index++;
            }
        }

        return indexList;
    }

    /**
     * 映射键值（参考Python的zip()函数） 例如： keys = a,b,c,d values = 1,2,3,4 delimiter = , 则得到的Map是 {a=1, b=2, c=3, d=4}
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param keys      键列表
     * @param values    值列表
     * @param delimiter 分隔符
     * @param isOrder   是否有序
     * @return Map
     */
    public static Map<String, String> zip(final String keys, final String values, final String delimiter,
            final boolean isOrder) {
        return ArrayKit.zip(CharsBacker.splitToArray(keys, delimiter), CharsBacker.splitToArray(values, delimiter),
                isOrder);
    }

    /**
     * 映射键值（参考Python的zip()函数），返回Map无序 例如： keys = a,b,c,d values = 1,2,3,4 delimiter = , 则得到的Map是 {a=1, b=2, c=3, d=4}
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param keys      键列表
     * @param values    值列表
     * @param delimiter 分隔符
     * @return Map
     */
    public static Map<String, String> zip(final String keys, final String values, final String delimiter) {
        return zip(keys, values, delimiter, false);
    }

    /**
     * 映射键值（参考Python的zip()函数） 例如： keys = [a,b,c,d] values = [1,2,3,4] 则得到的Map是 {a=1, b=2, c=3, d=4} 如果两个数组长度不同，则只对应最短部分
     *
     * @param <K>    键类型
     * @param <V>    值类型
     * @param keys   键列表
     * @param values 值列表
     * @return Map
     */
    public static <K, V> Map<K, V> zip(final Collection<K> keys, final Collection<V> values) {
        if (isEmpty(keys) || isEmpty(values)) {
            return MapKit.empty();
        }

        int entryCount = Math.min(keys.size(), values.size());
        final Map<K, V> map = MapKit.newHashMap(entryCount);

        final Iterator<K> keyIterator = keys.iterator();
        final Iterator<V> valueIterator = values.iterator();
        while (entryCount > 0) {
            map.put(keyIterator.next(), valueIterator.next());
            entryCount--;
        }

        return map;
    }

    /**
     * 将集合转换为排序后的TreeSet
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param comparator 比较器
     * @return treeSet
     */
    public static <T> TreeSet<T> toTreeSet(final Collection<T> collection, final Comparator<T> comparator) {
        final TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        return treeSet;
    }

    /**
     * Iterator转换为Enumeration Adapt the specified {@link Iterator} to the {@link Enumeration} interface.
     *
     * @param <E>  集合元素类型
     * @param iter {@link Iterator}
     * @return {@link Enumeration}
     */
    public static <E> Enumeration<E> asEnumeration(final Iterator<E> iter) {
        return new IteratorEnumeration<>(Objects.requireNonNull(iter));
    }

    /**
     * {@link Iterable}转为{@link Collection} 首先尝试强转，强转失败则构建一个新的{@link ArrayList}
     *
     * @param <E>      集合元素类型
     * @param iterable {@link Iterable}
     * @return {@link Collection} 或者 {@link ArrayList}
     */
    public static <E> Collection<E> toCollection(final Iterable<E> iterable) {
        return (iterable instanceof Collection) ? (Collection<E>) iterable : ListKit.of(IteratorKit.getIter(iterable));
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
     * 
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
     * @see MapKit#toListMap(Iterable)
     */
    public static <K, V> Map<K, List<V>> toListMap(final Iterable<? extends Map<K, V>> mapList) {
        return MapKit.toListMap(mapList);
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
     * 
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
     * @see MapKit#toMapList(Map)
     */
    public static <K, V> List<Map<K, V>> toMapList(final Map<K, ? extends Iterable<V>> listMap) {
        return MapKit.toMapList(listMap);
    }

    /**
     * 将指定对象全部加入到集合中 提供的对象如果为集合类型，会自动转换为目标元素类型
     *
     * @param <T>        元素类型
     * @param collection 被加入的集合
     * @param value      对象，可能为Iterator、Iterable、Enumeration、Array
     * @return 被加入集合
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Object value) {
        return addAll(collection, value, TypeKit.getTypeArgument(collection.getClass()));
    }

    /**
     * 将指定对象全部加入到集合中 提供的对象如果为集合类型，会自动转换为目标元素类型 如果为String，支持类似于[1,2,3,4] 或者 1,2,3,4 这种格式
     *
     * @param <T>         元素类型
     * @param collection  被加入的集合
     * @param value       对象，可能为Iterator、Iterable、Enumeration、Array，或者与集合元素类型一致
     * @param elementType 元素类型，为空时，使用Object类型来接纳所有类型
     * @return 被加入集合
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Object value, Type elementType) {
        return addAll(collection, value, elementType, null);
    }

    /**
     * 将指定对象全部加入到集合中 提供的对象如果为集合类型，会自动转换为目标元素类型 如果为String，支持类似于[1,2,3,4] 或者 1,2,3,4 这种格式
     *
     * @param <T>         元素类型
     * @param collection  被加入的集合
     * @param value       对象，可能为Iterator、Iterable、Enumeration、Array，或者与集合元素类型一致
     * @param elementType 元素类型，为空时，使用Object类型来接纳所有类型
     * @param converter   自定义元素类型转换器，{@code null}表示使用默认转换器
     * @return 被加入集合
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Object value, Type elementType,
            final Converter converter) {
        if (null == collection || null == value) {
            return collection;
        }
        if (TypeKit.isUnknown(elementType)) {
            // 元素类型为空时，使用Object类型来接纳所有类型
            elementType = Object.class;
        }

        final Iterator iter;
        // 对字符串的特殊处理
        if (value instanceof CharSequence) {
            // String按照逗号分隔的列表对待
            final String arrayStr = StringKit.unWrap((CharSequence) value, '[', ']');
            iter = CharsBacker.splitTrim(arrayStr, Symbol.COMMA).iterator();
        } else if (value instanceof Map && BeanKit.isWritableBean(TypeKit.getClass(elementType))) {
            // 如果值为Map，而目标为一个Bean，则Map应整体转换为Bean，而非拆分成Entry转换
            iter = new ArrayIterator(new Object[] { value });
        } else {
            iter = IteratorKit.getIter(value);
        }

        final Converter convert = ObjectKit.defaultIfNull(converter, CompositeConverter::getInstance);
        while (iter.hasNext()) {
            collection.add((T) convert.convert(elementType, iter.next()));
        }

        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterator   要加入的{@link Iterator}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Iterator<T> iterator) {
        if (null != collection && null != iterator) {
            while (iterator.hasNext()) {
                collection.add(iterator.next());
            }
        }
        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterable   要加入的内容{@link Iterable}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Iterable<T> iterable) {
        if (iterable == null) {
            return collection;
        }
        return addAll(collection, iterable.iterator());
    }

    /**
     * 加入全部
     *
     * @param <T>         集合元素类型
     * @param collection  被加入的集合 {@link Collection}
     * @param enumeration 要加入的内容{@link Enumeration}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final Enumeration<T> enumeration) {
        if (null != collection && null != enumeration) {
            while (enumeration.hasMoreElements()) {
                collection.add(enumeration.nextElement());
            }
        }
        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param values     要加入的内容数组
     * @return 原集合
     */
    public static <T> Collection<T> addAll(final Collection<T> collection, final T[] values) {
        if (null != collection && null != values) {
            Collections.addAll(collection, values);
        }
        return collection;
    }

    /**
     * 获取集合的最后一个元素
     *
     * @param <T>        集合元素类型
     * @param collection {@link Collection}
     * @return 最后一个元素
     */
    public static <T> T getLast(final Collection<T> collection) {
        return get(collection, -1);
    }

    /**
     * 获取集合中指定下标的元素值，下标可以为负数，例如-1表示最后一个元素 如果元素越界，返回null
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param index      下标，支持负数
     * @return 元素值
     */
    public static <T> T get(final Collection<T> collection, int index) {
        if (null == collection) {
            return null;
        }

        final int size = collection.size();
        if (0 == size) {
            return null;
        }
        if (index < 0) {
            index += size;
        }

        // 检查越界
        if (index >= size || index < 0) {
            return null;
        }

        if (collection instanceof List) {
            final List<T> list = ((List<T>) collection);
            return list.get(index);
        } else {
            return IteratorKit.get(collection.iterator(), index);
        }
    }

    /**
     * 获取集合中指定多个下标的元素值，下标可以为负数，例如-1表示最后一个元素
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param indexes    下标，支持负数
     * @return 元素值列表
     */
    public static <T> List<T> getAny(final Collection<T> collection, final int... indexes) {
        if (isEmpty(collection) || ArrayKit.isEmpty(indexes)) {
            return ListKit.zero();
        }
        final int size = collection.size();
        final List<T> result = new ArrayList<>(indexes.length);
        if (collection instanceof List) {
            final List<T> list = ((List<T>) collection);
            for (int index : indexes) {
                if (index < 0) {
                    index += size;
                }
                result.add(list.get(index));
            }
        } else {
            final Object[] array = collection.toArray();
            for (int index : indexes) {
                if (index < 0) {
                    index += size;
                }
                result.add((T) array[index]);
            }
        }
        return result;
    }

    /**
     * 排序集合，排序不会修改原集合
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param comparator 比较器
     * @return treeSet
     */
    public static <T> List<T> sort(final Collection<T> collection, final Comparator<? super T> comparator) {
        final List<T> list = new ArrayList<>(collection);
        list.sort(comparator);
        return list;
    }

    /**
     * 针对List排序，排序会修改原List
     *
     * @param <T>  元素类型
     * @param list 被排序的List
     * @param c    {@link Comparator}
     * @return 原list
     * @see Collections#sort(List, Comparator)
     */
    public static <T> List<T> sort(final List<T> list, final Comparator<? super T> c) {
        return ListKit.sort(list, c);
    }

    /**
     * 根据Bean的属性排序
     *
     * @param <T>        元素类型
     * @param collection 集合，会被转换为List
     * @param property   属性名
     * @return 排序后的List
     */
    public static <T> List<T> sortByProperty(final Collection<T> collection, final String property) {
        return sort(collection, new PropertyCompare<>(property));
    }

    /**
     * 根据Bean的属性排序
     *
     * @param <T>      元素类型
     * @param list     List
     * @param property 属性名
     * @return 排序后的List
     */
    public static <T> List<T> sortByProperty(final List<T> list, final String property) {
        return ListKit.sortByProperty(list, property);
    }

    /**
     * 根据汉字的拼音顺序排序
     *
     * @param collection 集合，会被转换为List
     * @return 排序后的List
     */
    public static List<String> sortByPinyin(final Collection<String> collection) {
        return sort(collection, new PinyinCompare());
    }

    /**
     * 根据汉字的拼音顺序排序
     *
     * @param list List
     * @return 排序后的List
     */
    public static List<String> sortByPinyin(final List<String> list) {
        return ListKit.sortByPinyin(list);
    }

    /**
     * 排序Map
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param map        Map
     * @param comparator Entry比较器
     * @return {@link TreeMap}
     */
    public static <K, V> TreeMap<K, V> sort(final Map<K, V> map, final Comparator<? super K> comparator) {
        final TreeMap<K, V> result = new TreeMap<>(comparator);
        result.putAll(map);
        return result;
    }

    /**
     * 通过Entry排序，可以按照键排序，也可以按照值排序，亦或者两者综合排序
     *
     * @param <K>             键类型
     * @param <V>             值类型
     * @param entryCollection Entry集合
     * @param comparator      {@link Comparator}
     * @return {@link LinkedList}
     */
    public static <K, V> LinkedHashMap<K, V> sortToMap(final Collection<Map.Entry<K, V>> entryCollection,
            final Comparator<Map.Entry<K, V>> comparator) {
        final List<Map.Entry<K, V>> list = new LinkedList<>(entryCollection);
        list.sort(comparator);

        final LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (final Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 通过Entry排序，可以按照键排序，也可以按照值排序，亦或者两者综合排序
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param map        被排序的Map
     * @param comparator {@link Comparator}
     * @return {@link LinkedList}
     */
    public static <K, V> LinkedHashMap<K, V> sortByEntry(final Map<K, V> map,
            final Comparator<Map.Entry<K, V>> comparator) {
        return sortToMap(map.entrySet(), comparator);
    }

    /**
     * 将Set排序（根据Entry的值）
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param collection 被排序的{@link Collection}
     * @return 排序后的Set
     */
    public static <K, V> List<Entry<K, V>> sortEntryToList(final Collection<Entry<K, V>> collection) {
        final List<Entry<K, V>> list = new LinkedList<>(collection);
        list.sort((o1, o2) -> {
            final V v1 = o1.getValue();
            final V v2 = o2.getValue();

            if (v1 instanceof Comparable) {
                return ((Comparable) v1).compareTo(v2);
            } else {
                return v1.toString().compareTo(v2.toString());
            }
        });
        return list;
    }

    /**
     * 循环遍历 {@link Iterable}，使用{@link BiConsumerX} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @param consumer {@link BiConsumerX} 遍历的每条数据处理器
     */
    public static <T> void forEach(final Iterable<T> iterable, final BiConsumerX<Integer, T> consumer) {
        if (iterable == null) {
            return;
        }
        forEach(iterable.iterator(), consumer);
    }

    /**
     * 循环遍历 {@link Iterator}，使用{@link BiConsumerX} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @param consumer {@link BiConsumerX} 遍历的每条数据处理器
     */
    public static <T> void forEach(final Iterator<T> iterator, final BiConsumerX<Integer, T> consumer) {
        IteratorKit.forEach(iterator, consumer);
    }

    /**
     * 循环遍历 {@link Enumeration}，使用{@link BiConsumerX} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <T>         集合元素类型
     * @param enumeration {@link Enumeration}
     * @param consumer    {@link BiConsumerX} 遍历的每条数据处理器
     */
    public static <T> void forEach(final Enumeration<T> enumeration, final BiConsumerX<Integer, T> consumer) {
        if (enumeration == null) {
            return;
        }
        int index = 0;
        while (enumeration.hasMoreElements()) {
            consumer.accept(index, enumeration.nextElement());
            index++;
        }
    }

    /**
     * 循环遍历Map，使用{@link Consumer3X} 接受遍历的每条数据，并针对每条数据做处理 和JDK8中的map.forEach不同的是，此方法支持index
     *
     * @param <K>        Key类型
     * @param <V>        Value类型
     * @param map        {@link Map}
     * @param kvConsumer {@link Consumer3X} 遍历的每条数据处理器
     */
    public static <K, V> void forEach(final Map<K, V> map, final Consumer3X<Integer, K, V> kvConsumer) {
        MapKit.forEach(map, kvConsumer);
    }

    /**
     * 分组，按照{@link Hash32}接口定义的hash算法，集合中的元素放入hash值对应的子列表中
     *
     * @param <T>        元素类型
     * @param collection 被分组的集合
     * @param hash       Hash值算法，决定元素放在第几个分组的规则
     * @return 分组后的集合
     */
    public static <T> List<List<T>> group(final Collection<T> collection, Hash32<T> hash) {
        final List<List<T>> result = new ArrayList<>();
        if (isEmpty(collection)) {
            return result;
        }
        if (null == hash) {
            // 默认hash算法，按照元素的hashCode分组
            hash = t -> (null == t) ? 0 : t.hashCode();
        }

        int index;
        List<T> subList;
        for (final T t : collection) {
            index = hash.hash32(t);
            if (result.size() - 1 < index) {
                while (result.size() - 1 < index) {
                    result.add(null);
                }
                result.set(index, ListKit.of(t));
            } else {
                subList = result.get(index);
                if (null == subList) {
                    result.set(index, ListKit.of(t));
                } else {
                    subList.add(t);
                }
            }
        }
        return result;
    }

    /**
     * 根据元素的指定字段值分组，非Bean都放在第一个分组中
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param fieldName  元素Bean中的字段名，非Bean都放在第一个分组中
     * @return 分组列表
     */
    public static <T> List<List<T>> groupByField(final Collection<T> collection, final String fieldName) {
        return groupByFunc(collection, t -> BeanKit.getProperty(t, fieldName));
    }

    /**
     * 根据元素的指定字段值分组，非Bean都放在第一个分组中 例如：{@code
     * CollKit.groupByFunc(list, TestBean::getAge)
     * }
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param getter     getter方法引用
     * @return 分组列表
     */
    public static <T> List<List<T>> groupByFunc(final Collection<T> collection, final Function<T, ?> getter) {
        return group(collection, new Hash32<T>() {
            private final List<Object> hashValList = new ArrayList<>();

            @Override
            public int hash32(final T t) {
                if (null == t || !BeanKit.isWritableBean(t.getClass())) {
                    // 非Bean放在同一子分组中
                    return 0;
                }
                final Object value = getter.apply(t);
                int hash = hashValList.indexOf(value);
                if (hash < 0) {
                    hashValList.add(value);
                    hash = hashValList.size() - 1;
                }
                return hash;
            }
        });
    }

    /**
     * 获取指定Map列表中所有的Key
     *
     * @param <K>           键类型
     * @param mapCollection Map列表
     * @return key集合
     */
    public static <K> Set<K> keySet(final Collection<Map<K, ?>> mapCollection) {
        if (isEmpty(mapCollection)) {
            return new HashSet<>();
        }
        final HashSet<K> set = new HashSet<>(mapCollection.size() * 16);
        for (final Map<K, ?> map : mapCollection) {
            set.addAll(map.keySet());
        }

        return set;
    }

    /**
     * 获取指定Map列表中所有的Value
     *
     * @param <V>           值类型
     * @param mapCollection Map列表
     * @return Value集合
     */
    public static <V> List<V> values(final Collection<Map<?, V>> mapCollection) {
        if (isEmpty(mapCollection)) {
            return ListKit.zero();
        }
        // 统计每个map的大小总和
        int size = 0;
        for (final Map<?, V> map : mapCollection) {
            size += map.size();
        }
        if (size == 0) {
            return ListKit.zero();
        }
        final List<V> values = new ArrayList<>(size);
        for (final Map<?, V> map : mapCollection) {
            values.addAll(map.values());
        }

        return values;
    }

    /**
     * 取最大值
     *
     * @param <T>  元素类型
     * @param coll 集合
     * @return 最大值
     * @see Collections#max(Collection)
     */
    public static <T extends Comparable<? super T>> T max(final Collection<T> coll) {
        return isEmpty(coll) ? null : Collections.max(coll);
    }

    /**
     * 取最小值
     *
     * @param <T>  元素类型
     * @param coll 集合
     * @return 最小值
     * @see Collections#min(Collection)
     */
    public static <T extends Comparable<? super T>> T min(final Collection<T> coll) {
        return isEmpty(coll) ? null : Collections.min(coll);
    }

    /**
     * 转为只读集合
     *
     * @param <T> 元素类型
     * @param c   集合
     * @return 只读集合
     */
    public static <T> Collection<T> view(final Collection<? extends T> c) {
        if (null == c) {
            return null;
        }
        return Collections.unmodifiableCollection(c);
    }

    /**
     * 清除一个或多个集合内的元素，每个集合调用clear()方法
     *
     * @param collections 一个或多个集合
     */
    public static void clear(final Collection<?>... collections) {
        for (final Collection<?> collection : collections) {
            if (isNotEmpty(collection)) {
                collection.clear();
            }
        }
    }

    /**
     * 填充List，以达到最小长度
     *
     * @param <T>    集合元素类型
     * @param list   列表
     * @param minLen 最小长度
     * @param padObj 填充的对象
     */
    public static <T> void padLeft(final List<T> list, final int minLen, final T padObj) {
        Objects.requireNonNull(list);
        if (list.isEmpty()) {
            padRight(list, minLen, padObj);
            return;
        }
        // 已达到最小长度, 不需要填充
        if (list.size() >= minLen) {
            return;
        }
        if (list instanceof ArrayList) {
            // 避免频繁移动元素
            list.addAll(0, Collections.nCopies(minLen - list.size(), padObj));
        } else {
            for (int i = list.size(); i < minLen; i++) {
                list.add(0, padObj);
            }
        }
    }

    /**
     * 填充List，以达到最小长度
     *
     * @param <T>    集合元素类型
     * @param list   列表
     * @param minLen 最小长度
     * @param padObj 填充的对象
     */
    public static <T> void padRight(final Collection<T> list, final int minLen, final T padObj) {
        Objects.requireNonNull(list);
        for (int i = list.size(); i < minLen; i++) {
            list.add(padObj);
        }
    }

    /**
     * 使用给定的转换函数，转换源集合为新类型的集合
     *
     * @param <F>        源元素类型
     * @param <T>        目标元素类型
     * @param collection 集合
     * @param function   转换函数
     * @return 新类型的集合
     */
    public static <F, T> Collection<T> trans(final Collection<F> collection,
            final Function<? super F, ? extends T> function) {
        return new TransCollection<>(collection, function);
    }

    /**
     * 使用给定的map将集合中的元素进行属性或者值的重新设定
     *
     * @param <E>         元素类型
     * @param <K>         替换的键
     * @param <V>         替换的值
     * @param iterable    集合
     * @param map         映射集
     * @param keyGenerate 映射键生成函数
     * @param biConsumer  封装映射到的值函数 nick_wys
     */
    public static <E, K, V> void setValueByMap(final Iterable<E> iterable, final Map<K, V> map,
            final Function<E, K> keyGenerate, final BiConsumer<E, V> biConsumer) {
        iterable.forEach(
                x -> Optional.ofNullable(map.get(keyGenerate.apply(x))).ifPresent(y -> biConsumer.accept(x, y)));
    }

    /**
     * 一个对象不为空且不存在于该集合中时，加入到该集合中
     * 
     * <pre>
     *     null, null -&gt; false
     *     [], null -&gt; false
     *     null, "123" -&gt; false
     *     ["123"], "123" -&gt; false
     *     [], "123" -&gt; true
     *     ["456"], "123" -&gt; true
     *     [Animal{"name": "jack"}], Dog{"name": "jack"} -&gt; true
     * </pre>
     *
     * @param collection 被加入的集合
     * @param object     要添加到集合的对象
     * @param <T>        集合元素类型
     * @param <S>        要添加的元素类型【为集合元素类型的类型或子类型】
     * @return 是否添加成功 Cloud-Style
     */
    public static <T, S extends T> boolean addIfAbsent(final Collection<T> collection, final S object) {
        if (object == null || collection == null || collection.contains(object)) {
            return false;
        }
        return collection.add(object);
    }

    /**
     * 是否至少有一个符合判断条件
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param predicate  自定义判断函数
     * @return 是否有一个值匹配 布尔值
     */
    public static <T> boolean anyMatch(final Collection<T> collection, final Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return Boolean.FALSE;
        }
        return collection.stream().anyMatch(predicate);
    }

    /**
     * 是否全部匹配判断条件
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param predicate  自定义判断函数
     * @return 是否全部匹配 布尔值
     */
    public static <T> boolean allMatch(final Collection<T> collection, final Predicate<T> predicate) {
        if (isEmpty(collection)) {
            return Boolean.FALSE;
        }
        return collection.stream().allMatch(predicate);
    }

    /**
     * 解构多层集合 例如：{@code List<List<List<String>>> 解构成 List<String>}
     *
     * @param <T>        元素类型
     * @param collection 需要解构的集合
     * @return 解构后的集合
     */
    public static <T> List<T> flat(final Collection<?> collection) {
        return flat(collection, true);
    }

    /**
     * 解构多层集合 例如：{@code List<List<List<String>>> 解构成 List<String>} skipNull如果为true, 则解构后的集合里不包含null值，为false则会包含null值。
     *
     * @param <T>        元素类型
     * @param collection 需要结构的集合
     * @param skipNull   是否跳过空的值
     * @return 解构后的集合
     */
    public static <T> List<T> flat(final Collection<?> collection, final boolean skipNull) {
        final LinkedList<Object> queue = new LinkedList<>(collection);

        final List<Object> result = new ArrayList<>();

        while (isNotEmpty(queue)) {
            final Object t = queue.removeFirst();

            if (skipNull && t == null) {
                continue;
            }

            if (t instanceof Collection) {
                queue.addAll((Collection<?>) t);
            } else {
                result.add(t);
            }
        }
        return (List<T>) result;
    }

    /**
     * 通过cas操作 实现对指定值内的回环累加
     *
     * @param object        集合
     *                      <ul>
     *                      <li>Collection - the collection size
     *                      <li>Map - the map size
     *                      <li>Array - the array size
     *                      <li>Iterator - the number of elements remaining in the iterator
     *                      <li>Enumeration - the number of elements remaining in the enumeration
     *                      </ul>
     * @param atomicInteger 原子操作类
     * @return 索引位置
     */
    public static int ringNextIntByObject(final Object object, final AtomicInteger atomicInteger) {
        Assert.notNull(object);
        final int modulo = size(object);
        return ringNextInt(modulo, atomicInteger);
    }

    /**
     * 通过cas操作 实现对指定值内的回环累加
     *
     * @param modulo        回环周期值
     * @param atomicInteger 原子操作类
     * @return 索引位置
     */
    public static int ringNextInt(final int modulo, final AtomicInteger atomicInteger) {
        Assert.notNull(atomicInteger);
        Assert.isTrue(modulo > 0);
        if (modulo <= 1) {
            return 0;
        }
        for (;;) {
            final int current = atomicInteger.get();
            final int next = (current + 1) % modulo;
            if (atomicInteger.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    /**
     * 通过cas操作 实现对指定值内的回环累加 此方法一般用于大量数据完成回环累加（如数据库中的值大于int最大值）
     *
     * @param modulo     回环周期值
     * @param atomicLong 原子操作类
     * @return 索引位置
     */
    public static long ringNextLong(final long modulo, final AtomicLong atomicLong) {
        Assert.notNull(atomicLong);
        Assert.isTrue(modulo > 0);
        if (modulo <= 1) {
            return 0;
        }
        for (;;) {
            final long current = atomicLong.get();
            final long next = (current + 1) % modulo;
            if (atomicLong.compareAndSet(current, next)) {
                return next;
            }
        }
    }

}
