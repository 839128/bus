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
package org.miaixz.bus.image.metric.api;

import java.io.Closeable;
import java.security.cert.X509Certificate;
import java.util.EnumSet;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.Device;
import org.miaixz.bus.image.metric.WebApplication;
import org.miaixz.bus.image.metric.net.ApplicationEntity;
import org.miaixz.bus.image.metric.net.ApplicationEntityInfo;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public interface DicomConfiguration extends Closeable {

    WebApplication[] listWebApplicationInfos(WebApplication keys) throws InternalException;

    boolean configurationExists() throws InternalException;

    boolean purgeConfiguration() throws InternalException;

    boolean registerAETitle(String aet) throws InternalException;

    boolean registerWebAppName(String webAppName) throws InternalException;

    void unregisterAETitle(String aet) throws InternalException;

    void unregisterWebAppName(String webAppName) throws InternalException;

    ApplicationEntity findApplicationEntity(String aet) throws InternalException;

    WebApplication findWebApplication(String name) throws InternalException;

    Device findDevice(String name) throws InternalException;

    /**
     * 查询具有指定属性的设备
     *
     * @param keys 设备属性必须与*匹配或为空，以获取所有已配置设备的信息
     * @return 具有匹配属性的已配置设备*的DeviceInfo对象数组
     * @throws InternalException 异常
     */
    Device[] listDeviceInfos(Device keys) throws InternalException;

    /**
     * 查询具有指定属性的应用程序实体
     *
     * @param keys 应与匹配或为空的应用程序实体属性将获取所有已配置的应用程序实体的信息
     * @return 具有匹配属性的已配置应用程序实体*的ApplicationEntityInfo对象数组
     * @throws InternalException 异常
     */
    ApplicationEntityInfo[] listAETInfos(ApplicationEntityInfo keys) throws InternalException;

    String[] listDeviceNames() throws InternalException;

    String[] listRegisteredAETitles() throws InternalException;

    String[] listRegisteredWebAppNames() throws InternalException;

    ConfigurationChanges persist(Device device, EnumSet<Option> options) throws InternalException;

    ConfigurationChanges merge(Device device, EnumSet<Option> options) throws InternalException;

    ConfigurationChanges removeDevice(String name, EnumSet<Option> options) throws InternalException;

    byte[][] loadDeviceVendorData(String deviceName) throws InternalException;

    ConfigurationChanges updateDeviceVendorData(String deviceName, byte[]... vendorData) throws InternalException;

    String deviceRef(String name);

    void persistCertificates(String ref, X509Certificate... certs) throws InternalException;

    void removeCertificates(String ref) throws InternalException;

    X509Certificate[] findCertificates(String dn) throws InternalException;

    void close();

    void sync() throws InternalException;

    <T> T getDicomConfigurationExtension(Class<T> clazz);

    enum Option {
        REGISTER, PRESERVE_VENDOR_DATA, PRESERVE_CERTIFICATE, CONFIGURATION_CHANGES, CONFIGURATION_CHANGES_VERBOSE
    }

}
