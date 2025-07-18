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
package org.miaixz.bus.notify.metric.dingtalk;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.http.Httpx;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.notify.Context;
import org.miaixz.bus.notify.magic.ErrorCode;
import org.miaixz.bus.notify.metric.AbstractProvider;

import lombok.Setter;

/**
 * 钉钉通知
 *
 * @author Justubborn
 * @since Java 17+
 */
@Setter
public class DingTalkProvider extends AbstractProvider<DingTalkMaterial, Context> {

    private AtomicReference<String> accessToken = new AtomicReference<>();
    private long refreshTokenTime;
    private long tokenTimeOut = Duration.ofSeconds(7000).toMillis();

    public DingTalkProvider(Context context) {
        super(context);
    }

    @Override
    public Message send(DingTalkMaterial entity) {
        Map<String, String> bodys = new HashMap<>();
        bodys.put("access_token", entity.getToken());
        bodys.put("agent_id", entity.getAgentId());
        bodys.put("msg", entity.getMsg());
        if (StringKit.isNotBlank(entity.getUserIdList())) {
            bodys.put("userid_list", entity.getUserIdList());
        }
        if (StringKit.isNotBlank(entity.getDeptIdList())) {
            bodys.put("dept_id_list", entity.getDeptIdList());
        }
        bodys.put("to_all_user", String.valueOf(entity.isToAllUser()));
        String response = Httpx.post(this.getUrl(entity), bodys);
        String errcode = JsonKit.getValue(response, "errcode");
        return Message.builder()
                .errcode(String.valueOf(HTTP.HTTP_OK).equals(errcode) ? ErrorCode._SUCCESS.getKey() : errcode)
                .errmsg(JsonKit.getValue(response, "errmsg")).build();
    }

    private String getToken(String url) {
        if (System.currentTimeMillis() - refreshTokenTime > tokenTimeOut || null == accessToken.get()) {
            return requestToken(url);
        }
        return accessToken.get();
    }

    /**
     * 获取钉钉token
     *
     * @return 结果
     */
    private String requestToken(String url) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("corpid", context.getAppKey());
        paramMap.put("corpsecret", context.getAppSecret());
        String response = Httpx.get(url, paramMap);
        String errcode = JsonKit.getValue(response, "errcode");
        if (String.valueOf(HTTP.HTTP_OK).equals(errcode)) {
            String access_token = JsonKit.getValue(response, "access_token");
            refreshTokenTime = System.currentTimeMillis();
            accessToken.set(access_token);
            return access_token;
        }

        Logger.error("获取钉钉token失败：{}", JsonKit.getValue(response, "errmsg"));
        return null;
    }

}
