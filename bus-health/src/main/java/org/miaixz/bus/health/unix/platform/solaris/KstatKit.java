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
package org.miaixz.bus.health.unix.platform.solaris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.GuardedBy;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.unix.platform.solaris.software.SolarisOperatingSystem;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.solaris.Kstat2;
import com.sun.jna.platform.unix.solaris.Kstat2.Kstat2Handle;
import com.sun.jna.platform.unix.solaris.Kstat2.Kstat2Map;
import com.sun.jna.platform.unix.solaris.Kstat2.Kstat2MatcherList;
import com.sun.jna.platform.unix.solaris.Kstat2StatusException;
import com.sun.jna.platform.unix.solaris.LibKstat;
import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import com.sun.jna.platform.unix.solaris.LibKstat.KstatCtl;
import com.sun.jna.platform.unix.solaris.LibKstat.KstatNamed;

/**
 * Provides access to kstat information on Solaris
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class KstatKit {

    private static final Lock CHAIN = new ReentrantLock();
    // Only one thread may access the chain at any time, so we wrap this object in
    // the KstatChain class locked until the lock is released on auto-close.
    @GuardedBy("CHAIN")
    private static KstatCtl kstatCtl = null;

    private KstatKit() {
    }

    /**
     * Lock the Kstat chain for use by this object until it's closed.
     *
     * @return A locked copy of the chain. It should be unlocked/released when you are done with it with
     *         {@link KstatChain#close()}.
     */
    public static synchronized KstatChain openChain() {
        CHAIN.lock();
        if (kstatCtl == null) {
            kstatCtl = LibKstat.INSTANCE.kstat_open();
        }
        return new KstatChain(kstatCtl);
    }

    /**
     * Convenience method for {@link LibKstat#kstat_data_lookup} with String return values. Searches the kstat's data
     * section for the record with the specified name. This operation is valid only for kstat types which have named
     * data records. Currently, only the KSTAT_TYPE_NAMED and KSTAT_TYPE_TIMER kstats have named data records.
     *
     * @param ksp  The kstat to search
     * @param name The key for the name-value pair, or name of the timer as applicable
     * @return The value as a String.
     */
    public static String dataLookupString(Kstat ksp, String name) {
        if (ksp.ks_type != LibKstat.KSTAT_TYPE_NAMED && ksp.ks_type != LibKstat.KSTAT_TYPE_TIMER) {
            throw new IllegalArgumentException("Not a kstat_named or kstat_timer kstat.");
        }
        Pointer p = LibKstat.INSTANCE.kstat_data_lookup(ksp, name);
        if (p == null) {
            Logger.debug("Failed to lookup kstat value for key {}", name);
            return Normal.EMPTY;
        }
        KstatNamed data = new KstatNamed(p);
        switch (data.data_type) {
        case LibKstat.KSTAT_DATA_CHAR:
            return Native.toString(data.value.charc, Charset.UTF_8);
        case LibKstat.KSTAT_DATA_INT32:
            return Integer.toString(data.value.i32);
        case LibKstat.KSTAT_DATA_UINT32:
            return Formats.toUnsignedString(data.value.ui32);
        case LibKstat.KSTAT_DATA_INT64:
            return Long.toString(data.value.i64);
        case LibKstat.KSTAT_DATA_UINT64:
            return Formats.toUnsignedString(data.value.ui64);
        case LibKstat.KSTAT_DATA_STRING:
            return data.value.str.addr.getString(0);
        default:
            Logger.error("Unimplemented kstat data type {}", data.data_type);
            return Normal.EMPTY;
        }
    }

    /**
     * Convenience method for {@link LibKstat#kstat_data_lookup} with numeric return values. Searches the kstat's data
     * section for the record with the specified name. This operation is valid only for kstat types which have named
     * data records. Currently, only the KSTAT_TYPE_NAMED and KSTAT_TYPE_TIMER kstats have named data records.
     *
     * @param ksp  The kstat to search
     * @param name The key for the name-value pair, or name of the timer as applicable
     * @return The value as a long. If the data type is a character or string type, returns 0 and logs an error.
     */
    public static long dataLookupLong(Kstat ksp, String name) {
        if (ksp.ks_type != LibKstat.KSTAT_TYPE_NAMED && ksp.ks_type != LibKstat.KSTAT_TYPE_TIMER) {
            throw new IllegalArgumentException("Not a kstat_named or kstat_timer kstat.");
        }
        Pointer p = LibKstat.INSTANCE.kstat_data_lookup(ksp, name);
        if (p == null) {
            if (Logger.isDebugEnabled()) {
                Logger.debug("Failed lo lookup kstat value on {}:{}:{} for key {}",
                        Native.toString(ksp.ks_module, Charset.US_ASCII), ksp.ks_instance,
                        Native.toString(ksp.ks_name, Charset.US_ASCII), name);
            }
            return 0L;
        }
        KstatNamed data = new KstatNamed(p);
        switch (data.data_type) {
        case LibKstat.KSTAT_DATA_INT32:
            return data.value.i32;
        case LibKstat.KSTAT_DATA_UINT32:
            return Formats.getUnsignedInt(data.value.ui32);
        case LibKstat.KSTAT_DATA_INT64:
            return data.value.i64;
        case LibKstat.KSTAT_DATA_UINT64:
            return data.value.ui64;
        default:
            Logger.error("Unimplemented or non-numeric kstat data type {}", data.data_type);
            return 0L;
        }
    }

    /**
     * Query Kstat2 iterating over maps using a wildcard indicating a 0-indexed list, such as a cpu.
     *
     * @param beforeStr The part of the string before the wildcard
     * @param afterStr  The part of the string after the wildcard
     * @param names     Names of data to query
     * @return A list of object arrays with the data corresponding to the names
     */
    public static List<Object[]> queryKstat2List(String beforeStr, String afterStr, String... names) {
        if (!SolarisOperatingSystem.HAS_KSTAT2) {
            throw new UnsupportedOperationException(
                    "Kstat2 requires Solaris 11.4+. Use SolarisOperatingSystem#HAS_KSTAT2 to test this.");
        }
        List<Object[]> results = new ArrayList<>();
        int s = 0;
        Kstat2MatcherList matchers = new Kstat2MatcherList();
        KstatKit.CHAIN.lock();
        try {
            matchers.addMatcher(Kstat2.KSTAT2_M_GLOB, beforeStr + Symbol.STAR + afterStr);
            Kstat2Handle handle = new Kstat2Handle();
            try {
                for (s = 0; s < Integer.MAX_VALUE; s++) {
                    Object[] result = new Object[names.length];
                    Kstat2Map map = handle.lookupMap(beforeStr + s + afterStr);
                    for (int i = 0; i < names.length; i++) {
                        result[i] = map.getValue(names[i]);
                    }
                    results.add(result);
                }
            } finally {
                handle.close();
            }
        } catch (Kstat2StatusException e) {
            // Expected to end iteration
            Logger.debug("Failed to get stats on {}{}{} for names {}: {}", beforeStr, s, afterStr,
                    Arrays.toString(names), e.getMessage());
        } finally {
            KstatKit.CHAIN.unlock();
            matchers.free();
        }
        return results;
    }

    /**
     * Query Kstat2 with a single map
     *
     * @param mapStr The map to query
     * @param names  Names of data to query
     * @return An object array with the data corresponding to the names
     */
    public static Object[] queryKstat2(String mapStr, String... names) {
        if (!SolarisOperatingSystem.HAS_KSTAT2) {
            throw new UnsupportedOperationException(
                    "Kstat2 requires Solaris 11.4+. Use SolarisOperatingSystem#HAS_KSTAT2 to test this.");
        }
        Object[] result = new Object[names.length];
        Kstat2MatcherList matchers = new Kstat2MatcherList();
        KstatKit.CHAIN.lock();
        try {
            matchers.addMatcher(Kstat2.KSTAT2_M_STRING, mapStr);
            Kstat2Handle handle = new Kstat2Handle();
            try {
                Kstat2Map map = handle.lookupMap(mapStr);
                for (int i = 0; i < names.length; i++) {
                    result[i] = map.getValue(names[i]);
                }
            } finally {
                handle.close();
            }
        } catch (Kstat2StatusException e) {
            Logger.debug("Failed to get stats on {} for names {}: {}", mapStr, Arrays.toString(names), e.getMessage());
        } finally {
            KstatKit.CHAIN.unlock();
            matchers.free();
        }
        return result;
    }

    /**
     * A copy of the Kstat chain, encapsulating a {@code kstat_ctl_t} object. Only one thread may actively use this
     * object at any time. The chain is created once calling {@link LibKstat#kstat_open} and then this object is
     * instantiated using the {@link KstatKit#openChain} method. Instantiating this object updates the chain using
     * {@link LibKstat#kstat_chain_update}. The control object should be closed with {@link #close}, which releases the
     * lock and allows another instance to be instantiated.
     */
    public static final class KstatChain implements AutoCloseable {

        private final KstatCtl localCtlRef;

        private KstatChain(KstatCtl ctl) {
            this.localCtlRef = ctl;
            update();
        }

        /**
         * Convenience method for {@link LibKstat#kstat_read} which gets data from the kernel for the kstat pointed to
         * by {@code ksp}. {@code ksp.ks_data} is automatically allocated (or reallocated) to be large enough to hold
         * all of the data. {@code ksp.ks_ndata} is set to the number of data fields, {@code ksp.ks_data_size} is set to
         * the total size of the data, and ksp.ks_snaptime is set to the high-resolution time at which the data snapshot
         * was taken.
         *
         * @param ksp The kstat from which to retrieve data
         * @return {@code true} if successful; {@code false} otherwise
         */
        @GuardedBy("CHAIN")
        public boolean read(Kstat ksp) {
            int retry = 0;
            while (0 > LibKstat.INSTANCE.kstat_read(localCtlRef, ksp, null)) {
                if (LibKstat.EAGAIN != Native.getLastError() || 5 <= ++retry) {
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Failed to read kstat {}:{}:{}", Native.toString(ksp.ks_module, Charset.US_ASCII),
                                ksp.ks_instance, Native.toString(ksp.ks_name, Charset.US_ASCII));
                    }
                    return false;
                }
                ThreadKit.sleep(8 << retry);
            }
            return true;
        }

        /**
         * Convenience method for {@link LibKstat#kstat_lookup}. Traverses the kstat chain, searching for a kstat with
         * the same {@code module}, {@code instance}, and {@code name} fields; this triplet uniquely identifies a kstat.
         * If {@code module} is {@code null}, {@code instance} is -1, or {@code name} is {@code null}, then those fields
         * will be ignored in the search.
         *
         * @param module   The module, or null to ignore
         * @param instance The instance, or -1 to ignore
         * @param name     The name, or null to ignore
         * @return The first match of the requested Kstat structure if found, or {@code null}
         */
        @GuardedBy("CHAIN")
        public Kstat lookup(String module, int instance, String name) {
            return LibKstat.INSTANCE.kstat_lookup(localCtlRef, module, instance, name);
        }

        /**
         * Convenience method for {@link LibKstat#kstat_lookup}. Traverses the kstat chain, searching for all kstats
         * with the same {@code module}, {@code instance}, and {@code name} fields; this triplet uniquely identifies a
         * kstat. If {@code module} is {@code null}, {@code instance} is -1, or {@code name} is {@code null}, then those
         * fields will be ignored in the search.
         *
         * @param module   The module, or null to ignore
         * @param instance The instance, or -1 to ignore
         * @param name     The name, or null to ignore
         * @return All matches of the requested Kstat structure if found, or an empty list otherwise
         */
        @GuardedBy("CHAIN")
        public List<Kstat> lookupAll(String module, int instance, String name) {
            List<Kstat> kstats = new ArrayList<>();
            for (Kstat ksp = LibKstat.INSTANCE.kstat_lookup(localCtlRef, module, instance, name); ksp != null; ksp = ksp
                    .next()) {
                if ((module == null || module.equals(Native.toString(ksp.ks_module, Charset.US_ASCII)))
                        && (instance < 0 || instance == ksp.ks_instance)
                        && (name == null || name.equals(Native.toString(ksp.ks_name, Charset.US_ASCII)))) {
                    kstats.add(ksp);
                }
            }
            return kstats;
        }

        /**
         * Convenience method for {@link LibKstat#kstat_chain_update}. Brings this kstat header chain in sync with that
         * of the kernel. This function compares the kernel's current kstat chain ID(KCID), which is incremented every
         * time the kstat chain changes, to this object's KCID.
         *
         * @return the new KCID if the kstat chain has changed, 0 if it hasn't, or -1 on failure.
         */
        @GuardedBy("CHAIN")
        public int update() {
            return LibKstat.INSTANCE.kstat_chain_update(localCtlRef);
        }

        /**
         * Release the lock on the chain.
         */
        @Override
        public void close() {
            CHAIN.unlock();
        }
    }

}
