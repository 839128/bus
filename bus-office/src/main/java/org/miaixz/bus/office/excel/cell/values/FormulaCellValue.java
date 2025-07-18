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
package org.miaixz.bus.office.excel.cell.values;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.miaixz.bus.office.excel.cell.setters.CellSetter;

/**
 * 公式类型的值
 *
 * <ul>
 * <li>在Sax读取模式时，此对象用于接收单元格的公式以及公式结果值信息</li>
 * <li>在写出模式时，用于定义写出的单元格类型为公式</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormulaCellValue implements CellValue<String>, CellSetter {

    /**
     * 公式
     */
    private final String formula;
    /**
     * 结果，使用ExcelWriter时可以不用
     */
    private final Object result;
    private final CellType resultType;

    /**
     * 构造
     *
     * @param formula 公式
     */
    public FormulaCellValue(final String formula) {
        this(formula, null);
    }

    /**
     * 构造
     *
     * @param formula 公式
     * @param result  结果
     */
    public FormulaCellValue(final String formula, final Object result) {
        this(formula, result, null);
    }

    /**
     * 构造
     *
     * @param formula    公式
     * @param result     结果
     * @param resultType 结果类型
     */
    public FormulaCellValue(final String formula, final Object result, final CellType resultType) {
        this.formula = formula;
        this.result = result;
        this.resultType = resultType;
    }

    /**
     * 获取结果类型
     *
     * @return 结果类型，{@code null}表示未明确
     */
    public CellType getResultType() {
        return this.resultType;
    }

    @Override
    public String getValue() {
        return this.formula;
    }

    @Override
    public void setValue(final Cell cell) {
        cell.setCellFormula(this.formula);
    }

    /**
     * 获取结果
     *
     * @return 结果
     */
    public Object getResult() {
        return this.result;
    }

    @Override
    public String toString() {
        return getResult().toString();
    }

}
