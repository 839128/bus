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
package org.miaixz.bus.office.excel.cell;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.miaixz.bus.office.excel.cell.values.FormulaCellValue;
import org.miaixz.bus.office.excel.xyz.CellKit;

/**
 * 虚拟单元格，表示一个单元格的位置、值和样式，但是并非实际创建的单元格 注意：虚拟单元格设置值和样式均不会在实际工作簿中生效
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VirtualCell extends CellBase {

    private final Row row;
    private final int columnIndex;
    private final int rowIndex;

    private CellType cellType;
    private Object value;
    private CellStyle style;
    private Comment comment;

    /**
     * 构造
     *
     * @param cell 参照单元格
     * @param x    新的列号，从0开始
     * @param y    新的行号，从0开始
     */
    public VirtualCell(final Cell cell, final int x, final int y) {
        this(cell.getRow(), x, y);
        this.cellType = cell.getCellType();
        this.value = CellKit.getCellValue(cell);
        this.style = cell.getCellStyle();
        this.comment = cell.getCellComment();
    }

    /**
     * 构造
     *
     * @param cell  参照单元格
     * @param x     新的列号，从0开始
     * @param y     新的行号，从0开始
     * @param value 新值
     */
    public VirtualCell(final Cell cell, final int x, final int y, final Object value) {
        this(cell.getRow(), x, y);
        this.style = cell.getCellStyle();
        this.comment = cell.getCellComment();
        CellKit.setCellValue(this, value);
    }

    /**
     * 构造
     *
     * @param row 行
     * @param y   行号，从0开始
     * @param x   列号，从0开始
     */
    public VirtualCell(final Row row, final int x, final int y) {
        this.row = row;
        this.rowIndex = y;
        this.columnIndex = x;
    }

    @Override
    protected void setCellTypeImpl(final CellType cellType) {
        this.cellType = cellType;
    }

    @Override
    protected void setCellFormulaImpl(final String formula) {
        this.value = new FormulaCellValue(formula);
    }

    @Override
    protected void removeFormulaImpl() {
        if (this.value instanceof FormulaCellValue) {
            this.value = null;
        }
    }

    @Override
    protected void setCellValueImpl(final double value) {
        this.cellType = CellType.NUMERIC;
        this.value = value;
    }

    @Override
    protected void setCellValueImpl(final Date value) {
        this.cellType = CellType.NUMERIC;
        this.value = value;
    }

    @Override
    protected void setCellValueImpl(final LocalDateTime value) {
        this.cellType = CellType.NUMERIC;
        this.value = value;
    }

    @Override
    protected void setCellValueImpl(final Calendar value) {
        this.cellType = CellType.NUMERIC;
        this.value = value;
    }

    @Override
    protected void setCellValueImpl(final String value) {
        this.cellType = CellType.STRING;
        this.value = value;
    }

    @Override
    protected void setCellValueImpl(final RichTextString value) {
        this.cellType = CellType.STRING;
        this.value = value;
    }

    @Override
    protected SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL2007;
    }

    @Override
    public int getColumnIndex() {
        return this.columnIndex;
    }

    @Override
    public int getRowIndex() {
        return this.rowIndex;
    }

    @Override
    public Sheet getSheet() {
        return this.row.getSheet();
    }

    @Override
    public Row getRow() {
        return this.row;
    }

    @Override
    public CellType getCellType() {
        return this.cellType;
    }

    @Override
    public CellType getCachedFormulaResultType() {
        if (this.value instanceof FormulaCellValue) {
            return ((FormulaCellValue) this.value).getResultType();
        }
        return null;
    }

    @Override
    public String getCellFormula() {
        if (this.value instanceof FormulaCellValue) {
            return ((FormulaCellValue) this.value).getValue();
        }
        return null;
    }

    @Override
    public double getNumericCellValue() {
        return (double) this.value;
    }

    @Override
    public Date getDateCellValue() {
        return (Date) this.value;
    }

    @Override
    public LocalDateTime getLocalDateTimeCellValue() {
        return (LocalDateTime) this.value;
    }

    @Override
    public RichTextString getRichStringCellValue() {
        return (RichTextString) this.value;
    }

    @Override
    public String getStringCellValue() {
        return (String) this.value;
    }

    @Override
    public void setCellValue(final boolean value) {
        this.cellType = CellType.BOOLEAN;
        this.value = value;
    }

    @Override
    public void setCellErrorValue(final byte value) {
        this.cellType = CellType.ERROR;
        this.value = value;
    }

    @Override
    public boolean getBooleanCellValue() {
        return (boolean) this.value;
    }

    @Override
    public byte getErrorCellValue() {
        return (byte) this.value;
    }

    @Override
    public CellStyle getCellStyle() {
        return this.style;
    }

    @Override
    public void setCellStyle(final CellStyle style) {
        this.style = style;
    }

    @Override
    public void setAsActiveCell() {
        throw new UnsupportedOperationException("Virtual cell cannot be set as active cell");
    }

    @Override
    public Comment getCellComment() {
        return this.comment;
    }

    @Override
    public void setCellComment(final Comment comment) {
        this.comment = comment;
    }

    @Override
    public void removeCellComment() {
        this.comment = null;
    }

    @Override
    public Hyperlink getHyperlink() {
        return (Hyperlink) this.value;
    }

    @Override
    public void setHyperlink(final Hyperlink link) {
        this.value = link;
    }

    @Override
    public void removeHyperlink() {
        if (this.value instanceof Hyperlink) {
            this.value = null;
        }
    }

    @Override
    public CellRangeAddress getArrayFormulaRange() {
        return null;
    }

    @Override
    public boolean isPartOfArrayFormulaGroup() {
        return false;
    }

}
