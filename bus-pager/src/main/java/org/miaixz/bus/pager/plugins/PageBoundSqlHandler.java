/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
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
package org.miaixz.bus.pager.plugins;

import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.pager.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageBoundSqlHandler {

    private BoundSqlHandler.Chain chain;

    public void setProperties(Properties properties) {
        // 初始化 boundSqlInterceptorChain
        String boundSqlInterceptorStr = properties.getProperty("boundSqlInterceptors");
        if (StringKit.isNotEmpty(boundSqlInterceptorStr)) {
            String[] boundSqlInterceptors = boundSqlInterceptorStr.split("[;|,]");
            List<BoundSqlHandler> list = new ArrayList<>();
            for (int i = 0; i < boundSqlInterceptors.length; i++) {
                try {
                    BoundSqlHandler boundSqlInterceptor = (BoundSqlHandler) Class.forName(boundSqlInterceptors[i]).getConstructor().newInstance();
                    if (boundSqlInterceptor instanceof Property) {
                        ((Property) boundSqlInterceptor).setProperties(properties);
                    }
                    list.add(boundSqlInterceptor);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (list.size() > 0) {
                chain = new BoundSqlChain(null, list);
            }
        }
    }

    public BoundSqlHandler.Chain getChain() {
        return chain;
    }

}