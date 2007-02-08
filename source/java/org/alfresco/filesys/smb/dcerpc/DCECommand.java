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
package org.alfresco.filesys.smb.dcerpc;

/**
 * DCE/RPC Command Codes
 */
public class DCECommand
{

    // DCE/RPC Packet Types

    public final static byte REQUEST    = 0x00;
    public final static byte RESPONSE   = 0x02;
    public final static byte FAULT      = 0x03;
    public final static byte BIND       = 0x0B;
    public final static byte BINDACK    = 0x0C;
    public final static byte ALTCONT    = 0x0E;
    public final static byte AUTH3      = 0x0F;
    public final static byte BINDCONT   = 0x10;

    /**
     * Convert the command type to a string
     * 
     * @param cmd int
     * @return String
     */
    public final static String getCommandString(int cmd)
    {

        // Determine the PDU command type

        String ret = "";
        switch (cmd)
        {
        case REQUEST:
            ret = "Request";
            break;
        case RESPONSE:
            ret = "Repsonse";
            break;
        case FAULT:
            ret = "Fault";
            break;
        case BIND:
            ret = "Bind";
            break;
        case BINDACK:
            ret = "BindAck";
            break;
        case ALTCONT:
            ret = "AltCont";
            break;
        case AUTH3:
            ret = "Auth3";
            break;
        case BINDCONT:
            ret = "BindCont";
            break;
        }
        return ret;
    }
}