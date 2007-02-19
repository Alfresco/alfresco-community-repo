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
package org.alfresco.repo.security.authentication.ntlm;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Hashtable;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.AuthenticationServiceException;
import net.sf.acegisecurity.BadCredentialsException;
import net.sf.acegisecurity.CredentialsExpiredException;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.server.auth.PasswordEncryptor;
import org.alfresco.filesys.server.auth.passthru.AuthenticateSession;
import org.alfresco.filesys.server.auth.passthru.PassthruServers;
import org.alfresco.filesys.smb.SMBException;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * NTLM Authentication Component Class
 * 
 * <p>Provides authentication using passthru to a Windows server(s)/domain controller(s) using the accounts
 * defined on the passthru server to validate users.
 * 
 * @author GKSpencer
 */
public class NTLMAuthenticationComponentImpl extends AbstractAuthenticationComponent
{
    // Logging
    
    private static final Log logger = LogFactory.getLog("org.alfresco.passthru.auth");

    // Constants
    //
    // Standard authorities
    
    public static final String NTLMAuthorityGuest         = "Guest";
    public static final String NTLMAuthorityAdministrator = "Administrator";
    
    // Active session timeout
    
    private static final long DefaultSessionTimeout = 60000L;   // 1 minute
    private static final long MinimumSessionTimeout = 5000L;    // 5 seconds
    
    // Passthru authentication servers
    
    private PassthruServers m_passthruServers;
    
    // Password encryptor for generating password hash for local authentication
    
    private PasswordEncryptor m_encryptor;
    
    // Allow guest access
    
    private boolean m_allowGuest;
    
    // Table of currently active passthru authentications and the associated authentication session
    //
    // If the two authentication stages are not completed within a reasonable time the authentication
    // session will be closed by the reaper thread.
    
    private Hashtable<NTLMPassthruToken,AuthenticateSession> m_passthruSessions;
    
    // Active authentication session timeout, in milliseconds
    
    private long m_passthruSessTmo = DefaultSessionTimeout;
    
    // Authentication session reaper thread
    
    private PassthruReaperThread m_reaperThread;
    
    // Person service, used to map passthru usernames to Alfresco person names
    
    private PersonService m_personService;
    private NodeService m_nodeService;
    
    /**
     * Passthru Session Reaper Thread
     */
    class PassthruReaperThread extends Thread
    {
        // Thread shutdown request flag
        
        private boolean m_ishutdown;

        // Reaper wakeup interval, in milliseconds
        
        private long m_wakeupInterval = m_passthruSessTmo / 2;
        
        /**
         * Default constructor
         */
        PassthruReaperThread()
        {
            setDaemon(true);
            setName("PassthruReaper");
            start();
        }
        
        /**
         * Set the wakeup interval
         * 
         * @param wakeup long
         */
        public final void setWakeup(long wakeup)
        {
            m_wakeupInterval = wakeup;
        }
        
        /**
         * Main thread code
         */
        public void run()
        {
            // Loop until shutdown
            
            m_ishutdown = false;
            
            while ( m_ishutdown == false)
            {
                // Sleep for a while
                
                try
                {
                    sleep( m_wakeupInterval);
                }
                catch ( InterruptedException ex)
                {
                }
                
                // Check if there are any active sessions to check
                
                if ( m_passthruSessions.size() > 0)
                {
                    // Enumerate the active sessions
                    
                    Enumeration<NTLMPassthruToken> tokenEnum = m_passthruSessions.keys();
                    long timeNow = System.currentTimeMillis();
                    
                    while (tokenEnum.hasMoreElements())
                    {
                        // Get the current NTLM token and check if it has expired
                        
                        NTLMPassthruToken ntlmToken = tokenEnum.nextElement();
                        
                        if ( ntlmToken != null && ntlmToken.getAuthenticationExpireTime() < timeNow)
                        {
                            // Authentication token has expired, close the associated authentication session
                            
                            AuthenticateSession authSess = m_passthruSessions.get(ntlmToken);
                            if ( authSess != null)
                            {
                                try
                                {
                                    // Close the authentication session
                                    
                                    authSess.CloseSession();
                                }
                                catch ( Exception ex)
                                {
                                    // Debug
                                    
                                    if(logger.isDebugEnabled())
                                        logger.debug("Error closing expired authentication session", ex);
                                }
                            }
                            
                            // Remove the expired token from the active list
                            
                            m_passthruSessions.remove(ntlmToken);
                            
                            // Debug
                            
                            if(logger.isDebugEnabled())
                                logger.debug("Removed expired NTLM token " + ntlmToken);
                        }
                    }
                }
            }
            
            // Debug
            
            if(logger.isDebugEnabled())
                logger.debug("Passthru reaper thread shutdown");
        }
        
        /**
         * Shutdown the reaper thread
         */
        public final void shutdownRequest()
        {
            m_ishutdown = true;
            this.interrupt();
        }
    }

    /**
     * Class constructor
     */
    public NTLMAuthenticationComponentImpl() {
        
        // Create the passthru authentication server list
        
        m_passthruServers = new PassthruServers();
        
        // Create the password encryptor for local password hashing
        
        m_encryptor = new PasswordEncryptor();
        
        // Create the active session list and reaper thread
        
        m_passthruSessions = new Hashtable<NTLMPassthruToken,AuthenticateSession>();
        m_reaperThread = new PassthruReaperThread();
    }

    /**
     * Determine if guest logons are allowed
     * 
     * @return boolean
     */
    public final boolean allowsGuest()
    {
        return m_allowGuest;
    }
    
    /**
     * Set the domain to authenticate against
     * 
     * @param domain String
     */
    public void setDomain(String domain) {
        
        // Check if the passthru server list is already configured
        
        if ( m_passthruServers.getTotalServerCount() > 0)
            throw new AlfrescoRuntimeException("Passthru server list already configured");
        
        // Configure the passthru authentication server list using the domain controllers
        
        m_passthruServers.setDomain(domain);
    }
    
    /**
     * Set the server(s) to authenticate against
     * 
     * @param servers String
     */
    public void setServers(String servers) {
        
        // Check if the passthru server list is already configured
        
        if ( m_passthruServers.getTotalServerCount() > 0)
            throw new AlfrescoRuntimeException("Passthru server list already configured");
        
        // Configure the passthru authenticaiton list using a list of server names/addresses
        
        m_passthruServers.setServerList(servers);
    }
    
    /**
     * Use the local server as the authentication server
     * 
     * @param useLocal String
     */
    public void setUseLocalServer(String useLocal)
    {
        // Check if the local server should be used for authentication
        
        if ( Boolean.parseBoolean(useLocal) == true)
        {
            // Check if the passthru server list is already configured
            
            if ( m_passthruServers.getTotalServerCount() > 0)
                throw new AlfrescoRuntimeException("Passthru server list already configured");

            try
            {
                // Get the list of local network addresses
                
                InetAddress[] localAddrs = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
                
                // Build the list of local addresses
                
                if ( localAddrs != null && localAddrs.length > 0)
                {
                    StringBuilder addrStr = new StringBuilder();
                
                    for ( InetAddress curAddr  : localAddrs)
                    {
                        if ( curAddr.isLoopbackAddress() == false)
                        {
                            addrStr.append(curAddr.getHostAddress());
                            addrStr.append(",");
                        }
                    }
                    
                    if ( addrStr.length() > 0)
                        addrStr.setLength(addrStr.length() - 1);
                    
                    // Set the server list using the local address list
                    
                    m_passthruServers.setServerList(addrStr.toString());
                }
                else
                    throw new AlfrescoRuntimeException("No local server address(es)");
            }
            catch ( UnknownHostException ex)
            {
                throw new AlfrescoRuntimeException("Failed to get local address list");
            }
        }
    }
    
    /**
     * Allow guest access
     * 
     * @param guest String
     */
    public void setGuestAccess(String guest)
    {
        m_allowGuest = Boolean.parseBoolean(guest);
    }
    
    /**
     * Set the JCE provider
     * 
     * @param providerClass String
     */
    public void setJCEProvider(String providerClass)
    {
        // Set the JCE provider, required to provide various encryption/hashing algorithms not available
        // in the standard Sun JDK/JRE
        
        try
        {

            // Load the JCE provider class and validate

            Object jceObj = Class.forName(providerClass).newInstance();
            if (jceObj instanceof java.security.Provider)
            {

                // Inform listeners, validate the configuration change

                Provider jceProvider = (Provider) jceObj;

                // Add the JCE provider

                Security.addProvider(jceProvider);
                
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Using JCE provider " + providerClass);
            }
            else
            {
                throw new AlfrescoRuntimeException("JCE provider class is not a valid Provider class");
            }
        }
        catch (ClassNotFoundException ex)
        {
            throw new AlfrescoRuntimeException("JCE provider class " + providerClass + " not found");
        }
        catch (Exception ex)
        {
            throw new AlfrescoRuntimeException("JCE provider class error", ex);
        }
    }
    
    /**
     * Set the authentication session timeout, in seconds
     * 
     * @param sessTmo String
     */
    public void setSessionTimeout(String sessTmo)
    {
        // Convert to an integer value and range check the timeout value
        
        try
        {
            // Convert to an integer value
            
            long sessTmoMilli = Long.parseLong(sessTmo) * 1000L;
            
            if ( sessTmoMilli < MinimumSessionTimeout)
                throw new AlfrescoRuntimeException("Authentication session timeout too low, " + sessTmo);
            
            // Set the authentication session timeout value
            
            m_passthruSessTmo = sessTmoMilli;
            
            // Set the reaper thread wakeup interval
            
            m_reaperThread.setWakeup( sessTmoMilli / 2);
        }
        catch(NumberFormatException ex)
        {
            throw new AlfrescoRuntimeException("Invalid authenication session timeout value");
        }
    }
    
    /**
     * Set the person service
     * 
     * @param personService PersonService
     */
    public final void setPersonService(PersonService personService)
    {
        m_personService = personService;
    }
    
    /**
     * Set the node service
     * 
     * @param nodeService NodeService
     */
    public final void setNodeService(NodeService nodeService)
    {
        m_nodeService = nodeService;
    }
    
    /**
     * Return the authentication session timeout, in milliseconds
     * 
     * @return long
     */
    private final long getSessionTimeout()
    {
        return m_passthruSessTmo;
    }
    
    /**
     * Authenticate
     * 
     * @param userName String
     * @param password char[]
     * @throws AuthenticationException
     */     
    public void authenticate(String userName, char[] password) throws AuthenticationException
    {
        // Debug
        
        if ( logger.isDebugEnabled())
            logger.debug("Authenticate user=" + userName + " via local credentials");
        
        // Create a local authentication token
        
        NTLMLocalToken authToken = new NTLMLocalToken(userName, new String(password));
        
        // Authenticate using the token
        
        authenticate( authToken);
    }

    /**
     * Authenticate using a token
     * 
     * @param token Authentication
     * @return Authentication
     * @throws AuthenticationException
     */
    public Authentication authenticate(Authentication auth) throws AuthenticationException
    {
        // DEBUG
        
        if ( logger.isDebugEnabled())
            logger.debug("Authenticate " + auth + " via token");
        
        // Check if the token is for passthru authentication
        
        if( auth instanceof NTLMPassthruToken)
        {
            // Access the NTLM passthru token
            
            NTLMPassthruToken ntlmToken = (NTLMPassthruToken) auth;
            
            // Authenticate using passthru
            
            authenticatePassthru(ntlmToken);
        }

        // Check for a local authentication token
        
        else if( auth instanceof NTLMLocalToken)
        {
            AuthenticateSession authSess = null;
            
            try
            {

                // Access the NTLM token
                
                NTLMLocalToken ntlmToken = (NTLMLocalToken) auth;
    
                // Open a session to an authentication server
                
                authSess = m_passthruServers.openSession();
                
                if ( authSess == null)
                	throw new AuthenticationException("Failed to open session to passthru server");
                
                // Authenticate using the credentials supplied
                    
                authenticateLocal(ntlmToken, authSess);
            }
            finally
            {
                // Make sure the authentication session is closed
                
                if ( authSess != null)
                {
                    try
                    {
                        authSess.CloseSession();
                    }
                    catch ( Exception ex)
                    {
                    }
                }
            }
        }
        else
        {
            // Unsupported authentication token
            
            throw new AuthenticationException("Unsupported authentication token type");
        }

        // Return the updated authentication token
        
        return getCurrentAuthentication();
    }
    
    /**
     * Get the enum that describes NTLM integration
     * 
     * @return NTLMMode
     */
    public NTLMMode getNTLMMode()
    {
        return NTLMMode.PASS_THROUGH;
    }

    /**
     * Get the MD4 password hash, as required by NTLM based authentication methods.
     * 
     * @param userName String
     * @return String
     */
    public String getMD4HashedPassword(String userName)
    {
        // Do not support MD4 hashed password
        
        throw new AlfrescoRuntimeException("MD4 passwords not supported");
    }
    
    /**
     * Authenticate a user using local credentials
     * 
     * @param ntlmToken NTLMLocalToken
     * @param authSess AuthenticateSession
     */
    private void authenticateLocal(NTLMLocalToken ntlmToken, AuthenticateSession authSess)
    {
        try
        {
            // Get the plaintext password and generate an NTLM1 password hash
            
            String username = (String) ntlmToken.getPrincipal();
            String plainPwd = (String) ntlmToken.getCredentials();
            byte[] ntlm1Pwd = m_encryptor.generateEncryptedPassword( plainPwd, authSess.getEncryptionKey(), PasswordEncryptor.NTLM1, null, null);
            
            // Send the logon request to the authentication server
            //
            // Note: Only use the stronger NTLM hash, we do not send the LM hash
            
            authSess.doSessionSetup(username, null, ntlm1Pwd);
            
            // Check if the session has logged on as a guest
            
            if ( authSess.isGuest() || username.equalsIgnoreCase("GUEST"))
            {
                // If guest access is enabled add a guest authority to the token
                
                if ( allowsGuest())
                {
                    // Set the guest authority
                    
                    GrantedAuthority[] authorities = new GrantedAuthority[2];
                    authorities[0] = new GrantedAuthorityImpl(NTLMAuthorityGuest);
                    authorities[1] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
                    
                    ntlmToken.setAuthorities(authorities);
                }
                else
                {
                    // Guest access not allowed
                    
                    throw new AuthenticationException("Guest logons disabled");
                }
            }
            else
            {
                // Set authorities
                
                GrantedAuthority[] authorities = new GrantedAuthority[1];
                authorities[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
                
                ntlmToken.setAuthorities(authorities);
            }
            
            // Indicate that the token is authenticated
            
            ntlmToken.setAuthenticated(true);
            
            // Map the passthru username to an Alfresco person
            
            NodeRef userNode = m_personService.getPerson(username);
            if ( userNode != null)
            {
                // Get the person name and use that as the current user to line up with permission checks
                
                String personName = (String) m_nodeService.getProperty(userNode, ContentModel.PROP_USERNAME);
                setCurrentUser(personName);
                
                // DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Setting current user using person " + personName + " (username " + username + ")");
            }
            else
            {
                // Set using the user name
                
              
                setCurrentUser( username);
                
                // DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Setting current user using username " + username);
            }
            
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Authenticated token=" + ntlmToken);
        }
        catch (NoSuchAlgorithmException ex)
        {
            // JCE provider does not have the required encryption/hashing algorithms
            
            throw new AuthenticationServiceException("JCE provider error", ex);
        }
        catch (InvalidKeyException ex)
        {
            // Problem creating key during encryption
            
            throw new AuthenticationServiceException("Invalid key error", ex);
        }
        catch (IOException ex)
        {
            // Error connecting to the authentication server
            
            throw new AuthenticationServiceException("I/O error", ex);
        }
        catch (SMBException ex)
        {
            // Check the returned status code to determine why the logon failed and throw an appropriate exception
            
            if ( ex.getErrorClass() == SMBStatus.NTErr)
            {
                AuthenticationException authEx = null;
                
                switch( ex.getErrorCode())
                {
                case SMBStatus.NTLogonFailure:
                    authEx = new AuthenticationException("Logon failure");
                    break;
                case SMBStatus.NTAccountDisabled:
                    authEx = new AuthenticationException("Account disabled");
                    break;
                default:
                    authEx = new AuthenticationException("Logon failure");
                break;
                }
                
                throw authEx;
            }
            else
                throw new AuthenticationException("Logon failure");
        }
    }
    
    /**
     * Authenticate using passthru authentication with a client
     * 
     * @param ntlmToken NTLMPassthruToken
     */
    private void authenticatePassthru(NTLMPassthruToken ntlmToken)
    {
        // Check if the token has an authentication session, if not then it is either a new token
        // or the session has been timed out
        
        AuthenticateSession authSess = m_passthruSessions.get(ntlmToken);
        
        if ( authSess == null)
        {
            // Check if the token has a challenge, if it does then the associated session has been
            // timed out
            
            if ( ntlmToken.getChallenge() != null)
                throw new CredentialsExpiredException("Authentication session expired");
            
            // Open an authentication session for the new token and add to the active session list
            
            authSess = m_passthruServers.openSession();
            
            // Check if the session was opened to the passthru server
            
            if ( authSess == null)
            	throw new AuthenticationServiceException("Failed to open passthru auth session");
            
            ntlmToken.setAuthenticationExpireTime(System.currentTimeMillis() + getSessionTimeout());
            
            // Get the challenge from the initial session negotiate stage
            
            ntlmToken.setChallenge(new NTLMChallenge(authSess.getEncryptionKey()));

            StringBuilder details = new StringBuilder();

            // Build a details string with the authentication session details
            
            details.append(authSess.getDomain());
            details.append("\\");
            details.append(authSess.getPCShare().getNodeName());
            details.append(",");
            details.append(authSess.getSession().getProtocolName());
            
            ntlmToken.setDetails(details.toString());

            // Put the token/session into the active session list
            
            m_passthruSessions.put(ntlmToken, authSess);
            
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Passthru stage 1 token " + ntlmToken);
        }
        else
        {
            try
            {
                // Stage two of the authentication, send the hashed password to the authentication server
            
                byte[] lmPwd = null;
                byte[] ntlmPwd = null;
                
                if ( ntlmToken.getPasswordType() == PasswordEncryptor.LANMAN)
                    lmPwd = ntlmToken.getHashedPassword();
                else if ( ntlmToken.getPasswordType() == PasswordEncryptor.NTLM1)
                    ntlmPwd = ntlmToken.getHashedPassword();
                
                String username = (String) ntlmToken.getPrincipal();
                
                authSess.doSessionSetup(username, lmPwd, ntlmPwd);
                
                // Check if the session has logged on as a guest
                
                if ( authSess.isGuest() || username.equalsIgnoreCase("GUEST"))
                {
                    // If guest access is enabled add a guest authority to the token
                    
                    if ( allowsGuest())
                    {
                        // Set the guest authority
                        
                        GrantedAuthority[] authorities = new GrantedAuthority[1];
                        authorities[0] = new GrantedAuthorityImpl(NTLMAuthorityGuest);
                        
                        ntlmToken.setAuthorities(authorities);
                    }
                    else
                    {
                        // Guest access not allowed
                        
                        throw new BadCredentialsException("Guest logons disabled");
                    }
                }
                
                // Indicate that the token is authenticated
                
                ntlmToken.setAuthenticated(true);

                // Map the passthru username to an Alfresco person
                
                NodeRef userNode = m_personService.getPerson(username);
                if ( userNode != null)
                {
                    // Get the person name and use that as the current user to line up with permission checks
                    
                    String personName = (String) m_nodeService.getProperty(userNode, ContentModel.PROP_USERNAME);
                    setCurrentUser(personName);
                    
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Setting current user using person " + personName + " (username " + username + ")");
                }
                else
                {
                    // Set using the user name
                    
                    setCurrentUser( username);
                    
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Setting current user using username " + username);
                }
            }                
            catch (IOException ex)
            {
                // Error connecting to the authentication server
                
                throw new AuthenticationServiceException("I/O error", ex);
            }
            catch (SMBException ex)
            {
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Passthru exception, " + ex);
                
                // Check the returned status code to determine why the logon failed and throw an appropriate exception
                
                if ( ex.getErrorClass() == SMBStatus.NTErr)
                {
                    AuthenticationException authEx = null;
                    
                    switch( ex.getErrorCode())
                    {
                    case SMBStatus.NTLogonFailure:
                        authEx = new AuthenticationException("Logon failure");
                        break;
                    case SMBStatus.NTAccountDisabled:
                        authEx = new AuthenticationException("Account disabled");
                        break;
                    default:
                        authEx = new AuthenticationException("Logon failure");
                    break;
                    }
                    
                    throw authEx;
                }
                else
                    throw new BadCredentialsException("Logon failure");
            }
            finally
            {
                // Make sure the authentication session is closed
                
                if ( authSess != null)
                {
                    try
                    {
                        // Remove the session from the active list
                        
                        m_passthruSessions.remove(ntlmToken);
                        
                        // Close the session to the authentication server
                        
                        authSess.CloseSession();
                    }
                    catch (Exception ex)
                    {
                    }
                }
            }
        }
    }

    /**
     * Check if the user exists
     * 
     * @param userName String
     * @return boolean
     */
    public boolean exists(String userName)
    {
       throw new UnsupportedOperationException();
    }

    @Override
    protected boolean implementationAllowsGuestLogin()
    {
       return allowsGuest();
    }
    
    
}
