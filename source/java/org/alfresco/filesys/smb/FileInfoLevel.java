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
package org.alfresco.filesys.smb;

/**
 * File Information Levels class. This class contains the file information levels that may be
 * requested in the various Transact2 requests.
 */
public class FileInfoLevel
{

    // Find first/next information levels

    public static final int FindStandard =          0x0001;
    public static final int FindQueryEASize =       0x0002;
    public static final int FindQueryEAsList =      0x0003;
    public static final int FindFileDirectory =     0x0101;
    public static final int FindFileFullDirectory = 0x0102;
    public static final int FindFileNames =         0x0103;
    public static final int FindFileBothDirectory = 0x0104;

    // File information levels

    public static final int SetStandard =           0x0001;
    public static final int SetQueryEASize =        0x0002;
    public static final int SetBasicInfo =          0x0101;
    public static final int SetDispositionInfo =    0x0102;
    public static final int SetAllocationInfo =     0x0103;
    public static final int SetEndOfFileInfo =      0x0104;

    // Query path information levels

    public static final int PathStandard =          0x0001;
    public static final int PathQueryEASize =       0x0002;
    public static final int PathQueryEAsFromList =  0x0003;
    public static final int PathAllEAs =            0x0004;
    public static final int PathIsNameValid =       0x0006;
    public static final int PathFileBasicInfo =     0x0101;
    public static final int PathFileStandardInfo =  0x0102;
    public static final int PathFileEAInfo =        0x0103;
    public static final int PathFileNameInfo =      0x0104;
    public static final int PathFileAllInfo =       0x0107;
    public static final int PathFileAltNameInfo =   0x0108;
    public static final int PathFileStreamInfo =    0x0109;
    public static final int PathFileCompressionInfo = 0x010B;

    // Filesystem query information levels

    public static final int FSInfoAllocation =      0x0001;
    public static final int FSInfoVolume =          0x0002;
    public static final int FSInfoQueryVolume =     0x0102;
    public static final int FSInfoQuerySize =       0x0103;
    public static final int FSInfoQueryDevice =     0x0104;
    public static final int FSInfoQueryAttribute =  0x0105;

    // NT pasthru levels

    public static final int NTFileBasicInfo =       1004;
    public static final int NTFileStandardInfo =    1005;
    public static final int NTFileInternalInfo =    1006;
    public static final int NTFileEAInfo =          1007;
    public static final int NTFileAccessInfo =      1008;
    public static final int NTFileNameInfo =        1009;
    public static final int NTFileRenameInfo =      1010;
    public static final int NTFileDispositionInfo = 1013;
    public static final int NTFilePositionInfo =    1014;
    public static final int NTFileModeInfo =        1016;
    public static final int NTFileAlignmentInfo =   1017;
    public static final int NTFileAllInfo =         1018;
    public static final int NTFileAltNameInfo =     1021;
    public static final int NTFileStreamInfo =      1022;
    public static final int NTFileCompressionInfo = 1028;
    public static final int NTNetworkOpenInfo =     1034;
    public static final int NTAttributeTagInfo =    1035;
}