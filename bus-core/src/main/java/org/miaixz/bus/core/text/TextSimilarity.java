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
package org.miaixz.bus.core.text;

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 文本相似度计算
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextSimilarity {

    /**
     * 利用莱文斯坦距离(Levenshtein distance)算法计算相似度，两个都是空串相似度为1，被认为是相同的串 比较方法为：
     * <ul>
     * <li>只比较两个字符串字母、数字、汉字部分，其他符号去除</li>
     * <li>计算出两个字符串最大子串，除以最长的字符串，结果即为相似度</li>
     * </ul>
     *
     * @param strA 字符串1
     * @param strB 字符串2
     * @return 相似度
     */
    public static double similar(final String strA, final String strB) {
        final String newStrA;
        final String newStrB;
        if (strA.length() < strB.length()) {
            newStrA = removeSign(strB);
            newStrB = removeSign(strA);
        } else {
            newStrA = removeSign(strA);
            newStrB = removeSign(strB);
        }

        // 用较大的字符串长度作为分母，相似子串作为分子计算出字串相似度
        final int temp = Math.max(newStrA.length(), newStrB.length());
        if (0 == temp) {
            // 两个都是空串相似度为1，被认为是相同的串
            return 1;
        }

        final int commonLength = longestCommonSubstringLength(newStrA, newStrB);
        return MathKit.div(commonLength, temp).doubleValue();
    }

    /**
     * 利用莱文斯坦距离(Levenshtein distance)算法计算相似度百分比
     *
     * @param strA  字符串1
     * @param strB  字符串2
     * @param scale 保留小数
     * @return 百分比
     */
    public static String similar(final String strA, final String strB, final int scale) {
        return MathKit.formatPercent(similar(strA, strB), scale);
    }

    /**
     * 最长公共子串，采用动态规划算法。 其不要求所求得的字符在所给的字符串中是连续的。 算法解析见：<a href=
     * "https://leetcode-cn.com/problems/longest-common-subsequence/solution/zui-chang-gong-gong-zi-xu-lie-by-leetcod-y7u0/">zui-chang-gong-gong-zi-xu-lie-by-leetcod-y7u0</a>
     *
     * @param strA 字符串1
     * @param strB 字符串2
     * @return 最长公共子串
     */
    public static String longestCommonSubstring(final String strA, final String strB) {
        // 初始化矩阵数据,matrix[0][0]的值为0， 如果字符数组chars_strA和chars_strB的对应位相同，则matrix[i][j]的值为左上角的值加1，
        // 否则，matrix[i][j]的值等于左上方最近两个位置的较大值， 矩阵中其余各点的值为0.
        final int[][] matrix = generateMatrix(strA, strB);

        int m = strA.length();
        int n = strB.length();
        // 矩阵中，如果matrix[m][n]的值不等于matrix[m-1][n]的值也不等于matrix[m][n-1]的值，
        // 则matrix[m][n]对应的字符为相似字符元，并将其存入result数组中。
        final char[] result = new char[matrix[m][n]];
        int currentIndex = result.length - 1;
        while (matrix[m][n] != 0) {
            if (matrix[m][n] == matrix[m][n - 1]) {
                n--;
            } else if (matrix[m][n] == matrix[m - 1][n]) {
                m--;
            } else {
                result[currentIndex] = strA.charAt(m - 1);
                currentIndex--;
                n--;
                m--;
            }
        }
        return new String(result);
    }

    /**
     * 将字符串的所有数据依次写成一行，去除无意义字符串
     *
     * @param text 字符串
     * @return 处理后的字符串
     */
    private static String removeSign(final String text) {
        final int length = text.length();
        final StringBuilder sb = StringKit.builder(length);
        // 遍历字符串str,如果是汉字数字或字母，则追加到ab上面
        char c;
        for (int i = 0; i < length; i++) {
            c = text.charAt(i);
            if (isValidChar(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    /**
     * 判断字符是否为汉字，数字和字母， 因为对符号进行相似度比较没有实际意义，故符号不加入考虑范围。
     *
     * @param charValue 字符
     * @return true表示为非汉字，数字和字母，false反之
     */
    private static boolean isValidChar(final char charValue) {
        return (charValue >= 0x4E00 && charValue <= 0X9FFF) || //
                (charValue >= 'a' && charValue <= 'z') || //
                (charValue >= 'A' && charValue <= 'Z') || //
                (charValue >= '0' && charValue <= '9');
    }

    /**
     * 求公共子串，采用动态规划算法。 其不要求所求得的字符在所给的字符串中是连续的。 2023-04-06 优化堆内存占用，此处不需要matrix[m][n]的完整矩阵，仅需右下角值
     *
     * @param strA 字符串1
     * @param strB 字符串2
     * @return 公共子串
     */
    public static int longestCommonSubstringLength(final String strA, final String strB) {
        final int m = strA.length();
        final int n = strB.length();

        // 初始化矩阵数据,matrix[0][0]的值为0， 如果字符数组chars_strA和chars_strB的对应位相同，则matrix[i][j]的值为左上角的值加1，
        // 否则，matrix[i][j]的值等于左上方最近两个位置的较大值， 矩阵中其余各点的值为0.
        int[] lastLine = new int[n + 1];
        int[] currLine = new int[n + 1];
        int[] temp;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (strA.charAt(i - 1) == strB.charAt(j - 1)) {
                    currLine[j] = lastLine[j - 1] + 1;
                } else {
                    currLine[j] = Math.max(currLine[j - 1], lastLine[j]);
                }
            }
            temp = lastLine;
            lastLine = currLine;
            currLine = temp;
        }

        return lastLine[n];
    }

    /**
     * 求公共子串，采用动态规划算法。 其不要求所求得的字符在所给的字符串中是连续的。
     *
     * @param strA 字符串1
     * @param strB 字符串2
     * @return 公共串矩阵
     */
    private static int[][] generateMatrix(final String strA, final String strB) {
        final int m = strA.length();
        final int n = strB.length();

        // 初始化矩阵数据,matrix[0][0]的值为0， 如果字符数组chars_strA和chars_strB的对应位相同，则matrix[i][j]的值为左上角的值加1，
        // 否则，matrix[i][j]的值等于左上方最近两个位置的较大值， 矩阵中其余各点的值为0.
        final int[][] matrix = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (strA.charAt(i - 1) == strB.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                } else {
                    matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
                }
            }
        }

        return matrix;
    }

}
