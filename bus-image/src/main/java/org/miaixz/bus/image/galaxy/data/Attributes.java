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
package org.miaixz.bus.image.galaxy.data;

import java.io.*;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.image.*;
import org.miaixz.bus.image.galaxy.io.ImageEncodingOptions;
import org.miaixz.bus.image.galaxy.io.ImageInputStream;
import org.miaixz.bus.image.galaxy.io.ImageOutputStream;
import org.miaixz.bus.logger.Logger;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Attributes implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852260209995L;

    public static final String COERCE = "COERCE";
    public static final String CORRECT = "CORRECT";
    private static final int INIT_CAPACITY = 16;
    private static final int TO_STRING_LIMIT = 50;
    private static final int TO_STRING_WIDTH = 78;
    private transient Attributes parent;
    private transient String parentSequencePrivateCreator;
    private transient int parentSequenceTag;
    private transient int[] tags;
    private transient VR[] vrs;
    private transient Object[] values;
    private transient int size;
    private transient SpecificCharacterSet cs;
    private transient TimeZone tz;
    private transient int length = -1;
    private transient int[] groupLengths;
    private transient int groupLengthIndex0;
    private volatile boolean bigEndian;
    private long itemPosition = -1;
    private boolean containsSpecificCharacterSet;
    private boolean containsTimezoneOffsetFromUTC;
    private Map<String, Object> properties;
    private TimeZone defaultTimeZone;
    private volatile boolean readOnly;

    public Attributes() {
        this(false, INIT_CAPACITY);
    }

    public Attributes(boolean bigEndian) {
        this(bigEndian, INIT_CAPACITY);
    }

    public Attributes(int initialCapacity) {
        this(false, initialCapacity);
    }

    public Attributes(boolean bigEndian, int initialCapacity) {
        this.bigEndian = bigEndian;
        init(initialCapacity);
    }

    public Attributes(Attributes other) {
        this(other, other.bigEndian);
    }

    public Attributes(Attributes other, boolean bigEndian) {
        this(bigEndian, other.size);
        if (other.properties != null)
            properties = new HashMap<String, Object>(other.properties);
        addAll(other);
    }

    public Attributes(Attributes other, int... selection) {
        this(other, other.bigEndian, selection);
    }

    public Attributes(Attributes other, boolean bigEndian, int... selection) {
        this(bigEndian, selection.length);
        if (other.properties != null)
            properties = new HashMap<String, Object>(other.properties);
        addSelected(other, selection);
    }

    public Attributes(Attributes other, boolean bigEndian, Attributes selection) {
        this(bigEndian, selection.size());
        if (other.properties != null)
            properties = new HashMap<>(other.properties);
        addSelected(other, selection);
    }

    private static boolean isEmpty(Object value) {
        return (value instanceof Value) && ((Value) value).isEmpty();
    }

    private static String[] toStrings(Object val) {
        return (val instanceof String) ? new String[] { (String) val } : (String[]) val;
    }

    private static String[] splitRange(String s) {
        String[] range = new String[2];
        int delim = s.indexOf('-');
        if (delim == -1)
            range[0] = range[1] = s;
        else {
            if (delim > 0)
                range[0] = s.substring(0, delim);
            if (delim < s.length() - 1)
                range[1] = s.substring(delim + 1);
        }
        return range;
    }

    private static boolean isRange(String s) {
        return s.indexOf('-') >= 0;
    }

    private static String toString(DateRange range, VR vr, TimeZone tz, DatePrecision precision) {
        String start = range.getStartDate() != null
                ? (String) vr.toValue(new Date[] { range.getStartDate() }, tz, precision)
                : "";
        String end = range.getEndDate() != null ? (String) vr.toValue(new Date[] { range.getEndDate() }, tz, precision)
                : "";
        return toDateRangeString(start, end);
    }

    private static String toDateRangeString(String start, String end) {
        return start.equals(end) ? start : (start + Symbol.C_MINUS + end);
    }

    private static boolean containsNonASCIIStringValues(Object val, VR vr) {
        if (val instanceof Sequence) {
            for (Attributes item : ((Sequence) val)) {
                if (item.containsNonASCIIStringValues(null, null, 0, 0, null)) {
                    return true;
                }
            }
        } else
            return val != Value.NULL && vr.useSpecificCharacterSet();
        return false;
    }

    private static Object toggleEndian(VR vr, Object value, boolean toggleEndian) {
        return (toggleEndian && value instanceof byte[]) ? vr.toggleEndian((byte[]) value, true) : value;
    }

    private static boolean containsPNValue(Object v) {
        return v != Value.NULL && !new PersonName((String) v, true).isEmpty();
    }

    private static boolean equalPNValues(String[] v1, String[] v2) {
        if (v1.length != v2.length)
            return false;

        for (int i = 0; i < v1.length; i++)
            if (!equalPNValues(v1[i], v2[i]))
                return false;

        return true;
    }

    private static boolean equalPNValues(String v1, String v2) {
        return new PersonName(v1, true).equals(new PersonName(v2, true));
    }

    /**
     * Creates DICOM File Meta Information with given <i>Media Storage SOP Instance UID (0002,0013)</i>, <i>Media
     * Storage SOP Class UID (0002,0012)</i> and <i>Transfer Syntax UID (0002,0010)</i>, including optional
     * <i>Implementation Version Name (0002,0013)</i>.
     *
     * @param iuid  <i>Media Storage SOP Instance UID (0002,0013)</i>
     * @param cuid  <i>Media Storage SOP Class UID (0002,0012)</i>
     * @param tsuid <i>Transfer Syntax UID (0002,0010)</i>
     * @return created DICOM File Meta Information
     */
    public static Attributes createFileMetaInformation(String iuid, String cuid, String tsuid) {
        return createFileMetaInformation(iuid, cuid, tsuid, true);
    }

    /**
     * Creates DICOM File Meta Information with given <i>Media Storage SOP Instance UID (0002,0013)</i>, <i>Media
     * Storage SOP Class UID (0002,0012)</i> and <i>Transfer Syntax UID (0002,0010)</i>.
     *
     * @param iuid                             <i>Media Storage SOP Instance UID (0002,0013)</i>
     * @param cuid                             <i>Media Storage SOP Class UID (0002,0012)</i>
     * @param tsuid                            <i>Transfer Syntax UID (0002,0010)</i>
     * @param includeImplementationVersionName <code>true</code> if the optional <i>Implementation Version Name
     *                                         (0002,0013)</i> is to be included; <code>false</code> if it is to be
     *                                         omitted.
     * @return created DICOM File Meta Information
     */
    public static Attributes createFileMetaInformation(String iuid, String cuid, String tsuid,
            boolean includeImplementationVersionName) {
        if (iuid == null || iuid.isEmpty())
            throw new IllegalArgumentException("Missing SOP Instance UID");
        if (cuid == null || cuid.isEmpty())
            throw new IllegalArgumentException("Missing SOP Class UID");
        if (tsuid == null || tsuid.isEmpty())
            throw new IllegalArgumentException("Missing Transfer Syntax UID");

        Attributes fmi = new Attributes(6);
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[] { 0, 1 });
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, cuid);
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, VR.UI, Implementation.getClassUID());
        if (includeImplementationVersionName)
            fmi.setString(Tag.ImplementationVersionName, VR.SH, Implementation.getVersionName());
        return fmi;
    }

    public static void unifyCharacterSets(Attributes... attrsList) {
        if (attrsList.length == 0)
            return;

        SpecificCharacterSet utf8 = SpecificCharacterSet.valueOf("ISO_IR 192");
        SpecificCharacterSet commonCS = attrsList[0].getSpecificCharacterSet();
        if (!commonCS.equals(utf8)) {
            for (int i = 1; i < attrsList.length; i++) {
                SpecificCharacterSet cs = attrsList[i].getSpecificCharacterSet();
                if (!(cs.equals(commonCS) || cs.isASCII() && commonCS.containsASCII())) {
                    if (commonCS.isASCII() && cs.containsASCII())
                        commonCS = cs;
                    else {
                        commonCS = utf8;
                        break;
                    }
                }
            }
        }
        for (Attributes attrs : attrsList) {
            SpecificCharacterSet cs = attrs.getSpecificCharacterSet();
            if (!(cs.equals(commonCS))) {
                if (!cs.isASCII() || !commonCS.containsASCII())
                    attrs.decodeStringValuesUsingSpecificCharacterSet();
                attrs.setString(Tag.SpecificCharacterSet, VR.CS, commonCS.toCodes());
            }
        }
    }

    private static boolean isBulkData(Object value) {
        return value instanceof BulkData || (value instanceof Fragments && ((Fragments) value).size() > 1
                && ((Fragments) value).get(1) instanceof BulkData);
    }

    public void clear() {
        size = 0;
        Arrays.fill(tags, 0);
        Arrays.fill(vrs, null);
        Arrays.fill(values, null);
    }

    private void init(int initialCapacity) {
        this.tags = new int[initialCapacity];
        this.vrs = new VR[initialCapacity];
        this.values = new Object[initialCapacity];
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly() {
        this.readOnly = true;
        for (int i = 0, n = size; i < n; i++) {
            Object value = values[i];
            if (value instanceof Sequence)
                ((Sequence) value).setReadOnly();
            else if (value instanceof Fragments)
                ((Fragments) value).setReadOnly();
        }
    }

    private void ensureModifiable() {
        if (readOnly) {
            throw new UnsupportedOperationException("read-only");
        }
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        ensureModifiable();
        this.properties = properties;
    }

    public Object getProperty(String key, Object defVal) {
        if (properties == null)
            return defVal;

        Object val = properties.get(key);
        return val != null ? val : defVal;
    }

    public Object setProperty(String key, Object value) {
        ensureModifiable();
        if (properties == null)
            properties = new HashMap<String, Object>();
        return properties.put(key, value);
    }

    public Object clearProperty(String key) {
        ensureModifiable();
        return properties != null ? properties.remove(key) : null;
    }

    public final boolean isRoot() {
        return parent == null;
    }

    public final Attributes getRoot() {
        return isRoot() ? this : parent.getRoot();
    }

    public final int getLevel() {
        return isRoot() ? 0 : 1 + parent.getLevel();
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    public final Attributes getParent() {
        return parent;
    }

    public String getParentSequencePrivateCreator() {
        return parentSequencePrivateCreator;
    }

    public int getParentSequenceTag() {
        return parentSequenceTag;
    }

    public final int getLength() {
        return length;
    }

    Attributes setParent(Attributes parent, String parentSequencePrivateCreator, int parentSequenceTag) {
        if (parent != null) {
            if (this.parent != null)
                throw new IllegalArgumentException("Item already contained by Sequence");
            if (!containsSpecificCharacterSet)
                cs = null;
            if (!containsTimezoneOffsetFromUTC)
                tz = null;
            if (parent.bigEndian != bigEndian)
                toggleEndian();
        }
        this.parent = parent;
        this.parentSequencePrivateCreator = parentSequencePrivateCreator;
        this.parentSequenceTag = parentSequenceTag;
        return this;
    }

    private void toggleEndian() {
        for (int i = 0; i < size; i++) {
            Object value = values[i];
            if (value instanceof byte[]) {
                vrs[i].toggleEndian((byte[]) value, false);
            } else if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value) {
                    item.toggleEndian();
                }
            }
            bigEndian = !bigEndian;
        }
    }

    public final long getItemPosition() {
        return itemPosition;
    }

    public final void setItemPosition(long itemPosition) {
        this.itemPosition = itemPosition;
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public final int size() {
        return size;
    }

    public ItemPointer[] itemPointers() {
        return itemPointers(0);
    }

    private ItemPointer[] itemPointers(int n) {
        if (parent == null)
            return new ItemPointer[n];

        ItemPointer[] itemPointers = parent.itemPointers(n + 1);
        itemPointers[itemPointers.length - n - 1] = new ItemPointer(parentSequencePrivateCreator, parentSequenceTag,
                itemIndex());
        return itemPointers;
    }

    public int itemIndex() {
        if (parent == null)
            return -1;

        Sequence seq = parent.getSequence(parentSequencePrivateCreator, parentSequenceTag);
        if (seq == null)
            return -1;

        return seq.indexOf(this);
    }

    public int[] tags() {
        return Arrays.copyOf(tags, size);
    }

    public void trimToSize() {
        trimToSize(false);
    }

    public void trimToSize(boolean recursive) {
        ensureModifiable();
        int oldCapacity = tags.length;
        if (size < oldCapacity) {
            tags = Arrays.copyOf(tags, size);
            vrs = Arrays.copyOf(vrs, size);
            values = Arrays.copyOf(values, size);
        }
        if (recursive)
            for (Object value : values) {
                if (value instanceof Sequence) {
                    ((Sequence) value).trimToSize(recursive);
                } else if (value instanceof Fragments)
                    ((Fragments) value).trimToSize();
            }
    }

    public void internalizeStringValues(boolean decode) {
        ensureModifiable();
        SpecificCharacterSet cs = getSpecificCharacterSet();
        for (int i = 0; i < values.length; i++) {
            VR vr = vrs[i];
            Object value = values[i];
            if (vr.isStringType()) {
                if (value instanceof byte[]) {
                    if (!decode)
                        continue;
                    value = vr.toStrings(value, bigEndian, cs);
                }
                if (value instanceof String)
                    values[i] = ((String) value).intern();
                else if (value instanceof String[] ss) {
                    for (int j = 0; j < ss.length; j++)
                        ss[j] = ss[j].intern();
                }
            } else if (value instanceof Sequence)
                for (Attributes item : (Sequence) value)
                    item.internalizeStringValues(decode);
        }
    }

    private void decodeStringValuesUsingSpecificCharacterSet() {
        Object value;
        VR vr;
        SpecificCharacterSet cs = getSpecificCharacterSet();
        for (int i = 0; i < size; i++) {
            value = values[i];
            if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value)
                    item.decodeStringValuesUsingSpecificCharacterSet();
            } else if ((vr = vrs[i]).useSpecificCharacterSet())
                if (value instanceof byte[])
                    values[i] = vr.toStrings(value, bigEndian, cs);
        }
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = tags.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = Math.max(minCapacity, oldCapacity << 1);
            tags = Arrays.copyOf(tags, newCapacity);
            vrs = Arrays.copyOf(vrs, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }
    }

    public Attributes getNestedDataset(int sequenceTag) {
        return getNestedDataset(null, sequenceTag, 0);
    }

    public Attributes getNestedDataset(int sequenceTag, int itemIndex) {
        return getNestedDataset(null, sequenceTag, itemIndex);
    }

    public Attributes getNestedDataset(String privateCreator, int sequenceTag) {
        return getNestedDataset(privateCreator, sequenceTag, 0);
    }

    public Attributes getNestedDataset(String privateCreator, int sequenceTag, int itemIndex) {
        Object value = getSequence(privateCreator, sequenceTag);
        if (value == null)
            return null;

        Sequence sq = (Sequence) value;
        if (itemIndex >= sq.size())
            return null;

        return sq.get(itemIndex);
    }

    public Attributes getNestedDataset(ItemPointer... itemPointers) {
        return getNestedDataset(Arrays.asList(itemPointers));
    }

    public Attributes getNestedDataset(List<ItemPointer> itemPointers) {
        Attributes item = this;
        for (ItemPointer ip : itemPointers) {
            Object value = item.getValue(ip.privateCreator, ip.sequenceTag);
            if (!(value instanceof Sequence sq))
                return null;

            if (ip.itemIndex >= sq.size())
                return null;

            item = sq.get(ip.itemIndex);
        }
        return item;
    }

    public Attributes getFunctionGroup(int sequenceTag, int frameIndex) {
        Attributes sfgs = getNestedDataset(Tag.SharedFunctionalGroupsSequence);
        if (sfgs == null)
            return null;

        Attributes item = sfgs.getNestedDataset(sequenceTag);
        if (item != null)
            return item;

        Attributes fgs = getNestedDataset(Tag.PerFrameFunctionalGroupsSequence, frameIndex);
        if (fgs == null)
            return null;

        return fgs.getNestedDataset(sequenceTag);
    }

    private int indexForInsertOf(int tag) {
        return size == 0 ? -1 : tags[size - 1] < tag ? -(size + 1) : indexOf(tag);
    }

    private int indexOf(int tag) {
        return Arrays.binarySearch(tags, 0, size, tag);
    }

    private int indexOf(String privateCreator, int tag) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1)
                return -1;
            tag = Tag.toPrivateTag(creatorTag, tag);
        }
        return indexOf(tag);
    }

    /**
     * resolves to the actual private tag, given a private tag with placeholers (like 0011,xx13)
     */
    public int tagOf(String privateCreator, int tag) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1)
                return -1;
            tag = Tag.toPrivateTag(creatorTag, tag);
        }
        return tag;
    }

    private int creatorTagOf(String privateCreator, int tag, boolean reserve) {
        if (!Tag.isPrivateGroup(tag))
            throw new IllegalArgumentException(Tag.toString(tag) + " is not a private Data Element");

        int group = tag & 0xffff0000;
        int creatorTag = group | 0x10;
        int index = indexOf(creatorTag);
        if (index < 0)
            index = -index - 1;
        while (index < size && (tags[index] & 0xffffff00) == group) {
            creatorTag = tags[index];
            if (vrs[index].isStringType()) {
                Object creatorID = decodeStringValue(index);
                if (privateCreator.equals(creatorID))
                    return creatorTag;
            }
            index++;
            creatorTag++;
        }
        if (!reserve)
            return -1;

        if ((creatorTag & 0xff00) != 0)
            throw new IllegalStateException("No free block for Private Element " + Tag.toString(tag));
        setString(creatorTag, VR.LO, privateCreator);
        return creatorTag;
    }

    private Object decodeStringValue(int index) {
        Object value = loadBulkData(vrs[index], values[index]);
        return decodeStringValue(index, value);
    }

    private Object decodeStringValue(int index, Object value) {
        if (value instanceof byte[]) {
            value = vrs[index].toStrings(value, bigEndian, getSpecificCharacterSet(vrs[index]));
            if (value instanceof String && ((String) value).isEmpty())
                value = Value.NULL;
            values[index] = value;
        }
        return value;
    }

    private Object loadAndStoreBulkData(int index) {
        return values[index] = loadBulkData(vrs[index], values[index]);
    }

    private Object loadBulkData(VR vr, Object value) {
        try {
            return (value instanceof BulkData) ? ((BulkData) value).toBytes(vr, bigEndian) : value;
        } catch (Exception e) {
            Logger.info("Failed to load {}", value);
            return Value.NULL;
        }
    }

    public SpecificCharacterSet getSpecificCharacterSet(VR vr) {
        return vr.useSpecificCharacterSet() ? getSpecificCharacterSet() : SpecificCharacterSet.ASCII;
    }

    private double[] decodeDSValue(int index) {
        Object value = index < 0 ? Value.NULL : values[index];
        if (value == Value.NULL)
            return new double[] {};

        if (value instanceof double[])
            return (double[]) value;

        double[] ds;
        if (value instanceof byte[])
            value = vrs[index].toStrings(value, bigEndian, SpecificCharacterSet.ASCII);
        if (value instanceof String s) {
            if (s.isEmpty()) {
                values[index] = Value.NULL;
                return new double[] {};
            }
            ds = new double[] { Builder.parseDS(s) };
        } else { // value instanceof String[]
            String[] ss = (String[]) value;
            ds = new double[ss.length];
            for (int i = 0; i < ds.length; i++) {
                String s = ss[i];
                ds[i] = (s != null && !s.isEmpty()) ? Builder.parseDS(s) : Double.NaN;
            }
        }
        values[index] = ds;
        return ds;
    }

    private long[] decodeISValue(int index) {
        Object value = index < 0 ? Value.NULL : values[index];
        if (value == Value.NULL)
            return new long[] {};

        if (value instanceof long[])
            return (long[]) value;

        long[] ls;
        if (value instanceof byte[])
            value = vrs[index].toStrings(value, bigEndian, SpecificCharacterSet.ASCII);
        if (value instanceof String s) {
            if (s.isEmpty()) {
                values[index] = Value.NULL;
                return new long[] {};
            }
            ls = new long[] { Builder.parseIS(s) };
        } else { // value instanceof String[]
            String[] ss = (String[]) value;
            ls = new long[ss.length];
            for (int i = 0; i < ls.length; i++) {
                String s = ss[i];
                ls[i] = (s != null && !s.isEmpty()) ? Builder.parseIS(s) : Long.MIN_VALUE;
            }
        }
        values[index] = ls;
        return ls;
    }

    private VR updateVR(int index, VR vr) {
        VR prev = vrs[index];
        if (vr == null || vr == prev)
            return prev;

        Object value = values[index];
        if (!(value == Value.NULL || value instanceof byte[]
                || vr.isStringType() && (value instanceof String || value instanceof String[])))
            throw new IllegalStateException("value instanceof " + value.getClass());

        return vrs[index] = vr;
    }

    public boolean contains(int tag) {
        return indexOf(tag) >= 0;
    }

    public boolean contains(String privateCreator, int tag) {
        return indexOf(privateCreator, tag) >= 0;
    }

    public boolean containsValue(int tag) {
        return containsValue(null, tag);
    }

    public boolean containsValue(String privateCreator, int tag) {
        int index = indexOf(privateCreator, tag);
        return index >= 0 && !isEmpty(vrs[index].isStringType() ? decodeStringValue(index) : values[index]);
    }

    /**
     * Test whether at least one tag within the given range is contained.
     *
     * @param firstTag first tag (inclusive)
     * @param lastTag  last tag (inclusive)
     * @return whether at least one tag within the given range is contained
     */
    public boolean containsTagInRange(int firstTag, int lastTag) {
        final int indexFirstTag = indexForInsertOf(firstTag);
        if (indexFirstTag >= 0) {
            return true;
        }

        int insertIndex = -indexFirstTag - 1;
        return insertIndex < size && tags[insertIndex] <= lastTag;
    }

    public String privateCreatorOf(int tag) {
        return Tag.isPrivateTag(tag) ? privateCreatorAt(indexOf(Tag.creatorTagOf(tag))) : null;
    }

    private String privateCreatorAt(int index) {
        Object value;
        return (index < 0 || !vrs[index].isStringType() || (value = decodeStringValue(index)) == Value.NULL) ? null
                : VR.LO.toString(value, false, 0, null);
    }

    public Object getValue(int tag) {
        return getValue(null, tag, null);
    }

    public Object getValue(int tag, VR.Holder vr) {
        return getValue(null, tag, vr);
    }

    public Object getValue(String privateCreator, int tag) {
        return getValue(privateCreator, tag, null);
    }

    public Object getValue(String privateCreator, int tag, VR.Holder vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        if (vr != null)
            vr.vr = vrs[index];
        return values[index];
    }

    public VR getVR(int tag) {
        return getVR(null, tag);
    }

    public VR getVR(String privateCreator, int tag) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        return vrs[index];
    }

    public Sequence getSequence(int tag) {
        return getSequence(null, tag);
    }

    public Sequence getSequence(String privateCreator, int tag) {
        int sqtag = tag;
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, false);
            if (creatorTag == -1)
                return null;
            sqtag = Tag.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(sqtag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr != VR.SQ && vr != VR.UN)
            return null;

        Object value = values[index];
        if (value instanceof Sequence)
            return (Sequence) value;

        if (value == Value.NULL) {
            vrs[index] = VR.SQ;
            values[index] = new Sequence(this, privateCreator, tag, 0);
        } else {
            try {
                ImageInputStream.parseUNSequence((byte[]) value, this, sqtag);
            } catch (IOException e) {
                return null;
            }
        }
        return (Sequence) values[index];
    }

    public byte[] getBytes(int tag) throws IOException {
        return getBytes(null, tag);
    }

    public byte[] getBytes(String privateCreator, int tag) throws IOException {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        VR vr = vrs[index];

        try {
            if (value instanceof Value)
                return ((Value) value).toBytes(vr, bigEndian);

            return vr.toBytes(value, getSpecificCharacterSet(vr));
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as bytes", Tag.toString(tag), vr);
            return null;
        }
    }

    public byte[] getSafeBytes(int tag) {
        return getSafeBytes(null, tag);
    }

    public byte[] getSafeBytes(String privateCreator, int tag) {
        try {
            return getBytes(privateCreator, tag);
        } catch (IOException e) {
            Logger.info("Access " + Tag.toString(tag) + " throws i/o exception", e);
            return null;
        }
    }

    public String getString(int tag) {
        return getString(null, tag, null, 0, null);
    }

    public String getString(int tag, String defVal) {
        return getString(null, tag, null, 0, defVal);
    }

    public String getString(int tag, int valueIndex) {
        return getString(null, tag, null, valueIndex, null);
    }

    public String getString(int tag, int valueIndex, String defVal) {
        return getString(null, tag, null, valueIndex, defVal);
    }

    public String getString(String privateCreator, int tag) {
        return getString(privateCreator, tag, null, 0, null);
    }

    public String getString(String privateCreator, int tag, String defVal) {
        return getString(privateCreator, tag, null, 0, defVal);
    }

    public String getString(String privateCreator, int tag, VR vr) {
        return getString(privateCreator, tag, vr, 0, null);
    }

    public String getString(String privateCreator, int tag, VR vr, String defVal) {
        return getString(privateCreator, tag, vr, 0, defVal);
    }

    public String getString(String privateCreator, int tag, int valueIndex) {
        return getString(privateCreator, tag, null, valueIndex, null);
    }

    public String getString(String privateCreator, int tag, int valueIndex, String defVal) {
        return getString(privateCreator, tag, null, valueIndex, defVal);
    }

    public String getString(String privateCreator, int tag, VR vr, int valueIndex) {
        return getString(privateCreator, tag, vr, valueIndex, null);
    }

    public String getString(String privateCreator, int tag, VR vr, int valueIndex, String defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == Value.NULL)
            return defVal;

        vr = updateVR(index, vr);

        value = loadBulkData(vr, value);
        if (vr.isStringType()) {
            value = decodeStringValue(index, value);
        }
        if (value == Value.NULL)
            return defVal;
        try {
            return vr.toString(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as string", Tag.toString(tag), vr);
            return defVal;
        }
    }

    public String[] getStrings(int tag) {
        return getStrings(null, tag, null);
    }

    public String[] getStrings(String privateCreator, int tag) {
        return getStrings(privateCreator, tag, null);
    }

    public String[] getStrings(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == Value.NULL)
            return Normal.EMPTY_STRING_ARRAY;

        vr = updateVR(index, vr);

        value = loadBulkData(vr, value);
        if (vr.isStringType()) {
            value = decodeStringValue(index, value);
        }
        if (value == Value.NULL)
            return Normal.EMPTY_STRING_ARRAY;
        try {
            return toStrings(vr.toStrings(value, bigEndian, getSpecificCharacterSet(vr)));
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as string", Tag.toString(tag), vr);
            return null;
        }
    }

    public int getInt(int tag, int defVal) {
        return getInt(null, tag, null, 0, defVal);
    }

    public int getInt(int tag, int valueIndex, int defVal) {
        return getInt(null, tag, null, valueIndex, defVal);
    }

    public int getInt(String privateCreator, int tag, int defVal) {
        return getInt(privateCreator, tag, null, 0, defVal);
    }

    public int getInt(String privateCreator, int tag, VR vr, int defVal) {
        return getInt(privateCreator, tag, vr, 0, defVal);
    }

    public int getInt(String privateCreator, int tag, int valueIndex, int defVal) {
        return getInt(privateCreator, tag, null, valueIndex, defVal);
    }

    public int getInt(String privateCreator, int tag, VR vr, int valueIndex, int defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == Value.NULL)
            return defVal;

        vr = updateVR(index, vr);

        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.IS)
                value = decodeISValue(index);

            return vr.toInt(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as int", Tag.toString(tag), vr);
            return defVal;
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    public int[] getInts(int tag) {
        return getInts(null, tag, null);
    }

    public int[] getInts(String privateCreator, int tag) {
        return getInts(privateCreator, tag, null);
    }

    public int[] getInts(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == Value.NULL)
            return new int[] {};

        vr = updateVR(index, vr);

        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.IS)
                value = decodeISValue(index);

            return vr.toInts(value, bigEndian);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as int", Tag.toString(tag), vr);
            return null;
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    public long getLong(int tag, long defVal) {
        return getLong(null, tag, null, 0, defVal);
    }

    public long getLong(int tag, int valueIndex, long defVal) {
        return getLong(null, tag, null, valueIndex, defVal);
    }

    public long getLong(String privateCreator, int tag, long defVal) {
        return getLong(privateCreator, tag, null, 0, defVal);
    }

    public long getLong(String privateCreator, int tag, VR vr, long defVal) {
        return getLong(privateCreator, tag, vr, 0, defVal);
    }

    public long getLong(String privateCreator, int tag, int valueIndex, long defVal) {
        return getLong(privateCreator, tag, null, valueIndex, defVal);
    }

    public long getLong(String privateCreator, int tag, VR vr, int valueIndex, long defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == Value.NULL)
            return defVal;

        vr = updateVR(index, vr);

        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.IS)
                value = decodeISValue(index);

            return vr.toLong(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as int", Tag.toString(tag), vr);
            return defVal;
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    public long[] getLongs(int tag) {
        return getLongs(null, tag, null);
    }

    public long[] getLongs(String privateCreator, int tag) {
        return getLongs(privateCreator, tag, null);
    }

    public long[] getLongs(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == Value.NULL)
            return new long[] {};

        vr = updateVR(index, vr);

        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.IS)
                value = decodeISValue(index);

            return vr.toLongs(value, bigEndian);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as long", Tag.toString(tag), vr);
            return null;
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    public float getFloat(int tag, float defVal) {
        return getFloat(null, tag, null, 0, defVal);
    }

    public float getFloat(int tag, int valueIndex, float defVal) {
        return getFloat(null, tag, null, valueIndex, defVal);
    }

    public float getFloat(String privateCreator, int tag, float defVal) {
        return getFloat(privateCreator, tag, null, 0, defVal);
    }

    public float getFloat(String privateCreator, int tag, VR vr, float defVal) {
        return getFloat(privateCreator, tag, vr, 0, defVal);
    }

    public float getFloat(String privateCreator, int tag, int valueIndex, float defVal) {
        return getFloat(privateCreator, tag, null, valueIndex, defVal);
    }

    public float getFloat(String privateCreator, int tag, VR vr, int valueIndex, float defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == Value.NULL)
            return defVal;

        vr = updateVR(index, vr);

        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.DS)
                value = decodeDSValue(index);

            return vr.toFloat(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as float", Tag.toString(tag), vr);
            return defVal;
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    public float[] getFloats(int tag) {
        return getFloats(null, tag, null);
    }

    public float[] getFloats(String privateCreator, int tag) {
        return getFloats(privateCreator, tag, null);
    }

    public float[] getFloats(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == Value.NULL)
            return new float[] {};

        vr = updateVR(index, vr);

        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.DS)
                value = decodeDSValue(index);

            return vr.toFloats(value, bigEndian);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as float", Tag.toString(tag), vr);
            return null;
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    public double getDouble(int tag, double defVal) {
        return getDouble(null, tag, null, 0, defVal);
    }

    public double getDouble(int tag, int valueIndex, double defVal) {
        return getDouble(null, tag, null, valueIndex, defVal);
    }

    public double getDouble(String privateCreator, int tag, double defVal) {
        return getDouble(privateCreator, tag, null, 0, defVal);
    }

    public double getDouble(String privateCreator, int tag, VR vr, double defVal) {
        return getDouble(privateCreator, tag, vr, 0, defVal);
    }

    public double getDouble(String privateCreator, int tag, int valueIndex, double defVal) {
        return getDouble(privateCreator, tag, null, valueIndex, defVal);
    }

    public double getDouble(String privateCreator, int tag, VR vr, int valueIndex, double defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == Value.NULL)
            return defVal;

        vr = updateVR(index, vr);

        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.DS)
                value = decodeDSValue(index);

            return vr.toDouble(value, bigEndian, valueIndex, defVal);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as double", Tag.toString(tag), vr);
            return defVal;
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    public double[] getDoubles(int tag) {
        return getDoubles(null, tag, null);
    }

    public double[] getDoubles(String privateCreator, int tag) {
        return getDoubles(privateCreator, tag, null);
    }

    public double[] getDoubles(String privateCreator, int tag, VR vr) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == Value.NULL)
            return new double[] {};

        vr = updateVR(index, vr);

        try {
            value = loadAndStoreBulkData(index);
            if (vr == VR.DS)
                value = decodeDSValue(index);

            return vr.toDoubles(value, bigEndian);
        } catch (UnsupportedOperationException e) {
            Logger.info("Attempt to access {} {} as double", Tag.toString(tag), vr);
            return null;
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    /**
     * Gets the most accurate temporal type for the given tag.
     * <p>
     * An instance of {@link ZonedDateTime} will be returned for:
     * <ul>
     * <li>A tag with {@link VR#DT} which has a timezone offset defined within its value.</li>
     * <li>A tag with {@link VR#DT} without a timezone offset within its value, but a {@link Tag#TimezoneOffsetFromUTC}
     * is defined within this or any parent Attributes, or a default TimeZone (see
     * {@link #setDefaultTimeZone(TimeZone)}) has been set for this or any parent.</li>
     * <li>A tag with {@link VR#DA} or {@link VR#TM}, in case the corresponding {@link VR#TM} or {@link VR#DA} tag value
     * is also available (e.g. given {@link Tag#StudyDate}, also {@link Tag#StudyTime} is available), and
     * {@link Tag#TimezoneOffsetFromUTC} or a default TimeZone is available.</li>
     * </ul>
     *
     * If no timezone information is available, then an instance of {@link LocalDateTime} will be returned for
     * {@link VR#DT} tags, and for {@link VR#DA} or {@link VR#TM} tags if the corresponding {@link VR#TM} or
     * {@link VR#DA} tag value is also available.
     *
     * If the corresponding {@link VR#TM} or {@link VR#DA} tag value is not available then an instance of
     * {@link LocalDate} (for VR#DA tags) or {@link LocalTime} (for VR#TM tags) will be returned.
     *
     * In case the value for the given tag itself is not set (or empty), then <code>null</code> (or the supplied
     * <code>defVal</code> for other variants of this method) will be returned.
     *
     * @param tag tag number
     * @return an instance of {@link ZonedDateTime}, {@link LocalDateTime}, {@link LocalDate} or {@link LocalTime}, or
     *         null
     */
    public Temporal getTemporal(int tag) {
        return getTemporal(null, tag, null, 0, null, new DatePrecision());
    }

    /**
     * See {@link #getTemporal(int)}.
     *
     * @param privateCreator private creator
     * @param tag            tag number
     * @param vr             VR
     * @param valueIndex     value index
     * @param defVal         default value, if the tag value is not set or empty
     * @param precision      used as a return value: contains information about the contained date/time precision and
     *                       whether the tag value itself contained timezone information (only for {@link VR#DT} tags).
     * @return an instance of {@link ZonedDateTime}, {@link LocalDateTime}, {@link LocalDate} or {@link LocalTime}, or
     *         defVal
     */
    public Temporal getTemporal(String privateCreator, int tag, VR vr, int valueIndex, Temporal defVal,
            DatePrecision precision) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == Value.NULL)
            return defVal;

        vr = updateVR(index, vr);

        if (!vr.isTemporalType()) {
            Logger.info("Attempt to access {} {} as date/time", Tag.toString(tag), vr);
            return defVal;
        }

        value = decodeStringValue(index);
        if (value == Value.NULL) {
            return defVal;
        }

        Temporal t;
        try {
            t = vr.toTemporal(value, valueIndex, precision);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }

        if (t == null) {
            return defVal;
        } else if (t instanceof OffsetDateTime) {
            return ((OffsetDateTime) t).toZonedDateTime();
        } else if (t instanceof LocalDateTime) {
            ZoneId zoneId = getZoneId();
            if (zoneId != null) {
                return ((LocalDateTime) t).atZone(zoneId);
            }
        }

        return t;
    }

    private LocalTime getLocalTime(String privateCreator, int tag, VR vr, int valueIndex, DatePrecision precision) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == Value.NULL)
            return null;

        vr = updateVR(index, vr);

        if (vr != VR.TM) {
            Logger.info("Attempt to access {} {} as local time", Tag.toString(tag), vr);
            return null;
        }

        value = decodeStringValue(index);
        if (value == Value.NULL) {
            return null;
        }

        try {
            return (LocalTime) vr.toTemporal(value, valueIndex, precision);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return null;
        }
    }

    public Date getDate(int tag) {
        return getDate(null, tag, null, 0, null, new DatePrecision());
    }

    public Date getDate(int tag, DatePrecision precision) {
        return getDate(null, tag, null, 0, null, precision);
    }

    public Date getDate(int tag, Date defVal) {
        return getDate(null, tag, null, 0, defVal, new DatePrecision());
    }

    public Date getDate(int tag, Date defVal, DatePrecision precision) {
        return getDate(null, tag, null, 0, defVal, precision);
    }

    public Date getDate(int tag, int valueIndex) {
        return getDate(null, tag, null, valueIndex, null, new DatePrecision());
    }

    public Date getDate(int tag, int valueIndex, DatePrecision precision) {
        return getDate(null, tag, null, valueIndex, null, precision);
    }

    public Date getDate(int tag, int valueIndex, Date defVal) {
        return getDate(null, tag, null, valueIndex, defVal, new DatePrecision());
    }

    public Date getDate(int tag, int valueIndex, Date defVal, DatePrecision precision) {
        return getDate(null, tag, null, valueIndex, defVal, precision);
    }

    public Date getDate(String privateCreator, int tag) {
        return getDate(privateCreator, tag, null, 0, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, DatePrecision precision) {
        return getDate(privateCreator, tag, null, 0, null, precision);
    }

    public Date getDate(String privateCreator, int tag, Date defVal, DatePrecision precision) {
        return getDate(privateCreator, tag, null, 0, defVal, precision);
    }

    public Date getDate(String privateCreator, int tag, VR vr) {
        return getDate(privateCreator, tag, vr, 0, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, VR vr, DatePrecision precision) {
        return getDate(privateCreator, tag, vr, 0, null, precision);
    }

    public Date getDate(String privateCreator, int tag, VR vr, Date defVal) {
        return getDate(privateCreator, tag, vr, 0, defVal, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, VR vr, Date defVal, DatePrecision precision) {
        return getDate(privateCreator, tag, vr, 0, defVal, precision);
    }

    public Date getDate(String privateCreator, int tag, int valueIndex) {
        return getDate(privateCreator, tag, null, valueIndex, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, int valueIndex, DatePrecision precision) {
        return getDate(privateCreator, tag, null, valueIndex, null, precision);
    }

    public Date getDate(String privateCreator, int tag, int valueIndex, Date defVal) {
        return getDate(privateCreator, tag, null, valueIndex, defVal, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, int valueIndex, Date defVal, DatePrecision precision) {
        return getDate(privateCreator, tag, null, valueIndex, defVal, precision);
    }

    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex) {
        return getDate(privateCreator, tag, vr, valueIndex, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, DatePrecision precision) {
        return getDate(privateCreator, tag, vr, valueIndex, null, precision);
    }

    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, Date defVal) {
        return getDate(privateCreator, tag, vr, valueIndex, defVal, new DatePrecision());
    }

    public Date getDate(String privateCreator, int tag, VR vr, int valueIndex, Date defVal, DatePrecision precision) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == Value.NULL)
            return defVal;

        vr = updateVR(index, vr);
        if (!vr.isTemporalType()) {
            Logger.info("Attempt to access {} {} as date", Tag.toString(tag), vr);
            return defVal;
        }
        value = decodeStringValue(index);
        if (value == Value.NULL)
            return defVal;

        try {
            return vr.toDate(value, getTimeZone(), valueIndex, false, defVal, precision);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    public Date getDate(long tag) {
        return getDate(null, tag, null, new DatePrecision());
    }

    public Date getDate(long tag, DatePrecision precision) {
        return getDate(null, tag, null, precision);
    }

    public Date getDate(long tag, Date defVal) {
        return getDate(null, tag, defVal, new DatePrecision());
    }

    public Date getDate(long tag, Date defVal, DatePrecision precision) {
        return getDate(null, tag, defVal, precision);
    }

    public Date getDate(String privateCreator, long tag) {
        return getDate(privateCreator, tag, null, new DatePrecision());
    }

    public Date getDate(String privateCreator, long tag, DatePrecision precision) {
        return getDate(privateCreator, tag, null, precision);
    }

    public Date getDate(String privateCreator, long tag, Date defVal) {
        return getDate(privateCreator, tag, defVal, new DatePrecision());
    }

    public Date getDate(String privateCreator, long tag, Date defVal, DatePrecision precision) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;

        String tm = getString(privateCreator, tmTag, VR.TM, null);
        if (tm == null)
            return getDate(daTag, defVal, precision);

        String da = getString(privateCreator, daTag, VR.DA, null);
        if (da == null)
            return defVal;
        try {
            return VR.DT.toDate(da + tm, getTimeZone(), 0, false, null, precision);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} DA or {} TM", Tag.toString(daTag), Tag.toString(tmTag));
            return defVal;
        }
    }

    public Date[] getDates(int tag) {
        return getDates(null, tag, null, new DatePrecision());
    }

    public Date[] getDates(int tag, DatePrecision precisions) {
        return getDates(null, tag, null, precisions);
    }

    public Date[] getDates(String privateCreator, int tag) {
        return getDates(privateCreator, tag, null, new DatePrecision());
    }

    public Date[] getDates(String privateCreator, int tag, DatePrecision precision) {
        return getDates(privateCreator, tag, null, precision);
    }

    public Date[] getDates(String privateCreator, int tag, VR vr) {
        return getDates(privateCreator, tag, vr, new DatePrecision());
    }

    public Date[] getDates(String privateCreator, int tag, VR vr, DatePrecision precisions) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == Value.NULL)
            return Normal.EMPTY_DATE_OBJECT_ARRAY;

        vr = updateVR(index, vr);
        if (!vr.isTemporalType()) {
            Logger.info("Attempt to access {} {} as date", Tag.toString(tag), vr);
            return Normal.EMPTY_DATE_OBJECT_ARRAY;
        }
        value = decodeStringValue(index);
        if (value == Value.NULL)
            return Normal.EMPTY_DATE_OBJECT_ARRAY;

        try {
            return vr.toDates(value, getTimeZone(), false, precisions);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return Normal.EMPTY_DATE_OBJECT_ARRAY;
        }
    }

    public Date[] getDates(long tag) {
        return getDates(null, tag, new DatePrecision());
    }

    public Date[] getDates(long tag, DatePrecision precisions) {
        return getDates(null, tag, precisions);
    }

    public Date[] getDates(String privateCreator, long tag) {
        return getDates(privateCreator, tag, new DatePrecision());
    }

    public Date[] getDates(String privateCreator, long tag, DatePrecision precision) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;

        String[] tm = getStrings(privateCreator, tmTag);
        if (tm == null || tm.length == 0)
            return getDates(daTag, precision);

        String[] da = getStrings(privateCreator, daTag);
        if (da == null || da.length == 0)
            return Normal.EMPTY_DATE_OBJECT_ARRAY;

        Date[] dates = new Date[da.length];
        precision.precisions = new DatePrecision[da.length];
        int i = 0;
        try {
            TimeZone tz = getTimeZone();
            while (i < tm.length)
                dates[i++] = VR.DT.toDate(da[i] + tm[i], tz, 0, false, null,
                        precision.precisions[i] = new DatePrecision());
            while (i < da.length)
                dates[i++] = VR.DA.toDate(da[i], tz, 0, false, null, precision.precisions[i] = new DatePrecision());
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} DA or {} TM", Tag.toString(daTag), Tag.toString(tmTag));
            dates = Arrays.copyOf(dates, i);
        }
        return dates;
    }

    public DateRange getDateRange(int tag) {
        return getDateRange(null, tag, null, null);
    }

    public DateRange getDateRange(int tag, DateRange defVal) {
        return getDateRange(null, tag, null, defVal);
    }

    public DateRange getDateRange(String privateCreator, int tag) {
        return getDateRange(privateCreator, tag, null, null);
    }

    public DateRange getDateRange(String privateCreator, int tag, DateRange defVal) {
        return getDateRange(privateCreator, tag, null, defVal);
    }

    public DateRange getDateRange(String privateCreator, int tag, VR vr) {
        return getDateRange(privateCreator, tag, vr, null);
    }

    public DateRange getDateRange(String privateCreator, int tag, VR vr, DateRange defVal) {
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == Value.NULL)
            return defVal;

        vr = updateVR(index, vr);
        if (!vr.isTemporalType()) {
            Logger.info("Attempt to access {} {} as date", Tag.toString(tag), vr);
            return defVal;
        }
        value = decodeStringValue(index);
        if (value == Value.NULL)
            return defVal;

        try {
            return toDateRange((value instanceof String) ? (String) value : ((String[]) value)[0], vr);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} {}", Tag.toString(tag), vr);
            return defVal;
        }
    }

    private DateRange toDateRange(String s, VR vr) {
        String[] range = splitRange(s);
        TimeZone tz = getTimeZone();
        DatePrecision precision = new DatePrecision();
        Date start = range[0] == null ? null : vr.toDate(range[0], tz, 0, false, null, precision);
        Date end = range[1] == null ? null : vr.toDate(range[1], tz, 0, true, null, precision);
        return new DateRange(start, end);
    }

    public DateRange getDateRange(long tag) {
        return getDateRange(null, tag, null);
    }

    public DateRange getDateRange(long tag, DateRange defVal) {
        return getDateRange(null, tag, defVal);
    }

    public DateRange getDateRange(String privateCreator, long tag) {
        return getDateRange(privateCreator, tag, null);
    }

    public DateRange getDateRange(String privateCreator, long tag, DateRange defVal) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;

        String tm = getString(privateCreator, tmTag, VR.TM, null);
        if (tm == null)
            return getDateRange(daTag, defVal);

        String da = getString(privateCreator, daTag, VR.DA, null);
        if (da == null)
            return defVal;

        try {
            return toDateRange(da, tm);
        } catch (IllegalArgumentException e) {
            Logger.info("Invalid value of {} TM", Tag.toString((int) tag));
            return defVal;
        }
    }

    private DateRange toDateRange(String da, String tm) {
        String[] darange = splitRange(da);
        String[] tmrange = splitRange(tm);
        DatePrecision precision = new DatePrecision();
        return new DateRange(
                darange[0] == null ? null
                        : VR.DT.toDate(tmrange[0] == null ? darange[0] : darange[0] + tmrange[0], tz, 0, false, null,
                                precision),
                darange[1] == null ? null
                        : VR.DT.toDate(tmrange[1] == null ? darange[1] : darange[1] + tmrange[1], tz, 0, true, null,
                                precision));
    }

    public SpecificCharacterSet getSpecificCharacterSet() {
        if (cs != null)
            return cs;

        if (containsSpecificCharacterSet)
            cs = SpecificCharacterSet.valueOf(getStrings(null, Tag.SpecificCharacterSet, VR.CS));
        else if (parent != null)
            return parent.getSpecificCharacterSet();
        else
            cs = SpecificCharacterSet.getDefaultCharacterSet();

        return cs;
    }

    /**
     * Set Specific Character Set (0008,0005) to specified code(s) and re-encode contained LO, LT, PN, SH, ST, UT
     * attributes accordingly.
     *
     * @param codes new value(s) of Specific Character Set (0008,0005)
     */
    public void setSpecificCharacterSet(String... codes) {
        ensureModifiable();
        decodeStringValuesUsingSpecificCharacterSet();
        setString(Tag.SpecificCharacterSet, VR.CS, codes);
    }

    public boolean containsTimezoneOffsetFromUTC() {
        return containsTimezoneOffsetFromUTC;
    }

    public TimeZone getDefaultTimeZone() {
        if (defaultTimeZone != null)
            return defaultTimeZone;

        if (parent != null)
            return parent.getDefaultTimeZone();

        return TimeZone.getDefault();
    }

    public void setDefaultTimeZone(TimeZone tz) {
        ensureModifiable();
        defaultTimeZone = tz;
    }

    public TimeZone getTimeZone() {
        if (tz != null)
            return tz;

        if (containsTimezoneOffsetFromUTC) {
            String s = getString(Tag.TimezoneOffsetFromUTC);
            if (s != null)
                try {
                    tz = Format.timeZone(s);
                } catch (IllegalArgumentException e) {
                    Logger.info(e.getMessage());
                }
        } else if (parent != null)
            return parent.getTimeZone();
        else
            tz = getDefaultTimeZone();

        return tz;
    }

    public ZoneId getDefaultZoneId() {
        if (defaultTimeZone != null)
            return defaultTimeZone.toZoneId();

        if (parent != null)
            return parent.getDefaultZoneId();

        return null;
    }

    public ZoneId getZoneId() {
        if (tz != null)
            return tz.toZoneId();

        if (containsTimezoneOffsetFromUTC) {
            String s = getString(Tag.TimezoneOffsetFromUTC);
            if (s == null) {
                return null;
            }
            try {
                tz = Format.timeZone(s);
            } catch (IllegalArgumentException e) {
                Logger.info(e.getMessage());
                return null;
            }
            return tz.toZoneId();
        } else if (parent != null) {
            return parent.getZoneId();
        } else {
            return getDefaultZoneId();
        }
    }

    /**
     * Set Timezone Offset From UTC (0008,0201) to specified value and adjust contained DA, DT and TM attributs
     * accordingly
     *
     * @param utcOffset offset from UTC as (+|-)HHMM
     */
    public void setTimezoneOffsetFromUTC(String utcOffset) {
        ensureModifiable();
        TimeZone tz = Format.timeZone(utcOffset);
        updateTimezone(getTimeZone(), tz);
        setString(Tag.TimezoneOffsetFromUTC, VR.SH, utcOffset);
        this.tz = tz;
    }

    /**
     * Set the Default Time Zone to specified value and adjust contained DA, DT and TM attributs accordingly. If the
     * Time Zone does not use Daylight Saving Time, attribute Timezone Offset From UTC (0008,0201) will be also set
     * accordingly. If the Time zone uses Daylight Saving Time, a previous existing attribute Timezone Offset From UTC
     * (0008,0201) will be removed.
     *
     * @param tz Time Zone
     * @see #setDefaultTimeZone(TimeZone)
     * @see #setTimezoneOffsetFromUTC(String)
     */
    public void setTimezone(TimeZone tz) {
        ensureModifiable();
        updateTimezone(getTimeZone(), tz);
        if (tz.useDaylightTime()) {
            remove(Tag.TimezoneOffsetFromUTC);
            setDefaultTimeZone(tz);
        } else {
            setString(Tag.TimezoneOffsetFromUTC, VR.SH, Format.formatTimezoneOffsetFromUTC(tz));
        }
        this.tz = tz;
    }

    private void updateTimezone(TimeZone from, TimeZone to) {
        if (from.hasSameRules(to))
            return;

        for (int i = 0; i < size; i++) {
            Object val = values[i];
            if (val instanceof Sequence new_name) {
                for (Attributes item : new_name) {
                    item.updateTimezone(item.getTimeZone(), to);
                    item.remove(Tag.TimezoneOffsetFromUTC);
                }
            } else if (vrs[i] == VR.TM && tags[i] != Tag.PatientBirthTime
                    || vrs[i] == VR.DT && tags[i] != Tag.ContextGroupVersion && tags[i] != Tag.ContextGroupLocalVersion)
                updateTimezone(from, to, i);
        }
    }

    private void updateTimezone(TimeZone from, TimeZone to, int tmIndex) {
        Object tm = decodeStringValue(tmIndex);
        if (tm == Value.NULL)
            return;

        int tmTag = tags[tmIndex];
        if (vrs[tmIndex] == VR.DT) {
            if (tm instanceof String[] tms) {
                for (int i = 0; i < tms.length; i++) {
                    tms[i] = updateTimeZoneDT(from, to, tms[i]);
                }
            } else
                values[tmIndex] = updateTimeZoneDT(from, to, (String) tm);
        } else {
            int daTag = ElementDictionary.getElementDictionary(privateCreatorOf(tmTag)).daTagOf(tmTag);
            int daIndex = daTag != 0 ? indexOf(daTag) : -1;
            Object da = daIndex >= 0 ? decodeStringValue(daIndex) : Value.NULL;

            if (tm instanceof String[] tms) {
                if (da instanceof String[] das) {
                    for (int i = 0; i < tms.length; i++) {
                        if (i < das.length) {
                            String dt = updateTimeZoneDT(from, to, das[i] + tms[i]);
                            das[i] = dt.substring(0, 8);
                            tms[i] = dt.substring(8);
                        } else {
                            tms[i] = updateTimeZoneTM(from, to, tms[i]);
                        }
                    }
                } else {
                    if (da == Value.NULL) {
                        tms[0] = updateTimeZoneTM(from, to, tms[0]);
                    } else {
                        String dt = updateTimeZoneDT(from, to, da + tms[0]);
                        values[daIndex] = dt.substring(0, 8);
                        tms[0] = dt.substring(8);
                    }
                    for (int i = 1; i < tms.length; i++) {
                        tms[i] = updateTimeZoneTM(from, to, tms[i]);
                    }
                }
            } else {
                if (da instanceof String[] das) {
                    String dt = updateTimeZoneDT(from, to, das[0] + tm);
                    das[0] = dt.substring(0, 8);
                    values[tmIndex] = dt.substring(8);
                } else {
                    String[] tmRange = null;
                    if (isRange((String) tm)) {
                        tmRange = splitRange((String) tm);
                        if (tmRange[0] == null)
                            tmRange[0] = "000000.000";
                        if (tmRange[1] == null)
                            tmRange[1] = "235959.999";
                    }
                    if (da == Value.NULL) {
                        if (tmRange != null) {
                            tmRange[0] = updateTimeZoneTM(from, to, tmRange[0]);
                            tmRange[1] = updateTimeZoneTM(from, to, tmRange[1]);
                            values[tmIndex] = toDateRangeString(tmRange[0], tmRange[1]);
                        } else {
                            values[tmIndex] = updateTimeZoneTM(from, to, (String) tm);
                        }
                    } else {
                        if (tmRange != null) {
                            String[] daRange = splitRange((String) da);
                            if (daRange[0] == null) {
                                daRange[0] = "";
                                tmRange[0] = updateTimeZoneTM(from, to, tmRange[0]);
                            } else {
                                String dt = updateTimeZoneDT(from, to, daRange[0] + tmRange[0]);
                                daRange[0] = dt.substring(0, 8);
                                tmRange[0] = dt.substring(8);
                            }
                            if (daRange[1] == null) {
                                daRange[1] = "";
                                tmRange[1] = updateTimeZoneTM(from, to, tmRange[1]);
                            } else {
                                String dt = updateTimeZoneDT(from, to, daRange[1] + tmRange[1]);
                                daRange[1] = dt.substring(0, 8);
                                tmRange[1] = dt.substring(8);
                            }
                            values[daIndex] = toDateRangeString(daRange[0], daRange[1]);
                            values[tmIndex] = toDateRangeString(tmRange[0], tmRange[1]);
                        } else {
                            String dt = updateTimeZoneDT(from, to, da + (String) tm);
                            values[daIndex] = dt.substring(0, 8);
                            values[tmIndex] = dt.substring(8);
                        }
                    }
                }
            }
        }
    }

    private String updateTimeZoneDT(TimeZone from, TimeZone to, String dt) {
        int dtlen = dt.length();
        if (dtlen > 8) {
            char ch = dt.charAt(dtlen - 5);
            if (ch == '+' || ch == '-')
                return dt;
        }
        try {
            DatePrecision precision = new DatePrecision();
            Date date = Format.parseDT(from, dt, false, precision);
            dt = Format.formatDT(to, date, precision);
        } catch (IllegalArgumentException e) {
        }
        return dt;
    }

    private String updateTimeZoneTM(TimeZone from, TimeZone to, String tm) {
        try {
            DatePrecision precision = new DatePrecision();
            Date date = Format.parseTM(from, tm, false, precision);
            tm = Format.formatTM(to, date, precision);
        } catch (IllegalArgumentException e) {
        }
        return tm;
    }

    public String getPrivateCreator(int tag) {
        return Tag.isPrivateTag(tag) ? getString(Tag.creatorTagOf(tag), null) : null;
    }

    public Object remove(int tag) {
        return remove(null, tag);
    }

    public Object remove(String privateCreator, int tag) {
        ensureModifiable();
        int index = indexOf(privateCreator, tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value instanceof Sequence) {
            for (Attributes attrs : ((Sequence) value)) {
                attrs.setParent(null, null, 0);
            }
        }
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(tags, index + 1, tags, index, numMoved);
            System.arraycopy(vrs, index + 1, vrs, index, numMoved);
            System.arraycopy(values, index + 1, values, index, numMoved);
        }
        values[--size] = null;

        if (tag == Tag.SpecificCharacterSet) {
            containsSpecificCharacterSet = false;
            cs = null;
        } else if (tag == Tag.TimezoneOffsetFromUTC) {
            containsTimezoneOffsetFromUTC = false;
            tz = null;
        }

        return value;
    }

    public Object setNull(int tag, VR vr) {
        return setNull(null, tag, vr);
    }

    public Object setNull(String privateCreator, int tag, VR vr) {
        ensureModifiable();
        return set(privateCreator, tag, vr, Value.NULL);
    }

    public Object setBytes(int tag, VR vr, byte[] b) {
        return setBytes(null, tag, vr, b);
    }

    public Object setBytes(String privateCreator, int tag, VR vr, byte[] b) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(b));
    }

    public Object setString(int tag, VR vr, String s) {
        return setString(null, tag, vr, s);
    }

    public Object setString(String privateCreator, int tag, VR vr, String s) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(s, bigEndian));
    }

    public Object setString(int tag, VR vr, String... ss) {
        return setString(null, tag, vr, ss);
    }

    public Object setString(String privateCreator, int tag, VR vr, String... ss) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(ss, bigEndian));
    }

    public Object setInt(int tag, VR vr, int... is) {
        return setInt(null, tag, vr, is);
    }

    public Object setInt(String privateCreator, int tag, VR vr, int... is) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(is, bigEndian));
    }

    public Object setLong(int tag, VR vr, long... ls) {
        return setLong(null, tag, vr, ls);
    }

    public Object setLong(String privateCreator, int tag, VR vr, long... ls) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(ls, bigEndian));
    }

    public Object setFloat(int tag, VR vr, float... fs) {
        return setFloat(null, tag, vr, fs);
    }

    public Object setFloat(String privateCreator, int tag, VR vr, float... fs) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(fs, bigEndian));
    }

    public Object setDouble(int tag, VR vr, double... ds) {
        return setDouble(null, tag, vr, ds);
    }

    public Object setDouble(String privateCreator, int tag, VR vr, double... ds) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(ds, bigEndian));
    }

    public Object setDate(int tag, VR vr, Date... ds) {
        return setDate(null, tag, vr, ds);
    }

    public Object setDate(int tag, VR vr, DatePrecision precision, Date... ds) {
        return setDate(null, tag, vr, precision, ds);
    }

    public Object setDate(String privateCreator, int tag, VR vr, Date... ds) {
        return setDate(privateCreator, tag, vr, new DatePrecision(), ds);
    }

    public Object setDate(String privateCreator, int tag, VR vr, DatePrecision precision, Date... ds) {
        return setDate(privateCreator, tag, vr, vr == VR.DT, precision, ds);
    }

    public Object setDate(int tag, VR vr, boolean applyTimezoneOffset, Date... ds) {
        return setDate(null, tag, vr, applyTimezoneOffset, ds);
    }

    public Object setDate(int tag, VR vr, boolean applyTimezoneOffset, DatePrecision precision, Date... ds) {
        return setDate(null, tag, vr, applyTimezoneOffset, precision, ds);
    }

    public Object setDate(String privateCreator, int tag, VR vr, boolean applyTimezoneOffset, Date... ds) {
        return setDate(privateCreator, tag, vr, applyTimezoneOffset, new DatePrecision(), ds);
    }

    public Object setDate(String privateCreator, int tag, VR vr, boolean applyTimezoneOffset, DatePrecision precision,
            Date... ds) {
        ensureModifiable();
        return set(privateCreator, tag, vr, vr.toValue(ds, applyTimezoneOffset ? getTimeZone() : null, precision));
    }

    public void setDate(long tag, Date... dt) {
        setDate(null, tag, dt);
    }

    public void setDate(long tag, DatePrecision precision, Date... dt) {
        setDate(null, tag, precision, dt);
    }

    public void setDate(String privateCreator, long tag, Date... dt) {
        setDate(privateCreator, tag, new DatePrecision(), dt);
    }

    public void setDate(String privateCreator, long tag, DatePrecision precision, Date... dt) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;
        setDate(privateCreator, daTag, VR.DA, true, precision, dt);
        setDate(privateCreator, tmTag, VR.TM, true, precision, dt);
    }

    public Object setDateRange(int tag, VR vr, DateRange range) {
        return setDateRange(null, tag, vr, range);
    }

    public Object setDateRange(int tag, VR vr, DatePrecision precision, DateRange range) {
        return setDateRange(null, tag, vr, precision, range);
    }

    public Object setDateRange(String privateCreator, int tag, VR vr, DateRange range) {
        return setDateRange(privateCreator, tag, vr, new DatePrecision(), range);
    }

    public Object setDateRange(String privateCreator, int tag, VR vr, DatePrecision precision, DateRange range) {
        ensureModifiable();
        return set(privateCreator, tag, vr, toString(range, vr, getTimeZone(), precision));
    }

    public void setDateRange(long tag, DateRange dr) {
        setDateRange(null, tag, dr);
    }

    public void setDateRange(String privateCreator, long tag, DateRange range) {
        int daTag = (int) (tag >>> 32);
        int tmTag = (int) tag;
        setDateRange(privateCreator, daTag, VR.DA, range);
        setDateRange(privateCreator, tmTag, VR.TM, range);
    }

    public Object setValue(int tag, VR vr, Object value) {
        return setValue(null, tag, vr, value);
    }

    public Object setValue(String privateCreator, int tag, VR vr, Object value) {
        ensureModifiable();
        return set(privateCreator, tag, vr, value != null ? value : Value.NULL);
    }

    public Sequence newSequence(int tag, int initialCapacity) {
        return newSequence(null, tag, initialCapacity);
    }

    public Sequence newSequence(String privateCreator, int tag, int initialCapacity) {
        ensureModifiable();
        Sequence seq = new Sequence(this, privateCreator, tag, initialCapacity);
        set(privateCreator, tag, VR.SQ, seq);
        return seq;
    }

    public Sequence ensureSequence(int tag, int initialCapacity) {
        return ensureSequence(null, tag, initialCapacity);
    }

    public Sequence ensureSequence(String privateCreator, int tag, int initialCapacity) {
        ensureModifiable();
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, true);
            tag = Tag.toPrivateTag(creatorTag, tag);
        }

        Sequence seq;
        int index = indexOf(tag);
        if (index >= 0) {
            Object oldValue = values[index];
            if (oldValue instanceof Sequence)
                seq = (Sequence) oldValue;
            else
                values[index] = seq = new Sequence(this, null, 0, initialCapacity);
        } else {
            seq = new Sequence(this, null, 0, initialCapacity);
            insert(-index - 1, tag, VR.SQ, seq);
        }
        return seq;
    }

    public Fragments newFragments(int tag, VR vr, int initialCapacity) {
        return newFragments(null, tag, vr, initialCapacity);
    }

    public Fragments newFragments(String privateCreator, int tag, VR vr, int initialCapacity) {
        ensureModifiable();
        Fragments frags = new Fragments(vr, bigEndian, initialCapacity);
        set(privateCreator, tag, vr, frags);
        return frags;
    }

    private Object set(String privateCreator, int tag, VR vr, Object value) {
        if (vr == null)
            throw new NullPointerException("vr");

        if (privateCreator != null) {
            int creatorTag = creatorTagOf(privateCreator, tag, true);
            tag = Tag.toPrivateTag(creatorTag, tag);
        }

        if (Tag.isGroupLength(tag))
            return null;

        Object oldValue = set(tag, vr, value);

        if (tag == Tag.SpecificCharacterSet) {
            containsSpecificCharacterSet = true;
            cs = null;
        } else if (tag == Tag.TimezoneOffsetFromUTC) {
            containsTimezoneOffsetFromUTC = value != Value.NULL;
            tz = null;
        }

        return oldValue;
    }

    private Object set(int tag, VR vr, Object value) {
        int index = indexForInsertOf(tag);
        if (index >= 0) {
            Object oldValue = values[index];
            vrs[index] = vr;
            values[index] = value;
            return oldValue;
        }
        insert(-index - 1, tag, vr, value);
        return null;
    }

    private void insert(int index, int tag, VR vr, Object value) {
        ensureCapacity(size + 1);
        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(tags, index, tags, index + 1, numMoved);
            System.arraycopy(vrs, index, vrs, index + 1, numMoved);
            System.arraycopy(values, index, values, index + 1, numMoved);
        }
        tags[index] = tag;
        vrs[index] = vr;
        values[index] = value;
        size++;
    }

    public boolean addAll(Attributes other) {
        ensureModifiable();
        return add(other, null, null, 0, 0, null, null, false, false, null);
    }

    public boolean addAll(Attributes other, boolean mergeOriginalAttributesSequence) {
        ensureModifiable();
        return add(other, null, null, 0, 0, null, null, mergeOriginalAttributesSequence, false, null);
    }

    public boolean addSelected(Attributes other, Attributes selection) {
        ensureModifiable();
        return add(other, null, null, 0, 0, selection, null, false, false, null);
    }

    public boolean addSelected(Attributes other, String privateCreator, int tag) {
        ensureModifiable();
        int index = other.indexOf(privateCreator, tag);
        if (index < 0)
            return false;

        VR vr = other.vrs[index];
        Object value = other.values[index];
        if (!getSpecificCharacterSet().contains(other.getSpecificCharacterSet())
                && containsNonASCIIStringValues(value, vr)) {
            if (!(getSpecificCharacterSet()).isUTF8()) {
                throw new InternalException(
                        "Specific Character Sets " + Arrays.toString(getSpecificCharacterSet().toCodes()) + " and "
                                + Arrays.toString(other.getSpecificCharacterSet().toCodes()) + " not compatible");
            }
            if (vr.useSpecificCharacterSet()) {
                value = other.loadBulkData(vr, value);
                if (value instanceof byte[])
                    value = vr.toStrings(value, other.bigEndian(), other.getSpecificCharacterSet());
            }
        }
        if (value instanceof Sequence) {
            set(privateCreator, tag, (Sequence) value, null);
        } else if (value instanceof Fragments) {
            set(privateCreator, tag, (Fragments) value);
        } else {
            set(privateCreator, tag, vr, toggleEndian(vr, value, bigEndian != other.bigEndian));
        }
        return true;
    }

    /**
     * Add selected attributes from another Attributes object to this. The specified array of tag values must be sorted
     * (as by the {@link Arrays#sort(int[])} method) prior to making this call.
     *
     * @param other     the other Attributes object
     * @param selection sorted tag values
     * @return true if one ore more attributes were added
     */
    public boolean addSelected(Attributes other, int... selection) {
        return addSelected(other, selection, 0, selection.length);
    }

    /**
     * Add selected attributes from another Attributes object to this. The specified array of tag values must be sorted
     * (as by the {@link Arrays#sort(int[], int, int)} method) prior to making this call.
     *
     * @param other     the other Attributes object
     * @param selection sorted tag values
     * @param fromIndex the index of the first tag (inclusive)
     * @param toIndex   the index of the last tag (exclusive)
     * @return true if one ore more attributes were added
     */
    public boolean addSelected(Attributes other, int[] selection, int fromIndex, int toIndex) {
        ensureModifiable();
        return add(other, selection, null, fromIndex, toIndex, null, null, false, false, null);
    }

    /**
     * Add not selected attributes from another Attributes object to this. The specified array of tag values must be
     * sorted (as by the {@link Arrays#sort(int[])} method) prior to making this call.
     *
     * @param other     the other Attributes object
     * @param selection sorted tag values
     * @return true if one ore more attributes were added
     */
    public boolean addNotSelected(Attributes other, int... selection) {
        return addNotSelected(other, selection, 0, selection.length);
    }

    /**
     * Add not selected attributes from another Attributes object to this. The specified array of tag values must be
     * sorted (as by the {@link Arrays#sort(int[])} method) prior to making this call.
     *
     * @param other     the other Attributes object
     * @param selection sorted tag values
     * @param fromIndex the index of the first tag (inclusive)
     * @param toIndex   the index of the last tag (exclusive)
     * @return true if one ore more attributes were added
     */
    public boolean addNotSelected(Attributes other, int[] selection, int fromIndex, int toIndex) {
        ensureModifiable();
        return add(other, null, selection, fromIndex, toIndex, null, null, false, false, null);
    }

    public void supplementEmpty(Attributes selection) {
        ensureModifiable();
        final int[] otherTags = selection.tags;
        final VR[] otherVRs = selection.vrs;
        final Object[] otherValues = selection.values;
        for (int i = 0; i < selection.size; i++) {
            int index = indexOf(otherTags[i]);
            if (index < 0) {
                insert(-index - 1, otherTags[i], otherVRs[i], Value.NULL);
            } else if (otherValues[i] instanceof Sequence otherSeq && values[index] instanceof Sequence) {
                Attributes otherItem;
                if (!otherSeq.isEmpty() && !(otherItem = otherSeq.get(0)).isEmpty()) {
                    for (Attributes item : (Sequence) values[index]) {
                        item.supplementEmpty(otherItem);
                    }
                }
            }
        }
    }

    private boolean add(Attributes other, int[] include, int[] exclude, int fromIndex, int toIndex,
            Attributes selection, UpdatePolicy updatePolicy, boolean mergeOriginalAttributesSequence, boolean simulate,
            Attributes modified) {
        if (updatePolicy == UpdatePolicy.REPLACE)
            throw new IllegalArgumentException("updatePolicy:" + updatePolicy);

        boolean decodeStringValue = false;
        if (updatePolicy != UpdatePolicy.PRESERVE && !isEmpty()) {
            boolean updateSpecificCharacterSet = isUpdateSpecificCharacterSet(other, include, exclude, fromIndex,
                    toIndex, selection, updatePolicy);
            if (!(updateSpecificCharacterSet
                    ? other.getSpecificCharacterSet().contains(getSpecificCharacterSet())
                            || !containsNonASCIIStringValues(null, null, 0, 0, null)
                    : getSpecificCharacterSet().contains(other.getSpecificCharacterSet())
                            || !other.containsNonASCIIStringValues(include, exclude, fromIndex, toIndex, selection))) {
                if (!(updateSpecificCharacterSet ? other.getSpecificCharacterSet() : getSpecificCharacterSet())
                        .isUTF8()) {
                    throw new InternalException(
                            "Specific Character Sets " + Arrays.toString(getSpecificCharacterSet().toCodes()) + " and "
                                    + Arrays.toString(other.getSpecificCharacterSet().toCodes()) + " not compatible");
                }
                if (updateSpecificCharacterSet) {
                    decodeStringValuesUsingSpecificCharacterSet();
                } else {
                    decodeStringValue = true;
                }
            }
        }

        boolean toggleEndian = bigEndian != other.bigEndian;
        boolean modifiedToggleEndian = modified != null && bigEndian != modified.bigEndian;
        final int[] otherTags = other.tags;
        final VR[] srcVRs = other.vrs;
        final Object[] srcValues = other.values;
        final int otherSize = other.size;
        int numAdd = 0;
        String privateCreator = null;
        String privateCreator0 = null;
        int creatorTag = 0;
        for (int i = 0; i < otherSize; i++) {
            int tag = otherTags[i];
            VR vr = srcVRs[i];
            Object value = srcValues[i];

            if (include != null && Arrays.binarySearch(include, fromIndex, toIndex, tag) < 0)
                continue;
            if (exclude != null && Arrays.binarySearch(exclude, fromIndex, toIndex, tag) >= 0)
                continue;

            if (Tag.isPrivateCreator(tag) && (privateCreator = other.privateCreatorAt(i)) != null) {
                if ((selection == null || selection.creatorTagOf(privateCreator, tag, false) > 0) && !contains(tag)
                        && (creatorTagOf(privateCreator, tag, false) < 0
                                || other.creatorTagOf(privateCreator, tag, false) != tag)) { // preserve non-conflicting
                                                                                             // Private Creator ID tag
                                                                                             // positions
                    setString(tag, VR.LO, privateCreator);
                }
                continue;
            }

            if (Tag.isPrivateTag(tag)) {
                int tmp = Tag.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = other.privateCreatorAt(other.indexOf(creatorTag));
                    privateCreator0 = privateCreator == null
                            || privateCreator.equals(privateCreatorAt(indexOf(creatorTag))) // preserve private tag
                                    ? null
                                    : privateCreator;
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
                privateCreator0 = null;
            }

            if (selection != null && !selection.contains(privateCreator, tag))
                continue;

            if (updatePolicy != null) {
                if (updatePolicy != UpdatePolicy.OVERWRITE && isEmpty(value))
                    continue;

                int j = indexOf(tag);
                if (j < 0) {
                    if (updatePolicy == UpdatePolicy.PRESERVE)
                        value = Value.NULL;
                } else {
                    if (updatePolicy == UpdatePolicy.PRESERVE)
                        continue;
                    Object origValue = vrs[j].isStringType() ? decodeStringValue(j) : values[j];
                    if (updatePolicy == UpdatePolicy.SUPPLEMENT ? !isEmpty(origValue) : equalValues(other, j, i))
                        continue;
                    if (modified != null && !isEmpty(origValue) && !modified.contains(privateCreator, tag)) {
                        if (origValue instanceof Sequence) {
                            modified.set(privateCreator, tag, (Sequence) origValue, null);
                        } else if (origValue instanceof Fragments) {
                            modified.set(privateCreator, tag, (Fragments) origValue);
                        } else {
                            modified.set(privateCreator, tag, vrs[j],
                                    toggleEndian(vrs[j], origValue, modifiedToggleEndian));
                        }
                    }
                }
            }
            if (!simulate) {
                if (value instanceof Sequence) {
                    Sequence dest;
                    if (mergeOriginalAttributesSequence && tag == Tag.OriginalAttributesSequence
                            && (dest = getSequence(tag)) != null)
                        mergeOriginalAttributesSequence((Sequence) value, dest);
                    else
                        set(privateCreator0, tag, (Sequence) value,
                                selection != null ? selection.getNestedDataset(privateCreator, tag) : null);
                } else if (value instanceof Fragments) {
                    set(privateCreator0, tag, (Fragments) value);
                } else {
                    if (decodeStringValue && vr.useSpecificCharacterSet()) {
                        value = other.loadBulkData(vr, value);
                        if (value instanceof byte[])
                            value = vr.toStrings(value, other.bigEndian(), other.getSpecificCharacterSet());
                    }
                    set(privateCreator0, tag, vr, toggleEndian(vr, value, toggleEndian));
                }
            }
            numAdd++;
        }
        return numAdd != 0;
    }

    private boolean containsNonASCIIStringValues(int[] include, int[] exclude, int fromIndex, int toIndex,
            Attributes selection) {
        for (int i = 0; i < size; i++) {
            int tag = tags[i];
            Object val = values[i];
            if ((include == null || Arrays.binarySearch(include, fromIndex, toIndex, tag) >= 0)
                    && (exclude == null || Arrays.binarySearch(exclude, fromIndex, toIndex, tag) < 0)
                    && (selection == null || selection.contains(tag))) {
                if (val instanceof Sequence) {
                    Attributes nestedSelection = selection != null ? selection.getNestedDataset(tag) : null;
                    for (Attributes item : ((Sequence) val)) {
                        if (item.containsNonASCIIStringValues(null, null, 0, 0, nestedSelection)) {
                            return true;
                        }
                    }
                } else if (val != Value.NULL && vrs[i].useSpecificCharacterSet()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isUpdateSpecificCharacterSet(Attributes other, int[] include, int[] exclude, int fromIndex,
            int toIndex, Attributes selection, UpdatePolicy updatePolicy) {
        String[] oscs = other.getStrings(Tag.SpecificCharacterSet);
        if (oscs == null)
            return false;

        if (updatePolicy != null)
            switch (updatePolicy) {
            case PRESERVE:
                return false;
            case SUPPLEMENT:
                if (containsValue(Tag.SpecificCharacterSet))
                    return false;
            case MERGE:
                if (oscs.length == 0)
                    return false;
            }

        return (include == null || Arrays.binarySearch(include, fromIndex, toIndex, Tag.SpecificCharacterSet) >= 0)
                && (exclude == null || Arrays.binarySearch(exclude, fromIndex, toIndex, Tag.SpecificCharacterSet) < 0)
                && (selection == null || selection.contains(Tag.SpecificCharacterSet));
    }

    private void mergeOriginalAttributesSequence(Sequence src, Sequence dest) {
        Map<String, Attributes> sort = new TreeMap<>();
        for (Attributes destItem : dest) {
            sort.put(destItem.getString(Tag.AttributeModificationDateTime, ""), destItem);
        }
        dest.clear();
        for (Attributes srcItem : src) {
            String dt = srcItem.getString(Tag.AttributeModificationDateTime, "");
            Attributes destItem = sort.get(dt);
            Attributes destModified;
            if (destItem != null
                    && (destModified = destItem.getNestedDataset(Tag.ModifiedAttributesSequence)) != null) {
                try {
                    Attributes srcModified;
                    if ((srcModified = srcItem.getNestedDataset(Tag.ModifiedAttributesSequence)) != null) {
                        destModified.addAll(srcModified);
                    }
                } catch (InternalException e) {
                    Logger.info("Failed to merge original attributes modified at {}: {}", dt, e.getMessage());
                }
            } else {
                sort.put(dt, new Attributes(srcItem));
            }
        }
        for (Attributes destItem : sort.values()) {
            dest.add(destItem);
        }
    }

    public boolean update(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified) {
        ensureModifiable();
        return add(newAttrs, null, null, 0, 0, null, updatePolicy, false, false, modified);
    }

    public boolean update(UpdatePolicy updatePolicy, boolean mergeOriginalAttributesSequence, Attributes newAttrs,
            Attributes modified) {
        ensureModifiable();
        return add(newAttrs, null, null, 0, 0, null, updatePolicy, mergeOriginalAttributesSequence, false, modified);
    }

    public boolean testUpdate(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified) {
        return add(newAttrs, null, null, 0, 0, null, updatePolicy, false, true, modified);
    }

    /**
     * Add selected attributes from another Attributes object to this. Optionally, the original values of overwritten
     * existing non-empty attributes are preserved in another Attributes object. The specified array of tag values must
     * be sorted (as by the {@link Arrays#sort(int[])} method) prior to making this call.
     *
     * @param newAttrs  the other Attributes object
     * @param modified  Attributes object to collect overwritten non-empty attributes with original values or null
     * @param selection sorted tag values
     * @return true> if one ore more attribute were added or overwritten with a different value
     */
    public boolean updateSelected(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified,
            int... selection) {
        ensureModifiable();
        return add(newAttrs, selection, null, 0, selection.length, null, updatePolicy, false, false, modified);
    }

    /**
     * Tests if {@link #updateSelected} would modify attributes, without actually modifying this attributes
     *
     * @param newAttrs  the other Attributes object
     * @param modified  Attributes object to collect overwritten non-empty attributes with original values or null
     * @param selection sorted tag values
     * @return true if one ore more attribute would be added or overwritten with a different value
     */
    public boolean testUpdateSelected(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified,
            int... selection) {
        return add(newAttrs, selection, null, 0, selection.length, null, updatePolicy, false, true, modified);
    }

    /**
     * Add not selected attributes from another Attributes object to this. Optionally, the original values of
     * overwritten existing non-empty attributes are preserved in another Attributes object. The specified array of tag
     * values must be sorted (as by the {@link Arrays#sort(int[])} method) prior to making this call.
     *
     * @param newAttrs  the other Attributes object
     * @param modified  Attributes object to collect overwritten non-empty attributes with original values or null
     * @param selection sorted tag values
     * @return true if one ore more attribute were added or overwritten with a different value
     */
    public boolean updateNotSelected(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified,
            int... selection) {
        ensureModifiable();
        return add(newAttrs, null, selection, 0, selection.length, null, updatePolicy, false, false, modified);
    }

    /**
     * Tests if {@link #updateNotSelected} would modify attributes, without actually modifying this attributes
     *
     * @param newAttrs  the other Attributes object
     * @param modified  Attributes object to collect overwritten non-empty attributes with original values or null
     * @param selection sorted tag values
     * @return true if one ore more attribute would be added or overwritten with a different value
     */
    public boolean testUpdateNotSelected(UpdatePolicy updatePolicy, Attributes newAttrs, Attributes modified,
            int... selection) {
        return add(newAttrs, null, selection, 0, selection.length, null, updatePolicy, false, true, modified);
    }

    /**
     * Append item to already existing or new added (0400,0561) Original Attributes Sequence.
     *
     * @param sourceOfPreviousValues
     * @param modificationDateTime
     * @param reasonForModification
     * @param modifyingSystem
     * @param originalAttributes
     * @return the same Attributes instance
     */
    public Attributes addOriginalAttributes(String sourceOfPreviousValues, Date modificationDateTime,
            String reasonForModification, String modifyingSystem, Attributes originalAttributes) {
        ensureModifiable();
        if (originalAttributes.isEmpty())
            return this;

        Attributes item = new Attributes(bigEndian, 5);
        item.ensureSequence(Tag.ModifiedAttributesSequence, 1).add(originalAttributes);
        item.setDate(Tag.AttributeModificationDateTime, VR.DT, modificationDateTime);
        item.setString(Tag.ModifyingSystem, VR.LO, modifyingSystem);
        item.setString(Tag.SourceOfPreviousValues, VR.LO, sourceOfPreviousValues);
        item.setString(Tag.ReasonForTheAttributeModification, VR.CS, reasonForModification);
        ensureSequence(Tag.OriginalAttributesSequence, 1).add(item);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Attributes other))
            return false;

        if (size != other.size)
            return false;

        String privateCreator = null;
        int creatorTag = 0;
        int otherCreatorTag = 0;
        for (int i = 0; i < size; i++) {
            int tag = tags[i];
            switch (Tag.Type.typeOf(tag)) {
            case PRIVATE_CREATOR:
                continue;
            case PRIVATE:
                int tmp = Tag.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = privateCreatorAt(indexOf(tmp));
                    if (privateCreator != null) {
                        otherCreatorTag = other.creatorTagOf(privateCreator, tag, false);
                        if (otherCreatorTag == -1)
                            return false; // other has no matching private creator
                    } else {
                        if (other.privateCreatorAt(other.indexOf(tmp)) != null)
                            return false; // other attribute has associated private creator
                    }
                }
                if (privateCreator != null) {
                    int j = other.indexOf(Tag.toPrivateTag(otherCreatorTag, tag));
                    if (j < 0 || !equalValues(other, i, j))
                        return false;
                    continue;
                }
                // fall through: treat private attributes without associated private creator like standard attributes
            case STANDARD:
                if (tag != other.tags[i] || !equalValues(other, i, i))
                    return false;
            }
        }
        return true;
    }

    public boolean equalValues(Attributes other, int tag) {
        return equalValues(other, null, tag);
    }

    public boolean equalValues(Attributes other, String privateCreator, int tag) {
        return equalValues(other, indexOf(privateCreator, tag), other.indexOf(privateCreator, tag));
    }

    private boolean equalValues(Attributes other, int index, int otherIndex) {
        if (index < 0 && otherIndex < 0)
            return true;

        VR vr = index < 0 ? other.vrs[otherIndex] : vrs[index];
        if (otherIndex >= 0 && vr != other.vrs[otherIndex])
            return false;

        if (vr.isStringType()) {
            try {
                if (vr == VR.IS)
                    return Arrays.equals(decodeISValue(index), other.decodeISValue(otherIndex));
                else if (vr == VR.DS)
                    return Arrays.equals(decodeDSValue(index), other.decodeDSValue(otherIndex));
            } catch (NumberFormatException e) {
            }
            Object v1 = index < 0 ? Value.NULL : decodeStringValue(index);
            Object v2 = otherIndex < 0 ? Value.NULL : other.decodeStringValue(otherIndex);
            return (v1 instanceof String[])
                    ? (v2 instanceof String[]) && (vr == VR.PN ? equalPNValues((String[]) v1, (String[]) v2)
                            : Arrays.equals((String[]) v1, (String[]) v2))
                    : !(v2 instanceof String[]) && (vr == VR.PN ? equalPNValues(v1, v2) : v1.equals(v2));
        }

        Object v1 = index < 0 ? Value.NULL : values[index];
        Object v2 = otherIndex < 0 ? Value.NULL : other.values[otherIndex];
        if (v1 instanceof byte[]) {
            if (v2 instanceof byte[] && ((byte[]) v1).length == ((byte[]) v2).length) {
                if (bigEndian != other.bigEndian)
                    v2 = vr.toggleEndian((byte[]) v2, true);
                return Arrays.equals((byte[]) v1, (byte[]) v2);
            }
        } else
            return v1.equals(v2);
        return false;
    }

    private boolean equalPNValues(Object v1, Object v2) {
        return v1 == Value.NULL ? !containsPNValue(v2)
                : v2 == Value.NULL ? !containsPNValue(v1) : equalPNValues((String) v1, (String) v2);
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < size; i++) {
            int tag = tags[i];
            if (!Tag.isPrivateGroup(tag))
                h = 31 * h + tag;
        }
        return h;
    }

    private void set(String privateCreator, int tag, Sequence src, Attributes selection) {
        Sequence dst = newSequence(privateCreator, tag, src.size());
        for (Attributes item : src)
            dst.add(selection != null && !selection.isEmpty() ? new Attributes(item, bigEndian, selection)
                    : new Attributes(item, bigEndian));
    }

    private void set(String privateCreator, int tag, Fragments src) {
        boolean toogleEndian = src.bigEndian() != bigEndian;
        VR vr = src.vr();
        Fragments dst = newFragments(privateCreator, tag, vr, src.size());
        for (Object frag : src)
            dst.add(toggleEndian(vr, frag, toogleEndian));
    }

    @Override
    public String toString() {
        return toString(TO_STRING_LIMIT, TO_STRING_WIDTH);
    }

    public String toString(int limit, int maxWidth) {
        return toStringBuilder(limit, maxWidth, new StringBuilder(1024)).toString();
    }

    public StringBuilder toStringBuilder(StringBuilder sb) {
        return toStringBuilder(TO_STRING_LIMIT, TO_STRING_WIDTH, sb);
    }

    public StringBuilder toStringBuilder(int limit, int maxWidth, StringBuilder sb) {
        if (appendAttributes(limit, maxWidth, sb, "") > limit)
            sb.append("...\n");
        return sb;
    }

    private int appendAttributes(int limit, int maxWidth, StringBuilder sb, String prefix) {
        if (size == 0)
            return 0;
        if (tags[0] >= 0) {
            return appendAttributes(limit, maxWidth, sb, prefix, 0, size);
        }
        int lines, index0 = -(1 + indexOf(0));
        return (lines = appendAttributes(limit, maxWidth, sb, prefix, index0, size))
                + appendAttributes(limit - lines, maxWidth, sb, prefix, 0, index0);
    }

    private int appendAttributes(int limit, int maxWidth, StringBuilder sb, String prefix, int start, int end) {
        int lines = 0;
        int creatorTag = 0;
        String privateCreator = null;
        for (int i = start; i < end; i++) {
            if (++lines > limit)
                break;
            int tag = tags[i];
            if (Tag.isPrivateTag(tag)) {
                int tmp = Tag.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = getString(creatorTag, null);
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
            }
            Object value = values[i];
            appendAttribute(privateCreator, tag, vrs[i], value, sb.length() + maxWidth, sb, prefix);
            if (value instanceof Sequence)
                lines += appendItems((Sequence) value, limit - lines, maxWidth, sb, prefix + '>');
        }
        return lines;
    }

    private int appendItems(Sequence sq, int limit, int maxWidth, StringBuilder sb, String prefix) {
        int lines = 0;
        int itemNo = 0;
        for (Attributes item : sq) {
            if (++lines > limit)
                break;
            sb.append(prefix).append("Item #").append(++itemNo).append('\n');
            lines += item.appendAttributes(limit - lines, maxWidth, sb, prefix);
        }
        return lines;
    }

    private StringBuilder appendAttribute(String privateCreator, int tag, VR vr, Object value, int maxLength,
            StringBuilder sb, String prefix) {
        sb.append(prefix).append(Tag.toString(tag)).append(' ').append(vr).append(" [");
        if (vr.prompt(value, bigEndian, getSpecificCharacterSet(vr), maxLength - sb.length() - 1, sb)) {
            sb.append("] ").append(ElementDictionary.keywordOf(tag, privateCreator));
            if (sb.length() > maxLength)
                sb.setLength(maxLength);
        }
        sb.append('\n');
        return sb;
    }

    public int calcLength(ImageEncodingOptions encOpts, boolean explicitVR) {
        if (isEmpty())
            return 0;

        this.groupLengths = encOpts.groupLength ? new int[countGroups()] : null;
        this.length = calcLength(encOpts, explicitVR, getSpecificCharacterSet(), groupLengths);
        return this.length;
    }

    private int calcLength(ImageEncodingOptions encOpts, boolean explicitVR, SpecificCharacterSet cs,
            int[] groupLengths) {
        int len, totlen = 0;
        int groupLengthTag = -1;
        int groupLengthIndex = -1;
        VR vr;
        Object val;
        for (int i = 0; i < size; i++) {
            vr = vrs[i];
            val = values[i];
            len = explicitVR ? vr.headerLength() : 8;
            if (val instanceof Value)
                len += ((Value) val).calcLength(encOpts, explicitVR, vr);
            else {
                if (!(val instanceof byte[]))
                    values[i] = val = vr.toBytes(val, cs);
                len += (((byte[]) val).length + 1) & ~1;
            }
            totlen += len;
            if (groupLengths != null) {
                int tmp = Tag.groupLengthTagOf(tags[i]);
                if (groupLengthTag != tmp) {
                    groupLengthTag = tmp;
                    groupLengthIndex++;
                    totlen += 12;
                }
                groupLengths[groupLengthIndex] += len;
            }
        }
        return totlen;
    }

    private int countGroups() {
        int groupLengthTag = -1;
        int count = 0;
        for (int i = 0; i < size; i++) {
            int tmp = Tag.groupLengthTagOf(tags[i]);
            if (groupLengthTag != tmp) {
                if (groupLengthTag < 0)
                    this.groupLengthIndex0 = count;
                groupLengthTag = tmp;
                count++;
            }
        }
        return count;
    }

    public void writeTo(ImageOutputStream out) throws IOException {
        if (isEmpty())
            return;

        if (groupLengths == null && out.getEncodingOptions().groupLength)
            throw new IllegalStateException("groupLengths not initialized by calcLength()");

        SpecificCharacterSet cs = getSpecificCharacterSet();
        if (tags[0] < 0) {
            int index0 = -(1 + indexOf(0));
            writeTo(out, cs, index0, size, groupLengthIndex0);
            writeTo(out, cs, 0, index0, 0);
        } else {
            writeTo(out, cs, 0, size, 0);
        }
    }

    public void writePostPixelDataTo(ImageOutputStream out) throws IOException {
        if (isEmpty() || tags[0] >= 0 && tags[size - 1] <= Tag.PixelData)
            return;

        SpecificCharacterSet cs = getSpecificCharacterSet();
        int indexPostPixelData = indexOf(Tag.PixelData) + 1;
        if (indexPostPixelData < 0)
            indexPostPixelData = -indexPostPixelData;

        writeTo(out, cs, indexPostPixelData, size, 0);
        if (tags[0] < 0) {
            int index0 = -(1 + indexOf(0));
            writeTo(out, cs, 0, index0, 0);
        }
    }

    public void writeItemTo(ImageOutputStream out) throws IOException {
        ImageEncodingOptions encOpts = out.getEncodingOptions();
        int len = getEncodedItemLength(encOpts, out.isExplicitVR());
        out.writeHeader(Tag.Item, null, len);
        writeTo(out);
        if (len == -1)
            out.writeHeader(Tag.ItemDelimitationItem, null, 0);
    }

    private int getEncodedItemLength(ImageEncodingOptions encOpts, boolean explicitVR) {
        if (isEmpty())
            return encOpts.undefEmptyItemLength ? -1 : 0;

        if (encOpts.undefItemLength)
            return -1;

        if (length == -1)
            calcLength(encOpts, explicitVR);

        return length;
    }

    private void writeTo(ImageOutputStream out, SpecificCharacterSet cs, int start, int end, int groupLengthIndex)
            throws IOException {
        boolean groupLength = groupLengths != null;
        int groupLengthTag = -1;
        for (int i = start; i < end; i++) {
            int tag = tags[i];
            if (groupLength) {
                int tmp = Tag.groupLengthTagOf(tag);
                if (groupLengthTag != tmp) {
                    groupLengthTag = tmp;
                    out.writeGroupLength(groupLengthTag, groupLengths[groupLengthIndex++]);
                }
            }
            out.writeAttribute(tag, vrs[i], values[i], cs);
        }
    }

    /**
     * Invokes {@link Visitor#visit} for each attribute in this instance. The operation will be aborted if
     * <code>visitor.visit()</code> returns <code>false</code>.
     *
     * @param visitor
     * @param visitNestedDatasets controls if <code>visitor.visit()</code> is also invoked for attributes in nested
     *                            datasets
     * @return <code>true</code> if the operation was not aborted.
     */
    public boolean accept(Visitor visitor, boolean visitNestedDatasets) throws Exception {
        if (isEmpty())
            return true;

        if (tags[0] < 0) {
            int index0 = -(1 + indexOf(0));
            return accept(visitor, visitNestedDatasets, index0, size)
                    && accept(visitor, visitNestedDatasets, 0, index0);
        } else {
            return accept(visitor, visitNestedDatasets, 0, size);
        }
    }

    private boolean accept(Visitor visitor, boolean visitNestedDatasets, int start, int end) throws Exception {
        for (int i = start; i < end; i++) {
            if (!visitor.visit(this, tags[i], vrs[i], values[i]))
                return false;
            if (visitNestedDatasets && (values[i] instanceof Sequence)) {
                if (visitor instanceof SequenceVisitor)
                    ((SequenceVisitor) visitor).startSequence(tags[i]);
                int itemIndex = 0;
                for (Attributes item : (Sequence) values[i]) {
                    if (visitor instanceof SequenceVisitor)
                        ((SequenceVisitor) visitor).startItem(tags[i], itemIndex);
                    if (!item.accept(visitor, true))
                        return false;
                    if (visitor instanceof SequenceVisitor)
                        ((SequenceVisitor) visitor).endItem();
                    itemIndex++;
                }
                if (visitor instanceof SequenceVisitor)
                    ((SequenceVisitor) visitor).endSequence();
            }
        }
        return true;
    }

    public void writeGroupTo(ImageOutputStream out, int groupLengthTag) throws IOException {
        if (isEmpty())
            throw new IllegalStateException("No attributes");

        checkInGroup(0, groupLengthTag);
        checkInGroup(size - 1, groupLengthTag);
        SpecificCharacterSet cs = getSpecificCharacterSet();
        out.writeGroupLength(groupLengthTag, calcLength(out.getEncodingOptions(), out.isExplicitVR(), cs, null));
        writeTo(out, cs, 0, size, 0);
    }

    private void checkInGroup(int i, int groupLengthTag) {
        int tag = tags[i];
        if (Tag.groupLengthTagOf(tag) != groupLengthTag)
            throw new IllegalStateException(Tag.toString(tag) + " does not belong to group ("
                    + Tag.shortToHexString(Tag.groupNumber(groupLengthTag)) + ",eeee).");

    }

    /**
     * Creates DICOM File Meta Information for this <i>Data Set</i> with given <i>Transfer Syntax UID (0002,0010)</i>,
     * including optional <i>Implementation Version Name (0002,0013)</i>.
     *
     * @param tsuid <i>Transfer Syntax UID (0002,0010)</i>
     * @return created DICOM File Meta Information
     */
    public Attributes createFileMetaInformation(String tsuid) {
        return createFileMetaInformation(tsuid, true);
    }

    /**
     * Creates DICOM File Meta Information for this <i>Data Set</i> with given <i>Transfer Syntax UID (0002,0010)</i>.
     *
     * @param tsuid                            <i>Transfer Syntax UID (0002,0010)</i>
     * @param includeImplementationVersionName <code>true</code> if the optional <i>Implementation Version Name
     *                                         (0002,0013)</i> is to be included; <code>false</code> if it is to be
     *                                         omitted.
     * @return created DICOM File Meta Information
     */
    public Attributes createFileMetaInformation(String tsuid, boolean includeImplementationVersionName) {
        return createFileMetaInformation(getString(Tag.SOPInstanceUID, null), getString(Tag.SOPClassUID, null), tsuid,
                includeImplementationVersionName);
    }

    public boolean matches(Attributes keys, boolean ignorePNCase, boolean matchNoValue) {
        int[] keyTags = keys.tags;
        VR[] keyVrs = keys.vrs;
        Object[] keyValues = keys.values;
        int keysSize = keys.size;
        String privateCreator = null;
        int creatorTag = 0;
        for (int i = 0; i < keysSize; i++) {
            int tag = keyTags[i];
            if (Tag.isPrivateCreator(tag))
                continue;

            if (Tag.isPrivateGroup(tag)) {
                int tmp = Tag.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = keys.getString(creatorTag, null);
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
            }

            Object keyValue = keyValues[i];
            if (isEmpty(keyValue))
                continue;

            if (keyVrs[i].isStringType()) {
                if (!matches(privateCreator, tag, keyVrs[i], ignorePNCase, matchNoValue,
                        keys.getStrings(privateCreator, tag, null)))
                    return false;
            } else if (keyValue instanceof Sequence) {
                if (!matches(privateCreator, tag, ignorePNCase, matchNoValue, (Sequence) keyValue))
                    return false;
            } else {
                Logger.info("Matching Key {} with VR: {} not supported", Tag.toString(tag), keyVrs[i]);
            }
        }
        return true;
    }

    private boolean matches(String privateCreator, int tag, VR vr, boolean ignorePNCase, boolean matchNoValue,
            String[] keyVals) {
        String[] vals = getStrings(privateCreator, tag, null);
        if (vals == null || vals.length == 0)
            return matchNoValue;

        boolean ignoreCase = ignorePNCase && vr == VR.PN;
        for (String keyVal : keyVals) {
            DateRange dateRange = null;
            switch (vr) {
            case PN:
                keyVal = new PersonName(keyVals[0]).toString();
                break;
            case DA:
            case DT:
            case TM:
                dateRange = toDateRange(keyVal, vr);
                break;
            }

            if (Builder.containsWildCard(keyVal)) {
                Pattern pattern = Builder.compilePattern(keyVal, ignoreCase);
                for (String val : vals) {
                    if (val == null)
                        if (matchNoValue)
                            return true;
                        else
                            continue;
                    if (vr == VR.PN)
                        val = new PersonName(val).toString();
                    if (pattern.matcher(val).matches())
                        return true;
                }
            } else {
                for (String val : vals) {
                    if (val == null)
                        if (matchNoValue)
                            return true;
                        else
                            continue;
                    if (dateRange != null)
                        if (dateRange.contains(vr.toDate(val, getTimeZone(), 0, false, null, new DatePrecision())))
                            return true;
                        else
                            continue;
                    if (vr == VR.PN)
                        val = new PersonName(val).toString();
                    if (ignoreCase ? keyVal.equalsIgnoreCase(val) : keyVal.equals(val))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean matches(String privateCreator, int tag, boolean ignorePNCase, boolean matchNoValue,
            Sequence keySeq) {
        int n = keySeq.size();
        if (n > 1)
            Logger.info("Matching Key {} with VR: SQ contains {} Items - only consider first Item", Tag.toString(tag),
                    n);

        Attributes keys = keySeq.get(0);
        if (keys.isEmpty())
            return true;

        Object value = getValue(privateCreator, tag);
        if (value == null || isEmpty(value))
            return matchNoValue;

        if (value instanceof Sequence sq) {
            for (Attributes item : sq)
                if (item.matches(keys, ignorePNCase, matchNoValue))
                    return true;
        }
        return false;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(size);

        ImageOutputStream dout = new ImageOutputStream(out,
                bigEndian ? UID.ExplicitVRBigEndian.uid : UID.ExplicitVRLittleEndian.uid);
        dout.writeDataset(null, this);
        dout.writeHeader(Tag.ItemDelimitationItem, null, 0);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init(in.readInt());

        ImageInputStream din = new ImageInputStream(in,
                bigEndian ? UID.ExplicitVRBigEndian.uid : UID.ExplicitVRLittleEndian.uid);
        din.readItemValue(this, -1);
    }

    public ValidationResult validate(IOD iod) {
        ValidationResult result = new ValidationResult();
        HashMap<String, Boolean> resolvedConditions = new HashMap<>();
        for (IOD.DataElement el : iod) {
            validate(el, result, resolvedConditions);
        }
        return result;
    }

    public void validate(IOD.DataElement el, ValidationResult result) {
        validate(el, result, null);
    }

    private void validate(IOD.DataElement el, ValidationResult result, Map<String, Boolean> processedConditions) {
        IOD.Condition condition = el.getCondition();
        if (condition != null) {
            String id = condition.id();
            Boolean match = id != null ? processedConditions.get(id) : null;
            if (match == null) {
                match = condition.match(this);
                if (id != null)
                    processedConditions.put(id, match);
            }
            if (!match)
                return;
        }
        int index = indexOf(el.tag);
        if (index < 0) {
            if (el.type == IOD.DataElementType.TYPE_1 || el.type == IOD.DataElementType.TYPE_2) {
                result.addMissingAttribute(el);
            }
            return;
        }
        Object value = values[index];
        if (isEmpty(value)) {
            if (el.type == IOD.DataElementType.TYPE_1) {
                result.addMissingAttributeValue(el);
            }
            return;
        }
        if (el.type == IOD.DataElementType.TYPE_0) {
            result.addNotAllowedAttribute(el);
            return;
        }
        VR vr = vrs[index];
        if (vr.isStringType()) {
            value = decodeStringValue(index);
        }

        Object validVals = el.getValues();
        if (el.vr == VR.SQ) {
            if (!(value instanceof Sequence seq)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            int seqSize = seq.size();
            if (el.maxVM > 0 && seqSize > el.maxVM) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.MultipleItems);
                return;
            }
            if (validVals instanceof Code[]) {
                boolean invalidItem = false;
                ValidationResult[] itemValidationResults = new ValidationResult[seqSize];
                for (int i = 0; i < seqSize; i++) {
                    ValidationResult itemValidationResult = validateCode(seq.get(i), (Code[]) validVals);
                    invalidItem = invalidItem || !itemValidationResult.isValid();
                    itemValidationResults[i] = itemValidationResult;
                }
                if (invalidItem) {
                    result.addInvalidAttributeValue(el, ValidationResult.Invalid.Code, itemValidationResults, null);
                }
            } else if (validVals instanceof IOD[] itemIODs) {
                int[] matchingItems = new int[itemIODs.length];
                boolean invalidItem = false;
                ValidationResult[] itemValidationResults = new ValidationResult[seqSize];
                for (int i = 0; i < seqSize; i++) {
                    ValidationResult itemValidationResult = new ValidationResult();
                    HashMap<String, Boolean> resolvedItemConditions = new HashMap<String, Boolean>();
                    Attributes item = seq.get(i);
                    for (int j = 0; j < itemIODs.length; j++) {
                        IOD itemIOD = itemIODs[j];
                        IOD.Condition itemCondition = itemIOD.getCondition();
                        if (itemCondition != null) {
                            String id = itemCondition.id();
                            Boolean match = id != null ? resolvedItemConditions.get(id) : null;
                            if (match == null) {
                                match = itemCondition.match(item);
                                if (id != null)
                                    resolvedItemConditions.put(id, match);
                            }
                            if (!match)
                                continue;
                        }
                        matchingItems[j]++;
                        for (IOD.DataElement itemEl : itemIOD) {
                            item.validate(itemEl, itemValidationResult, resolvedItemConditions);
                        }
                    }
                    invalidItem = invalidItem || !itemValidationResult.isValid();
                    itemValidationResults[i] = itemValidationResult;
                }
                IOD[] missingItems = checkforMissingItems(matchingItems, itemIODs);
                if (invalidItem || missingItems != null) {
                    result.addInvalidAttributeValue(el, ValidationResult.Invalid.Item, itemValidationResults,
                            missingItems);
                }
            }
            return;
        }

        if (el.maxVM > 0 || el.minVM > 1) {
            int vm = vr.vmOf(value);
            if (el.maxVM > 0 && vm > el.maxVM || el.minVM > 1 && vm < el.minVM) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VM);
                return;
            }
        }
        if (validVals == null)
            return;

        if (validVals instanceof String[]) {
            if (!vr.isStringType()) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            if (!isValidValue(toStrings(value), el.valueNumber, (String[]) validVals)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.Value);
            }
        } else if (validVals instanceof int[]) {
            if (vr == VR.IS)
                value = decodeISValue(index);
            else if (!vr.isIntType()) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.VR);
                return;
            }
            if (!isValidValue(vr.toInts(value, bigEndian), el.valueNumber, (int[]) validVals)) {
                result.addInvalidAttributeValue(el, ValidationResult.Invalid.Value);
            }
        }
    }

    private IOD[] checkforMissingItems(int[] matchingItems, IOD[] itemIODs) {
        IOD[] missingItems = new IOD[matchingItems.length];
        int n = 0;
        for (int i = 0; i < matchingItems.length; i++) {
            IOD itemIOD = itemIODs[i];
            if (matchingItems[i] == 0 && itemIOD.getType() == IOD.DataElementType.TYPE_1)
                missingItems[n++] = itemIOD;
        }
        return n > 0 ? Arrays.copyOf(missingItems, n) : null;
    }

    private ValidationResult validateCode(Attributes item, Code[] validVals) {
        ValidationResult result = null;
        for (Code code : validVals) {
            result = item.validate(IOD.valueOf(code));
            if (result.isValid())
                break;
        }
        return result;
    }

    private boolean isValidValue(String[] val, int valueNumber, String[] validVals) {
        if (valueNumber != 0)
            return val.length < valueNumber || isOneOf(val[valueNumber - 1], validVals);

        for (int i = 0; i < val.length; i++)
            if (!isOneOf(val[i], validVals))
                return false;
        return true;
    }

    private <T> boolean isOneOf(Object val, T[] ss) {
        if (ss == null)
            return true;
        for (T s : ss)
            if (val.equals(s))
                return true;
        return false;
    }

    private boolean isValidValue(int[] val, int valueNumber, int[] validVals) {
        if (valueNumber != 0)
            return val.length < valueNumber || isOneOf(val[valueNumber - 1], validVals);

        for (int i = 0; i < val.length; i++)
            if (!isOneOf(val[i], validVals))
                return false;
        return true;
    }

    private boolean isOneOf(int val, int[] is) {
        if (is == null)
            return true;
        for (int i : is)
            if (val == i)
                return true;
        return false;
    }

    /**
     * Add attributes of this data set which were replaced in the specified other data set into the result data set. If
     * no result data set is passed, a new result set will be instantiated.
     *
     * @param other  data set
     * @param result data set or {@code null}
     * @return result data set.
     */
    public Attributes getModified(Attributes other, Attributes result) {
        if (result == null)
            result = new Attributes(other.size);
        int creatorTag = -1;
        int prevOtherCreatorTag = -1;
        int otherCreatorTag = -1;
        String privateCreator = null;
        for (int i = 0; i < other.size; i++) {
            int tag = other.tags[i];
            if ((tag & 0x00010000) != 0) { // private group
                if ((tag & 0x0000ff00) == 0)
                    continue; // skip private creator

                otherCreatorTag = Tag.creatorTagOf(tag);
                if (prevOtherCreatorTag != otherCreatorTag) {
                    prevOtherCreatorTag = otherCreatorTag;
                    creatorTag = -1;
                    int k = other.indexOf(otherCreatorTag);
                    if (k >= 0) {
                        Object o = other.decodeStringValue(k);
                        if (o instanceof String) {
                            privateCreator = (String) o;
                            creatorTag = creatorTagOf(privateCreator, tag, false);
                        }
                    }
                }
                if (creatorTag == -1)
                    continue; // no matching Private Creator

                tag = Tag.toPrivateTag(creatorTag, tag);
            } else {
                privateCreator = null;
            }

            int j = indexOf(tag);
            if (j < 0)
                continue;

            Object origValue = values[j];
            if (origValue instanceof Value && ((Value) origValue).isEmpty())
                continue;

            if (equalValues(other, j, i))
                continue;

            if (origValue instanceof Sequence) {
                result.set(privateCreator, tag, (Sequence) origValue, null);
            } else if (origValue instanceof Fragments) {
                result.set(privateCreator, tag, (Fragments) origValue);
            } else {
                result.set(privateCreator, tag, vrs[j], origValue);
            }
        }
        return result;
    }

    /**
     * Returns attributes of this data set which were removed or replaced in the specified other data set.
     *
     * @param other data set
     * @return attributes of this data set which were removed or replaced in the specified other data set.
     */
    public Attributes getRemovedOrModified(Attributes other) {
        Attributes modified = new Attributes(size);
        int creatorTag = -1;
        int prevCreatorTag = -1;
        int otherCreatorTag = 0;
        String privateCreator = null;
        for (int i = 0; i < size; i++) {
            int tag = tags[i];
            if ((tag & 0x00010000) != 0) { // private group
                if ((tag & 0x0000ff00) == 0)
                    continue; // skip private creator

                creatorTag = Tag.creatorTagOf(tag);
                if (prevCreatorTag != creatorTag) {
                    prevCreatorTag = creatorTag;
                    otherCreatorTag = -1;
                    privateCreator = null;
                    int k = indexOf(creatorTag);
                    if (k >= 0) {
                        Object o = decodeStringValue(k);
                        if (o instanceof String) {
                            privateCreator = (String) o;
                            otherCreatorTag = other.creatorTagOf(privateCreator, tag, false);
                        }
                    }
                }
                if (privateCreator == null)
                    continue; // no Private Creator

                if (otherCreatorTag != -1)
                    tag = Tag.toPrivateTag(otherCreatorTag, tag);
            } else {
                otherCreatorTag = 0;
                privateCreator = null;
            }

            Object origValue = values[i];
            if (origValue instanceof Value && ((Value) origValue).isEmpty())
                continue;

            if (otherCreatorTag >= 0) {
                int j = other.indexOf(tag);
                if (j >= 0 && equalValues(other, i, j))
                    continue;
            }

            if (origValue instanceof Sequence) {
                modified.set(privateCreator, tag, (Sequence) origValue, null);
            } else if (origValue instanceof Fragments) {
                modified.set(privateCreator, tag, (Fragments) origValue);
            } else {
                modified.set(privateCreator, tag, vrs[i], origValue);
            }
        }
        return modified;
    }

    public int diff(Attributes other, int[] selection, Attributes diff) {
        return diff(other, selection, diff, false);
    }

    public int diff(Attributes other, int[] selection, Attributes diff, boolean onlyModified) {
        int count = 0;
        for (int tag : selection) {
            int index = indexOf(tag);
            int otherIndex = other.indexOf(tag);
            if (!equalValues(other, index, otherIndex)) {
                if (diff != null) {
                    Object value = index < 0 ? Value.NULL : values[index];
                    if (!onlyModified || value != Value.NULL) {
                        if (value instanceof Sequence) {
                            diff.set(null, tag, (Sequence) value, null);
                        } else {
                            diff.set(tag, index < 0 ? other.vrs[otherIndex] : vrs[index], value);
                        }
                    }
                }
                count++;
            }
        }
        return count;
    }

    public int removeAllBulkData() {
        ensureModifiable();
        int removed = 0;
        for (int i = 0; i < size; i++) {
            Object value = values[i];
            if (isBulkData(value)) {
                int srcPos = i + 1;
                int len = size - srcPos;
                System.arraycopy(tags, srcPos, tags, i, len);
                System.arraycopy(vrs, srcPos, vrs, i, len);
                System.arraycopy(values, srcPos, values, i, len);
                i--;
                size--;
                removed++;
            } else if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value) {
                    removed += item.removeAllBulkData();
                }
            }
        }
        return removed;
    }

    private int creatorIndexOf(String privateCreator, int groupNumber) {
        if ((groupNumber & 1) == 0)
            throw new IllegalArgumentException(
                    "(" + Tag.shortToHexString(groupNumber) + ",xxxx) is not a private Group");

        int group = groupNumber << 16;
        int creatorTag = group | 0x10;
        int index = indexOf(creatorTag);
        if (index < 0)
            index = -index - 1;
        while (index < size && (tags[index] & 0xffffff00) == group) {
            if (vrs[index].isStringType()) {
                Object creatorID = decodeStringValue(index);
                if (privateCreator.equals(creatorID))
                    return index;
            }
            index++;
            creatorTag++;
        }
        return -1;
    }

    public int removePrivateAttributes(String privateCreator, int groupNumber) {
        ensureModifiable();
        int privateCreatorIndex = creatorIndexOf(privateCreator, groupNumber);
        if (privateCreatorIndex < 0)
            return 0;

        int creatorTag = tags[privateCreatorIndex];
        int privateTag = (creatorTag & 0xffff0000) | ((creatorTag & 0xff) << 8);
        int srcPos = privateCreatorIndex + 1;
        int start = srcPos;
        while (start < size && tags[start] < privateTag)
            start++;

        int end = start;
        while (end < size && (tags[end] & 0xffffff00) == privateTag)
            end++;

        int len1 = start - srcPos;
        if (len1 > 0) {
            System.arraycopy(tags, srcPos, tags, privateCreatorIndex, len1);
            System.arraycopy(vrs, srcPos, vrs, privateCreatorIndex, len1);
            System.arraycopy(values, srcPos, values, privateCreatorIndex, len1);
        }

        int len2 = size - end;
        if (len2 > 0) {
            int destPos = start - 1;
            System.arraycopy(tags, end, tags, destPos, len2);
            System.arraycopy(vrs, end, vrs, destPos, len2);
            System.arraycopy(values, end, values, destPos, len2);
        }
        int removed = end - start;
        int size1 = size - removed - 1;
        Arrays.fill(tags, size1, size, 0);
        Arrays.fill(vrs, size1, size, null);
        Arrays.fill(values, size1, size, null);
        size = size1;
        return removed;
    }

    public int removePrivateAttributes() {
        ensureModifiable();
        int size1 = size;
        for (int i = 0; i < size1; i++) {
            int j = i;
            while (j < size1 && Tag.isPrivateGroup(tags[j]))
                j++;
            if (j > i) {
                int len = size1 - j;
                if (len > 0) {
                    System.arraycopy(tags, j, tags, i, len);
                    System.arraycopy(vrs, j, vrs, i, len);
                    System.arraycopy(values, j, values, i, len);
                }
                size1 -= j - i;
            }
        }
        int removed = size - size1;
        if (removed > 0) {
            Arrays.fill(tags, size1, size, 0);
            Arrays.fill(vrs, size1, size, null);
            Arrays.fill(values, size1, size, null);
            size = size1;
        }
        return removed;
    }

    public void removeSelected(int... selection) {
        ensureModifiable();
        for (int i = 0; i < size; i++) {
            if (Arrays.binarySearch(selection, tags[i]) >= 0) {
                int numMoved = size - i - 1;
                if (numMoved > 0) {
                    System.arraycopy(tags, i + 1, tags, i, numMoved);
                    System.arraycopy(vrs, i + 1, vrs, i, numMoved);
                    System.arraycopy(values, i + 1, values, i, numMoved);
                }
                values[--size] = null;
                --i;
            }
        }
    }

    public void replaceSelected(Attributes others, int... selection) {
        ensureModifiable();
        for (int i = 0; i < size; i++) {
            if (Arrays.binarySearch(selection, tags[i]) >= 0) {
                values[i] = Builder.maskNull(others.getValue(tags[i]), Value.NULL);
            }
        }
    }

    public void replaceUIDSelected(int... selection) {
        ensureModifiable();
        for (int i = 0; i < size; i++) {
            if (Arrays.binarySearch(selection, tags[i]) >= 0 && values[i] != Value.NULL) {
                values[i] = replaceUIDs(decodeStringValue(i));
            }
        }
    }

    private Object replaceUIDs(Object val) {
        if (val instanceof String) {
            return UID.remapUID((String) val);
        }
        if (val instanceof String[] ss) {
            for (int i = 0; i < ss.length; i++) {
                ss[i] = UID.remapUID(ss[i]);
            }
        }
        return val;
    }

    public int removeCurveData() {
        ensureModifiable();
        return removeRepeatingGroup(0x50000000);
    }

    public int removeOverlayData() {
        ensureModifiable();
        return removeRepeatingGroup(0x60000000);
    }

    private int removeRepeatingGroup(int ggxxxxxx) {
        int size1 = size;
        int i = indexForInsertOf(ggxxxxxx);
        if (i < 0)
            i = -i - 1;
        while (i < size1 && (tags[i] & 0xFFE00000) == ggxxxxxx) {
            int j = i;
            while (j < size1 && (tags[j] & 0xFFE10000) == ggxxxxxx)
                j++;
            if (j > i) {
                int len = size1 - j;
                if (len > 0) {
                    System.arraycopy(tags, j, tags, i, len);
                    System.arraycopy(vrs, j, vrs, i, len);
                    System.arraycopy(values, j, values, i, len);
                }
                size1 -= j - i;
            }
            i++;
        }
        int removed = size - size1;
        if (removed > 0) {
            Arrays.fill(tags, size1, size, 0);
            Arrays.fill(vrs, size1, size, null);
            Arrays.fill(values, size1, size, null);
            size = size1;
        }
        return removed;
    }

}
