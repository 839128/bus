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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.center.list.AvgPartition;
import org.miaixz.bus.core.center.list.Partition;
import org.miaixz.bus.core.center.list.RandomAccessAvgPartition;
import org.miaixz.bus.core.center.list.RandomAccessPartition;
import org.miaixz.bus.core.compare.PinyinCompare;
import org.miaixz.bus.core.compare.PropertyCompare;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Validator;

/**
 * List相关工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ListKit {

    /**
     * 新建一个{@link ArrayList} 如果提供的初始化数组为空，新建默认初始长度的List 替换之前的：CollKit.newArrayList()
     *
     * @param <T>    集合元素类型
     * @param values 数组，可以为{@code null}
     * @return List对象
     */
    @SafeVarargs
    public static <T> ArrayList<T> of(final T... values) {
        if (ArrayKit.isEmpty(values)) {
            return new ArrayList<>();
        }
        final ArrayList<T> arrayList = new ArrayList<>(values.length);
        Collections.addAll(arrayList, values);
        return arrayList;
    }

    /**
     * 新建一个{@link LinkedList} 如果提供的初始化数组为空，新建默认初始长度的List
     *
     * @param <T>    集合元素类型
     * @param values 数组，可以为{@code null}
     * @return {@link LinkedList}对象
     */
    @SafeVarargs
    public static <T> LinkedList<T> ofLinked(final T... values) {
        final LinkedList<T> list = new LinkedList<>();
        if (ArrayKit.isNotEmpty(values)) {
            Collections.addAll(list, values);
        }
        return list;
    }

    /**
     * 新建一个List 如果提供的初始化数组为空，新建默认初始长度的List
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否为链表
     * @return List对象
     */
    public static <T> List<T> of(final boolean isLinked) {
        return isLinked ? ofLinked() : of();
    }

    /**
     * 新建一个List 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param iterable {@link Iterable}
     * @return List对象
     */
    public static <T> List<T> of(final boolean isLinked, final Iterable<T> iterable) {
        if (null == iterable) {
            return of(isLinked);
        }
        if (iterable instanceof Collection) {
            final Collection<T> collection = (Collection<T>) iterable;
            return isLinked ? new LinkedList<>(collection) : new ArrayList<>(collection);
        }
        return of(isLinked, iterable.iterator());
    }

    /**
     * 新建一个List 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>        集合元素类型
     * @param isLinked   是否新建LinkedList
     * @param enumration {@link Enumeration}
     * @return ArrayList对象
     */
    public static <T> List<T> of(final boolean isLinked, final Enumeration<T> enumration) {
        return of(isLinked, (Iterator<T>) new EnumerationIterator<>(enumration));
    }

    /**
     * 新建一个List 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param iter     {@link Iterator}
     * @return ArrayList对象
     */
    public static <T> List<T> of(final boolean isLinked, final Iterator<T> iter) {
        final List<T> list = of(isLinked);
        if (null != iter) {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }
        return list;
    }

    /**
     * 新建一个ArrayList 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> of(final Iterable<T> iterable) {
        return (ArrayList<T>) of(false, iterable);
    }

    /**
     * 新建一个ArrayList 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> of(final Iterator<T> iterator) {
        return (ArrayList<T>) of(false, iterator);
    }

    /**
     * 新建一个ArrayList 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>         集合元素类型
     * @param enumeration {@link Enumeration}
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> of(final Enumeration<T> enumeration) {
        return (ArrayList<T>) of(false, enumeration);
    }

    /**
     * 数组转为一个不可变List 类似于Java9中的List.of 不同于Arrays.asList，此方法不允许修改数组
     *
     * @param ts  对象
     * @param <T> 对象类型
     * @return 不可修改List
     */
    @SafeVarargs
    public static <T> List<T> view(final T... ts) {
        return view(of(ts));
    }

    /**
     * 转为一个不可变List 类似于Java9中的List.of
     *
     * @param ts  对象
     * @param <T> 对象类型
     * @return 不可修改List，如果提供List为{@code null}或者空，返回{@link Collections#emptyList()}
     */
    public static <T> List<T> view(final List<T> ts) {
        if (ArrayKit.isEmpty(ts)) {
            return empty();
        }
        return Collections.unmodifiableList(ts);
    }

    /**
     * 获取一个空List，这个空List不可变
     *
     * @param <T> 元素类型
     * @return 空的List
     * @see Collections#emptyList()
     */
    public static <T> List<T> empty() {
        return Collections.emptyList();
    }

    /**
     * 获取一个初始大小为0的List，这个空List可变
     *
     * @param <T> 元素类型
     * @return 空的List
     */
    public static <T> List<T> zero() {
        return new ArrayList<>(0);
    }

    /**
     * 获取一个只包含一个元素的List，不可变
     *
     * @param <T>     元素类型
     * @param element 元素
     * @return 只包含一个元素的List
     */
    public static <T> List<T> singleton(final T element) {
        return Collections.singletonList(element);
    }

    /**
     * 新建一个CopyOnWriteArrayList
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link CopyOnWriteArrayList}
     */
    public static <T> CopyOnWriteArrayList<T> ofCopyOnWrite(final Collection<T> collection) {
        return (null == collection) ? (new CopyOnWriteArrayList<>()) : (new CopyOnWriteArrayList<>(collection));
    }

    /**
     * 新建一个CopyOnWriteArrayList
     *
     * @param <T> 集合元素类型
     * @param ts  集合
     * @return {@link CopyOnWriteArrayList}
     */
    @SafeVarargs
    public static <T> CopyOnWriteArrayList<T> ofCopyOnWrite(final T... ts) {
        return (null == ts) ? (new CopyOnWriteArrayList<>()) : (new CopyOnWriteArrayList<>(ts));
    }

    /**
     * 针对List自然排序，排序会修改原List
     *
     * @param <T>  元素类型
     * @param list 被排序的List
     * @return 原list
     * @see Collections#sort(List, Comparator)
     */
    public static <T> List<T> sort(final List<T> list) {
        return sort(list, null);
    }

    /**
     * 针对List排序，排序会修改原List
     *
     * @param <T>  元素类型
     * @param list 被排序的List
     * @param c    {@link Comparator}，null表示自然排序（null安全的）
     * @return 原list
     * @see Collections#sort(List, Comparator)
     */
    public static <T> List<T> sort(final List<T> list, Comparator<? super T> c) {
        if (CollKit.isEmpty(list)) {
            return list;
        }
        if (null == c) {
            c = Comparator.nullsFirst((Comparator<? super T>) Comparator.naturalOrder());
        }
        list.sort(c);
        return list;
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
        return sort(list, new PropertyCompare<>(property));
    }

    /**
     * 根据汉字的拼音顺序排序
     *
     * @param list List
     * @return 排序后的List
     */
    public static List<String> sortByPinyin(final List<String> list) {
        return sort(list, new PinyinCompare());
    }

    /**
     * 反序给定List，会在原List基础上直接修改 注意此方法不支持不可编辑的列表
     *
     * @param <T>  元素类型
     * @param list 被反转的List
     * @return 反转后的List
     * @see Collections#reverse(List)
     */
    public static <T> List<T> reverse(final List<T> list) {
        if (CollKit.isEmpty(list)) {
            return list;
        }
        Collections.reverse(list);
        return list;
    }

    /**
     * 反序给定List，会创建一个新的List，原List数据不变
     *
     * @param <T>  元素类型
     * @param list 被反转的List
     * @return 反转后的List
     */
    public static <T> List<T> reverseNew(final List<T> list) {
        List<T> list2 = ObjectKit.clone(list);
        if (null == list2) {
            // 不支持clone
            list2 = new ArrayList<>(list);
        }
        try {
            return reverse(list2);
        } catch (final UnsupportedOperationException e) {
            // 提供的列表不可编辑,新建列表
            return reverse(of(list));
        }
    }

    /**
     * 设置或增加元素。当index小于List的长度时，替换指定位置的值，否则在尾部追加
     *
     * @param <T>     元素类型
     * @param list    List列表
     * @param index   位置
     * @param element 新元素
     * @return 原List
     */
    public static <T> List<T> setOrAppend(final List<T> list, final int index, final T element) {
        if (index < list.size()) {
            list.set(index, element);
        } else {
            list.add(element);
        }
        return list;
    }

    /**
     * 在指定位置设置元素。当index小于List的长度时，替换指定位置的值，否则追加{@code null}直到到达index后，设置值
     *
     * @param <T>     元素类型
     * @param list    List列表
     * @param index   位置
     * @param element 新元素
     * @return the list
     */
    public static <T> List<T> setOrPadding(final List<T> list, final int index, final T element) {
        return setOrPadding(list, index, element, null);
    }

    /**
     * 在指定位置设置元素。当index小于List的长度时，替换指定位置的值，否则追加{@code paddingElement}直到到达index后，设置值
     *
     * @param <T>            元素类型
     * @param list           List列表
     * @param index          位置，不能大于(list.size()+1) * 2
     * @param element        新元素
     * @param paddingElement 填充的值
     * @return the list
     */
    public static <T> List<T> setOrPadding(final List<T> list, final int index, final T element,
            final T paddingElement) {
        return setOrPadding(list, index, element, paddingElement, (list.size() + 1) * 10);
    }

    /**
     * 在指定位置设置元素。当index小于List的长度时，替换指定位置的值，否则追加{@code paddingElement}直到到达index后，设置值
     *
     * @param <T>            元素类型
     * @param list           List列表
     * @param index          位置
     * @param element        新元素
     * @param paddingElement 填充的值
     * @param indexLimit     最大索引限制
     * @return the list
     */
    public static <T> List<T> setOrPadding(final List<T> list, final int index, final T element, final T paddingElement,
            final int indexLimit) {
        Assert.notNull(list, "List must be not null !");
        final int size = list.size();
        if (index < size) {
            list.set(index, element);
        } else {
            if (indexLimit > 0) {
                // 增加安全检查
                Validator.checkIndexLimit(index, indexLimit);
            }
            for (int i = size; i < index; i++) {
                list.add(paddingElement);
            }
            list.add(element);
        }
        return list;
    }

    /**
     * 截取集合的部分
     *
     * @param <T>           集合元素类型
     * @param list          被截取的数组
     * @param begionInclude 开始位置（包含）
     * @param endExclude    结束位置（不包含）
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     */
    public static <T> List<T> sub(final List<T> list, final int begionInclude, final int endExclude) {
        return sub(list, begionInclude, endExclude, 1);
    }

    /**
     * 截取集合的部分 此方法与{@link List#subList(int, int)} 不同在于子列表是新的副本，操作子列表不会影响原列表。
     *
     * @param <T>           集合元素类型
     * @param list          被截取的数组
     * @param begionInclude 开始位置（包含）
     * @param endExclude    结束位置（不包含）
     * @param step          步进
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     */
    public static <T> List<T> sub(final List<T> list, int begionInclude, int endExclude, int step) {
        if (list == null) {
            return null;
        }

        if (list.isEmpty()) {
            return new ArrayList<>(0);
        }

        final int size = list.size();
        if (begionInclude < 0) {
            begionInclude += size;
        }
        if (endExclude < 0) {
            endExclude += size;
        }
        if (begionInclude == size) {
            return new ArrayList<>(0);
        }
        if (begionInclude > endExclude) {
            final int tmp = begionInclude;
            begionInclude = endExclude;
            endExclude = tmp;
        }
        if (endExclude > size) {
            if (begionInclude >= size) {
                return new ArrayList<>(0);
            }
            endExclude = size;
        }

        if (step < 1) {
            step = 1;
        }

        final List<T> result = new ArrayList<>();
        for (int i = begionInclude; i < endExclude; i += step) {
            result.add(list.get(i));
        }
        return result;
    }

    /**
     * 获取匹配规则定义中匹配到元素的最后位置 此方法对于某些无序集合的位置信息，以转换为数组后的位置为准。
     *
     * @param <T>     元素类型
     * @param list    List集合
     * @param matcher 匹配器，为空则全部匹配
     * @return 最后一个位置
     */
    public static <T> int lastIndexOf(final List<T> list, final Predicate<? super T> matcher) {
        if (null != list) {
            final int size = list.size();
            if (size > 0) {
                for (int i = size - 1; i >= 0; i--) {
                    if (null == matcher || matcher.test(list.get(i))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 通过传入分区长度，将指定列表分区为不同的块，每块区域的长度相同（最后一块可能小于长度） 分区是在原List的基础上进行的，返回的分区是不可变的抽象列表，原列表元素变更，分区中元素也会变更。
     *
     * <p>
     * 需要特别注意的是，此方法调用{@link List#subList(int, int)}切分List， 此方法返回的是原List的视图，也就是说原List有变更，切分后的结果也会变更。
     * </p>
     *
     * @param <T>  集合元素类型
     * @param list 列表，为空时返回{@link #empty()}
     * @param size 每个段的长度，当长度超过list长度时，size按照list长度计算，即只返回一个节点
     * @return 分段列表
     */
    public static <T> List<List<T>> partition(final List<T> list, final int size) {
        if (CollKit.isEmpty(list)) {
            return empty();
        }

        return (list instanceof RandomAccess) ? new RandomAccessPartition<>(list, size) : new Partition<>(list, size);
    }

    /**
     * 将集合平均分成多个list，返回这个集合的列表
     * <p>
     * 例：
     * </p>
     * 
     * <pre>
     * ListKit.avgPartition(null, 3); // []
     * ListKit.avgPartition(Arrays.asList(1, 2, 3, 4), 2); // [[1, 2], [3, 4]]
     * ListKit.avgPartition(Arrays.asList(1, 2, 3), 5); // [[1], [2], [3], [], []]
     * ListKit.avgPartition(Arrays.asList(1, 2, 3), 2); // [[1, 2], [3]]
     * </pre>
     *
     * @param <T>   集合元素类型
     * @param list  集合
     * @param limit 要均分成几个list
     * @return 分段列表
     */
    public static <T> List<List<T>> avgPartition(final List<T> list, final int limit) {
        if (CollKit.isEmpty(list)) {
            return empty();
        }

        return (list instanceof RandomAccess) ? new RandomAccessAvgPartition<>(list, limit)
                : new AvgPartition<>(list, limit);
    }

    /**
     * 将指定元素交换到指定索引位置,其他元素的索引值不变 交换会修改原List 如果集合中有多个相同元素，只交换第一个找到的元素
     *
     * @param <T>         元素类型
     * @param list        列表
     * @param element     需交换元素
     * @param targetIndex 目标索引
     */
    public static <T> void swapTo(final List<T> list, final T element, final Integer targetIndex) {
        if (CollKit.isNotEmpty(list)) {
            final int index = list.indexOf(element);
            if (index >= 0) {
                Collections.swap(list, index, targetIndex);
            }
        }
    }

    /**
     * 将指定元素交换到指定元素位置,其他元素的索引值不变 交换会修改原List 如果集合中有多个相同元素，只交换第一个找到的元素
     *
     * @param <T>           元素类型
     * @param list          列表
     * @param element       需交换元素
     * @param targetElement 目标元素
     */
    public static <T> void swapElement(final List<T> list, final T element, final T targetElement) {
        if (CollKit.isNotEmpty(list)) {
            final int targetIndex = list.indexOf(targetElement);
            if (targetIndex >= 0) {
                swapTo(list, element, targetIndex);
            }
        }
    }

    /**
     * 转为只读List
     *
     * @param <T> 元素类型
     * @param c   集合
     * @return 只读集合
     */
    public static <T> List<T> unmodifiable(final List<? extends T> c) {
        if (null == c) {
            return null;
        }
        return Collections.unmodifiableList(c);
    }

    /**
     * 将另一个列表中的元素加入到列表中，如果列表中已经存在此元素则忽略之
     *
     * @param <T>       集合元素类型
     * @param list      列表
     * @param otherList 其它列表
     * @return 此列表
     */
    public static <T> List<T> addAllIfNotContains(final List<T> list, final List<T> otherList) {
        for (final T t : otherList) {
            if (!list.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 通过删除或替换现有元素或者原地添加新的元素来修改列表，并以列表形式返回被修改的内容。此方法不会改变原列表。 类似js的<a href=
     * "https://developer.mozilla.org/zh-CN/docs/Web/JavaScript/Reference/Global_Objects/Array/splice">splice</a>函数
     *
     * @param <T>         元素类型
     * @param list        列表
     * @param start       指定修改的开始位置（从 0 计数）, 可以为负数, -1代表最后一个元素
     * @param deleteCount 删除个数，必须是正整数
     * @param items       放入的元素
     * @return 结果列表
     */
    @SafeVarargs
    public static <T> List<T> splice(final List<T> list, int start, int deleteCount, final T... items) {
        if (CollKit.isEmpty(list)) {
            return zero();
        }
        final int size = list.size();
        // 从后往前查找
        if (start < 0) {
            start += size;
        } else if (start >= size) {
            // 直接在尾部追加，不删除
            start = size;
            deleteCount = 0;
        }
        // 起始位置 加上 删除的数量 超过 数据长度，需要重新计算需要删除的数量
        if (start + deleteCount > size) {
            deleteCount = size - start;
        }

        // 新列表的长度
        final int newSize = size - deleteCount + items.length;
        List<T> resList = list;
        // 新列表的长度 大于 旧列表，创建新列表
        if (newSize > size) {
            resList = new ArrayList<>(newSize);
            resList.addAll(list);
        }
        // 需要删除的部分
        if (deleteCount > 0) {
            resList.subList(start, start + deleteCount).clear();
        }
        // 新增的部分
        if (ArrayKit.isNotEmpty(items)) {
            resList.addAll(start, Arrays.asList(items));
        }
        return resList;
    }

    /**
     * 对指定List分页取值
     *
     * @param <T>      集合元素类型
     * @param pageNo   页码,从1开始计数,0和1效果相同
     * @param pageSize 每页的条目数
     * @param list     列表
     * @return 分页后的段落内容
     */
    public static <T> List<T> page(int pageNo, int pageSize, List<T> list) {
        if (CollKit.isEmpty(list)) {
            return new ArrayList<>(0);
        }

        int resultSize = list.size();
        // 每页条目数大于总数直接返回所有
        if (resultSize <= pageSize) {
            if (pageNo <= 1) {
                return unmodifiable(list);
            } else {
                // 越界直接返回空
                return new ArrayList<>(0);
            }
        }

        if (pageNo < 1) {
            pageNo = 1;
        }

        if (pageSize < 1) {
            pageSize = 0;
        }

        int start = (pageNo - 1) * pageSize;
        int end = start + pageSize;

        final int[] startEnd = new int[] { start, end };
        if (startEnd[1] > resultSize) {
            startEnd[1] = resultSize;
            if (startEnd[0] > startEnd[1]) {
                return new ArrayList<>(0);
            }
        }
        return sub(list, startEnd[0], startEnd[1]);
    }

    /**
     * 将元素移动到指定列表的新位置。
     * <ul>
     * <li>如果元素不在列表中，则将其添加到新位置。</li>
     * <li>如果元素已在列表中，则先移除它，然后再将其添加到新位置。</li>
     * </ul>
     *
     * @param list        原始列表，元素将在这个列表上进行操作。
     * @param element     需要移动的元素。
     * @param newPosition 元素的新位置，从0开始计数，位置计算是以移除元素后的列表位置计算的
     * @param <T>         列表和元素的通用类型。
     * @return the list
     */
    public static <T> List<T> move(final List<T> list, final T element, final int newPosition) {
        Assert.notNull(list);
        if (!list.contains(element)) {
            list.add(newPosition, element);
        } else {
            list.remove(element);
            list.add(newPosition, element);
        }
        return list;
    }

}
