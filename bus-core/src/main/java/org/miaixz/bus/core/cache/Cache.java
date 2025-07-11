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
package org.miaixz.bus.core.cache;

import java.io.Serializable;
import java.util.Iterator;

import org.miaixz.bus.core.cache.provider.CacheObject;
import org.miaixz.bus.core.center.function.SupplierX;

/**
 * 缓存接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Cache<K, V> extends Iterable<V>, Serializable {

    /**
     * 返回缓存容量，{@code 0}表示无大小限制
     *
     * @return 返回缓存容量，{@code 0}表示无大小限制
     */
    int capacity();

    /**
     * 缓存失效时长， {@code 0} 表示没有设置，单位毫秒
     *
     * @return 缓存失效时长， {@code 0} 表示没有设置，单位毫秒
     */
    long timeout();

    /**
     * 将对象加入到缓存，使用默认失效时长
     *
     * @param key    键
     * @param object 缓存的对象
     * @see Cache#put(Object, Object, long)
     */
    void put(K key, V object);

    /**
     * 将对象加入到缓存，使用指定失效时长 如果缓存空间满了，{@link #prune()} 将被调用以获得空间来存放新对象
     *
     * @param key     键
     * @param object  缓存的对象
     * @param timeout 失效时长，单位毫秒
     */
    void put(K key, V object, long timeout);

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期返回{@code null} 调用此方法时，会检查上次调用时间，如果与当前时间差值大于超时时间返回{@code null}，否则返回值。
     * 每次调用此方法会刷新最后访问时间，也就是说会重新计算超时时间。
     *
     * @param key 键
     * @return 键对应的对象
     * @see #get(Object, boolean)
     */
    default V get(final K key) {
        return get(key, true);
    }

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期（与当前时间差值大于超时时间）返回 {@link SupplierX} 回调产生的对象，否则返回值。 每次调用此方法会刷新最后访问时间，也就是说会重新计算超时时间。
     *
     * @param key      键
     * @param supplier 如果不存在回调方法，用于生产值对象
     * @return 值对象
     */
    default V get(final K key, final SupplierX<V> supplier) {
        return get(key, true, supplier);
    }

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期（与当前时间差值大于超时时间）返回 {@link SupplierX} 回调产生的对象，否则返回值。
     * 每次调用此方法会可选是否刷新最后访问时间，{@code true}表示会重新计算超时时间。
     *
     * @param key                键
     * @param isUpdateLastAccess 是否更新最后访问时间，即重新计算超时时间。
     * @param supplier           如果不存在回调方法，用于生产值对象
     * @return 值对象
     */
    V get(K key, boolean isUpdateLastAccess, SupplierX<V> supplier);

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期（与当前时间差值大于超时时间）返回 {@link SupplierX} 回调产生的对象，否则返回值。
     * 每次调用此方法会可选是否刷新最后访问时间，{@code true}表示会重新计算超时时间。
     *
     * @param key                键
     * @param isUpdateLastAccess 是否更新最后访问时间，即重新计算超时时间。
     * @param timeout            自定义超时时间
     * @param supplier           如果不存在回调方法，用于生产值对象
     * @return 值对象
     */
    V get(K key, boolean isUpdateLastAccess, final long timeout, SupplierX<V> supplier);

    /**
     * 从缓存中获得对象，当对象不在缓存中或已经过期（与当前时间差值大于超时时间）返回{@code null}，否则返回值。 每次调用此方法会可选是否刷新最后访问时间，{@code true}表示会重新计算超时时间。
     *
     * @param key                键
     * @param isUpdateLastAccess 是否更新最后访问时间，即重新计算超时时间。
     * @return 键对应的对象
     */
    V get(K key, boolean isUpdateLastAccess);

    /**
     * 返回包含键和值得迭代器
     *
     * @return 缓存对象迭代器
     */
    Iterator<CacheObject<K, V>> cacheObjIterator();

    /**
     * 从缓存中清理过期对象，清理策略取决于具体实现
     *
     * @return 清理的缓存对象个数
     */
    int prune();

    /**
     * 缓存是否已满，仅用于有空间限制的缓存对象
     *
     * @return 缓存是否已满，仅用于有空间限制的缓存对象
     */
    boolean isFull();

    /**
     * 从缓存中移除对象
     *
     * @param key 键
     */
    void remove(K key);

    /**
     * 清空缓存
     */
    void clear();

    /**
     * 缓存的对象数量
     *
     * @return 缓存的对象数量
     */
    int size();

    /**
     * 缓存是否为空
     *
     * @return 缓存是否为空
     */
    boolean isEmpty();

    /**
     * 是否包含key
     *
     * @param key KEY
     * @return 是否包含key
     */
    boolean containsKey(K key);

    /**
     * 设置监听
     *
     * @param listener 监听
     * @return this
     */
    default Cache<K, V> setListener(final CacheListener<K, V> listener) {
        return this;
    }

}
