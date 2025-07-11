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
package org.miaixz.bus.http.plugin.httpz;

import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.bodys.FormBody;
import org.miaixz.bus.http.bodys.MultipartBody;
import org.miaixz.bus.http.bodys.RequestBody;

/**
 * POST请求处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PostRequest extends HttpRequest {

    public PostRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers,
            List<MultipartFile> list, String body, MultipartBody multipartBody, String id) {
        super(url, tag, params, headers, list, body, multipartBody, id);
    }

    public PostRequest(String url, Object tag, Map<String, String> params, Map<String, String> encoded,
            Map<String, String> headers, List<MultipartFile> list, String body, MultipartBody multipartBody,
            String id) {
        super(url, tag, params, encoded, headers, list, body, multipartBody, id);
    }

    @Override
    protected RequestBody buildRequestBody() {
        if (null != multipartBody) {
            return multipartBody;
        } else if (null != list && list.size() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MediaType.MULTIPART_FORM_DATA_TYPE);
            addParam(builder);
            list.forEach(file -> {
                RequestBody fileBody;
                if (null != file.file) {
                    fileBody = RequestBody.create(MediaType.APPLICATION_OCTET_STREAM_TYPE, file.file);
                } else if (null != file.in) {
                    fileBody = createRequestBody(MediaType.APPLICATION_OCTET_STREAM_TYPE, file.in);
                } else {
                    fileBody = RequestBody.create(MediaType.valueOf(ObjectKit
                            .defaultIfNull(FileKit.getMimeType(file.name), MediaType.APPLICATION_OCTET_STREAM)),
                            file.content);
                }
                builder.addFormDataPart(file.part, file.name, fileBody);
            });
            if (null != body && body.length() > 0) {
                builder.addPart(RequestBody.create(MediaType.MULTIPART_FORM_DATA_TYPE, body));
            }
            return builder.build();
        } else if (null != body && body.length() > 0) {
            MediaType mediaType;
            if (headers.containsKey(HTTP.CONTENT_TYPE)) {
                mediaType = MediaType.valueOf(headers.get(HTTP.CONTENT_TYPE));
            } else {
                mediaType = MediaType.TEXT_PLAIN_TYPE;
            }
            return RequestBody.create(mediaType, body);
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            addParam(builder);
            return builder.build();
        }
    }

    @Override
    protected Request buildRequest(RequestBody requestBody) {
        return builder.post(requestBody).build();
    }

    private void addParam(FormBody.Builder builder) {
        if (null != params) {
            params.forEach((k, v) -> builder.add(k, v));
        }
        if (null != encodedParams) {
            encodedParams.forEach((k, v) -> builder.addEncoded(k, v));
        }
    }

    private void addParam(MultipartBody.Builder builder) {
        if (null != params && !params.isEmpty()) {
            params.forEach((k, v) -> builder.addPart(
                    Headers.of(HTTP.CONTENT_DISPOSITION, "form-data; name=\"" + k + Symbol.DOUBLE_QUOTES),
                    RequestBody.create(null, v)));
        }
    }

}
