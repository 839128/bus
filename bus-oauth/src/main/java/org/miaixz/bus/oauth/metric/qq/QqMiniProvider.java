/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org justauth.cn and other contributors.        ~
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
package org.miaixz.bus.oauth.metric.qq;

import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.AuthorizedException;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.AccToken;
import org.miaixz.bus.oauth.magic.Callback;
import org.miaixz.bus.oauth.magic.Material;
import org.miaixz.bus.oauth.metric.AbstractProvider;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * QQ 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class QqMiniProvider extends AbstractProvider {

    public QqMiniProvider(Context context) {
        super(context, Registry.QQ_MINI);
    }

    public QqMiniProvider(Context context, ExtendCache cache) {
        super(context, Registry.QQ_MINI, cache);
    }

    @Override
    public AccToken getAccessToken(Callback authCallback) {
        // 参见 https://q.qq.com/wiki/develop/miniprogram/server/open_port/port_login.html#code2session 文档
        // 使用 code 获取对应的 openId、unionId 等字段
        String response = Httpx.get(accessTokenUrl(authCallback.getCode()));

        JSCode2SessionResponse accessTokenObject = JSONObject.parseObject(response, JSCode2SessionResponse.class);
        assert accessTokenObject != null;
        checkResponse(accessTokenObject);
        // 拼装结果
        return AccToken.builder().openId(accessTokenObject.getOpenid()).unionId(accessTokenObject.getUnionId())
                .accessToken(accessTokenObject.getSessionKey()).build();
    }

    @Override
    public Material getUserInfo(AccToken authToken) {
        // 参见 https://q.qq.com/wiki/develop/game/API/open-port/user-info.html#qq-getuserinfo 文档
        // 如果需要用户信息，需要在小程序调用函数后传给后端
        return Material.builder().username("").nickname("").avatar("").uuid(authToken.getOpenId()).token(authToken)
                .source(complex.toString()).build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param response 请求响应内容
     */
    private void checkResponse(JSCode2SessionResponse response) {
        if (!Symbol.ZERO.equals(response.getErrorCode())) {
            throw new AuthorizedException(response.getErrorCode(), response.getErrorMsg());
        }
    }

    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromUrl(complex.accessToken()).queryParam("appid", context.getAppKey())
                .queryParam("secret", context.getAppSecret()).queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code").build();
    }

    @Data
    private static class JSCode2SessionResponse {

        @JSONField(name = "errcode")
        private String errorCode;
        @JSONField(name = "errmsg")
        private String errorMsg;
        @JSONField(name = "session_key")
        private String sessionKey;
        private String openid;
        @JSONField(name = "unionid")
        private String unionId;

    }

}