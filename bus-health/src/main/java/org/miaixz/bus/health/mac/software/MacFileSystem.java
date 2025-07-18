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
package org.miaixz.bus.health.mac.software;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractFileSystem;
import org.miaixz.bus.health.mac.CFKit;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFMutableDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.DiskArbitration;
import com.sun.jna.platform.mac.DiskArbitration.DADiskRef;
import com.sun.jna.platform.mac.DiskArbitration.DASessionRef;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.Statfs;

/**
 * The Mac File System contains {@link OSFileStore}s which are a storage pool, device, partition, volume, concrete file
 * system or other implementation specific means of file storage. In macOS, these are found in the /Volumes directory.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class MacFileSystem extends AbstractFileSystem {

    private static final List<PathMatcher> FS_PATH_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._MAC_FS_PATH_EXCLUDES);
    private static final List<PathMatcher> FS_PATH_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._MAC_FS_PATH_INCLUDES);
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._MAC_FS_VOLUME_EXCLUDES);
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._MAC_FS_VOLUME_INCLUDES);

    // Regexp matcher for /dev/disk1 etc.
    private static final Pattern LOCAL_DISK = Pattern.compile("/dev/disk\\d");

    // User specifiable flags.
    private static final int MNT_RDONLY = 0x00000001;
    private static final int MNT_SYNCHRONOUS = 0x00000002;
    private static final int MNT_NOEXEC = 0x00000004;
    private static final int MNT_NOSUID = 0x00000008;
    private static final int MNT_NODEV = 0x00000010;
    private static final int MNT_UNION = 0x00000020;
    private static final int MNT_ASYNC = 0x00000040;
    private static final int MNT_CPROTECT = 0x00000080;
    private static final int MNT_EXPORTED = 0x00000100;
    private static final int MNT_QUARANTINE = 0x00000400;
    private static final int MNT_LOCAL = 0x00001000;
    private static final int MNT_QUOTA = 0x00002000;
    private static final int MNT_ROOTFS = 0x00004000;
    private static final int MNT_DOVOLFS = 0x00008000;
    private static final int MNT_DONTBROWSE = 0x00100000;
    private static final int MNT_IGNORE_OWNERSHIP = 0x00200000;
    private static final int MNT_AUTOMOUNTED = 0x00400000;
    private static final int MNT_JOURNALED = 0x00800000;
    private static final int MNT_NOUSERXATTR = 0x01000000;
    private static final int MNT_DEFWRITE = 0x02000000;
    private static final int MNT_MULTILABEL = 0x04000000;
    private static final int MNT_NOATIME = 0x10000000;

    private static final Map<Integer, String> OPTIONS_MAP = new HashMap<>();

    static {
        OPTIONS_MAP.put(MNT_SYNCHRONOUS, "synchronous");
        OPTIONS_MAP.put(MNT_NOEXEC, "noexec");
        OPTIONS_MAP.put(MNT_NOSUID, "nosuid");
        OPTIONS_MAP.put(MNT_NODEV, "nodev");
        OPTIONS_MAP.put(MNT_UNION, "union");
        OPTIONS_MAP.put(MNT_ASYNC, "asynchronous");
        OPTIONS_MAP.put(MNT_CPROTECT, "content-protection");
        OPTIONS_MAP.put(MNT_EXPORTED, "exported");
        OPTIONS_MAP.put(MNT_QUARANTINE, "quarantined");
        OPTIONS_MAP.put(MNT_LOCAL, "local");
        OPTIONS_MAP.put(MNT_QUOTA, "quotas");
        OPTIONS_MAP.put(MNT_ROOTFS, "rootfs");
        OPTIONS_MAP.put(MNT_DOVOLFS, "volfs");
        OPTIONS_MAP.put(MNT_DONTBROWSE, "nobrowse");
        OPTIONS_MAP.put(MNT_IGNORE_OWNERSHIP, "noowners");
        OPTIONS_MAP.put(MNT_AUTOMOUNTED, "automounted");
        OPTIONS_MAP.put(MNT_JOURNALED, "journaled");
        OPTIONS_MAP.put(MNT_NOUSERXATTR, "nouserxattr");
        OPTIONS_MAP.put(MNT_DEFWRITE, "defwrite");
        OPTIONS_MAP.put(MNT_MULTILABEL, "multilabel");
        OPTIONS_MAP.put(MNT_NOATIME, "noatime");
    }

    private static List<OSFileStore> getFileStoreMatching(String nameToMatch, boolean localOnly) {
        List<OSFileStore> fsList = new ArrayList<>();

        // Use getfsstat to find fileSystems
        // Query with null to get total # required
        int numfs = SystemB.INSTANCE.getfsstat64(null, 0, 0);
        if (numfs > 0) {
            // Open a DiskArbitration session to get VolumeName of file systems
            // with bsd names
            DASessionRef session = DiskArbitration.INSTANCE
                    .DASessionCreate(CoreFoundation.INSTANCE.CFAllocatorGetDefault());
            if (session == null) {
                Logger.error("Unable to open session to DiskArbitration framework.");
            } else {
                CFStringRef daVolumeNameKey = CFStringRef.createCFString("DAVolumeName");

                // Create array to hold results
                Statfs s = new Statfs();
                Statfs[] fs = (Statfs[]) s.toArray(numfs);
                // Fill array with results
                numfs = SystemB.INSTANCE.getfsstat64(fs, fs[0].size() * fs.length, SystemB.MNT_NOWAIT);
                for (int f = 0; f < numfs; f++) {
                    // Mount on name will match mounted path, e.g. /Volumes/foo
                    // Mount to name will match canonical path., e.g., /dev/disk0s2
                    // Byte arrays are null-terminated strings

                    // Get volume and path name, and type
                    String volume = Native.toString(fs[f].f_mntfromname, Charset.UTF_8);
                    String path = Native.toString(fs[f].f_mntonname, Charset.UTF_8);
                    String type = Native.toString(fs[f].f_fstypename, Charset.UTF_8);
                    // Skip non-local drives if requested, skip system types
                    final int flags = fs[f].f_flags;

                    // Skip non-local drives if requested, and exclude pseudo file systems
                    if ((localOnly && (flags & MNT_LOCAL) == 0) || !path.equals("/")
                            && (PSEUDO_FS_TYPES.contains(type) || Builder.isFileStoreExcluded(path, volume,
                                    FS_PATH_INCLUDES, FS_PATH_EXCLUDES, FS_VOLUME_INCLUDES, FS_VOLUME_EXCLUDES))) {
                        continue;
                    }

                    String description = "Volume";
                    if (LOCAL_DISK.matcher(volume).matches()) {
                        description = "Local Disk";
                    } else if (volume.startsWith("localhost:") || volume.startsWith("//") || volume.startsWith("smb://")
                            || NETWORK_FS_TYPES.contains(type)) {
                        description = "Network Drive";
                    }
                    File file = new File(path);
                    String name = file.getName();
                    // getName() for / is still blank, so:
                    if (name.isEmpty()) {
                        name = file.getPath();
                    }
                    if (nameToMatch != null && !nameToMatch.equals(name)) {
                        continue;
                    }

                    StringBuilder options = new StringBuilder((MNT_RDONLY & flags) == 0 ? "rw" : "ro");
                    String moreOptions = OPTIONS_MAP.entrySet().stream().filter(e -> (e.getKey() & flags) > 0)
                            .map(Map.Entry::getValue).collect(Collectors.joining(Symbol.COMMA));
                    if (!moreOptions.isEmpty()) {
                        options.append(Symbol.C_COMMA).append(moreOptions);
                    }

                    String uuid = Normal.EMPTY;
                    // Use volume to find DiskArbitration volume name and search for
                    // the registry entry for UUID
                    String bsdName = volume.replace("/dev/disk", "disk");
                    if (bsdName.startsWith("disk")) {
                        // Get the DiskArbitration dictionary for this disk,
                        // which has volumename
                        DADiskRef disk = DiskArbitration.INSTANCE.DADiskCreateFromBSDName(
                                CoreFoundation.INSTANCE.CFAllocatorGetDefault(), session, volume);
                        if (disk != null) {
                            CFDictionaryRef diskInfo = DiskArbitration.INSTANCE.DADiskCopyDescription(disk);
                            if (diskInfo != null) {
                                // get volume name from its key
                                Pointer result = diskInfo.getValue(daVolumeNameKey);
                                name = CFKit.cfPointerToString(result);
                                diskInfo.release();
                            }
                            disk.release();
                        }
                        // Search for bsd name in IOKit registry for UUID
                        CFMutableDictionaryRef matchingDict = IOKitUtil.getBSDNameMatchingDict(bsdName);
                        if (matchingDict != null) {
                            // search for all IOservices that match the bsd name
                            IOIterator fsIter = IOKitUtil.getMatchingServices(matchingDict);
                            if (fsIter != null) {
                                // getMatchingServices releases matchingDict
                                // Should only match one logical drive
                                IORegistryEntry fsEntry = fsIter.next();
                                if (fsEntry != null && fsEntry.conformsTo("IOMedia")) {
                                    // Now get the UUID
                                    uuid = fsEntry.getStringProperty("UUID");
                                    if (uuid != null) {
                                        uuid = uuid.toLowerCase(Locale.ROOT);
                                    }
                                    fsEntry.release();
                                }
                                fsIter.release();
                            }
                        }
                    }

                    fsList.add(new MacOSFileStore(name, volume, name, path, options.toString(),
                            uuid == null ? Normal.EMPTY : uuid, Normal.EMPTY, description, type, file.getFreeSpace(),
                            file.getUsableSpace(), file.getTotalSpace(), fs[f].f_ffree, fs[f].f_files));
                }
                daVolumeNameKey.release();
                // Close DA session
                session.release();
            }
        }
        return fsList;
    }

    // Called by MacOSFileStore
    static List<OSFileStore> getFileStoreMatching(String nameToMatch) {
        return getFileStoreMatching(nameToMatch, false);
    }

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        // List of file systems
        return getFileStoreMatching(null, localOnly);
    }

    @Override
    public long getOpenFileDescriptors() {
        return SysctlKit.sysctl("kern.num_files", 0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return SysctlKit.sysctl("kern.maxfiles", 0);
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        return SysctlKit.sysctl("kern.maxfilesperproc", 0);
    }

}
