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
package org.alfresco.filesys.smb.server;

/**
 * <p>
 * Contains the various states that an SMB server session will go through during the session
 * lifetime.
 */
public class SMBSrvSessionState
{

    // NetBIOS session has been closed.

    public static final int NBHANGUP = 5;

    // NetBIOS session request state.

    public static final int NBSESSREQ = 0;

    // SMB session closed down.

    public static final int SMBCLOSED = 4;

    // Negotiate SMB dialect.

    public static final int SMBNEGOTIATE = 1;

    // SMB session is initialized, ready to receive/handle standard SMB requests.

    public static final int SMBSESSION = 3;

    // SMB session setup.

    public static final int SMBSESSSETUP = 2;

    // State name strings

    private static final String _stateName[] = {
            "NBSESSREQ",
            "SMBNEGOTIATE",
            "SMBSESSSETUP",
            "SMBSESSION",
            "SMBCLOSED",
            "NBHANGUP" };

    /**
     * Return the specified SMB state as a string.
     */
    public static String getStateAsString(int state)
    {
        if (state < _stateName.length)
            return _stateName[state];
        return "[UNKNOWN]";
    }
}