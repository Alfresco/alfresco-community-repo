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

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.beans.factory.InitializingBean;

/**
 * Common code for authentication services
 * 
 * @author andyh
 */
public abstract class AbstractAuthenticationService implements AuthenticationService, InitializingBean
{

    private SimpleCache<String, Object> sysAdminCache;

    private static final String KEY_SYSADMIN_ALLOWED_USERS = "sysAdminCache.authAllowedUsers";

    private static final String KEY_SYSADMIN_MAX_USERS = "sysAdminCache.authMaxUsers";

    private boolean initialised = false;

    private Integer initialMaxUsers = null;

    private List<String> initialAllowedUsers = null;

    public void setSysAdminCache(SimpleCache<String, Object> sysAdminCache)
    {
        this.sysAdminCache = sysAdminCache;
    }

    public void preAuthenticationCheck(String userName) throws AuthenticationException
    {
        if (sysAdminCache != null)
        {
            List<String> allowedUsers = (List<String>) sysAdminCache.get(KEY_SYSADMIN_ALLOWED_USERS);

            if ((allowedUsers != null) && (!allowedUsers.contains(userName)))
            {
                throw new AuthenticationDisallowedException("Username not allowed: " + userName);
            }

            Integer maxUsers = (Integer) sysAdminCache.get(KEY_SYSADMIN_MAX_USERS);

            if ((maxUsers != null) && (maxUsers != -1) && (getUsersWithTickets(true).size() >= maxUsers))
            {
                throw new AuthenticationMaxUsersException("Max users exceeded: " + maxUsers);
            }
        }
    }

    public void setAllowedUsers(List<String> allowedUsers)
    {
        if (initialised)
        {
            if (sysAdminCache != null)
            {
                sysAdminCache.put(KEY_SYSADMIN_ALLOWED_USERS, allowedUsers);
            }
        }
        else
        {
            initialAllowedUsers = allowedUsers;
        }

    }

    @SuppressWarnings("unchecked")
    public List<String> getAllowedUsers()
    {
        if (sysAdminCache != null)
        {
            return (List<String>) sysAdminCache.get(KEY_SYSADMIN_ALLOWED_USERS);
        }
        else
        {
            return null;
        }
    }

    public void setMaxUsers(int maxUsers)
    {
        if (initialised)
        {
            if (sysAdminCache != null)
            {
                sysAdminCache.put(KEY_SYSADMIN_MAX_USERS, new Integer(maxUsers));
            }
        }
        else
        {
            initialMaxUsers = new Integer(maxUsers);
        }
    }

    @SuppressWarnings("unchecked")
    public int getMaxUsers()
    {
        if (sysAdminCache != null)
        {
            Integer maxUsers = (Integer) sysAdminCache.get(KEY_SYSADMIN_MAX_USERS);
            return (maxUsers == null ? -1 : maxUsers.intValue());
        }
        else
        {
            return -1;
        }
    }

    public abstract Set<String> getUsersWithTickets(boolean nonExpiredOnly);

    public abstract int invalidateTickets(boolean nonExpiredOnly);

    public abstract int countTickets(boolean nonExpiredOnly);

    public abstract Set<TicketComponent> getTicketComponents();

    final public void afterPropertiesSet() throws Exception
    {
        initialised = true;
        if (sysAdminCache != null)
        {
            sysAdminCache.put(KEY_SYSADMIN_MAX_USERS, initialMaxUsers);
            sysAdminCache.put(KEY_SYSADMIN_ALLOWED_USERS, initialAllowedUsers);
        }
    }

}
