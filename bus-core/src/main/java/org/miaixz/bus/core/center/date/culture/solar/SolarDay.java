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

import org.miaixz.bus.core.center.date.Holiday;
import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.HiddenStems;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;
import org.miaixz.bus.core.center.date.culture.cn.Week;
import org.miaixz.bus.core.center.date.culture.cn.climate.Climate;
import org.miaixz.bus.core.center.date.culture.cn.climate.ClimateDay;
import org.miaixz.bus.core.center.date.culture.cn.dog.Dog;
import org.miaixz.bus.core.center.date.culture.cn.dog.DogDay;
import org.miaixz.bus.core.center.date.culture.cn.nine.Nine;
import org.miaixz.bus.core.center.date.culture.cn.nine.NineDay;
import org.miaixz.bus.core.center.date.culture.cn.plumrain.PlumRain;
import org.miaixz.bus.core.center.date.culture.cn.plumrain.PlumRainDay;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HiddenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HiddenStemDay;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycleDay;
import org.miaixz.bus.core.center.date.culture.en.Constellation;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.rabjung.RabjungDay;

/**
 * 公历日
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarDay extends Loops {

    public static final String[] NAMES = { "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日", "11日", "12日",
            "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日", "21日", "22日", "23日", "24日", "25日", "26日", "27日",
            "28日", "29日", "30日", "31日" };

    /**
     * 公历月
     */
    protected SolarMonth month;

    /**
     * 日
     */
    protected int day;

    /**
     * 初始化
     *
     * @param year  年
     * @param month 月
     * @param day   日
     */
    public SolarDay(int year, int month, int day) {
        if (day < 1) {
            throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
        }
        SolarMonth m = SolarMonth.fromYm(year, month);
        if (1582 == year && 10 == month) {
            if ((day > 4 && day < 15) || day > 31) {
                throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
            }
        } else if (day > m.getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
        }
        this.month = m;
        this.day = day;
    }

    public static SolarDay fromYmd(int year, int month, int day) {
        return new SolarDay(year, month, day);
    }

    /**
     * 公历月
     *
     * @return 公历月
     */
    public SolarMonth getSolarMonth() {
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
        return month.getMonth();
    }

    /**
     * 日
     *
     * @return 日
     */
    public int getDay() {
        return day;
    }

    /**
     * 星期
     *
     * @return 星期
     */
    public Week getWeek() {
        return getJulianDay().getWeek();
    }

    /**
     * 星座
     *
     * @return 星座
     */
    public Constellation getConstellation() {
        int y = getMonth() * 100 + day;
        return Constellation.get(y > 1221 || y < 120 ? 9
                : y < 219 ? 10
                        : y < 321 ? 11
                                : y < 420 ? 0
                                        : y < 521 ? 1
                                                : y < 622 ? 2
                                                        : y < 723 ? 3
                                                                : y < 823 ? 4
                                                                        : y < 923 ? 5
                                                                                : y < 1024 ? 6 : y < 1123 ? 7 : 8);
    }

    public String getName() {
        return NAMES[day - 1];
    }

    @Override
    public String toString() {
        return month + getName();
    }

    public SolarDay next(int n) {
        return getJulianDay().next(n).getSolarDay();
    }

    /**
     * 是否在指定公历日之前
     *
     * @param target 公历日
     * @return true/false
     */
    public boolean isBefore(SolarDay target) {
        int aYear = getYear();
        int bYear = target.getYear();
        if (aYear != bYear) {
            return aYear < bYear;
        }
        int aMonth = getMonth();
        int bMonth = target.getMonth();
        return aMonth != bMonth ? aMonth < bMonth : day < target.getDay();
    }

    /**
     * 是否在指定公历日之后
     *
     * @param target 公历日
     * @return true/false
     */
    public boolean isAfter(SolarDay target) {
        int aYear = getYear();
        int bYear = target.getYear();
        if (aYear != bYear) {
            return aYear > bYear;
        }
        int aMonth = getMonth();
        int bMonth = target.getMonth();
        return aMonth != bMonth ? aMonth > bMonth : day > target.getDay();
    }

    /**
     * 节气
     *
     * @return 节气
     */
    public SolarTerms getTerm() {
        return getTermDay().getSolarTerm();
    }

    /**
     * 节气第几天
     *
     * @return 节气第几天
     */
    public SolarTermDay getTermDay() {
        int y = getYear();
        int i = getMonth() * 2;
        if (i == 24) {
            y += 1;
            i = 0;
        }
        SolarTerms term = SolarTerms.fromIndex(y, i);
        SolarDay day = term.getJulianDay().getSolarDay();
        while (isBefore(day)) {
            term = term.next(-1);
            day = term.getJulianDay().getSolarDay();
        }
        return new SolarTermDay(term, subtract(day));
    }

    /**
     * 公历周
     *
     * @param start 起始星期，1234560分别代表星期一至星期天
     * @return 公历周
     */
    public SolarWeek getSolarWeek(int start) {
        int y = getYear();
        int m = getMonth();
        return SolarWeek.fromYm(y, m,
                (int) Math.ceil((day + fromYmd(y, m, 1).getWeek().next(-start).getIndex()) / 7D) - 1, start);
    }

    /**
     * 候
     *
     * @return 候
     */
    public Climate getPhenology() {
        return getClimateDay().getClimate();
    }

    /**
     * 七十二候
     *
     * @return 七十二候
     */
    public ClimateDay getClimateDay() {
        SolarTermDay d = getTermDay();
        int dayIndex = d.getDayIndex();
        int index = dayIndex / 5;
        if (index > 2) {
            index = 2;
        }
        SolarTerms term = d.getSolarTerm();
        return new ClimateDay(Climate.fromIndex(term.getYear(), term.getIndex() * 3 + index), dayIndex - index * 5);
    }

    /**
     * 三伏天
     *
     * @return 三伏天
     */
    public DogDay getDogDay() {
        // 夏至
        SolarTerms xiaZhi = SolarTerms.fromIndex(getYear(), 12);
        SolarDay start = xiaZhi.getJulianDay().getSolarDay();
        // 第3个庚日，即初伏第1天
        start = start.next(start.getLunarDay().getSixtyCycle().getHeavenStem().stepsTo(6) + 20);
        int days = subtract(start);
        // 初伏以前
        if (days < 0) {
            return null;
        }
        if (days < 10) {
            return new DogDay(Dog.fromIndex(0), days);
        }
        // 第4个庚日，中伏第1天
        start = start.next(10);
        days = subtract(start);
        if (days < 10) {
            return new DogDay(Dog.fromIndex(1), days);
        }
        // 第5个庚日，中伏第11天或末伏第1天
        start = start.next(10);
        days = subtract(start);
        // 立秋
        if (xiaZhi.next(3).getJulianDay().getSolarDay().isAfter(start)) {
            if (days < 10) {
                return new DogDay(Dog.fromIndex(1), days + 10);
            }
            start = start.next(10);
            days = subtract(start);
        }
        return days >= 10 ? null : new DogDay(Dog.fromIndex(2), days);
    }

    /**
     * 数九天
     *
     * @return 数九天
     */
    public NineDay getNineDay() {
        int year = getYear();
        SolarDay start = SolarTerms.fromIndex(year + 1, 0).getJulianDay().getSolarDay();
        if (isBefore(start)) {
            start = SolarTerms.fromIndex(year, 0).getJulianDay().getSolarDay();
        }
        SolarDay end = start.next(81);
        if (isBefore(start) || !isBefore(end)) {
            return null;
        }
        int days = subtract(start);
        return new NineDay(Nine.fromIndex(days / 9), days % 9);
    }

    /**
     * 梅雨天（芒种后的第1个丙日入梅，小暑后的第1个未日出梅）
     *
     * @return 梅雨天
     */
    public PlumRainDay getPlumRainDay() {
        // 芒种
        SolarTerms grainInEar = SolarTerms.fromIndex(getYear(), 11);
        SolarDay start = grainInEar.getJulianDay().getSolarDay();
        int add = 2 - start.getLunarDay().getSixtyCycle().getHeavenStem().getIndex();
        if (add < 0) {
            add += 10;
        }
        // 芒种后的第1个丙日
        start = start.next(add);

        // 小暑
        SolarTerms slightHeat = grainInEar.next(2);
        SolarDay end = slightHeat.getJulianDay().getSolarDay();
        add = 7 - end.getLunarDay().getSixtyCycle().getEarthBranch().getIndex();
        if (add < 0) {
            add += 12;
        }
        // 小暑后的第1个未日
        end = end.next(add);

        if (isBefore(start) || isAfter(end)) {
            return null;
        }
        return equals(end) ? new PlumRainDay(PlumRain.fromIndex(1), 0)
                : new PlumRainDay(PlumRain.fromIndex(0), subtract(start));
    }

    /**
     * 人元司令分野
     *
     * @return 人元司令分野
     */
    public HiddenStemDay getHideHeavenStemDay() {
        int[] dayCounts = { 3, 5, 7, 9, 10, 30 };
        SolarTerms term = getTerm();
        if (term.isQi()) {
            term = term.next(-1);
        }
        int dayIndex = subtract(term.getJulianDay().getSolarDay());
        int startIndex = (term.getIndex() - 1) * 3;
        String data = "93705542220504xx1513904541632524533533105544806564xx7573304542018584xx95".substring(startIndex,
                startIndex + 6);
        int days = 0;
        int heavenStemIndex = 0;
        int typeIndex = 0;
        while (typeIndex < 3) {
            int i = typeIndex * 2;
            String d = data.substring(i, i + 1);
            int count = 0;
            if (!d.equals("x")) {
                heavenStemIndex = Integer.parseInt(d);
                count = dayCounts[Integer.parseInt(data.substring(i + 1, i + 2))];
                days += count;
            }
            if (dayIndex <= days) {
                dayIndex -= days - count;
                break;
            }
            typeIndex++;
        }
        return new HiddenStemDay(new HiddenStem(heavenStemIndex, HiddenStems.fromCode(typeIndex)), dayIndex);
    }

    /**
     * 位于当年的索引
     *
     * @return 索引
     */
    public int getIndexInYear() {
        return subtract(fromYmd(getYear(), 1, 1));
    }

    /**
     * 公历日期相减，获得相差天数
     *
     * @param target 公历
     * @return 天数
     */
    public int subtract(SolarDay target) {
        return (int) (getJulianDay().subtract(target.getJulianDay()));
    }

    /**
     * 儒略日
     *
     * @return 儒略日
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromYmdHms(getYear(), getMonth(), day, 0, 0, 0);
    }

    /**
     * 农历日
     *
     * @return 农历日
     */
    public LunarDay getLunarDay() {
        LunarMonth m = LunarMonth.fromYm(getYear(), getMonth());
        int days = subtract(m.getFirstJulianDay().getSolarDay());
        while (days < 0) {
            m = m.next(-1);
            days += m.getDayCount();
        }
        return LunarDay.fromYmd(m.getYear(), m.getMonthWithLeap(), days + 1);
    }

    /**
     * 干支日
     *
     * @return 干支日
     */
    public SixtyCycleDay getSixtyCycleDay() {
        return SixtyCycleDay.fromSolarDay(this);
    }

    /**
     * 法定假日，如果当天不是法定假日，返回null
     *
     * @return 法定假日
     */
    public Holiday getHoliday() {
        return Holiday.fromYmd(getYear(), getMonth(), day);
    }

    /**
     * 公历现代节日，如果当天不是公历现代节日，返回null
     *
     * @return 公历现代节日
     */
    public SolarFestival getFestival() {
        return SolarFestival.fromYmd(getYear(), getMonth(), day);
    }

    /**
     * 藏历日
     *
     * @return 藏历日
     */
    public RabjungDay getRabByungDay() {
        return RabjungDay.fromSolarDay(this);
    }

}
