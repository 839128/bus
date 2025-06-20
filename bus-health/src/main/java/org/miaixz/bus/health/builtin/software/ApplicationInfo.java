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
package org.miaixz.bus.health.builtin.software;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents common information about an installed application across different operating systems. This class provides
 * standardized access to essential application details while allowing flexibility for OS-specific fields via an
 * additional information map.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class ApplicationInfo {

    /**
     * The name of the application.
     */
    private final String name;
    /**
     * The version of the application.
     */
    private final String version;
    /**
     * The vendor or publisher of the application.
     */
    private final String vendor;
    /**
     * The installation or last modified timestamp of the application in milliseconds since epoch. This represents the
     * Unix timestamp.
     */
    private final long timestamp;
    /**
     * A map containing additional application details such as install location, source, etc. Keys are field names, and
     * values are corresponding details.
     */
    private final Map<String, String> additionalInfo;

    /**
     * Constructs an {@code ApplicationInfo} object with the specified details.
     *
     * @param name           The name of the application.
     * @param version        The version of the application.
     * @param vendor         The vendor or publisher of the application.
     * @param timestamp      The installation or last modified timestamp in milliseconds since epoch.
     * @param additionalInfo A map of additional information (can be {@code null}, in which case an empty map is used).
     */
    public ApplicationInfo(String name, String version, String vendor, long timestamp,
            Map<String, String> additionalInfo) {
        this.name = name;
        this.version = version;
        this.vendor = vendor;
        this.timestamp = timestamp;
        this.additionalInfo = additionalInfo != null ? new LinkedHashMap<>(additionalInfo) : Collections.emptyMap();
    }

    /**
     * Gets the name of the installed application.
     *
     * @return The application name, or an empty string if not available.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the version of the installed application.
     *
     * @return The application version, or an empty string if not available.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the vendor or publisher of the installed application.
     *
     * @return The vendor name, or an empty string if not available.
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Gets the last modified or installation time of the application. The timestamp is represented in milliseconds
     * since the Unix epoch (January 1, 1970, UTC).
     * <p>
     * - On Windows, this corresponds to the application's install date. - On Linux, it represents the package's
     * installation or last modified time. - On macOS, it reflects the last modification timestamp of the application
     * bundle.
     * </p>
     *
     * @return The last modified time in epoch milliseconds, or {@code 0} if unavailable.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets additional application details that are OS-specific and not covered by the main fields. This map may include
     * attributes like installation location, source, architecture, or other metadata.
     *
     * @return A map containing optional key-value pairs of application details.
     */
    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicationInfo)) {
            return false;
        }
        ApplicationInfo that = (ApplicationInfo) o;
        return timestamp == that.timestamp && Objects.equals(name, that.name) && Objects.equals(version, that.version)
                && Objects.equals(vendor, that.vendor) && Objects.equals(additionalInfo, that.additionalInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, vendor, timestamp, additionalInfo);
    }

    @Override
    public String toString() {
        return "AppInfo{" + "name=" + name + ", version=" + version + ", vendor=" + vendor + ", timestamp=" + timestamp
                + ", additionalInfo=" + additionalInfo + '}';
    }

}
