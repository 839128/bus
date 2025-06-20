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

import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.pager.Builder;

import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

/**
 * 处理 Order by
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultOrderBySqlParser implements OrderBySqlParser {

    /**
     * extra order by and set default orderby to null
     *
     * @param select 获取select
     * @return the list
     */
    public static List<OrderByElement> extraOrderBy(Select select) {
        if (select != null) {
            if (select instanceof PlainSelect || select instanceof SetOperationList) {
                List<OrderByElement> orderByElements = select.getOrderByElements();
                select.setOrderByElements(null);
                return orderByElements;
            } else if (select instanceof ParenthesedSelect) {
                extraOrderBy(((ParenthesedSelect) select).getSelect());
            }
        }
        return null;
    }

    /**
     * convert to order by sql
     *
     * @param sql     sql
     * @param orderBy 排序
     * @return the string
     */
    @Override
    public String converToOrderBySql(String sql, String orderBy) {
        try {
            // 解析SQL
            Statement stmt = Builder.parse(sql);
            Select select = (Select) stmt;
            // 处理body-去最外层order by
            List<OrderByElement> orderByElements = extraOrderBy(select);
            String defaultOrderBy = PlainSelect.orderByToString(orderByElements);
            if (defaultOrderBy.indexOf(Symbol.C_QUESTION_MARK) != -1) {
                throw new PageException("The order by in the original SQL[" + sql
                        + "] contains parameters, so it cannot be modified using the OrderBy handler!");
            }
            // 新的sql
            sql = select.toString();
        } catch (Throwable e) {
            Logger.warn("Failed to handle sorting: " + e + ", downgraded to a direct splice of the order by parameter");
        }
        return sql + " order by " + orderBy;
    }

}
