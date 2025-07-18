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
package org.miaixz.bus.core.center.date.culture.lunar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.*;
import org.miaixz.bus.core.center.date.culture.cn.fetus.FetusDay;
import org.miaixz.bus.core.center.date.culture.cn.ren.MinorRen;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycleDay;
import org.miaixz.bus.core.center.date.culture.cn.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.cn.star.six.SixStar;
import org.miaixz.bus.core.center.date.culture.cn.star.twelve.TwelveStar;
import org.miaixz.bus.core.center.date.culture.cn.star.twentyeight.TwentyEightStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

/**
 * 农历日
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarDay extends Loops {

    public static final String[] NAMES = { "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三",
            "十四", "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十" };

    /**
     * 农历月
     */
    protected LunarMonth month;

    /**
     * 日
     */
    protected int day;

    /**
     * 公历日（第一次使用时才会初始化）
     */
    protected SolarDay solarDay;

    /**
     * 干支日（第一次使用时才会初始化）
     */
    protected SixtyCycleDay sixtyCycleDay;

    /**
     * 初始化
     *
     * @param year  农历年
     * @param month 农历月，闰月为负
     * @param day   农历日
     */
    public LunarDay(int year, int month, int day) {
        LunarMonth m = LunarMonth.fromYm(year, month);
        if (day < 1 || day > m.getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal day %d in %s", day, m));
        }
        this.month = m;
        this.day = day;
    }

    /**
     * 从农历年月日初始化
     *
     * @param year  农历年
     * @param month 农历月，闰月为负
     * @param day   农历日
     */
    public static LunarDay fromYmd(int year, int month, int day) {
        return new LunarDay(year, month, day);
    }

    /**
     * 农历月
     *
     * @return 农历月
     */
    public LunarMonth getLunarMonth() {
        return month;
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return month.getYear();
    }

    /**
     * 月
     *
     * @return 月
     */
    public int getMonth() {
        return month.getMonthWithLeap();
    }

    /**
     * 日
     *
     * @return 日
     */
    public int getDay() {
        return day;
    }

    public String getName() {
        return NAMES[day - 1];
    }

    @Override
    public String toString() {
        return month + getName();
    }

    public LunarDay next(int n) {
        return getSolarDay().next(n).getLunarDay();
    }

    /**
     * 是否在指定农历日之前
     *
     * @param target 农历日
     * @return true/false
     */
    public boolean isBefore(LunarDay target) {
        int aYear = getYear();
        int bYear = target.getYear();
        if (aYear != bYear) {
            return aYear < bYear;
        }
        int aMonth = getMonth();
        int bMonth = target.getMonth();
        if (aMonth != bMonth) {
            return Math.abs(aMonth) < Math.abs(bMonth);
        }
        return day < target.getDay();
    }

    /**
     * 是否在指定农历日之后
     *
     * @param target 农历日
     * @return true/false
     */
    public boolean isAfter(LunarDay target) {
        int aYear = getYear();
        int bYear = target.getYear();
        if (aYear != bYear) {
            return aYear > bYear;
        }
        int aMonth = getMonth();
        int bMonth = target.getMonth();
        if (aMonth != bMonth) {
            return Math.abs(aMonth) >= Math.abs(bMonth);
        }
        return day > target.getDay();
    }

    /**
     * 星期
     *
     * @return 星期
     */
    public Week getWeek() {
        return getSolarDay().getWeek();
    }

    /**
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        int offset = (int) month.getFirstJulianDay().next(day - 12).getDay();
        return SixtyCycle.fromName(HeavenStem.fromIndex(offset).getName() + EarthBranch.fromIndex(offset).getName());
    }

    /**
     * 建除十二值神
     *
     * @return 建除十二值神
     * @see SixtyCycleDay
     */
    public Duty getDuty() {
        return getSixtyCycleDay().getDuty();
    }

    /**
     * 黄道黑道十二神
     *
     * @return 黄道黑道十二神
     * @see SixtyCycleDay
     */
    public TwelveStar getTwelveStar() {
        return getSixtyCycleDay().getTwelveStar();
    }

    /**
     * 九星
     *
     * @return 九星
     */
    public NineStar getNineStar() {
        SolarDay d = getSolarDay();
        SolarTerms dongZhi = SolarTerms.fromIndex(d.getYear(), 0);
        SolarTerms xiaZhi = dongZhi.next(12);
        SolarTerms dongZhi2 = dongZhi.next(24);
        SolarDay dongZhiSolar = dongZhi.getJulianDay().getSolarDay();
        SolarDay xiaZhiSolar = xiaZhi.getJulianDay().getSolarDay();
        SolarDay dongZhiSolar2 = dongZhi2.getJulianDay().getSolarDay();
        int dongZhiIndex = dongZhiSolar.getLunarDay().getSixtyCycle().getIndex();
        int xiaZhiIndex = xiaZhiSolar.getLunarDay().getSixtyCycle().getIndex();
        int dongZhiIndex2 = dongZhiSolar2.getLunarDay().getSixtyCycle().getIndex();
        SolarDay solarShunBai = dongZhiSolar.next(dongZhiIndex > 29 ? 60 - dongZhiIndex : -dongZhiIndex);
        SolarDay solarShunBai2 = dongZhiSolar2.next(dongZhiIndex2 > 29 ? 60 - dongZhiIndex2 : -dongZhiIndex2);
        SolarDay solarNiZi = xiaZhiSolar.next(xiaZhiIndex > 29 ? 60 - xiaZhiIndex : -xiaZhiIndex);
        int offset = 0;
        if (!d.isBefore(solarShunBai) && d.isBefore(solarNiZi)) {
            offset = d.subtract(solarShunBai);
        } else if (!d.isBefore(solarNiZi) && d.isBefore(solarShunBai2)) {
            offset = 8 - d.subtract(solarNiZi);
        } else if (!d.isBefore(solarShunBai2)) {
            offset = d.subtract(solarShunBai2);
        } else if (d.isBefore(solarShunBai)) {
            offset = 8 + solarShunBai.subtract(d);
        }
        return NineStar.fromIndex(offset);
    }

    /**
     * 太岁方位
     *
     * @return 方位
     */
    public Direction getJupiterDirection() {
        int index = getSixtyCycle().getIndex();
        return index % 12 < 6 ? Element.fromIndex(index / 12).getDirection()
                : month.getLunarYear().getJupiterDirection();
    }

    /**
     * 逐日胎神
     *
     * @return 逐日胎神
     */
    public FetusDay getFetusDay() {
        return FetusDay.fromLunarDay(this);
    }

    /**
     * 月相
     *
     * @return 月相
     */
    public Phase getPhase() {
        return Phase.fromIndex(day - 1);
    }

    /**
     * 六曜
     *
     * @return 六曜
     */
    public SixStar getSixStar() {
        return SixStar.fromIndex((month.getMonth() + day - 2) % 6);
    }

    /**
     * 公历日
     *
     * @return 公历日
     */
    public SolarDay getSolarDay() {
        if (null == solarDay) {
            solarDay = month.getFirstJulianDay().next(day - 1).getSolarDay();
        }
        return solarDay;
    }

    /**
     * 干支日
     *
     * @return 干支日
     */
    public SixtyCycleDay getSixtyCycleDay() {
        if (null == sixtyCycleDay) {
            sixtyCycleDay = getSolarDay().getSixtyCycleDay();
        }
        return sixtyCycleDay;
    }

    /**
     * 二十八宿
     *
     * @return 二十八宿
     */
    public TwentyEightStar getTwentyEightStar() {
        return TwentyEightStar.fromIndex(new int[] { 10, 18, 26, 6, 14, 22, 2 }[getSolarDay().getWeek().getIndex()])
                .next(-7 * getSixtyCycle().getEarthBranch().getIndex());
    }

    /**
     * 农历传统节日，如果当天不是农历传统节日，返回null
     *
     * @return 农历传统节日
     */
    public LunarFestival getFestival() {
        return LunarFestival.fromYmd(getYear(), getMonth(), day);
    }

    /**
     * 当天的农历时辰列表
     *
     * @return 农历时辰列表
     */
    public List<LunarHour> getHours() {
        List<LunarHour> l = new ArrayList<>();
        int y = getYear();
        int m = getMonth();
        l.add(LunarHour.fromYmdHms(y, m, day, 0, 0, 0));
        for (int i = 0; i < 24; i += 2) {
            l.add(LunarHour.fromYmdHms(y, m, day, i + 1, 0, 0));
        }
        return l;
    }

    /**
     * 神煞列表(吉神宜趋，凶神宜忌)
     *
     * @return 神煞列表
     */
    public List<God> getGods() {
        return getSixtyCycleDay().getGods();
    }

    /**
     * 宜
     *
     * @return 宜忌列表
     */
    public List<Taboo> getRecommends() {
        return getSixtyCycleDay().getRecommends();
    }

    /**
     * 忌
     *
     * @return 宜忌列表
     */
    public List<Taboo> getAvoids() {
        return getSixtyCycleDay().getAvoids();
    }

    /**
     * 小六壬
     *
     * @return 小六壬
     */
    public MinorRen getMinorRen() {
        return getLunarMonth().getMinorRen().next(day - 1);
    }

}
