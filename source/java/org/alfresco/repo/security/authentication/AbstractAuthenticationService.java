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
package org.alfresco.repo.security.authentication;

import java.util.List;
import java.util.Set;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.security.AuthenticationService;

/**
 * Common code for authentication services
 * 
 * @author andyh
 */
public abstract class AbstractAuthenticationService implements AuthenticationService
{
    private SysAdminParams sysAdminParams;

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public void preAuthenticationCheck(String userName) throws AuthenticationException
    {
        if (sysAdminParams != null)
        {
            List<String> allowedUsers = sysAdminParams.getAllowedUserList();

            if ((allowedUsers != null) && (!allowedUsers.contains(userName)))
            {
                throw new AuthenticationDisallowedException("Username not allowed: " + userName);
            }

            Integer maxUsers = (Integer) sysAdminParams.getMaxUsers();

            if ((maxUsers != null) && (maxUsers != -1) && (getUsersWithTickets(true).size() >= maxUsers))
            {
                throw new AuthenticationMaxUsersException("Max users exceeded: " + maxUsers);
            }
        }
    }

    public List<String> getAllowedUsers()
    {
        return sysAdminParams.getAllowedUserList();
    }

    public int getMaxUsers()
    {
        return sysAdminParams.getMaxUsers();
    }
    
    public String getCurrentTicket()
    {
        return getCurrentTicket(null);
    }

    public abstract Set<String> getUsersWithTickets(boolean nonExpiredOnly);

    public abstract int invalidateTickets(boolean nonExpiredOnly);

    public abstract int countTickets(boolean nonExpiredOnly);

    public abstract Set<TicketComponent> getTicketComponents();
}
