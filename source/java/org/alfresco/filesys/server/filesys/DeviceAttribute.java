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
package org.alfresco.filesys.server.filesys;

/**
 * Device Attribute Constants Class
 * <p>
 * Specifies the constants that can be used to set the DiskDeviceContext device attributes.
 */
public final class DeviceAttribute
{
    // Device attributes

    public static final int Removable = 0x0001;
    public static final int ReadOnly = 0x0002;
    public static final int FloppyDisk = 0x0004;
    public static final int WriteOnce = 0x0008;
    public static final int Remote = 0x0010;
    public static final int Mounted = 0x0020;
    public static final int Virtual = 0x0040;
}
