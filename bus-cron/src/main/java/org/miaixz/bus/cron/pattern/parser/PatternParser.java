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
package org.miaixz.bus.cron.pattern.parser;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.cron.pattern.Part;
import org.miaixz.bus.cron.pattern.matcher.AlwaysTrueMatcher;
import org.miaixz.bus.cron.pattern.matcher.PartMatcher;
import org.miaixz.bus.cron.pattern.matcher.PatternMatcher;

/**
 * 定时任务表达式解析器，用于将表达式字符串解析为{@link PatternMatcher}的列表
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternParser {

    private static final PartParser SECOND_VALUE_PARSER = PartParser.of(Part.SECOND);
    private static final PartParser MINUTE_VALUE_PARSER = PartParser.of(Part.MINUTE);
    private static final PartParser HOUR_VALUE_PARSER = PartParser.of(Part.HOUR);
    private static final PartParser DAY_OF_MONTH_VALUE_PARSER = PartParser.of(Part.DAY_OF_MONTH);
    private static final PartParser MONTH_VALUE_PARSER = PartParser.of(Part.MONTH);
    private static final PartParser DAY_OF_WEEK_VALUE_PARSER = PartParser.of(Part.DAY_OF_WEEK);
    private static final PartParser YEAR_VALUE_PARSER = PartParser.of(Part.YEAR);

    /**
     * 解析表达式到匹配列表中
     *
     * @param cronPattern 复合表达式
     * @return {@link List}
     */
    public static List<PatternMatcher> parse(final String cronPattern) {
        return parseGroupPattern(cronPattern);
    }

    /**
     * 解析复合任务表达式，格式为：
     * 
     * <pre>
     *     cronA | cronB | ...
     * </pre>
     *
     * @param groupPattern 复合表达式
     * @return {@link List}
     */
    private static List<PatternMatcher> parseGroupPattern(final String groupPattern) {
        Assert.notBlank(groupPattern, "Cron expression must not be empty!");
        final List<String> patternList = CharsBacker.splitTrim(groupPattern, Symbol.OR);
        final List<PatternMatcher> patternMatchers = new ArrayList<>(patternList.size());
        for (final String pattern : patternList) {
            patternMatchers.add(parseSingle(pattern));
        }
        return patternMatchers;
    }

    /**
     * 解析单一定时任务表达式
     *
     * @param pattern 表达式
     * @return {@link PatternMatcher}
     */
    private static PatternMatcher parseSingle(final String pattern) {
        final String[] parts = pattern.split("\\s+");
        Assert.checkBetween(parts.length, 5, 7,
                () -> new CrontabException("Pattern [{}] is invalid, it must be 5-7 parts!", pattern));

        // 偏移量用于兼容Quartz表达式，当表达式有6或7项时，第一项为秒
        int offset = 0;
        if (parts.length == 6 || parts.length == 7) {
            offset = 1;
        }

        // 秒，如果不支持秒的表达式，则第一位赋值0，表示整分匹配
        final String secondPart = (1 == offset) ? parts[0] : "0";

        // 年
        final PartMatcher yearMatcher;
        if (parts.length == 7) {// 支持年的表达式
            yearMatcher = YEAR_VALUE_PARSER.parse(parts[6]);
        } else {// 不支持年的表达式，全部匹配
            yearMatcher = AlwaysTrueMatcher.INSTANCE;
        }

        return new PatternMatcher(
                // 秒
                SECOND_VALUE_PARSER.parse(secondPart),
                // 分
                MINUTE_VALUE_PARSER.parse(parts[offset]),
                // 时
                HOUR_VALUE_PARSER.parse(parts[1 + offset]),
                // 天
                DAY_OF_MONTH_VALUE_PARSER.parse(parts[2 + offset]),
                // 月
                MONTH_VALUE_PARSER.parse(parts[3 + offset]),
                // 周
                DAY_OF_WEEK_VALUE_PARSER.parse(parts[4 + offset]),
                // 年
                yearMatcher);
    }

}
