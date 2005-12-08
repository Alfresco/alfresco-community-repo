/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.smb.server;

import org.alfresco.filesys.server.filesys.DiskInfo;
import org.alfresco.filesys.server.filesys.SrvDiskInfo;
import org.alfresco.filesys.server.filesys.VolumeInfo;
import org.alfresco.filesys.smb.NTTime;
import org.alfresco.filesys.util.DataBuffer;
import org.alfresco.filesys.util.DataPacker;

/**
 * Disk information packer class.
 */
class DiskInfoPacker
{

    // Disk information levels

    public static final int InfoStandard = 1;
    public static final int InfoVolume = 2;
    public static final int InfoFsVolume = 0x102;
    public static final int InfoFsSize = 0x103;
    public static final int InfoFsDevice = 0x104;
    public static final int InfoFsAttribute = 0x105;
    public static final int InfoCifsUnix = 0x200;
    public static final int InfoMacFsInfo = 0x301;
    public static final int InfoFullFsSize = 0x3EF;

    // Mac support flags

    public static final int MacAccessControl = 0x0010;
    public static final int MacGetSetComments = 0x0020;
    public static final int MacDesktopDbCalls = 0x0040;
    public static final int MacUniqueIds = 0x0080;
    public static final int MacNoStreamsOrMacSupport = 0x0100;

    /**
     * Class constructor.
     */
    public DiskInfoPacker()
    {
        super();
    }

    /**
     * Pack the standard disk information, InfoStandard.
     * 
     * @param info SMBDiskInfo to be packed.
     * @param buf Buffer to pack the data into.
     */
    public final static void packStandardInfo(DiskInfo info, DataBuffer buf)
    {

        // Information format :-
        // ULONG File system identifier, always 0 ?
        // ULONG Sectors per allocation unit.
        // ULONG Total allocation units.
        // ULONG Total available allocation units.
        // USHORT Number of bytes per sector.

        // Pack the file system identifier, 0 = NT file system

        buf.putZeros(4);
        // buf.putInt(999);

        // Pack the disk unit information

        buf.putInt(info.getBlocksPerAllocationUnit());
        buf.putInt((int) info.getTotalUnits());
        buf.putInt((int) info.getFreeUnits());
        buf.putShort(info.getBlockSize());
    }

    /**
     * Pack the volume label information, InfoVolume.
     * 
     * @param info Volume information
     * @param buf Buffer to pack data into.
     * @param uni Use Unicode strings if true, else use ASCII strings
     */
    public final static void packVolumeInfo(VolumeInfo info, DataBuffer buf, boolean uni)
    {

        // Information format :-
        // ULONG Volume serial number
        // UCHAR Volume label length
        // STRING Volume label

        // Pack the volume serial number

        buf.putInt(info.getSerialNumber());

        // Pack the volume label length and string

        buf.putByte(info.getVolumeLabel().length());
        buf.putString(info.getVolumeLabel(), uni);
    }

    /**
     * Pack the filesystem size information, InfoFsSize
     * 
     * @param info Disk size information
     * @param buf Buffer to pack data into.
     */
    public final static void packFsSizeInformation(SrvDiskInfo info, DataBuffer buf)
    {

        // Information format :-
        // ULONG Disk size (in units)
        // ULONG Free size (in units)
        // UINT Unit size in blocks
        // UINT Block size in bytes

        buf.putLong(info.getTotalUnits());
        buf.putLong(info.getFreeUnits());
        buf.putInt(info.getBlocksPerAllocationUnit());
        buf.putInt(info.getBlockSize());
    }

    /**
     * Pack the filesystem volume information, InfoFsVolume
     * 
     * @param info Volume information
     * @param buf Buffer to pack data into.
     * @param uni Use Unicode strings if true, else use ASCII strings
     */
    public final static void packFsVolumeInformation(VolumeInfo info, DataBuffer buf, boolean uni)
    {

        // Information format :-
        // ULONG Volume creation date/time (NT 64bit time fomat)
        // UINT Volume serial number
        // UINT Volume label length
        // SHORT Reserved
        // STRING Volume label (no null)

        if (info.hasCreationDateTime())
            buf.putLong(NTTime.toNTTime(info.getCreationDateTime()));
        else
            buf.putZeros(8);

        if (info.hasSerialNumber())
            buf.putInt(info.getSerialNumber());
        else
            buf.putZeros(4);

        int len = info.getVolumeLabel().length();
        if (uni)
            len *= 2;
        buf.putInt(len);

        buf.putZeros(2); // reserved
        buf.putString(info.getVolumeLabel(), uni, false);
    }

    /**
     * Pack the filesystem device information, InfoFsDevice
     * 
     * @param typ Device type
     * @param devChar Device characteristics
     * @param buf Buffer to pack data into.
     */
    public final static void packFsDevice(int typ, int devChar, DataBuffer buf)
    {

        // Information format :-
        // UINT Device type
        // UINT Characteristics

        buf.putInt(typ);
        buf.putInt(devChar);
    }

    /**
     * Pack the filesystem attribute information, InfoFsAttribute
     * 
     * @param attr Attribute flags
     * @param maxName Maximum file name component length
     * @param fsType File system type name
     * @param uni Unicode strings required
     * @param buf Buffer to pack data into.
     */
    public final static void packFsAttribute(int attr, int maxName, String fsType, boolean uni, DataBuffer buf)
    {

        // Information format :-
        // UINT Attribute flags
        // UINT Maximum filename component length (usually 255)
        // UINT Filesystem type length
        // STRING Filesystem type string

        buf.putInt(attr);
        buf.putInt(maxName);

        if (uni)
            buf.putInt(fsType.length() * 2);
        else
            buf.putInt(fsType.length());
        buf.putString(fsType, uni, false);
    }

    /**
     * Pack the Mac filesystem information, InfoMacFsInfo
     * 
     * @param diskInfo SMBDiskInfo to be packed.
     * @param volInfo Volume information to be packed
     * @param ntfs Filesystem supports NTFS streams
     * @param buf Buffer to pack the data into.
     */
    public final static void packMacFsInformation(DiskInfo diskInfo, VolumeInfo volInfo, boolean ntfs, DataBuffer buf)
    {

        // Information format :-
        // LARGE_INTEGER Volume creation time (NT format)
        // LARGE_INTEGER Volume modify time (NT format)
        // LARGE_INTEGER Volume backup time (NT format)
        // ULONG Allocation blocks
        // ULONG Allocation block size (multiple of 512)
        // ULONG Free blocks on the volume
        // UCHAR[32] Finder info
        // LONG Number of files in root directory (zero if unknown)
        // LONG Number of directories in the root directory (zero if unknown)
        // LONG Number of files on the volume (zero if unknown)
        // LONG Number of directories on the volume (zero if unknown)
        // LONG Mac support flags (big endian)

        // Pack the volume creation time

        if (volInfo.hasCreationDateTime())
        {
            long ntTime = NTTime.toNTTime(volInfo.getCreationDateTime());
            buf.putLong(ntTime);
            buf.putLong(ntTime);
            buf.putLong(ntTime);
        }
        else
            buf.putZeros(24);

        // Pack the number of allocation blocks, block size and free block count

        buf.putInt((int) diskInfo.getTotalUnits());
        buf.putInt(diskInfo.getBlockSize() * diskInfo.getBlocksPerAllocationUnit());
        buf.putInt((int) diskInfo.getFreeUnits());

        // Pack the finder information area

        buf.putZeros(32);

        // Pack the file/directory counts

        buf.putInt(0);
        buf.putInt(0);
        buf.putInt(0);
        buf.putInt(0);

        // Pack the Mac support flags

        DataPacker.putIntelInt(ntfs ? 0 : MacNoStreamsOrMacSupport, buf.getBuffer(), buf.getPosition());
        buf.setPosition(buf.getPosition() + 4);
    }

    /**
     * Pack the filesystem size information, InfoFsSize
     * 
     * @param userLimit User free units
     * @param info Disk size information
     * @param buf Buffer to pack data into.
     */
    public final static void packFullFsSizeInformation(long userLimit, SrvDiskInfo info, DataBuffer buf)
    {

        // Information format :-
        // ULONG Disk size (in units)
        // ULONG User free size (in units)
        // ULONG Free size (in units)
        // UINT Unit size in blocks
        // UINT Block size in bytes

        buf.putLong(info.getTotalUnits());
        buf.putLong(userLimit);
        buf.putLong(info.getFreeUnits());
        buf.putInt(info.getBlocksPerAllocationUnit());
        buf.putInt(info.getBlockSize());
    }
}