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
package org.alfresco.filesys.smb.server;

/**
 * <p>
 * Contains the named pipe transaction codes.
 */
public class NamedPipeTransaction
{

    // Transaction sub-commands

    public static final int CallNamedPipe = 0x54;
    public static final int WaitNamedPipe = 0x53;
    public static final int PeekNmPipe = 0x23;
    public static final int QNmPHandState = 0x21;
    public static final int SetNmPHandState = 0x01;
    public static final int QNmPipeInfo = 0x22;
    public static final int TransactNmPipe = 0x26;
    public static final int RawReadNmPipe = 0x11;
    public static final int RawWriteNmPipe = 0x31;

    /**
     * Return the named pipe transaction sub-command as a string
     * 
     * @param subCmd int
     * @return String
     */
    public final static String getSubCommand(int subCmd)
    {

        // Determine the sub-command code

        String ret = "";

        switch (subCmd)
        {
        case CallNamedPipe:
            ret = "CallNamedPipe";
            break;
        case WaitNamedPipe:
            ret = "WaitNamedPipe";
            break;
        case PeekNmPipe:
            ret = "PeekNmPipe";
            break;
        case QNmPHandState:
            ret = "QNmPHandState";
            break;
        case SetNmPHandState:
            ret = "SetNmPHandState";
            break;
        case QNmPipeInfo:
            ret = "QNmPipeInfo";
            break;
        case TransactNmPipe:
            ret = "TransactNmPipe";
            break;
        case RawReadNmPipe:
            ret = "RawReadNmPipe";
            break;
        case RawWriteNmPipe:
            ret = "RawWriteNmPipe";
            break;
        }
        return ret;
    }
}
