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
package org.miaixz.bus.health.unix.platform.openbsd.software;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractFileSystem;
import org.miaixz.bus.health.unix.platform.openbsd.OpenBsdSysctlKit;

/**
 * The FreeBSD File System contains {@link OSFileStore}s which are a storage pool, device, partition, volume, concrete
 * file system or other implementation specific means of file storage.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class OpenBsdFileSystem extends AbstractFileSystem {

    private static final List<PathMatcher> FS_PATH_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_OPENBSD_FS_PATH_EXCLUDES);
    private static final List<PathMatcher> FS_PATH_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_OPENBSD_FS_PATH_INCLUDES);
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_OPENBSD_FS_VOLUME_EXCLUDES);
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_OPENBSD_FS_VOLUME_INCLUDES);

    private static List<OSFileStore> getFileStoreMatching(String nameToMatch, boolean localOnly) {
        List<OSFileStore> fsList = new ArrayList<>();

        // Get inode usage data
        Map<String, Long> inodeFreeMap = new HashMap<>();
        Map<String, Long> inodeUsedlMap = new HashMap<>();
        String command = "df -i" + (localOnly ? " -l" : Normal.EMPTY);
        for (String line : Executor.runNative(command)) {
            /*- Sample Output:
             $ df -i
            Filesystem  512-blocks      Used     Avail Capacity iused   ifree  %iused  Mounted on
            /dev/wd0a      2149212    908676   1133076    45%    8355  147163     5%   /
            /dev/wd0e      4050876        36   3848300     0%      10  285108     0%   /home
            /dev/wd0d      6082908   3343172   2435592    58%   27813  386905     7%   /usr
            */
            if (line.startsWith("/")) {
                String[] split = Pattern.SPACES_PATTERN.split(line);
                if (split.length > 6) {
                    inodeUsedlMap.put(split[0], Parsing.parseLongOrDefault(split[5], 0L));
                    inodeFreeMap.put(split[0], Parsing.parseLongOrDefault(split[6], 0L));
                }
            }
        }

        // Get mount table
        for (String fs : Executor.runNative("mount -v")) { // NOSONAR squid:S135
            /*-
             Sample Output:
             /dev/wd0a (d1c342b6965d372c.a) on / type ffs (rw, local, ctime=Sun Jan  3 18:03:00 2021)
             /dev/wd0e (d1c342b6965d372c.e) on /home type ffs (rw, local, nodevl, nosuid, ctime=Sun Jan  3 18:02:56 2021)
             /dev/wd0d (d1c342b6965d372c.d) on /usr type ffs (rw, local, nodev, wxallowed, ctime=Sun Jan  3 18:02:56 2021)
             */
            String[] split = Pattern.SPACES_PATTERN.split(fs, 7);
            if (split.length == 7) {
                // 1st field is volume name [0-index] + partition letter
                // 2nd field is disklabel UUID (DUID) + partition letter after the dot
                // 4th field is mount point
                // 6rd field is fs type
                // 7th field is options
                String volume = split[0];
                String uuid = split[1];
                String path = split[3];
                String type = split[5];
                String options = split[6];

                // Skip non-local drives if requested, and exclude pseudo file systems
                if ((localOnly && NETWORK_FS_TYPES.contains(type))
                        || !path.equals("/") && (PSEUDO_FS_TYPES.contains(type) || Builder.isFileStoreExcluded(path,
                                volume, FS_PATH_INCLUDES, FS_PATH_EXCLUDES, FS_VOLUME_INCLUDES, FS_VOLUME_EXCLUDES))) {
                    continue;
                }

                String name = path.substring(path.lastIndexOf('/') + 1);
                // Special case for /, pull last element of volume instead
                if (name.isEmpty()) {
                    name = volume.substring(volume.lastIndexOf('/') + 1);
                }

                if (nameToMatch != null && !nameToMatch.equals(name)) {
                    continue;
                }
                File f = new File(path);
                long totalSpace = f.getTotalSpace();
                long usableSpace = f.getUsableSpace();
                long freeSpace = f.getFreeSpace();

                String description;
                if (volume.startsWith("/dev") || path.equals("/")) {
                    description = "Local Disk";
                } else if (volume.equals("tmpfs")) {
                    // dynamic size in memory FS
                    description = "Ram Disk (dynamic)";
                } else if (volume.equals("mfs")) {
                    // fixed size in memory FS
                    description = "Ram Disk (fixed)";
                } else if (NETWORK_FS_TYPES.contains(type)) {
                    description = "Network Disk";
                } else {
                    description = "Mount Point";
                }

                fsList.add(new OpenBsdOSFileStore(name, volume, name, path, options, uuid, Normal.EMPTY, description,
                        type, freeSpace, usableSpace, totalSpace, inodeFreeMap.getOrDefault(volume, 0L),
                        inodeUsedlMap.getOrDefault(volume, 0L) + inodeFreeMap.getOrDefault(volume, 0L)));
            }
        }
        return fsList;
    }

    // Called by OpenBsdOSFileStore
    static List<OSFileStore> getFileStoreMatching(String nameToMatch) {
        return getFileStoreMatching(nameToMatch, false);
    }

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        return getFileStoreMatching(null, localOnly);
    }

    @Override
    public long getOpenFileDescriptors() {
        return OpenBsdSysctlKit.sysctl("kern.nfiles", 0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return OpenBsdSysctlKit.sysctl("kern.maxfiles", 0);
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        return OpenBsdSysctlKit.sysctl("kern.maxfilesperproc", 0);
    }

}
