/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.mapper.builder;

import java.util.List;

import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * 实体类表工厂处理链，支持单例，线程安全
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TableSchemaChain implements TableSchemaBuilder.Chain {

    /**
     * 表工厂列表
     */
    private final List<TableSchemaBuilder> factories;

    /**
     * 下一个处理链节点
     */
    private final TableSchemaChain next;

    /**
     * 当前工厂索引
     */
    private final int index;

    /**
     * 构造函数，初始化表工厂处理链
     *
     * @param factories 表工厂列表
     */
    public TableSchemaChain(List<TableSchemaBuilder> factories) {
        this(factories, 0);
    }

    /**
     * 私有构造函数，初始化处理链节点
     *
     * @param factories 表工厂列表
     * @param index     当前工厂索引
     */
    private TableSchemaChain(List<TableSchemaBuilder> factories, int index) {
        this.factories = factories;
        this.index = index;
        if (this.index < this.factories.size()) {
            this.next = new TableSchemaChain(factories, this.index + 1);
        } else {
            this.next = null;
        }
    }

    /**
     * 创建实体表信息，链式调用表工厂
     *
     * @param entityClass 实体类类型
     * @return 实体表信息，若无工厂处理则返回 null
     */
    @Override
    public TableMeta createTable(Class<?> entityClass) {
        if (index < factories.size()) {
            return factories.get(index).createTable(entityClass, next);
        }
        return null;
    }

}