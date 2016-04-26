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

package org.alfresco.filesys.auth.ftp;

import java.net.InetAddress;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.ExtendedServerConfigurationAccessor;
import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.filesys.auth.PassthruServerFactory;
import org.alfresco.jlan.ftp.FTPSrvSession;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.PasswordEncryptor;
import org.alfresco.jlan.server.auth.passthru.AuthenticateSession;
import org.alfresco.jlan.server.auth.passthru.DomainMapping;
import org.alfresco.jlan.server.auth.passthru.PassthruServers;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;
import org.springframework.extensions.config.ConfigElement;

/**
 * Passthru FTP Authenticator Class
 * 
 * @author gkspencer
 */
public class PassthruFtpAuthenticator extends FTPAuthenticatorBase {

    // Constants

    public final static int DefaultSessionTmo = 5000;   // 5 seconds
    public final static int MinSessionTmo = 2000;       // 2 seconds
    public final static int MaxSessionTmo = 30000;      // 30 seconds

    public final static int MinCheckInterval = 10;		// 10 seconds
    public final static int MaxCheckInterval = 15 * 60; // 15 minutes
    
    // Passthru keep alive interval

    public final static long PassthruKeepAliveInterval = 60000L; // 60 seconds

    // Passthru servers used to authenticate users

    private PassthruServers m_passthruServers;

    private boolean m_localPassThruServers;
    
    // Password encryption, for CIFS NTLM style encryption/hashing
    
    private PasswordEncryptor m_passwordEncryptor;

    protected SecurityConfigSection getSecurityConfig()
    {
        return (SecurityConfigSection) this.serverConfiguration.getConfigSection(SecurityConfigSection.SectionName);
    }

    public void setPassthruServers(PassthruServers passthruServers)
    {
        m_passthruServers = passthruServers;
    }

    /**
     * Initialize the authenticator
     * 
	 * @param config ServerConfiguration
	 * @param params ConfigElement
     * @exception InvalidConfigurationException
     */
    @Override
	public void initialize(ServerConfiguration config, ConfigElement params)
		throws InvalidConfigurationException {
        // Manually construct our own passthru server list

        PassthruServerFactory factory = new PassthruServerFactory();

        // Check if the offline check interval has been specified
        
        ConfigElement checkInterval = params.getChild("offlineCheckInterval");
        if ( checkInterval != null)
        {
            try
            {
                // Validate the check interval value

                factory.setOfflineCheckInterval(Integer.parseInt(checkInterval.getValue()));
            }
            catch (NumberFormatException ex)
            {
                throw new InvalidConfigurationException("Invalid offline check interval specified");
            }
        }

        // Check if the session timeout has been specified

        ConfigElement sessTmoElem = params.getChild("Timeout");
        if (sessTmoElem != null)
        {

            try
            {

                // Validate the session timeout value

                factory.setTimeout(Integer.parseInt(sessTmoElem.getValue()));

            }
            catch (NumberFormatException ex)
            {
                throw new InvalidConfigurationException("Invalid timeout value specified");
            }
        }

        // Get the extended server configuration
        
        ExtendedServerConfigurationAccessor configExtended = null;
        
        if ( config instanceof ExtendedServerConfigurationAccessor)
            configExtended = (ExtendedServerConfigurationAccessor) config;
        
        // Check if the local server should be used

		if ( params.getChild("LocalServer") != null && configExtended != null) {

            // Get the local server name, trim the domain name

            String server = configExtended.getLocalServerName( true);
            if ( server == null)
                throw new AlfrescoRuntimeException("Passthru authenticator failed to get local server name");

            factory.setServer(server);            
        }

        // Check if a server name has been specified

        ConfigElement srvNamesElem = params.getChild("Server");

        if (srvNamesElem != null && srvNamesElem.getValue().length() > 0)
        {
            factory.setServer(srvNamesElem.getValue());
        }

        // Check if the local domain/workgroup should be used

        if ( params.getChild("LocalDomain") != null && configExtended != null) {
            
            // Get the local domain/workgroup name

            factory.setDomain(configExtended.getLocalDomainName());
        }

        // Check if a domain name has been specified

        ConfigElement domNameElem = params.getChild("Domain");

        if (domNameElem != null && domNameElem.getValue().length() > 0)
        {

            factory.setDomain(domNameElem.getValue());
        }

        // Check if a protocol order has been set

        ConfigElement protoOrderElem = params.getChild("ProtocolOrder");

        if (protoOrderElem != null && protoOrderElem.getValue().length() > 0)
        {
            factory.setProtocolOrder(protoOrderElem.getValue());
        }
        
        // Complete initialization
        factory.afterPropertiesSet();
        setPassthruServers((PassthruServers) factory.getObject());
        // Remember that we have to shut down the servers
        m_localPassThruServers = true;
        
        super.initialize(config, params);
    }

    
    /**
     * Initialize the authenticator (after properties have been set)
     * 
     * @exception InvalidConfigurationException
     */
    @Override
    public void initialize() throws InvalidConfigurationException
    {
        super.initialize();

        // Check if the appropriate authentication component type is configured
        AuthenticationComponent authenticationComponent = getAuthenticationComponent();
        if (authenticationComponent instanceof NLTMAuthenticator
                && ((NLTMAuthenticator) authenticationComponent).getNTLMMode() == NTLMMode.MD4_PROVIDER)
            throw new AlfrescoRuntimeException(
                    "Wrong authentication setup for passthru authenticator (cannot be used with Alfresco users)");

        // Create the password encryptor

        m_passwordEncryptor = new PasswordEncryptor();
    }

    /**
     * Authenticate the user
     * 
     * @param client ClientInfo
     * @param sess FTPSrvSession
     * @return boolean
     */
    public boolean authenticateUser(ClientInfo client, FTPSrvSession sess) {
        
        // Check that the client is an Alfresco client

        if ( client instanceof AlfrescoClientInfo == false)
            return false;

        // Check if this is a guest logon

        boolean authSts = false;
        UserTransaction tx = null;

        try {
            if ( client.isGuest()) {
                
                // Get a guest authentication token

                doGuestLogon((AlfrescoClientInfo) client, sess);

                // Indicate logged on as guest

                authSts = true;

                // DEBUG

                if ( logger.isDebugEnabled())
                    logger.debug("Authenticated guest user " + client.getUserName() + " sts=" + authSts);

                // Return the guest status

                return authSts;
            }

            // Start a transaction

            tx = getTransactionService().getUserTransaction(false);
            tx.begin();

            // Perform passthru authentication check
            
            authSts = doPassthruUserAuthentication(client, sess);
            
            // Check if the user is an administrator
            
            if ( authSts == true && client.getLogonType() == ClientInfo.LogonNormal)
                checkForAdminUserName( client);
        }
        catch (Exception ex) {
            if ( logger.isDebugEnabled())
                logger.debug(ex);
        }
        finally {
            
            // Commit the transaction

            if ( tx != null) {
                try {
                    
                    // Commit or rollback the transaction

                    if ( tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                        
                        // Transaction is marked for rollback

                        tx.rollback();
                    }
                    else {
                        // Commit the transaction

                        tx.commit();
                    }
                }
                catch (Exception ex) {
                }
            }
        }

        // DEBUG

        if ( logger.isDebugEnabled())
            logger.debug("Authenticated user " + client.getUserName() + " sts=" + authSts + " via Passthru");

        // Return the authentication status

        return authSts;
    }

    /**
     * Logon using the guest user account
     * 
     * @param client AlfrescoClientInfo
     * @param sess SrvSession
     */
    protected void doGuestLogon(AlfrescoClientInfo client, SrvSession sess) {
        
        // Get a guest authentication token

        getAuthenticationService().authenticateAsGuest();
        String ticket = getAuthenticationService().getCurrentTicket();

        client.setAuthenticationTicket(ticket);

        // Mark the client as being a guest logon

        client.setGuest(true);
    }

    /**
     * Perform passthru authentication
     * 
     * @param client Client information
     * @param sess Server session
     * @return boolean
     */
    private final boolean doPassthruUserAuthentication(ClientInfo client, SrvSession sess) {

        // Authenticate the FTP user by opening a session to a remote CIFS server
        
        boolean authSts = false;
        AuthenticateSession authSess = null;
        
        try
        {
            // Try and map the client address to a domain
            
            String domain = mapClientAddressToDomain( sess.getRemoteAddress());
            
            authSess = m_passthruServers.openSession( false, domain);
            
            if (authSess != null)
            {
                // Use the challenge key returned from the authentication server to generate the hashed password

                byte[] challenge = authSess.getEncryptionKey();
                byte[] ntlmHash = m_passwordEncryptor.generateEncryptedPassword( client.getPasswordAsString(), challenge, PasswordEncryptor.NTLM1, client.getUserName(), null);
                
                // Run the passthru authentication second stage
                
                authSess.doSessionSetup(client.getDomain(), client.getUserName(), null, null, ntlmHash, 0);
                
                // Check if the user has been logged on as a guest

                if (authSess.isGuest())
                {
                    //  Get a guest authentication token
                        
                    doGuestLogon((AlfrescoClientInfo) client, sess);
                        
                    // Allow the user access as a guest

                    authSts = true;

                    // Debug

                    if (logger.isDebugEnabled())
                        logger.debug("Passthru authenticate user=" + client.getUserName() + ", GUEST");
                }
                else
                {
                    // Set the current user to be authenticated, save the authentication token

                    AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
                    getAuthenticationComponent().setCurrentUser(client.getUserName());                   
                    alfClient.setAuthenticationTicket(getAuthenticationService().getCurrentTicket());

                    // Passwords match, grant access

                    authSts = true;
                    client.setLogonType( ClientInfo.LogonNormal);

                    // Logging

                    if ( logger.isInfoEnabled())
                        logger.info("Logged on user " + client.getUserName() + " ( address " + sess.getRemoteAddress() + ")");
                }
                
                // Close the passthru authentication session
                
                authSess.CloseSession();
                authSess = null;
            }
        }
        catch (Exception ex)
        {
            logger.debug("Passthru error", ex);
        }
        finally {
            
            // Make sure the authentication session has been closed
            
            if ( authSess != null) {
                try {
                    authSess.CloseSession();
                }
                catch( Exception ex) {
                }
            }
        }

        // Return the logon status
        
        return authSts;
    }

    /**
     * Map a client IP address to a domain
     * 
     * @param clientIP InetAddress
     * @return String
     */
    protected final String mapClientAddressToDomain(InetAddress clientIP) {

        // Check if there are any domain mappings

        if ( !getSecurityConfig().hasDomainMappings() )
            return null;

        // Convert the client IP address to an integer value

        int clientAddr = IPAddress.asInteger(clientIP);

        for (DomainMapping domainMap : getSecurityConfig().getDomainMappings()) {

            if ( domainMap.isMemberOfDomain(clientAddr)) {

                // DEBUG

                if ( logger.isDebugEnabled())
                    logger.debug("Mapped client IP " + clientIP + " to domain " + domainMap.getDomain());

                return domainMap.getDomain();
            }
        }

        // DEBUG

        if ( logger.isDebugEnabled())
            logger.debug("Failed to map client IP " + clientIP + " to a domain");

        // No domain mapping for the client address

        return null;
    }
    
    /**
     * Close the authenticator
     */
    public void closeAuthenticator()
    {
        super.closeAuthenticator();
        
        // Close the passthru authentication server list
        
        if ( m_localPassThruServers && m_passthruServers != null)
            m_passthruServers.shutdown();
    }
}
