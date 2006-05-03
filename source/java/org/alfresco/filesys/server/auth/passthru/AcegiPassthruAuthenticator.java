/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Alfresco Network License. You may obtain a
 * copy of the License at
 *
 *   http://www.alfrescosoftware.com/legal/
 *
 * Please view the license relevant to your network subscription.
 *
 * BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 * READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 * YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 * ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 * THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 * AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 * TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 * BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 * HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 * SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 * TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 * CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
 */
package org.alfresco.filesys.server.auth.passthru;

import java.util.List;

import net.sf.acegisecurity.AuthenticationManager;
import net.sf.acegisecurity.providers.ProviderManager;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.auth.SrvAuthenticator;
import org.alfresco.filesys.server.auth.UserAccount;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.repo.security.authentication.ntlm.NTLMAuthenticationProvider;
import org.alfresco.repo.security.authentication.ntlm.NTLMPassthruToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Passthru authenticator implementation that uses the Acegi NTLM passthru authentication provider
 * 
 * @author GKSpencer
 */
public class AcegiPassthruAuthenticator extends SrvAuthenticator
{
    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.auth");
    
    // Constants
    //
    // Default authentication manager bean name
    
    private static final String DefaultAuthManagerName = "authenticationManager";
    
    // Acegi authentication manager
    
    private AuthenticationManager m_authMgr;
    
    /**
     * Default constructor
     */
    public AcegiPassthruAuthenticator()
    {
        setAccessMode(SrvAuthenticator.USER_MODE);
        setEncryptedPasswords(true);
    }
    
    /**
     * Authenticate the connection to a particular share, called when the SMB server is in share
     * security mode
     * 
     * @param client ClientInfo
     * @param share SharedDevice
     * @param sharePwd String
     * @param sess SrvSession
     * @return int
     */
    public int authenticateShareConnect(ClientInfo client, SharedDevice share, String sharePwd, SrvSession sess)
    {
        return SrvAuthenticator.Writeable;
    }

    /**
     * Authenticate a session setup by a user
     * 
     * @param client ClientInfo
     * @param sess SrvSession
     * @param alg int
     * @return int
     */
    public int authenticateUser(ClientInfo client, SrvSession sess, int alg)
    {
        // Get the authentication token for the session

        NTLMPassthruToken authToken = (NTLMPassthruToken) sess.getAuthenticationToken();
        
        if ( authToken == null)
            return SrvAuthenticator.AUTH_DISALLOW;

        // Get the appropriate hashed password for the algorithm
        
        int authSts = SrvAuthenticator.AUTH_DISALLOW;
        byte[] hashedPassword = null;
        
        if ( alg == NTLM1)
            hashedPassword = client.getPassword();
        else if ( alg == LANMAN)
            hashedPassword = client.getANSIPassword();
        else
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Invalid algorithm specified for user authentication (" + alg + ")");
            
            // Invalid/unsupported algorithm specified
            
            return SrvAuthenticator.AUTH_DISALLOW;
        }
        
        // Set the username and hashed password in the authentication token
        
        authToken.setUserAndPassword( client.getUserName(), hashedPassword, alg);
        
        // Authenticate the user
        
        try
        {
            // Run the second stage of the passthru authentication
            
            m_authMgr.authenticate( authToken);
            
            // Check if the user has been logged on as a guest

            if (authToken.isGuestLogon())
            {

                // Check if the local server allows guest access

                if (allowGuest() == true)
                {

                    // Allow the user access as a guest

                    authSts = SrvAuthenticator.AUTH_GUEST;

                    // Debug

                    if (logger.isDebugEnabled())
                        logger.debug("Acegi passthru authenticate user=" + client.getUserName() + ", GUEST");
                }
            }
            else
            {

                // Allow the user full access to the server

                authSts = SrvAuthenticator.AUTH_ALLOW;

                // Debug

                if (logger.isDebugEnabled())
                    logger.debug("Acegi passthru authenticate user=" + client.getUserName() + ", FULL");
            }
        }
        catch ( Exception ex)
        {
            // Log the error
            
            if ( logger.isErrorEnabled())
                logger.error("Logon failure, " + ex.getMessage());
        }
        
        // Clear the authentication token
        
        sess.setAuthenticationToken(null);
        
        // Return the authentication status
        
        return authSts;
    }

    /**
     * Get user account details for the specified user
     * 
     * @param user String
     * @return UserAccount
     */
    public UserAccount getUserDetails(String user)
    {
        // No user details to return

        return null;
    }

    /**
     * Get a challenge key for a new session
     * 
     * @param sess SrvSession
     * @return byte[]
     */
    public byte[] getChallengeKey(SrvSession sess)
    {
        // Create an authentication token for the session
        
        NTLMPassthruToken authToken = new NTLMPassthruToken();
        
        // Run the first stage of the passthru authentication to get the challenge
        
        m_authMgr.authenticate( authToken);
        
        // Save the authentication token for the second stage of the authentication
        
        sess.setAuthenticationToken(authToken);
        
        // Debug
        
        if ( logger.isDebugEnabled())
            logger.debug("Created new passthru token " + authToken);
        
        // Get the challenge from the token
        
        if ( authToken.getChallenge() != null)
            return authToken.getChallenge().getBytes();
        return null;
    }

    /**
     * Initialzie the authenticator
     * 
     * @param config ServerConfiguration
     * @param params ConfigElement
     * @exception InvalidConfigurationException
     */
    public void initialize(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
        // Call the base class

        super.initialize(config, params);

        // Check if the configuration has an associated bean factory, if not it looks like we are
        // not running inside Spring
        
        if ( config.getAuthenticationManager() == null)
            throw new InvalidConfigurationException("Acegi authentication manager not available");
        
        // Passthru authenticator only works in user mode
        
        if ( getAccessMode() != USER_MODE)
            throw new InvalidConfigurationException("Acegi authenticator only works in user mode");
        
        // Check if authentication manager is the required type and that the NTLM authentication provider
        // is available.
        
        Object authMgrObj = config.getAuthenticationManager();
        
        if ( authMgrObj instanceof ProviderManager)
        {
            // The required authentication manager is configured, now check if the NTLM provider is configured
            
            ProviderManager providerManager = (ProviderManager) authMgrObj;
            List providerList = providerManager.getProviders();
            
            if ( providerList != null)
            {
                // Check for the NTLM authentication provider
                
                int i = 0;
                boolean foundProvider = false;
                
                while ( i < providerList.size() && foundProvider == false)
                {
                    if ( providerList.get(i++) instanceof NTLMAuthenticationProvider)
                        foundProvider = true;
                }
                
                if (foundProvider == false)
                    throw new InvalidConfigurationException("NTLM authentication provider is not available");
                
                // Save the authentication manager
                
                m_authMgr = (AuthenticationManager) authMgrObj;
            }
            else
                throw new InvalidConfigurationException("No authentication providers available");
        }
        else
            throw new InvalidConfigurationException("Required authentication manager is not configured");
    }    
}
