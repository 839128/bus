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
package org.miaixz.bus.core.center.list;

import java.util.AbstractList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;

/**
 * 列表分区或分段 通过传入分区长度，将指定列表分区为不同的块，每块区域的长度相同（最后一块可能小于长度） 分区是在原List的基础上进行的，返回的分区是不可变的抽象列表，原列表元素变更，分区中元素也会变更。
 * 参考：Guava的Lists#Partition
 *
 * @param <T> 元素类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class Partition<T> extends AbstractList<List<T>> {

    /**
     * 被分区的列表
     */
    protected final List<T> list;
    /**
     * 每个分区的长度
     */
    protected final int size;

    /**
     * 列表分区
     *
     * @param list 被分区的列表
     * @param size 每个分区的长度
     */
    public Partition(final List<T> list, final int size) {
        this.list = Assert.notNull(list);
        this.size = Math.min(size, list.size());
    }

    @Override
    public List<T> get(final int index) {
        final int start = index * size;
        final int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    @Override
    public int size() {
        // 此处采用动态计算，以应对list变
        final int size = this.size;
        if (0 == size) {
            return 0;
        }

        final int total = list.size();
        // 类似于判断余数，当总数非整份size时，多余的数>=1，则相当于被除数多一个size，做到+1目的
        // 类似于：if(total % size > 0){length += 1;}
        return (total + size - 1) / size;
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

}
