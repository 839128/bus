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
 * 微信支付 v3 接口-分账接口
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ProfitSharingApi implements Matcher {

    /**
     * 请求分账
     */
    PROFIT_SHARING_ORDERS("/v3/profitsharing/orders", "请求分账"),

    /**
     * 查询分账结果
     */
    PROFIT_SHARING_ORDERS_QUERY("/v3/profitsharing/orders/%s", "查询分账结果"),

    /**
     * 请求分账回退
     */
    PROFIT_SHARING_RETURN_ORDERS("/v3/profitsharing/return-orders", "请求分账回退"),

    /**
     * 查询分账回退结果
     */
    PROFIT_SHARING_RETURN_ORDERS_QUERY("/v3/profitsharing/return-orders/%s", "查询分账回退结果"),

    /**
     * 解冻剩余资金
     */
    PROFIT_SHARING_UNFREEZE("/v3/profitsharing/orders/unfreeze", "解冻剩余资金"),

    /**
     * 查询剩余待分金额
     */
    PROFIT_SHARING_UNFREEZE_QUERY("/v3/profitsharing/transactions/%s/amounts", "查询剩余待分金额"),

    /**
     * 查询最大分账比例
     */
    PROFIT_SHARING_MERCHANT_CONFIGS("/v3/profitsharing/merchant-configs/%s", "查询最大分账比例"),

    /**
     * 添加分账接收方
     */
    PROFIT_SHARING_RECEIVERS_ADD("/v3/profitsharing/receivers/add", "添加分账接收方"),

    /**
     * 删除分账接收方
     */
    PROFIT_SHARING_RECEIVERS_DELETE("/v3/profitsharing/receivers/delete", "删除分账接收方"),

    /**
     * 申请分账账单
     */
    PROFIT_SHARING_BILLS("/v3/profitsharing/bills", "申请分账账单");

    /**
     * 接口方法
     */
    private final String method;

    /**
     * 接口描述
     */
    private final String desc;

    ProfitSharingApi(String method, String desc) {
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
