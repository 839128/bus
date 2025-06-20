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
package org.miaixz.bus.core.net.url;

import java.io.Serial;
import java.net.*;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;

/**
 * URL 生成器，格式形如：
 * 
 * <pre>
 * [scheme:]scheme-specific-part[#fragment]
 * [scheme:][//authority][path][?query][#fragment]
 * [scheme:][//host:port][path][?query][#fragment]
 * </pre>
 *
 * @author Kimi Liu
 * @see <a href="https://en.wikipedia.org/wiki/Uniform_Resource_Identifier">Uniform Resource Identifier</a>
 * @since Java 17+
 */
public final class UrlBuilder implements Builder<String> {

    @Serial
    private static final long serialVersionUID = 2852231355809L;

    /**
     * 协议，例如http
     */
    private String scheme;
    /**
     * 主机，例如127.0.0.1
     */
    private String host;
    /**
     * 端口，默认-1
     */
    private int port = -1;
    /**
     * 路径，例如/aa/bb/cc
     */
    private UrlPath path;
    /**
     * 查询语句，例如a=1&amp;b=2
     */
    private UrlQuery query;
    /**
     * 标识符，例如#后边的部分
     */
    private String fragment;
    /**
     * 编码，用于URLEncode和URLDecode
     */
    private java.nio.charset.Charset charset;

    /**
     * 构造
     */
    public UrlBuilder() {
        this.charset = Charset.UTF_8;
    }

    /**
     * 构造
     *
     * @param scheme   协议，默认http
     * @param host     主机，例如127.0.0.1
     * @param port     端口，-1表示默认端口
     * @param path     路径，例如/aa/bb/cc
     * @param query    查询，例如a=1&amp;b=2
     * @param fragment 标识符例如#后边的部分
     * @param charset  编码，用于URLEncode和URLDecode，{@code null}表示不编码
     */
    public UrlBuilder(final String scheme, final String host, final int port, final UrlPath path, final UrlQuery query,
            final String fragment, final java.nio.charset.Charset charset) {
        this.charset = charset;
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
        this.setFragment(fragment);
    }

    /**
     * 使用UrlBuilder构建UrlBuilder
     *
     * @param builder {@code UrlBuilder}
     * @return UrlBuilder
     */
    public static UrlBuilder of(final UrlBuilder builder) {
        return of(builder.getScheme(), builder.getHost(), builder.getPort(), builder.getPaths(), builder.getQuerys(),
                builder.getFragment(), builder.getCharset());
    }

    /**
     * 使用URI构建UrlBuilder
     *
     * @param uri     URI
     * @param charset 编码，用于URLEncode和URLDecode
     * @return UrlBuilder
     */
    public static UrlBuilder of(final URI uri, final java.nio.charset.Charset charset) {
        return of(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getRawQuery(), uri.getFragment(),
                charset);
    }

    /**
     * 使用URL字符串构建UrlBuilder，当传入的URL没有协议时，按照http协议对待 此方法不对URL编码
     *
     * @param httpUrl URL字符串
     * @return UrlBuilder
     */
    public static UrlBuilder ofHttpWithoutEncode(final String httpUrl) {
        return ofHttp(httpUrl, null);
    }

    /**
     * 使用URL字符串构建UrlBuilder，当传入的URL没有协议时，按照http协议对待，编码默认使用UTF-8
     *
     * @param httpUrl URL字符串
     * @return UrlBuilder
     */
    public static UrlBuilder ofHttp(final String httpUrl) {
        return ofHttp(httpUrl, Charset.UTF_8);
    }

    /**
     * 使用URL字符串构建UrlBuilder，当传入的URL没有协议时，按照http协议对待。
     * <ul>
     * <li>如果url用户传入的URL没有做编码，则charset设置为{@code null}，此时URL不会解码，在build时也不会编码。</li>
     * <li>如果url已经编码，或部分编码，则需要设置charset，此时URL会解码编码后的参数，在build时也会编码。</li>
     * <li>如果url未编码，且存在歧义字符串，则需要设置charset为{@code null}，并调用{@link #setCharset(java.nio.charset.Charset)}在build时编码URL。</li>
     * </ul>
     *
     * @param httpUrl URL字符串
     * @param charset 编码，用于URLEncode和URLDecode，如果为{@code null}，则不对传入的URL解码
     * @return UrlBuilder
     */
    public static UrlBuilder ofHttp(String httpUrl, final java.nio.charset.Charset charset) {
        Assert.notBlank(httpUrl, "Url must be not blank!");

        httpUrl = StringKit.trimPrefix(httpUrl);
        if (!StringKit.startWithAnyIgnoreCase(httpUrl, "http://", "https://")) {
            httpUrl = "http://" + httpUrl;
        }

        return of(UrlKit.toUrlForHttp(httpUrl), charset);
    }

    /**
     * 使用URL字符串构建UrlBuilder，默认使用UTF-8编码 注意：此方法如果提供的URL为非网络协议，自动尝试使用文件协议
     *
     * @param url URL字符串
     * @return UrlBuilder
     */
    public static UrlBuilder of(final String url) {
        return of(url, Charset.UTF_8);
    }

    /**
     * 使用URL字符串构建UrlBuilder，规则如下：
     * <ul>
     * <li>如果url用户传入的URL没有做编码，则charset设置为{@code null}，此时URL不会解码，在build时也不会编码。</li>
     * <li>如果url已经编码，或部分编码，则需要设置charset，此时URL会解码编码后的参数，在build时也会编码。</li>
     * <li>如果url未编码，且存在歧义字符串，则需要设置charset为{@code null}，并调用{@link #setCharset(java.nio.charset.Charset)}在build时编码URL。</li>
     * </ul>
     *
     * @param url     URL字符串
     * @param charset 编码，用于URLEncode和URLDecode
     * @return UrlBuilder
     */
    public static UrlBuilder of(final String url, final java.nio.charset.Charset charset) {
        Assert.notBlank(url, "Url must be not blank!");
        return of(UrlKit.url(StringKit.trim(url)), charset);
    }

    /**
     * 使用URL构建UrlBuilder
     *
     * @param url     URL
     * @param charset 编码，用于URLEncode和URLDecode，{@code null}表示不解码
     * @return UrlBuilder
     */
    public static UrlBuilder of(final URL url, final java.nio.charset.Charset charset) {
        return of(url.getProtocol(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef(),
                charset);
    }

    /**
     * 构建UrlBuilder
     *
     * @param scheme   协议，默认http
     * @param host     主机，例如127.0.0.1
     * @param port     端口，-1表示默认端口
     * @param path     路径，例如/aa/bb/cc
     * @param query    查询，例如a=1&amp;b=2
     * @param fragment 标识符例如#后边的部分
     * @param charset  编码，用于URLEncode和URLDecode
     * @return UrlBuilder
     */
    public static UrlBuilder of(final String scheme, final String host, final int port, final String path,
            final String query, final String fragment, final java.nio.charset.Charset charset) {
        return of(scheme, host, port, UrlPath.of(path, charset), UrlQuery.of(query, charset, false), fragment, charset);
    }

    /**
     * 构建UrlBuilder
     *
     * @param scheme   协议，默认http
     * @param host     主机，例如127.0.0.1
     * @param port     端口，-1表示默认端口
     * @param path     路径，例如/aa/bb/cc
     * @param query    查询，例如a=1&amp;b=2
     * @param fragment 标识符例如#后边的部分
     * @param charset  编码，用于URLEncode和URLDecode
     * @return UrlBuilder
     */
    public static UrlBuilder of(final String scheme, final String host, final int port, final UrlPath path,
            final UrlQuery query, final String fragment, final java.nio.charset.Charset charset) {
        return new UrlBuilder(scheme, host, port, path, query, fragment, charset);
    }

    /**
     * 创建空的UrlBuilder
     *
     * @return UrlBuilder
     */
    public static UrlBuilder of() {
        return new UrlBuilder();
    }

    /**
     * 获取协议，例如http
     *
     * @return 协议，例如http
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * 设置协议，例如http
     *
     * @param scheme 协议，例如http
     * @return this
     */
    public UrlBuilder setScheme(final String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * 获取协议，例如http，如果用户未定义协议，使用默认的http协议
     *
     * @return 协议，例如http
     */
    public String getSchemeWithDefault() {
        return StringKit.defaultIfEmpty(this.scheme, Protocol.HTTP.name);
    }

    /**
     * 获取 主机，例如127.0.0.1
     *
     * @return 主机，例如127.0.0.1
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置主机，例如127.0.0.1
     *
     * @param host 主机，例如127.0.0.1
     * @return this
     */
    public UrlBuilder setHost(final String host) {
        this.host = host;
        return this;
    }

    /**
     * 获取端口，默认-1
     *
     * @return 端口，默认-1
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置端口，默认-1
     *
     * @param port 端口，默认-1
     * @return this
     */
    public UrlBuilder setPort(final int port) {
        this.port = port;
        return this;
    }

    /**
     * 获取端口，如果未自定义返回协议默认端口
     *
     * @return 端口
     */
    public int getPortWithDefault() {
        int port = getPort();
        if (port <= 0) {
            port = toURL().getDefaultPort();
            return port;
        }
        return port;
    }

    /**
     * 获得authority部分
     *
     * @return authority部分
     */
    public String getAuthority() {
        return (port < 0) ? host : host + Symbol.COLON + port;
    }

    /**
     * 获取路径，例如/aa/bb/cc
     *
     * @return 路径，例如/aa/bb/cc
     */
    public UrlPath getPath() {
        return path;
    }

    /**
     * 设置路径，例如/aa/bb/cc，将覆盖之前所有的path相关设置
     *
     * @param path 路径，例如/aa/bb/cc
     * @return this
     */
    public UrlBuilder setPath(final UrlPath path) {
        this.path = path;
        return this;
    }

    /**
     * 获得路径，例如/aa/bb/cc
     *
     * @return 路径，例如/aa/bb/cc
     */
    public String getPaths() {
        return null == this.path ? Symbol.SLASH : this.path.build(charset);
    }

    /**
     * 增加路径，在现有路径基础上追加路径
     *
     * @param path 路径，例如aaa/bbb/ccc
     * @return this
     */
    public UrlBuilder addPath(final CharSequence path) {
        UrlPath.of(path, this.charset).getSegments().forEach(this::addPathSegment);
        return this;
    }

    /**
     * 是否path的末尾加 /
     *
     * @param withEngTag 是否path的末尾加 /
     * @return this
     */
    public UrlBuilder setWithEndTag(final boolean withEngTag) {
        if (null == this.path) {
            this.path = UrlPath.of();
        }

        this.path.setWithEndTag(withEngTag);
        return this;
    }

    /**
     * 增加路径节点，路径节点中的"/"会被转义为"%2F"
     *
     * @param segment 路径节点
     * @return this
     */
    public UrlBuilder addPathSegment(final CharSequence segment) {
        if (StringKit.isEmpty(segment)) {
            return this;
        }
        if (null == this.path) {
            this.path = new UrlPath();
        }
        this.path.add(segment);
        return this;
    }

    /**
     * 获取查询语句，例如a=1&amp;b=2 可能为{@code null}
     *
     * @return 查询语句，例如a=1&amp;b=2，可能为{@code null}
     */
    public UrlQuery getQuery() {
        return query;
    }

    /**
     * 设置查询语句，例如a=1&amp;b=2，将覆盖之前所有的query相关设置
     *
     * @param query 查询语句，例如a=1&amp;b=2
     * @return this
     */
    public UrlBuilder setQuery(final UrlQuery query) {
        this.query = query;
        return this;
    }

    /**
     * 获取查询语句，例如a=1&amp;b=2
     *
     * @return 查询语句，例如a=1&amp;b=2
     */
    public String getQuerys() {
        return null == this.query ? null : this.query.build(this.charset);
    }

    /**
     * 添加查询项，支持重复键，默认非严格模式
     *
     * @param key   键
     * @param value 值
     * @return this
     */
    public UrlBuilder addQuery(final String key, final Object value) {
        if (StringKit.isEmpty(key)) {
            return this;
        }

        if (this.query == null) {
            this.query = UrlQuery.of();
        }
        this.query.add(key, value);
        return this;
    }

    /**
     * 获取标识符，#后边的部分
     *
     * @return 标识符，例如#后边的部分
     */
    public String getFragment() {
        return fragment;
    }

    /**
     * 设置标识符，例如#后边的部分
     *
     * @param fragment 标识符，例如#后边的部分
     * @return this
     */
    public UrlBuilder setFragment(final String fragment) {
        if (StringKit.isEmpty(fragment)) {
            this.fragment = null;
        }
        this.fragment = StringKit.removePrefix(fragment, Symbol.HASH);
        return this;
    }

    /**
     * 获取标识符，#后边的部分
     *
     * @return 标识符，例如#后边的部分
     */
    public String getFragmentEncoded() {
        return RFC3986.FRAGMENT.encode(this.fragment, this.charset);
    }

    /**
     * 获取编码，用于URLEncode和URLDecode
     *
     * @return 编码
     */
    public java.nio.charset.Charset getCharset() {
        return charset;
    }

    /**
     * 设置编码，用于URLEncode和URLDecode
     *
     * @param charset 编码
     * @return this
     */
    public UrlBuilder setCharset(final java.nio.charset.Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 创建URL字符串
     *
     * @return URL字符串
     */
    @Override
    public String build() {
        return toURL().toString();
    }

    /**
     * 转换为{@link URL} 对象
     *
     * @return {@link URL}
     */
    public URL toURL() {
        return toURL(null);
    }

    /**
     * 转换为{@link URL} 对象
     *
     * @param handler {@link URLStreamHandler}，null表示默认
     * @return {@link URL}
     */
    public URL toURL(final URLStreamHandler handler) {
        final StringBuilder fileBuilder = new StringBuilder();

        // path
        fileBuilder.append(getPaths());

        // query
        final String query = getQuerys();
        if (StringKit.isNotBlank(query)) {
            fileBuilder.append(Symbol.C_QUESTION_MARK).append(query);
        }

        // fragment
        if (StringKit.isNotBlank(this.fragment)) {
            fileBuilder.append(Symbol.C_HASH).append(getFragmentEncoded());
        }

        try {
            return new URL(getSchemeWithDefault(), host, port, fileBuilder.toString(), handler);
        } catch (final MalformedURLException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 转换为URI
     *
     * @return URI
     */
    public URI toURI() {
        try {
            return toURL().toURI();
        } catch (final URISyntaxException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public String toString() {
        return build();
    }

}
