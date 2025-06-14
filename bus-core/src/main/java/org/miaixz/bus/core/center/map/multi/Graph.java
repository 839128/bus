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
package org.miaixz.bus.core.center.map.multi;

import java.io.Serial;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.miaixz.bus.core.xyz.CollKit;

/**
 * 支持处理无向图结构的{@link Map}，本质上是基于{@link SetValueMap}实现的邻接表
 *
 * @param <T> 节点类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Graph<T> extends SetValueMap<T, T> {

    @Serial
    private static final long serialVersionUID = 2852277389299L;

    /**
     * 添加边
     *
     * @param target1 节点
     * @param target2 节点
     */
    public void putEdge(final T target1, final T target2) {
        this.putValue(target1, target2);
        this.putValue(target2, target1);
    }

    /**
     * 是否存在边
     *
     * @param target1 节点
     * @param target2 节点
     * @return 是否
     */
    public boolean containsEdge(final T target1, final T target2) {
        return this.getValues(target1).contains(target2) && this.getValues(target2).contains(target1);
    }

    /**
     * 移除边
     *
     * @param target1 节点
     * @param target2 节点
     */
    public void removeEdge(final T target1, final T target2) {
        this.removeValue(target1, target2);
        this.removeValue(target2, target1);
    }

    /**
     * 移除节点，并删除该节点与其他节点之间连成的边
     *
     * @param target 目标对象
     */
    public void removePoint(final T target) {
        final Collection<T> associatedPoints = this.remove(target);
        if (CollKit.isNotEmpty(associatedPoints)) {
            associatedPoints.forEach(p -> this.removeValue(p, target));
        }
    }

    /**
     * 两节点是否存在直接或间接的关联
     *
     * @param target1 节点
     * @param target2 节点
     * @return 两节点是否存在关联
     */
    public boolean containsAssociation(final T target1, final T target2) {
        if (!this.containsKey(target1) || !this.containsKey(target2)) {
            return false;
        }
        final AtomicBoolean flag = new AtomicBoolean(false);
        visitAssociatedPoints(target1, t -> {
            if (Objects.equals(t, target2)) {
                flag.set(true);
                return true;
            }
            return false;
        });
        return flag.get();
    }

    /**
     * 按广度优先，获得节点的所有直接或间接关联的节点，节点默认按添加顺序排序
     *
     * @param target        节点
     * @param includeTarget 是否包含查询节点
     * @return 节点的所有关联节点
     */
    public Collection<T> getAssociatedPoints(final T target, final boolean includeTarget) {
        final Set<T> points = visitAssociatedPoints(target, t -> false);
        if (!includeTarget) {
            points.remove(target);
        }
        return points;
    }

    /**
     * 获取节点的邻接节点
     *
     * @param target 节点
     * @return 邻接节点
     */
    public Collection<T> getAdjacentPoints(final T target) {
        return this.getValues(target);
    }

    /**
     * 按广度优先，访问节点的所有关联节点
     */
    private Set<T> visitAssociatedPoints(final T key, final Predicate<T> breaker) {
        if (!this.containsKey(key)) {
            return Collections.emptySet();
        }
        final Set<T> accessed = new HashSet<>();
        final Deque<T> deque = new LinkedList<>();
        deque.add(key);
        while (!deque.isEmpty()) {
            // 访问节点
            final T t = deque.removeFirst();
            if (accessed.contains(t)) {
                continue;
            }
            accessed.add(t);
            // 若符合条件则中断循环
            if (breaker.test(t)) {
                break;
            }
            // 获取邻接节点
            final Collection<T> neighbours = this.getValues(t);
            if (!neighbours.isEmpty()) {
                deque.addAll(neighbours);
            }
        }
        return accessed;
    }

}
