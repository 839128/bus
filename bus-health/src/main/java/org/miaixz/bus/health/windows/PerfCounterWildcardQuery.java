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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.PdhUtil;
import com.sun.jna.platform.win32.PdhUtil.PdhEnumObjectItems;
import com.sun.jna.platform.win32.PdhUtil.PdhException;
import com.sun.jna.platform.win32.COM.Wbemcli;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Enables queries of Performance Counters using wild cards to filter instances
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class PerfCounterWildcardQuery {

    private static final boolean PERF_DISABLE_ALL_ON_FAILURE = Config.get(Config._WINDOWS_PERF_DISABLE_ALL_ON_FAILURE,
            false);

    // Use a thread safe set to cache failed pdh queries
    private static final Set<String> FAILED_QUERY_CACHE = ConcurrentHashMap.newKeySet();

    /**
     * Query the a Performance Counter using PDH, with WMI backup on failure, for values corresponding to the property
     * enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements {@link PerfCounterQuery.PdhCounterProperty} and contains the WMI
     *                     field (Enum value) and PDH Counter string (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will be refreshed at the same
     *                     time
     * @param perfWmiClass The WMI PerfData_RawData_* class corresponding to the PDH object
     * @return A pair containing a list of instances and an {@link EnumMap} of the corresponding values indexed by
     *         {@code propertyEnum} on success, or an empty list and empty map if both PDH and WMI queries failed.
     */
    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValues(
            Class<T> propertyEnum, String perfObject, String perfWmiClass) {
        return queryInstancesAndValues(propertyEnum, perfObject, perfWmiClass, null);
    }

    /**
     * Query the a Performance Counter using PDH, with WMI backup on failure, for values corresponding to the property
     * enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements {@link PerfCounterQuery.PdhCounterProperty} and contains the WMI
     *                     field (Enum value) and PDH Counter string (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will be refreshed at the same
     *                     time
     * @param perfWmiClass The WMI PerfData_RawData_* class corresponding to the PDH object
     * @param customFilter a custom instance filter to use. If null, uses the first element of the property enum
     * @return A pair containing a list of instances and an {@link EnumMap} of the corresponding values indexed by
     *         {@code propertyEnum} on success, or an empty list and empty map if both PDH and WMI queries failed.
     */
    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValues(
            Class<T> propertyEnum, String perfObject, String perfWmiClass, String customFilter) {
        if (FAILED_QUERY_CACHE.isEmpty()
                || (!PERF_DISABLE_ALL_ON_FAILURE && !FAILED_QUERY_CACHE.contains(perfObject))) {
            Pair<List<String>, Map<T, List<Long>>> instancesAndValuesMap = queryInstancesAndValuesFromPDH(propertyEnum,
                    perfObject, customFilter);
            if (!instancesAndValuesMap.getLeft().isEmpty()) {
                return instancesAndValuesMap;
            }
            // If we are here, query returned no results
            if (StringKit.isBlank(customFilter)) {
                if (PERF_DISABLE_ALL_ON_FAILURE) {
                    Logger.info("Disabling further attempts to query performance counters.");
                } else {
                    Logger.info("Disabling further attempts to query {}.", perfObject);
                }
                FAILED_QUERY_CACHE.add(perfObject);
            }
        }
        return queryInstancesAndValuesFromWMI(propertyEnum, perfWmiClass);
    }

    /**
     * Query the a Performance Counter using PDH for values corresponding to the property enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements {@link PerfCounterQuery.PdhCounterProperty} and contains the WMI
     *                     field (Enum value) and PDH Counter string (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will be refreshed at the same
     *                     time
     * @return An pair containing a list of instances and an {@link EnumMap} of the corresponding values indexed by
     *         {@code propertyEnum} on success, or an empty list and empty map if the PDH query failed.
     */
    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromPDH(
            Class<T> propertyEnum, String perfObject) {
        return queryInstancesAndValuesFromPDH(propertyEnum, perfObject, null);
    }

    /**
     * Query the a Performance Counter using PDH for values corresponding to the property enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements {@link PerfCounterQuery.PdhCounterProperty} and contains the WMI
     *                     field (Enum value) and PDH Counter string (instance and counter)
     * @param perfObject   The PDH object for this counter; all counters on this object will be refreshed at the same
     *                     time
     * @param customFilter a custom instance filter to use. If null, uses the first element of the property enum
     * @return An pair containing a list of instances and an {@link EnumMap} of the corresponding values indexed by
     *         {@code propertyEnum} on success, or an empty list and empty map if the PDH query failed.
     */
    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromPDH(
            Class<T> propertyEnum, String perfObject, String customFilter) {
        T[] props = propertyEnum.getEnumConstants();
        if (props.length < 2) {
            throw new IllegalArgumentException("Enum " + propertyEnum.getName()
                    + " must have at least two elements, an instance filter and a counter.");
        }
        String instanceFilter = StringKit.isBlank(customFilter)
                ? ((PdhCounterWildcardProperty) propertyEnum.getEnumConstants()[0]).getCounter()
                        .toLowerCase(Locale.ROOT)
                : customFilter;
        // Localize the perfObject using different variable for the EnumObjectItems
        // Will still use unlocalized perfObject for the query
        String perfObjectLocalized = PerfCounterQuery.localizeIfNeeded(perfObject, true);

        // Get list of instances
        PdhEnumObjectItems objectItems = null;
        try {
            objectItems = PdhUtil.PdhEnumObjectItems(null, null, perfObjectLocalized, 100);
        } catch (PdhException e) {
            Logger.warn(
                    "Failed to locate performance object for {} in the registry. Performance counters may be corrupt. {}",
                    perfObjectLocalized, e.getMessage());
        }
        if (objectItems == null) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        List<String> instances = objectItems.getInstances();
        // Filter out instances not matching filter
        instances.removeIf(i -> !Builder.wildcardMatch(i.toLowerCase(Locale.ROOT), instanceFilter));
        EnumMap<T, List<Long>> valuesMap = new EnumMap<>(propertyEnum);
        try (PerfCounterQueryHandler pdhQueryHandler = new PerfCounterQueryHandler()) {
            // Set up the query and counter handles
            EnumMap<T, List<PerfDataKit.PerfCounter>> counterListMap = new EnumMap<>(propertyEnum);
            // Start at 1, first counter defines instance filter
            for (int i = 1; i < props.length; i++) {
                T prop = props[i];
                List<PerfDataKit.PerfCounter> counterList = new ArrayList<>(instances.size());
                for (String instance : instances) {
                    PerfDataKit.PerfCounter counter = PerfDataKit.createCounter(perfObject, instance,
                            ((PdhCounterWildcardProperty) prop).getCounter());
                    if (!pdhQueryHandler.addCounterToQuery(counter)) {
                        return Pair.of(Collections.emptyList(), Collections.emptyMap());
                    }
                    counterList.add(counter);
                }
                counterListMap.put(prop, counterList);
            }
            // And then query. Zero timestamp means update failed
            if (0 < pdhQueryHandler.updateQuery()) {
                // Start at 1, first counter defines instance filter
                for (int i = 1; i < props.length; i++) {
                    T prop = props[i];
                    List<Long> values = new ArrayList<>();
                    for (PerfDataKit.PerfCounter counter : counterListMap.get(prop)) {
                        values.add(pdhQueryHandler.queryCounter(counter));
                    }
                    valuesMap.put(prop, values);
                }
            }
        }
        return Pair.of(instances, valuesMap);
    }

    /**
     * Query the a Performance Counter using WMI for values corresponding to the property enum.
     *
     * @param <T>          The enum type of {@code propertyEnum}
     * @param propertyEnum An enum which implements {@link PerfCounterQuery.PdhCounterProperty} and contains the WMI
     *                     field (Enum value) and PDH Counter string (instance and counter)
     * @param wmiClass     The WMI PerfData_RawData_* class corresponding to the PDH object
     * @return An pair containing a list of instances and an {@link EnumMap} of the corresponding values indexed by
     *         {@code propertyEnum} on success, or an empty list and empty map if the WMI query failed.
     */
    public static <T extends Enum<T>> Pair<List<String>, Map<T, List<Long>>> queryInstancesAndValuesFromWMI(
            Class<T> propertyEnum, String wmiClass) {
        List<String> instances = new ArrayList<>();
        EnumMap<T, List<Long>> valuesMap = new EnumMap<>(propertyEnum);
        WmiQuery<T> query = new WmiQuery<>(wmiClass, propertyEnum);
        WmiResult<T> result = Objects.requireNonNull(WmiQueryHandler.createInstance()).queryWMI(query);
        if (result.getResultCount() > 0) {
            for (T prop : propertyEnum.getEnumConstants()) {
                // First element is instance name
                if (prop.ordinal() == 0) {
                    for (int i = 0; i < result.getResultCount(); i++) {
                        instances.add(WmiKit.getString(result, prop, i));
                    }
                } else {
                    List<Long> values = new ArrayList<>();
                    for (int i = 0; i < result.getResultCount(); i++) {
                        switch (result.getCIMType(prop)) {
                        case Wbemcli.CIM_UINT16:
                            values.add((long) WmiKit.getUint16(result, prop, i));
                            break;
                        case Wbemcli.CIM_UINT32:
                            values.add(WmiKit.getUint32asLong(result, prop, i));
                            break;
                        case Wbemcli.CIM_UINT64:
                            values.add(WmiKit.getUint64(result, prop, i));
                            break;
                        case Wbemcli.CIM_DATETIME:
                            values.add(WmiKit.getDateTime(result, prop, i).toInstant().toEpochMilli());
                            break;
                        default:
                            throw new ClassCastException("Unimplemented CIM Type Mapping.");
                        }
                    }
                    valuesMap.put(prop, values);
                }
            }
        }
        return Pair.of(instances, valuesMap);
    }

    /**
     * Contract for Counter Property Enums
     */
    public interface PdhCounterWildcardProperty {
        /**
         * @return Returns the counter. The first element of the enum will return the instance filter rather than a
         *         counter.
         */
        String getCounter();
    }

}
