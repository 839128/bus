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

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

/**
 * 数据库方言，针对不同数据库进行实现
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Dialect {

    /**
     * 跳过 count 和 分页查询
     *
     * @param ms              MappedStatement
     * @param parameterObject 方法参数
     * @param rowBounds       分页参数
     * @return true 跳过，返回默认查询结果，false 执行分页查询
     */
    boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds);

    /**
     * 是否使用异步 count 查询，使用异步后不会根据返回的 count 数来判断是否有必要进行分页查询
     *
     * @return true 异步，false 同步
     */
    default boolean isAsyncCount() {
        return false;
    }

    /**
     * 执行异步 count 查询
     *
     * @param task 异步查询任务
     * @param <T>  引用对象
     * @return the object
     */
    default <T> Future<T> asyncCountTask(Callable<T> task) {
        return ForkJoinPool.commonPool().submit(task);
    }

    /**
     * 执行分页前，返回 true 会进行 count 查询，false 会继续下面的 beforePage 判断
     *
     * @param ms              MappedStatement
     * @param parameterObject 方法参数
     * @param rowBounds       分页参数
     * @return the object
     */
    boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds);

    /**
     * 生成 count 查询 sql
     *
     * @param ms              MappedStatement
     * @param boundSql        绑定 SQL 对象
     * @param parameterObject 方法参数
     * @param rowBounds       分页参数
     * @param countKey        count 缓存 key
     * @return the object
     */
    String getCountSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey countKey);

    /**
     * 执行完 count 查询后
     *
     * @param count           查询结果总数
     * @param parameterObject 接口参数
     * @param rowBounds       分页参数
     * @return true 继续分页查询，false 直接返回
     */
    boolean afterCount(long count, Object parameterObject, RowBounds rowBounds);

    /**
     * 处理查询参数对象
     *
     * @param ms              MappedStatement
     * @param parameterObject 方法参数
     * @param boundSql        绑定 SQL 对象
     * @param pageKey         分页缓存 key
     * @return the object
     */
    Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey);

    /**
     * 执行分页前，返回 true 会进行分页查询，false 会返回默认查询结果
     *
     * @param ms              MappedStatement
     * @param parameterObject 方法参数
     * @param rowBounds       分页参数
     * @return the object
     */
    boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds);

    /**
     * 生成分页查询 sql
     *
     * @param ms              MappedStatement
     * @param boundSql        绑定 SQL 对象
     * @param parameterObject 方法参数
     * @param rowBounds       分页参数
     * @param pageKey         分页缓存 key
     * @return the object
     */
    String getPageSql(MappedStatement ms, BoundSql boundSql, Object parameterObject, RowBounds rowBounds,
            CacheKey pageKey);

    /**
     * 分页查询后，处理分页结果，拦截器中直接 return 该方法的返回值
     *
     * @param pageList        分页查询结果
     * @param parameterObject 方法参数
     * @param rowBounds       分页参数
     * @return the object
     */
    Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds);

    /**
     * 完成所有任务后
     */
    void afterAll();

    /**
     * 设置参数
     *
     * @param properties 插件属性
     */
    void setProperties(Properties properties);

}
