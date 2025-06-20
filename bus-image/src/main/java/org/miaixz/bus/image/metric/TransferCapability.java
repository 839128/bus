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
package org.miaixz.bus.image.metric;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.image.Builder;
import org.miaixz.bus.image.UID;
import org.miaixz.bus.image.metric.net.ApplicationEntity;

/**
 * DICOM Standard, Part 15, Annex H: Transfer Capability - The description of the SOP classes and syntaxes supported by
 * a Network AE. An instance of the <code>TransferCapability</code> class describes the DICOM transfer capabilities of
 * an SCU or SCP in terms of a single presentation syntax. This includes the role selection (SCU or SCP), the acceptable
 * transfer syntaxes for a given SOP Class, and any extra information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TransferCapability implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852261961377L;

    private ApplicationEntity ae;
    private String commonName;
    private String sopClass;
    private Role role;
    private String[] transferSyntaxes;
    private String[] prefTransferSyntaxes = {};
    private EnumSet<QueryOption> queryOptions;
    private StorageOptions storageOptions;

    public TransferCapability() {
        this(null, UID.Verification.uid, Role.SCU, UID.ImplicitVRLittleEndian.uid);
    }

    public TransferCapability(String commonName, String sopClass, Role role, String... transferSyntaxes) {
        setCommonName(commonName);
        setSopClass(sopClass);
        setRole(role);
        setTransferSyntaxes(transferSyntaxes);
    }

    public void setApplicationEntity(ApplicationEntity ae) {
        if (ae != null) {
            if (this.ae != null)
                throw new IllegalStateException("already owned by AE " + this.ae.getAETitle());
        }
        this.ae = ae;
    }

    /**
     * 获取传输能力对象的名称,可以是有意义的名称或任何唯一的字符序列
     *
     * @return 包含常用名称的字符串
     */
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * 获取这个TransferCapability实例的角色
     *
     * @return 角色(SCU或SCP)
     */
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (role == null)
            throw new NullPointerException();

        if (this.role == role)
            return;

        ApplicationEntity ae = this.ae;
        if (ae != null)
            ae.removeTransferCapabilityFor(sopClass, this.role);

        this.role = role;

        if (ae != null)
            ae.addTransferCapability(this);
    }

    /**
     * 获取此传输能力对象的SOP类
     *
     * @return 包含SOP类UID的字符串
     */
    public String getSopClass() {
        return sopClass;
    }

    public void setSopClass(String sopClass) {
        if (sopClass.isEmpty())
            throw new IllegalArgumentException("empty sopClass");

        if (sopClass.equals(this.sopClass))
            return;

        ApplicationEntity ae = this.ae;
        if (ae != null)
            ae.removeTransferCapabilityFor(sopClass, this.role);

        this.sopClass = sopClass;

        if (ae != null)
            ae.addTransferCapability(this);
    }

    /**
     * 获取可能作为SCU请求或作为SCP提供的传输语法
     *
     * @return 传输语法列表
     */
    public String[] getTransferSyntaxes() {
        return transferSyntaxes;
    }

    public void setTransferSyntaxes(String... transferSyntaxes) {
        this.transferSyntaxes = Builder.requireContainsNoEmpty(
                Builder.requireNotEmpty(transferSyntaxes, "missing transferSyntax"), "empty transferSyntax");
    }

    public String[] getPreferredTransferSyntaxes() {
        return prefTransferSyntaxes;
    }

    public void setPreferredTransferSyntaxes(String... transferSyntaxes) {
        this.prefTransferSyntaxes = Builder.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    public boolean containsTransferSyntax(String ts) {
        return Symbol.STAR.equals(transferSyntaxes[0]) || Builder.contains(transferSyntaxes, ts);
    }

    public String selectTransferSyntax(String... transferSyntaxes) {
        if (transferSyntaxes.length == 1)
            return containsTransferSyntax(transferSyntaxes[0]) ? transferSyntaxes[0] : null;

        List<String> acceptable = retainAcceptable(transferSyntaxes);
        if (acceptable.isEmpty())
            return null;

        for (String prefTransferSyntax : prefTransferSyntaxes.length > 0 ? prefTransferSyntaxes
                : ae.getPreferredTransferSyntaxes())
            if (acceptable.contains(prefTransferSyntax))
                return prefTransferSyntax;

        return acceptable.get(0);
    }

    private List<String> retainAcceptable(String[] transferSyntaxes) {
        List<String> acceptable = new ArrayList<>(transferSyntaxes.length);
        for (String transferSyntax : transferSyntaxes) {
            if (containsTransferSyntax(transferSyntax))
                acceptable.add(transferSyntax);
        }
        return acceptable;
    }

    public EnumSet<QueryOption> getQueryOptions() {
        return queryOptions;
    }

    public void setQueryOptions(EnumSet<QueryOption> queryOptions) {
        this.queryOptions = queryOptions;
    }

    public StorageOptions getStorageOptions() {
        return storageOptions;
    }

    public void setStorageOptions(StorageOptions storageOptions) {
        this.storageOptions = storageOptions;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(Normal._512), Normal.EMPTY).toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + Symbol.SPACE;
        Builder.appendLine(sb, indent, "TransferCapability[cn: ", commonName);
        Builder.appendLine(sb, indent2, "role: ", role);
        sb.append(indent2).append("as: ");
        UID.promptTo(sopClass, sb).append(Builder.LINE_SEPARATOR);
        for (String ts : transferSyntaxes) {
            sb.append(indent2).append("ts: ");
            UID.promptTo(ts, sb).append(Builder.LINE_SEPARATOR);
        }
        if (queryOptions != null)
            sb.append(indent2).append("QueryOptions").append(queryOptions).append(Builder.LINE_SEPARATOR);
        if (storageOptions != null)
            sb.append(indent2).append(storageOptions).append(Builder.LINE_SEPARATOR);
        return sb.append(indent).append(Symbol.C_BRACKET_RIGHT);
    }

    public enum Role {
        SCU, SCP
    }

}
