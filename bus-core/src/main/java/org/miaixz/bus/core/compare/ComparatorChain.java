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
package org.miaixz.bus.core.compare;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import org.miaixz.bus.core.lang.Chain;

/**
 * 比较器链。此链包装了多个比较器，最终比较结果按照比较器顺序综合多个比较器结果 按照比较器链的顺序分别比较，如果比较出相等则转向下一个比较器，否则直接返回 此类copy from Apache-commons-collections
 *
 * @param <E> 被比较的对象
 * @author Kimi Liu
 * @since Java 17+
 */
public class ComparatorChain<E> implements Chain<Comparator<E>, ComparatorChain<E>>, Comparator<E>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852260773785L;

    /**
     * 比较器链.
     */
    private final List<Comparator<E>> chain;
    /**
     * 对应比较器位置是否反序.
     */
    private final BitSet orderingBits;
    /**
     * 比较器是否被锁定。锁定的比较器链不能再添加新的比较器。比较器会在开始比较时开始加锁。
     */
    private boolean lock = false;

    /**
     * 构造空的比较器链，必须至少有一个比较器，否则会在compare时抛出{@link UnsupportedOperationException}
     */
    public ComparatorChain() {
        this(new ArrayList<>(), new BitSet());
    }

    /**
     * 构造，初始化单一比较器。比较器为正序
     *
     * @param comparator 在比较器链中的第一个比较器
     */
    public ComparatorChain(final Comparator<E> comparator) {
        this(comparator, false);
    }

    /**
     * 构造，初始化单一比较器。自定义正序还是反序
     *
     * @param comparator 在比较器链中的第一个比较器
     * @param reverse    是否反序，true表示反序，false正序
     */
    public ComparatorChain(final Comparator<E> comparator, final boolean reverse) {
        chain = new ArrayList<>(1);
        chain.add(comparator);
        orderingBits = new BitSet(1);
        if (reverse == true) {
            orderingBits.set(0);
        }
    }

    /**
     * 构造，使用已有的比较器列表
     *
     * @param list 比较器列表
     * @see #ComparatorChain(List, BitSet)
     */
    public ComparatorChain(final List<Comparator<E>> list) {
        this(list, new BitSet(list.size()));
    }

    /**
     * 构造，使用已有的比较器列表和对应的BitSet BitSet中的boolean值需与list中的{@link Comparator}一一对应，true表示正序，false反序
     *
     * @param list {@link Comparator} 列表
     * @param bits {@link Comparator} 列表对应的排序boolean值，true表示正序，false反序
     */
    public ComparatorChain(final List<Comparator<E>> list, final BitSet bits) {
        chain = list;
        orderingBits = bits;
    }

    /**
     * 构建 {@code ComparatorChain}
     *
     * @param <E>        被比较对象类型
     * @param comparator 比较器
     * @return {@code ComparatorChain}
     */
    public static <E> ComparatorChain<E> of(final Comparator<E> comparator) {
        return of(comparator, false);
    }

    /**
     * 构建 {@code ComparatorChain}
     *
     * @param <E>        被比较对象类型
     * @param comparator 比较器
     * @param reverse    是否反向
     * @return {@code ComparatorChain}
     */
    public static <E> ComparatorChain<E> of(final Comparator<E> comparator, final boolean reverse) {
        return new ComparatorChain<>(comparator, reverse);
    }

    /**
     * 构建 {@code ComparatorChain}
     *
     * @param <E>         被比较对象类型
     * @param comparators 比较器数组
     * @return {@code ComparatorChain}
     */
    @SafeVarargs
    public static <E> ComparatorChain<E> of(final Comparator<E>... comparators) {
        return of(Arrays.asList(comparators));
    }

    /**
     * 构建 {@code ComparatorChain}
     *
     * @param <E>         被比较对象类型
     * @param comparators 比较器列表
     * @return {@code ComparatorChain}
     */
    public static <E> ComparatorChain<E> of(final List<Comparator<E>> comparators) {
        return new ComparatorChain<>(comparators);
    }

    /**
     * 构建 {@code ComparatorChain}
     *
     * @param <E>         被比较对象类型
     * @param comparators 比较器列表
     * @param bits        {@link Comparator} 列表对应的排序boolean值，true表示正序，false反序
     * @return {@code ComparatorChain}
     */
    public static <E> ComparatorChain<E> of(final List<Comparator<E>> comparators, final BitSet bits) {
        return new ComparatorChain<>(comparators, bits);
    }

    /**
     * 在链的尾部添加比较器，使用正向排序
     *
     * @param comparator {@link Comparator} 比较器，正向
     * @return this
     */
    public ComparatorChain<E> addComparator(final Comparator<E> comparator) {
        return addComparator(comparator, false);
    }

    /**
     * 在链的尾部添加比较器，使用给定排序方式
     *
     * @param comparator {@link Comparator} 比较器
     * @param reverse    是否反序，true表示正序，false反序
     * @return this
     */
    public ComparatorChain<E> addComparator(final Comparator<E> comparator, final boolean reverse) {
        checkLocked();

        chain.add(comparator);
        if (reverse == true) {
            orderingBits.set(chain.size() - 1);
        }
        return this;
    }

    /**
     * 替换指定位置的比较器，保持原排序方式
     *
     * @param index      位置
     * @param comparator {@link Comparator}
     * @return this
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &gt;= size()
     */
    public ComparatorChain<E> setComparator(final int index, final Comparator<E> comparator)
            throws IndexOutOfBoundsException {
        return setComparator(index, comparator, false);
    }

    /**
     * 替换指定位置的比较器，替换指定排序方式
     *
     * @param index      位置
     * @param comparator {@link Comparator}
     * @param reverse    是否反序，true表示正序，false反序
     * @return this
     */
    public ComparatorChain<E> setComparator(final int index, final Comparator<E> comparator, final boolean reverse) {
        checkLocked();

        chain.set(index, comparator);
        if (reverse == true) {
            orderingBits.set(index);
        } else {
            orderingBits.clear(index);
        }
        return this;
    }

    /**
     * 更改指定位置的排序方式为正序
     *
     * @param index 位置
     * @return this
     */
    public ComparatorChain<E> setForwardSort(final int index) {
        checkLocked();
        orderingBits.clear(index);
        return this;
    }

    /**
     * 更改指定位置的排序方式为反序
     *
     * @param index 位置
     * @return this
     */
    public ComparatorChain<E> setReverseSort(final int index) {
        checkLocked();
        orderingBits.set(index);
        return this;
    }

    /**
     * 比较器链中比较器个数
     *
     * @return Comparator count
     */
    public int size() {
        return chain.size();
    }

    /**
     * 是否已经被锁定。当开始比较时（调用compare方法）此值为true
     *
     * @return true = ComparatorChain cannot be modified; false = ComparatorChain can still be modified.
     */
    public boolean isLocked() {
        return lock;
    }

    @Override
    public Iterator<Comparator<E>> iterator() {
        return this.chain.iterator();
    }

    @Override
    public ComparatorChain<E> addChain(final Comparator<E> element) {
        return this.addComparator(element);
    }

    /**
     * 执行比较 按照比较器链的顺序分别比较，如果比较出相等则转向下一个比较器，否则直接返回
     *
     * @param o1 第一个对象
     * @param o2 第二个对象
     * @return -1, 0, or 1
     * @throws UnsupportedOperationException 如果比较器链为空，无法完成比较
     */
    @Override
    public int compare(final E o1, final E o2) throws UnsupportedOperationException {
        if (lock == false) {
            checkChainIntegrity();
            lock = true;
        }

        final Iterator<Comparator<E>> comparators = chain.iterator();
        Comparator<? super E> comparator;
        int retval;
        for (int comparatorIndex = 0; comparators.hasNext(); ++comparatorIndex) {
            comparator = comparators.next();
            retval = comparator.compare(o1, o2);
            if (retval != 0) {
                // invert the order if it is a reverse sort
                if (true == orderingBits.get(comparatorIndex)) {
                    retval = (retval > 0) ? -1 : 1;
                }
                return retval;
            }
        }

        // if comparators are exhausted, return 0
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        if (null != chain) {
            hash ^= chain.hashCode();
        }
        if (null != orderingBits) {
            hash ^= orderingBits.hashCode();
        }
        return hash;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (null == object) {
            return false;
        }
        if (object.getClass().equals(this.getClass())) {
            final ComparatorChain<?> otherChain = (ComparatorChain<?>) object;
            //
            return Objects.equals(this.orderingBits, otherChain.orderingBits) && this.chain.equals(otherChain.chain);
        }
        return false;
    }

    /**
     * 被锁定时抛出异常
     *
     * @throws UnsupportedOperationException 被锁定抛出此异常
     */
    private void checkLocked() {
        if (lock == true) {
            throw new UnsupportedOperationException(
                    "Comparator ordering cannot be changed after the first comparison is performed");
        }
    }

    /**
     * 检查比较器链是否为空，为空抛出异常
     *
     * @throws UnsupportedOperationException 为空抛出此异常
     */
    private void checkChainIntegrity() {
        if (chain.isEmpty()) {
            throw new UnsupportedOperationException("ComparatorChains must contain at least one Comparator");
        }
    }

}
