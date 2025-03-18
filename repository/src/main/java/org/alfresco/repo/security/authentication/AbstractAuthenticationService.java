/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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
    public static final String GUEST_AUTHENTICATION_NOT_SUPPORTED = "Guest authentication not supported";

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
                throw new AuthenticationDisallowedException("Username not allowed: " + AuthenticationUtil.maskUsername(userName));
            }

            Integer maxUsers = (Integer) sysAdminParams.getMaxUsers();

            if ((maxUsers != null) && (maxUsers > -1) && (getUsersWithTickets(true).size() >= maxUsers))
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

    public abstract Set<String> getUsersWithTickets(boolean nonExpiredOnly);

    public abstract int invalidateTickets(boolean nonExpiredOnly);

    public abstract int countTickets(boolean nonExpiredOnly);

    public abstract Set<TicketComponent> getTicketComponents();
}
