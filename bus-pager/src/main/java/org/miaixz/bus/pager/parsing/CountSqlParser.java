/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.pager.parsing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * sql解析类,提供更智能的count查询sql
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CountSqlParser {

    /**
     * 聚合函数，以下列函数开头的都认为是聚合函数
     */
    Set<String> AGGREGATE_FUNCTIONS = new HashSet<>(
            Arrays.asList(("APPROX_COUNT_DISTINCT," + "ARRAY_AGG," + "AVG," + "BIT_," +
            // "BIT_AND," +
            // "BIT_OR," +
            // "BIT_XOR," +
                    "BOOL_," +
                    // "BOOL_AND," +
                    // "BOOL_OR," +
                    "CHECKSUM_AGG," + "COLLECT," + "CORR," +
                    // "CORR_," +
                    // "CORRELATION," +
                    "COUNT," +
                    // "COUNT_BIG," +
                    "COVAR," +
                    // "COVAR_POP," +
                    // "COVAR_SAMP," +
                    // "COVARIANCE," +
                    // "COVARIANCE_SAMP," +
                    "CUME_DIST," + "DENSE_RANK," + "EVERY," + "FIRST," + "GROUP," +
                    // "GROUP_CONCAT," +
                    // "GROUP_ID," +
                    // "GROUPING," +
                    // "GROUPING," +
                    // "GROUPING_ID," +
                    "JSON_," +
                    // "JSON_AGG," +
                    // "JSON_ARRAYAGG," +
                    // "JSON_OBJECT_AGG," +
                    // "JSON_OBJECTAGG," +
                    // "JSONB_AGG," +
                    // "JSONB_OBJECT_AGG," +
                    "LAST," + "LISTAGG," + "MAX," + "MEDIAN," + "MIN," + "PERCENT_," +
                    // "PERCENT_RANK," +
                    // "PERCENTILE_CONT," +
                    // "PERCENTILE_DISC," +
                    "RANK," + "REGR_," + "SELECTIVITY," + "STATS_," +
                    // "STATS_BINOMIAL_TEST," +
                    // "STATS_CROSSTAB," +
                    // "STATS_F_TEST," +
                    // "STATS_KS_TEST," +
                    // "STATS_MODE," +
                    // "STATS_MW_TEST," +
                    // "STATS_ONE_WAY_ANOVA," +
                    // "STATS_T_TEST_*," +
                    // "STATS_WSR_TEST," +
                    "STD," +
                    // "STDDEV," +
                    // "STDDEV_POP," +
                    // "STDDEV_SAMP," +
                    // "STDDEV_SAMP," +
                    // "STDEV," +
                    // "STDEVP," +
                    "STRING_AGG," + "SUM," + "SYS_OP_ZONE_ID," + "SYS_XMLAGG," + "VAR," +
                    // "VAR_POP," +
                    // "VAR_SAMP," +
                    // "VARIANCE," +
                    // "VARIANCE_SAMP," +
                    // "VARP," +
                    "XMLAGG").split(Symbol.COMMA)));

    /**
     * 添加到聚合函数，可以是逗号隔开的多个函数前缀
     *
     * @param functions 函数
     */
    static void addAggregateFunctions(String functions) {
        if (StringKit.isNotEmpty(functions)) {
            String[] funs = functions.split(Symbol.COMMA);
            for (int i = 0; i < funs.length; i++) {
                AGGREGATE_FUNCTIONS.add(funs[i].toUpperCase());
            }
        }
    }

    /**
     * 获取智能的countSql
     *
     * @param sql sql
     * @return the string
     */
    default String getSmartCountSql(String sql) {
        return getSmartCountSql(sql, Symbol.ZERO);
    }

    /**
     * 获取智能的countSql
     *
     * @param sql         sql
     * @param countColumn 列名,默认 0
     * @return the string
     */
    String getSmartCountSql(String sql, String countColumn);

}
