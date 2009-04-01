/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.filesys.auth.ftp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.StringTokenizer;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import net.sf.acegisecurity.Authentication;

import org.alfresco.config.ConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.ServerConfigurationBean;
import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.jlan.ftp.FTPSrvSession;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.PasswordEncryptor;
import org.alfresco.jlan.server.auth.passthru.AuthSessionFactory;
import org.alfresco.jlan.server.auth.passthru.AuthenticateSession;
import org.alfresco.jlan.server.auth.passthru.DomainMapping;
import org.alfresco.jlan.server.auth.passthru.PassthruServers;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.smb.Protocol;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.repo.security.authentication.NTLMMode;

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

    // Password encryption, for CIFS NTLM style encryption/hashing
    
    private PasswordEncryptor m_passwordEncryptor;
    
    private Integer timeout;
    
    private String server;

    private String domain;

    private String protocolOrder;
    
    private Integer offlineCheckInterval;
    
    public void setTimeout(Integer timeout)
    {
        this.timeout = timeout;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }
    
    public void setProtocolOrder(String protocolOrder)
    {
        this.protocolOrder = protocolOrder;
    }
    
    public void setOfflineCheckInterval(Integer offlineCheckInterval)
    {
        this.offlineCheckInterval = offlineCheckInterval;
    }
    
    protected SecurityConfigSection getSecurityConfig()
    {
        return (SecurityConfigSection) this.serverConfiguration.getConfigSection(SecurityConfigSection.SectionName);
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

        // Check if the offline check interval has been specified
        
        ConfigElement checkInterval = params.getChild("offlineCheckInterval");
        if ( checkInterval != null)
        {
            try
            {
                // Validate the check interval value

                setOfflineCheckInterval(Integer.parseInt(checkInterval.getValue()));
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

                setTimeout(Integer.parseInt(sessTmoElem.getValue()));

            }
            catch (NumberFormatException ex)
            {
                throw new InvalidConfigurationException("Invalid timeout value specified");
            }
        }

        // Get the server configuration bean
        
        ServerConfigurationBean configBean = null;
        
        if ( config instanceof ServerConfigurationBean)
            configBean = (ServerConfigurationBean) config;
        
        // Check if the local server should be used

		if ( params.getChild("LocalServer") != null && configBean != null) {

            // Get the local server name, trim the domain name

            String server = configBean.getLocalServerName( true);
            if ( server == null)
                throw new AlfrescoRuntimeException("Passthru authenticator failed to get local server name");

            setServer(server);            
        }

        // Check if a server name has been specified

        ConfigElement srvNamesElem = params.getChild("Server");

        if (srvNamesElem != null && srvNamesElem.getValue().length() > 0)
        {
            setServer(srvNamesElem.getValue());
        }

        // Check if the local domain/workgroup should be used

        if ( params.getChild("LocalDomain") != null && configBean != null) {
            
            // Get the local domain/workgroup name

            setDomain(configBean.getLocalDomainName());
        }

        // Check if a domain name has been specified

        ConfigElement domNameElem = params.getChild("Domain");

        if (domNameElem != null && domNameElem.getValue().length() > 0)
        {

            setDomain(domNameElem.getValue());
        }

        // Check if a protocol order has been set

        ConfigElement protoOrderElem = params.getChild("ProtocolOrder");

        if (protoOrderElem != null && protoOrderElem.getValue().length() > 0)
        {
            setProtocolOrder(protoOrderElem.getValue());
        }
        
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

        if (getAuthenticationComponent().getNTLMMode() == NTLMMode.MD4_PROVIDER)
            throw new AlfrescoRuntimeException(
                    "Wrong authentication setup for passthru authenticator (cannot be used with Alfresco users)");

        // Create the password encryptor
        
        m_passwordEncryptor = new PasswordEncryptor();
        
        // Check if the offline check interval has been specified
        
        if ( this.offlineCheckInterval != null)
        {
            // Range check the value
            
            if ( this.offlineCheckInterval < MinCheckInterval || this.offlineCheckInterval > MaxCheckInterval)
                throw new InvalidConfigurationException("Invalid offline check interval, valid range is " + MinCheckInterval + " to " + MaxCheckInterval);
            
            // Set the offline check interval for offline passthru servers
            
            m_passthruServers = new PassthruServers( this.offlineCheckInterval);
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
            	logger.debug("Using offline check interval of " + this.offlineCheckInterval + " seconds");
        }
        else
        {
        	// Create the passthru server list with the default offline check interval
        	
        	m_passthruServers = new PassthruServers();
        }
        
        // Check if the session timeout has been specified

        if (this.timeout != null)
        {

            // Range check the timeout

            if (this.timeout < MinSessionTmo || this.timeout > MaxSessionTmo)
                throw new InvalidConfigurationException("Invalid session timeout, valid range is " + MinSessionTmo
                        + " to " + MaxSessionTmo);

            // Set the session timeout for connecting to an authentication server

            m_passthruServers.setConnectionTimeout(this.timeout);
        }
        
        // Check if a server name has been specified

        String srvList = null;

        if ( this.server != null && this.server.length() > 0) {

            // Get the passthru authenticator server name

            srvList = this.server;
        }

        // If the passthru server name has been set initialize the passthru connection

        if ( srvList != null) {
            // Initialize using a list of server names/addresses

            m_passthruServers.setServerList(srvList);
        }
        else {

            // Get the domain/workgroup name

            String domainName = null;

            // Check if a domain name has been specified

            if ( this.domain != null && this.domain.length() > 0) {

                // Check if the authentication server has already been set, ie. server name was also
                // specified

                if ( srvList != null)
                    throw new AlfrescoRuntimeException("Specify server or domain name for passthru authentication");

                domainName = this.domain;
            }

            // If the domain name has been set initialize the passthru connection

            if ( domainName != null) {
                try {
                    // Initialize using the domain

                    m_passthruServers.setDomain(domainName);
                }
                catch (IOException ex) {
                    throw new AlfrescoRuntimeException("Error setting passthru domain, " + ex.getMessage());
                }
            }
        }

        // Check if a protocol order has been set
        
        if ( this.protocolOrder != null && this.protocolOrder.length() > 0)
        {
            // Parse the protocol order list
            
            StringTokenizer tokens = new StringTokenizer( this.protocolOrder, ",");
            int primaryProto = Protocol.None;
            int secondaryProto = Protocol.None;
    
            // There should only be one or two tokens
            
            if ( tokens.countTokens() > 2)
                throw new AlfrescoRuntimeException("Invalid protocol order list, " + this.protocolOrder);
            
            // Get the primary protocol
            
            if ( tokens.hasMoreTokens())
            {
                // Parse the primary protocol
                
                String primaryStr = tokens.nextToken();
                
                if ( primaryStr.equalsIgnoreCase( "TCPIP"))
                    primaryProto = Protocol.NativeSMB;
                else if ( primaryStr.equalsIgnoreCase( "NetBIOS"))
                    primaryProto = Protocol.TCPNetBIOS;
                else
                    throw new AlfrescoRuntimeException("Invalid protocol type, " + primaryStr);
                
                // Check if there is a secondary protocol, and validate
                
                if ( tokens.hasMoreTokens())
                {
                    // Parse the secondary protocol
                    
                    String secondaryStr = tokens.nextToken();
                    
                    if ( secondaryStr.equalsIgnoreCase( "TCPIP") && primaryProto != Protocol.NativeSMB)
                        secondaryProto = Protocol.NativeSMB;
                    else if ( secondaryStr.equalsIgnoreCase( "NetBIOS") && primaryProto != Protocol.TCPNetBIOS)
                        secondaryProto = Protocol.TCPNetBIOS;
                    else
                        throw new AlfrescoRuntimeException("Invalid secondary protocol, " + secondaryStr);
                }
            }
            
            // Set the protocol order used for passthru authentication sessions
            
            AuthSessionFactory.setProtocolOrder( primaryProto, secondaryProto);
            
            // DEBUG
            
            if (logger.isDebugEnabled())
                logger.debug("Protocol order primary=" + Protocol.asString(primaryProto) + ", secondary=" + Protocol.asString(secondaryProto));
        }
        
        // Check if we have an authentication server

        if ( m_passthruServers.getTotalServerCount() == 0)
            throw new AlfrescoRuntimeException("No valid authentication servers found for passthru");
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
        Authentication authToken = getAuthenticationComponent().getCurrentAuthentication();

        client.setAuthenticationToken(authToken);

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
                    alfClient.setAuthenticationToken(getAuthenticationComponent().setCurrentUser(client.getUserName()));

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
            logger.error("Passthru error", ex);
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
        
        if ( m_passthruServers != null)
            m_passthruServers.shutdown();
    }
}
