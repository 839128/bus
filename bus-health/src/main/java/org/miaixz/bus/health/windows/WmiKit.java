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
package org.miaixz.bus.health.windows;

import java.time.OffsetDateTime;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;

import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Helper class for WMI
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class WmiKit {

    /**
     * The namespace where Open Hardware Monitor publishes to WMI,
     * <code>OHM_NAMESPACE="ROOT\\OpenHardwareMonitor"</code>. This namespace is not built-in to WMI, so if OHM is not
     * running would result in unnecessary log messages.
     */
    public static final String OHM_NAMESPACE = "ROOT\\OpenHardwareMonitor";

    private static final String CLASS_CAST_MSG = "%s is not a %s type. CIM Type is %d and VT type is %d";

    /**
     * Translate a WmiQuery to the actual query string
     *
     * @param <T>   WMI queries use an Enum to identify the fields to query, and use the enum values as keys to retrieve
     *              the results.
     * @param query The WmiQuery object
     * @return The string that is queried in WMI
     */
    public static <T extends Enum<T>> String queryToString(WmiQuery<T> query) {
        T[] props = query.getPropertyEnum().getEnumConstants();
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(props[0].name());
        for (int i = 1; i < props.length; i++) {
            sb.append(Symbol.C_COMMA).append(props[i].name());
        }
        sb.append(" FROM ").append(query.getWmiClassName());
        return sb.toString();
    }

    /**
     * Gets a String value from a WmiResult
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, an empty-string otherwise
     */
    public static <T extends Enum<T>> String getString(WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == Wbemcli.CIM_STRING) {
            return getStr(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "String",
                result.getCIMType(property), result.getVtType(property)));
    }

    /**
     * Gets a Date value from a WmiResult as a String in ISO 8601 format
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, an empty-string otherwise
     */
    public static <T extends Enum<T>> String getDateString(WmiResult<T> result, T property, int index) {
        OffsetDateTime dateTime = getDateTime(result, property, index);
        // Null result returns the Epoch
        if (dateTime.equals(Builder.UNIX_EPOCH)) {
            return Normal.EMPTY;
        }
        return dateTime.toLocalDate().toString();
    }

    /**
     * Gets a DateTime value from a WmiResult as an OffsetDateTime
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, otherwise the constant {@link Builder#UNIX_EPOCH}
     */
    public static <T extends Enum<T>> OffsetDateTime getDateTime(WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == Wbemcli.CIM_DATETIME) {
            return Parsing.parseCimDateTimeToOffset(getStr(result, property, index));
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "DateTime",
                result.getCIMType(property), result.getVtType(property)));
    }

    /**
     * Gets a Reference value from a WmiResult as a String
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, an empty-string otherwise
     */
    public static <T extends Enum<T>> String getRefString(WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == Wbemcli.CIM_REFERENCE) {
            return getStr(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "Reference",
                result.getCIMType(property), result.getVtType(property)));
    }

    private static <T extends Enum<T>> String getStr(WmiResult<T> result, T property, int index) {
        Object o = result.getValue(property, index);
        if (o == null) {
            return Normal.EMPTY;
        } else if (result.getVtType(property) == Variant.VT_BSTR) {
            return (String) o;
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "String-mapped",
                result.getCIMType(property), result.getVtType(property)));
    }

    /**
     * Gets a Uint64 value from a WmiResult (parsing the String). Note that while the CIM type is unsigned, the return
     * type is signed and the parsing will exclude any return values above Long.MAX_VALUE.
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null and parseable as a long, 0 otherwise
     */
    public static <T extends Enum<T>> long getUint64(WmiResult<T> result, T property, int index) {
        Object o = result.getValue(property, index);
        if (o == null) {
            return 0L;
        } else if (result.getCIMType(property) == Wbemcli.CIM_UINT64 && result.getVtType(property) == Variant.VT_BSTR) {
            return Parsing.parseLongOrDefault((String) o, 0L);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "UINT64",
                result.getCIMType(property), result.getVtType(property)));
    }

    /**
     * Gets an UINT32 value from a WmiResult. Note that while a UINT32 CIM type is unsigned, the return type is signed
     * and requires further processing by the user if unsigned values are desired.
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, 0 otherwise
     */
    public static <T extends Enum<T>> int getUint32(WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == Wbemcli.CIM_UINT32) {
            return getInt(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "UINT32",
                result.getCIMType(property), result.getVtType(property)));
    }

    /**
     * Gets an UINT32 value from a WmiResult as a long, preserving the unsignedness.
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, 0 otherwise
     */
    public static <T extends Enum<T>> long getUint32asLong(WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == Wbemcli.CIM_UINT32) {
            return getInt(result, property, index) & 0xFFFFFFFFL;
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "UINT32",
                result.getCIMType(property), result.getVtType(property)));
    }

    /**
     * Gets a Sint32 value from a WmiResult. Note that while the CIM type is unsigned, the return type is signed and
     * requires further processing by the user if unsigned values are desired.
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, 0 otherwise
     */
    public static <T extends Enum<T>> int getSint32(WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == Wbemcli.CIM_SINT32) {
            return getInt(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "SINT32",
                result.getCIMType(property), result.getVtType(property)));
    }

    /**
     * Gets a Uint16 value from a WmiResult. Note that while the CIM type is unsigned, the return type is signed and
     * requires further processing by the user if unsigned values are desired.
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, 0 otherwise
     */
    public static <T extends Enum<T>> int getUint16(WmiResult<T> result, T property, int index) {
        if (result.getCIMType(property) == Wbemcli.CIM_UINT16) {
            return getInt(result, property, index);
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "UINT16",
                result.getCIMType(property), result.getVtType(property)));
    }

    private static <T extends Enum<T>> int getInt(WmiResult<T> result, T property, int index) {
        Object o = result.getValue(property, index);
        if (o == null) {
            return 0;
        } else if (result.getVtType(property) == Variant.VT_I4) {
            return (int) o;
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "32-bit integer",
                result.getCIMType(property), result.getVtType(property)));
    }

    /**
     * Gets a Float value from a WmiResult
     *
     * @param <T>      WMI queries use an Enum to identify the fields to query, and use the enum values as keys to
     *                 retrieve the results.
     * @param result   The WmiResult from which to fetch the value
     * @param property The property (column) to fetch
     * @param index    The index (row) to fetch
     * @return The stored value if non-null, 0 otherwise
     */
    public static <T extends Enum<T>> float getFloat(WmiResult<T> result, T property, int index) {
        Object o = result.getValue(property, index);
        if (o == null) {
            return 0f;
        } else if (result.getCIMType(property) == Wbemcli.CIM_REAL32 && result.getVtType(property) == Variant.VT_R4) {
            return (float) o;
        }
        throw new ClassCastException(String.format(Locale.ROOT, CLASS_CAST_MSG, property.name(), "Float",
                result.getCIMType(property), result.getVtType(property)));
    }

}
