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
package org.miaixz.bus.shade.screw.dialect.oracle;

import org.miaixz.bus.shade.screw.mapping.MappingField;
import org.miaixz.bus.shade.screw.metadata.Column;

import lombok.Getter;
import lombok.Setter;

/**
 * Oracle table column
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class OracleColumn implements Column {
    /**
     * 表名称，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null）
     */
    @MappingField(value = "SCOPE_TABLE")
    private Object scopeTable;
    /**
     * 表类别
     */
    @MappingField(value = "TABLE_CAT")
    private Object tableCat;
    /**
     * 未被使用
     */
    @MappingField(value = "BUFFER_LENGTH")
    private String bufferLength;
    /**
     * ISO 规则用于确定列是否包括 null。
     */
    @MappingField(value = "IS_NULLABLE")
    private String isNullable;
    /**
     * 数据源依赖的类型名称，对于 UDT，该类型名称是完全限定的
     */
    @MappingField(value = "TABLE_NAME")
    private String tableName;
    /**
     * 该列的默认值，当值在单引号内时应被解释为一个字符串（可为 null）
     */
    @MappingField(value = "COLUMN_DEF")
    private String columnDef;
    /**
     * 表的类别，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null）
     */
    @MappingField(value = "SCOPE_CATALOG")
    private Object scopeCatalog;
    /**
     * 表模式
     */
    @MappingField(value = "TABLE_SCHEM")
    private String tableSchem;
    /**
     * 列名称
     */
    @MappingField(value = "COLUMN_NAME")
    private String columnName;
    /**
     * 是否允许使用 NULL。
     */
    @MappingField(value = "NULLABLE")
    private String nullable;
    /**
     * 描述列的注释（可为 null）
     */
    @MappingField(value = "REMARKS")
    private String remarks;
    /**
     * 小数部分的位数。对于 DECIMAL_DIGITS 不适用的数据类型，则返回 Null。
     */
    @MappingField(value = "DECIMAL_DIGITS")
    private String decimalDigits;
    /**
     * 基数（通常为 10 或 2）
     */
    @MappingField(value = "NUM_PREC_RADIX")
    private String numPrecRadix;
    /**
     *
     */
    @MappingField(value = "SQL_DATETIME_SUB")
    private String sqlDatetimeSub;
    /**
     *
     */
    @MappingField(value = "IS_GENERATEDCOLUMN")
    private String isGeneratedColumn;
    /**
     * 指示此列是否自动增加 YES --- 如果该列自动增加 NO --- 如果该列不自动增加
     */
    @MappingField(value = "IS_AUTOINCREMENT")
    private String isAutoIncrement;
    /**
     * SQL数据类型
     */
    @MappingField(value = "SQL_DATA_TYPE")
    private String sqlDataType;
    /**
     * 对于 char 类型，该长度是列中的最大字节数
     */
    @MappingField(value = "CHAR_OCTET_LENGTH")
    private String charOctetLength;
    /**
     * 表中的列的索引（从 1 开始）
     */
    @MappingField(value = "ORDINAL_POSITION")
    private String ordinalPosition;
    /**
     * 表的模式，它是引用属性的作用域（如果 DATA_TYPE 不是 REF，则为 null）
     */
    @MappingField(value = "SCOPE_SCHEMA")
    private String scopeSchema;
    /**
     * 不同类型或用户生成 Ref 类型、来自 java.sql.Types 的 SQL 类型的源类型（如果 DATA_TYPE 不是 DISTINCT 或用户生成的 REF，则为 null）
     */
    @MappingField(value = "SOURCE_DATA_TYPE")
    private String sourceDataType;
    /**
     * 来自 java.sql.Types 的 SQL 类型
     */
    @MappingField(value = "DATA_TYPE")
    private String dataType;
    /**
     * 数据源依赖的类型名称，对于 UDT，该类型名称是完全限定的
     */
    @MappingField(value = "TYPE_NAME")
    private String typeName;
    /**
     * 列表示给定列的指定列大小。 对于数值数据，这是最大精度。 对于字符数据，这是字符长度。 对于日期时间数据类型，这是 String 表示形式的字符长度（假定允许的最大小数秒组件的精度）。 对于二进制数据，这是字节长度。 对于
     * ROWID 数据类型，这是字节长度。对于列大小不适用的数据类型，则返回 Null。
     */
    @MappingField(value = "COLUMN_SIZE")
    private String columnSize;

    /**
     * 是否主键
     */
    private String primaryKey;
    /**
     * 列类型（带长度）
     */
    @MappingField(value = "COLUMN_TYPE")
    private String columnType;

    /**
     * 列长度
     */
    @MappingField(value = "COLUMN_LENGTH")
    private String columnLength;

}
