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
package org.miaixz.bus.office.excel.sax;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.cell.values.FormulaCellValue;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.xyz.ExcelSaxKit;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * sheetData标签内容读取处理器
 *
 * <pre>
 * &lt;sheetData&gt;&lt;/sheetData&gt;
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SheetDataSaxHandler extends DefaultHandler {

    // 上一次的内容
    private final StringBuilder lastContent = new StringBuilder();
    // 配置项：是否对齐数据，即在行尾补充null cell
    private final boolean padCellAtEndOfRow;
    // 上一次的内容
    private final StringBuilder lastFormula = new StringBuilder();
    /**
     * 行处理器
     */
    protected RowHandler rowHandler;
    // 单元格的格式表，对应style.xml
    protected StylesTable stylesTable;
    // excel 2007 的共享字符串表,对应sharedString.xml
    protected SharedStrings sharedStrings;
    // sheet的索引，从0开始
    protected int sheetIndex;
    // 当前非空行
    protected int index;
    // 当前列
    private int curCell;
    // 单元数据类型
    private CellDataType cellDataType;
    // 当前行号，从0开始
    private long rowNumber;
    // 当前列坐标， 如A1，B5
    private String curCoordinate;
    // 当前节点名称
    private ElementName curElementName;
    // 前一个列的坐标
    private String preCoordinate;
    // 行的最大列坐标
    private String maxCellCoordinate;
    // 单元格样式
    private XSSFCellStyle xssfCellStyle;
    // 单元格存储的格式化字符串，nmtFmt的formatCode属性的值
    private String numFmtString;
    // 是否处于sheetData标签内，sax只解析此标签内的内容，其它标签忽略
    private boolean isInSheetData;
    // 存储每行的列元素
    private List<Object> rowCellList = new ArrayList<>();

    /**
     * 构造
     *
     * @param rowHandler        行处理器
     * @param padCellAtEndOfRow 是否对齐数据，即在行尾补充null cell
     */
    public SheetDataSaxHandler(final RowHandler rowHandler, final boolean padCellAtEndOfRow) {
        this.rowHandler = rowHandler;
        this.padCellAtEndOfRow = padCellAtEndOfRow;
    }

    /**
     * 设置行处理器
     *
     * @param rowHandler 行处理器
     */
    public void setRowHandler(final RowHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    /**
     * 读到一个xml开始标签时的回调处理方法
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) {
        if ("sheetData".equals(qName)) {
            this.isInSheetData = true;
            return;
        }

        if (!this.isInSheetData) {
            // 非sheetData标签，忽略解析
            return;
        }

        final ElementName name = ElementName.of(qName);
        this.curElementName = name;

        if (null != name) {
            switch (name) {
            case row:
                // 行开始
                startRow(attributes);
                break;
            case c:
                // 单元格元素
                startCell(attributes);
                break;
            }
        }
    }

    /**
     * 标签结束的回调处理方法
     */
    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        if ("sheetData".equals(qName)) {
            // sheetData结束，不再解析别的标签
            this.isInSheetData = false;
            return;
        }

        if (!this.isInSheetData) {
            // 非sheetData标签，忽略解析
            return;
        }

        this.curElementName = null;
        if (ElementName.c.match(qName)) { // 单元格结束
            endCell();
        } else if (ElementName.row.match(qName)) {// 行结束
            endRow();
        }
        // 其它标签忽略
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (!this.isInSheetData) {
            // 非sheetData标签，忽略解析
            return;
        }

        final ElementName elementName = this.curElementName;
        if (null != elementName) {
            switch (elementName) {
            case v:
                // 得到单元格内容的值
                lastContent.append(ch, start, length);
                break;
            case f:
                // 得到单元格内容的值
                lastFormula.append(ch, start, length);
                break;
            }
        } else {
            // 按理说内容应该为"<v>内容</v>"，但是某些特别的XML内容不在v或f标签中，此处做一些兼容
            lastContent.append(ch, start, length);
        }
    }

    /**
     * 行开始
     *
     * @param attributes 属性列表
     */
    private void startRow(final Attributes attributes) {
        final String rValue = AttributeName.r.getValue(attributes);
        this.rowNumber = (null == rValue) ? -1 : Long.parseLong(rValue) - 1;
    }

    /**
     * 单元格开始
     *
     * @param attributes 属性列表
     */
    private void startCell(final Attributes attributes) {
        // 获取当前列坐标
        final String tempCurCoordinate = AttributeName.r.getValue(attributes);
        // 前一列为null，则将其设置为"@",A为第一列，ascii码为65，前一列即为@，ascii码64
        if (preCoordinate == null) {
            preCoordinate = String.valueOf(Symbol.C_AT);
        } else {
            // 存在，则前一列要设置为上一列的坐标
            preCoordinate = curCoordinate;
        }
        // 重置当前列
        curCoordinate = tempCurCoordinate;
        // 设置单元格类型
        setCellType(attributes);

        // 清空之前的数据
        lastContent.setLength(0);
        lastFormula.setLength(0);
    }

    /**
     * 一行结尾
     */
    private void endRow() {
        // 最大列坐标以第一个非空行的为准
        if (index == 0) {
            maxCellCoordinate = curCoordinate;
        }

        // 补全一行尾部可能缺失的单元格
        if (padCellAtEndOfRow && maxCellCoordinate != null) {
            padCell(curCoordinate, maxCellCoordinate, true);
        }

        rowHandler.handle(sheetIndex, rowNumber, rowCellList);

        // 一行结束
        // 新建一个新列，之前的列抛弃（可能被回收或rowHandler处理）
        rowCellList = new ArrayList<>(curCell + 1);
        // 行数增加
        index++;
        // 当前列置0
        curCell = 0;
        // 置空当前列坐标和前一列坐标
        curCoordinate = null;
        preCoordinate = null;
    }

    /**
     * 一个单元格结尾
     */
    private void endCell() {
        // 补全单元格之间的空格
        padCell(preCoordinate, curCoordinate, false);

        final String contentStr = StringKit.trim(lastContent);
        final Object value;
        if (this.lastFormula.length() > 0) {
            if (CellDataType.NULL == this.cellDataType) {
                // 对于公式，默认值类型为数字
                this.cellDataType = CellDataType.NUMBER;
            }
            value = new FormulaCellValue(StringKit.trim(lastFormula),
                    ExcelSaxKit.getDataValue(this.cellDataType, contentStr, this.sharedStrings, this.numFmtString));
        } else {
            // 默认的cellDataType是NULL而非NUMBER
            value = ExcelSaxKit.getDataValue(this.cellDataType, contentStr, this.sharedStrings, this.numFmtString);
        }
        addCellValue(curCell++, value);
    }

    /**
     * 在一行中的指定列增加值
     *
     * @param index 位置
     * @param value 值
     */
    private void addCellValue(final int index, final Object value) {
        this.rowCellList.add(index, value);
        this.rowHandler.handleCell(this.sheetIndex, this.rowNumber, index, value, this.xssfCellStyle);
    }

    /**
     * 填充空白单元格，如果前一个单元格大于后一个，不需要填充
     *
     * @param preCoordinate 前一个单元格坐标
     * @param curCoordinate 当前单元格坐标
     * @param isEnd         是否为最后一个单元格
     */
    private void padCell(final String preCoordinate, final String curCoordinate, final boolean isEnd) {
        if (!curCoordinate.equals(preCoordinate)) {
            int len = ExcelSaxKit.countNullCell(preCoordinate, curCoordinate);
            if (isEnd) {
                len++;
            }
            while (len-- > 0) {
                addCellValue(curCell++, null);
            }
        }
    }

    /**
     * 设置单元格的类型
     *
     * @param attributes 属性
     */
    private void setCellType(final Attributes attributes) {
        // numFmtString的值
        numFmtString = Normal.EMPTY;
        this.cellDataType = CellDataType.of(AttributeName.t.getValue(attributes));

        // 获取单元格的xf索引，对应style.xml中cellXfs的子元素xf
        if (null != this.stylesTable) {
            final String xfIndexStr = AttributeName.s.getValue(attributes);
            if (null != xfIndexStr) {
                this.xssfCellStyle = stylesTable.getStyleAt(Integer.parseInt(xfIndexStr));
                // 单元格存储格式的索引，对应style.xml中的numFmts元素的子元素索引
                final int numFmtIndex = xssfCellStyle.getDataFormat();
                this.numFmtString = ObjectKit.defaultIfNull(xssfCellStyle.getDataFormatString(),
                        () -> BuiltinFormats.getBuiltinFormat(numFmtIndex));

                // 日期格式的单元格可能没有t元素
                if ((CellDataType.NUMBER == this.cellDataType || CellDataType.NULL == this.cellDataType)
                        && ExcelSaxKit.isDateFormat(numFmtIndex, numFmtString)) {
                    cellDataType = CellDataType.DATE;
                }
            }
        }

    }

}
