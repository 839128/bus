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
package org.miaixz.bus.core.builder;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.center.function.Consumer3X;

/**
 * 通用Builder 参考: <a href="https://blog.csdn.net/weixin_43935907/article/details/105003719">一看就会的java8通用Builder</a>
 * <p>
 * 使用方法如下：
 * </p>
 * 
 * <pre>
 * Box box = GenericBuilder.of(Box::new).with(Box::setId, 1024L).with(Box::setTitle, "Hello World!")
 *         .with(Box::setLength, 9).with(Box::setWidth, 8).with(Box::setHeight, 7).build();
 *
 * </pre>
 *
 * <p>
 * 我们也可以对已创建的对象进行修改：
 * </p>
 * 
 * <pre>
 * Box boxModified = GenericBuilder.of(() -&gt; box).with(Box::setTitle, "Hello Friend!").with(Box::setLength, 3)
 *         .with(Box::setWidth, 4).with(Box::setHeight, 5).build();
 * </pre>
 * <p>
 * 我们还可以对这样调用有参构造，这对于创建一些在有参构造中包含初始化函数的对象是有意义的：
 * </p>
 * 
 * <pre>
 * Box box1 = GenericBuilder.of(Box::new, 2048L, "Hello Partner!", 222, 333, 444).with(Box::alis).build();
 * </pre>
 * <p>
 * 还可能这样构建Map对象：
 * </p>
 * {@code
 * HashMap<String, String> colorMap = GenericBuilder
 * .of(HashMap<String,String>::new)
 * .with(Map::put, "red", "#FF0000")
 * .with(Map::put, "yellow", "#FFFF00")
 * .with(Map::put, "blue", "#0000FF")
 * .build();
 * }
 *
 * <p>
 * 注意：本工具类支持调用的构造方法的参数数量不超过5个，一般方法的参数数量不超过2个，更多的参数不利于阅读和维护
 * </p>
 *
 * @param <T> 构建对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenericBuilder<T> implements Builder<T> {

    @Serial
    private static final long serialVersionUID = 2852229897009L;

    /**
     * 实例化器
     */
    private final Supplier<T> instant;

    /**
     * 修改器列表
     */
    private final List<Consumer<T>> modifiers = new ArrayList<>();

    /**
     * 构造
     *
     * @param instant 实例化器
     */
    public GenericBuilder(final Supplier<T> instant) {
        this.instant = instant;
    }

    /**
     * 通过Supplier创建GenericBuilder
     *
     * @param instant 实例化器
     * @param <T>     目标类型
     * @return GenericBuilder对象
     */
    public static <T> GenericBuilder<T> of(final Supplier<T> instant) {
        return new GenericBuilder<>(instant);
    }

    /**
     * 调用无参数方法
     *
     * @param consumer 无参数Consumer
     * @return GenericBuilder对象
     */
    public GenericBuilder<T> with(final Consumer<T> consumer) {
        modifiers.add(consumer);
        return this;
    }

    /**
     * 调用1参数方法
     *
     * @param consumer 1参数Consumer
     * @param p1       参数一
     * @param <P1>     参数一类型
     * @return GenericBuilder对象
     */
    public <P1> GenericBuilder<T> with(final BiConsumer<T, P1> consumer, final P1 p1) {
        modifiers.add(instant -> consumer.accept(instant, p1));
        return this;
    }

    /**
     * 调用2参数方法
     *
     * @param consumer 2参数Consumer
     * @param p1       参数一
     * @param p2       参数二
     * @param <P1>     参数一类型
     * @param <P2>     参数二类型
     * @return GenericBuilder对象
     */
    public <P1, P2> GenericBuilder<T> with(final Consumer3X<T, P1, P2> consumer, final P1 p1, final P2 p2) {
        modifiers.add(instant -> consumer.accept(instant, p1, p2));
        return this;
    }

    /**
     * 构建
     *
     * @return 目标对象
     */
    @Override
    public T build() {
        final T value = instant.get();
        modifiers.forEach(modifier -> modifier.accept(value));
        modifiers.clear();
        return value;
    }

}
