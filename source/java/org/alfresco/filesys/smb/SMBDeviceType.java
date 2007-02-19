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
 * SMB device types class.
 * <p>
 * The class provides symbols for the remote device types that may be connected to. The values are
 * also used when returning remote share information.
 */
public class SMBDeviceType
{

    // Device type constants

    public static final int Disk = 0;
    public static final int Printer = 1;
    public static final int Comm = 2;
    public static final int Pipe = 3;
    public static final int Unknown = -1;

    /**
     * Convert the device type to a string
     * 
     * @param devtyp Device type
     * @return Device type string
     */
    public static String asString(int devtyp)
    {
        String devStr = null;

        switch (devtyp)
        {
        case Disk:
            devStr = "Disk";
            break;
        case Printer:
            devStr = "Printer";
            break;
        case Pipe:
            devStr = "Pipe";
            break;
        case Comm:
            devStr = "Comm";
            break;
        default:
            devStr = "Unknown";
            break;
        }
        return devStr;
    }
}