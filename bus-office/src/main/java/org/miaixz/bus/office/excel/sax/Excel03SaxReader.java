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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.eventusermodel.EventWorkbookBuilder.SheetRecordCollectingListener;
import org.apache.poi.hssf.eventusermodel.dummyrecord.LastCellOfRowDummyRecord;
import org.apache.poi.hssf.eventusermodel.dummyrecord.MissingCellDummyRecord;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.*;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.TerminateException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.office.excel.sax.handler.RowHandler;
import org.miaixz.bus.office.excel.xyz.ExcelSaxKit;

/**
 * Excel2003格式的事件-用户模型方式读取器，统一将此归类为Sax读取 参考：http://www.cnblogs.com/wshsdlau/p/5643862.html
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Excel03SaxReader implements HSSFListener, ExcelSaxReader<Excel03SaxReader> {

    /**
     * 如果为公式，true表示输出公式计算后的结果值，false表示输出公式本身
     */
    private final boolean isOutputFormulaValues = true;
    /**
     * Sheet边界记录，此Record中可以获得Sheet名
     */
    private final List<BoundSheetRecord> boundSheetRecords = new ArrayList<>();
    /**
     * 行处理器
     */
    private final RowHandler rowHandler;
    /**
     * 用于解析公式
     */
    private SheetRecordCollectingListener workbookBuildingListener;
    /**
     * 子工作簿，用于公式计算
     */
    private HSSFWorkbook stubWorkbook;
    /**
     * 静态字符串表
     */
    private SSTRecord sstRecord;
    private FormatTrackingHSSFListener formatListener;
    private boolean isOutputNextStringRecord;
    /**
     * 存储行记录的容器
     */
    private List<Object> rowCellList = new ArrayList<>();
    /**
     * 自定义需要处理的sheet编号，如果-1表示处理所有sheet
     */
    private int rid = -1;
    /**
     * sheet名称，主要用于使用sheet名读取的情况
     */
    private String sheetName;
    /**
     * 当前rid索引
     */
    private int curRid = -1;

    /**
     * 构造
     *
     * @param rowHandler 行处理器
     */
    public Excel03SaxReader(final RowHandler rowHandler) {
        this.rowHandler = rowHandler;
    }

    @Override
    public Excel03SaxReader read(final File file, final String idOrRidOrSheetName) throws InternalException {
        try (final POIFSFileSystem poifsFileSystem = new POIFSFileSystem(file, true)) {
            return read(poifsFileSystem, idOrRidOrSheetName);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public Excel03SaxReader read(final InputStream excelStream, final String idOrRidOrSheetName)
            throws InternalException {
        try {
            return read(new POIFSFileSystem(excelStream), idOrRidOrSheetName);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 读取
     *
     * @param fs                 {@link POIFSFileSystem}
     * @param idOrRidOrSheetName sheet id或者rid编号或sheet名称，从0开始，rid必须加rId前缀，例如rId0，如果为-1处理所有编号的sheet
     * @return this
     * @throws InternalException IO异常包装
     */
    public Excel03SaxReader read(final POIFSFileSystem fs, final String idOrRidOrSheetName) throws InternalException {
        this.rid = getSheetIndex(idOrRidOrSheetName);

        formatListener = new FormatTrackingHSSFListener(new MissingRecordAwareHSSFListener(this));
        final HSSFRequest request = new HSSFRequest();
        if (isOutputFormulaValues) {
            request.addListenerForAllRecords(formatListener);
        } else {
            workbookBuildingListener = new SheetRecordCollectingListener(formatListener);
            request.addListenerForAllRecords(workbookBuildingListener);
        }
        final HSSFEventFactory factory = new HSSFEventFactory();
        try {
            factory.processWorkbookEvents(request, fs);
        } catch (final IOException e) {
            throw new InternalException(e);
        } catch (final TerminateException e) {
            // 用户抛出此异常，表示强制结束读取
        } finally {
            IoKit.closeQuietly(fs);
        }
        return this;
    }

    /**
     * 获得Sheet序号，如果处理所有sheet，获得最大的Sheet序号，从0开始
     *
     * @return sheet序号
     */
    public int getSheetIndex() {
        return this.rid;
    }

    /**
     * 获得Sheet名，如果处理所有sheet，获得后一个Sheet名，从0开始
     *
     * @return Sheet名
     */
    public String getSheetName() {
        if (null != this.sheetName) {
            return this.sheetName;
        }

        if (this.boundSheetRecords.size() > this.rid) {
            return this.boundSheetRecords.get(this.rid > -1 ? this.rid : this.curRid).getSheetname();
        }

        return null;
    }

    /**
     * HSSFListener 监听方法，处理 Record
     *
     * @param record 记录
     */
    @Override
    public void processRecord(final Record record) {
        if (this.rid > -1 && this.curRid > this.rid) {
            // 指定Sheet之后的数据不再处理
            return;
        }

        if (record instanceof BoundSheetRecord) {
            // Sheet边界记录，此Record中可以获得Sheet名
            final BoundSheetRecord boundSheetRecord = (BoundSheetRecord) record;
            boundSheetRecords.add(boundSheetRecord);
            final String currentSheetName = boundSheetRecord.getSheetname();
            if (null != this.sheetName && StringKit.equals(this.sheetName, currentSheetName)) {
                this.rid = this.boundSheetRecords.size() - 1;
            }
        } else if (record instanceof SSTRecord) {
            // 静态字符串表
            sstRecord = (SSTRecord) record;
        } else if (record instanceof BOFRecord) {
            final BOFRecord bofRecord = (BOFRecord) record;
            if (bofRecord.getType() == BOFRecord.TYPE_WORKSHEET) {
                // 如果有需要，则建立子工作薄
                if (workbookBuildingListener != null && stubWorkbook == null) {
                    stubWorkbook = workbookBuildingListener.getStubHSSFWorkbook();
                }
                curRid++;
            }
        } else if (record instanceof EOFRecord) {
            if (this.rid < 0 && null != this.sheetName) {
                throw new InternalException("Sheet [{}] not exist!", this.sheetName);
            }
            if (this.curRid != -1 && isProcessCurrentSheet()) {
                // 只有在当前指定的sheet中，才触发结束事件，且curId=-1时也不处理，避免重复调用
                processLastCellSheet();
            }
        } else if (isProcessCurrentSheet()) {
            if (record instanceof MissingCellDummyRecord) {
                // 空值的操作
                final MissingCellDummyRecord mc = (MissingCellDummyRecord) record;
                addToRowCellList(mc);
            } else if (record instanceof LastCellOfRowDummyRecord) {
                // 行结束
                processLastCell((LastCellOfRowDummyRecord) record);
            } else {
                // 处理单元格值
                processCellValue(record);
            }
        }

    }

    /**
     * 将空数据加入到行列表中
     *
     * @param record MissingCellDummyRecord
     */
    private void addToRowCellList(final MissingCellDummyRecord record) {
        addToRowCellList(record.getRow(), record.getColumn(), Normal.EMPTY);
    }

    /**
     * 将单元格数据加入到行列表中
     *
     * @param record 单元格
     * @param value  值
     */
    private void addToRowCellList(final CellValueRecordInterface record, final Object value) {
        addToRowCellList(record.getRow(), record.getColumn(), value);
    }

    /**
     * 将单元格数据加入到行列表中
     *
     * @param row    行号
     * @param column 单元格
     * @param value  值
     */
    private void addToRowCellList(final int row, final int column, final Object value) {
        while (column > this.rowCellList.size()) {
            // 对于中间无数据的单元格补齐空白
            this.rowCellList.add(Normal.EMPTY);
            this.rowHandler.handleCell(this.curRid, row, rowCellList.size() - 1, value, null);
        }

        this.rowCellList.add(column, value);
        this.rowHandler.handleCell(this.curRid, row, column, value, null);
    }

    /**
     * 处理单元格值
     *
     * @param record 单元格
     */
    private void processCellValue(final Record record) {
        Object value = null;

        switch (record.getSid()) {
        case BlankRecord.sid:
            // 空白记录
            addToRowCellList(((BlankRecord) record), Normal.EMPTY);
            break;
        case BoolErrRecord.sid:
            // 布尔类型
            final BoolErrRecord berec = (BoolErrRecord) record;
            addToRowCellList(berec, berec.getBooleanValue());
            break;
        case FormulaRecord.sid:
            // 公式类型
            final FormulaRecord formulaRec = (FormulaRecord) record;
            if (isOutputFormulaValues) {
                if (Double.isNaN(formulaRec.getValue())) {
                    // Formula result is a string
                    // This is stored in the next record
                    isOutputNextStringRecord = true;
                } else {
                    value = ExcelSaxKit.getNumberOrDateValue(formulaRec, formulaRec.getValue(), this.formatListener);
                }
            } else {
                value = HSSFFormulaParser.toFormulaString(stubWorkbook, formulaRec.getParsedExpression());
            }
            addToRowCellList(formulaRec, value);
            break;
        case StringRecord.sid:
            // 单元格中公式的字符串
            if (isOutputNextStringRecord) {
                // String for formula
                // value = ((StringRecord) record).getString();
                isOutputNextStringRecord = false;
            }
            break;
        case LabelRecord.sid:
            final LabelRecord lrec = (LabelRecord) record;
            value = lrec.getValue();
            addToRowCellList(lrec, value);
            break;
        case LabelSSTRecord.sid:
            // 字符串类型
            final LabelSSTRecord lsrec = (LabelSSTRecord) record;
            if (null != sstRecord) {
                value = sstRecord.getString(lsrec.getSSTIndex()).toString();
            }
            addToRowCellList(lsrec, ObjectKit.defaultIfNull(value, Normal.EMPTY));
            break;
        case NumberRecord.sid: // 数字类型
            final NumberRecord numrec = (NumberRecord) record;
            value = ExcelSaxKit.getNumberOrDateValue(numrec, numrec.getValue(), this.formatListener);
            // 向容器加入列值
            addToRowCellList(numrec, value);
            break;
        default:
            break;
        }
    }

    /**
     * 处理行结束后的操作，{@link LastCellOfRowDummyRecord}是行结束的标识Record
     *
     * @param lastCell 行结束的标识Record
     */
    private void processLastCell(final LastCellOfRowDummyRecord lastCell) {
        // 每行结束时， 调用handle() 方法
        this.rowHandler.handle(curRid, lastCell.getRow(), this.rowCellList);
        // 清空行Cache
        this.rowCellList = new ArrayList<>(this.rowCellList.size());
    }

    /**
     * 处理sheet结束后的操作
     */
    private void processLastCellSheet() {
        this.rowHandler.doAfterAllAnalysed();
    }

    /**
     * 是否处理当前sheet
     *
     * @return 是否处理当前sheet
     */
    private boolean isProcessCurrentSheet() {
        // rid < 0 且 sheet名称存在，说明没有匹配到sheet名称
        return (this.rid < 0 && null == this.sheetName) || this.rid == this.curRid;
    }

    /**
     * 获取sheet索引，从0开始
     * <ul>
     * <li>传入'rId'开头，直接去除rId前缀</li>
     * <li>传入纯数字，表示sheetIndex，直接转换为rid</li>
     * </ul>
     *
     * @param idOrRidOrSheetName Excel中的sheet id或者rid编号或sheet名称，从0开始，rid必须加rId前缀，例如rId0，如果为-1处理所有编号的sheet
     * @return sheet索引，从0开始
     */
    private int getSheetIndex(final String idOrRidOrSheetName) {
        Assert.notBlank(idOrRidOrSheetName, "id or rid or sheetName must be not blank!");

        // rid直接处理
        if (StringKit.startWithIgnoreCase(idOrRidOrSheetName, RID_PREFIX)) {
            return Integer.parseInt(StringKit.removePrefixIgnoreCase(idOrRidOrSheetName, RID_PREFIX));
        } else if (StringKit.startWithIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX)) {
            // 支持任意名称
            this.sheetName = StringKit.removePrefixIgnoreCase(idOrRidOrSheetName, SHEET_NAME_PREFIX);
        } else {
            try {
                return Integer.parseInt(idOrRidOrSheetName);
            } catch (final NumberFormatException ignore) {
                // 如果用于传入非数字，按照sheet名称对待
                this.sheetName = idOrRidOrSheetName;
            }
        }

        return -1;
    }

}
