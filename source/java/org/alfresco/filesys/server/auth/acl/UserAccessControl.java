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

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.core.SharedDevice;

/**
 * User Access Control Class
 * <p>
 * Allow/disallow access to a shared device by checking the user name.
 */
public class UserAccessControl extends AccessControl
{
    /**
     * Class constructor
     * 
     * @param userName String
     * @param type String
     * @param access int
     */
    protected UserAccessControl(String userName, String type, int access)
    {
        super(userName, type, access);
    }

    /**
     * Check if the user name matches the access control user name and return the allowed access.
     * 
     * @param sess SrvSession
     * @param share SharedDevice
     * @param mgr AccessControlManager
     * @return int
     */
    public int allowsAccess(SrvSession sess, SharedDevice share, AccessControlManager mgr)
    {

        // Check if the session has client information

        if (sess.hasClientInformation() == false)
            return Default;

        // Check if the user name matches the access control name

        ClientInfo cInfo = sess.getClientInformation();

        if (cInfo.getUserName() != null && cInfo.getUserName().equalsIgnoreCase(getName()))
            return getAccess();
        return Default;
    }
}
