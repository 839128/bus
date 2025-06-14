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
package org.miaixz.bus.core.center.date.culture.solar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.en.Quarter;

/**
 * 公历季度
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarQuarter extends Loops {

    /**
     * 年
     */
    protected SolarYear year;

    /**
     * 索引，0-3
     */
    protected int index;

    /**
     * 初始化
     *
     * @param year  年
     * @param index 索引，0-3
     */
    public SolarQuarter(int year, int index) {
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException(String.format("illegal solar season index: %d", index));
        }
        this.year = SolarYear.fromYear(year);
        this.index = index;
    }

    public static SolarQuarter fromIndex(int year, int index) {
        return new SolarQuarter(year, index);
    }

    /**
     * 公历年
     *
     * @return 公历年
     */
    public SolarYear getSolarYear() {
        return year;
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return year.getYear();
    }

    /**
     * 索引
     *
     * @return 索引，0-3
     */
    public int getIndex() {
        return index;
    }

    public String getName() {
        return Quarter.getName(index);
    }

    @Override
    public String toString() {
        return year + getName();
    }

    public SolarQuarter next(int n) {
        int i = index + n;
        return fromIndex((getYear() * 4 + i) / 4, indexOf(i, 4));
    }

    /**
     * 月份列表
     *
     * @return 月份列表，1季度有3个月。
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(3);
        int y = getYear();
        for (int i = 1; i < 4; i++) {
            l.add(SolarMonth.fromYm(y, index * 3 + i));
        }
        return l;
    }

}
