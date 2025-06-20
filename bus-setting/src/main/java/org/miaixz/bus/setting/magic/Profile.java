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
package org.miaixz.bus.setting.magic;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.setting.Setting;

/**
 * Profile可以让我们定义一系列的配置信息，然后指定其激活条件。 此类中我们规范一套规则如下：
 * 默认的，我们读取${classpath}/default下的配置文件(*.setting文件)，当调用setProfile方法时，指定一个profile，即可读取其目录下的配置文件。
 * 比如我们定义几个profile：test，develop，production，分别代表测试环境、开发环境和线上环境，我希望读取数据库配置文件db.setting，那么：
 * <ol>
 * <li>test = ${classpath}/test/db.setting</li>
 * <li>develop = ${classpath}/develop/db.setting</li>
 * <li>production = ${classpath}/production/db.setting</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Profile implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852228167252L;

    /**
     * 配置文件缓存
     */
    private final Map<String, Setting> settingMap = new ConcurrentHashMap<>();
    /**
     * 条件
     */
    private String profile;
    /**
     * 编码
     */
    private Charset charset;
    /**
     * 是否使用变量
     */
    private boolean useVar;

    /**
     * 默认构造，环境使用默认的：default，编码UTF-8，不使用变量
     */
    public Profile() {
        this("default");
    }

    /**
     * 构造，编码UTF-8，不使用变量
     *
     * @param profile 环境
     */
    public Profile(final String profile) {
        this(profile, Setting.DEFAULT_CHARSET, false);
    }

    /**
     * 构造
     *
     * @param profile 环境
     * @param charset 编码
     * @param useVar  是否使用变量
     */
    public Profile(final String profile, final Charset charset, final boolean useVar) {
        this.profile = profile;
        this.charset = charset;
        this.useVar = useVar;
    }

    /**
     * 获取当前环境下的配置文件
     *
     * @param name 文件名，如果没有扩展名，默认为.setting
     * @return 当前环境下配置文件
     */
    public Setting getSetting(final String name) {
        final String nameForProfile = fixNameForProfile(name);
        Setting setting = settingMap.get(nameForProfile);
        if (null == setting) {
            setting = new Setting(nameForProfile, this.charset, this.useVar);
            settingMap.put(nameForProfile, setting);
        }
        return setting;
    }

    /**
     * 设置环境
     *
     * @param profile 环境
     * @return 自身
     */
    public Profile setProfile(final String profile) {
        this.profile = profile;
        return this;
    }

    /**
     * 设置编码
     *
     * @param charset 编码
     * @return 自身
     */
    public Profile setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 设置是否使用变量
     *
     * @param useVar 变量
     * @return 自身
     */
    public Profile setUseVar(final boolean useVar) {
        this.useVar = useVar;
        return this;
    }

    /**
     * 清空所有环境的配置文件
     *
     * @return 自身
     */
    public Profile clear() {
        this.settingMap.clear();
        return this;
    }

    /**
     * 修正文件名
     *
     * @param name 文件名
     * @return 修正后的文件名
     */
    private String fixNameForProfile(final String name) {
        Assert.notBlank(name, "Setting name must be not blank !");
        final String actralProfile = StringKit.toStringOrEmpty(this.profile);
        if (!name.contains(Symbol.DOT)) {
            return StringKit.format("{}/{}.setting", actralProfile, name);
        }
        return StringKit.format("{}/{}", actralProfile, name);
    }

}
