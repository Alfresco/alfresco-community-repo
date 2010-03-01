/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
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

package org.alfresco.filesys.auth.ftp;

import javax.transaction.UserTransaction;

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.filesys.AlfrescoConfigSection;
import org.alfresco.jlan.ftp.FTPAuthenticator;
import org.alfresco.jlan.ftp.FTPSrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.config.ServerConfigurationAccessor;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * 
 * @author gkspencer
 */
public abstract class FTPAuthenticatorBase implements FTPAuthenticator, ActivateableBean, DisposableBean {

    // Logging
	  
    protected static final Log logger = LogFactory.getLog("org.alfresco.ftp.protocol.auth");


    protected ServerConfigurationAccessor serverConfiguration;


    private AuthenticationComponent authenticationComponent;


    private AuthenticationService authenticationService;


    private TransactionService transactionService;


    private AuthorityService authorityService;
    
    /** Is this component active, i.e. should it be used? */
    private boolean active = true;

    /**
	 * Default constructor
	 */
	public FTPAuthenticatorBase() {
	}

	
	public void setConfig(ServerConfigurationAccessor config)
    {
	    this.serverConfiguration = config;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
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
     * Initialize the authenticator
     * 
	 * @param config ServerConfiguration
	 * @param params ConfigElement
     * @exception InvalidConfigurationException
     */
    public void initialize(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
        setConfig(config);

        // Get the alfresco configuration section, required to get hold of various services/components
        
        AlfrescoConfigSection alfrescoConfig = (AlfrescoConfigSection) config.getConfigSection( AlfrescoConfigSection.SectionName);
        
        // Copy over relevant bean properties for backward compatibility
        setAuthenticationComponent(alfrescoConfig.getAuthenticationComponent());
        setAuthenticationService(alfrescoConfig.getAuthenticationService());
        setTransactionService(alfrescoConfig.getTransactionService());
        setAuthorityService(alfrescoConfig.getAuthorityService());

        // Complete initialization
        initialize();
    }

    /**
     * Initialize the authenticator (after properties have been set)
     * 
     * @exception InvalidConfigurationException
     */
    public void initialize() throws InvalidConfigurationException
    {
        if ( this.serverConfiguration == null)
            throw new InvalidConfigurationException("server configuration accessor property not set");
	}

    /**
	 * Authenticate the user
	 * 
	 * @param client ClientInfo
	 * @param sess FTPSrvSession
	 * @return boolean
	 */
	public abstract boolean authenticateUser(ClientInfo info, FTPSrvSession sess);

	/**
	 * Close the authenticator, perform any cleanup
	 */
	public void closeAuthenticator()
	{
	}
	

	/**
	 * Return the authentication componenet
	 * 
	 * @return AuthenticationComponent
	 */
	protected final AuthenticationComponent getAuthenticationComponent() {
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
	 * Return the authentication service
	 * 
	 * @return AuthenticationService
	 */
	protected final AuthenticationService getAuthenticationService() {
		return this.authenticationService;
	}

	/**
	 * Return the transaction service
	 * 
	 * @return TransactionService
	 */
	protected final TransactionService getTransactionService() {
		return this.transactionService;
	}

	/**
	 * Return the authority service
	 * 
	 * @return AuthorityService
	 */
	protected final AuthorityService getAuthorityService() {
		return this.authorityService;
	}

	/**
	 * Check if the user is an administrator user name
	 * 
	 * @param cInfo ClientInfo
	 */
	protected final void checkForAdminUserName(ClientInfo cInfo) {
		
		// Check if the user name is an administrator

		UserTransaction tx = getTransactionService().getUserTransaction();

		try {
			tx.begin();

			if ( cInfo.getLogonType() == ClientInfo.LogonNormal && getAuthorityService().isAdminAuthority(cInfo.getUserName())) {
				
				// Indicate that this is an administrator logon

				cInfo.setLogonType(ClientInfo.LogonAdmin);
			}
			tx.commit();
		}
		catch (Throwable ex) {
			try {
				tx.rollback();
			}
			catch (Throwable ex2) {
				logger.error("Failed to rollback transaction", ex2);
			}

			// Re-throw the exception

			if ( ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			else {
				throw new RuntimeException("Error during execution of transaction.", ex);
			}
		}
	}
	
	/**
	 * Create a transaction, this will be a wrteable transaction unless the system is in read-only mode.
	 * 
	 * return UserTransaction
	 */
	protected final UserTransaction createTransaction()
	{
		// Get the transaction service
		
		TransactionService txService = getTransactionService();
		
		// DEBUG
		
		if ( logger.isDebugEnabled())
			logger.debug("Using " + (txService.isReadOnly() ? "ReadOnly" : "Write") + " transaction");
		
		// Create the transaction
		
		return txService.getUserTransaction( txService.isReadOnly() ? true : false);
	}

	/**
	 * Handle tidy up on container shutdown
	 */
    public void destroy()
    {
        closeAuthenticator();
    }
	
	
}
