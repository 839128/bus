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
package org.miaixz.bus.core.center;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 使用给定的转换函数，转换源{@link Spliterator}为新类型的{@link Spliterator}
 *
 * @param <F> 源元素类型
 * @param <T> 目标元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class TransSpliterator<F, T> implements Spliterator<T> {

    private final Spliterator<F> from;
    private final Function<? super F, ? extends T> function;

    /**
     * 构造
     *
     * @param from     源iterator
     * @param function 函数
     */
    public TransSpliterator(final Spliterator<F> from, final Function<? super F, ? extends T> function) {
        this.from = from;
        this.function = function;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        return from.tryAdvance(fromElement -> action.accept(function.apply(fromElement)));
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        from.forEachRemaining(fromElement -> action.accept(function.apply(fromElement)));
    }

    @Override
    public Spliterator<T> trySplit() {
        final Spliterator<F> fromSplit = from.trySplit();
        return (fromSplit != null) ? new TransSpliterator<>(fromSplit, function) : null;
    }

    @Override
    public long estimateSize() {
        return from.estimateSize();
    }

    @Override
    public int characteristics() {
        return from.characteristics() & ~(Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
    }

}
