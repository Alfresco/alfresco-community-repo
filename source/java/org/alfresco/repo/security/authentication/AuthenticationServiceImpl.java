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

import java.util.Collections;
import java.util.Set;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent.UserNameValidationMode;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthenticationServiceImpl extends AbstractAuthenticationService implements ActivateableBean
{
    private Log logger = LogFactory.getLog(AuthenticationServiceImpl.class);
    AuthenticationComponent authenticationComponent;
    TicketComponent ticketComponent;
    
    private String domain;
    private boolean allowsUserCreation = true;
    private boolean allowsUserDeletion = true;
    private boolean allowsUserPasswordChange = true;

    private static final String AUTHENTICATION_UNSUCCESSFUL = "Authentication was not successful.";
    private int protectionPeriodSeconds;
    private boolean protectionEnabled;
    private int protectionLimit;
    private SimpleCache<String, ProtectedUser> protectedUsersCache;

    private PersonService personService;

    public void setProtectionPeriodSeconds(int protectionPeriodSeconds)
    {
        this.protectionPeriodSeconds = protectionPeriodSeconds;
    }

    public void setProtectionEnabled(boolean protectionEnabled)
    {
        this.protectionEnabled = protectionEnabled;
    }

    public void setProtectionLimit(int protectionLimit)
    {
        this.protectionLimit = protectionLimit;
    }

    public void setProtectedUsersCache(SimpleCache<String, ProtectedUser> protectedUsersCache)
    {
        this.protectedUsersCache = protectedUsersCache;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public AuthenticationServiceImpl()
    {
        super();
    }
    
    public void setTicketComponent(TicketComponent ticketComponent)
    {
        this.ticketComponent = ticketComponent;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
   
    public boolean isActive()
    {
        return !(this.authenticationComponent instanceof ActivateableBean)
                || ((ActivateableBean) this.authenticationComponent).isActive();
    }

    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        try
        {
            String tenant = getPrevalidationTenantDomain();
            // clear context - to avoid MT concurrency issue (causing domain mismatch) - see also 'validate' below
            clearCurrentSecurityContext();
            preAuthenticationCheck(userName);
            if (protectionEnabled)
            {
                ProtectedUser protectedUser = protectedUsersCache.get(userName);
                if (protectedUser != null && protectedUser.isProtected())
                {
                    throw new AuthenticationException(AUTHENTICATION_UNSUCCESSFUL);
                }
            }
            authenticationComponent.authenticate(userName, password);
            if (tenant == null)
            {
                Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(userName);
                tenant = userTenant.getSecond();
            }
            TenantContextHolder.setTenantDomain(tenant);
            if (protectionEnabled)
            {
                if (protectedUsersCache.get(userName) != null)
                {
                     protectedUsersCache.remove(userName);
                }
            }
        }
        catch(AuthenticationException ae)
        {
            clearCurrentSecurityContext();
            if (protectionEnabled)
            {
                ProtectedUser protectedUser = protectedUsersCache.get(userName);
                if (protectedUser == null)
                {
                    protectedUser = new ProtectedUser(userName);
                }
                else
                {
                    protectedUser.recordUnsuccessfulLogin();
                }
                protectedUsersCache.put(userName, protectedUser);
            }
            throw ae;
        }
        ticketComponent.clearCurrentTicket();
        getCurrentTicket();
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
        String currentUser = null;
        try
        {
            String tenant = getPrevalidationTenantDomain();
            
            // clear context - to avoid MT concurrency issue (causing domain mismatch) - see also 'authenticate' above
            clearCurrentSecurityContext();
            currentUser = ticketComponent.validateTicket(ticket);
            authenticationComponent.setCurrentUser(currentUser, UserNameValidationMode.NONE);
            
            if (tenant == null)
            {
                Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(currentUser);
                tenant = userTenant.getSecond();
            }
            TenantContextHolder.setTenantDomain(tenant);
        }
        catch (AuthenticationException ae)
        {
            clearCurrentSecurityContext();
            throw ae;
        }
    }
    
    /**
     * This method is called from the {@link #validate(String)} method. If this method returns null then
     * the user's tenant will be obtained from the username. This is generally correct in the case where the user can be
     * associated with just one tenant.
     * Override this method in order to force the selection of a different tenant (for whatever reason).
     * @return String
     */
    protected String getPrevalidationTenantDomain()
    {
        return null;
    }

    public String getCurrentTicket() throws AuthenticationException
    {
        String userName = getCurrentUserName();
        
        // So that preAuthenticationCheck can constrain the creation of new tickets, we first ask for the current ticket
        // without auto-creation
        String ticket = ticketComponent.getCurrentTicket(userName, false);
        if (ticket == null)
        {
            // If we get through the authentication check then it's safe to issue a new ticket (e.g. for
            // SSO/external-based login)
            return getNewTicket();
        }
        return ticket;
    }

    public String getNewTicket()
    {
        String userName = getCurrentUserName();
        
        try
        {
            preAuthenticationCheck(userName);
        }
        catch (AuthenticationException e)
        {
            clearCurrentSecurityContext();
            throw e;
        }
        return ticketComponent.getNewTicket(userName);
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

    public void authenticateAsGuest() throws AuthenticationException
    {
        String defaultGuestName = AuthenticationUtil.getGuestUserName();
        if (defaultGuestName == null || defaultGuestName.length() == 0)
        {
            throw new AuthenticationException(GUEST_AUTHENTICATION_NOT_SUPPORTED);
        }
        preAuthenticationCheck(defaultGuestName);
        authenticationComponent.setGuestUserAsCurrentUser();
        String guestUser = authenticationComponent.getCurrentUserName();
        ticketComponent.clearCurrentTicket();
        ticketComponent.getCurrentTicket(guestUser, true); // to ensure new ticket is created (even if client does not explicitly call getCurrentTicket)
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

    @Override
    public Set<TicketComponent> getTicketComponents()
    {
        return Collections.singleton(ticketComponent);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getDefaultAdministratorUserNames()
    {
        return authenticationComponent.getDefaultAdministratorUserNames();
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getDefaultGuestUserNames()
    {
        return authenticationComponent.getDefaultGuestUserNames();
    }

    /**
     * {@inheritDoc}
     */
    public boolean authenticationExists(String userName)
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getAuthenticationEnabled(String userName) throws AuthenticationException
    {
        if (personService != null && personService.personExists(userName))
        {
            return personService.isEnabled(userName);
        }

        return true;
    }

    /**
     * An object to hold information about unsuccessful logins
     */
    /*package*/ class ProtectedUser
    {
        private String userId;
        /** number of consecutive unsuccessful login attempts */
        private long numLogins;
        /** time stamp of last unsuccessful login attempt */
        private long timeStamp;

        public ProtectedUser(String userId)
        {
            this.userId = userId;
            this.numLogins = 1;
            this.timeStamp = System.currentTimeMillis();
        }

        public void recordUnsuccessfulLogin()
        {
            this.numLogins+=1;
            this.timeStamp = System.currentTimeMillis();
            if (numLogins == protectionLimit + 1 && logger.isWarnEnabled())
            {
                // Shows only first 2 symbols of the username and masks all other character with '*'
                logger.warn("Brute force attack was detected for user " +
                        userId.substring(0,2) + new String(new char[(userId.length() - 2)]).replace("\0", "*"));
            }
        }

        public boolean isProtected()
        {
            long currentTimeStamp = System.currentTimeMillis();
            return numLogins >= protectionLimit &&
                    currentTimeStamp - timeStamp < protectionPeriodSeconds * 1000;
        }

        @Override
        public String toString()
        {
            return "ProtectedUser{" +
                    "userId='" + userId + '\'' +
                    ", numLogins=" + numLogins +
                    ", timeStamp=" + timeStamp +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ProtectedUser that = (ProtectedUser) o;

            if (numLogins != that.numLogins) return false;
            if (timeStamp != that.timeStamp) return false;
            return userId.equals(that.userId);
        }

        @Override
        public int hashCode()
        {
            int result = userId.hashCode();
            result = 31 * result + (int) (numLogins ^ (numLogins >>> 32));
            result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
            return result;
        }
    }
}
