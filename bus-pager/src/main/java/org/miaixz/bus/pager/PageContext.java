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
package org.miaixz.bus.pager;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.binding.PageAutoDialect;
import org.miaixz.bus.pager.binding.PageMethod;
import org.miaixz.bus.pager.binding.PageParams;
import org.miaixz.bus.pager.dialect.AbstractPaging;
import org.miaixz.bus.pager.parsing.CountSqlParser;
import org.miaixz.bus.pager.builder.BoundSqlChainBuilder;
import org.miaixz.bus.pager.builder.BoundSqlBuilder;
import org.miaixz.bus.pager.builder.PageBoundSqlBuilder;

/**
 * Mybatis - 通用分页拦截器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageContext extends PageMethod implements Dialect, BoundSqlBuilder.Chain {

    private PageParams pageParams;
    private PageAutoDialect autoDialect;
    private PageBoundSqlBuilder pageBoundSqlBuilder;
    private ForkJoinPool asyncCountService;

    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = pageParams.getPage(parameterObject, rowBounds);
        if (page == null) {
            return true;
        } else {
            // 设置默认的 count 列
            if (StringKit.isEmpty(page.getCountColumn())) {
                page.setCountColumn(pageParams.getCountColumn());
            }
            // 设置默认的异步 count 设置
            if (page.getAsyncCount() == null) {
                page.setAsyncCount(pageParams.isAsyncCount());
            }
            autoDialect.initDelegateDialect(ms, page.getDialectClass());
            return false;
        }
    }

    @Override
    public boolean isAsyncCount() {
        return getLocalPage().asyncCount();
    }

    @Override
    public <T> Future<T> asyncCountTask(Callable<T> task) {
        // 异步执行时需要将ThreadLocal值传递，否则会找不到
        AbstractPaging dialectThreadLocal = autoDialect.getDialectThreadLocal();
        Page<Object> localPage = getLocalPage();
        String countId = UUID.randomUUID().toString();
        return asyncCountService.submit(() -> {
            try {
                // 设置 ThreadLocal
                autoDialect.setDialectThreadLocal(dialectThreadLocal);
                setLocalPage(localPage);
                return task.call();
            } finally {
                autoDialect.clearDelegate();
                clearPage();
            }
        });
    }

    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforeCount(ms, parameterObject, rowBounds);
    }

    @Override
    public String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey countKey) {
        return autoDialect.getDelegate().getCountSql(ms, boundSql, parameterObject, rowBounds, countKey);
    }

    @Override
    public boolean afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().afterCount(count, parameterObject, rowBounds);
    }

    @Override
    public Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql,
            CacheKey pageKey) {
        return autoDialect.getDelegate().processParameterObject(ms, parameterObject, boundSql, pageKey);
    }

    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforePage(ms, parameterObject, rowBounds);
    }

    @Override
    public String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(ms, boundSql, parameterObject, rowBounds, pageKey);
    }

    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(sql, page, pageKey);
    }

    @Override
    public Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds) {
        // 这个方法即使不分页也会被执行，所以要判断 null
        AbstractPaging delegate = autoDialect.getDelegate();
        if (delegate != null) {
            return delegate.afterPage(pageList, parameterObject, rowBounds);
        }
        return pageList;
    }

    @Override
    public void afterAll() {
        // 这个方法即使不分页也会被执行，所以要判断 null
        AbstractPaging delegate = autoDialect.getDelegate();
        if (delegate != null) {
            delegate.afterAll();
            autoDialect.clearDelegate();
        }
        clearPage();
    }

    @Override
    public BoundSql doBoundSql(BoundSqlBuilder.Type type, BoundSql boundSql, CacheKey cacheKey) {
        Page<Object> localPage = getLocalPage();
        BoundSqlBuilder.Chain chain = localPage != null ? localPage.getChain() : null;
        if (chain == null) {
            BoundSqlBuilder boundSqlHandler = localPage != null ? localPage.getBoundSqlInterceptor() : null;
            BoundSqlBuilder.Chain defaultChain = pageBoundSqlBuilder != null ? pageBoundSqlBuilder.getChain() : null;
            if (boundSqlHandler != null) {
                chain = new BoundSqlChainBuilder(defaultChain, Arrays.asList(boundSqlHandler));
            } else if (defaultChain != null) {
                chain = defaultChain;
            }
            if (chain == null) {
                chain = DO_NOTHING;
            }
            if (localPage != null) {
                localPage.setChain(chain);
            }
        }
        return chain.doBoundSql(type, boundSql, cacheKey);
    }

    @Override
    public void setProperties(Properties properties) {
        setStaticProperties(properties);
        pageParams = new PageParams();
        autoDialect = new PageAutoDialect();
        pageBoundSqlBuilder = new PageBoundSqlBuilder();
        pageParams.setProperties(properties);
        autoDialect.setProperties(properties);
        pageBoundSqlBuilder.setProperties(properties);
        // 20180902新增 aggregateFunctions, 允许手动添加聚合函数（影响行数）
        CountSqlParser.addAggregateFunctions(properties.getProperty("aggregateFunctions"));
        // 异步 asyncCountService 并发度设置，这里默认为应用可用的处理器核心数 * 2，更合理的值应该综合考虑数据库服务器的处理能力
        int asyncCountParallelism = Integer.parseInt(properties.getProperty("asyncCountParallelism",
                Normal.EMPTY + (Runtime.getRuntime().availableProcessors() * 2)));
        asyncCountService = new ForkJoinPool(asyncCountParallelism, pool -> {
            final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName("pager-async-count-" + worker.getPoolIndex());
            return worker;
        }, null, true);
    }

}
