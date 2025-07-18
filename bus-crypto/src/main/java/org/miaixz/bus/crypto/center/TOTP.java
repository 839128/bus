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
package org.miaixz.bus.crypto.center;

import java.time.Duration;
import java.time.Instant;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * time-based one-time passwords (TOTP) 基于时间戳算法的一次性密码生成器
 *
 * <p>
 * 规范见：<a href="https://tools.ietf.org/html/rfc6238">RFC&nbsp;6238</a>
 * </p>
 *
 *
 * <p>
 * 时间同步，基于客户端的动态口令和动态口令验证服务器的时间比对，一般每30秒产生一个新口令， 要求客户端和服务器能够十分精确的保持正确的时钟，客户端和服务端基于时间计算的动态口令才能一致。
 * </p>
 * <p>
 * 参考：https://github.com/jchambers/java-otp
 * </p>
 *
 * <p>
 * OTP基于具有时间戳计数器的OTP。 通过定义纪元（T0）的开始并以时间间隔（TI）为单位计数，将当前时间戳变为整数时间计数器（TC）。 例如： TC = floor, TOTP = HOTP（SecretKey，TC），
 * TOTP-Value = TOTP mod 10d，其中d是一次性密码的所需位数。 像google auth的二步认证使用了这种方式。
 * </p>
 *
 * <p>
 * 认证过程 生成二维码,带有otpauth链接的google地址 生成公用密钥 返回给app，同时用户户和服务名也会返回,这时密钥是被base32加密过的,app存储,以后用这个密钥来生成6位校验码
 * 服务端同时存储这个密钥和用户名，你可以把用户名当key，把密钥当value进行存储 app每30秒生成一个6位校验码,用户使用这个码来网站进行登陆 服务器使用存储的密钥+fmac算法生成6位随机数,与客户端传来的数进行对比
 * 两个码相等,授权成功,反之,失败.（注意，服务端可以根据当前登陆的用户名拿到它的密钥，有了密钥，再进行totp的算法生成校验码）
 * </p>
 *
 * <p>
 * 登陆的过程整理 用户和密码先登陆 session里存储了用户名等信息 产生二维码及密钥，密钥存储到服务器的k/v介质里，k使用session里的用户名，v使用刚才的密钥 客户使用app扫二维码，产生新的6位数字
 * 客户在用户名和密码登陆后，进行验证码页面，输入刚才的6位数字 提交到服务端，服务端根据用户名取出对应的密钥，然后使用totp算法生成6位数字 如果服务端与客户端数字相同，表示登陆成功！
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TOTP extends HOTP {

    private final Duration timeStep;

    /**
     * 构造，使用默认HMAC算法(HmacSHA1)
     *
     * @param key 共享密码，RFC 4226要求最少128位
     */
    public TOTP(final byte[] key) {
        this(Duration.ofSeconds(30), key);
    }

    /**
     * 构造，使用默认HMAC算法(HmacSHA1)
     *
     * @param timeStep 日期步进 用于生成移动因子（moving factor）,默认步进 (30秒)
     * @param key      共享密码，RFC 4226要求最少128位
     */
    public TOTP(final Duration timeStep, final byte[] key) {
        this(timeStep, DEFAULT_PASSWORD_LENGTH, key);
    }

    /**
     * 构造，使用默认HMAC算法(HmacSHA1)
     *
     * @param timeStep       日期步进，用于生成移动因子（moving factor）
     * @param passwordLength 密码长度，可以是6,7,8
     * @param key            共享密码，RFC 4226要求最少128位
     */
    public TOTP(final Duration timeStep, final int passwordLength, final byte[] key) {
        this(timeStep, passwordLength, Algorithm.HMACSHA1, key);
    }

    /**
     * 构造
     *
     * @param timeStep       日期步进，用于生成移动因子（moving factor）
     * @param passwordLength 密码长度，可以是6,7,8
     * @param algorithm      HMAC算法枚举
     * @param key            共享密码，RFC 4226要求最少128位
     */
    public TOTP(final Duration timeStep, final int passwordLength, final Algorithm algorithm, final byte[] key) {
        super(passwordLength, algorithm, key);
        this.timeStep = timeStep;
    }

    /**
     * 生成谷歌认证器的字符串（扫码字符串） 基于时间的，计数器不适合
     *
     * @param account  账户名。
     * @param numBytes 将生成的种子字节数量。
     * @return 共享密钥
     */
    public static String generateGoogleSecretKey(final String account, final int numBytes) {
        return StringKit.format("otpauth://totp/{}?secret={}", account, generateSecretKey(numBytes));
    }

    /**
     * 使用给定的时间戳生成一次性密码.
     *
     * @param timestamp 用于生成密码的时间戳
     * @return 一次性密码的int形式
     */
    public int generate(final Instant timestamp) {
        return this.generate(timestamp.toEpochMilli() / this.timeStep.toMillis());
    }

    /**
     * 用于验证code是否正确
     *
     * @param timestamp  验证时间戳
     * @param offsetSize 误差范围
     * @param code       code
     * @return 是否通过
     */
    public boolean validate(final Instant timestamp, final int offsetSize, final int code) {
        if (offsetSize == 0) {
            return generate(timestamp) == code;
        }
        for (int i = -offsetSize; i <= offsetSize; i++) {
            if (generate(timestamp.plus(getTimeStep().multipliedBy(i))) == code) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取步进
     *
     * @return 步进
     */
    public Duration getTimeStep() {
        return this.timeStep;
    }

}
