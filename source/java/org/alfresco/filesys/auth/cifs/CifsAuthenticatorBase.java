/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.filesys.auth.cifs;

import org.alfresco.filesys.AlfrescoConfigSection;
import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.CifsAuthenticator;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.SrvDiskInfo;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MD4PasswordEncoder;
import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.config.ConfigElement;

/**
 * CIFS Authenticator Base Class
 * <p>
 * Base class for Alfresco CIFS authenticator implementations.
 * 
 * @author gkspencer
 */
public abstract class CifsAuthenticatorBase extends CifsAuthenticator implements ActivateableBean, InitializingBean, DisposableBean
{
    // Logging
    
    /** The Constant logger. */
    protected static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.auth");

    // MD4 hash decoder
    
    /** The m_md4 encoder. */
    protected MD4PasswordEncoder m_md4Encoder = new MD4PasswordEncoderImpl();

    /** The authentication component. */
    private AuthenticationComponent authenticationComponent;

    /** The authentication service. */
    private AuthenticationService authenticationService;

    /** The node service. */
    private NodeService nodeService;

    /** The person service. */
    private PersonService personService;

    /** The transaction service. */
    private TransactionService transactionService;

    /** The authority service. */
    private AuthorityService authorityService;

    /** The disk interface. */
    private DiskInterface diskInterface;
    
    /** Is this component active, i.e. should it be used? */
    private boolean active = true;
    
    // Alfresco configuration
    
    /**
     * Instantiates a new cifs authenticator base.
     */
    public CifsAuthenticatorBase()
    {
        // The default access mode
        setAccessMode(USER_MODE);
    }
    
    /**
     * Sets the authentication component.
     * 
     * @param authenticationComponent
     *            the authenticationComponent to set
     */
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    /**
     * Sets the authentication service.
     * 
     * @param authenticationService
     *            the authenticationService to set
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Sets the node service.
     * 
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the person service.
     * 
     * @param personService
     *            the personService to set
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Sets the transaction service.
     * 
     * @param transactionService
     *            the transactionService to set
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Sets the authority service.
     * 
     * @param authorityService
     *            the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Set the filesystem driver for the node service based filesystem.
     * 
     * @param diskInterface
     *            DiskInterface
     */
    public void setDiskInterface(DiskInterface diskInterface)
    {
        this.diskInterface = diskInterface;
    }
    
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * Activates or deactivates the bean.
     * 
     * @param active
     *            <code>true</code> if the bean is active and initialization should complete
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /**
     * Initialize the authenticator.
     * 
     * @param config
     *            ServerConfiguration
     * @param params
     *            ConfigElement
     * @throws InvalidConfigurationException
     *             the invalid configuration exception
     * @exception InvalidConfigurationException
     */
    public void initialize(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
        // Get the alfresco configuration section, required to get hold of various services/components
        
        AlfrescoConfigSection alfrescoConfig = (AlfrescoConfigSection) config.getConfigSection( AlfrescoConfigSection.SectionName);
        
        // Copy over relevant bean properties for backward compatibility
        setAuthenticationComponent(alfrescoConfig.getAuthenticationComponent());
        setAuthenticationService(alfrescoConfig.getAuthenticationService());
        setNodeService(alfrescoConfig.getNodeService());
        setPersonService(alfrescoConfig.getPersonService());
        setTransactionService(alfrescoConfig.getTransactionService());
        setAuthorityService(alfrescoConfig.getAuthorityService());
        setDiskInterface(alfrescoConfig.getRepoDiskInterface());

        super.initialize(config, params);
    }

    
    /**
     * Initialize the authenticator.
     * 
     * @throws InvalidConfigurationException
     *             the invalid configuration exception
     * @exception InvalidConfigurationException
     */
    @Override
    public void initialize() throws InvalidConfigurationException
    {
        super.initialize();

        // Check that the required authentication classes are available

        if ( getAuthenticationComponent() == null)
            throw new InvalidConfigurationException("Authentication component not available");

        // Propagate the allow guest flag
        setAllowGuest(allowGuest() || getAuthenticationComponent().guestUserAuthenticationAllowed());
        
        // Set the guest user name
        
        setGuestUserName( getAuthenticationComponent().getGuestUserName());
        
        // Check that the authentication component is the required type for this authenticator
        
        if ( ! validateAuthenticationMode() )
            throw new InvalidConfigurationException("Required authentication mode not available");
    }
    
    

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public final void afterPropertiesSet() throws InvalidConfigurationException
    {
        // If the bean is active, call the overridable initialize method
        if (this.active)
        {
            initialize();
        }
    }

    /**
     * Validate that the authentication component supports the required mode.
     * 
     * @return boolean
     */
    protected boolean validateAuthenticationMode()
    {
        return true;
    }
    
    /**
     * Logon using the guest user account.
     * 
     * @param client
     *            ClientInfo
     * @param sess
     *            SrvSession
     */
    protected void doGuestLogon( ClientInfo client, SrvSession sess)
    {
        //  Check that the client is an Alfresco client
      
        if ( client instanceof AlfrescoClientInfo == false)
            return;
        
        AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
        
        //  Get a guest authentication token
        
        getAuthenticationService().authenticateAsGuest();
        String ticket = getAuthenticationService().getCurrentTicket();
        
        alfClient.setAuthenticationTicket(ticket);
        
        // Set the home folder for the guest user
        
        client.setUserName( getGuestUserName());
        getHomeFolderForUser( client);
        
        // Mark the client as being a guest logon
        
        client.setGuest( true);

        // Create a dynamic share for the guest user, create the disk driver and context
        
        DiskDeviceContext diskCtx = new ContentContext(client.getUserName(), "", "", alfClient.getHomeFolder());

        //  Default the filesystem to look like an 80Gb sized disk with 90% free space

        diskCtx.setDiskInformation(new SrvDiskInfo(2560, 64, 512, 2304));
        
        //  Create a temporary shared device for the users home directory
        
        sess.addDynamicShare( new DiskSharedDevice( client.getUserName(), this.diskInterface, diskCtx, SharedDevice.Temporary));
    }
    
    /**
     * Get the home folder for the user.
     * 
     * @param client
     *            ClientInfo
     */
    protected final void getHomeFolderForUser(final ClientInfo client)
    {
        // Check if the client is an Alfresco client, and not a null logon

        if (client instanceof AlfrescoClientInfo == false || client.isNullSession() == true)
            return;

        final AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;

        // Get the home folder for the user

        doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                NodeRef homeSpaceRef = (NodeRef) getNodeService().getProperty(
                        getPersonService().getPerson(client.getUserName()), ContentModel.PROP_HOMEFOLDER);
                alfClient.setHomeFolder(homeSpaceRef);
                return null;
            }
        });
    }
    
    /**
     * Map the case insensitive logon name to the internal person object user name.
     * 
     * @param userName
     *            String
     * @return String
     */
    protected final String mapUserNameToPerson(final String userName)
    {
        // Do the lookup as the system user
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>()
        {
            public String doWork() throws Exception
            {
                return doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<String>()
                {

                    public String execute() throws Throwable
                    {
                        // Get the home folder for the user

                        String personName = getPersonService().getUserIdentifier(userName);

                        // Check if the person exists

                        if (personName == null)
                        {
                            // Force creation of a person if possible
                            getPersonService().getPerson(userName);
                            personName = getPersonService().getUserIdentifier(userName);
                            return personName == null ? userName : personName;
                        }
                        return personName;
                    }
                });
            }
        }, AuthenticationUtil.getSystemUserName());
    }
    
    /**
     * Set the current authenticated user context for this thread.
     * 
     * @param client ClientInfo or null to clear the context
     */
    public void setCurrentUser(final ClientInfo client) {

        // Check the account type and setup the authentication context
        
        // No need for a transaction to clear the context
        if (client == null || client.isNullSession())
        {
            // Clear the authentication, null user should not be allowed to do any service calls

            getAuthenticationComponent().clearCurrentSecurityContext();
            return;
        }

        doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                if (client.isGuest() == false && client instanceof AlfrescoClientInfo)
                {
                    // Set the authentication context for the request
        
                    AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
                    if (alfClient.hasAuthenticationTicket())
                    {
                        try
                        {
                            getAuthenticationService().validate(alfClient.getAuthenticationTicket());
                        }
                        catch (AuthenticationException e)
                        {
                            // Ticket no longer valid or maximum tickets exceeded
                            alfClient.setAuthenticationTicket(null);
                            getAuthenticationComponent().clearCurrentSecurityContext();
                        }
                    }
                    else
                    {
                        getAuthenticationComponent().clearCurrentSecurityContext();
                    }
                }
                else
                {
                    // Enable guest access for the request
        
                    getAuthenticationComponent().setGuestUserAsCurrentUser();
                }
                return null;
            }
        });
    }
    
    /**
     * Return the authentication component.
     * 
     * @return AuthenticationComponent
     */
    protected final AuthenticationComponent getAuthenticationComponent()
    {
        return this.authenticationComponent;
    }
    

    /**
     * Returns an SSO-enabled authentication component.
     * 
     * @return NLTMAuthenticator
     */
    protected final NLTMAuthenticator getNTLMAuthenticator()
    {
        if (!(this.authenticationComponent instanceof NLTMAuthenticator))
        {
            throw new IllegalStateException("Attempt to use non SSO-enabled authentication component for SSO");
        }
        return (NLTMAuthenticator)this.authenticationComponent;
    }

    /**
     * Return the authentication service.
     * 
     * @return AuthenticationService
     */
    protected final AuthenticationService getAuthenticationService()
    {
        return this.authenticationService;
    }
    
    /**
     * Return the node service.
     * 
     * @return NodeService
     */
    protected final NodeService getNodeService()
    {
        return this.nodeService;
    }
    
    /**
     * Return the person service.
     * 
     * @return PersonService
     */
    protected final PersonService getPersonService()
    {
        return this.personService;
    }

    /**
     * Return the transaction service.
     * 
     * @return TransactionService
     */
    private final TransactionService getTransactionService()
    {
        return this.transactionService;
    }
    
    /**
     * Return the authority service.
     * 
     * @return AuthorityService
     */
    protected final AuthorityService getAuthorityService() {
        return this.authorityService;
    }

    /**
     * Check if the user is an administrator user name.
     * 
     * @param cInfo
     *            ClientInfo
     */
    protected final void checkForAdminUserName(final ClientInfo cInfo)
    {

        // Check if the user name is an administrator

        doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Object>()
        {

            public Object execute() throws Throwable
            {
                if (cInfo.getLogonType() == ClientInfo.LogonNormal
                        && getAuthorityService().isAdminAuthority(cInfo.getUserName()))
                {

                    // Indicate that this is an administrator logon

                    cInfo.setLogonType(ClientInfo.LogonAdmin);
                }
                return null;
            }
        });
    }
    
    /**
     * Does work in a transaction. This will be a writeable transaction unless the system is in read-only mode.
     * 
     * @param callback
     *            a callback that does the work
     * @return the result, or <code>null</code> if not applicable
     */
    protected <T> T doInTransaction(RetryingTransactionHelper.RetryingTransactionCallback<T> callback)
    {
        // Get the transaction service

        TransactionService txService = getTransactionService();

        // DEBUG

        if (logger.isDebugEnabled())
        {
            logger.debug("Using " + (txService.isReadOnly() ? "ReadOnly" : "Write") + " transaction");
        }
	    //
	    // the repository is read-only, we settle for a read-only transaction
	    if (txService.isReadOnly())
	    {
	        return txService.getRetryingTransactionHelper().doInTransaction(callback, true, false);
	    }
	
	    // otherwise we want force a writable transaction 
        return txService.getRetryingTransactionHelper().doInTransaction(callback, 
                false, 
                false);

    }

    /**
     * Handle tidy up on container shutdown.
     * 
     * @throws Exception
     *             the exception
     */
    public void destroy() throws Exception
    {
        closeAuthenticator();
    }
}