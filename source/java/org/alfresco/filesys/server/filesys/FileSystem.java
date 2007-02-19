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
 * Filesystem Attributes Class
 * <p>
 * Contains constant attributes used to define filesystem features available. The values are taken
 * from the SMB/CIFS protocol query filesystem call.
 */
public final class FileSystem
{

    // Filesystem attributes

    public static final int CaseSensitiveSearch = 0x00000001;
    public static final int CasePreservedNames 	= 0x00000002;
    public static final int UnicodeOnDisk 		= 0x00000004;
    public static final int PersistentACLs 		= 0x00000008;
    public static final int FileCompression 	= 0x00000010;
    public static final int VolumeQuotas 		= 0x00000020;
    public static final int SparseFiles 		= 0x00000040;
    public static final int ReparsePoints 		= 0x00000080;
    public static final int RemoteStorage 		= 0x00000100;
    public static final int LFNAPISupport		= 0x00004000;
    public static final int VolumeIsCompressed 	= 0x00008000;
    public static final int ObjectIds 			= 0x00010000;
    public static final int Encryption 			= 0x00020000;
    
    // Filesystem type strings
    
    public static final String TypeFAT  = "FAT";
    public static final String TypeNTFS = "NTFS";
}
