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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;

/**
 * This class abstract the support required to set up and query the Acegi context for security enforcement. There are some simple default method implementations to support simple authentication.
 * 
 * @author Andy Hind
 */
public abstract class AbstractAuthenticationComponent implements AuthenticationComponent
{
    /**
     * The abstract class keeps track of support for guest login
     */
    private Boolean allowGuestLogin = null;

    private Set<String> defaultAdministratorUserNames = Collections.emptySet();

    private Set<String> defaultGuestUserNames = Collections.emptySet();

    private AuthenticationContext authenticationContext;

    private PersonService personService;

    private NodeService nodeService;

    private TransactionService transactionService;

    private UserRegistrySynchronizer userRegistrySynchronizer;

    protected final Log logger = LogFactory.getLog(getClass());

    public AbstractAuthenticationComponent()
    {
        super();
    }

    /**
     * Set if guest login is supported.
     * 
     * @param allowGuestLogin
     *            Boolean
     */
    public void setAllowGuestLogin(Boolean allowGuestLogin)
    {
        this.allowGuestLogin = allowGuestLogin;
    }

    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setUserRegistrySynchronizer(UserRegistrySynchronizer userRegistrySynchronizer)
    {
        this.userRegistrySynchronizer = userRegistrySynchronizer;
    }

    public TransactionService getTransactionService()
    {
        return transactionService;
    }

    public Boolean getAllowGuestLogin()
    {
        return allowGuestLogin;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public PersonService getPersonService()
    {
        return personService;
    }

    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Authenticating user \"" + AuthenticationUtil.maskUsername(userName) + '"');
        }
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }
        // Support guest login from the login screen
        if (isGuestUserName(userName))
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("User \"" + AuthenticationUtil.maskUsername(userName) + "\" recognized as a guest user");
            }
            setGuestUserAsCurrentUser(getUserDomain(userName));
        }
        else
        {
            try
            {
                authenticateImpl(userName, password);
                onAuthenticate();
            }
            catch (RuntimeException e)
            {
                onFail();
                if (logger.isDebugEnabled())
                {
                    logger.debug("Failed to authenticate user \"" + AuthenticationUtil.maskUsername(userName) +
                            "' , with message: " + e.getMessage(), e);
                }
                throw e;
            }
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("User \"" + AuthenticationUtil.maskUsername(userName) + "\" authenticated successfully");
        }
    }

    /**
     * Default unsupported authentication implementation - as of 2.1 this is the best way to implement your own authentication component as it will support guest login - prior to this direct over ride for authenticate(String , char[]) was used. This will still work.
     * 
     * @param userName
     *            String
     * @param password
     *            char[]
     */
    protected void authenticateImpl(String userName, char[] password)
    {
        throw new UnsupportedOperationException();
    }

    public Authentication setCurrentUser(final String userName) throws AuthenticationException
    {
        return setCurrentUser(userName, UserNameValidationMode.CHECK_AND_FIX);
    }

    public Authentication setCurrentUser(String userName, UserNameValidationMode validationMode)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("About to set the current user: " + AuthenticationUtil.maskUsername(userName) + " with validation mode: " + validationMode);
        }
        if (validationMode == UserNameValidationMode.NONE || isSystemUserName(userName))
        {
            return setCurrentUserImpl(userName);
        }
        else
        {
            CurrentUserCallback callback = validationMode == UserNameValidationMode.CHECK_AND_FIX ? new FixCurrentUserCallback(
                    userName)
                    : new CheckCurrentUserCallback(userName);
            Authentication authentication;
            // If the repository is read only, we have to settle for a read only transaction. Auto user creation
            // will not be possible.
            if (transactionService.isReadOnly())
            {
                authentication = transactionService.getRetryingTransactionHelper().doInTransaction(callback, true,
                        false);
            }
            // Otherwise,
            // - for check-only mode we want a readable txn or
            // - for check-and-fix mode we want a writeable transaction, so if the current transaction is read only we set the
            // requiresNew flag to true
            else
            {
                boolean readOnly = (validationMode == UserNameValidationMode.CHECK);
                boolean requiresNew = ((!readOnly) && (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY));

                authentication = transactionService.getRetryingTransactionHelper().doInTransaction(callback, readOnly, requiresNew);
            }
            if ((authentication == null) || (callback.ae != null))
            {
                throw callback.ae;
            }
            return authentication;
        }
    }

    /**
     * Explicitly set the current user to be authenticated.
     * 
     * @param userName
     *            String
     * @return Authentication
     */
    private Authentication setCurrentUserImpl(String userName) throws AuthenticationException
    {
        if (userName == null)
        {
            throw new AuthenticationException("Null user name");
        }

        if (isSystemUserName(userName))
        {
            return setSystemUserAsCurrentUser(getUserDomain(userName));
        }

        try
        {
            UserDetails ud = null;
            if (isGuestUserName(userName))
            {
                String tenantDomain = getUserDomain(userName);
                if (logger.isTraceEnabled())
                {
                    logger.trace("Setting the current user to the guest user of tenant domain \"" + tenantDomain + '"');
                }
                GrantedAuthority[] gas = new GrantedAuthority[0];
                ud = new User(userName, "", true, true, true, true, gas);
            }
            else
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace("Setting the current user to \"" + AuthenticationUtil.maskUsername(userName) + '"');
                }
                ud = getUserDetails(userName);
                if (!userName.equals(ud.getUsername()))
                {
                    ud = new User(userName, ud.getPassword(), ud.isEnabled(), ud.isAccountNonExpired(),
                            ud.isCredentialsNonExpired(), ud.isAccountNonLocked(), ud.getAuthorities());
                }

            }
            return setUserDetails(ud);
        }
        catch (net.sf.acegisecurity.AuthenticationException ae)
        {
            throw new AuthenticationException(ae.getMessage(), ae);
        }
    }

    /**
     * Default implementation that makes an ACEGI object on the fly
     * 
     * @param userName
     *            String
     * @return UserDetails
     */
    protected UserDetails getUserDetails(String userName)
    {
        GrantedAuthority[] gas = new GrantedAuthority[1];
        gas[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
        UserDetails ud = new User(userName, "", true, true, true, true, gas);
        return ud;
    }

    public Authentication setCurrentAuthentication(Authentication authentication)
    {
        return this.authenticationContext.setCurrentAuthentication(authentication);
    }

    /**
     * Get the current authentication context
     * 
     * @return Authentication
     * @throws AuthenticationException
     */
    public Authentication getCurrentAuthentication() throws AuthenticationException
    {
        return authenticationContext.getCurrentAuthentication();
    }

    /**
     * Get the current user name.
     * 
     * @return String
     * @throws AuthenticationException
     */
    public String getCurrentUserName() throws AuthenticationException
    {
        return authenticationContext.getCurrentUserName();
    }

    /**
     * Set the system user as the current user note: for MT, will set to default domain only
     * 
     * @return Authentication
     */
    public Authentication setSystemUserAsCurrentUser()
    {
        return authenticationContext.setSystemUserAsCurrentUser();
    }

    /**
     * Get the name of the system user note: for MT, will get system for default domain only
     * 
     * @return String
     */
    public String getSystemUserName()
    {
        return authenticationContext.getSystemUserName();
    }

    /**
     * Is this the system user ?
     * 
     * @return boolean
     */
    public boolean isSystemUserName(String userName)
    {
        return authenticationContext.isSystemUserName(userName);
    }

    /**
     * Is the current user the system user?
     * 
     * @return boolean
     */
    public boolean isCurrentUserTheSystemUser()
    {
        return authenticationContext.isCurrentUserTheSystemUser();
    }

    /**
     * Get the name of the Guest User note: for MT, will get guest for default domain only
     * 
     * @return String
     */
    public String getGuestUserName()
    {
        return authenticationContext.getGuestUserName();
    }

    public String getGuestUserName(String tenantDomain)
    {
        return authenticationContext.getGuestUserName(tenantDomain);
    }

    /**
     * Set the guest user as the current user. note: for MT, will set to default domain only
     */
    public Authentication setGuestUserAsCurrentUser() throws AuthenticationException
    {
        return setGuestUserAsCurrentUser(TenantService.DEFAULT_DOMAIN);
    }

    /**
     * Set the guest user as the current user.
     */
    private Authentication setGuestUserAsCurrentUser(String tenantDomain) throws AuthenticationException
    {
        if (allowGuestLogin == null)
        {
            if (implementationAllowsGuestLogin())
            {
                return setCurrentUser(getGuestUserName(tenantDomain));
            }
            else
            {
                throw new AuthenticationException("Guest authentication is not allowed");
            }
        }
        else
        {
            if (allowGuestLogin.booleanValue())
            {
                return setCurrentUser(getGuestUserName(tenantDomain));
            }
            else
            {
                throw new AuthenticationException("Guest authentication is not allowed");
            }
        }
    }

    public boolean isGuestUserName(String userName)
    {
        return authenticationContext.isGuestUserName(userName);
    }

    protected abstract boolean implementationAllowsGuestLogin();

    /**
     * @return true if Guest user authentication is allowed, false otherwise
     */
    public boolean guestUserAuthenticationAllowed()
    {
        if (allowGuestLogin == null)
        {
            return (implementationAllowsGuestLogin());
        }
        else
        {
            return (allowGuestLogin.booleanValue());
        }
    }

    /**
     * Remove the current security information
     */
    public void clearCurrentSecurityContext()
    {
        authenticationContext.clearCurrentSecurityContext();
    }

    abstract class CurrentUserCallback implements RetryingTransactionHelper.RetryingTransactionCallback<Authentication>
    {
        AuthenticationException ae = null;

        String userNameIn;

        CurrentUserCallback(String userNameIn)
        {
            this.userNameIn = userNameIn;
        }
    }

    class CheckCurrentUserCallback extends CurrentUserCallback
    {

        CheckCurrentUserCallback(String userNameIn)
        {
            super(userNameIn);
        }

        public Authentication execute() throws Throwable
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Check current user: " + AuthenticationUtil.maskUsername(userNameIn));
            }
            try
            {
                // We must set full authentication before calling runAs in order to retain tickets
                Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(userNameIn);
                final String userName = userTenant.getFirst();
                final String tenantDomain = userTenant.getSecond();

                Authentication authentication = setCurrentUserImpl(userName);
                TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>() {
                    public Object doWork() throws Exception
                    {
                        String identifier;
                        if ((identifier = personService.getUserIdentifier(userName)) == null
                                || !identifier.equals(userName))
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("User \"" + AuthenticationUtil.maskUsername(userName)
                                        + "\" does not exist in Alfresco. Failing validation.");
                            }
                            throw new AuthenticationException("User \"" + AuthenticationUtil.maskUsername(userName) + "\" does not exist in Alfresco");
                        }
                        return null;
                    }
                }, tenantDomain);

                TenantContextHolder.setTenantDomain(tenantDomain);
                return authentication;
            }
            catch (AuthenticationException ae)
            {
                this.ae = ae;
                return null;
            }
        }
    }

    class FixCurrentUserCallback extends CurrentUserCallback
    {
        FixCurrentUserCallback(String userNameIn)
        {
            super(userNameIn);
        }

        public Authentication execute() throws Throwable
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Fix current user: " + AuthenticationUtil.maskUsername(userNameIn));
            }
            try
            {
                Pair<String, String> userTenant = AuthenticationUtil.getUserTenant(userNameIn);
                final String userName = userTenant.getFirst();
                final String tenantDomain = userTenant.getSecond();

                Authentication authentication = setCurrentUserImpl(TenantUtil.runAsSystemTenant(new TenantRunAsWork<String>() {
                    public String doWork() throws Exception
                    {
                        if (!personService.personExists(userName))
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("User \"" + AuthenticationUtil.maskUsername(userName)
                                        + "\" does not exist in Alfresco. Attempting to import / create the user.");
                            }
                            if (!userRegistrySynchronizer.createMissingPerson(userName))
                            {
                                if (logger.isDebugEnabled())
                                {
                                    logger.debug("Failed to import / create user \"" + AuthenticationUtil.maskUsername(userName) + '"');
                                }
                                throw new AuthenticationException(
                                        "User \"" + AuthenticationUtil.maskUsername(userName) + "\" does not exist in Alfresco");
                            }
                        }
                        NodeRef userNode = personService.getPerson(userName);
                        // Get the person name and use that as the current user to line up with permission checks
                        return (String) nodeService.getProperty(userNode, ContentModel.PROP_USERNAME);
                    }
                }, tenantDomain));

                TenantContextHolder.setTenantDomain(tenantDomain);
                return authentication;
            }
            catch (AuthenticationException ae)
            {
                this.ae = ae;
                return null;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getDefaultAdministratorUserNames()
    {
        return this.defaultAdministratorUserNames;
    }

    /**
     * Sets the user names who for this particular authentication system should be considered administrators by default.
     * 
     * @param defaultAdministratorUserNames
     *            a set of user names
     */
    public void setDefaultAdministratorUserNames(Set<String> defaultAdministratorUserNames)
    {
        this.defaultAdministratorUserNames = defaultAdministratorUserNames;
    }

    /**
     * Convenience method to allow the administrator user names to be specified as a comma separated list
     * 
     * @param defaultAdministratorUserNames
     *            String
     */
    public void setDefaultAdministratorUserNameList(String defaultAdministratorUserNames)
    {
        Set<String> nameSet = new TreeSet<String>();
        if (defaultAdministratorUserNames.length() > 0)
        {
            nameSet.addAll(Arrays.asList(defaultAdministratorUserNames.split(",")));
        }
        setDefaultAdministratorUserNames(nameSet);
    }

    public Set<String> getDefaultGuestUserNames()
    {
        return this.defaultGuestUserNames;
    }

    /**
     * Sets the user names who for this particular authentication system should be considered administrators by default.
     * 
     * @param defaultGuestUserNames
     *            a set of user names
     */
    public void setDefaultGuestUserNames(Set<String> defaultGuestUserNames)
    {
        this.defaultGuestUserNames = defaultGuestUserNames;
    }

    /**
     * Convenience method to allow the administrator user names to be specified as a comma separated list
     * 
     * @param defaultGuestUserNames
     *            String
     */
    public void setDefaultGuestUserNameList(String defaultGuestUserNames)
    {
        Set<String> nameSet = new TreeSet<String>();
        if (defaultGuestUserNames.length() > 0)
        {
            nameSet.addAll(Arrays.asList(defaultGuestUserNames.split(",")));
        }
        setDefaultGuestUserNames(nameSet);
    }

    public String getSystemUserName(String tenantDomain)
    {
        return authenticationContext.getSystemUserName(tenantDomain);
    }

    public String getUserDomain(String userName)
    {
        return authenticationContext.getUserDomain(userName);
    }

    public Authentication setSystemUserAsCurrentUser(String tenantDomain)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Setting the current user to the system user of tenant domain \"" + tenantDomain + '"');
        }
        return authenticationContext.setSystemUserAsCurrentUser(tenantDomain);
    }

    public Authentication setUserDetails(UserDetails ud)
    {
        return authenticationContext.setUserDetails(ud);
    }

    AtomicInteger numberSuccessfulAuthentications = new AtomicInteger(0);
    AtomicInteger numberFailedAuthentications = new AtomicInteger(0);

    protected void onAuthenticate()
    {
        numberSuccessfulAuthentications.getAndIncrement();
    }

    protected void onFail()
    {
        numberFailedAuthentications.getAndIncrement();
    }

    public int getNumberSuccessfulAuthentications()
    {
        return numberSuccessfulAuthentications.get();
    }

    public int getNumberFailedAuthentications()
    {
        return numberFailedAuthentications.get();
    }
}
