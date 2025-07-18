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
package org.miaixz.bus.pay.metric.wechat.api.v3;

import org.miaixz.bus.pay.Matcher;

/**
 * 微信支付 v3 接口-消费者投诉2.0接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ComplaintsApi implements Matcher {

    /**
     * 查询投诉单列表
     */
    COMPLAINTS_V2("/v3/merchant-service/complaints-v2", "查询投诉单列表"),

    /**
     * 查询投诉单详情
     */
    COMPLAINTS_DETAIL("/v3/merchant-service/complaints-v2/%s", "查询投诉单详情"),

    /**
     * 查询投诉协商历史
     */
    COMPLAINTS_NEGOTIATION_HISTORY("/v3/merchant-service/complaints-v2/%s/negotiation-historys", "查询投诉协商历史"),

    /**
     * 创建/查询/更新/删除投诉通知回调
     */
    COMPLAINTS_NOTIFICATION("/v3/merchant-service/complaint-notifications", "创建/查询/更新/删除投诉通知回调"),

    /**
     * 提交回复
     */
    COMPLAINTS_RESPONSE("/v3/merchant-service/complaints-v2/%s/response", "提交回复"),

    /**
     * 商户上传反馈图片
     */
    IMAGES_UPLOAD("/v3/merchant-service/images/upload", "商户上传反馈图片"),

    /**
     * 反馈处理完成
     */
    COMPLAINTS_COMPLETE("/v3/merchant-service/complaints-v2/%s/complete", "反馈处理完成");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    ComplaintsApi(String method, String desc) {
        this.method = method;
        this.desc = desc;
    }

    /**
     * 交易类型
     *
     * @return the string
     */
    @Override
    public String type() {
        return this.name();
    }

    /**
     * 类型描述
     *
     * @return the string
     */
    @Override
    public String desc() {
        return this.desc;
    }

    /**
     * 接口方法
     *
     * @return the string
     */
    @Override
    public String method() {
        return this.method;
    }

}
