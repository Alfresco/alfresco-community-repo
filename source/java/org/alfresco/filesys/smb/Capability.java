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
package org.alfresco.filesys.smb;

/**
 * SMB Capabilities Class
 * <p>
 * Contains the capability flags for the client/server during a session setup.
 * 
 * @author GKSpencer
 */
public class Capability
{
    // Capabilities

    public static final int RawMode         = 0x00000001;
    public static final int MpxMode         = 0x00000002;
    public static final int Unicode         = 0x00000004;
    public static final int LargeFiles      = 0x00000008;
    public static final int NTSMBs          = 0x00000010;
    public static final int RemoteAPIs      = 0x00000020;
    public static final int NTStatus        = 0x00000040;
    public static final int Level2Oplocks   = 0x00000080;
    public static final int LockAndRead     = 0x00000100;
    public static final int NTFind          = 0x00000200;
    public static final int DFS             = 0x00001000;
    public static final int InfoPassthru    = 0x00002000;
    public static final int LargeRead       = 0x00004000;
    public static final int LargeWrite      = 0x00008000;
    public static final int UnixExtensions  = 0x00800000;
    public static final int BulkTransfer    = 0x20000000;
    public static final int CompressedData  = 0x40000000;
    public static final int ExtendedSecurity = 0x80000000;
}
