/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.filesys.server.filesys;

/**
 * SMB file attribute class.
 * <p>
 * Defines various bit masks that may be returned in an FileInfo object, that is returned by the
 * DiskInterface.getFileInformation () and SearchContext.nextFileInfo() methods.
 * <p>
 * The values are also used by the DiskInterface.StartSearch () method to determine the
 * file/directory types that are returned.
 * 
 * @see DiskInterface
 * @see SearchContext
 */
public final class FileAttribute
{

    // Standard file attribute constants

    public static final int Normal = 0x00;
    public static final int ReadOnly = 0x01;
    public static final int Hidden = 0x02;
    public static final int System = 0x04;
    public static final int Volume = 0x08;
    public static final int Directory = 0x10;
    public static final int Archive = 0x20;

    // NT file attribute flags

    public static final int NTReadOnly = 0x00000001;
    public static final int NTHidden = 0x00000002;
    public static final int NTSystem = 0x00000004;
    public static final int NTVolumeId = 0x00000008;
    public static final int NTDirectory = 0x00000010;
    public static final int NTArchive = 0x00000020;
    public static final int NTDevice = 0x00000040;
    public static final int NTNormal = 0x00000080;
    public static final int NTTemporary = 0x00000100;
    public static final int NTSparse = 0x00000200;
    public static final int NTReparsePoint = 0x00000400;
    public static final int NTCompressed = 0x00000800;
    public static final int NTOffline = 0x00001000;
    public static final int NTIndexed = 0x00002000;
    public static final int NTEncrypted = 0x00004000;
    public static final int NTOpenNoRecall = 0x00100000;
    public static final int NTOpenReparsePoint = 0x00200000;
    public static final int NTPosixSemantics = 0x01000000;
    public static final int NTBackupSemantics = 0x02000000;
    public static final int NTDeleteOnClose = 0x04000000;
    public static final int NTSequentialScan = 0x08000000;
    public static final int NTRandomAccess = 0x10000000;
    public static final int NTNoBuffering = 0x20000000;
    public static final int NTOverlapped = 0x40000000;
    public static final int NTWriteThrough = 0x80000000;

    /**
     * Determine if the specified file attribute mask has the specified file attribute enabled.
     * 
     * @return boolean
     * @param attr int
     * @param reqattr int
     */
    public final static boolean hasAttribute(int attr, int reqattr)
    {

        // Check for the specified attribute

        if ((attr & reqattr) != 0)
            return true;
        return false;
    }

    /**
     * Check if the read-only attribute is set
     * 
     * @param attr int
     * @return boolean
     */
    public static final boolean isReadOnly(int attr)
    {
        return (attr & ReadOnly) != 0 ? true : false;
    }

    /**
     * Check if the directory attribute is set
     * 
     * @param attr int
     * @return boolean
     */
    public static final boolean isDirectory(int attr)
    {
        return (attr & Directory) != 0 ? true : false;
    }

    /**
     * Check if the hidden attribute is set
     * 
     * @param attr int
     * @return boolean
     */
    public static final boolean isHidden(int attr)
    {
        return (attr & Hidden) != 0 ? true : false;
    }

    /**
     * Check if the system attribute is set
     * 
     * @param attr int
     * @return boolean
     */
    public static final boolean isSystem(int attr)
    {
        return (attr & System) != 0 ? true : false;
    }

    /**
     * Check if the archive attribute is set
     * 
     * @param attr int
     * @return boolean
     */
    public static final boolean isArchived(int attr)
    {
        return (attr & Archive) != 0 ? true : false;
    }
}