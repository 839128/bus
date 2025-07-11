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
package org.miaixz.bus.extra.template.provider.thymeleaf;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.template.Template;
import org.miaixz.bus.extra.template.TemplateConfig;
import org.miaixz.bus.extra.template.TemplateProvider;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.*;

/**
 * Thymeleaf模板引擎实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThymeleafProvider implements TemplateProvider {

    TemplateEngine engine;
    TemplateConfig config;

    /**
     * 默认构造
     */
    public ThymeleafProvider() {
        // SPI方式加载时检查库是否引入
        Assert.notNull(org.thymeleaf.TemplateEngine.class);
    }

    /**
     * 构造
     *
     * @param config 模板配置
     */
    public ThymeleafProvider(final TemplateConfig config) {
        init(config);
    }

    /**
     * 构造
     *
     * @param engine {@link TemplateEngine}
     */
    public ThymeleafProvider(final TemplateEngine engine) {
        init(engine);
    }

    /**
     * 创建引擎
     *
     * @param config 模板配置
     * @return {@link TemplateProvider}
     */
    private static TemplateEngine create(TemplateConfig config) {
        if (null == config) {
            config = new TemplateConfig();
        }

        final ITemplateResolver resolver;
        switch (config.getResourceMode()) {
        case CLASSPATH:
            final ClassLoaderTemplateResolver classLoaderResolver = new ClassLoaderTemplateResolver();
            classLoaderResolver.setCharacterEncoding(config.getCharsetString());
            classLoaderResolver.setTemplateMode(TemplateMode.HTML);
            classLoaderResolver.setPrefix(StringKit.addSuffixIfNot(config.getPath(), "/"));
            classLoaderResolver.setCacheable(config.isUseCache());
            resolver = classLoaderResolver;
            break;
        case FILE:
            final FileTemplateResolver fileResolver = new FileTemplateResolver();
            fileResolver.setCharacterEncoding(config.getCharsetString());
            fileResolver.setTemplateMode(TemplateMode.HTML);
            fileResolver.setPrefix(StringKit.addSuffixIfNot(config.getPath(), "/"));
            fileResolver.setCacheable(config.isUseCache());
            resolver = fileResolver;
            break;
        case WEB_ROOT:
            final FileTemplateResolver webRootResolver = new FileTemplateResolver();
            webRootResolver.setCharacterEncoding(config.getCharsetString());
            webRootResolver.setTemplateMode(TemplateMode.HTML);
            webRootResolver.setPrefix(StringKit.addSuffixIfNot(
                    FileKit.getAbsolutePath(FileKit.file(FileKit.getWebRoot(), config.getPath())), "/"));
            webRootResolver.setCacheable(config.isUseCache());
            resolver = webRootResolver;
            break;
        case STRING:
            resolver = new StringTemplateResolver();
            break;
        default:
            resolver = new DefaultTemplateResolver();
            break;
        }

        final TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Override
    public TemplateProvider init(TemplateConfig config) {
        if (null == config) {
            config = TemplateConfig.DEFAULT;
        }
        this.config = config;
        init(create(config));
        return this;
    }

    /**
     * 初始化引擎
     *
     * @param engine 引擎
     */
    private void init(final TemplateEngine engine) {
        this.engine = engine;
    }

    @Override
    public Template getTemplate(final String resource) {
        if (null == this.engine) {
            init(TemplateConfig.DEFAULT);
        }
        return ThymeleafTemplate.wrap(this.engine, resource, (null == this.config) ? null : this.config.getCharset());
    }

    /**
     * 获取原始引擎的钩子方法，用于自定义特殊属性，如插件等
     *
     * @return {@link TemplateEngine}
     */
    @Override
    public TemplateEngine getRaw() {
        return this.engine;
    }

}
