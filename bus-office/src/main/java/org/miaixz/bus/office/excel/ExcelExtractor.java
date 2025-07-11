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
package org.miaixz.bus.office.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * {@link org.apache.poi.ss.extractor.ExcelExtractor}工具封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExcelExtractor {

    /**
     * 获取 {@link org.apache.poi.ss.extractor.ExcelExtractor} 对象
     *
     * @param wb {@link Workbook}
     * @return {@link org.apache.poi.ss.extractor.ExcelExtractor}
     */
    public static org.apache.poi.ss.extractor.ExcelExtractor getExtractor(final Workbook wb) {
        final org.apache.poi.ss.extractor.ExcelExtractor extractor;
        if (wb instanceof HSSFWorkbook) {
            extractor = new org.apache.poi.hssf.extractor.ExcelExtractor((HSSFWorkbook) wb);
        } else {
            extractor = new XSSFExcelExtractor((XSSFWorkbook) wb);
        }
        return extractor;
    }

    /**
     * 读取为文本格式 使用{@link org.apache.poi.ss.extractor.ExcelExtractor} 提取Excel内容
     *
     * @param wb            {@link Workbook}
     * @param withSheetName 是否附带sheet名
     * @return Excel文本
     */
    public static String readAsText(final Workbook wb, final boolean withSheetName) {
        final org.apache.poi.ss.extractor.ExcelExtractor extractor = getExtractor(wb);
        extractor.setIncludeSheetNames(withSheetName);
        return extractor.getText();
    }

}
