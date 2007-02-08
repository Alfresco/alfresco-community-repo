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
package org.alfresco.filesys.server.auth.acl;

import java.util.StringTokenizer;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.core.SharedDevice;

/**
 * Protocol Access Control Class
 * <p>
 * Allow/disallow access to a share based on the protocol type.
 */
public class ProtocolAccessControl extends AccessControl
{

    // Available protocol type names

    private static final String[] _protoTypes = { "SMB", "CIFS", "NFS", "FTP" };

    // Parsed list of protocol types

    private String[] m_checkList;

    /**
     * Class constructor
     * 
     * @param protList String
     * @param type String
     * @param access int
     */
    protected ProtocolAccessControl(String protList, String type, int access)
    {
        super(protList, type, access);

        // Parse the protocol list

        m_checkList = listFromString(protList);
    }

    /**
     * Check if the protocol matches the access control protocol list and return the allowed access.
     * 
     * @param sess SrvSession
     * @param share SharedDevice
     * @param mgr AccessControlManager
     * @return int
     */
    public int allowsAccess(SrvSession sess, SharedDevice share, AccessControlManager mgr)
    {

        // Determine the session protocol type

        String sessProto = null;
        String sessName = sess.getClass().getName();

        if (sessName.endsWith(".SMBSrvSession"))
            sessProto = "CIFS";
        else if (sessName.endsWith(".FTPSrvSession"))
            sessProto = "FTP";
        else if (sessName.endsWith(".NFSSrvSession"))
            sessProto = "NFS";

        // Check if the session protocol type is in the protocols to be checked

        if (sessProto != null && indexFromList(sessProto, m_checkList, false) != -1)
            return getAccess();
        return Default;
    }

    /**
     * Validate the protocol list
     * 
     * @param protList String
     * @return boolean
     */
    public static final boolean validateProtocolList(String protList)
    {

        // Check if the protocol list string is valid

        if (protList == null || protList.length() == 0)
            return false;

        // Split the protocol list and validate each protocol name

        StringTokenizer tokens = new StringTokenizer(protList, ",");

        while (tokens.hasMoreTokens())
        {

            // Get the current protocol name and validate

            String name = tokens.nextToken().toUpperCase();
            if (indexFromList(name, _protoTypes, false) == -1)
                return false;
        }

        // Protocol list is valid

        return true;
    }
}
