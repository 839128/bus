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
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.UsbDevice;
import org.miaixz.bus.health.builtin.hardware.common.AbstractUsbDevice;

/**
 * Solaris Usb Device
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public class SolarisUsbDevice extends AbstractUsbDevice {

    private static final String PCI_TYPE_USB = "000c";

    public SolarisUsbDevice(String name, String vendor, String vendorId, String productId, String serialNumber,
            String uniqueDeviceId, List<UsbDevice> connectedDevices) {
        super(name, vendor, vendorId, productId, serialNumber, uniqueDeviceId, connectedDevices);
    }

    /**
     * Instantiates a list of {@link UsbDevice} objects, representing devices connected via a usb port (including
     * internal devices). If the value of {@code tree} is true, the top level devices returned from this method are the
     * USB Controllers; connected hubs and devices in its device tree share that controller's bandwidth. If the value of
     * {@code tree} is false, USB devices (not controllers) are listed in a single flat list.
     *
     * @param tree If true, returns a list of controllers, which requires recursive iteration of connected devices. If
     *             false, returns a flat list of devices excluding controllers.
     * @return a list of {@link UsbDevice} objects.
     */
    public static List<UsbDevice> getUsbDevices(boolean tree) {
        List<UsbDevice> devices = getUsbDevices();
        if (tree) {
            return devices;
        }
        List<UsbDevice> deviceList = new ArrayList<>();
        // Top level is controllers; they won't be added to the list, but all
        // their connected devices will be
        for (UsbDevice device : devices) {
            deviceList.add(new SolarisUsbDevice(device.getName(), device.getVendor(), device.getVendorId(),
                    device.getProductId(), device.getSerialNumber(), device.getUniqueDeviceId(),
                    Collections.emptyList()));
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
        return deviceList;
    }

    private static List<UsbDevice> getUsbDevices() {
        Map<String, String> nameMap = new HashMap<>();
        Map<String, String> vendorIdMap = new HashMap<>();
        Map<String, String> productIdMap = new HashMap<>();
        Map<String, List<String>> hubMap = new HashMap<>();
        Map<String, String> deviceTypeMap = new HashMap<>();

        // Enumerate all usb devices and build information maps
        List<String> devices = Executor.runNative("prtconf -pv");
        if (devices.isEmpty()) {
            return Collections.emptyList();
        }
        // For each item enumerated, store information in the maps
        Map<Integer, String> lastParent = new HashMap<>();
        String key = Normal.EMPTY;
        int indent = 0;
        List<String> usbControllers = new ArrayList<>();
        for (String line : devices) {
            // Node 0x... identifies start of a new tree
            if (line.contains("Node 0x")) {
                // Remove indent for key
                key = line.replaceFirst("^\\s*", Normal.EMPTY);
                // Calculate indent and store as last parent at this depth
                int depth = line.length() - key.length();
                // Store first indent for future use
                if (indent == 0) {
                    indent = depth;
                }
                // Store this Node ID as parent at this depth
                lastParent.put(depth, key);
                // Add as child to appropriate parent
                if (depth > indent) {
                    // Has a parent. Get parent and add this node to child list
                    hubMap.computeIfAbsent(lastParent.get(depth - indent), x -> new ArrayList<>()).add(key);
                } else {
                    // No parent, add to controllers list
                    usbControllers.add(key);
                }
            } else if (!key.isEmpty()) {
                // We are currently processing for node identified by key. Save
                // approrpriate variables to maps.
                line = line.trim();
                if (line.startsWith("model:")) {
                    nameMap.put(key, Parsing.getSingleQuoteStringValue(line));
                } else if (line.startsWith("name:")) {
                    // Name is backup for model if model doesn't exist, so only
                    // put if key doesn't yet exist
                    nameMap.putIfAbsent(key, Parsing.getSingleQuoteStringValue(line));
                } else if (line.startsWith("vendor-id:")) {
                    // Format: vendor-id: 00008086
                    vendorIdMap.put(key, line.substring(line.length() - 4));
                } else if (line.startsWith("device-id:")) {
                    // Format: device-id: 00002440
                    productIdMap.put(key, line.substring(line.length() - 4));
                } else if (line.startsWith("class-code:")) {
                    // USB devices are 000cxxxx
                    deviceTypeMap.putIfAbsent(key, line.substring(line.length() - 8, line.length() - 4));
                } else if (line.startsWith("device_type:")) {
                    // USB devices are 000cxxxx
                    deviceTypeMap.putIfAbsent(key, Parsing.getSingleQuoteStringValue(line));
                }
            }
        }

        // Build tree and return
        List<UsbDevice> controllerDevices = new ArrayList<>();
        for (String controller : usbControllers) {
            // Only do controllers that are USB device type
            if (PCI_TYPE_USB.equals(deviceTypeMap.getOrDefault(controller, Normal.EMPTY))
                    || "usb".equals(deviceTypeMap.getOrDefault(controller, Normal.EMPTY))) {
                controllerDevices.add(
                        getDeviceAndChildren(controller, "0000", "0000", nameMap, vendorIdMap, productIdMap, hubMap));
            }
        }
        return controllerDevices;
    }

    private static void addDevicesToList(List<UsbDevice> deviceList, List<UsbDevice> list) {
        for (UsbDevice device : list) {
            deviceList.add(device);
            addDevicesToList(deviceList, device.getConnectedDevices());
        }
    }

    /**
     * Recursively creates SolarisUsbDevices by fetching information from maps to populate fields
     *
     * @param devPath      The device node path.
     * @param vid          The default (parent) vendor ID
     * @param pid          The default (parent) product ID
     * @param nameMap      the map of names
     * @param vendorIdMap  the map of vendorIds
     * @param productIdMap the map of productIds
     * @param hubMap       the map of hubs
     * @return A SolarisUsbDevice corresponding to this device
     */
    private static SolarisUsbDevice getDeviceAndChildren(String devPath, String vid, String pid,
            Map<String, String> nameMap, Map<String, String> vendorIdMap, Map<String, String> productIdMap,
            Map<String, List<String>> hubMap) {
        String vendorId = vendorIdMap.getOrDefault(devPath, vid);
        String productId = productIdMap.getOrDefault(devPath, pid);
        List<String> childPaths = hubMap.getOrDefault(devPath, new ArrayList<>());
        List<UsbDevice> usbDevices = new ArrayList<>();
        for (String path : childPaths) {
            usbDevices.add(getDeviceAndChildren(path, vendorId, productId, nameMap, vendorIdMap, productIdMap, hubMap));
        }
        Collections.sort(usbDevices);
        return new SolarisUsbDevice(nameMap.getOrDefault(devPath, vendorId + Symbol.COLON + productId), Normal.EMPTY,
                vendorId, productId, Normal.EMPTY, devPath, usbDevices);
    }

}
