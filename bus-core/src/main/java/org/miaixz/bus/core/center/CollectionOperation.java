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
package org.miaixz.bus.core.center;

import java.util.*;

import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.SetKit;

/**
 * 集合运算，包括：
 * <ul>
 * <li>求集合的并集</li>
 * <li>求集合的唯一并集</li>
 * <li>求集合的完全并集</li>
 * <li>求集合的交集</li>
 * <li>求集合的差集</li>
 * </ul>
 *
 * @param <E> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class CollectionOperation<E> {

    private final Collection<E>[] colls;

    /**
     * 构造
     *
     * @param colls 集合数组
     */
    public CollectionOperation(final Collection<? extends E>[] colls) {
        this.colls = (Collection<E>[]) colls;
    }

    /**
     * 创建运算对象
     *
     * @param colls 集合列表
     * @param <E>   元素类型
     * @return CollectionOperation
     */
    @SafeVarargs
    public static <E> CollectionOperation<E> of(final Collection<? extends E>... colls) {
        return new CollectionOperation<>(colls);
    }

    /**
     * 两个集合的并集 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留最多的个数 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c, c,
     * c]，此结果中只保留了三个c 结果集合与原集合顺序无关
     *
     * @param <T>   集合元素类型
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 并集的集合，返回 {@link ArrayList}
     */
    private static <T> Collection<T> _union(final Collection<T> coll1, final Collection<T> coll2) {
        if (CollKit.isEmpty(coll1)) {
            return ListKit.of(coll2);
        } else if (CollKit.isEmpty(coll2)) {
            return ListKit.of(coll1);
        }

        // 给每个元素计数
        final Map<T, Integer> map1 = CollKit.countMap(coll1);
        final Map<T, Integer> map2 = CollKit.countMap(coll2);

        // 两个集合的全部元素
        final Set<T> elements = CollectionOperation.of(map1.keySet(), map2.keySet()).unionDistinct();

        // 并集, 每个元素至少会有一个
        final List<T> list = new ArrayList<>(coll1.size() + coll2.size());
        for (final T t : elements) {
            // 每个元素 保留最多的个数
            final int amount = Math.max(map1.getOrDefault(t, 0), map2.getOrDefault(t, 0));
            for (int i = 0; i < amount; i++) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 两个集合的交集 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留最少的个数 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c,
     * c]，此结果中只保留了两个c
     *
     * @param <E>   集合元素类型
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 交集的集合，返回 {@link ArrayList}
     */
    private static <E> Collection<E> _intersection(final Collection<E> coll1, final Collection<E> coll2) {
        if (CollKit.isEmpty(coll1) || CollKit.isEmpty(coll2)) {
            return ListKit.zero();
        }
        final Map<E, Integer> map1 = CollKit.countMap(coll1);
        final Map<E, Integer> map2 = CollKit.countMap(coll2);

        final boolean isFirstSmaller = map1.size() <= map2.size();
        // 只需要遍历数量较少的集合的元素
        final Set<E> elements = SetKit.of(isFirstSmaller ? map1.keySet() : map2.keySet());
        // 交集的元素个数 最多为 较少集合的元素个数
        final List<E> list = new ArrayList<>(isFirstSmaller ? coll1.size() : coll2.size());
        for (final E t : elements) {
            final int amount = Math.min(map1.getOrDefault(t, 0), map2.getOrDefault(t, 0));
            for (int i = 0; i < amount; i++) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 两个集合的差集 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留两个集合中此元素个数差的个数 例如：
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
    private static <T> Collection<T> _disjunction(final Collection<T> coll1, final Collection<T> coll2) {
        if (CollKit.isEmpty(coll1)) {
            if (CollKit.isEmpty(coll2)) {
                return ListKit.zero();
            }
            return coll2;
        } else if (CollKit.isEmpty(coll2)) {
            return coll1;
        }

        final List<T> result = new ArrayList<>(coll1.size() + coll2.size());
        final Map<T, Integer> map1 = CollKit.countMap(coll1);
        final Map<T, Integer> map2 = CollKit.countMap(coll2);
        // 两个集合的全部元素
        final Set<T> elements = SetKit.of(map1.keySet());
        elements.addAll(map2.keySet());
        // 元素的个数为 该元素在两个集合中的个数的差
        for (final T t : elements) {
            final int amount = Math.abs(map1.getOrDefault(t, 0) - map2.getOrDefault(t, 0));
            for (int i = 0; i < amount; i++) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * 多个集合的并集 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留最多的个数 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c, c,
     * c]，此结果中只保留了三个c
     *
     * @return 并集的集合，返回 {@link ArrayList}
     */
    public Collection<E> union() {
        final Collection<E>[] colls = this.colls;
        if (ArrayKit.isEmpty(colls)) {
            return ListKit.zero();
        }

        Collection<E> result = colls[0];
        for (int i = 1; i < colls.length; i++) {
            result = _union(result, colls[i]);
        }

        return result;
    }

    /**
     * 多个集合的非重复并集，类似于SQL中的“UNION DISTINCT” 针对一个集合中存在多个相同元素的情况，只保留一个 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b,
     * c]，此结果中只保留了一个c
     *
     * @return 并集的集合，返回 {@link LinkedHashSet}
     */
    public Set<E> unionDistinct() {
        final Collection<E>[] colls = this.colls;
        int totalLength = 0;
        for (final Collection<E> set : colls) {
            if (CollKit.isNotEmpty(set)) {
                totalLength += set.size();
            }
        }
        final Set<E> result = new HashSet<>(totalLength, 1);
        for (final Collection<E> set : colls) {
            if (CollKit.isNotEmpty(set)) {
                result.addAll(set);
            }
        }
        return result;
    }

    /**
     * 多个集合的完全并集，类似于SQL中的“UNION ALL” 针对一个集合中存在多个相同元素的情况，保留全部元素 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c, c,
     * c, a, b, c, c]
     *
     * @return 并集的集合，返回 {@link ArrayList}
     */
    public List<E> unionAll() {
        final Collection<E>[] colls = this.colls;
        if (ArrayKit.isEmpty(colls)) {
            return ListKit.zero();
        }
        // 先统计所有集合的元素数量, 避免扩容
        int totalSize = 0;
        for (final Collection<E> coll : colls) {
            if (CollKit.isNotEmpty(coll)) {
                totalSize += CollKit.size(coll);
            }
        }
        if (totalSize == 0) {
            return ListKit.zero();
        }

        // 遍历并全部加入集合
        final List<E> result = new ArrayList<>(totalSize);
        for (final Collection<E> coll : colls) {
            if (CollKit.isNotEmpty(coll)) {
                result.addAll(coll);
            }
        }

        return result;
    }

    /**
     * 多个集合的交集 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留最少的个数 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c,
     * c]，此结果中只保留了两个c
     *
     * @return 交集的集合，返回 {@link ArrayList}
     */
    public Collection<E> intersection() {
        final Collection<E>[] colls = this.colls;
        if (ArrayKit.isEmpty(colls)) {
            return ListKit.zero();
        }

        Collection<E> result = colls[0];
        for (int i = 1; i < colls.length; i++) {
            result = _intersection(result, colls[i]);
        }

        return result;
    }

    /**
     * 多个集合的唯一交集 针对一个集合中存在多个相同元素的情况，只保留一个 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c] 结果：[a, b, c]，此结果中只保留了一个c
     *
     * @return 唯一交集的集合，返回 {@link LinkedHashSet}
     */
    public Set<E> intersectionDistinct() {
        final Collection<E>[] colls = this.colls;
        if (ArrayKit.isEmpty(colls)) {
            return SetKit.zeroLinked();
        }

        // 任意容器为空, 则返回空集
        for (final Collection<E> coll : colls) {
            if (CollKit.isEmpty(coll)) {
                return SetKit.zeroLinked();
            }
        }

        final Set<E> result = SetKit.of(true, colls[0]);
        for (int i = 1; i < colls.length; i++) {
            if (CollKit.isNotEmpty(colls[i])) {
                result.retainAll(colls[i]);
            }
        }

        return result;
    }

    /**
     * 多个集合的差集 针对一个集合中存在多个相同元素的情况，计算每两个集合中此元素的个数，保留两个集合中此元素个数差的个数 例如：
     *
     * <pre>
     *     disjunction([a, b, c, c, c], [a, b, c, c]) - [c]
     *     disjunction([a, b], [])                    - [a, b]
     *     disjunction([a, b, c], [b, c, d])          - [a, d]
     * </pre>
     * 
     * 任意一个集合为空，返回另一个集合 两个集合无差集则返回空集合
     *
     * @return 差集的集合，返回 {@link ArrayList}
     */
    public Collection<E> disjunction() {
        final Collection<E>[] colls = this.colls;
        if (ArrayKit.isEmpty(colls)) {
            return ListKit.zero();
        }

        Collection<E> result = colls[0];
        for (int i = 1; i < colls.length; i++) {
            result = _disjunction(result, colls[i]);
        }

        return result;
    }

    /**
     * 计算集合的单差集，即只返回【集合1】中有，但是【集合2】、【集合3】...中没有的元素，例如：
     *
     * <pre>
     *     subtract([1,2,3,4],[2,3,4,5]) - [1]
     * </pre>
     *
     * @return 单差集的List
     */
    public List<E> subtract() {
        final Collection<E>[] colls = this.colls;
        if (ArrayKit.isEmpty(colls)) {
            return ListKit.zero();
        }
        final List<E> result = ListKit.of(colls[0]);
        for (int i = 1; i < colls.length; i++) {
            result.removeAll(colls[i]);
        }
        return result;
    }

}
