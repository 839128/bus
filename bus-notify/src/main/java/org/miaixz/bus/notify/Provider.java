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
package org.miaixz.bus.notify;

import java.util.Collections;
import java.util.List;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.notify.magic.Material;

/**
 * 通知器,用于发送通知,如: 短信,邮件,语音,微信等
 *
 * @author Justubborn
 * @since Java 17+
 */
public interface Provider<T extends Material> extends org.miaixz.bus.core.Provider {

    /**
     * 指定模版{@link Material}并发送 注意:不同等服务商使用的模版实现不同
     *
     * @param entity 通知内容
     * @return 发送结果
     */
    Message send(T entity);

    /**
     * 发送通知
     *
     * @param entity 通知内容
     * @param mobile 手机号列表
     * @return 发送结果
     */
    Message send(T entity, List<String> mobile);

    /**
     * 发送通知
     *
     * @param entity 通知内容
     * @param mobile 手机号列表
     * @return 发送结果
     */
    default Message send(T entity, String mobile) {
        return send(entity, Collections.singletonList(mobile));
    }

    /**
     * 发送通知
     *
     * @param entity 通知内容
     * @param mobile 手机号列表
     * @return 发送结果
     */
    default Message send(T entity, String... mobile) {
        return send(entity, ListKit.of(mobile));
    }

    @Override
    default Object type() {
        return EnumValue.Povider.NOTIFY;
    }

}
