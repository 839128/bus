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

import java.time.LocalDateTime;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.office.Builder;

/**
 * 数字类型单元格值 单元格值可能为Long、Double、Date
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumericCellValue implements CellValue<Object> {

    private final Cell cell;

    /**
     * 构造
     *
     * @param cell {@link Cell}
     */
    public NumericCellValue(final Cell cell) {
        this.cell = cell;
    }

    @Override
    public Object getValue() {
        final double value = cell.getNumericCellValue();

        final CellStyle style = cell.getCellStyle();
        if (null != style) {
            // 判断是否为日期
            if (Builder.isDateFormat(cell)) {
                final LocalDateTime date = cell.getLocalDateTimeCellValue();
                // 1899年写入会导致数据错乱，读取到1899年证明这个单元格的信息不关注年月日
                if (1899 == date.getYear()) {
                    return date.toLocalTime();
                }
                return date;
            }

            final String format = style.getDataFormatString();
            // 普通数字
            if (null != format && format.indexOf(Symbol.C_DOT) < 0) {
                final long longPart = (long) value;
                if (((double) longPart) == value) {
                    // 对于无小数部分的数字类型，转为Long
                    return longPart;
                }
            }
        }

        // 某些Excel单元格值为double计算结果，可能导致精度问题，通过转换解决精度问题。
        return Double.parseDouble(NumberToTextConverter.toText(value));
    }

}
