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
package org.miaixz.bus.core.center.array;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.RandomKit;

/**
 * 原始类型数组工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PrimitiveArray extends ArrayValidator {

    /**
     * 生成一个新的重新设置大小的数组 调整大小后拷贝原数组到新数组下。扩大则占位前N个位置，其它位置补充0，缩小则截断
     *
     * @param bytes   原数组
     * @param newSize 新的数组大小
     * @return 调整后的新数组
     */
    public static byte[] resize(final byte[] bytes, final int newSize) {
        if (newSize < 0) {
            return bytes;
        }
        final byte[] newArray = new byte[newSize];
        if (newSize > 0 && isNotEmpty(bytes)) {
            System.arraycopy(bytes, 0, newArray, 0, Math.min(bytes.length, newSize));
        }
        return newArray;
    }

    /**
     * 将多个数组合并在一起 忽略null的数组
     *
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static byte[] addAll(final byte[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // 计算总长度
        int length = 0;
        for (final byte[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final byte[] result = new byte[length];
        length = 0;
        for (final byte[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * 将多个数组合并在一起 忽略null的数组
     *
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static int[] addAll(final int[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // 计算总长度
        int length = 0;
        for (final int[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final int[] result = new int[length];
        length = 0;
        for (final int[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * 将多个数组合并在一起 忽略null的数组
     *
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static long[] addAll(final long[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // 计算总长度
        int length = 0;
        for (final long[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final long[] result = new long[length];
        length = 0;
        for (final long[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * 将多个数组合并在一起 忽略null的数组
     *
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static double[] addAll(final double[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // 计算总长度
        int length = 0;
        for (final double[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final double[] result = new double[length];
        length = 0;
        for (final double[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * 将多个数组合并在一起 忽略null的数组
     *
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static float[] addAll(final float[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // 计算总长度
        int length = 0;
        for (final float[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final float[] result = new float[length];
        length = 0;
        for (final float[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * 将多个数组合并在一起 忽略null的数组
     *
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static char[] addAll(final char[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // 计算总长度
        int length = 0;
        for (final char[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final char[] result = new char[length];
        length = 0;
        for (final char[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * 将多个数组合并在一起 忽略null的数组
     *
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static boolean[] addAll(final boolean[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // 计算总长度
        int length = 0;
        for (final boolean[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final boolean[] result = new boolean[length];
        length = 0;
        for (final boolean[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * 将多个数组合并在一起 忽略null的数组
     *
     * @param arrays 数组集合
     * @return 合并后的数组
     */
    public static short[] addAll(final short[]... arrays) {
        if (arrays.length == 1) {
            return arrays[0];
        }

        // 计算总长度
        int length = 0;
        for (final short[] array : arrays) {
            if (isNotEmpty(array)) {
                length += array.length;
            }
        }

        final short[] result = new short[length];
        length = 0;
        for (final short[] array : arrays) {
            if (isNotEmpty(array)) {
                System.arraycopy(array, 0, result, length, array.length);
                length += array.length;
            }
        }
        return result;
    }

    /**
     * 拆分byte数组为几个等份（最后一份按照剩余长度分配空间）
     *
     * @param array 数组
     * @param len   每个小节的长度
     * @return 拆分后的数组
     */
    public static byte[][] split(final byte[] array, final int len) {
        final int amount = array.length / len;
        final int remainder = array.length % len;
        // 兼容切片长度大于原数组长度的情况
        final boolean hasRemainder = remainder > 0;
        final byte[][] arrays = new byte[hasRemainder ? (amount + 1) : amount][];
        byte[] arr;
        int start = 0;
        for (int i = 0; i < amount; i++) {
            arr = new byte[len];
            System.arraycopy(array, start, arr, 0, len);
            arrays[i] = arr;
            start += len;
        }
        if (hasRemainder) {
            // 有剩余，按照实际长度创建
            arr = new byte[remainder];
            System.arraycopy(array, start, arr, 0, remainder);
            arrays[amount] = arr;
        }
        return arrays;
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static int indexOf(final long[] array, final long value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static int lastIndexOf(final long[] array, final long value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final long[] array, final long value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static int indexOf(final int[] array, final int value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static int lastIndexOf(final int[] array, final int value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final int[] array, final int value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static int indexOf(final short[] array, final short value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static int lastIndexOf(final short[] array, final short value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final short[] array, final short value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static int indexOf(final char[] array, final char value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static int lastIndexOf(final char[] array, final char value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final char[] array, final char value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static int indexOf(final byte[] array, final byte value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static int lastIndexOf(final byte[] array, final byte value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final byte[] array, final byte value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static int indexOf(final double[] array, final double value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (MathKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static int lastIndexOf(final double[] array, final double value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (MathKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final double[] array, final double value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static int indexOf(final float[] array, final float value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (MathKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static int lastIndexOf(final float[] array, final float value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (MathKit.equals(value, array[i])) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final float[] array, final float value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 返回数组中指定元素所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素所在位置，未找到返回{@link Normal#__1}
     */
    public static int indexOf(final boolean[] array, final boolean value) {
        if (isNotEmpty(array)) {
            for (int i = 0; i < array.length; i++) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 返回数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 数组中指定元素最后的所在位置，未找到返回{@link Normal#__1}
     */
    public static int lastIndexOf(final boolean[] array, final boolean value) {
        if (isNotEmpty(array)) {
            for (int i = array.length - 1; i >= 0; i--) {
                if (value == array[i]) {
                    return i;
                }
            }
        }
        return Normal.__1;
    }

    /**
     * 数组中是否包含元素
     *
     * @param array 数组
     * @param value 被检查的元素
     * @return 是否包含
     */
    public static boolean contains(final boolean[] array, final boolean value) {
        return indexOf(array, value) > Normal.__1;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Integer[] wrap(final int... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Integer[0];
        }

        final Integer[] array = new Integer[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组，null转为0
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static int[] unWrap(final Integer... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new int[0];
        }

        final int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], 0);
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Long[] wrap(final long... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Long[0];
        }

        final Long[] array = new Long[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static long[] unWrap(final Long... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new long[0];
        }

        final long[] array = new long[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], 0L);
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Character[] wrap(final char... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Character[0];
        }

        final Character[] array = new Character[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static char[] unWrap(final Character... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new char[0];
        }

        final char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], Character.MIN_VALUE);
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Byte[] wrap(final byte... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Byte[0];
        }

        final Byte[] array = new Byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static byte[] unWrap(final Byte... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new byte[0];
        }

        final byte[] array = new byte[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], (byte) 0);
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Short[] wrap(final short... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Short[0];
        }

        final Short[] array = new Short[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static short[] unWrap(final Short... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new short[0];
        }

        final short[] array = new short[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], (short) 0);
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Float[] wrap(final float... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Float[0];
        }

        final Float[] array = new Float[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static float[] unWrap(final Float... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new float[0];
        }

        final float[] array = new float[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], 0F);
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Double[] wrap(final double... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Double[0];
        }

        final Double[] array = new Double[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static double[] unWrap(final Double... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new double[0];
        }

        final double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], 0D);
        }
        return array;
    }

    /**
     * 将原始类型数组包装为包装类型
     *
     * @param values 原始类型数组
     * @return 包装类型数组
     */
    public static Boolean[] wrap(final boolean... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new Boolean[0];
        }

        final Boolean[] array = new Boolean[length];
        for (int i = 0; i < length; i++) {
            array[i] = values[i];
        }
        return array;
    }

    /**
     * 包装类数组转为原始类型数组 {@code null} 按照 {@code false} 对待
     *
     * @param values 包装类型数组
     * @return 原始类型数组
     */
    public static boolean[] unWrap(final Boolean... values) {
        if (null == values) {
            return null;
        }
        final int length = values.length;
        if (0 == length) {
            return new boolean[0];
        }

        final boolean[] array = new boolean[length];
        for (int i = 0; i < length; i++) {
            array[i] = ObjectKit.defaultIfNull(values[i], false);
        }
        return array;
    }

    /**
     * 获取子数组
     * <ul>
     * <li>位置可以为负数，例如 -1 代表 数组最后一个元素的位置</li>
     * <li>如果 开始位置 大于 结束位置，会自动交换</li>
     * <li>如果 结束位置 大于 数组长度，会变为数组长度</li>
     * </ul>
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static byte[] sub(final byte[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = Array.getLength(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return new byte[0];
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 获取子数组
     * <ul>
     * <li>位置可以为负数，例如 -1 代表 数组最后一个元素的位置</li>
     * <li>如果 开始位置 大于 结束位置，会自动交换</li>
     * <li>如果 结束位置 大于 数组长度，会变为数组长度</li>
     * </ul>
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static int[] sub(final int[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = Array.getLength(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return new int[0];
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 获取子数组
     * <ul>
     * <li>位置可以为负数，例如 -1 代表 数组最后一个元素的位置</li>
     * <li>如果 开始位置 大于 结束位置，会自动交换</li>
     * <li>如果 结束位置 大于 数组长度，会变为数组长度</li>
     * </ul>
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static long[] sub(final long[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = Array.getLength(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return new long[0];
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 获取子数组
     * <ul>
     * <li>位置可以为负数，例如 -1 代表 数组最后一个元素的位置</li>
     * <li>如果 开始位置 大于 结束位置，会自动交换</li>
     * <li>如果 结束位置 大于 数组长度，会变为数组长度</li>
     * </ul>
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static short[] sub(final short[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = Array.getLength(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return new short[0];
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 获取子数组
     * <ul>
     * <li>位置可以为负数，例如 -1 代表 数组最后一个元素的位置</li>
     * <li>如果 开始位置 大于 结束位置，会自动交换</li>
     * <li>如果 结束位置 大于 数组长度，会变为数组长度</li>
     * </ul>
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static char[] sub(final char[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = Array.getLength(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return new char[0];
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 获取子数组
     * <ul>
     * <li>位置可以为负数，例如 -1 代表 数组最后一个元素的位置</li>
     * <li>如果 开始位置 大于 结束位置，会自动交换</li>
     * <li>如果 结束位置 大于 数组长度，会变为数组长度</li>
     * </ul>
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static double[] sub(final double[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = Array.getLength(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return new double[0];
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 获取子数组
     * <ul>
     * <li>位置可以为负数，例如 -1 代表 数组最后一个元素的位置</li>
     * <li>如果 开始位置 大于 结束位置，会自动交换</li>
     * <li>如果 结束位置 大于 数组长度，会变为数组长度</li>
     * </ul>
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static float[] sub(final float[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = Array.getLength(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return new float[0];
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 获取子数组
     * <ul>
     * <li>位置可以为负数，例如 -1 代表 数组最后一个元素的位置</li>
     * <li>如果 开始位置 大于 结束位置，会自动交换</li>
     * <li>如果 结束位置 大于 数组长度，会变为数组长度</li>
     * </ul>
     *
     * @param array 数组
     * @param start 开始位置（包括）
     * @param end   结束位置（不包括）
     * @return 新的数组
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static boolean[] sub(final boolean[] array, int start, int end) {
        Assert.notNull(array, "array must be not null !");
        final int length = Array.getLength(array);
        if (start < 0) {
            start += length;
        }
        if (end < 0) {
            end += length;
        }
        if (start > end) {
            final int tmp = start;
            start = end;
            end = tmp;
        }
        if (start >= length) {
            return new boolean[0];
        }
        if (end > length) {
            end = length;
        }
        return Arrays.copyOfRange(array, start, end);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static long[] remove(final long[] array, final int index) throws IllegalArgumentException {
        return (long[]) remove((Object) array, index);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static int[] remove(final int[] array, final int index) throws IllegalArgumentException {
        return (int[]) remove((Object) array, index);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static short[] remove(final short[] array, final int index) throws IllegalArgumentException {
        return (short[]) remove((Object) array, index);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static char[] remove(final char[] array, final int index) throws IllegalArgumentException {
        return (char[]) remove((Object) array, index);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static byte[] remove(final byte[] array, final int index) throws IllegalArgumentException {
        return (byte[]) remove((Object) array, index);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static double[] remove(final double[] array, final int index) throws IllegalArgumentException {
        return (double[]) remove((Object) array, index);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static float[] remove(final float[] array, final int index) throws IllegalArgumentException {
        return (float[]) remove((Object) array, index);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static boolean[] remove(final boolean[] array, final int index) throws IllegalArgumentException {
        return (boolean[]) remove((Object) array, index);
    }

    /**
     * 移除数组中对应位置的元素 copier from commons-lang
     *
     * @param array 数组对象，可以是对象数组，也可以原始类型数组
     * @param index 位置，如果位置小于0或者大于长度，返回原数组
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static Object remove(final Object array, final int index) throws IllegalArgumentException {
        if (null == array) {
            return null;
        }
        final int length = Array.getLength(array);
        if (index < 0 || index >= length) {
            return array;
        }

        final Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
        System.arraycopy(array, 0, result, 0, index);
        if (index < length - 1) {
            // 后半部分
            System.arraycopy(array, index + 1, result, index, length - index - 1);
        }

        return result;
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param array   数组对象
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static long[] removeEle(final long[] array, final long element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param array   数组对象
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static int[] removeEle(final int[] array, final int element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param array   数组对象
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static short[] removeEle(final short[] array, final short element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param array   数组对象
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static char[] removeEle(final char[] array, final char element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param array   数组对象
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static byte[] removeEle(final byte[] array, final byte element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param array   数组对象
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static double[] removeEle(final double[] array, final double element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param array   数组对象
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static float[] removeEle(final float[] array, final float element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 移除数组中指定的元素 只会移除匹配到的第一个元素 copier from commons-lang
     *
     * @param array   数组对象
     * @param element 要移除的元素
     * @return 去掉指定元素后的新数组或原数组
     * @throws IllegalArgumentException 参数对象不为数组对象
     */
    public static boolean[] removeEle(final boolean[] array, final boolean element) throws IllegalArgumentException {
        return remove(array, indexOf(array, element));
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array               数组，会变更
     * @param startIndexInclusive 起始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static long[] reverse(final long[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static long[] reverse(final long[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array               数组，会变更
     * @param startIndexInclusive 起始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static int[] reverse(final int[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static int[] reverse(final int[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array               数组，会变更
     * @param startIndexInclusive 起始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static short[] reverse(final short[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static short[] reverse(final short[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array               数组，会变更
     * @param startIndexInclusive 起始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static char[] reverse(final char[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static char[] reverse(final char[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array               数组，会变更
     * @param startIndexInclusive 起始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static byte[] reverse(final byte[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static byte[] reverse(final byte[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array               数组，会变更
     * @param startIndexInclusive 起始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static double[] reverse(final double[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static double[] reverse(final double[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array               数组，会变更
     * @param startIndexInclusive 起始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static float[] reverse(final float[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static float[] reverse(final float[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array               数组，会变更
     * @param startIndexInclusive 起始位置（包含）
     * @param endIndexExclusive   结束位置（不包含）
     * @return 变更后的原数组
     */
    public static boolean[] reverse(final boolean[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (isEmpty(array)) {
            return array;
        }
        int i = Math.max(startIndexInclusive, 0);
        int j = Math.min(array.length, endIndexExclusive) - 1;
        while (j > i) {
            swap(array, i, j);
            j--;
            i++;
        }
        return array;
    }

    /**
     * 反转数组，会变更原数组
     *
     * @param array 数组，会变更
     * @return 变更后的原数组
     */
    public static boolean[] reverse(final boolean[] array) {
        return reverse(array, 0, array.length);
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     */
    public static long min(final long... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        long min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     */
    public static int min(final int... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        int min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     */
    public static short min(final short... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        short min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     */
    public static char min(final char... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        char min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     */
    public static byte min(final byte... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        byte min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     */
    public static double min(final double... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        double min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最小值
     *
     * @param numberArray 数字数组
     * @return 最小值
     */
    public static float min(final float... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        float min = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (min > numberArray[i]) {
                min = numberArray[i];
            }
        }
        return min;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     */
    public static long max(final long... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        long max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     */
    public static int max(final int... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        int max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     */
    public static short max(final short... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        short max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     */
    public static char max(final char... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        char max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     */
    public static byte max(final byte... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        byte max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     */
    public static double max(final double... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        double max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 取最大值
     *
     * @param numberArray 数字数组
     * @return 最大值
     */
    public static float max(final float... numberArray) {
        if (isEmpty(numberArray)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        float max = numberArray[0];
        for (int i = 1; i < numberArray.length; i++) {
            if (max < numberArray[i]) {
                max = numberArray[i];
            }
        }
        return max;
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static int[] shuffle(final int[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
     */
    public static int[] shuffle(final int[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static long[] shuffle(final long[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
     */
    public static long[] shuffle(final long[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static double[] shuffle(final double[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
     */
    public static double[] shuffle(final double[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static float[] shuffle(final float[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
     */
    public static float[] shuffle(final float[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static boolean[] shuffle(final boolean[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
     */
    public static boolean[] shuffle(final boolean[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static byte[] shuffle(final byte[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
     */
    public static byte[] shuffle(final byte[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static char[] shuffle(final char[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
     */
    public static char[] shuffle(final char[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array 数组，会变更
     * @return 打乱后的数组
     */
    public static short[] shuffle(final short[] array) {
        return shuffle(array, RandomKit.getRandom());
    }

    /**
     * 打乱数组顺序，会变更原数组
     *
     * @param array  数组，会变更
     * @param random 随机数生成器
     * @return 打乱后的数组
     */
    public static short[] shuffle(final short[] array, final Random random) {
        if (array == null || random == null || array.length <= 1) {
            return array;
        }

        for (int i = array.length; i > 1; i--) {
            swap(array, i - 1, random.nextInt(i));
        }

        return array;
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public static int[] swap(final int[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final int tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public static long[] swap(final long[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final long tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public static double[] swap(final double[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final double tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public static float[] swap(final float[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final float tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public static boolean[] swap(final boolean[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final boolean tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public static byte[] swap(final byte[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final byte tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public static char[] swap(final char[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final char tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * 交换数组中两个位置的值
     *
     * @param array  数组
     * @param index1 位置1
     * @param index2 位置2
     * @return 交换后的数组，与传入数组为同一对象
     */
    public static short[] swap(final short[] array, final int index1, final int index2) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("Number array must not empty !");
        }
        final short tmp = array[index1];
        array[index1] = array[index2];
        array[index2] = tmp;
        return array;
    }

    /**
     * array数组是否以prefix开头
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @return 是否开头
     */
    public static boolean startWith(final boolean[] array, final boolean... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * array数组是否以prefix开头
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @return 是否开头
     */
    public static boolean startWith(final byte[] array, final byte... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        return isSubEquals(array, 0, prefix);
    }

    /**
     * array数组是否以prefix开头
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @return 是否开头
     */
    public static boolean startWith(final char[] array, final char... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * array数组是否以prefix开头
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @return 是否开头
     */
    public static boolean startWith(final double[] array, final double... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * array数组是否以prefix开头
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @return 是否开头
     */
    public static boolean startWith(final float[] array, final float... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * array数组是否以prefix开头
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @return 是否开头
     */
    public static boolean startWith(final int[] array, final int... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * array数组是否以prefix开头
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @return 是否开头
     */
    public static boolean startWith(final long[] array, final long... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * array数组是否以prefix开头
     * <ul>
     * <li>array和prefix为同一个数组（即array == prefix），返回{@code true}</li>
     * <li>array或prefix为空数组（null或length为0的数组），返回{@code true}</li>
     * <li>prefix长度大于array，返回{@code false}</li>
     * </ul>
     *
     * @param array  数组
     * @param prefix 前缀
     * @return 是否开头
     */
    public static boolean startWith(final short[] array, final short... prefix) {
        if (array == prefix) {
            return true;
        }
        if (isEmpty(array)) {
            return isEmpty(prefix);
        }
        if (prefix.length > array.length) {
            return false;
        }

        for (int i = 0; i < prefix.length; i++) {
            if (array[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

}
