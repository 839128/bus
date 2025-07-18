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

import java.util.regex.Pattern;

/**
 * 常用正则表达式字符
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Regex {

    /**
     * 英文字母 、数字和下划线
     */
    public static final String GENERAL = "^\\w+$";
    /**
     * 数字
     */
    public static final String NUMBERS = "\\d+";
    /**
     * 字母
     */
    public static final String WORD = "[a-zA-Z]+";
    /**
     * 非数字
     */
    public static final String NOT_NUMBERS = "[^0-9]+";

    /**
     * 从非数字开始
     */
    public static final String WITH_NOT_NUMBERS = "^[^0-9]*";
    /**
     * 空格
     */
    public static final String SPACES = "\\s+";
    /**
     * 空格冒号空格
     */
    public static final String SPACES_COLON_SPACE = "\\s+:\\s";
    /**
     * 用于检查十六进制字符串的有效性
     */
    public static final String VALID_HEX = "[0-9a-fA-F]+";
    /**
     * 单个中文汉字 参照维基百科汉字Unicode范围(<a href=
     * "https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97">https://zh.wikipedia.org/wiki/%E6%B1%89%E5%AD%97</a> 页面右侧)
     */
    public static final String CHINESE = "[\u2E80-\u2EFF\u2F00-\u2FDF\u31C0-\u31EF\u3400-\u4DBF\u4E00-\u9FFF\uF900-\uFAFF\uD840\uDC00-\uD869\uDEDF\uD869\uDF00-\uD86D\uDF3F\uD86D\uDF40-\uD86E\uDC1F\uD86E\uDC20-\uD873\uDEAF\uD87E\uDC00-\uD87E\uDE1F]";
    /**
     * 中文汉字
     */
    public static final String CHINESES = CHINESE + Symbol.PLUS;
    /**
     * 分组
     */
    public static final String GROUP_VAR = "\\$(\\d+)";
    /**
     * 快速区分IP地址和主机名
     */
    public static final String IP_ADDRESS = "([0-9a-fA-F]*:[0-9a-fA-F:.]*)|([\\d.]+)";
    /**
     * IP v4 采用分组方式便于解析地址的每一个段
     */
    public static final String IPV4 = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)$";
    /**
     * IP v6
     */
    public static final String IPV6 = "(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]+|::(ffff(:0{1,4})?:)?((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9]))";
    /**
     * 货币
     */
    public static final String MONEY = "^(\\d+(?:\\.\\d+)?)$";
    /**
     * 邮件，符合RFC 5322规范，注意email 要宽松一点 正则来自：<a href="http://emailregex.com/">http://emailregex.com/</a> 参考：
     * <ul>
     * <li>https://stackoverflow.com/questions/386294/what-is-the-maximum-length-of-a-valid-email-address/44317754</li>
     * <li>https://stackoverflow.com/questions/201323/how-can-i-validate-an-email-address-using-a-regular-expression</li>
     * </ul>
     */
    public static final String EMAIL = "(?:[a-z0-9\\u4e00-\\u9fa5!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9\\u4e00-\\u9fa5!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9\\u4e00-\\u9fa5](?:[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5])?\\.)+[a-z0-9\\u4e00-\\u9fa5](?:[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9\\u4e00-\\u9fa5-]*[a-z0-9\\u4e00-\\u9fa5]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";

    /**
     * 移动电话 eg: 中国大陆： +86 180 4953 1399，2位区域码标示+13位数字
     */
    public static final String MOBILE = "(?:0|86|\\+86)?1[3-9]\\d{9}";
    public static final Pattern MOBILE_PATTERN = Pattern.compile(Regex.MOBILE);
    /**
     * 中国香港移动电话 eg: 中国香港： +852 5100 4810， 三位区域码+10位数字, 中国香港手机号码8位数
     */
    public static final String MOBILE_HK = "(?:0|852|\\+852)?\\d{8}";
    /**
     * 中国台湾移动电话 eg: 中国台湾： +886 09 60 000000， 三位区域码+号码以数字09开头 + 8位数字, 中国台湾手机号码10位数
     */
    public static final String MOBILE_TW = "(?:0|886|\\+886)?(?:|-)09\\d{8}";
    /**
     * 中国澳门移动电话 eg: 中国澳门： +853 68 00000， 三位区域码 +号码以数字6开头 + 7位数字, 中国台湾手机号码8位数
     */
    public static final String MOBILE_MO = "(?:0|853|\\+853)?(?:|-)6\\d{7}";
    /**
     * 座机号码
     */
    public static final String TEL = "(010|02\\d|0[3-9]\\d{2})-?(\\d{6,8})";
    /**
     * 座机号码+400+800电话
     */
    public static final String TEL_400_800 = "0\\d{2,3}[\\- ]?[0-9]\\d{6,7}|[48]00[\\- ]?[0-9]\\d{2}[\\- ]?\\d{4}";
    /**
     * 18位身份证号码
     */
    public static final String CITIZEN_ID = "[1-9]\\d{5}[1-2]\\d{3}((0\\d)|(1[0-2]))(([012]\\d)|3[0-1])\\d{3}(\\d|X|x)";
    /**
     * 邮编，兼容港澳台
     */
    public static final String ZIP_CODE = "^(0[1-7]|1[0-356]|2[0-7]|3[0-6]|4[0-7]|5[0-7]|6[0-7]|7[0-5]|8[0-9]|9[0-8])\\d{4}|99907[78]$";
    /**
     * 生日
     */
    public static final String BIRTHDAY = "^(\\d{2,4})([/\\-.年]?)(\\d{1,2})([/\\-.月]?)(\\d{1,2})日?$";
    /**
     * URI 定义见：<a href=
     * "https://www.ietf.org/rfc/rfc3986.html#appendix-B">https://www.ietf.org/rfc/rfc3986.html#appendix-B</a>
     */
    public static final String URI = "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
    /**
     * URL
     */
    public static final String URL = "[a-zA-Z]+://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]";
    /**
     * Protocol URL（来自：<a href="http://urlregex.com/">http://urlregex.com/</a>） 此正则同时支持FTP、File等协议的URL
     */
    public static final String URL_HTTP = "(https?|ftp|file)://[\\w-+&@#/%?=~_|!:,.;]*[\\w-+&@#/%=~_|]";
    /**
     * 中文字、英文字母、数字和下划线
     */
    public static final String GENERAL_WITH_CHINESE = "^[\u4E00-\u9FFF\\w]+$";
    /**
     * UUID
     */
    public static final String UUID = "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$";
    /**
     * 不带横线的UUID
     */
    public static final String UUID_SIMPLE = "^[0-9a-fA-F]{32}$";
    /**
     * 16进制字符串
     */
    public static final String HEX = "^[a-fA-F0-9]+$";
    /**
     * 时间正则
     */
    public static final String TIME = "\\d{1,2}[:时]\\d{1,2}([:分]\\d{1,2})?秒?";
    /**
     * 中国车牌号码（兼容新能源车牌）
     */
    public static final String PLATE_NUMBER = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[ABCDEFGHJK])|([ABCDEFGHJK]([A-HJ-NP-Z0-9])[0-9]{4})))|"
            + "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领]\\d{3}\\d{1,3}[领])|"
            + "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$";
    /**
     * 统一社会信用代码
     *
     * <pre>
     * 第一部分：登记管理部门代码1位 (数字或大写英文字母)
     * 第二部分：机构类别代码1位 (数字或大写英文字母)
     * 第三部分：登记管理机关行政区划码6位 (数字)
     * 第四部分：主体标识码（组织机构代码）9位 (数字或大写英文字母)
     * 第五部分：校验码1位 (数字或大写英文字母)
     * </pre>
     */
    public static final String CREDIT_CODE = "^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$";
    /**
     * 车架号（车辆识别代号由世界制造厂识别代号(WMI、车辆说明部分(VDS)车辆指示部分(VIS)三部分组成，共 17 位字码。） 别名：车辆识别代号、车辆识别码、车架号、十七位码 标准号：GB 16735-2019
     * 标准官方地址：https://openstd.samr.gov.cn/bzgk/gb/newGbInfo?hcno=E2EBF667F8C032B1EDFD6DF9C1114E02 对年产量大于或等于1 000
     * 辆的完整车辆和/或非完整车辆制造厂：
     *
     * <pre>
     *   第一部分为世界制造厂识别代号(WMI)，3位
     *   第二部分为车辆说明部分(VDS)，     6位
     *   第三部分为车辆指示部分(VIS)，     8位
     * </pre>
     * <p>
     * 对年产量小于 1 000 辆的完整车辆和/或非完整车辆制造厂：
     *
     * <pre>
     *   第一部分为世界制造广识别代号(WMI),3位;
     *   第二部分为车辆说明部分(VDS)，6位;
     *   第三部分的三、四、五位与第一部分的三位字码起构成世界制造厂识别代号(WMI),其余五位为车辆指示部分(VIS)，8位。
     * </pre>
     *
     * <pre>
     *   eg:LDC613P23A1305189
     *   eg:LSJA24U62JG269225
     *   eg:LBV5S3102ESJ25655
     * </pre>
     */
    public static final String CAR_VIN = "^[A-HJ-NPR-Z0-9]{8}[X0-9]([A-HJ-NPR-Z0-9]{3}\\d{5}|[A-HJ-NPR-Z0-9]{5}\\d{3})$";
    /**
     * 驾驶证 别名：驾驶证档案编号、行驶证编号 eg:430101758218 12位数字字符串 仅限：中国驾驶证档案编号
     */
    public static final String CAR_DRIVING_LICENCE = "^[0-9]{12}$";
    /**
     * 中文姓名 维吾尔族姓名里面的点是 · 输入法中文状态下，键盘左上角数字1前面的那个符号； 错误字符：{@code ．.。．.} 正确维吾尔族姓名：
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
     * 总结中文姓名：2-60位，只能是中文和维吾尔族的点· 放宽汉字范围：[CJK统一汉字, CJK统一汉字扩展A区]如生僻姓名 刘欣䶮(yǎn)
     * 汉字范围见：https://www.cnblogs.com/animalize/p/5432864.html
     */
    public static final String CHINESE_NAME = "^[\u3400-\u9FFF·]{2,60}$";
    /**
     * MAC地址正则
     */
    public static String MAC_ADDRESS = "((?:[a-fA-F0-9]{1,2}[:-]){5}[a-fA-F0-9]{1,2})|((?:[a-fA-F0-9]{1,4}[.]){2}[a-fA-F0-9]{1,4})|[a-fA-F0-9]{12}|0x(\\d{12}).+ETHER";

}
