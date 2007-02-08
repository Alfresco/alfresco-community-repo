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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.smb;

/**
 * Protocol Class
 * <p>
 * Declares constants for the available SMB protocols (TCP/IP NetBIOS and native TCP/IP SMB)
 */
public class Protocol
{

    // Available protocol types

    public final static int TCPNetBIOS = 1;
    public final static int NativeSMB = 2;

    // Protocol control constants

    public final static int UseDefault = 0;
    public final static int None = -1;

    /**
     * Return the protocol type as a string
     * 
     * @param typ int
     * @return String
     */
    public static final String asString(int typ)
    {
        String ret = "";
        if (typ == TCPNetBIOS)
            ret = "TCP/IP NetBIOS";
        else if (typ == NativeSMB)
            ret = "Native SMB (port 445)";

        return ret;
    }
}
