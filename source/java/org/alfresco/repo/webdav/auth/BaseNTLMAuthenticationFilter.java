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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import net.sf.acegisecurity.BadCredentialsException;

import org.alfresco.filesys.ServerConfigurationBean;
import org.alfresco.jlan.server.auth.PasswordEncryptor;
import org.alfresco.jlan.server.auth.ntlm.NTLM;
import org.alfresco.jlan.server.auth.ntlm.NTLMLogonDetails;
import org.alfresco.jlan.server.auth.ntlm.NTLMv2Blob;
import org.alfresco.jlan.server.auth.ntlm.TargetInfo;
import org.alfresco.jlan.server.auth.ntlm.Type1NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type2NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type3NTLMMessage;
import org.alfresco.jlan.server.auth.passthru.DomainMapping;
import org.alfresco.jlan.server.config.SecurityConfigSection;
import org.alfresco.jlan.util.DataPacker;
import org.alfresco.jlan.util.IPAddress;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.MD4PasswordEncoder;
import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.ntlm.NTLMPassthruToken;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Base class with common code and initialisation for NTLM authentication filters.
 */
public abstract class BaseNTLMAuthenticationFilter implements Filter
{
    // NTLM authentication session object names
    public static final String NTLM_AUTH_SESSION = "_alfNTLMAuthSess";
    public static final String NTLM_AUTH_DETAILS = "_alfNTLMDetails";
    
    protected static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    protected static final String AUTHORIZATION = "Authorization";
    protected static final String AUTH_NTLM = "NTLM";
    
    // NTLM flags mask for use with an authentication component that supports MD4 hashed password
    private static final int NTLM_FLAGS_MD4 = NTLM.Flag56Bit +
                                              NTLM.Flag128Bit +
                                              NTLM.FlagLanManKey +
                                              NTLM.FlagNegotiateNTLM +
                                              NTLM.FlagNTLM2Key +
                                              NTLM.FlagNegotiateUnicode;

    // NTLM flags mask for use with an authentication component that uses passthru auth
    private static final int NTLM_FLAGS_PASSTHRU = NTLM.Flag56Bit +
                                                   NTLM.FlagLanManKey +
                                                   NTLM.FlagNegotiateNTLM +
                                                   NTLM.FlagNegotiateOEM +
                                                   NTLM.FlagNegotiateUnicode;
    
    // NTLM flags to send to the client with the allowed logon types
    private int m_ntlmFlags;
    
    // Servlet context, required to get authentication service
    protected ServletContext m_context;
    
    // File server configuration
    private ServerConfigurationBean m_srvConfig;
    
    // Security configuration section, for domain mappings
    private SecurityConfigSection m_secConfig;
    
    // Various services required by NTLM authenticator
    protected AuthenticationService m_authService;
    protected AuthenticationComponent m_authComponent;
    protected PersonService m_personService;
    protected NodeService m_nodeService;
    protected TransactionService m_transactionService;
    
    // Password encryptor
    private PasswordEncryptor m_encryptor = new PasswordEncryptor();
    
    // Random number generator used to generate challenge keys
    private Random m_random = new Random(System.currentTimeMillis());
    
    // MD4 hash decoder
    private MD4PasswordEncoder m_md4Encoder = new MD4PasswordEncoderImpl();
    
    // Local server name, from either the file servers config or DNS host name
    protected String m_srvName;
    
    // Allow guest access
    private boolean m_allowGuest = false;
    
    // Allow guest access, map unknown users to the guest account
    private boolean m_mapUnknownUserToGuest = false;
    
    
    /**
     * Initialize the filter
     * 
     * @param args FilterConfig
     * 
     * @exception ServletException
     */
    public void init(FilterConfig args) throws ServletException
    {
        // Save the servlet context, needed to get hold of the authentication service
        m_context = args.getServletContext();
        
        // Setup the authentication context
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(m_context);
        
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        m_nodeService = serviceRegistry.getNodeService();
        m_transactionService = serviceRegistry.getTransactionService();
        m_authService = serviceRegistry.getAuthenticationService();
        
        m_authComponent = (AuthenticationComponent) ctx.getBean("AuthenticationComponent");
        m_personService = (PersonService) ctx.getBean("personService");
        
        m_srvConfig = (ServerConfigurationBean) ctx.getBean(ServerConfigurationBean.SERVER_CONFIGURATION);
        
        // Check that the authentication component supports the required mode
        if (m_authComponent.getNTLMMode() != NTLMMode.MD4_PROVIDER &&
            m_authComponent.getNTLMMode() != NTLMMode.PASS_THROUGH)
        {
            throw new ServletException("Required authentication mode not available");
        }
        
        // Get the local server name, try the file server config first
        if (m_srvConfig != null)
        {
            m_srvName = m_srvConfig.getServerName();
            
            if (m_srvName != null)
            {
                try
                {
                    InetAddress resolved = InetAddress.getByName(m_srvName);
                    if (resolved == null)
                    {
                        // failed to resolve the configured name
                        m_srvName = m_srvConfig.getLocalServerName(true);
                    }
                }
                catch (UnknownHostException ex)
                {
                    if (getLogger().isErrorEnabled())
                        getLogger().error("NTLM filter, error getting resolving host name", ex);
                }
            }
            else
            {
                m_srvName = m_srvConfig.getLocalServerName(true);
            }
        }
        else
        {
            // Get the host name
            try
            {
                // Get the local host name
                m_srvName = InetAddress.getLocalHost().getHostName();
                
                // Strip any domain name
                int pos = m_srvName.indexOf(".");
                if (pos != -1)
                {
                    m_srvName = m_srvName.substring(0, pos - 1);
                }
            }
            catch (UnknownHostException ex)
            {
                if (getLogger().isErrorEnabled())
                    getLogger().error("NTLM filter, error getting local host name", ex);
            }
        }
        
        // Check if the server name is valid
        if (m_srvName == null || m_srvName.length() == 0)
        {
            throw new ServletException("Failed to get local server name");
        }
        
        // Check if guest access is to be allowed
        String guestAccess = args.getInitParameter("AllowGuest");
        if (guestAccess != null)
        {
            m_allowGuest = Boolean.parseBoolean(guestAccess);
            
            if (getLogger().isDebugEnabled() && m_allowGuest)
                getLogger().debug("NTLM filter guest access allowed");
        }
        
        // Check if unknown users should be mapped to guest access
        String mapUnknownToGuest = args.getInitParameter("MapUnknownUserToGuest");
        if (mapUnknownToGuest != null)
        {
            m_mapUnknownUserToGuest = Boolean.parseBoolean(mapUnknownToGuest);
            
            if (getLogger().isDebugEnabled() && m_mapUnknownUserToGuest)
                getLogger().debug("NTLM filter map unknown users to guest");
        }
        
        // Set the NTLM flags depending on the authentication component supporting MD4 passwords,
        // or is using passthru auth
        if (m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            // Allow the client to use an NTLMv2 logon
            m_ntlmFlags = NTLM_FLAGS_MD4;
        }
        else
        {
            // Only allows NTLMv1 type logons as passthru authentication is being used
            m_ntlmFlags = NTLM_FLAGS_PASSTHRU;
        }
    }
    
    /**
     * Delete the servlet filter
     */
    public void destroy()
    {
    }
    
    /**
     * Process a type 1 NTLM message
     * 
     * @param type1Msg Type1NTLMMessage
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @param session HttpSession
     * @exception IOException
     */
    protected void processType1(Type1NTLMMessage type1Msg, HttpServletRequest req,
            HttpServletResponse res, HttpSession session) throws IOException
    {
        Log logger = getLogger();
        
        if (logger.isDebugEnabled())
            logger.debug("Received type1 " + type1Msg);
        
        // Get the existing NTLM details
        NTLMLogonDetails ntlmDetails = null;
        
        if (session != null)
        {
            ntlmDetails = (NTLMLogonDetails)session.getAttribute(NTLM_AUTH_DETAILS);
        }
        
        // Check if cached logon details are available
        if (ntlmDetails != null && ntlmDetails.hasType2Message() &&
            ntlmDetails.hasNTLMHashedPassword() && ntlmDetails.hasAuthenticationToken())
        {
            // Get the authentication server type2 response
            Type2NTLMMessage cachedType2 = ntlmDetails.getType2Message();
            
            byte[] type2Bytes = cachedType2.getBytes();
            String ntlmBlob = "NTLM " + new String(Base64.encodeBase64(type2Bytes));
            
            if (logger.isDebugEnabled())
                logger.debug("Sending cached NTLM type2 to client - " + cachedType2);
            
            // Send back a request for NTLM authentication
            res.setHeader(WWW_AUTHENTICATE, ntlmBlob);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.flushBuffer();
        }
        else
        {
            // Clear any cached logon details
            session.removeAttribute(NTLM_AUTH_DETAILS);
            
            // Set the 8 byte challenge for the new logon request
            byte[] challenge = null;
            NTLMPassthruToken authToken = null;
            
            if (m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
            {
                // Generate a random 8 byte challenge
                challenge = new byte[8];
                DataPacker.putIntelLong(m_random.nextLong(), challenge, 0);
            }
            else
            {
                // Get the client domain
                String domain = type1Msg.getDomain();
                if (domain == null || domain.length() == 0)
                {
                    domain = mapClientAddressToDomain(req.getRemoteAddr());
                }
                
                if (logger.isDebugEnabled())
                    logger.debug("Client domain " + domain);
                
                // Create an authentication token for the new logon
                authToken = new NTLMPassthruToken(domain);
                
                // Run the first stage of the passthru authentication to get the challenge
                m_authComponent.authenticate(authToken);
                
                // Get the challenge from the token
                if (authToken.getChallenge() != null)
                {
                    challenge = authToken.getChallenge().getBytes();
                }
            }
            
            // Get the flags from the client request and mask out unsupported features
            int ntlmFlags = type1Msg.getFlags() & m_ntlmFlags;
            
            // Build a type2 message to send back to the client, containing the challenge
            List<TargetInfo> tList = new ArrayList<TargetInfo>();
            tList.add(new TargetInfo(NTLM.TargetServer, m_srvName));
            
            Type2NTLMMessage type2Msg = new Type2NTLMMessage();
            type2Msg.buildType2(ntlmFlags, m_srvName, challenge, null, tList);
            
            // Store the NTLM logon details, cache the type2 message, and token if using passthru
            ntlmDetails = new NTLMLogonDetails();
            ntlmDetails.setType2Message(type2Msg);
            ntlmDetails.setAuthenticationToken(authToken);
            
            session.setAttribute(NTLM_AUTH_DETAILS, ntlmDetails);
            
            if (logger.isDebugEnabled())
                logger.debug("Sending NTLM type2 to client - " + type2Msg);
            
            // Send back a request for NTLM authentication
            byte[] type2Bytes = type2Msg.getBytes();
            String ntlmBlob = "NTLM " + new String(Base64.encodeBase64(type2Bytes));

            res.setHeader(WWW_AUTHENTICATE, ntlmBlob);
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.flushBuffer();
        }
    }
    
    /**
     * Process a type 3 NTLM message
     * 
     * @param type3Msg Type3NTLMMessage
     * @param req HttpServletRequest
     * @param res HttpServletResponse
     * @param session HttpSession
     * @param chain FilterChain
     * 
     * @exception IOException
     * @exception ServletException
     */
    protected void processType3(Type3NTLMMessage type3Msg, HttpServletRequest req, HttpServletResponse res,
            HttpSession session, FilterChain chain) throws IOException, ServletException
    {
        Log logger = getLogger();
        
        if (logger.isDebugEnabled())
            logger.debug("Received type3 " + type3Msg);
        
        // Get the existing NTLM details
        NTLMLogonDetails ntlmDetails = null;
        SessionUser user = null;
        
        if (session != null)
        {
            ntlmDetails = (NTLMLogonDetails)session.getAttribute(NTLM_AUTH_DETAILS);
            user = getSessionUser(session);
        }
        
        // Get the NTLM logon details
        String userName = type3Msg.getUserName();
        String workstation = type3Msg.getWorkstation();
        String domain = type3Msg.getDomain();
        
        boolean authenticated = false;
        
        // Check if we are using cached details for the authentication
        if (user != null && ntlmDetails != null && ntlmDetails.hasNTLMHashedPassword())
        {
            // Check if the received NTLM hashed password matches the cached password
            byte[] ntlmPwd = type3Msg.getNTLMHash();
            byte[] cachedPwd = ntlmDetails.getNTLMHashedPassword();
            
            if (ntlmPwd != null)
            {
                if (ntlmPwd.length == cachedPwd.length)
                {
                    authenticated = true;
                    for (int i = 0; i < ntlmPwd.length; i++)
                    {
                        if (ntlmPwd[i] != cachedPwd[i])
                            authenticated = false;
                    }
                }
            }
            
            if (logger.isDebugEnabled())
                logger.debug("Using cached NTLM hash, authenticated = " + authenticated);
            
            try
            {
                if (logger.isDebugEnabled())
                    logger.debug("User " + user.getUserName() + " validate ticket");
                
                // Validate the user ticket
                m_authService.validate(user.getTicket());
                
                onValidate(req, session);
            }
            catch (AuthenticationException ex)
            {
                if (logger.isErrorEnabled())
                    logger.error("Failed to validate user " + user.getUserName(), ex);
                
                onValidateFailed(req, res, session);
                return;
            }
            
            // Allow the user to access the requested page
            chain.doFilter(req, res);
            return;
        }
        else
        {
            // Check if we are using local MD4 password hashes or passthru authentication
            if (m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
            {
                // Check if guest logons are allowed and this is a guest logon
                if (m_allowGuest && userName.equalsIgnoreCase(m_authComponent.getGuestUserName()))
                {
                    // Indicate that the user has been authenticated
                    authenticated = true;
                    
                    if (logger.isDebugEnabled())
                        logger.debug("Guest logon");
                }
                else
                {
                    // Get the stored MD4 hashed password for the user, or null if the user does not exist
                    String md4hash = getMD4Hash(userName);
                    
                    if (md4hash != null)
                    {
                        authenticated = validateLocalHashedPassword(type3Msg, ntlmDetails, authenticated, md4hash);
                    }
                    else
                    {
                        // Check if unknown users should be logged on as guest
                        if (m_mapUnknownUserToGuest)
                        {
                            // Reset the user name to be the guest user
                            userName = m_authComponent.getGuestUserName();
                            authenticated = true;
                            
                            if (logger.isDebugEnabled())
                                logger.debug("User " + userName + " logged on as guest, no Alfresco account");
                        }
                        else
                        {
                            if (logger.isDebugEnabled())
                                logger.debug("User " + userName + " does not have Alfresco account");
                            
                            // Bypass NTLM authentication and display the logon screen,
                            // as user account does not exist in Alfresco
                            authenticated = false;
                        }
                    }
                }
            }
            else
            {
                //  Determine if the client sent us NTLMv1 or NTLMv2
                if (type3Msg.hasFlag(NTLM.Flag128Bit) && type3Msg.hasFlag(NTLM.FlagNTLM2Key) ||
                    (type3Msg.getNTLMHash() != null && type3Msg.getNTLMHash().length > 24))
                {
                    // Cannot accept NTLMv2 if we are using passthru auth
                    if (logger.isErrorEnabled())
                        logger.error("Client " + workstation + " using NTLMv2 logon, not valid with passthru authentication");
                }
                else
                {
                    // Passthru mode, send the hashed password details to the passthru authentication server
                    NTLMPassthruToken authToken = (NTLMPassthruToken) ntlmDetails.getAuthenticationToken();
                    authToken.setUserAndPassword( type3Msg.getUserName(), type3Msg.getNTLMHash(), PasswordEncryptor.NTLM1);
                    
                    try
                    {
                        // Run the second stage of the passthru authentication
                        m_authComponent.authenticate(authToken);
                        authenticated = true;
                        
                        // Check if the user has been logged on as guest
                        if (authToken.isGuestLogon())
                        {
                            userName = m_authComponent.getGuestUserName();
                        }
                        
                        // Set the authentication context
                        m_authComponent.setCurrentUser(userName);
                    }
                    catch (BadCredentialsException ex)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Authentication failed, " + ex.getMessage());
                    }
                    catch (AuthenticationException ex)
                    {
                        if (logger.isDebugEnabled())
                            logger.debug("Authentication failed, " + ex.getMessage());
                    }
                    finally
                    {
                        // Clear the authentication token from the NTLM details
                        ntlmDetails.setAuthenticationToken(null);
                    }
                }
            }
            
            // Check if the user has been authenticated, if so then setup the user environment
            if (authenticated == true)
            {
                if (user == null)
                {
                    user = createUserEnvironment(session, userName);
                }
                else
                {
                    // user already exists - revalidate ticket to authenticate the current user thread
                    try
                    {
                        m_authService.validate(user.getTicket());
                    }
                    catch (AuthenticationException ex)
                    {
                        if (logger.isErrorEnabled())
                            logger.error("Failed to validate user " + user.getUserName(), ex);
                        
                        onValidateFailed(req, res, session);
                        return;
                    }
                }
                
                onValidate(req, session);
                
                // Update the NTLM logon details in the session
                if (ntlmDetails == null)
                {
                    // No cached NTLM details
                    ntlmDetails = new NTLMLogonDetails( userName, workstation, domain, false, m_srvName);
                    ntlmDetails.setNTLMHashedPassword(type3Msg.getNTLMHash());
                    session.setAttribute(NTLM_AUTH_DETAILS, ntlmDetails);
                    
                    if (logger.isDebugEnabled())
                        logger.debug("No cached NTLM details, created");
                }
                else
                {
                    // Update the cached NTLM details
                    ntlmDetails.setDetails(userName, workstation, domain, false, m_srvName);
                    ntlmDetails.setNTLMHashedPassword(type3Msg.getNTLMHash());

                    if (logger.isDebugEnabled())
                        logger.debug("Updated cached NTLM details");
                }
                
                if (logger.isDebugEnabled())
                    logger.debug("User logged on via NTLM, " + ntlmDetails);
                
                if (onLoginComplete(req, res))
                {
                    // Allow the user to access the requested page
                    chain.doFilter(req, res);
                }
            }
            else
            {
                restartLoginChallenge(res, session);
            }
        }
    }
    
    /**
     * Callback to get the specific impl of the Session User for a filter
     * 
     * @return User from the session
     */
    protected abstract SessionUser getSessionUser(HttpSession session);
    
    /**
     * Callback to create the User environment as appropriate for a filter impl
     * 
     * @param session
     * @param userName
     * 
     * @return SessionUser impl
     * 
     * @throws IOException
     * @throws ServletException
     */
    protected abstract SessionUser createUserEnvironment(HttpSession session, String userName)
        throws IOException, ServletException;
    
    /**
     * Callback executed on successful ticket validation during Type3 Message processing
     */
    protected abstract void onValidate(HttpServletRequest req, HttpSession session);
    
    /**
     * Callback executed on failed authentication of a user ticket during Type3 Message processing 
     */
    protected abstract void onValidateFailed(HttpServletRequest req, HttpServletResponse res, HttpSession session)
        throws IOException;
    
    /**
     * Callback executed on completion of NTLM login
     * 
     * @return true to continue filter chaining, false otherwise
     */
    protected abstract boolean onLoginComplete(HttpServletRequest req, HttpServletResponse res)
        throws IOException;
    
    /**
     * Validate the MD4 hash against local password
     * 
     * @param type3Msg
     * @param ntlmDetails
     * @param authenticated
     * @param md4hash
     * 
     * @return true if password hash is valid, false otherwise
     */
    protected boolean validateLocalHashedPassword(Type3NTLMMessage type3Msg, NTLMLogonDetails ntlmDetails, boolean authenticated, String md4hash)
    {
        //  Determine if the client sent us NTLMv1 or NTLMv2
        if (type3Msg.hasFlag(NTLM.FlagNTLM2Key))
        {
            //  Determine if the client sent us an NTLMv2 blob or an NTLMv2 session key
            if (type3Msg.getNTLMHashLength() > 24)
            {
                //  Looks like an NTLMv2 blob
                authenticated = checkNTLMv2(md4hash, ntlmDetails.getChallengeKey(), type3Msg);
                
                if (getLogger().isDebugEnabled())
                    getLogger().debug((authenticated ? "Logged on" : "Logon failed") + " using NTLMSSP/NTLMv2");
            }
            else
            {
                //  Looks like an NTLMv2 session key
                authenticated = checkNTLMv2SessionKey(md4hash, ntlmDetails.getChallengeKey(), type3Msg);
                
                if (getLogger().isDebugEnabled())
                    getLogger().debug((authenticated ? "Logged on" : "Logon failed") + " using NTLMSSP/NTLMv2SessKey");
            }
        }
        else
        {
            //  Looks like an NTLMv1 blob
            authenticated = checkNTLMv1(md4hash, ntlmDetails.getChallengeKey(), type3Msg);
            
            if (getLogger().isDebugEnabled())
                getLogger().debug((authenticated ? "Logged on" : "Logon failed") + " using NTLMSSP/NTLMv1");
        }
        return authenticated;
    }
    
    /**
     * Perform an NTLMv1 hashed password check
     * 
     * @param String md4hash
     * @param byte[] challenge
     * @param Type3NTLMMessage type3Msg
     * @return boolean
     */
    protected final boolean checkNTLMv1(String md4hash, byte[] challenge, Type3NTLMMessage type3Msg)
    {
        // Generate the local encrypted password using the challenge that was sent to the client
        byte[] p21 = new byte[21];
        byte[] md4byts = m_md4Encoder.decodeHash(md4hash);
        System.arraycopy(md4byts, 0, p21, 0, 16);

        // Generate the local hash of the password using the same challenge
        byte[] localHash = null;

        try
        {
            localHash = m_encryptor.doNTLM1Encryption(p21, challenge);
        }
        catch (NoSuchAlgorithmException ex)
        {
        }

        // Validate the password
        byte[] clientHash = type3Msg.getNTLMHash();

        if (clientHash != null && localHash != null && clientHash.length == localHash.length)
        {
            int i = 0;

            while (i < clientHash.length && clientHash[i] == localHash[i])
            {
                i++;
            }

            if (i == clientHash.length)
            {
                // Hashed passwords match
                return true;
            }
        }

        // Hashed passwords do not match
        return false;
    }

    /**
     * Perform an NTLMv2 check
     * 
     * @param String md4hash
     * @param byte[] challenge
     * @param Type3NTLMMessage type3Msg
     * @return boolean
     */
    protected final boolean checkNTLMv2(String md4hash, byte[] challenge, Type3NTLMMessage type3Msg)
    {
        try
        {
            // Generate the v2 hash using the challenge that was sent to the client
            byte[] v2hash = m_encryptor.doNTLM2Encryption(m_md4Encoder.decodeHash(md4hash), type3Msg.getUserName(), type3Msg.getDomain());

            // Get the NTLMv2 blob sent by the client and the challenge that was sent by the server
            NTLMv2Blob v2blob = new NTLMv2Blob(type3Msg.getNTLMHash());

            // Calculate the HMAC of the received blob and compare
            byte[] srvHmac = v2blob.calculateHMAC(challenge, v2hash);
            byte[] clientHmac = v2blob.getHMAC();

            if (clientHmac != null && srvHmac != null && clientHmac.length == srvHmac.length)
            {
                int i = 0;

                while (i < clientHmac.length && clientHmac[i] == srvHmac[i])
                {
                    i++;
                }

                if (i == clientHmac.length)
                {
                    //  HMAC matches the client, user authenticated
                    return true;
                }
            }
        }
        catch (Exception ex)
        {
            if (getLogger().isDebugEnabled())
                getLogger().debug(ex);
        }

        // NTLMv2 check failed
        return false;
    }

    /**
     * Perform an NTLMv2 session key check
     * 
     * @param String md4hash
     * @param byte[] challenge
     * @param Type3NTLMMessage type3Msg
     * @return boolean
     */
    protected final boolean checkNTLMv2SessionKey(String md4hash, byte[] challenge, Type3NTLMMessage type3Msg)
    {
        // Create the value to be encrypted by appending the server challenge and client challenge
        // and applying an MD5 digest
        byte[] nonce = new byte[16];
        System.arraycopy(challenge, 0, nonce, 0, 8);
        System.arraycopy(type3Msg.getLMHash(), 0, nonce, 8, 8);

        MessageDigest md5 = null;
        byte[] v2challenge = new byte[8];

        try
        {
            //  Create the MD5 digest
            md5 = MessageDigest.getInstance("MD5");

            //  Apply the MD5 digest to the nonce
            md5.update(nonce);
            byte[] md5nonce = md5.digest();

            //  We only want the first 8 bytes
            System.arraycopy(md5nonce, 0, v2challenge, 0, 8);
        }
        catch (NoSuchAlgorithmException ex)
        {
            // Log the error
            getLogger().error(ex);
        }

        // Generate the local encrypted password using the MD5 generated challenge
        byte[] p21 = new byte[21];
        byte[] md4byts = m_md4Encoder.decodeHash(md4hash);
        System.arraycopy(md4byts, 0, p21, 0, 16);

        // Generate the local hash of the password
        byte[] localHash = null;

        try
        {
            localHash = m_encryptor.doNTLM1Encryption(p21, v2challenge);
        }
        catch (NoSuchAlgorithmException ex)
        {
            // Log the error
            getLogger().error(ex);
        }

        // Validate the password
        byte[] clientHash = type3Msg.getNTLMHash();

        if (clientHash != null && localHash != null && clientHash.length == localHash.length)
        {
            int i = 0;

            while (i < clientHash.length && clientHash[i] == localHash[i])
            {
                i++;
            }

            if (i == clientHash.length)
            {
                //  Hashed password check successful
                return true;
            }
        }

        // Password check failed
        return false;
    }
    
    /**
     * Get the stored MD4 hashed password for the user, or null if the user does not exist
     * 
     * @param userName
     * @param md4hash
     * 
     * @return MD4 hash or null
     */
    protected String getMD4Hash(String userName)
    {
        String md4hash = null;
        
        // Wrap the auth component calls in a transaction
        UserTransaction tx = m_transactionService.getUserTransaction();
        try
        {
            tx.begin();
            
            // Get the stored MD4 hashed password for the user, or null if the user does not exist
            md4hash = m_authComponent.getMD4HashedPassword(userName);
        }
        catch (Throwable ex)
        {
            if (getLogger().isDebugEnabled())
                getLogger().debug(ex);
        }
        finally
        {
            // Rollback/commit the transaction if still valid
            if (tx != null)
            {
                try
                {
                    // Commit or rollback the transaction
                    if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK ||
                        tx.getStatus() == Status.STATUS_ROLLEDBACK ||
                        tx.getStatus() == Status.STATUS_ROLLING_BACK)
                    {
                        // Transaction is marked for rollback
                        tx.rollback();
                    }
                    else
                    {
                        // Commit the transaction
                        tx.commit();
                    }
                }
                catch (Throwable ex)
                {
                    if (getLogger().isDebugEnabled())
                        getLogger().debug(ex);
                }
            }
        }
        
        return md4hash;
    }
    
    /**
     * @param resp
     * @param httpSess
     * @throws IOException
     */
    protected void restartLoginChallenge(HttpServletResponse res, HttpSession session) throws IOException
    {
        // Remove any existing session and NTLM details from the session
        session.removeAttribute(NTLM_AUTH_SESSION);
        session.removeAttribute(NTLM_AUTH_DETAILS);
        
        // Force the logon to start again
        res.setHeader(WWW_AUTHENTICATE, AUTH_NTLM);
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.flushBuffer();
    }
    
    /**
     * Map a client IP address to a domain
     * 
     * @param clientIP String
     * @return String
     */
    protected final String mapClientAddressToDomain(String clientIP)
    {
        // Check if there are any domain mappings
        if (m_secConfig != null && m_secConfig.hasDomainMappings() == false)
        {
            return null;
        }
        
        // convert the client IP address to an integer value
        int clientAddr = IPAddress.parseNumericAddress(clientIP);
        for (DomainMapping domainMap : m_secConfig.getDomainMappings())
        {
            if (domainMap.isMemberOfDomain(clientAddr))
            {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Mapped client IP " + clientIP + " to domain " + domainMap.getDomain());
            
                return domainMap.getDomain();
            }
        }
        
        if (getLogger().isDebugEnabled())
            getLogger().debug("Failed to map client IP " + clientIP + " to a domain");
        
        // No domain mapping for the client address
        return null;
    }
    
    protected abstract Log getLogger();
}
