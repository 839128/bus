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
package org.miaixz.bus.office.excel.reader;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.office.excel.ExcelBase;
import org.miaixz.bus.office.excel.ExcelExtractor;
import org.miaixz.bus.office.excel.writer.ExcelWriter;
import org.miaixz.bus.office.excel.xyz.CellKit;
import org.miaixz.bus.office.excel.xyz.ExcelKit;
import org.miaixz.bus.office.excel.xyz.RowKit;
import org.miaixz.bus.office.excel.xyz.WorkbookKit;

/**
 * Excel读取器 读取Excel工作簿
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelReader extends ExcelBase<ExcelReader, ExcelReadConfig> {

    /**
     * 构造
     *
     * @param excelFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @param sheetIndex    sheet序号，0表示第一个sheet
     */
    public ExcelReader(final String excelFilePath, final int sheetIndex) {
        this(FileKit.file(excelFilePath), sheetIndex);
    }

    /**
     * 构造
     *
     * @param excelFilePath Excel文件路径，绝对路径或相对于ClassPath路径
     * @param sheetName     sheet名，第一个默认是sheet1
     */
    public ExcelReader(final String excelFilePath, final String sheetName) {
        this(FileKit.file(excelFilePath), sheetName);
    }

    /**
     * 构造（读写方式读取）
     *
     * @param bookFile   Excel文件
     * @param sheetIndex sheet序号，0表示第一个sheet
     */
    public ExcelReader(final File bookFile, final int sheetIndex) {
        this(WorkbookKit.createBook(bookFile, true), sheetIndex);
        this.targetFile = bookFile;
    }

    /**
     * 构造（读写方式读取）
     *
     * @param bookFile  Excel文件
     * @param sheetName sheet名，第一个默认是sheet1
     */
    public ExcelReader(final File bookFile, final String sheetName) {
        this(WorkbookKit.createBook(bookFile, true), sheetName);
        this.targetFile = bookFile;
    }

    /**
     * 构造（只读方式读取）
     *
     * @param bookStream Excel文件的流
     * @param sheetIndex sheet序号，0表示第一个sheet
     */
    public ExcelReader(final InputStream bookStream, final int sheetIndex) {
        this(WorkbookKit.createBook(bookStream), sheetIndex);
    }

    /**
     * 构造（只读方式读取）
     *
     * @param bookStream Excel文件的流
     * @param sheetName  sheet名，第一个默认是sheet1
     */
    public ExcelReader(final InputStream bookStream, final String sheetName) {
        this(WorkbookKit.createBook(bookStream), sheetName);
    }

    /**
     * 构造
     *
     * @param book       {@link Workbook} 表示一个Excel文件
     * @param sheetIndex sheet序号，0表示第一个sheet
     */
    public ExcelReader(final Workbook book, final int sheetIndex) {
        this(getSheetOrCloseWorkbook(book, sheetIndex));
    }

    /**
     * 构造
     *
     * @param book      {@link Workbook} 表示一个Excel文件
     * @param sheetName sheet名，第一个默认是sheet1
     */
    public ExcelReader(final Workbook book, final String sheetName) {
        this(getSheetOrCloseWorkbook(book, sheetName));
    }

    /**
     * 构造
     *
     * @param sheet Excel中的sheet
     */
    public ExcelReader(final Sheet sheet) {
        super(new ExcelReadConfig(), sheet);
    }

    /**
     * 获取Sheet，如果不存在则关闭{@link Workbook}并抛出异常，解决当sheet不存在时，文件依旧被占用问题
     *
     * @param workbook {@link Workbook}，非空
     * @param name     sheet名称，不存在抛出异常
     * @return {@link Sheet}
     * @throws IllegalArgumentException workbook为空或sheet不能存在
     */
    private static Sheet getSheetOrCloseWorkbook(final Workbook workbook, String name) throws IllegalArgumentException {
        Assert.notNull(workbook);
        if (null == name) {
            name = "sheet1";
        }
        final Sheet sheet = workbook.getSheet(name);
        if (null == sheet) {
            IoKit.closeQuietly(workbook);
            throw new IllegalArgumentException("Sheet [" + name + "] not exist!");
        }
        return sheet;
    }

    /**
     * 获取Sheet，如果不存在则关闭{@link Workbook}并抛出异常，解决当sheet不存在时，文件依旧被占用问题
     *
     * @param workbook   {@link Workbook}，非空
     * @param sheetIndex sheet index
     * @return {@link Sheet}
     * @throws IllegalArgumentException workbook为空或sheet不能存在
     */
    private static Sheet getSheetOrCloseWorkbook(final Workbook workbook, final int sheetIndex)
            throws IllegalArgumentException {
        Assert.notNull(workbook);
        final Sheet sheet;
        try {
            sheet = workbook.getSheetAt(sheetIndex);
        } catch (final IllegalArgumentException e) {
            IoKit.closeQuietly(workbook);
            throw e;
        }
        if (null == sheet) {
            IoKit.closeQuietly(workbook);
            throw new IllegalArgumentException("Sheet at [" + sheetIndex + "] not exist!");
        }
        return sheet;
    }

    /**
     * 读取工作簿中指定的Sheet的所有行列数据
     *
     * @return 行的集合，一行使用List表示
     */
    public List<List<Object>> read() {
        return read(0);
    }

    /**
     * 读取工作簿中指定的Sheet
     *
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @return 行的集合，一行使用List表示
     */
    public List<List<Object>> read(final int startRowIndex) {
        return read(startRowIndex, Integer.MAX_VALUE);
    }

    /**
     * 读取工作簿中指定的Sheet，此方法会把第一行作为标题行，替换标题别名
     *
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @param endRowIndex   结束行（包含，从0开始计数）
     * @return 行的集合，一行使用List表示
     */
    public List<List<Object>> read(final int startRowIndex, final int endRowIndex) {
        return read(startRowIndex, endRowIndex, false);
    }

    /**
     * 读取工作簿中指定的Sheet
     *
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param endRowIndex    结束行（包含，从0开始计数）
     * @param aliasFirstLine 是否首行作为标题行转换别名
     * @return 行的集合，一行使用List表示
     */
    public List<List<Object>> read(final int startRowIndex, final int endRowIndex, final boolean aliasFirstLine) {
        final ListSheetReader reader = new ListSheetReader(startRowIndex, endRowIndex, aliasFirstLine);
        reader.setExcelConfig(this.config);
        return read(reader);
    }

    /**
     * 读取工作簿中指定的Sheet中指定列
     *
     * @param columnIndex   列号，从0开始计数
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @return 列的集合
     */
    public List<Object> readColumn(final int columnIndex, final int startRowIndex) {
        return readColumn(columnIndex, startRowIndex, Integer.MAX_VALUE);
    }

    /**
     * 读取工作簿中指定的Sheet中指定列
     *
     * @param columnIndex   列号，从0开始计数
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @param endRowIndex   结束行（包含，从0开始计数）
     * @return 列的集合
     */
    public List<Object> readColumn(final int columnIndex, final int startRowIndex, final int endRowIndex) {
        final ColumnSheetReader reader = new ColumnSheetReader(columnIndex, startRowIndex, endRowIndex);
        reader.setExcelConfig(this.config);
        return read(reader);
    }

    /**
     * 读取工作簿中指定的Sheet，此方法为类流处理方式，当读到指定单元格时，会调用CellEditor接口 用户通过实现此接口，可以更加灵活地处理每个单元格的数据。
     *
     * @param cellHandler 单元格处理器，用于处理读到的单元格及其数据
     */
    public void read(final BiConsumerX<Cell, Object> cellHandler) {
        read(0, Integer.MAX_VALUE, cellHandler);
    }

    /**
     * 读取工作簿中指定的Sheet，此方法为类流处理方式，当读到指定单元格时，会调用CellEditor接口 用户通过实现此接口，可以更加灵活地处理每个单元格的数据。
     *
     * @param startRowIndex 起始行（包含，从0开始计数）
     * @param endRowIndex   结束行（包含，从0开始计数）
     * @param cellHandler   单元格处理器，用于处理读到的单元格及其数据
     */
    public void read(final int startRowIndex, final int endRowIndex, final BiConsumerX<Cell, Object> cellHandler) {
        checkClosed();

        final WalkSheetReader reader = new WalkSheetReader(startRowIndex, endRowIndex, cellHandler);
        reader.setExcelConfig(this.config);
        reader.read(sheet);
    }

    /**
     * 读取Excel为Map的列表，读取所有行，默认第一行做为标题，数据从第二行开始 Map表示一行，标题为key，单元格内容为value
     *
     * @return Map的列表
     */
    public List<Map<Object, Object>> readAll() {
        return read(0, 1, Integer.MAX_VALUE);
    }

    /**
     * 读取Excel为Map的列表 Map表示一行，标题为key，单元格内容为value
     *
     * @param headerRowIndex 标题所在行，如果标题行在读取的内容行中间，这行做为数据将忽略
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param endRowIndex    读取结束行（包含，从0开始计数）
     * @return Map的列表
     */
    public List<Map<Object, Object>> read(final int headerRowIndex, final int startRowIndex, final int endRowIndex) {
        final MapSheetReader reader = new MapSheetReader(headerRowIndex, startRowIndex, endRowIndex);
        reader.setExcelConfig(this.config);
        return read(reader);
    }

    /**
     * 读取Excel为Bean的列表，读取所有行，默认第一行做为标题，数据从第二行开始
     *
     * @param <T>      Bean类型
     * @param beanType 每行对应Bean的类型
     * @return Map的列表
     */
    public <T> List<T> readAll(final Class<T> beanType) {
        return read(0, 1, Integer.MAX_VALUE, beanType);
    }

    /**
     * 读取Excel为Bean的列表
     *
     * @param <T>            Bean类型
     * @param headerRowIndex 标题所在行，如果标题行在读取的内容行中间，这行做为数据将忽略，从0开始计数
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param beanType       每行对应Bean的类型
     * @return Map的列表
     */
    public <T> List<T> read(final int headerRowIndex, final int startRowIndex, final Class<T> beanType) {
        return read(headerRowIndex, startRowIndex, Integer.MAX_VALUE, beanType);
    }

    /**
     * 读取Excel为Bean的列表
     *
     * @param <T>            Bean类型
     * @param headerRowIndex 标题所在行，如果标题行在读取的内容行中间，这行做为数据将忽略，从0开始计数
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param endRowIndex    读取结束行（包含，从0开始计数）
     * @param beanType       每行对应Bean的类型
     * @return Map的列表
     */
    public <T> List<T> read(final int headerRowIndex, final int startRowIndex, final int endRowIndex,
            final Class<T> beanType) {
        final BeanSheetReader<T> reader = new BeanSheetReader<>(headerRowIndex, startRowIndex, endRowIndex, beanType);
        reader.setExcelConfig(this.config);
        return read(reader);
    }

    /**
     * 读取数据为指定类型
     *
     * @param <T>         读取数据类型
     * @param sheetReader {@link SheetReader}实现
     * @return 数据读取结果
     */
    public <T> T read(final SheetReader<T> sheetReader) {
        checkClosed();
        return Assert.notNull(sheetReader).read(this.sheet);
    }

    /**
     * 读取为文本格式 使用{@link org.apache.poi.ss.extractor.ExcelExtractor} 提取Excel内容
     *
     * @param withSheetName 是否附带sheet名
     * @return Excel文本
     */
    public String readAsText(final boolean withSheetName) {
        return ExcelExtractor.readAsText(this.workbook, withSheetName);
    }

    /**
     * 获取 {@link org.apache.poi.ss.extractor.ExcelExtractor} 对象
     *
     * @return {@link org.apache.poi.ss.extractor.ExcelExtractor}
     */
    public org.apache.poi.ss.extractor.ExcelExtractor getExtractor() {
        return ExcelExtractor.getExtractor(this.workbook);
    }

    /**
     * 读取某一行数据
     *
     * @param rowIndex 行号，从0开始
     * @return 一行数据
     */
    public List<Object> readRow(final int rowIndex) {
        return readRow(this.sheet.getRow(rowIndex));
    }

    /**
     * 读取某个单元格的值
     *
     * @param x X坐标，从0计数，即列号
     * @param y Y坐标，从0计数，即行号
     * @return 值，如果单元格无值返回null
     */
    public Object readCellValue(final int x, final int y) {
        return CellKit.getCellValue(getCell(x, y), this.config.getCellEditor());
    }

    /**
     * 获取Excel写出器 在读取Excel并做一定编辑后，获取写出器写出，规则如下：
     * <ul>
     * <li>1. 当从流中读取时，转换为Writer直接使用Sheet对象，此时修改不会影响源文件，Writer中flush需要指定新的路径。</li>
     * <li>2. 当从文件读取时，直接获取文件及sheet名称，此时可以修改原文件。</li>
     * </ul>
     *
     * @return {@link ExcelWriter}
     */
    public ExcelWriter getWriter() {
        if (null == this.targetFile) {
            // 非读取文件形式，直接获取sheet操作。
            return new ExcelWriter(this.sheet);
        }
        return ExcelKit.getWriter(this.targetFile, this.sheet.getSheetName());
    }

    /**
     * 读取一行
     *
     * @param row 行
     * @return 单元格值列表
     */
    private List<Object> readRow(final Row row) {
        return RowKit.readRow(row, this.config.getCellEditor());
    }

}
