/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.security.authentication.ntlm;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.AuthenticationServiceException;
import net.sf.acegisecurity.BadCredentialsException;
import net.sf.acegisecurity.CredentialsExpiredException;
import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.auth.PassthruServerFactory;
import org.alfresco.jlan.server.auth.PasswordEncryptor;
import org.alfresco.jlan.server.auth.passthru.AuthSessionFactory;
import org.alfresco.jlan.server.auth.passthru.AuthenticateSession;
import org.alfresco.jlan.server.auth.passthru.PassthruServers;
import org.alfresco.jlan.smb.Protocol;
import org.alfresco.jlan.smb.SMBException;
import org.alfresco.jlan.smb.SMBStatus;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * NTLM Authentication Component Class
 * 
 * <p>Provides authentication using passthru to a Windows server(s)/domain controller(s) using the accounts
 * defined on the passthru server to validate users.
 * 
 * @author GKSpencer
 */
public class NTLMAuthenticationComponentImpl extends AbstractAuthenticationComponent implements NLTMAuthenticator, InitializingBean
{
    // Logging
    
    private static final Log logger = LogFactory.getLog(NTLMAuthenticationComponentImpl.class);

    // Constants
    //
    // Standard authorities
    
    public static final String NTLMAuthorityGuest         = "Guest";
    public static final String NTLMAuthorityAdministrator = "Administrator";
    
    // Active session timeout
    
    private static final long DefaultSessionTimeout = 60000L;   // 1 minute
    private static final long MinimumSessionTimeout = 5000L;    // 5 seconds
    
    // Passthru authentication servers
    
    private PassthruServerFactory m_passthruServerFactory = new PassthruServerFactory();
    private PassthruServers m_passthruServers;
    
    // Password encryptor for generating password hash for local authentication
    
    private PasswordEncryptor m_encryptor;
    
    // Allow guest access
    
    private boolean m_allowGuest;
    
    // Allow authenticated users that do not have an Alfresco person to logon as guest
    
    private boolean m_allowAuthUserAsGuest;
    
    // Table of currently active passthru authentications and the associated authentication session
    //
    // If the two authentication stages are not completed within a reasonable time the authentication
    // session will be closed by the reaper thread.
    
    private Hashtable<NTLMPassthruToken,AuthenticateSession> m_passthruSessions;
    
    // Active authentication session timeout, in milliseconds
    
    private long m_passthruSessTmo = DefaultSessionTimeout;
    
    // Authentication session reaper thread
    
    private PassthruReaperThread m_reaperThread;
    
    // Null domain uses any available server option
    
    private boolean m_nullDomainUseAnyServer;
    
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
                
        // Create the password encryptor for local password hashing
        
        m_encryptor = new PasswordEncryptor();
        
        // Create the active session list and reaper thread
        
        m_passthruSessions = new Hashtable<NTLMPassthruToken,AuthenticateSession>();
        m_reaperThread = new PassthruReaperThread();
    }
    
    

    public void afterPropertiesSet() throws Exception
    {
        if (m_passthruServers == null)
        {
            // Create the passthru authentication server list
            m_passthruServerFactory.afterPropertiesSet();
            
            m_passthruServers = (PassthruServers) m_passthruServerFactory.getObject();
        }            
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
     * Directly sets the passthru server list.
     * 
     * @param servers
     *            a passthru server list, usually created by {@link org.alfresco.filesys.auth.PassthruServerFactory}
     */
    public void setPassthruServers(PassthruServers servers)
    {
        m_passthruServers = servers;
    }

    /**
     * Set the domain to authenticate against
     * 
     * @param domain String
     */
    public void setDomain(String domain) {
        if (domain.length() > 0)
        {
            m_passthruServerFactory.setDomain(domain);
        }
    }
    
    /**
     * Set the server(s) to authenticate against
     * 
     * @param servers String
     */
    public void setServers(String servers) {        
        if (servers.length() > 0)
        {
            m_passthruServerFactory.setServer(servers);
        }
    }
    
    /**
     * Use the local server as the authentication server
     * 
     * @param useLocal String
     */
    public void setUseLocalServer(String useLocal)
    {
        m_passthruServerFactory.setLocalServer(Boolean.parseBoolean(useLocal));
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
     * Allow authenticated users with no alfresco person record to logon with guest access
     * 
     * @param auth String
     */
    public void setAllowAuthUserAsGuest(String auth)
    {
        m_allowAuthUserAsGuest = Boolean.parseBoolean(auth);
    }

    /**
     * Allow null domain passthru logons to use the first available passthru server
     * 
     * @param nullDomain String
     */
    public void setNullDomainUseAnyServer(String nullDomain)
    {
        m_nullDomainUseAnyServer = Boolean.parseBoolean(nullDomain);
        
        // Push the setting to the passthru server component
        
        m_passthruServers.setNullDomainUseAnyServer( m_nullDomainUseAnyServer);
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
                throw new AlfrescoRuntimeException("JCE provider class is not a valid Provider class:" + providerClass);
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
            {
                throw new AlfrescoRuntimeException("Authentication session timeout too low, " + sessTmo);
            }
            
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
     * Set the protocol order for passthru connections
     * 
     * @param protoOrder String
     */
    public void setProtocolOrder(String protoOrder)
    {
        // Parse the protocol order list
        
        StringTokenizer tokens = new StringTokenizer( protoOrder, ",");
        int primaryProto = Protocol.None;
        int secondaryProto = Protocol.None;

        // There should only be one or two tokens
        
        if ( tokens.countTokens() > 2)
            throw new AlfrescoRuntimeException("Invalid protocol order list, " + protoOrder);
        
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
        {
            logger.debug("Protocol order primary=" + Protocol.asString(primaryProto) + ", secondary=" + Protocol.asString(secondaryProto));
        }
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
    protected void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {
        // Debug
        
        if ( logger.isDebugEnabled())
        {
            logger.debug("Authenticate user=" + userName + " via local credentials");
        }
        
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
        {
            logger.debug("Authenticate " + auth + " via token");
        }
        
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
                
                // Check fi the passthru session is valid
                    
                if ( authSess == null)
                {
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                    {
                        logger.debug( "Failed to open passthru session, or no valid passthru server available for " + ntlmToken);
                    }
                        
                    throw new AuthenticationException("authentication.err.connection.passthru.server");
                }
                
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
            
            throw new AuthenticationException("authentication.err.passthru.token.unsupported");
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
     * @throws AutheticationException
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
                    
                    throw new AuthenticationException("authentication.err.passthru.guest.notenabled");
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
            
            clearCurrentSecurityContext();
            setCurrentUser( username);
            
            // Debug
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Authenticated token=" + ntlmToken);
            }
        }
        catch (NoSuchAlgorithmException ex)
        {
            // JCE provider does not have the required encryption/hashing algorithms
            
            throw new AuthenticationException("JCE provider error", ex);
        }
        catch (InvalidKeyException ex)
        {
            // Problem creating key during encryption
            
            throw new AuthenticationException("Invalid key error", ex);
        }
        catch (IOException ex)
        {
            // Error connecting to the authentication server
            
            throw new AuthenticationException("I/O error", ex);
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
                    authEx = new AuthenticationException("authentication.err.passthru.user.disabled");
                    break;
                default:
                    authEx = new AuthenticationException("Logon failure");
                break;
                }
                
                throw authEx;
            }
            else
            {
                throw new AuthenticationException("Logon failure");
            }
        }
    }
    
    /**
     * Authenticate using passthru authentication with a client
     * 
     * @param ntlmToken NTLMPassthruToken
     * @throws AuthenticationExcepion
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
            {
                throw new AuthenticationException("Authentication session expired");
            }
            
            // Open an authentication session for the new token and add to the active session list
            
            authSess = m_passthruServers.openSession( false, ntlmToken.getClientDomain());
            
            // Check if the session was opened to the passthru server
            
            if ( authSess == null)
            {
                throw new AuthenticationException("authentication.err.connection.passthru.server");
            }
            
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
                        
                        throw new AuthenticationException("authentication.err.passthru.guest.notenabled");
                    }
                }
                
                // Indicate that the token is authenticated
                
                ntlmToken.setAuthenticated(true);

                // Wrap the service calls in a transaction
                
                RetryingTransactionHelper helper = getTransactionService().getRetryingTransactionHelper();
                
                final String currentUser = username;
                
                helper.doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws AuthenticationException
                    {
                        clearCurrentSecurityContext();
                        setCurrentUser(currentUser);
                        return null;
                    }
                });               
            }
            catch (NoSuchPersonException ex)
            {
                // Check if authenticated users are allowed on as guest when there is no Alfresco person record
                
                if ( m_allowAuthUserAsGuest == true)
                {
                    // Set the guest authority
                    
                    GrantedAuthority[] authorities = new GrantedAuthority[1];
                    authorities[0] = new GrantedAuthorityImpl(NTLMAuthorityGuest);
                    
                    ntlmToken.setAuthorities(authorities);

                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                    {
                        logger.debug("Allow passthru authenticated user to logon as guest, user=" + ntlmToken.getName());
                    }
                }
                else
                {
                    // Logon failure, no matching person record
                    throw new AuthenticationException("authentication.err.passthru.user.notfound", ex);
                }
            }
            catch (IOException ex)
            {
                // Error connecting to the authentication server
                throw new AuthenticationException("Unable to connect to the authentication server", ex);
            }
            catch (SMBException ex)
            {
                // Debug
                
                if ( logger.isDebugEnabled())
                {
                    logger.debug("Passthru exception, " + ex);
                }
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
                        authEx = new AuthenticationException("authentication.err.passthru.user.disabled");
                        break;
                    default:
                        authEx = new AuthenticationException("Logon failure");
                    break;
                    }
                    
                    throw authEx;
                }
                else
                {
                    throw new AuthenticationException("Logon failure");
                }
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
                        logger.debug("unable to close session", ex);
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
