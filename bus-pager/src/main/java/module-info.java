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
/**
 * bus.pager
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.pager {

    requires java.sql;

    requires bus.cache;
    requires bus.core;
    requires bus.crypto;
    requires bus.logger;
    requires bus.mapper;

    requires org.mybatis;
    requires druid;
    requires com.zaxxer.hikari;
    requires com.google.common;
    requires net.sf.jsqlparser;

    exports org.miaixz.bus.pager;
    exports org.miaixz.bus.pager.binding;
    exports org.miaixz.bus.pager.builder;
    exports org.miaixz.bus.pager.cache;
    exports org.miaixz.bus.pager.dialect;
    exports org.miaixz.bus.pager.handler;
    exports org.miaixz.bus.pager.parsing;
    exports org.miaixz.bus.pager.dialect.auto;
    exports org.miaixz.bus.pager.dialect.base;
    exports org.miaixz.bus.pager.dialect.replace;
    exports org.miaixz.bus.pager.dialect.rowbounds;

    uses org.miaixz.bus.pager.parsing.SqlParser;

}
