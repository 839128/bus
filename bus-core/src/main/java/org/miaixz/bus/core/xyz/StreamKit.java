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
package org.miaixz.bus.core.xyz;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.miaixz.bus.core.center.stream.spliterators.DropWhileSpliterator;
import org.miaixz.bus.core.center.stream.spliterators.IterateSpliterator;
import org.miaixz.bus.core.center.stream.spliterators.TakeWhileSpliterator;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.tree.HierarchyIterator;

/**
 * {@link Stream} 工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamKit {

    /**
     * @param array 数组
     * @param <T>   元素类型
     * @return {@link Stream}，如果提供的array为{@code null}，返回{@link Stream#empty()}
     */
    @SafeVarargs
    public static <T> Stream<T> of(final T... array) {
        return null == array ? Stream.empty() : Stream.of(array);
    }

    /**
     * {@link Iterable}转换为{@link Stream}，默认非并行
     *
     * @param iterable 集合
     * @param <T>      集合元素类型
     * @return {@link Stream}，如果提供的iterator为{@code null}，返回{@link Stream#empty()}
     */
    public static <T> Stream<T> of(final Iterable<T> iterable) {
        return of(iterable, false);
    }

    /**
     * {@link Iterable}转换为{@link Stream}
     *
     * @param iterable 集合
     * @param parallel 是否并行
     * @param <T>      集合元素类型
     * @return {@link Stream}，如果提供的iterator为{@code null}，返回{@link Stream#empty()}
     */
    public static <T> Stream<T> of(final Iterable<T> iterable, final boolean parallel) {
        if (null == iterable) {
            return Stream.empty();
        }
        return iterable instanceof Collection
                ? parallel ? ((Collection<T>) iterable).parallelStream() : ((Collection<T>) iterable).stream()
                : StreamSupport.stream(iterable.spliterator(), parallel);
    }

    /**
     * {@link Iterator} 转换为 {@link Stream}
     *
     * @param iterator 迭代器
     * @param <T>      集合元素类型
     * @return {@link Stream}，如果提供的iterator为{@code null}，返回{@link Stream#empty()}
     */
    public static <T> Stream<T> ofIter(final Iterator<T> iterator) {
        return ofIter(iterator, false);
    }

    /**
     * {@link Iterator} 转换为 {@link Stream}
     *
     * @param iterator 迭代器
     * @param parallel 是否并行
     * @param <T>      集合元素类型
     * @return {@link Stream}，如果提供的iterator为{@code null}，返回{@link Stream#empty()}
     */
    public static <T> Stream<T> ofIter(final Iterator<T> iterator, final boolean parallel) {
        if (null == iterator) {
            return Stream.empty();
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), parallel);
    }

    /**
     * 按行读取文件为{@link Stream}
     *
     * @param file 文件
     * @return {@link Stream}，如果提供的file为{@code null}，返回{@link Stream#empty()}
     */
    public static Stream<String> of(final File file) {
        return of(file, Charset.UTF_8);
    }

    /**
     * 按行读取文件为{@link Stream}
     *
     * @param path 路径
     * @return {@link Stream}，如果提供的file为{@code null}，返回{@link Stream#empty()}
     */
    public static Stream<String> of(final Path path) {
        return of(path, Charset.UTF_8);
    }

    /**
     * 按行读取文件为{@link Stream}
     *
     * @param file    文件
     * @param charset 编码
     * @return {@link Stream}，如果提供的file为{@code null}，返回{@link Stream#empty()}
     */
    public static Stream<String> of(final File file, final java.nio.charset.Charset charset) {
        if (null == file) {
            return Stream.empty();
        }
        return of(file.toPath(), charset);
    }

    /**
     * 按行读取文件为{@link Stream}
     *
     * @param path    路径
     * @param charset 编码
     * @return {@link Stream}，如果提供的path为{@code null}，返回{@link Stream#empty()}
     */
    public static Stream<String> of(final Path path, final java.nio.charset.Charset charset) {
        if (null == path) {
            return Stream.empty();
        }
        try {
            return Files.lines(path, charset);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 通过函数创建Stream
     *
     * @param seed           初始值
     * @param elementCreator 递进函数，每次调用此函数获取下一个值
     * @param limit          限制个数
     * @param <T>            创建元素类型
     * @return {@link Stream}
     */
    public static <T> Stream<T> of(final T seed, final UnaryOperator<T> elementCreator, final int limit) {
        return Stream.iterate(seed, elementCreator).limit(limit);
    }

    /**
     * 将Stream中所有元素以指定分隔符，合并为一个字符串，对象默认调用toString方法
     *
     * @param stream    {@link Stream}
     * @param delimiter 分隔符
     * @param <T>       元素类型
     * @return 字符串，如果stream为{@code null}，返回{@code null}
     */
    public static <T> String join(final Stream<T> stream, final CharSequence delimiter) {
        if (null == stream) {
            return null;
        }
        return stream.collect(CollectorKit.joining(delimiter));
    }

    /**
     * 将Stream中所有元素以指定分隔符，合并为一个字符串
     *
     * @param stream       {@link Stream}
     * @param delimiter    分隔符
     * @param toStringFunc 元素转换为字符串的函数
     * @param <T>          元素类型
     * @return 字符串，如果stream为{@code null}，返回{@code null}
     */
    public static <T> String join(final Stream<T> stream, final CharSequence delimiter,
            final Function<T, ? extends CharSequence> toStringFunc) {
        if (null == stream) {
            return null;
        }
        return stream.collect(CollectorKit.joining(delimiter, toStringFunc));
    }

    /**
     * 返回无限有序流 该流由 初始值 然后判断条件 以及执行 迭代函数 进行迭代获取到元素
     *
     * @param <T>     元素类型
     * @param seed    初始值
     * @param hasNext 条件值
     * @param next    用上一个元素作为参数执行并返回一个新的元素
     * @return 无限有序流
     */
    public static <T> Stream<T> iterate(final T seed, final Predicate<? super T> hasNext, final UnaryOperator<T> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        return StreamSupport.stream(IterateSpliterator.of(seed, hasNext, next), false);
    }

    /**
     * 指定一个层级结构的根节点（通常是树或图）， 然后获取包含根节点在内，根节点所有层级结构中的节点组成的流。 该方法用于以平铺的方式按广度优先对图或树节点进行访问，可以使用并行流提高效率。
     *
     * <p>
     * eg:
     * 
     * <pre>{@code
     * Tree root = // 构建树结构
     * // 搜索树结构中所有级别为3的节点，并按权重排序
     * List<Tree> thirdLevelNodes = StreamKit.iterateHierarchies(root, Tree:getChildren)
     * 	.filter(node -> node.getLevel() == 3)
     * 	.sorted(Comparator.comparing(Tree::getWeight))
     * 	.toList();
     * }</pre>
     *
     * @param root       根节点，根节点不允许被{@code filter}过滤
     * @param discoverer 下一层级节点的获取方法
     * @param filter     节点过滤器，不匹配的节点与以其作为根节点的子树将将会被忽略
     * @param <T>        元素类型
     * @return 包含根节点在内，根节点所有层级结构中的节点组成的流
     * @see HierarchyIterator
     */
    public static <T> Stream<T> iterateHierarchies(final T root, final Function<T, Collection<T>> discoverer,
            final Predicate<T> filter) {
        return ofIter(HierarchyIterator.breadthFirst(root, discoverer, filter));
    }

    /**
     * 指定一个层级结构的根节点（通常是树或图）， 然后获取包含根节点在内，根节点所有层级结构中的节点组成的流。 该方法用于以平铺的方式按广度优先对图或树节点进行访问，可以使用并行流提高效率。
     *
     * <p>
     * eg:
     * 
     * <pre>{@code
     * Tree root = // 构建树结构
     * // 搜索树结构中所有级别为3的节点，并按权重排序
     * List<Tree> thirdLevelNodes = StreamKit.iterateHierarchies(root, Tree:getChildren)
     * 	.filter(node -> node.getLevel() == 3)
     * 	.sorted(Comparator.comparing(Tree::getWeight))
     * 	.toList();
     * }</pre>
     *
     * @param root       根节点，根节点不允许被{@code filter}过滤
     * @param discoverer 下一层级节点的获取方法
     * @param <T>        元素类型
     * @return 包含根节点在内，根节点所有层级结构中的节点组成的流
     * @see HierarchyIterator
     */
    public static <T> Stream<T> iterateHierarchies(final T root, final Function<T, Collection<T>> discoverer) {
        return ofIter(HierarchyIterator.breadthFirst(root, discoverer));
    }

    /**
     * 保留 与指定断言 匹配时的元素, 在第一次不匹配时终止, 抛弃当前(第一个不匹配元素)及后续所有元素
     * <p>
     * 与 jdk9 中的 takeWhile 方法不太一样, 这里的实现是个 顺序的、有状态的中间操作
     * </p>
     * <p>
     * 本环节中是顺序执行的, 但是后续操作可以支持并行流
     * </p>
     * <p>
     * 但是不建议在并行流中使用, 除非你确定 takeWhile 之后的操作能在并行流中受益很多
     * </p>
     *
     * @param source    源流
     * @param <T>       元素类型
     * @param predicate 断言
     * @return 与指定断言匹配的元素组成的流
     */
    public static <T> Stream<T> takeWhile(final Stream<T> source, final Predicate<? super T> predicate) {
        if (null == source) {
            return Stream.empty();
        }
        Objects.requireNonNull(predicate);
        return createStatefulNewStream(source, TakeWhileSpliterator.create(source.spliterator(), predicate));
    }

    /**
     * 删除 与指定断言 匹配的元素, 在第一次不匹配时终止, 返回当前(第一个不匹配元素)及剩余元素组成的新流
     * <p>
     * 与 jdk9 中的 dropWhile 方法不太一样, 这里的实现是个 顺序的、有状态的中间操作
     * </p>
     * <p>
     * 本环节中是顺序执行的, 但是后续操作可以支持并行流
     * </p>
     * <p>
     * 但是不建议在并行流中使用, 除非你确定 dropWhile 之后的操作能在并行流中受益很多
     * </p>
     *
     * @param source    源流
     * @param <T>       元素类型
     * @param predicate 断言
     * @return 剩余元素组成的流
     */
    public static <T> Stream<T> dropWhile(final Stream<T> source, final Predicate<? super T> predicate) {
        if (null == source) {
            return Stream.empty();
        }
        Objects.requireNonNull(predicate);
        return createStatefulNewStream(source, DropWhileSpliterator.of(source.spliterator(), predicate));
    }

    public static void readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (readAvailable(in, b, off, len) < len)
            throw new EOFException();
    }

    public static void skipFully(InputStream in, long n) throws IOException {
        while (n > 0) {
            long count = in.skip(n);
            if (count == 0) {
                if (in.read() == -1) {
                    throw new EOFException();
                }
                count = 1;
            }
            n -= count;
        }
    }

    public static int readAvailable(InputStream in, byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length)
            throw new IndexOutOfBoundsException();
        int wpos = off;
        while (len > 0) {
            int count = in.read(b, wpos, len);
            if (count < 0)
                break;
            wpos += count;
            len -= count;
        }
        return wpos - off;
    }

    /**
     * 根据 源流 和 新的Spliterator 生成新的流
     * <p>
     * 这是一个 顺序的、有状态的流
     * </p>
     * <p>
     * 在新流的第一个节点是顺序执行的, 但是后续操作可以支持并行流
     * </p>
     *
     * @param source         源流
     * @param newSpliterator 新流的Spliterator
     * @param <T>            旧流的元素类型
     * @param <R>            新流的元素类型
     * @return 新流
     */
    private static <T, R> Stream<R> createStatefulNewStream(final Stream<T> source,
            final Spliterator<R> newSpliterator) {
        // 创建新流
        Stream<R> newStream = StreamSupport.stream(newSpliterator, source.isParallel());
        // 如果旧流是并行流, 新流主动调用一个有状态的操作, 虽然没有意义, 但是可以让后续的无状态节点正常并发
        if (source.isParallel()) {
            newStream = newStream.limit(Long.MAX_VALUE);
        }
        // 由于新流不与旧流的节点关联, 所以需要主动设置旧流的close方法, 哪怕几乎不可能有人在旧流上设置onClose操作
        return newStream.onClose(source::close);
    }

}
