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

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

import org.miaixz.bus.core.compare.IndexedCompare;
import org.miaixz.bus.core.compare.PinyinCompare;

/**
 * 比较工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CompareKit {

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Character#compare(char, char)
     */
    public static int compare(final char x, final char y) {
        return Character.compare(x, y);
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Double#compare(double, double)
     */
    public static int compare(final double x, final double y) {
        return Double.compare(x, y);
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Integer#compare(int, int)
     */
    public static int compare(final int x, final int y) {
        return Integer.compare(x, y);
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Long#compare(long, long)
     */
    public static int compare(final long x, final long y) {
        return Long.compare(x, y);
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回小于0的数，x&gt;y返回大于0的数
     * @see Short#compare(short, short)
     */
    public static int compare(final short x, final short y) {
        return Short.compare(x, y);
    }

    /**
     * 比较两个值的大小
     *
     * @param x 第一个值
     * @param y 第二个值
     * @return x==y返回0，x&lt;y返回-1，x&gt;y返回1
     * @see Byte#compare(byte, byte)
     */
    public static int compare(final byte x, final byte y) {
        return Byte.compare(x, y);
    }

    /**
     * 获取自然排序器，即默认排序器
     *
     * <ul>
     * <li>如需对null友好操作如下</li>
     * <li>{@code Comparator.nullsLast(CompareKit.natural())}</li>
     * <li>{@code Comparator.nullsFirst(CompareKit.natural())}</li>
     * </ul>
     *
     * @param <E> 排序节点类型
     * @return 默认排序器
     */
    public static <E extends Comparable<? super E>> Comparator<E> natural() {
        return Comparator.naturalOrder();
    }

    /**
     * 获取反序排序器，即默认自然排序的反序排序器
     *
     * <ul>
     * <li>如需对null友好操作如下</li>
     * <li>{@code Comparator.nullsLast(CompareKit.naturalReverse())}</li>
     * <li>{@code Comparator.nullsFirst(CompareKit.naturalReverse())}</li>
     * </ul>
     *
     * @param <E> 排序节点类型
     * @return 默认排序器
     */
    public static <E extends Comparable<? super E>> Comparator<E> naturalReverse() {
        return Comparator.reverseOrder();
    }

    /**
     * 获取反序排序器，即默认排序器
     *
     * <ul>
     * <li>如需对null友好操作如下</li>
     * <li>{@code Comparator.nullsLast(CompareKit.reverse())}</li>
     * <li>{@code Comparator.nullsFirst(CompareKit.reverse())}</li>
     * </ul>
     *
     * @param <E>        排序节点类型
     * @param comparator 排序器
     * @return 默认排序器
     */
    public static <E extends Comparable<? super E>> Comparator<E> reverse(final Comparator<E> comparator) {
        return null == comparator ? naturalReverse() : comparator.reversed();
    }

    /**
     * 对象比较，比较结果取决于comparator，如果被比较对象为null，传入的comparator对象应处理此情况 如果传入comparator为null，则使用默认规则比较（此时被比较对象必须实现Comparable接口）
     * 一般而言，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
     *
     * @param <T>        被比较对象类型
     * @param c1         对象1
     * @param c2         对象2
     * @param comparator 比较器
     * @return 比较结果
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T> int compare(final T c1, final T c2, final Comparator<T> comparator) {
        if (null == comparator) {
            return compare((Comparable) c1, (Comparable) c2);
        }
        return comparator.compare(c1, c2);
    }

    /**
     * {@code null}安全的对象比较，{@code null}对象小于任何对象
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1，可以为{@code null}
     * @param c2  对象2，可以为{@code null}
     * @return 比较结果，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(final T c1, final T c2) {
        return compare(c1, c2, false);
    }

    /**
     * {@code null}安全的对象比较
     *
     * @param <T>           被比较对象类型（必须实现Comparable接口）
     * @param c1            对象1，可以为{@code null}
     * @param c2            对象2，可以为{@code null}
     * @param isNullGreater 当被比较对象为null时是否排在后面，true表示null大于任何对象，false反之
     * @return 比较结果，如果c1 &lt; c2，返回数小于0，c1==c2返回0，c1 &gt; c2 大于0
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(final T c1, final T c2, final boolean isNullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return isNullGreater ? 1 : -1;
        } else if (c2 == null) {
            return isNullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }

    /**
     * 自然比较两个对象的大小，比较规则如下：
     *
     * <pre>
     * 1、如果实现Comparable调用compareTo比较
     * 2、o1.equals(o2)返回0
     * 3、比较hashCode值
     * 4、比较toString值
     * </pre>
     *
     * @param <T>           被比较对象类型
     * @param o1            对象1
     * @param o2            对象2
     * @param isNullGreater null值是否做为最大值
     * @return 比较结果，如果o1 &lt; o2，返回数小于0，o1==o2返回0，o1 &gt; o2 大于0
     */
    public static <T> int compare(final T o1, final T o2, final boolean isNullGreater) {
        if (o1 == o2) {
            return 0;
        } else if (null == o1) {// null 排在后面
            return isNullGreater ? 1 : -1;
        } else if (null == o2) {
            return isNullGreater ? -1 : 1;
        }

        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            // 如果bean可比较，直接比较bean
            return ((Comparable) o1).compareTo(o2);
        }

        if (o1.equals(o2)) {
            return 0;
        }

        int result = Integer.compare(o1.hashCode(), o2.hashCode());
        if (0 == result) {
            result = compare(o1.toString(), o2.toString());
        }

        return result;
    }

    /**
     * 中文比较器
     *
     * @param keyExtractor 从对象中提取中文(参与比较的内容)
     * @param <T>          对象类型
     * @return 中文比较器
     */
    public static <T> Comparator<T> comparingPinyin(final Function<T, String> keyExtractor) {
        return comparingPinyin(keyExtractor, false);
    }

    /**
     * 中文（拼音）比较器
     *
     * @param keyExtractor 从对象中提取中文(参与比较的内容)
     * @param reverse      是否反序
     * @param <T>          对象类型
     * @return 中文比较器
     */
    public static <T> Comparator<T> comparingPinyin(final Function<T, String> keyExtractor, final boolean reverse) {
        Objects.requireNonNull(keyExtractor);
        final PinyinCompare pinyinComparator = new PinyinCompare();
        if (reverse) {
            return (o1, o2) -> pinyinComparator.compare(keyExtractor.apply(o2), keyExtractor.apply(o1));
        }
        return (o1, o2) -> pinyinComparator.compare(keyExtractor.apply(o1), keyExtractor.apply(o2));
    }

    /**
     * 索引比较器 通过keyExtractor函数，提取对象的某个属性或规则，根据提供的排序数组，完成比较 objs中缺失的，默认排序在前面(atEndIfMiss=false)
     *
     * @param keyExtractor 从对象中提取中文(参与比较的内容)
     * @param objs         参与排序的数组，数组的元素位置决定了对象的排序先后
     * @param <T>          对象类型
     * @param <U>          数组对象类型
     * @return 索引比较器
     */
    public static <T, U> Comparator<T> comparingIndexed(final Function<? super T, ? extends U> keyExtractor,
            final U[] objs) {
        return comparingIndexed(keyExtractor, false, objs);
    }

    /**
     * 索引比较器 通过keyExtractor函数，提取对象的某个属性或规则，根据提供的排序数组，完成比较 objs中缺失的，默认排序在前面(atEndIfMiss=false)
     *
     * @param keyExtractor 从对象中提取中文(参与比较的内容)
     * @param objs         参与排序的集合对象，数组的元素位置决定了对象的排序先后
     * @param <T>          对象类型
     * @param <U>          数组对象类型
     * @return 索引比较器
     */
    public static <T, U> Comparator<T> comparingIndexed(final Function<? super T, ? extends U> keyExtractor,
            final Iterable<U> objs) {
        return comparingIndexed(keyExtractor, false,
                ArrayKit.ofArray(objs, (Class<U>) objs.iterator().next().getClass()));
    }

    /**
     * 索引比较器 通过keyExtractor函数，提取对象的某个属性或规则，根据提供的排序数组，完成比较
     *
     * @param keyExtractor 从对象中提取排序键的函数(参与比较的内容)
     * @param atEndIfMiss  如果不在列表中是否排在后边; true:排在后边; false:排在前边
     * @param objs         参与排序的数组，数组的元素位置决定了对象的排序先后, 示例：{@code int[] objs = new int[]{3, 2, 1, 4, 5,6};}
     * @param <T>          对象类型
     * @param <U>          数组对象类型
     * @return 索引比较器
     */
    public static <T, U> Comparator<T> comparingIndexed(final Function<? super T, ? extends U> keyExtractor,
            final boolean atEndIfMiss, final U... objs) {
        Objects.requireNonNull(keyExtractor);
        final IndexedCompare<U> indexedComparator = new IndexedCompare<>(atEndIfMiss, objs);
        return (o1, o2) -> indexedComparator.compare(keyExtractor.apply(o1), keyExtractor.apply(o2));
    }

    /**
     * 取两个值中的最小值，大小相同返回第一个值
     *
     * @param <T> 值类型
     * @param t1  第一个值
     * @param t2  第二个值
     * @return 最小值
     */
    public static <T extends Comparable<? super T>> T min(final T t1, final T t2) {
        return compare(t1, t2) <= 0 ? t1 : t2;
    }

    /**
     * 取两个值中的最大值，大小相同返回第一个值
     *
     * @param <T> 值类型
     * @param t1  第一个值
     * @param t2  第二个值
     * @return 最大值
     */
    public static <T extends Comparable<? super T>> T max(final T t1, final T t2) {
        return compare(t1, t2) >= 0 ? t1 : t2;
    }

    /**
     * {@code null}安全的检查两个对象是否相同，通过调用{@code compare(c1, c2) == 0}完成
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1，可以为{@code null}
     * @param c2  对象2，可以为{@code null}
     * @return 是否相等
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> boolean equals(final T c1, final T c2) {
        return compare(c1, c2) == 0;
    }

    /**
     * c1是否大于c2，通过调用{@code compare(c1, c2) > 0}完成
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1，可以为{@code null}
     * @param c2  对象2，可以为{@code null}
     * @return c1是否大于c2
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> boolean gt(final T c1, final T c2) {
        return compare(c1, c2) > 0;
    }

    /**
     * c1是否大于或等于c2，通过调用{@code compare(c1, c2) >= 0}完成
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1，可以为{@code null}
     * @param c2  对象2，可以为{@code null}
     * @return c1是否大于或等于c2
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> boolean ge(final T c1, final T c2) {
        return compare(c1, c2) >= 0;
    }

    /**
     * c1是否大小于c2，通过调用{@code compare(c1, c2) < 0}完成
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1，可以为{@code null}
     * @param c2  对象2，可以为{@code null}
     * @return c1是否小于c2
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> boolean lt(final T c1, final T c2) {
        return compare(c1, c2) < 0;
    }

    /**
     * c1是否小于或等于c2，通过调用{@code compare(c1, c2) <= 0}完成
     *
     * @param <T> 被比较对象类型
     * @param c1  对象1，可以为{@code null}
     * @param c2  对象2，可以为{@code null}
     * @return c1是否小于或等于c2
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> boolean le(final T c1, final T c2) {
        return compare(c1, c2) <= 0;
    }

    /**
     * 给定的{@code value}是否在{@code c1}和{@code c2}的范围内 即 {@code min(c1,c2) <= value <= max(c1,c2)}
     *
     * @param <T>   被比较对象类型
     * @param value 检查的对象，可以为{@code null}
     * @param c1    对象1，可以为{@code null}
     * @param c2    对象2，可以为{@code null}
     * @return 给定的{@code value}是否在{@code c1}和{@code c2}的范围内
     */
    public static <T extends Comparable<? super T>> boolean isIn(final T value, final T c1, final T c2) {
        return ge(value, min(c1, c2)) && le(value, max(c1, c2));
    }

    /**
     * 给定的{@code value}是否在{@code c1}和{@code c2}的范围内，但是不包括边界 即 {@code min(c1,c2) < value < max(c1,c2)}
     *
     * @param <T>   被比较对象类型
     * @param value 检查的对象，可以为{@code null}
     * @param c1    对象1，可以为{@code null}
     * @param c2    对象2，可以为{@code null}
     * @return c1是否小于或等于c2
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> boolean isInExclusive(final T value, final T c1, final T c2) {
        return gt(value, min(c1, c2)) && lt(value, max(c1, c2));
    }

}
