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
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthenticationServiceImpl extends AbstractAuthenticationService implements ActivateableBean
{
    private Log logger = LogFactory.getLog(AuthenticationServiceImpl.class);
    AuthenticationComponent authenticationComponent;
    TicketComponent ticketComponent;

    /** a serviceInstanceId identifying this unique instance */
    private String serviceInstanceId;

    private String domain;
    private boolean allowsUserCreation = true;
    private boolean allowsUserDeletion = true;
    private boolean allowsUserPasswordChange = true;

    private static final String AUTHENTICATION_UNSUCCESSFUL = "Authentication was not successful.";
    private static final String BRUTE_FORCE_ATTACK_DETECTED = "Brute force attack was detected for user: %s";
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

    public boolean isProtectionEnabled()
    {
        return protectionEnabled;
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

        this.serviceInstanceId = GUID.generate();
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

    private boolean getUserNamesAreCaseSensitive()
    {
        if (personService != null)
        {
            return personService.getUserNamesAreCaseSensitive();
        }
        return false;
    }

    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        try
        {
            String tenant = getPrevalidationTenantDomain();
            // clear context - to avoid MT concurrency issue (causing domain mismatch) - see also 'validate' below
            clearCurrentSecurityContext();
            preAuthenticationCheck(userName);
            if (isUserProtected(userName))
            {
                throw new AuthenticationException(AUTHENTICATION_UNSUCCESSFUL);
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
                final String protectedUserKey = getProtectedUserKey(userName);
                if (protectedUsersCache.get(protectedUserKey) != null)
                {
                     protectedUsersCache.remove(protectedUserKey);
                }
            }
        }
        catch(AuthenticationException ae)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Exception in authenticating user: " + AuthenticationUtil.maskUsername(userName) + ", cause: " + ae.getMessage(), ae);
            }
            clearCurrentSecurityContext();
            recordFailedAuthentication(userName);
            throw ae;
        }
        ticketComponent.clearCurrentTicket();
        getCurrentTicket();
        if (logger.isTraceEnabled())
        {
            logger.trace("Authenticated user: " + AuthenticationUtil.maskUsername(userName));
        }
    }

    /**
     * @return <code>true</code> if user is 'protected' from brute force attack
     */
    public boolean isUserProtected(String userName)
    {
        boolean isProtected = false;
        if (protectionEnabled)
        {
            final String protectedUserKey = getProtectedUserKey(userName);
            ProtectedUser protectedUser = protectedUsersCache.get(protectedUserKey);
            if (protectedUser != null)
            {
                long currentTimeStamp = System.currentTimeMillis();
                isProtected = protectedUser.getNumLogins() >= protectionLimit &&
                        currentTimeStamp - protectedUser.getTimeStamp() < protectionPeriodSeconds * 1000;
            }
        }
        return isProtected;
    }

    /**
     * Method records a failed login attempt.
     * If the number of recorded failures exceeds {@link AuthenticationServiceImpl#protectionLimit}
     * the user will be considered 'protected'.
     */
    public void recordFailedAuthentication(String userName)
    {
        if (protectionEnabled)
        {
            final String protectedUserKey = getProtectedUserKey(userName);
            ProtectedUser protectedUser = protectedUsersCache.get(protectedUserKey);
            if (protectedUser == null)
            {
                protectedUser = new ProtectedUser(userName);
            }
            else
            {
                protectedUser = new ProtectedUser(userName, protectedUser.getNumLogins() + 1);
                if (protectedUser.getNumLogins() == protectionLimit && logger.isWarnEnabled())
                {
                    // Shows only first 2 symbols of the username and masks all other character with '*'
                    logger.warn(String.format(BRUTE_FORCE_ATTACK_DETECTED, AuthenticationUtil.maskUsername(userName)));
                }
            }
            protectedUsersCache.put(protectedUserKey, protectedUser);
        }
    }
    /**
     * Creates a key by combining the service instance ID with the username. This are the type of keys maintained by protectedUsersCache map.
     */
    public String getProtectedUserKey(String userName)
    {
        if (!getUserNamesAreCaseSensitive())
        {
            userName = userName.toLowerCase();
        }
        return serviceInstanceId + "@@" + userName;
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
            if (logger.isDebugEnabled())
            {
                logger.debug("Exception validating ticket: " + ticket + ", cause:" + ae.getMessage(), ae);
            }
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
        
        // So that preAuthenticationCheck can constrain the creation of new tickets, we first ask for the current ticket without auto-creation
        String ticket = ticketComponent.getCurrentTicket(userName, false);
        if (ticket == null)
        {
            // If we get through the authentication check then it's safe to issue a new ticket (e.g. for SSO/external-based login)
            if (logger.isTraceEnabled())
            {
                logger.trace("Ticket was null, but, if we got through the authentication check, then it's safe to issue a new ticket for user: "
                    + AuthenticationUtil.maskUsername(userName));
            }
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
            if (logger.isDebugEnabled())
            {
                logger.debug("Exception in pre authentication check: " + e.getMessage(), e);
            }
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
        if (logger.isTraceEnabled())
        {
            logger.trace("Authenticated as guest: " + guestUser);
        }
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
}
