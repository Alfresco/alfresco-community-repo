/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb.server;

import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.UnsupportedInfoLevelException;
import org.alfresco.filesys.smb.NTTime;
import org.alfresco.filesys.smb.SMBDate;
import org.alfresco.filesys.util.DataBuffer;

/**
 * Find Information Packer Class
 * <p>
 * Pack file information for a find first/find next information level.
 */
class FindInfoPacker
{

    // Enable 8.3 name generation (required for Mac OS9)

    private static final boolean Enable8Dot3Names = false;

    // Enable packing of file id

    private static final boolean EnableFileIdPacking = false;

    // File information levels

    public static final int InfoStandard 			= 1;
    public static final int InfoQueryEASize 		= 2;
    public static final int InfoQueryEAFromList 	= 3;
    public static final int InfoDirectory 			= 0x101;
    public static final int InfoFullDirectory 		= 0x102;
    public static final int InfoNames 				= 0x103;
    public static final int InfoDirectoryBoth 		= 0x104;
    public static final int InfoMacHfsInfo 			= 0x302;

    // File information fixed lengths, includes nulls on strings.

    public static final int InfoStandardLen 		= 24;
    public static final int InfoQueryEASizeLen 		= 28;
    public static final int InfoDirectoryLen 		= 64;
    public static final int InfoFullDirectoryLen 	= 68;
    public static final int InfoNamesLen 			= 12;
    public static final int InfoDirectoryBothLen 	= 94;
    public static final int InfoMacHfsLen 			= 120;

    /**
     * Pack a file information object into the specified buffer, using information level 1 format.
     * 
     * @param info File information to be packed.
     * @param buf Data buffer to pack the file information into
     * @param infoLevel File information level.
     * @param uni Pack Unicode strings if true, else pack ASCII strings
     * @return Length of data packed
     */
    public final static int packInfo(FileInfo info, DataBuffer buf, int infoLevel, boolean uni)
            throws UnsupportedInfoLevelException
    {

        // Determine the information level

        int curPos = buf.getPosition();

        switch (infoLevel)
        {

        // Standard information

        case InfoStandard:
            packInfoStandard(info, buf, false, uni);
            break;

        // Standard information + EA list size

        case InfoQueryEASize:
            packInfoStandard(info, buf, true, uni);
            break;

        // File name information

        case InfoNames:
            packInfoFileName(info, buf, uni);
            break;

        // File/directory information

        case InfoDirectory:
            packInfoDirectory(info, buf, uni);
            break;

        // Full file/directory information

        case InfoFullDirectory:
            packInfoDirectoryFull(info, buf, uni);
            break;

        // Full file/directory information with short name

        case InfoDirectoryBoth:
            packInfoDirectoryBoth(info, buf, uni);
            break;

        // Pack Macintosh format file information

        case InfoMacHfsInfo:
            packInfoMacHfs(info, buf, uni);
            break;
        }

        // Check if we packed any data

        if (curPos == buf.getPosition())
            throw new UnsupportedInfoLevelException();

        // Return the length of the packed data

        return buf.getPosition() - curPos;
    }

    /**
     * Calculate the file name offset for the specified information level.
     * 
     * @param infoLev int
     * @param offset int
     * @return int
     */
    public final static int calcFileNameOffset(int infoLev, int offset)
    {

        // Determine the information level

        int pos = offset;

        switch (infoLev)
        {

        // Standard information level

        case InfoStandard:
            pos += InfoStandard;
            break;

        // Standard + EA size

        case InfoQueryEASize:
            pos += InfoQueryEASizeLen;
            break;

        // File name information

        case InfoNames:
            pos += InfoNamesLen;
            break;

        // File/directory information

        case InfoDirectory:
            pos += InfoDirectoryLen;
            break;

        // File/directory information full

        case InfoFullDirectory:
            pos += InfoFullDirectoryLen;
            break;

        // Full file/directory information full plus short name

        case InfoDirectoryBoth:
            pos += InfoDirectoryBothLen;
            break;
        }

        // Return the file name offset

        return pos;
    }

    /**
     * Calculate the required buffer space for the file information at the specified file
     * information level.
     * 
     * @param info File information
     * @param infoLev File information level requested.
     * @param resKey true if resume keys are being returned, else false.
     * @param uni true if Unicode strings are being used, or false for ASCII strings
     * @return int Buffer space required, or -1 if unknown information level.
     */
    public final static int calcInfoSize(FileInfo info, int infoLev, boolean resKey, boolean uni)
    {

        // Determine the information level requested

        int len = -1;
        int nameLen = info.getFileName().length() + 1;
        if (uni)
            nameLen *= 2;

        switch (infoLev)
        {

        // Standard information level

        case InfoStandard:
            len = InfoStandardLen + nameLen;
            break;

        // Standard + EA size

        case InfoQueryEASize:
            len = InfoQueryEASizeLen + nameLen;
            break;

        // File name information

        case InfoNames:
            len += InfoNamesLen + nameLen;
            break;

        // File/directory information

        case InfoDirectory:
            len = InfoDirectoryLen + nameLen;
            break;

        // File/directory information full

        case InfoFullDirectory:
            len += InfoFullDirectoryLen + nameLen;
            break;

        // Full file/directory information plus short name

        case InfoDirectoryBoth:
            len = InfoDirectoryBothLen + nameLen;
            break;

        // Maacintosh information level

        case InfoMacHfsInfo:
            len = InfoMacHfsLen + nameLen;
            break;
        }

        // Add extra space for the resume key, if enabled

        if (resKey)
            len += 4;

        // Return the buffer length required.

        return len;
    }

    /**
     * Clear the next structure offset
     * 
     * @param dataBuf DataBuffer
     * @param level int
     * @param offset int
     */
    public static final void clearNextOffset(DataBuffer buf, int level, int offset)
    {

        // Standard information level does not have a next entry offset

        if (level == InfoStandard)
            return;

        // Clear the next entry offset

        int curPos = buf.getPosition();
        buf.setPosition(offset);
        buf.putInt(0);
        buf.setPosition(curPos);
    }

    /**
     * Pack a file information object into the specified buffer. Use the standard information level
     * if the EA size flag is false, else add the EA size field.
     * 
     * @param info File information to be packed.
     * @param buf Buffer to pack the data into.
     * @param EAflag Add EA size field if true.
     * @param uni Pack Unicode strings if true, else pack ASCII strings
     */
    protected final static void packInfoStandard(FileInfo info, DataBuffer buf, boolean EAflag, boolean uni)
    {

        // Information format :-
        // SMB_DATE CreationDate
        // SMB_TIME CreationTime
        // SMB_DATE LastAccessDate
        // SMB_TIME LastAccessTime
        // SMB_DATE LastWriteDate
        // SMB_TIME LastWriteTime
        // ULONG File size
        // ULONG Allocation size
        // USHORT File attributes
        // [ ULONG EA size ]
        // UCHAR File name length
        // STRING File name, null terminated

        // Pack the creation date/time

        SMBDate date = new SMBDate(0);

        if (info.hasCreationDateTime())
        {
            date.setTime(info.getCreationDateTime());
            buf.putShort(date.asSMBDate());
            buf.putShort(date.asSMBTime());
        }
        else
            buf.putZeros(4);

        // Pack the last access date/time

        if (info.hasAccessDateTime())
        {
            date.setTime(info.getAccessDateTime());
            buf.putShort(date.asSMBDate());
            buf.putShort(date.asSMBTime());
        }
        else
            buf.putZeros(4);

        // Pack the last write date/time

        if (info.hasModifyDateTime())
        {
            date.setTime(info.getModifyDateTime());
            buf.putShort(date.asSMBDate());
            buf.putShort(date.asSMBTime());
        }
        else
            buf.putZeros(4);

        // Pack the file size and allocation size

        buf.putInt(info.getSizeInt());

        if (info.getAllocationSize() < info.getSize())
            buf.putInt(info.getSizeInt());
        else
            buf.putInt(info.getAllocationSizeInt());

        // Pack the file attributes

        buf.putShort(info.getFileAttributes());

        // Pack the EA size, always zero

        if (EAflag)
            buf.putInt(0);

        // Pack the file name

        if (uni == true)
        {

            // Pack the number of bytes followed by the Unicode name word aligned

            buf.putByte(info.getFileName().length() * 2);
            buf.wordAlign();
            buf.putString(info.getFileName(), uni, true);
        }
        else
        {

            // Pack the number of bytes followed by the ASCII name

            buf.putByte(info.getFileName().length());
            buf.putString(info.getFileName(), uni, true);
        }
    }

    /**
     * Pack the file name information
     * 
     * @param info File information to be packed.
     * @param buf Buffer to pack the data into.
     * @param uni Pack Unicode strings if true, else pack ASCII strings
     */
    protected final static void packInfoFileName(FileInfo info, DataBuffer buf, boolean uni)
    {

        // Information format :-
        // ULONG NextEntryOffset
        // ULONG FileIndex
        // ULONG FileNameLength
        // STRING FileName

        // Pack the file id

        int startPos = buf.getPosition();
        buf.putZeros(4);
        buf.putInt(EnableFileIdPacking ? info.getFileId() : 0);

        // Pack the file name length

        int nameLen = info.getFileName().length();
        if (uni)
            nameLen *= 2;

        buf.putInt(nameLen);

        // Pack the long file name string

        buf.putString(info.getFileName(), uni, false);

        // Align the buffer pointer and set the offset to the next file information entry

        buf.wordAlign();

        int curPos = buf.getPosition();
        buf.setPosition(startPos);
        buf.putInt(curPos - startPos);
        buf.setPosition(curPos);
    }

    /**
     * Pack the file/directory information
     * 
     * @param info File information to be packed.
     * @param buf Buffer to pack the data into.
     * @param uni Pack Unicode strings if true, else pack ASCII strings
     */
    protected final static void packInfoDirectory(FileInfo info, DataBuffer buf, boolean uni)
    {

        // Information format :-
        // ULONG NextEntryOffset
        // ULONG FileIndex
        // LARGE_INTEGER CreationTime
        // LARGE_INTEGER LastAccessTime
        // LARGE_INTEGER LastWriteTime
        // LARGE_INTEGER ChangeTime
        // LARGE_INTEGER EndOfFile
        // LARGE_INTEGER AllocationSize
        // ULONG FileAttributes
        // ULONG FileNameLength
        // STRING FileName

        // Pack the file id

        int startPos = buf.getPosition();
        buf.putZeros(4);
        buf.putInt(EnableFileIdPacking ? info.getFileId() : 0);

        // Pack the creation date/time

        if (info.hasCreationDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getCreationDateTime()));
        }
        else
            buf.putZeros(8);

        // Pack the last access date/time

        if (info.hasAccessDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getAccessDateTime()));
        }
        else
            buf.putZeros(8);

        // Pack the last write date/time and change time

        if (info.hasModifyDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getModifyDateTime()));
            buf.putLong(NTTime.toNTTime(info.getModifyDateTime()));
        }
        else
            buf.putZeros(16);

        // Pack the file size and allocation size

        buf.putLong(info.getSize());

        if (info.getAllocationSize() < info.getSize())
            buf.putLong(info.getSize());
        else
            buf.putLong(info.getAllocationSize());

        // Pack the file attributes

        buf.putInt(info.getFileAttributes());

        // Pack the file name length

        int nameLen = info.getFileName().length();
        if (uni)
            nameLen *= 2;

        buf.putInt(nameLen);

        // Pack the long file name string

        buf.putString(info.getFileName(), uni, false);

        // Align the buffer pointer and set the offset to the next file information entry

        buf.wordAlign();

        int curPos = buf.getPosition();
        buf.setPosition(startPos);
        buf.putInt(curPos - startPos);
        buf.setPosition(curPos);
    }

    /**
     * Pack the full file/directory information
     * 
     * @param info File information to be packed.
     * @param buf Buffer to pack the data into.
     * @param uni Pack Unicode strings if true, else pack ASCII strings
     */
    protected final static void packInfoDirectoryFull(FileInfo info, DataBuffer buf, boolean uni)
    {

        // Information format :-
        // ULONG NextEntryOffset
        // ULONG FileIndex
        // LARGE_INTEGER CreationTime
        // LARGE_INTEGER LastAccessTime
        // LARGE_INTEGER LastWriteTime
        // LARGE_INTEGER ChangeTime
        // LARGE_INTEGER EndOfFile
        // LARGE_INTEGER AllocationSize
        // ULONG FileAttributes
        // ULONG FileNameLength
        // ULONG EaSize
        // STRING FileName

        // Pack the file id

        int startPos = buf.getPosition();
        buf.putZeros(4);
        buf.putInt(EnableFileIdPacking ? info.getFileId() : 0);

        // Pack the creation date/time

        if (info.hasCreationDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getCreationDateTime()));
        }
        else
            buf.putZeros(8);

        // Pack the last access date/time

        if (info.hasAccessDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getAccessDateTime()));
        }
        else
            buf.putZeros(8);

        // Pack the last write date/time

        if (info.hasModifyDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getModifyDateTime()));
            buf.putLong(NTTime.toNTTime(info.getModifyDateTime()));
        }
        else
            buf.putZeros(16);

        // Pack the file size and allocation size

        buf.putLong(info.getSize());

        if (info.getAllocationSize() < info.getSize())
            buf.putLong(info.getSize());
        else
            buf.putLong(info.getAllocationSize());

        // Pack the file attributes

        buf.putInt(info.getFileAttributes());

        // Pack the file name length

        int nameLen = info.getFileName().length();
        if (uni)
            nameLen *= 2;

        buf.putInt(nameLen);

        // Pack the EA size

        buf.putZeros(4);

        // Pack the long file name string

        buf.putString(info.getFileName(), uni, false);

        // Align the buffer pointer and set the offset to the next file information entry

        buf.wordAlign();

        int curPos = buf.getPosition();
        buf.setPosition(startPos);
        buf.putInt(curPos - startPos);
        buf.setPosition(curPos);
    }

    /**
     * Pack the full file/directory information
     * 
     * @param info File information to be packed.
     * @param buf Buffer to pack the data into.
     * @param uni Pack Unicode strings if true, else pack ASCII strings
     */
    protected final static void packInfoDirectoryBoth(FileInfo info, DataBuffer buf, boolean uni)
    {

        // Information format :-
        // ULONG NextEntryOffset
        // ULONG FileIndex
        // LARGE_INTEGER CreationTime
        // LARGE_INTEGER LastAccessTime
        // LARGE_INTEGER LastWriteTime
        // LARGE_INTEGER ChangeTime
        // LARGE_INTEGER EndOfFile
        // LARGE_INTEGER AllocationSize
        // ULONG FileAttributes
        // ULONG FileNameLength
        // ULONG EaSize
        // UCHAR ShortNameLength
        // WCHAR ShortName[12]
        // STRING FileName

        // Pack the file id

        int startPos = buf.getPosition();
        buf.putZeros(4);
        buf.putInt(EnableFileIdPacking ? info.getFileId() : 0);

        // Pack the creation date/time

        if (info.hasCreationDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getCreationDateTime()));
        }
        else
            buf.putZeros(8);

        // Pack the last access date/time

        if (info.hasAccessDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getAccessDateTime()));
        }
        else
            buf.putZeros(8);

        // Pack the last write date/time and change time

        if (info.hasModifyDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getModifyDateTime()));
            buf.putLong(NTTime.toNTTime(info.getModifyDateTime()));
        }
        else
            buf.putZeros(16);

        // Pack the file size and allocation size

        buf.putLong(info.getSize());

        if (info.getAllocationSize() < info.getSize())
            buf.putLong(info.getSize());
        else
            buf.putLong(info.getAllocationSize());

        // Pack the file attributes

        buf.putInt(info.getFileAttributes());

        // Pack the file name length

        int nameLen = info.getFileName().length();
        if (uni)
            nameLen *= 2;

        buf.putInt(nameLen);

        // Pack the EA size

        buf.putZeros(4);

        // Pack the short file name length (8.3 name)

        pack8Dot3Name(buf, info.getFileName(), uni);

        // Pack the long file name string

        buf.putString(info.getFileName(), uni, false);

        // Align the buffer pointer and set the offset to the next file information entry

        buf.wordAlign();

        int curPos = buf.getPosition();
        buf.setPosition(startPos);
        buf.putInt(curPos - startPos);
        buf.setPosition(curPos);
    }

    /**
     * Pack the Macintosh format file/directory information
     * 
     * @param info File information to be packed.
     * @param buf Buffer to pack the data into.
     * @param uni Pack Unicode strings if true, else pack ASCII strings
     */
    protected final static void packInfoMacHfs(FileInfo info, DataBuffer buf, boolean uni)
    {

        // Information format :-
        // ULONG NextEntryOffset
        // ULONG FileIndex
        // LARGE_INTEGER CreationTime
        // LARGE_INTEGER LastWriteTime
        // LARGE_INTEGER ChangeTime
        // LARGE_INTEGER Data stream length
        // LARGE_INTEGER Resource stream length
        // LARGE_INTEGER Data stream allocation size
        // LARGE_INTEGER Resource stream allocation size
        // ULONG ExtFileAttributes
        // UCHAR FLAttrib Macintosh SetFLock, 1 = file locked
        // UCHAR Pad
        // UWORD DrNmFls Number of items in a directory, zero for files
        // ULONG AccessControl
        // UCHAR FinderInfo[32]
        // ULONG FileNameLength
        // UCHAR ShortNameLength
        // UCHAR Pad
        // WCHAR ShortName[12]
        // STRING FileName
        // LONG UniqueId

        // Pack the file id

        int startPos = buf.getPosition();
        buf.putZeros(4);
        buf.putInt(EnableFileIdPacking ? info.getFileId() : 0);

        // Pack the creation date/time

        if (info.hasCreationDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getCreationDateTime()));
        }
        else
            buf.putZeros(8);

        // Pack the last write date/time and change time

        if (info.hasModifyDateTime())
        {
            buf.putLong(NTTime.toNTTime(info.getModifyDateTime()));
            buf.putLong(NTTime.toNTTime(info.getModifyDateTime()));
        }
        else
            buf.putZeros(16);

        // Pack the data stream size and resource stream size (always zero)

        buf.putLong(info.getSize());
        buf.putZeros(8);

        // Pack the data stream allocation size and resource stream allocation size (always zero)

        if (info.getAllocationSize() < info.getSize())
            buf.putLong(info.getSize());
        else
            buf.putLong(info.getAllocationSize());
        buf.putZeros(8);

        // Pack the file attributes

        buf.putInt(info.getFileAttributes());

        // Pack the file lock and padding byte

        buf.putZeros(2);

        // Pack the number of items in a directory, always zero for now

        buf.putShort(0);

        // Pack the access control

        buf.putInt(0);

        // Pack the finder information

        buf.putZeros(32);

        // Pack the file name length

        int nameLen = info.getFileName().length();
        if (uni)
            nameLen *= 2;

        buf.putInt(nameLen);

        // Pack the short file name length (8.3 name) and name

        pack8Dot3Name(buf, info.getFileName(), uni);

        // Pack the long file name string

        buf.putString(info.getFileName(), uni, false);

        // Pack the unique id

        buf.putInt(0);

        // Align the buffer pointer and set the offset to the next file information entry

        buf.wordAlign();

        int curPos = buf.getPosition();
        buf.setPosition(startPos);
        buf.putInt(curPos - startPos);
        buf.setPosition(curPos);
    }

    /**
     * Pack a file name as a short 8.3 DOS style name. Packs the short name length byte, reserved
     * byte and 8.3 file name string.
     * 
     * @param buf DataBuffer
     * @param fileName String
     * @param uni boolean
     */
    private static final void pack8Dot3Name(DataBuffer buf, String fileName, boolean uni)
    {

        if (Enable8Dot3Names == false)
        {

            // Pack an emty 8.3 name structure

            buf.putZeros(26);
        }
        else
        {

            // Split the file name string into name and extension

            int pos = fileName.lastIndexOf('.');

            String namePart = null;
            String extPart = null;

            if (pos != -1)
            {

                // Split the file name string

                namePart = fileName.substring(0, pos);
                extPart = fileName.substring(pos + 1);
            }
            else
                namePart = fileName;

            // If the name already fits into an 8.3 name we do not need to pack the short name

            if (namePart.length() <= 8 && (extPart == null || extPart.length() <= 3))
            {

                // Pack an emty 8.3 name structure

                buf.putZeros(26);
                return;
            }

            // Truncate the name and extension parts down to 8.3 sizes

            if (namePart.length() > 8)
                namePart = namePart.substring(0, 6) + "~1";

            if (extPart != null && extPart.length() > 3)
                extPart = extPart.substring(0, 3);

            // Build the 8.3 format string

            StringBuffer str = new StringBuffer(16);

            str.append(namePart);
            while (str.length() < 8)
                str.append(" ");

            if (extPart != null)
            {
                str.append(".");
                str.append(extPart);
            }
            else
                str.append("    ");

            // Space pad the string to 12 characters

            while (str.length() < 12)
                str.append(" ");

            // Calculate the used length

            int len = namePart.length();
            if (extPart != null)
                len = extPart.length() + 9;

            len *= 2;

            // Pack the 8.3 file name structure, always packed as Unicode

            buf.putByte(len);
            buf.putByte(0);

            buf.putString(str.toString(), true, false);
        }
    }
}