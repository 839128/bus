package org.aoju.bus.health.windows.hardware;


import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.WbemcliUtil;
import com.sun.jna.platform.win32.VersionHelpers;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.builtin.hardware.AbstractLogicalVolumeGroup;
import org.aoju.bus.health.builtin.hardware.LogicalVolumeGroup;
import org.aoju.bus.health.windows.MSFTStorage;
import org.aoju.bus.health.windows.WmiKit;
import org.aoju.bus.health.windows.WmiQueryHandler;
import org.aoju.bus.logger.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class WindowsLogicalVolumeGroup extends AbstractLogicalVolumeGroup {

    private static final Pattern SP_OBJECT_ID = Pattern.compile(".*ObjectId=.*SP:(\\{.*\\}).*");
    private static final Pattern PD_OBJECT_ID = Pattern.compile(".*ObjectId=.*PD:(\\{.*\\}).*");
    private static final Pattern VD_OBJECT_ID = Pattern.compile(".*ObjectId=.*VD:(\\{.*\\})(\\{.*\\}).*");

    private static final boolean IS_WINDOWS8_OR_GREATER = VersionHelpers.IsWindows8OrGreater();

    WindowsLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

    static List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        WmiQueryHandler h = WmiQueryHandler.createInstance();
        boolean comInit = false;
        try {
            comInit = h.initCOM();
            // Query Storage Pools first, so we can skip other queries if we have no pools
            WbemcliUtil.WmiResult<MSFTStorage.StoragePoolProperty> sp = MSFTStorage.queryStoragePools(h);
            int count = sp.getResultCount();
            if (count == 0) {
                return Collections.emptyList();
            }
            // We have storage pool(s) but now need to gather other info

            // Get all the Virtual Disks
            Map<String, String> vdMap = new HashMap<>();
            WbemcliUtil.WmiResult<MSFTStorage.VirtualDiskProperty> vds = MSFTStorage.queryVirtualDisks(h);
            count = vds.getResultCount();
            for (int i = 0; i < count; i++) {
                String vdObjectId = WmiKit.getString(vds, MSFTStorage.VirtualDiskProperty.OBJECTID, i);
                Matcher m = VD_OBJECT_ID.matcher(vdObjectId);
                if (m.matches()) {
                    vdObjectId = m.group(2) + " " + m.group(1);
                }
                // Store key with SP|VD
                vdMap.put(vdObjectId, WmiKit.getString(vds, MSFTStorage.VirtualDiskProperty.FRIENDLYNAME, i));
            }

            // Get all the Physical Disks
            Map<String, Pair<String, String>> pdMap = new HashMap<>();
            WbemcliUtil.WmiResult<MSFTStorage.PhysicalDiskProperty> pds = MSFTStorage.queryPhysicalDisks(h);
            count = pds.getResultCount();
            for (int i = 0; i < count; i++) {
                String pdObjectId = WmiKit.getString(pds, MSFTStorage.PhysicalDiskProperty.OBJECTID, i);
                Matcher m = PD_OBJECT_ID.matcher(pdObjectId);
                if (m.matches()) {
                    pdObjectId = m.group(1);
                }
                // Store key with PD
                pdMap.put(pdObjectId, Pair.of(WmiKit.getString(pds, MSFTStorage.PhysicalDiskProperty.FRIENDLYNAME, i),
                        WmiKit.getString(pds, MSFTStorage.PhysicalDiskProperty.PHYSICALLOCATION, i)));
            }

            // Get the Storage Pool to Physical Disk mappping
            Map<String, String> sppdMap = new HashMap<>();
            WbemcliUtil.WmiResult<MSFTStorage.StoragePoolToPhysicalDiskProperty> sppd = MSFTStorage.queryStoragePoolPhysicalDisks(h);
            count = sppd.getResultCount();
            for (int i = 0; i < count; i++) {
                // Ref string contains object id, will do partial match later
                String spObjectId = WmiKit.getRefString(sppd, MSFTStorage.StoragePoolToPhysicalDiskProperty.STORAGEPOOL, i);
                Matcher m = SP_OBJECT_ID.matcher(spObjectId);
                if (m.matches()) {
                    spObjectId = m.group(1);
                }
                String pdObjectId = WmiKit.getRefString(sppd, MSFTStorage.StoragePoolToPhysicalDiskProperty.PHYSICALDISK, i);
                m = PD_OBJECT_ID.matcher(pdObjectId);
                if (m.matches()) {
                    pdObjectId = m.group(1);
                }
                sppdMap.put(spObjectId + " " + pdObjectId, pdObjectId);
            }

            // Finally process the storage pools
            List<LogicalVolumeGroup> lvgList = new ArrayList<>();
            count = sp.getResultCount();
            for (int i = 0; i < count; i++) {
                // Name
                String name = WmiKit.getString(sp, MSFTStorage.StoragePoolProperty.FRIENDLYNAME, i);
                // Parse object ID to match
                String spObjectId = WmiKit.getString(sp, MSFTStorage.StoragePoolProperty.OBJECTID, i);
                Matcher m = SP_OBJECT_ID.matcher(spObjectId);
                if (m.matches()) {
                    spObjectId = m.group(1);
                }
                // find matching physical and logical volumes
                Set<String> physicalVolumeSet = new HashSet<>();
                for (Map.Entry<String, String> entry : sppdMap.entrySet()) {
                    if (entry.getKey().contains(spObjectId)) {
                        String pdObjectId = entry.getValue();
                        Pair<String, String> nameLoc = pdMap.get(pdObjectId);
                        if (nameLoc != null) {
                            physicalVolumeSet.add(nameLoc.getLeft() + " @ " + nameLoc.getRight());
                        }
                    }
                }
                // find matching logical volume
                Map<String, Set<String>> logicalVolumeMap = new HashMap<>();
                for (Map.Entry<String, String> entry : vdMap.entrySet()) {
                    if (entry.getKey().contains(spObjectId)) {
                        String vdObjectId = RegEx.SPACES.split(entry.getKey())[0];
                        logicalVolumeMap.put(entry.getValue() + " " + vdObjectId, physicalVolumeSet);
                    }
                }
                // Add to list
                lvgList.add(new WindowsLogicalVolumeGroup(name, logicalVolumeMap, physicalVolumeSet));
            }

            return lvgList;
        } catch (COMException e) {
            Logger.warn("COM exception: {}", e.getMessage());
            return Collections.emptyList();
        } finally {
            if (comInit) {
                h.unInitCOM();
            }
        }
    }

}