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
package org.miaixz.bus.core.tree;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StreamKit;
import org.miaixz.bus.core.xyz.TreeKit;

/**
 * <p>
 * 用于迭代层级结构（比如树或图）的迭代器， 支持{@link #depthFirst 广度优先}与{@link #breadthFirst 深度优先}两种遍历模式。
 * 迭代器仅适用于访问层级结构，因此不支持{@link Iterator#remove}方法。 要构建树或者操作数，请参见{@link BeanTree}或{@link TreeKit}。
 *
 * <p>
 * 该迭代器侧重于打通图或树这类数据结构与传统集合间的隔阂， 从而支持通过传统可迭代集合的方式对树或图中的节点进行操作。 比如：
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
 * @param <T> 元素类型
 * @author Kimi Liu
 * @see EasyStream#iterateHierarchies
 * @see StreamKit#iterateHierarchies
 * @since Java 17+
 */
public abstract class HierarchyIterator<T> implements Iterator<T> {

    /**
     * 下一层级节点的获取方法
     */
    protected final Function<T, Collection<T>> elementDiscoverer;
    /**
     * 节点过滤器，不匹配的节点与以其作为根节点的子树将将会被忽略
     */
    protected final Predicate<T> filter;
    /**
     * 已经访问过的列表
     */
    protected final Set<T> accessed = new HashSet<>();
    /**
     * 当前待遍历的节点
     */
    protected final LinkedList<T> queue = new LinkedList<>();

    /**
     * 创建一个迭代器
     *
     * @param root              根节点，根节点不允许被{@code filter}过滤
     * @param elementDiscoverer 获取下一层级节点的方法
     * @param filter            节点过滤器，不匹配的节点与以其作为根节点的子树将将会被忽略
     */
    HierarchyIterator(final T root, final Function<T, Collection<T>> elementDiscoverer, final Predicate<T> filter) {
        // 根节点不允许被过滤
        Assert.isTrue(filter.test(root), "root node cannot be filtered!");
        queue.add(root);
        this.elementDiscoverer = Assert.notNull(elementDiscoverer);
        this.filter = Assert.notNull(filter);
    }

    /**
     * 获取一个迭代器，用于按广度优先迭代层级结构中的每一个结点
     *
     * @param root           根节点，根节点不允许被{@code filter}过滤
     * @param nextDiscoverer 下一层级节点的获取方法
     * @param filter         节点过滤器，不匹配的节点与以其作为根节点的子树将将会被忽略
     * @param <T>            元素类型
     * @return 迭代器
     */
    public static <T> HierarchyIterator<T> breadthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer,
            final Predicate<T> filter) {
        return new BreadthFirst<>(root, nextDiscoverer, filter);
    }

    /**
     * 获取一个迭代器，用于按广度优先迭代层级结构中的每一个结点
     *
     * @param root           根节点，根节点不允许被{@code filter}过滤
     * @param nextDiscoverer 下一层级节点的获取方法
     * @param <T>            元素类型
     * @return 迭代器
     */
    public static <T> HierarchyIterator<T> breadthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer) {
        return breadthFirst(root, nextDiscoverer, t -> true);
    }

    /**
     * 获取一个迭代器，用于按深度优先迭代层级结构中的每一个结点
     *
     * @param root           根节点，根节点不允许被{@code filter}过滤
     * @param nextDiscoverer 下一层级节点的获取方法
     * @param filter         节点过滤器，不匹配的节点与以其作为根节点的子树将将会被忽略
     * @param <T>            元素类型
     * @return 迭代器
     */
    public static <T> HierarchyIterator<T> depthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer,
            final Predicate<T> filter) {
        return new DepthFirst<>(root, nextDiscoverer, filter);
    }

    /**
     * 获取一个迭代器，用于按深度优先迭代层级结构中的每一个结点
     *
     * @param root           根节点，根节点不允许被{@code filter}过滤
     * @param nextDiscoverer 下一层级节点的获取方法
     * @param <T>            元素类型
     * @return 迭代器
     */
    public static <T> HierarchyIterator<T> depthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer) {
        return depthFirst(root, nextDiscoverer, t -> true);
    }

    /**
     * 是否仍有下一个节点
     *
     * @return 是否
     */
    @Override
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    /**
     * 获取下一个节点
     *
     * @return 下一个节点
     */
    @Override
    public T next() {
        if (queue.isEmpty()) {
            throw new NoSuchElementException();
        }
        final T curr = queue.removeFirst();
        accessed.add(curr);
        Collection<T> nextElements = elementDiscoverer.apply(curr);
        if (Objects.nonNull(nextElements) && !nextElements.isEmpty()) {
            nextElements = nextElements.stream().filter(filter).collect(Collectors.toList());
            collectNextElementsToQueue(nextElements);
        }
        return curr;
    }

    /**
     * 将下一层级的节点搜集到队列
     *
     * @param nextElements 待收集的下一层级的节点
     * @see #queue
     * @see #accessed
     */
    protected abstract void collectNextElementsToQueue(final Collection<T> nextElements);

    /**
     * 深度优先遍历
     *
     * @param <T> 元素
     */
    static class DepthFirst<T> extends HierarchyIterator<T> {

        /**
         * 创建一个迭代器
         *
         * @param root           根节点，根节点不允许被{@code filter}过滤
         * @param nextDiscoverer 获取下一层级节点的方法
         * @param filter         节点过滤器，不匹配的节点与以其作为根节点的子树将将会被忽略
         */
        DepthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer, final Predicate<T> filter) {
            super(root, nextDiscoverer, filter);
        }

        /**
         * 将下一层级的节点搜集到队列
         *
         * @param nextElements 待收集的下一层级的节点
         */
        @Override
        protected void collectNextElementsToQueue(final Collection<T> nextElements) {
            int idx = 0;
            for (final T nextElement : nextElements) {
                if (!accessed.contains(nextElement)) {
                    queue.add(idx++, nextElement);
                    accessed.add(nextElement);
                }
            }
        }
    }

    /**
     * 广度优先遍历
     *
     * @param <T> 元素类型
     */
    static class BreadthFirst<T> extends HierarchyIterator<T> {

        /**
         * 创建一个迭代器
         *
         * @param root           根节点，根节点不允许被{@code filter}过滤
         * @param nextDiscoverer 获取下一层级节点的方法
         * @param filter         节点过滤器，不匹配的节点与以其作为根节点的子树将将会被忽略
         */
        BreadthFirst(final T root, final Function<T, Collection<T>> nextDiscoverer, final Predicate<T> filter) {
            super(root, nextDiscoverer, filter);
        }

        /**
         * 将下一层级的节点搜集到队列
         *
         * @param nextElements 待收集的下一层级的节点
         */
        @Override
        protected void collectNextElementsToQueue(final Collection<T> nextElements) {
            for (final T nextElement : nextElements) {
                if (!accessed.contains(nextElement)) {
                    queue.addLast(nextElement);
                    accessed.add(nextElement);
                }
            }
        }

    }

}
