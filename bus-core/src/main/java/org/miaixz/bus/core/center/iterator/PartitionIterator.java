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
package org.miaixz.bus.core.center.iterator;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.miaixz.bus.core.lang.Assert;

/**
 * 分批迭代工具，可以分批处理数据
 * <ol>
 * <li>比如调用其他客户的接口，传入的入参有限，需要分批</li>
 * <li>比如mysql/oracle用in语句查询，超过1000可以分批</li>
 * <li>比如数据库取出游标，可以把游标里的数据一批一批处理</li>
 * </ol>
 *
 * @param <T> 字段类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartitionIterator<T> implements IterableIterator<List<T>>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852267311619L;

    /**
     * 被分批的迭代器
     */
    protected final Iterator<T> iterator;
    /**
     * 实际每批大小
     */
    protected final int partitionSize;

    /**
     * 创建分组对象
     *
     * @param iterator      迭代器
     * @param partitionSize 每批大小，最后一批不满一批算一批
     * @throws IllegalArgumentException 当{@code partitionSize}小于等于0，或{@code iterator}为{@code null}时抛出
     */
    public PartitionIterator(final Iterator<T> iterator, final int partitionSize) {
        Assert.isTrue(partitionSize > 0, "partition size must greater than 0");
        this.iterator = Objects.requireNonNull(iterator);
        this.partitionSize = partitionSize;
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public List<T> next() {
        final List<T> list = new ArrayList<>(this.partitionSize);
        for (int i = 0; i < this.partitionSize; i++) {
            if (!iterator.hasNext()) {
                break;
            }
            list.add(iterator.next());
        }
        return list;
    }

}
