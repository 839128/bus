/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.driver;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.software.OSSession;

import com.sun.jna.Platform;

/**
 * Utility to query logged in users.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Who {

    // sample format:
    // oshi pts/0 2020-05-14 21:23 (192.168.1.23)
    private static final Pattern WHO_FORMAT_LINUX = Pattern
            .compile("(\\S+)\\s+(\\S+)\\s+(\\d{4}-\\d{2}-\\d{2})\\s+(\\d{2}:\\d{2})\\s*(?:\\((.+)\\))?");
    private static final DateTimeFormatter WHO_DATE_FORMAT_LINUX = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm",
            Locale.ROOT);
    // oshi ttys000 May 4 23:50 (192.168.1.23)
    // middle 12 characters from Thu Nov 24 18:22:48 1986
    private static final Pattern WHO_FORMAT_UNIX = Pattern
            .compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(\\d{2}:\\d{2})\\s*(?:\\((.+)\\))?");
    private static final DateTimeFormatter WHO_DATE_FORMAT_UNIX = new DateTimeFormatterBuilder()
            .appendPattern("MMM d HH:mm").parseDefaulting(ChronoField.YEAR, Year.now(ZoneId.systemDefault()).getValue())
            .toFormatter(Locale.US);

    private Who() {
    }

    /**
     * Query {@code who} to get logged in users
     *
     * @return A list of logged in user sessions
     */
    public static synchronized List<OSSession> queryWho() {
        List<OSSession> whoList = new ArrayList<>();
        List<String> who = Executor.runNative("who");
        for (String s : who) {
            boolean matched = false;
            if (Platform.isLinux()) {
                matched = matchLinux(whoList, s);
            }
            if (!matched) {
                matchUnix(whoList, s);
            }
        }
        return whoList;
    }

    /**
     * Attempt to match Linux WHO format and add to the list
     *
     * @param whoList the list to add to
     * @param s       the string to match
     * @return true if successful, false otherwise
     */
    private static boolean matchLinux(List<OSSession> whoList, String s) {
        Matcher m = WHO_FORMAT_LINUX.matcher(s);
        if (m.matches()) {
            try {
                whoList.add(new OSSession(m.group(1), m.group(2),
                        LocalDateTime.parse(m.group(3) + Symbol.SPACE + m.group(4), WHO_DATE_FORMAT_LINUX)
                                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        m.group(5) == null ? Normal.UNKNOWN : m.group(5)));
                return true;
            } catch (DateTimeParseException | NullPointerException e) {
                // shouldn't happen if regex matches and OS is producing sensible dates
            }
        }
        return false;
    }

    /**
     * Attempt to match Unix WHO format and add to the list
     *
     * @param whoList the list to add to
     * @param s       the string to match
     * @return true if successful, false otherwise
     */
    private static boolean matchUnix(List<OSSession> whoList, String s) {
        Matcher m = WHO_FORMAT_UNIX.matcher(s);
        if (m.matches()) {
            try {
                // Missing year, parse date time with current year
                LocalDateTime login = LocalDateTime.parse(
                        m.group(3) + Symbol.SPACE + m.group(4) + Symbol.SPACE + m.group(5), WHO_DATE_FORMAT_UNIX);
                // If this date is in the future, subtract a year
                if (login.isAfter(LocalDateTime.now(ZoneId.systemDefault()))) {
                    login = login.minus(1, ChronoUnit.YEARS);
                }
                long millis = login.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                whoList.add(
                        new OSSession(m.group(1), m.group(2), millis, m.group(6) == null ? Normal.EMPTY : m.group(6)));
                return true;
            } catch (DateTimeParseException | NullPointerException e) {
                // shouldn't happen if regex matches and OS is producing sensible dates
            }
        }
        return false;
    }

}
