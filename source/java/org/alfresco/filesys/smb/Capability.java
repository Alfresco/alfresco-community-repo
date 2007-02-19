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
