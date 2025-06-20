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
package org.miaixz.bus.core.lang.loader;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.Loader;

/**
 * 原子引用加载器 使用{@link AtomicReference} 实懒加载，过程如下
 * 
 * <pre>
 * 1. 检查引用中是否有加载好的对象，有则返回
 * 2. 如果没有则初始化一个对象，并再次比较引用中是否有其它线程加载好的对象，无则加入，有则返回已有的
 * </pre>
 * 
 * 当对象未被创建，对象的初始化操作在多线程情况下可能会被调用多次（多次创建对象），但是总是返回同一对象
 *
 * @param <T> 被加载对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AtomicLoader<T> implements Loader<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852267211560L;

    /**
     * 被加载对象的引用
     */
    private final AtomicReference<T> reference = new AtomicReference<>();

    /**
     * 获取一个对象，第一次调用此方法时初始化对象然后返回，之后调用此方法直接返回原对象
     */
    @Override
    public T get() {
        T result = reference.get();

        if (result == null) {
            result = init();
            if (!reference.compareAndSet(null, result)) {
                // 其它线程已经创建好此对象
                result = reference.get();
            }
        }

        return result;
    }

    @Override
    public boolean isInitialized() {
        return null != reference.get();
    }

    /**
     * 初始化被加载的对象 如果对象从未被加载过，调用此方法初始化加载对象，此方法只被调用一次
     *
     * @return 被加载的对象
     */
    protected abstract T init();

}
