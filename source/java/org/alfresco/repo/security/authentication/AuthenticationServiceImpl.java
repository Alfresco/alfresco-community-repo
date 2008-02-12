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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;

public class AuthenticationServiceImpl implements AuthenticationService
{
    MutableAuthenticationDao authenticationDao;

    AuthenticationComponent authenticationComponent;
    
    TicketComponent ticketComponent;
    
    private String domain;
    
    private boolean allowsUserCreation = true;
    
    private boolean allowsUserDeletion = true;
    
    private boolean allowsUserPasswordChange = true;
    
    // SysAdmin cache - used to cluster certain JMX operations
    private SimpleCache<String, Object> sysAdminCache;
    private final static String KEY_SYSADMIN_ALLOWED_USERS = "sysAdminCache.authAllowedUsers"; // List<String>
    private final static String KEY_SYSADMIN_MAX_USERS = "sysAdminCache.authMaxUsers"; // Integer

    
    public AuthenticationServiceImpl()
    {
        super();
    }
    
    public void setSysAdminCache(SimpleCache<String, Object> sysAdminCache)
    {
        this.sysAdminCache = sysAdminCache;
    }

    public void setAuthenticationDao(MutableAuthenticationDao authenticationDao)
    {
        this.authenticationDao = authenticationDao;
    }

    public void setTicketComponent(TicketComponent ticketComponent)
    {
        this.ticketComponent = ticketComponent;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void createAuthentication(String userName, char[] password) throws AuthenticationException
    {
        authenticationDao.createUser(userName, password);
    }

    public void updateAuthentication(String userName, char[] oldPassword, char[] newPassword)
            throws AuthenticationException
    {
        String currentUser = AuthenticationUtil.getCurrentUserName();
        try
        {
            authenticate(userName, oldPassword);
        }
        finally
        {
            AuthenticationUtil.setCurrentUser(currentUser);
        }
        authenticationDao.updateUser(userName, newPassword);
    }

    public void setAuthentication(String userName, char[] newPassword) throws AuthenticationException
    {
        authenticationDao.updateUser(userName, newPassword);
    }

    public void deleteAuthentication(String userName) throws AuthenticationException
    {
        authenticationDao.deleteUser(userName);
    }

    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException
    {
        return authenticationDao.getEnabled(userName);
    }

    public void setAuthenticationEnabled(String userName, boolean enabled) throws AuthenticationException
    {
        authenticationDao.setEnabled(userName, enabled);
    }

    @SuppressWarnings("unchecked")
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        try
        {
            // clear context - to avoid MT concurrency issue (causing domain mismatch) - see also 'validate' below
            clearCurrentSecurityContext();
        	List<String> allowedUsers = (List<String>)sysAdminCache.get(KEY_SYSADMIN_ALLOWED_USERS);
           
        	if ((allowedUsers != null) && (! allowedUsers.contains(userName)))
			{
				throw new AuthenticationDisallowedException("Username not allowed: " + userName);
			}
        	
        	Integer maxUsers = (Integer)sysAdminCache.get(KEY_SYSADMIN_MAX_USERS);
        	
        	if ((maxUsers != null) && (maxUsers != -1) && (ticketComponent.getUsersWithTickets(true).size() >= maxUsers))
        	{
        		throw new AuthenticationMaxUsersException("Max users exceeded: " + maxUsers);
        	}
        	
        	authenticationComponent.authenticate(userName, password);
        }
        catch(AuthenticationException ae)
        {
            clearCurrentSecurityContext();
            throw ae;
        }
        ticketComponent.clearCurrentTicket();
        
        ticketComponent.getCurrentTicket(userName); // to ensure new ticket is created (even if client does not explicitly call getCurrentTicket)
    }
    
    public boolean authenticationExists(String userName)
    {
        return authenticationDao.userExists(userName);
    }

    public String getCurrentUserName() throws AuthenticationException
    {
        return authenticationComponent.getCurrentUserName();
    }

    public void invalidateUserSession(String userName) throws AuthenticationException
    {
        ticketComponent.invalidateTicketByUser(userName);
    }
    
    public Set<String> getUsersWithTickets(boolean nonExpiredOnly)
    {
    	return ticketComponent.getUsersWithTickets(nonExpiredOnly);
    }
    
    public void setAllowedUsers(List<String> allowedUsers)
    {
    	sysAdminCache.put(KEY_SYSADMIN_ALLOWED_USERS, allowedUsers);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getAllowedUsers()
    {
    	return (List<String>)sysAdminCache.get(KEY_SYSADMIN_ALLOWED_USERS);
    }
    
    public void setMaxUsers(int maxUsers)
    {
    	sysAdminCache.put(KEY_SYSADMIN_MAX_USERS, new Integer(maxUsers));
    }
    
    @SuppressWarnings("unchecked")
    public int getMaxUsers()
    {
    	Integer maxUsers = (Integer)sysAdminCache.get(KEY_SYSADMIN_MAX_USERS);
    	return (maxUsers == null ? -1 : maxUsers.intValue());
    }

    public void invalidateTicket(String ticket) throws AuthenticationException
    {
        ticketComponent.invalidateTicketById(ticket);
    }
    
    public int countTickets(boolean nonExpiredOnly)
    {
    	return ticketComponent.countTickets(nonExpiredOnly);
    }
    
    public int invalidateTickets(boolean expiredOnly)
    {
    	return ticketComponent.invalidateTickets(expiredOnly);
    }
    

    public void validate(String ticket) throws AuthenticationException
    {
        try
        {
           // clear context - to avoid MT concurrency issue (causing domain mismatch) - see also 'authenticate' above
           clearCurrentSecurityContext();
           authenticationComponent.setCurrentUser(ticketComponent.validateTicket(ticket));
        }
        catch(AuthenticationException ae)
        {
            clearCurrentSecurityContext();
            throw ae;
        } 
    }

    public String getCurrentTicket()
    {
        return ticketComponent.getCurrentTicket(getCurrentUserName());
    }

    public String getNewTicket()
    {
        return ticketComponent.getNewTicket(getCurrentUserName());
    }
    
    public void clearCurrentSecurityContext()
    {
        authenticationComponent.clearCurrentSecurityContext();
        ticketComponent.clearCurrentTicket();
    }

    public boolean isCurrentUserTheSystemUser()
    {
        return authenticationComponent.isSystemUserName(getCurrentUserName());
    }

    @SuppressWarnings("unchecked")
    public void authenticateAsGuest() throws AuthenticationException
    {
    	List<String> allowedUsers = (List<String>)sysAdminCache.get(KEY_SYSADMIN_ALLOWED_USERS);
        
    	if ((allowedUsers != null) && (! allowedUsers.contains(PermissionService.GUEST_AUTHORITY)))
		{
			throw new AuthenticationException("Guest authentication is not allowed");
		}

        authenticationComponent.setGuestUserAsCurrentUser();
        ticketComponent.clearCurrentTicket();
    }
    
    public boolean guestUserAuthenticationAllowed()
    {
        return authenticationComponent.guestUserAuthenticationAllowed();
    }

    public boolean getAllowsUserCreation()
    {
        return allowsUserCreation;
    }

    public void setAllowsUserCreation(boolean allowsUserCreation)
    {
        this.allowsUserCreation = allowsUserCreation;
    }

    public boolean getAllowsUserDeletion()
    {
        return allowsUserDeletion;
    }

    public void setAllowsUserDeletion(boolean allowsUserDeletion)
    {
        this.allowsUserDeletion = allowsUserDeletion;
    }

    public boolean getAllowsUserPasswordChange()
    {
        return allowsUserPasswordChange;
    }

    public void setAllowsUserPasswordChange(boolean allowsUserPasswordChange)
    {
        this.allowsUserPasswordChange = allowsUserPasswordChange;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public Set<String> getDomains()
    {
       return Collections.singleton(getDomain());
    }

    public Set<String> getDomainsThatAllowUserCreation()
    {
        if(getAllowsUserCreation())
        {
            return Collections.singleton(getDomain()); 
        }
        else
        {
            return Collections.<String>emptySet();
        }
    }

    public Set<String> getDomainsThatAllowUserDeletion()
    {
        if(getAllowsUserDeletion())
        {
            return Collections.singleton(getDomain()); 
        }
        else
        {
            return Collections.<String>emptySet();
        }
    }

    public Set<String> getDomiansThatAllowUserPasswordChanges()
    {
        if(getAllowsUserPasswordChange())
        {
            return Collections.singleton(getDomain()); 
        }
        else
        {
            return Collections.<String>emptySet();
        }
    }

   

    
}
