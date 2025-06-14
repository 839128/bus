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
package org.miaixz.bus.core.lang;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.data.CreditCode;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.*;

/**
 * 字段验证器（验证器），分两种类型的验证：
 *
 * <ul>
 * <li>isXXX：通过返回boolean值判断是否满足给定格式。</li>
 * <li>validateXXX：通过抛出异常{@link ValidateException}检查是否满足给定格式。</li>
 * </ul>
 * <p>
 * 主要验证字段非空、是否为满足指定格式等（如是否为Email、电话等）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Validator {

    /**
     * 给定值是否为{@code true}
     *
     * @param value 值
     * @return 是否为<code>true</code>
     */
    public static boolean isTrue(final boolean value) {
        return value;
    }

    /**
     * 给定值是否不为{@code false}
     *
     * @param value 值
     * @return 是否不为<code>false</code>
     */
    public static boolean isFalse(final boolean value) {
        return !value;
    }

    /**
     * 检查指定值是否为{@code true}
     *
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws ValidateException 检查不满足条件抛出的异常
     */
    public static boolean validateTrue(final boolean value, final String errorMsgTemplate, final Object... params)
            throws ValidateException {
        if (isFalse(value)) {
            throw new ValidateException(errorMsgTemplate, params);
        }
        return true;
    }

    /**
     * 检查指定值是否为{@code false}
     *
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws ValidateException 检查不满足条件抛出的异常
     */
    public static boolean validateFalse(final boolean value, final String errorMsgTemplate, final Object... params)
            throws ValidateException {
        if (isTrue(value)) {
            throw new ValidateException(errorMsgTemplate, params);
        }
        return false;
    }

    /**
     * 给定值是否为{@code null}
     *
     * @param value 值
     * @return 是否为<code>null</code>
     */
    public static boolean isNull(final Object value) {
        return null == value;
    }

    /**
     * 给定值是否不为{@code null}
     *
     * @param value 值
     * @return 是否不为<code>null</code>
     */
    public static boolean isNotNull(final Object value) {
        return null != value;
    }

    /**
     * 检查指定值是否为{@code null}
     *
     * @param <T>              被检查的对象类型
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws ValidateException 检查不满足条件抛出的异常
     */
    public static <T> T validateNull(final T value, final String errorMsgTemplate, final Object... params)
            throws ValidateException {
        if (isNotNull(value)) {
            throw new ValidateException(errorMsgTemplate, params);
        }
        return null;
    }

    /**
     * 检查指定值是否非{@code null}
     *
     * @param <T>              被检查的对象类型
     * @param value            值
     * @param errorMsgTemplate 错误消息内容模板（变量使用{}表示）
     * @param params           模板中变量替换后的值
     * @return 检查过后的值
     * @throws ValidateException 检查不满足条件抛出的异常
     */
    public static <T> T validateNotNull(final T value, final String errorMsgTemplate, final Object... params)
            throws ValidateException {
        if (isNull(value)) {
            throw new ValidateException(errorMsgTemplate, params);
        }
        return value;
    }

    /**
     * 验证是否为空 对于String类型判定是否为empty(null 或 "")
     *
     * @param value 值
     * @return 是否为空
     * @see ObjectKit#isEmpty(Object)
     */
    public static boolean isEmpty(final Object value) {
        return ObjectKit.isEmpty(value);
    }

    /**
     * 验证是否为非空 对于String类型判定是否为empty(null 或 "")
     *
     * @param value 值
     * @return 是否为空
     * @see ObjectKit#isNotEmpty(Object)
     */
    public static boolean isNotEmpty(final Object value) {
        return ObjectKit.isNotEmpty(value);
    }

    /**
     * 验证是否为空，非空时抛出异常 对于String类型判定是否为empty(null 或 "")
     *
     * @param <T>      值类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值，验证通过返回此值，空值
     * @throws ValidateException 验证异常
     */
    public static <T> T validateEmpty(final T value, final String errorMsg) throws ValidateException {
        if (isNotEmpty(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为非空，为空时抛出异常 对于String类型判定是否为empty(null 或 "")
     *
     * @param <T>      值类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值，验证通过返回此值，非空值
     * @throws ValidateException 验证异常
     */
    public static <T> T validateNotEmpty(final T value, final String errorMsg) throws ValidateException {
        if (isEmpty(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否相等 当两值都为null返回true
     *
     * @param t1 对象1
     * @param t2 对象2
     * @return 当两值都为null或相等返回true
     */
    public static boolean equal(final Object t1, final Object t2) {
        return ObjectKit.equals(t1, t2);
    }

    /**
     * 验证是否相等，不相等抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @return 相同值
     * @throws ValidateException 验证异常
     */
    public static Object validateEqual(final Object t1, final Object t2, final String errorMsg)
            throws ValidateException {
        if (!equal(t1, t2)) {
            throw new ValidateException(errorMsg);
        }
        return t1;
    }

    /**
     * 验证是否不等，相等抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws ValidateException 验证异常
     */
    public static void validateNotEqual(final Object t1, final Object t2, final String errorMsg)
            throws ValidateException {
        if (equal(t1, t2)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * 验证是否非空且与指定值相等 当数据为空时抛出验证异常 当两值不等时抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws ValidateException 验证异常
     */
    public static void validateNotEmptyAndEqual(final Object t1, final Object t2, final String errorMsg)
            throws ValidateException {
        validateNotEmpty(t1, errorMsg);
        validateEqual(t1, t2, errorMsg);
    }

    /**
     * 验证是否非空且与指定值相等 当数据为空时抛出验证异常 当两值相等时抛出异常
     *
     * @param t1       对象1
     * @param t2       对象2
     * @param errorMsg 错误信息
     * @throws ValidateException 验证异常
     */
    public static void validateNotEmptyAndNotEqual(final Object t1, final Object t2, final String errorMsg)
            throws ValidateException {
        validateNotEmpty(t1, errorMsg);
        validateNotEqual(t1, t2, errorMsg);
    }

    /**
     * 通过正则表达式验证 不符合正则抛出{@link ValidateException} 异常
     *
     * @param <T>      字符串类型
     * @param regex    正则
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateMatchRegex(final String regex, final T value,
            final String errorMsg) throws ValidateException {
        if (!isMatchRegex(regex, value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 通过正则表达式验证
     *
     * @param pattern 正则模式
     * @param value   值
     * @return 是否匹配正则
     */
    public static boolean isMatchRegex(final java.util.regex.Pattern pattern, final CharSequence value) {
        return PatternKit.isMatch(pattern, value);
    }

    /**
     * 通过正则表达式验证
     *
     * @param regex 正则
     * @param value 值
     * @return 是否匹配正则
     */
    public static boolean isMatchRegex(final String regex, final CharSequence value) {
        return PatternKit.isMatch(regex, value);
    }

    /**
     * 验证是否为英文字母 、数字和下划线
     *
     * @param value 值
     * @return 是否为英文字母 、数字和下划线
     */
    public static boolean isGeneral(final CharSequence value) {
        return isMatchRegex(Pattern.GENERAL_PATTERN, value);
    }

    /**
     * 验证是否为英文字母 、数字和下划线
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateGeneral(final T value, final String errorMsg)
            throws ValidateException {
        if (!isGeneral(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为给定长度范围的英文字母 、数字和下划线
     *
     * @param value 值
     * @param min   最小长度，负数自动识别为0
     * @param max   最大长度，0或负数表示不限制最大长度
     * @return 是否为给定长度范围的英文字母 、数字和下划线
     */
    public static boolean isGeneral(final CharSequence value, int min, final int max) {
        if (min < 0) {
            min = 0;
        }
        String reg = "^\\w{" + min + Symbol.COMMA + max + "}$";
        if (max <= 0) {
            reg = "^\\w{" + min + ",}$";
        }
        return isMatchRegex(reg, value);
    }

    /**
     * 验证是否为给定长度范围的英文字母 、数字和下划线
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param min      最小长度，负数自动识别为0
     * @param max      最大长度，0或负数表示不限制最大长度
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateGeneral(final T value, final int min, final int max,
            final String errorMsg) throws ValidateException {
        if (!isGeneral(value, min, max)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为给定最小长度的英文字母 、数字和下划线
     *
     * @param value 值
     * @param min   最小长度，负数自动识别为0
     * @return 是否为给定最小长度的英文字母 、数字和下划线
     */
    public static boolean isGeneral(final CharSequence value, final int min) {
        return isGeneral(value, min, 0);
    }

    /**
     * 验证是否为给定最小长度的英文字母 、数字和下划线
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param min      最小长度，负数自动识别为0
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateGeneral(final T value, final int min, final String errorMsg)
            throws ValidateException {
        return validateGeneral(value, min, 0, errorMsg);
    }

    /**
     * 判断字符串是否全部为字母组成，包括大写和小写字母和汉字
     *
     * @param value 值
     * @return 是否全部为字母组成，包括大写和小写字母和汉字
     */
    public static boolean isLetter(final CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isLetter);
    }

    /**
     * 验证是否全部为字母组成，包括大写和小写字母和汉字
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateLetter(final T value, final String errorMsg)
            throws ValidateException {
        if (!isLetter(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 判断字符串是否全部为大写字母
     *
     * @param value 值
     * @return 是否全部为大写字母
     */
    public static boolean isUpperCase(final CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isUpperCase);
    }

    /**
     * 验证字符串是否全部为大写字母
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateUpperCase(final T value, final String errorMsg)
            throws ValidateException {
        if (!isUpperCase(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 判断字符串是否全部为小写字母
     *
     * @param value 值
     * @return 是否全部为小写字母
     */
    public static boolean isLowerCase(final CharSequence value) {
        return StringKit.isAllCharMatch(value, Character::isLowerCase);
    }

    /**
     * 验证字符串是否全部为小写字母
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateLowerCase(final T value, final String errorMsg)
            throws ValidateException {
        if (!isLowerCase(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证该字符串是否是数字
     *
     * @param value 字符串内容
     * @return 是否是数字
     */
    public static boolean isNumber(final CharSequence value) {
        return MathKit.isNumber(value);
    }

    /**
     * 是否包含数字
     *
     * @param value 当前字符串
     * @return boolean 是否存在数字
     */
    public static boolean hasNumber(final CharSequence value) {
        return PatternKit.contains(Pattern.NUMBERS_PATTERN, value);
    }

    /**
     * 验证是否为数字
     *
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static String validateNumber(final String value, final String errorMsg) throws ValidateException {
        if (!isNumber(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证该字符串是否是字母（包括大写和小写字母）
     *
     * @param value 字符串内容
     * @return 是否是字母（包括大写和小写字母）
     */
    public static boolean isWord(final CharSequence value) {
        return isMatchRegex(Pattern.WORD_PATTERN, value);
    }

    /**
     * 验证是否为字母（包括大写和小写字母）
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateWord(final T value, final String errorMsg)
            throws ValidateException {
        if (!isWord(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为货币
     *
     * @param value 值
     * @return 是否为货币
     */
    public static boolean isMoney(final CharSequence value) {
        return isMatchRegex(Pattern.MONEY_PATTERN, value);
    }

    /**
     * 验证是否为货币
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateMoney(final T value, final String errorMsg)
            throws ValidateException {
        if (!isMoney(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;

    }

    /**
     * 验证是否为邮政编码（中国）
     *
     * @param value 值
     * @return 是否为邮政编码（中国）
     */
    public static boolean isZipCode(final CharSequence value) {
        return isMatchRegex(Pattern.ZIP_CODE_PATTERN, value);
    }

    /**
     * 验证是否为邮政编码（中国）
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateZipCode(final T value, final String errorMsg)
            throws ValidateException {
        if (!isZipCode(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为可用邮箱地址
     * 邮箱地址限制长度为254个字符，参考：https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address/44317754
     *
     * @param value 值
     * @return true为可用邮箱地址
     */
    public static boolean isEmail(final CharSequence value) {
        final int codeLength = StringKit.codeLength(value);
        if (codeLength < 1 || codeLength > 254) {
            return false;
        }

        return isMatchRegex(Pattern.EMAIL_PATTERN, value);
    }

    /**
     * 验证是否为可用邮箱地址
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateEmail(final T value, final String errorMsg)
            throws ValidateException {
        if (!isEmail(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为手机号码（中国）
     *
     * @param value 值
     * @return 是否为手机号码（中国）
     */
    public static boolean isMobile(final CharSequence value) {
        return isMatchRegex(Regex.MOBILE_PATTERN, value);
    }

    /**
     * 验证是否为手机号码（中国）
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateMobile(final T value, final String errorMsg)
            throws ValidateException {
        if (!isMobile(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为身份证号码（支持18位、15位和港澳台的10位）
     *
     * @param value 身份证号，支持18位、15位和港澳台的10位
     * @return 是否为有效身份证号码
     */
    public static boolean isCitizenId(final CharSequence value) {
        return CitizenIdKit.isValidCard(String.valueOf(value));
    }

    /**
     * 验证是否为身份证号码（支持18位、15位和港澳台的10位）
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateCitizenIdNumber(final T value, final String errorMsg)
            throws ValidateException {
        if (!isCitizenId(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为生日
     *
     * @param year  年，从1900年开始计算
     * @param month 月，从1开始计数
     * @param day   日，从1开始计数
     * @return 是否为生日
     */
    public static boolean isBirthday(final int year, final int month, final int day) {
        // 验证年
        final int thisYear = DateKit.thisYear();
        if (year < 1900 || year > thisYear) {
            return false;
        }

        // 验证月
        if (month < 1 || month > 12) {
            return false;
        }

        // 验证日
        if (day < 1 || day > 31) {
            return false;
        }
        // 检查几个特殊月的最大天数
        if (day == 31 && (month == 4 || month == 6 || month == 9 || month == 11)) {
            return false;
        }
        if (month == 2) {
            // 在2月，非闰年最大28，闰年最大29
            return day < 29 || (day == 29 && DateKit.isLeapYear(year));
        }
        return true;
    }

    /**
     * 验证是否为生日 只支持以下几种格式：
     * <ul>
     * <li>yyyyMMdd</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>yyyy年MM月dd日</li>
     * </ul>
     *
     * @param value 值
     * @return 是否为生日
     */
    public static boolean isBirthday(final CharSequence value) {
        final Matcher matcher = Pattern.BIRTHDAY_PATTERN.matcher(value);
        if (matcher.find()) {
            final int year = Integer.parseInt(matcher.group(1));
            final int month = Integer.parseInt(matcher.group(3));
            final int day = Integer.parseInt(matcher.group(5));
            return isBirthday(year, month, day);
        }
        return false;
    }

    /**
     * 验证验证是否为生日
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateBirthday(final T value, final String errorMsg)
            throws ValidateException {
        if (!isBirthday(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为IPV4地址
     *
     * @param value 值
     * @return 是否为IPV4地址
     */
    public static boolean isIpv4(final CharSequence value) {
        return isMatchRegex(Pattern.IPV4_PATTERN, value);
    }

    /**
     * 验证是否为IPV4地址
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateIpv4(final T value, final String errorMsg)
            throws ValidateException {
        if (!isIpv4(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为IPV6地址
     *
     * @param value 值
     * @return 是否为IPV6地址
     */
    public static boolean isIpv6(final CharSequence value) {
        return isMatchRegex(Pattern.IPV6_PATTERN, value);
    }

    /**
     * 验证是否为IPV6地址
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateIpv6(final T value, final String errorMsg)
            throws ValidateException {
        if (!isIpv6(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为MAC地址
     *
     * @param value 值
     * @return 是否为MAC地址
     */
    public static boolean isMac(final CharSequence value) {
        return isMatchRegex(Pattern.MAC_ADDRESS_PATTERN, value);
    }

    /**
     * 验证是否为MAC地址
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateMac(final T value, final String errorMsg)
            throws ValidateException {
        if (!isMac(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为中国车牌号
     *
     * @param value 值
     * @return 是否为中国车牌号
     */
    public static boolean isPlateNumber(final CharSequence value) {
        return isMatchRegex(Pattern.PLATE_NUMBER_PATTERN, value);
    }

    /**
     * 验证是否为中国车牌号
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validatePlateNumber(final T value, final String errorMsg)
            throws ValidateException {
        if (!isPlateNumber(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为URL
     *
     * @param value 值
     * @return 是否为URL
     */
    public static boolean isUrl(final CharSequence value) {
        if (StringKit.isBlank(value)) {
            return false;
        }
        try {
            new java.net.URL(value.toString());
        } catch (final MalformedURLException e) {
            return false;
        }
        return true;
    }

    /**
     * 验证是否为URL
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateUrl(final T value, final String errorMsg)
            throws ValidateException {
        if (!isUrl(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否都为汉字
     *
     * @param value 值
     * @return 是否为汉字
     */
    public static boolean isChinese(final CharSequence value) {
        return isMatchRegex(Pattern.CHINESES_PATTERN, value);
    }

    /**
     * 验证是否包含汉字
     *
     * @param value 值
     * @return 是否包含汉字
     */
    public static boolean hasChinese(final CharSequence value) {
        return PatternKit.contains(Regex.CHINESES, value);
    }

    /**
     * 验证是否为汉字
     *
     * @param <T>      字符串类型
     * @param value    表单值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateChinese(final T value, final String errorMsg)
            throws ValidateException {
        if (!isChinese(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为中文字、英文字母、数字和下划线
     *
     * @param value 值
     * @return 是否为中文字、英文字母、数字和下划线
     */
    public static boolean isGeneralWithChinese(final CharSequence value) {
        return isMatchRegex(Pattern.GENERAL_WITH_CHINESE_PATTERN, value);
    }

    /**
     * 验证是否为中文字、英文字母、数字和下划线
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateGeneralWithChinese(final T value, final String errorMsg)
            throws ValidateException {
        if (!isGeneralWithChinese(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为UUID 包括带横线标准格式和不带横线的简单模式
     *
     * @param value 值
     * @return 是否为UUID
     */
    public static boolean isUUID(final CharSequence value) {
        return isMatchRegex(Pattern.UUID_PATTERN, value) || isMatchRegex(Pattern.UUID_SIMPLE_PATTERN, value);
    }

    /**
     * 验证是否为UUID 包括带横线标准格式和不带横线的简单模式
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateUUID(final T value, final String errorMsg)
            throws ValidateException {
        if (!isUUID(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为Hex（16进制）字符串
     *
     * @param value 值
     * @return 是否为Hex（16进制）字符串
     */
    public static boolean isHex(final CharSequence value) {
        return isMatchRegex(Pattern.HEX_PATTERN, value);
    }

    /**
     * 验证是否为Hex（16进制）字符串
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateHex(final T value, final String errorMsg)
            throws ValidateException {
        if (!isHex(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 检查给定的数字是否在指定范围内
     *
     * @param value 值
     * @param min   最小值（包含）
     * @param max   最大值（包含）
     * @return 是否满足
     */
    public static boolean isBetween(final Number value, final Number min, final Number max) {
        Assert.notNull(value);
        Assert.notNull(min);
        Assert.notNull(max);
        final double doubleValue = value.doubleValue();
        return (doubleValue >= min.doubleValue()) && (doubleValue <= max.doubleValue());
    }

    /**
     * 检查给定的数字是否在指定范围内
     *
     * @param value    值
     * @param min      最小值（包含）
     * @param max      最大值（包含）
     * @param errorMsg 验证错误的信息
     * @throws ValidateException 验证异常
     */
    public static void validateBetween(final Number value, final Number min, final Number max, final String errorMsg)
            throws ValidateException {
        if (!isBetween(value, min, max)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * 检查给定的日期是否在指定范围内
     *
     * @param value    值
     * @param start    最小值（包含）
     * @param end      最大值（包含）
     * @param errorMsg 验证错误的信息
     * @throws ValidateException 验证异常
     */
    public static void validateBetween(final Date value, final Date start, final Date end, final String errorMsg) {
        Assert.notNull(value);
        Assert.notNull(start);
        Assert.notNull(end);

        if (!DateKit.isIn(value, start, end)) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * 是否是有效的统一社会信用代码
     * 
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     *
     * @param creditCode 统一社会信用代码
     * @return 校验结果
     */
    public static boolean isCreditCode(final CharSequence creditCode) {
        return CreditCode.isCreditCode(creditCode);
    }

    /**
     * 验证是否为车架号；别名：行驶证编号 车辆识别代号 车辆识别码
     *
     * @param value 值，17位车架号；形如：LSJA24U62JG269225、LDC613P23A1305189
     * @return 是否为车架号
     */
    public static boolean isCarVin(final CharSequence value) {
        return isMatchRegex(Pattern.CAR_VIN_PATTERN, value);
    }

    /**
     * 验证是否为车架号，别名：行驶证编号 车辆识别代号 车辆识别码
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateCarVin(final T value, final String errorMsg)
            throws ValidateException {
        if (!isCarVin(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证是否为驾驶证 别名：驾驶证档案编号、行驶证编号 仅限：中国驾驶证档案编号
     *
     * @param value 值，12位数字字符串,eg:430101758218
     * @return 是否为档案编号
     */
    public static boolean isCarDrivingLicence(final CharSequence value) {
        return isMatchRegex(Pattern.CAR_DRIVING_LICENCE_PATTERN, value);
    }

    /**
     * 是否是中文姓名 维吾尔族姓名里面的点是 · 输入法中文状态下，键盘左上角数字1前面的那个符号； 错误字符：{@code ．.。．.} 正确维吾尔族姓名：
     * 
     * <pre>
     * 霍加阿卜杜拉·麦提喀斯木
     * 玛合萨提别克·哈斯木别克
     * 阿布都热依木江·艾斯卡尔
     * 阿卜杜尼亚孜·毛力尼亚孜
     * </pre>
     * 
     * <pre>
     * ----------
     * 错误示例：孟  伟                reason: 有空格
     * 错误示例：连逍遥0               reason: 数字
     * 错误示例：依帕古丽-艾则孜        reason: 特殊符号
     * 错误示例：牙力空.买提萨力        reason: 新疆人的点不对
     * 错误示例：王建鹏2002-3-2        reason: 有数字、特殊符号
     * 错误示例：雷金默(雷皓添）        reason: 有括号
     * 错误示例：翟冬:亮               reason: 有特殊符号
     * 错误示例：李                   reason: 少于2位
     * ----------
     * </pre>
     * 
     * 总结中文姓名：2-60位，只能是中文和 ·
     *
     * @param value 中文姓名
     * @return 是否是正确的中文姓名
     */
    public static boolean isChineseName(final CharSequence value) {
        return isMatchRegex(Regex.CHINESE_NAME, value);
    }

    /**
     * 验证是否为驾驶证 别名：驾驶证档案编号、行驶证编号
     *
     * @param <T>      字符串类型
     * @param value    值
     * @param errorMsg 验证错误的信息
     * @return 验证后的值
     * @throws ValidateException 验证异常
     */
    public static <T extends CharSequence> T validateCarDrivingLicence(final T value, final String errorMsg)
            throws ValidateException {
        if (!isCarDrivingLicence(value)) {
            throw new ValidateException(errorMsg);
        }
        return value;
    }

    /**
     * 验证字符的长度是否符合要求
     *
     * @param text     字符串
     * @param min      最小长度
     * @param max      最大长度
     * @param errorMsg 错误消息
     */
    public static void validateLength(final CharSequence text, final int min, final int max, final String errorMsg) {
        final int len = StringKit.length(text);
        if (len < min || len > max) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * 验证字符串的字节长度是否符合要求，默认采用"utf-8"编码
     *
     * @param text     字符串
     * @param min      最小长度
     * @param max      最大长度
     * @param errorMsg 错误消息
     */
    public static void validateByteLength(final CharSequence text, final int min, final int max,
            final String errorMsg) {
        validateByteLength(text, min, max, Charset.UTF_8, errorMsg);
    }

    /**
     * 验证字符串的字节长度是否符合要求
     *
     * @param text     字符串
     * @param min      最小长度
     * @param max      最大长度
     * @param charset  字符编码
     * @param errorMsg 错误消息
     */
    public static void validateByteLength(final CharSequence text, final int min, final int max,
            final java.nio.charset.Charset charset, final String errorMsg) {
        final int len = StringKit.byteLength(text, charset);
        if (len < min || len > max) {
            throw new ValidateException(errorMsg);
        }
    }

    /**
     * 检查给定的index是否超出长度限制
     * <ul>
     * <li>数组调用setOrPadding时，最多允许padding的长度</li>
     * <li>List调用setOrPadding时，最多允许padding的长度</li>
     * <li>JSONArray调用setOrPadding时，最多允许padding的长度</li>
     * </ul>
     *
     * @param index 索引
     * @param limit 限制大小
     */
    public static void checkIndexLimit(final int index, final int limit) {
        // 增加安全检查，最多增加10倍
        if (index > limit) {
            throw new ValidateException("Index [{}] is too large for limit: [{}]", index, limit);
        }
    }

}
