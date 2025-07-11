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
package org.miaixz.bus.health.builtin.hardware;

import java.net.NetworkInterface;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * A network interface in the machine, including statistics. Thread safe for the designed use of retrieving the most
 * recent data. Users should be aware that the {@link #updateAttributes()} method may update attributes, including the
 * time stamp, and should externally synchronize such usage to ensure consistent calculations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public interface NetworkIF {

    /**
     * Gets the {@link java.net.NetworkInterface} object.
     *
     * @return the network interface, an instance of {@link java.net.NetworkInterface}.
     */
    NetworkInterface queryNetworkInterface();

    /**
     * Interface name.
     *
     * @return The interface name.
     */
    String getName();

    /**
     * Interface index.
     *
     * @return The index of the network interface.
     */
    int getIndex();

    /**
     * Interface description.
     *
     * @return The description of the network interface. On some platforms, this is identical to the name.
     */
    String getDisplayName();

    /**
     * The {@code ifAlias} as described in RFC 2863. The ifAlias object allows a network manager to give one or more
     * interfaces their own unique names, irrespective of any interface-stack relationship. Further, the ifAlias name is
     * non-volatile, and thus an interface must retain its assigned ifAlias value across reboots, even if an agent
     * chooses a new ifIndex value for the interface. Only implemented for Windows (Vista and newer) and Linux.
     *
     * @return The {@code ifAlias} of the interface if available, otherwise the empty string.
     */
    default String getIfAlias() {
        return Normal.EMPTY;
    }

    /**
     * The {@code ifOperStatus} as described in RFC 2863. Only implemented for Windows (Vista and newer) and Linux.
     *
     * @return The current operational state of the interface.
     */
    default IfOperStatus getIfOperStatus() {
        return IfOperStatus.UNKNOWN;
    }

    /**
     * The interface Maximum Transmission Unit (MTU).
     *
     * @return The MTU of the network interface. The value is a 32-bit integer which may be unsigned on some operating
     *         systems. On Windows, some non-physical interfaces (e.g., loopback) may return a value of -1 which is
     *         equivalent to the maximum unsigned integer value. This value is set when the {@link NetworkIF} is
     *         instantiated and may not be up to date.
     */
    long getMTU();

    /**
     * The Media Access Control (MAC) address.
     *
     * @return The MAC Address. This value is set when the {@link NetworkIF} is instantiated and may not be up to date.
     */
    String getMacaddr();

    /**
     * The Internet Protocol (IP) v4 address.
     *
     * @return An array of IPv4 Addresses. This value is set when the {@link NetworkIF} is instantiated and may not be
     *         up to date.
     */
    String[] getIPv4addr();

    /**
     * The Internet Protocol (IP) v4 subnet masks.
     *
     * @return An array of IPv4 subnet mask lengths, corresponding to the IPv4 addresses from {@link #getIPv4addr()}.
     *         Ranges between 0-32. This value is set when the {@link NetworkIF} is instantiated and may not be up to
     *         date.
     */
    Short[] getSubnetMasks();

    /**
     * The Internet Protocol (IP) v6 address.
     *
     * @return An array of IPv6 Addresses. This value is set when the {@link NetworkIF} is instantiated and may not be
     *         up to date.
     */
    String[] getIPv6addr();

    /**
     * The Internet Protocol (IP) v6 address.
     *
     * @return The IPv6 address prefix lengths, corresponding to the IPv6 addresses from {@link #getIPv6addr()}. Ranges
     *         between 0-128. This value is set when the {@link NetworkIF} is instantiated and may not be up to date.
     */
    Short[] getPrefixLengths();

    /**
     * (Windows, macOS) The NDIS Interface Type. NDIS interface types are registered with the Internet Assigned Numbers
     * Authority (IANA), which publishes a list of interface types periodically in the Assigned Numbers RFC, or in a
     * derivative of it that is specific to Internet network management number assignments. (Linux) ARP Protocol
     * hardware identifiers defined in {@code include/uapi/linux/if_arp.h}
     *
     * @return the ifType
     */
    default int getIfType() {
        return 0;
    }

    /**
     * (Windows Vista and higher only) The NDIS physical medium type. This member can be one of the values from the
     * {@code NDIS_PHYSICAL_MEDIUM} enumeration type defined in the {@code Ntddndis.h} header file.
     *
     * @return the ndisPhysicalMediumType
     */
    default int getNdisPhysicalMediumType() {
        return 0;
    }

    /**
     * (Windows Vista and higher) Set if a connector is present on the network interface. (Linux) Indicates the current
     * physical link state of the interface.
     *
     * @return {@code true} if there is a physical network adapter (Windows) or a connected cable (Linux), false
     *         otherwise
     */
    default boolean isConnectorPresent() {
        return false;
    }

    /**
     * Getter for the field <code>bytesRecv</code>.
     *
     * @return The Bytes Received. This value is set when the {@link NetworkIF} is instantiated and may not be up to
     *         date. To update this value, execute the {@link #updateAttributes()} method
     */
    long getBytesRecv();

    /**
     * Getter for the field <code>bytesSent</code>.
     *
     * @return The Bytes Sent. This value is set when the {@link NetworkIF} is instantiated and may not be up to date.
     *         To update this value, execute the {@link #updateAttributes()} method
     */
    long getBytesSent();

    /**
     * Getter for the field <code>packetsRecv</code>.
     *
     * @return The Packets Received. This value is set when the {@link NetworkIF} is instantiated and may not be up to
     *         date. To update this value, execute the {@link #updateAttributes()} method
     */
    long getPacketsRecv();

    /**
     * Getter for the field <code>packetsSent</code>.
     *
     * @return The Packets Sent. This value is set when the {@link NetworkIF} is instantiated and may not be up to date.
     *         To update this value, execute the {@link #updateAttributes()} method
     */
    long getPacketsSent();

    /**
     * Getter for the field <code>inErrors</code>.
     *
     * @return Input Errors. This value is set when the {@link NetworkIF} is instantiated and may not be up to date. To
     *         update this value, execute the {@link #updateAttributes()} method
     */
    long getInErrors();

    /**
     * Getter for the field <code>outErrors</code>.
     *
     * @return The Output Errors. This value is set when the {@link NetworkIF} is instantiated and may not be up to
     *         date. To update this value, execute the {@link #updateAttributes()} method
     */
    long getOutErrors();

    /**
     * Getter for the field <code>inDrops</code>.
     *
     * @return Incoming/Received dropped packets. On Windows, returns discarded incoming packets. This value is set when
     *         the {@link NetworkIF} is instantiated and may not be up to date. To update this value, execute the
     *         {@link #updateAttributes()} method
     */
    long getInDrops();

    /**
     * Getter for the field <code>collisions</code>.
     *
     * @return Packet collisions. On Windows, returns discarded outgoing packets. This value is set when the
     *         {@link NetworkIF} is instantiated and may not be up to date. To update this value, execute the
     *         {@link #updateAttributes()} method
     */
    long getCollisions();

    /**
     * Getter for the field <code>speed</code>.
     *
     * @return The speed of the network interface in bits per second. This value is set when the {@link NetworkIF} is
     *         instantiated and may not be up to date. To update this value, execute the {@link #updateAttributes()}
     *         method
     */
    long getSpeed();

    /**
     * Getter for the field <code>timeStamp</code>.
     *
     * @return Returns the timeStamp.
     */
    long getTimeStamp();

    /**
     * Determines if the MAC address on this interface corresponds to a known Virtual Machine.
     *
     * @return {@code true} if the MAC address corresponds to a known virtual machine.
     */
    boolean isKnownVmMacAddr();

    /**
     * Updates interface network statistics on this interface. Statistics include packets and bytes sent and received,
     * and interface speed.
     *
     * @return {@code true} if the update was successful, {@code false} otherwise.
     */
    boolean updateAttributes();

    /**
     * The current operational state of a network interface. As described in RFC 2863.
     */
    enum IfOperStatus {
        /**
         * Up and operational. Ready to pass packets.
         */
        UP(1),
        /**
         * Down and not operational. Not ready to pass packets.
         */
        DOWN(2),
        /**
         * In some test mode.
         */
        TESTING(3),
        /**
         * The interface status is unknown.
         */
        UNKNOWN(4),
        /**
         * The interface is not up, but is in a pending state, waiting for some external event.
         */
        DORMANT(5),
        /**
         * Some component is missing
         */
        NOT_PRESENT(6),
        /**
         * Down due to state of lower-layer interface(s).
         */
        LOWER_LAYER_DOWN(7);

        private final int value;

        IfOperStatus(int value) {
            this.value = value;
        }

        /**
         * Find IfOperStatus by the integer value.
         *
         * @param value Integer value specified in RFC 2863
         * @return the matching IfOperStatu or UNKNOWN if no matching IfOperStatus can be found
         */
        public static IfOperStatus byValue(int value) {
            return Arrays.stream(IfOperStatus.values()).filter(st -> st.getValue() == value).findFirst()
                    .orElse(UNKNOWN);
        }

        /**
         * @return the integer value specified in RFC 2863 for this operational status.
         */
        public int getValue() {
            return this.value;
        }

    }

}
