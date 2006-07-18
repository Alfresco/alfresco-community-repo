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
