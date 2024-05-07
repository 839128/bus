/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org justauth and other contributors.           *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.oauth.metric.wechat.mp;

import com.alibaba.fastjson.JSONObject;
import org.miaixz.bus.cache.metric.ExtendCache;
import org.miaixz.bus.core.exception.AuthorizedException;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.oauth.Builder;
import org.miaixz.bus.oauth.Context;
import org.miaixz.bus.oauth.Registry;
import org.miaixz.bus.oauth.magic.*;
import org.miaixz.bus.oauth.metric.DefaultProvider;

/**
 * 微信公众平台 登录
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeChatMpProvider extends DefaultProvider {

    public WeChatMpProvider(Context context) {
        super(context, Registry.WECHAT_MP);
    }

    public WeChatMpProvider(Context context, ExtendCache authorizeCache) {
        super(context, Registry.WECHAT_MP, authorizeCache);
    }

    /**
     * 微信的特殊性，此时返回的信息同时包含 openid 和 access_token
     *
     * @param authCallback 回调返回的参数
     * @return 所有信息
     */
    @Override
    protected AccToken getAccessToken(Callback authCallback) {
        return this.getToken(accessTokenUrl(authCallback.getCode()));
    }

    @Override
    protected Property getUserInfo(AccToken accToken) {
        String openId = accToken.getOpenId();

        String scope = accToken.getScope();
        if (!StringKit.isEmpty(scope) && !scope.contains("snsapi_userinfo")) {
            return Property.builder()
                    .rawJson(JSONObject.parseObject(JSONObject.toJSONString(accToken)))
                    .uuid(openId)
                    .snapshotUser(accToken.isSnapshotUser())
                    .token(accToken)
                    .source(complex.toString())
                    .build();
        }

        String response = doGetUserInfo(accToken);
        JSONObject object = JSONObject.parseObject(response);

        this.checkResponse(object);
        String location = String.format("%s-%s-%s", object.getString("country"), object.getString("province"), object.getString("city"));

        if (object.containsKey("unionid")) {
            accToken.setUnionId(object.getString("unionid"));
        }
        return Property.builder()
                .rawJson(object)
                .username(object.getString("nickname"))
                .nickname(object.getString("nickname"))
                .avatar(object.getString("headimgurl"))
                .location(location)
                .uuid(openId)
                .snapshotUser(accToken.isSnapshotUser())
                .gender(Builder.getWechatRealGender(object.getString("sex")))
                .token(accToken)
                .source(complex.toString())
                .build();
    }

    @Override
    public Message refresh(AccToken oldToken) {
        return Message.builder()
                .errcode(ErrorCode.SUCCESS.getCode())
                .data(this.getToken(refreshTokenUrl(oldToken.getRefreshToken())))
                .build();
    }

    /**
     * 检查响应内容是否正确
     *
     * @param object 请求响应内容
     */
    private void checkResponse(JSONObject object) {
        if (object.containsKey("errcode")) {
            throw new AuthorizedException(object.getString("errcode"), object.getString("errmsg"));
        }
    }

    /**
     * 获取token，适用于获取access_token和刷新token
     *
     * @param accessTokenUrl 实际请求token的地址
     * @return token对象
     */
    private AccToken getToken(String accessTokenUrl) {
        String response = Httpx.get(accessTokenUrl);
        JSONObject accessTokenObject = JSONObject.parseObject(response);

        this.checkResponse(accessTokenObject);

        return AccToken.builder()
                .accessToken(accessTokenObject.getString("access_token"))
                .refreshToken(accessTokenObject.getString("refresh_token"))
                .expireIn(accessTokenObject.getIntValue("expires_in"))
                .openId(accessTokenObject.getString("openid"))
                .scope(accessTokenObject.getString("scope"))
                .snapshotUser(accessTokenObject.getIntValue("is_snapshotuser") == 1)
                .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     */
    @Override
    public String authorize(String state) {
        return Builder.fromBaseUrl(complex.authorize())
                .queryParam("appid", context.getAppKey())
                .queryParam("redirect_uri", Builder.urlEncode(context.getRedirectUri()))
                .queryParam("response_type", "code")
                .queryParam("scope", this.getScopes(",", false, this.getDefaultScopes(WechatMpScope.values())))
                .queryParam("state", getRealState(state).concat("#wechat_redirect"))
                .build();
    }

    /**
     * 返回获取accessToken的url
     *
     * @param code 授权码
     * @return 返回获取accessToken的url
     */
    @Override
    protected String accessTokenUrl(String code) {
        return Builder.fromBaseUrl(complex.accessToken())
                .queryParam("appid", context.getAppKey())
                .queryParam("secret", context.getAppSecret())
                .queryParam("code", code)
                .queryParam("grant_type", "authorization_code")
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param accToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AccToken accToken) {
        return Builder.fromBaseUrl(complex.userInfo())
                .queryParam("access_token", accToken.getAccessToken())
                .queryParam("openid", accToken.getOpenId())
                .queryParam("lang", "zh_CN")
                .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param refreshToken getAccessToken方法返回的refreshToken
     * @return 返回获取userInfo的url
     */
    @Override
    protected String refreshTokenUrl(String refreshToken) {
        return Builder.fromBaseUrl(complex.refresh())
                .queryParam("appid", context.getAppKey())
                .queryParam("grant_type", "refresh_token")
                .queryParam("refresh_token", refreshToken)
                .build();
    }
}