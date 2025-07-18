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

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * A FileStore represents a storage pool, device, partition, volume, concrete file system or other implementation
 * specific means of file storage. This object carries the same interpretation as core Java's
 * {@link java.nio.file.FileStore} class, with additional information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public interface OSFileStore {

    /**
     * Name of the File System. A human-readable label that does not necessarily correspond to a file system path.
     *
     * @return The file system name
     */
    String getName();

    /**
     * Volume name of the File System. Generally a path representing the device (e.g., {@code /dev/foo} which is being
     * mounted.
     *
     * @return The volume name of the file system
     */
    String getVolume();

    /**
     * Label of the File System. An optional replacement for the name on Windows and Linux.
     *
     * @return The volume label of the file system. Only relevant on Windows and on Linux, if assigned; otherwise
     *         defaults to the FileSystem name. On other operating systems is redundant with the name.
     */
    String getLabel();

    /**
     * Logical volume of the File System.
     * <p>
     * Provides an optional alternative volume identifier for the file system. Only supported on Linux, provides symlink
     * value via '/dev/mapper/' (used with LVM file systems).
     *
     * @return The logical volume of the file system
     */
    String getLogicalVolume();

    /**
     * Mount point of the File System. The directory users will normally use to interface with the file store.
     *
     * @return The mountpoint of the file system
     */
    String getMount();

    /**
     * Description of the File System.
     *
     * @return The file system description
     */
    String getDescription();

    /**
     * Type of the File System (FAT, NTFS, etx2, ext4, etc.)
     *
     * @return The file system type
     */
    String getType();

    /**
     * Filesystem options.
     *
     * @return A comma-deimited string of options
     */
    String getOptions();

    /**
     * UUID/GUID of the File System.
     *
     * @return The file system UUID/GUID
     */
    String getUUID();

    /**
     * Free space on the drive. This space is unallocated but may require elevated permissions to write.
     *
     * @return Free space on the drive (in bytes)
     */
    long getFreeSpace();

    /**
     * Usable space on the drive. This is space available to unprivileged users.
     *
     * @return Usable space on the drive (in bytes)
     */
    long getUsableSpace();

    /**
     * Total space/capacity of the drive.
     *
     * @return Total capacity of the drive (in bytes)
     */
    long getTotalSpace();

    /**
     * Usable / free inodes on the drive. Not applicable on Windows.
     *
     * @return Usable / free inodes on the drive (count), or -1 if unimplemented
     */
    long getFreeInodes();

    /**
     * Total / maximum number of inodes of the filesystem. Not applicable on Windows.
     *
     * @return Total / maximum number of inodes of the filesystem (count), or -1 if unimplemented
     */
    long getTotalInodes();

    /**
     * Make a best effort to update all the statistics about the file store without needing to recreate the file store
     * list. This method provides for more frequent periodic updates of file store statistics.
     *
     * @return True if the update was (probably) successful, false if the disk was not found
     */
    boolean updateAttributes();

}
