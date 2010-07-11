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
package org.alfresco.web.sharepoint.auth.ntlm;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import net.sf.acegisecurity.BadCredentialsException;

import org.alfresco.jlan.server.auth.PasswordEncryptor;
import org.alfresco.jlan.server.auth.ntlm.NTLM;
import org.alfresco.jlan.server.auth.ntlm.NTLMLogonDetails;
import org.alfresco.jlan.server.auth.ntlm.NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.NTLMv2Blob;
import org.alfresco.jlan.server.auth.ntlm.TargetInfo;
import org.alfresco.jlan.server.auth.ntlm.Type1NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type2NTLMMessage;
import org.alfresco.jlan.server.auth.ntlm.Type3NTLMMessage;
import org.alfresco.jlan.util.DataPacker;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MD4PasswordEncoder;
import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;
import org.alfresco.repo.security.authentication.ntlm.NTLMPassthruToken;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.sharepoint.auth.AbstractAuthenticationHandler;
import org.alfresco.web.sharepoint.auth.SiteMemberMapper;
import org.alfresco.web.sharepoint.auth.SiteMemberMappingException;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>
 * NTLM SSO web authentication implementation.
 * </p>
 */
public class NtlmAuthenticationHandler extends AbstractAuthenticationHandler implements InitializingBean
{
    // NTLM authentication session object names
    private static final String NTLM_AUTH_DETAILS = "_alfNTLMDetails";

    private MD4PasswordEncoder md4Encoder = new MD4PasswordEncoderImpl();
    private PasswordEncryptor encryptor = new PasswordEncryptor();
    private Random random = new Random(System.currentTimeMillis());

    private NLTMAuthenticator authenticationComponent;
    private TransactionService transactionService;
    private NodeService nodeService;
   
    // NTLM flags mask for use with an authentication component that supports MD4 hashed password
    // Enable NTLMv1 and NTLMv2
    private static final int NTLM_FLAGS_NTLM2 = NTLM.Flag56Bit +
                                         NTLM.Flag128Bit +
                                         NTLM.FlagLanManKey +
                                         NTLM.FlagNegotiateNTLM +
                                         NTLM.FlagNTLM2Key +
                                         NTLM.FlagNegotiateUnicode;
    
    // NTLM flags mask for use with an authentication component that uses passthru auth
    // Enable NTLMv1 only
    private static final int NTLM_FLAGS_NTLM1 = NTLM.Flag56Bit +
                                                NTLM.FlagLanManKey +
                                                NTLM.FlagNegotiateNTLM +
                                                NTLM.FlagNegotiateOEM +
                                                NTLM.FlagNegotiateUnicode;
   
    private int ntlmFlags;
    
    public void setAuthenticationComponent(NLTMAuthenticator authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void afterPropertiesSet() throws Exception
    {
        if (authenticationComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            ntlmFlags = NTLM_FLAGS_NTLM2;
        }        
        else
        {
            ntlmFlags = NTLM_FLAGS_NTLM1;
        }
    }

    public SessionUser authenticateRequest(HttpServletRequest request, HttpServletResponse response,
            SiteMemberMapper mapper, String alfrescoContext)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Start NTLM authentication for request: " + request.getRequestURI());
        }

        HttpSession session = request.getSession();
        SessionUser user = (SessionUser) session.getAttribute(USER_SESSION_ATTRIBUTE);

        String authHdr = request.getHeader(HEADER_AUTHORIZATION);

        boolean needToAuthenticate = false;

        if (authHdr != null && authHdr.startsWith(NTLM_START))
        {
            needToAuthenticate = true;
        }

        if (user != null && needToAuthenticate == false)
        {
            try
            {
                authenticationService.validate(user.getTicket());
                needToAuthenticate = false;
            }
            catch (AuthenticationException e)
            {
                session.removeAttribute(USER_SESSION_ATTRIBUTE);
                needToAuthenticate = true;
            }
        }

        if (needToAuthenticate == false && user != null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("NTLM header wasn't present. Authenticated by user from session. Username: "
                        + user.getUserName());
            }
            return user;
        }

        if (authHdr == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("NTLM header wasn't present. No user was found in session. Return 401 status.");
            }
            removeNtlmLogonDetailsFromSession(request);
            forceClientToPromptLogonDetails(response);
            return null;
        }
        else
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("NTLM header present in request.");
            }
            // Decode the received NTLM blob and validate
            final byte[] ntlmByts = Base64.decodeBase64(authHdr.substring(5).getBytes());
            int ntlmTyp = NTLMMessage.isNTLMType(ntlmByts);
            if (ntlmTyp == NTLM.Type1)
            {
                Type1NTLMMessage type1Msg = new Type1NTLMMessage(ntlmByts);
                try
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Start process type 1 message.");
                    }
                    processType1(type1Msg, request, response, session);
                    user = null;
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Finish process type 1 message.");
                    }
                }
                catch (Exception e)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Process type 1 message fail with error: " + e.getMessage());
                    }
                    session.removeAttribute(USER_SESSION_ATTRIBUTE);
                    removeNtlmLogonDetailsFromSession(request);
                    return null;
                }

            }
            else if (ntlmTyp == NTLM.Type3)
            {
                Type3NTLMMessage type3Msg = new Type3NTLMMessage(ntlmByts);

                try
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Start process message type 3.");
                    }
                    user = processType3(type3Msg, mapper, request, response, session, alfrescoContext);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Finish process message type 3.");
                    }
                }
                catch (SiteMemberMappingException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    if (user != null)
                    {
                        try
                        {
                            authenticationService.validate(user.getTicket());
                            return user;
                        }
                        catch (AuthenticationException ae)
                        {
                        }
                    }
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Process message type 3 fail with message: " + e.getMessage());
                    }
                    session.removeAttribute(USER_SESSION_ATTRIBUTE);
                    removeNtlmLogonDetailsFromSession(request);
                    return null;
                }
            }

            return user;
        }
    }       

    @Override
    public String getWWWAuthenticate()
    {
        return NTLM_START;
    }

    private void processType1(Type1NTLMMessage type1Msg, HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws IOException
    {
        removeNtlmLogonDetailsFromSession(request);

        NTLMLogonDetails ntlmDetails = new NTLMLogonDetails();

        // Set the 8 byte challenge for the new logon request
        byte[] challenge = null;

        // Generate a random 8 byte challenge
        NTLMPassthruToken authToken = null;
        
        if (authenticationComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            challenge = new byte[8];
            DataPacker.putIntelLong(random.nextLong(), challenge, 0);
        }
        else
        {
            // Get the client domain
            String domain = type1Msg.getDomain();
            
            // Create an authentication token for the new logon
            authToken = new NTLMPassthruToken(domain);
            
            // Run the first stage of the passthru authentication to get the challenge
            authenticationComponent.authenticate(authToken);
            
            // Get the challenge from the token
            if (authToken.getChallenge() != null)
            {
                challenge = authToken.getChallenge().getBytes();
            }
        }

        // Get the flags from the client request and mask out unsupported features
        int flags = type1Msg.getFlags() & ntlmFlags;

        // Build a type2 message to send back to the client, containing the challenge
        List<TargetInfo> tList = new ArrayList<TargetInfo>();
        String srvName = getServerName();
        tList.add(new TargetInfo(NTLM.TargetServer, srvName));

        Type2NTLMMessage type2Msg = new Type2NTLMMessage();
        type2Msg.buildType2(flags, srvName, challenge, null, tList);

        // Store the NTLM logon details, cache the type2 message, and token if using passthru
        ntlmDetails.setType2Message(type2Msg);
        ntlmDetails.setAuthenticationToken(authToken);

        putNtlmLogonDetailsToSession(request, ntlmDetails);

        // Send back a request for NTLM authentication
        byte[] type2Bytes = type2Msg.getBytes();
        String ntlmBlob = "NTLM " + new String(Base64.encodeBase64(type2Bytes));

        response.setHeader(HEADER_WWW_AUTHENTICATE, ntlmBlob);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.flushBuffer();
        response.getOutputStream().close();

    }

    private SessionUser processType3(Type3NTLMMessage type3Msg, SiteMemberMapper callback, HttpServletRequest request,
            HttpServletResponse response, HttpSession session, String alfrescoContext) throws IOException,
            ServletException
    {

        // Get the existing NTLM details
        NTLMLogonDetails ntlmDetails = null;
        SessionUser user = null;

        if (session != null)
        {
            ntlmDetails = getNtlmLogonDetailsFromSession(request);
            user = (SessionUser) session.getAttribute(USER_SESSION_ATTRIBUTE);
        }

        // Get the NTLM logon details
        String userName = type3Msg.getUserName();
        String workstation = type3Msg.getWorkstation();
        String domain = type3Msg.getDomain();

        boolean authenticated = false;

        if (authenticationComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            // Get the stored MD4 hashed password for the user, or null if the user does not exist
            String md4hash = getMD4Hash(userName);

            if (md4hash != null)
            {
                authenticated = validateLocalHashedPassword(type3Msg, ntlmDetails, authenticated, md4hash);
            }
            else
            {
                authenticated = false;
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
                authToken.setUserAndPassword(type3Msg.getUserName(), type3Msg.getNTLMHash(), PasswordEncryptor.NTLM1);
                
                try
                {
                    // Run the second stage of the passthru authentication
                    authenticationComponent.authenticate(authToken);
                    authenticated = true;                
                    
                    // Set the authentication context
                    authenticationComponent.setCurrentUser(userName);
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
        if (authenticated == true && callback.isSiteMember(request, alfrescoContext, userName))
        {
            String uri = request.getRequestURI();

            if (request.getMethod().equals("POST") && !uri.endsWith(".asmx"))
            {
                response.setHeader("Connection", "Close");
                response.setContentType("application/x-vermeer-rpc");
            }

            if (user == null)
            {
                user = createUserEnvironment(session, userName);
                session.setAttribute(USER_SESSION_ATTRIBUTE, user);
            }
            else
            {
                // user already exists - revalidate ticket to authenticate the current user thread
                try
                {
                    authenticationService.validate(user.getTicket());
                }
                catch (AuthenticationException ex)
                {
                    session.removeAttribute(USER_SESSION_ATTRIBUTE);
                    removeNtlmLogonDetailsFromSession(request);
                    return null;
                }
            }

            // Update the NTLM logon details in the session
            String srvName = getServerName();
            if (ntlmDetails == null)
            {
                // No cached NTLM details
                ntlmDetails = new NTLMLogonDetails(userName, workstation, domain, false, srvName);
                putNtlmLogonDetailsToSession(request, ntlmDetails);
            }
            else
            {
                // Update the cached NTLM details
                ntlmDetails.setDetails(userName, workstation, domain, false, srvName);
                putNtlmLogonDetailsToSession(request, ntlmDetails);
            }
        }
        else
        {
            removeNtlmLogonDetailsFromSession(request);
            session.removeAttribute(USER_SESSION_ATTRIBUTE);
            return null;
        }
        return user;
    }

    /*
     * returns server name
     */
    private String getServerName()
    {
        return "Alfresco Server";
    }

    /*
     * Create the SessionUser object that represent currently authenticated user.
     */
    private SessionUser createUserEnvironment(HttpSession session, final String userName) throws IOException,
            ServletException
    {
        SessionUser user = null;

        UserTransaction tx = transactionService.getUserTransaction();

        try
        {
            tx.begin();

            RunAsWork<NodeRef> getUserNodeRefRunAsWork = new RunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {

                    return personService.getPerson(userName);
                }
            };

            NodeRef personNodeRef = AuthenticationUtil.runAs(getUserNodeRefRunAsWork,
                    AuthenticationUtil.SYSTEM_USER_NAME);

            // Use the system user context to do the user lookup
            RunAsWork<String> getUserNameRunAsWork = new RunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                    final NodeRef personNodeRef = personService.getPerson(userName);
                    return (String) nodeService.getProperty(personNodeRef, ContentModel.PROP_USERNAME);
                }
            };
            String username = AuthenticationUtil.runAs(getUserNameRunAsWork, AuthenticationUtil.SYSTEM_USER_NAME);

            authenticationComponent.setCurrentUser(userName);
            String currentTicket = authenticationService.getCurrentTicket();

            // Create the user object to be stored in the session
            user = new User(username, currentTicket, personNodeRef);

            tx.commit();
        }
        catch (Throwable ex)
        {
            try
            {
                tx.rollback();
            }
            catch (Exception err)
            {
                logger.error("Failed to rollback transaction", err);
            }
            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException) ex;
            }
            else if (ex instanceof IOException)
            {
                throw (IOException) ex;
            }
            else if (ex instanceof ServletException)
            {
                throw (ServletException) ex;
            }
            else
            {
                throw new RuntimeException("Authentication setup failed", ex);
            }
        }

        // Store the user on the session
        session.setAttribute(USER_SESSION_ATTRIBUTE, user);

        return user;
    }

    /*
     * returns the hash of password
     */
    protected String getMD4Hash(String userName)
    {
        String md4hash = null;

        // Wrap the auth component calls in a transaction
        UserTransaction tx = transactionService.getUserTransaction();
        try
        {
            tx.begin();

            // Get the stored MD4 hashed password for the user, or null if the user does not exist
            md4hash = authenticationComponent.getMD4HashedPassword(userName);

            tx.commit();
        }
        catch (Throwable ex)
        {
            try
            {
                tx.rollback();
            }
            catch (Exception e)
            {
            }
        }

        return md4hash;
    }

    /*
     * Validate local hash for user password and hash that was sent by client
     */
    private boolean validateLocalHashedPassword(Type3NTLMMessage type3Msg, NTLMLogonDetails ntlmDetails,
            boolean authenticated, String md4hash)
    {
        if (ntlmDetails == null || ntlmDetails.getType2Message() == null)
        {
            return false;
        }

        if (type3Msg.hasFlag(NTLM.FlagNTLM2Key))
        {
            //  Determine if the client sent us an NTLMv2 blob or an NTLMv2 session key
            if (type3Msg.getNTLMHashLength() > 24)
            {
                //  Looks like an NTLMv2 blob
                authenticated = checkNTLMv2(md4hash, ntlmDetails.getChallengeKey(), type3Msg);                
                if (logger.isDebugEnabled())
                {
                    logger.debug((authenticated ? "Logged on" : "Logon failed") + " using NTLMSSP/NTLMv2");
                }
                
                if ( authenticated == false && type3Msg.hasFlag(NTLM.Flag56Bit) && type3Msg.getLMHashLength() == 24)
                {   
                    authenticated = checkNTLMv1(md4hash, ntlmDetails.getChallengeKey(), type3Msg, true);
                    if (logger.isDebugEnabled())
                    {
                        logger.debug((authenticated ? "Logged on" : "Logon failed") + " using NTLMSSP/NTLMv1 (via fallback)");
                    }
                }
            }
            else
            {                
                authenticated = checkNTLMv2SessionKey(md4hash, ntlmDetails.getChallengeKey(), type3Msg);                
                if (logger.isDebugEnabled())
                {
                    logger.debug((authenticated ? "Logged on" : "Logon failed") + " using NTLMSSP/NTLMv2SessKey");
                }
            }
        }
        else
        {            
            authenticated = checkNTLMv1(md4hash, ntlmDetails.getChallengeKey(), type3Msg, false);
            if (logger.isDebugEnabled())
            {
                logger.debug((authenticated ? "Logged on" : "Logon failed") + " using NTLMSSP/NTLMv1");
            }
        }

        return authenticated;
    }

    private final boolean checkNTLMv1(String md4hash, byte[] challenge, Type3NTLMMessage type3Msg, boolean checkLMHash)
    {
        // Generate the local encrypted password using the challenge that was sent to the client
        byte[] p21 = new byte[21];
        byte[] md4byts = md4Encoder.decodeHash(md4hash);
        System.arraycopy(md4byts, 0, p21, 0, 16);

        // Generate the local hash of the password using the same challenge
        byte[] localHash = null;

        try
        {
            localHash = encryptor.doNTLM1Encryption(p21, challenge);
        }
        catch (NoSuchAlgorithmException ex)
        {
        }

        // Validate the password
        byte[] clientHash = checkLMHash ? type3Msg.getLMHash() : type3Msg.getNTLMHash();

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

    private final boolean checkNTLMv2(String md4hash, byte[] challenge, Type3NTLMMessage type3Msg)
    {
        boolean ntlmv2OK = false;
        boolean lmv2OK   = false;
        
        try
        {
            byte[] v2hash = encryptor.doNTLM2Encryption(md4Encoder.decodeHash(md4hash), type3Msg.getUserName(), type3Msg.getDomain());

            NTLMv2Blob v2blob = new NTLMv2Blob(type3Msg.getNTLMHash());

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
                    ntlmv2OK = true;
                }
            }

            if ( ntlmv2OK == false)
            {
                byte[] lmv2 = type3Msg.getLMHash();
                byte[] clChallenge = v2blob.getClientChallenge();
                
                if ( lmv2 != null && lmv2.length == 24 && clChallenge != null && clChallenge.length == 8)
                {   
                    int i = 0;
                    
                    while ( i < clChallenge.length && lmv2[ i + 16] == clChallenge[ i])
                        i++;
                    
                    if ( i == clChallenge.length)
                    {                        
                        
                        byte[] lmv2Hmac = v2blob.calculateLMv2HMAC(v2hash, challenge, clChallenge);
                        i = 0;
    
                        while (i < lmv2Hmac.length && lmv2[i] == lmv2Hmac[i])
                            i++;
    
                        if (i == lmv2Hmac.length)
                        {
                            lmv2OK = true;
                        }
                        
                    }
                }
            }
        }
        catch (Exception ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(ex);
            }
        }
        if ( ntlmv2OK || lmv2OK)
            return true;
        return false;
    }
    
    private final boolean checkNTLMv2SessionKey(String md4hash, byte[] challenge, Type3NTLMMessage type3Msg)
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
            md5 = MessageDigest.getInstance("MD5");
            //  Apply the MD5 digest to the nonce
            md5.update(nonce);
            byte[] md5nonce = md5.digest();

            //  We only want the first 8 bytes
            System.arraycopy(md5nonce, 0, v2challenge, 0, 8);
        }
        catch (NoSuchAlgorithmException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(ex.getMessage());                
            }
        }

        // Generate the local encrypted password using the MD5 generated challenge
        byte[] p21 = new byte[21];
        byte[] md4byts = md4Encoder.decodeHash(md4hash);
        System.arraycopy(md4byts, 0, p21, 0, 16);

        // Generate the local hash of the password
        byte[] localHash = null;

        try
        {
            localHash = encryptor.doNTLM1Encryption(p21, v2challenge);
        }
        catch (NoSuchAlgorithmException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(ex.getMessage());
            }
        }
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
                return true;
            }
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private void putNtlmLogonDetailsToSession(HttpServletRequest request, NTLMLogonDetails details)
    {
        Object detailsMap = request.getSession().getAttribute(NTLM_AUTH_DETAILS);

        if (detailsMap != null)
        {
            ((Map<String, NTLMLogonDetails>) detailsMap).put(request.getRequestURI(), details);
            return;
        }
        else
        {
            Map<String, NTLMLogonDetails> newMap = new HashMap<String, NTLMLogonDetails>();
            newMap.put(request.getRequestURI(), details);
            request.getSession().setAttribute(NTLM_AUTH_DETAILS, newMap);
        }
    }

    @SuppressWarnings("unchecked")
    private NTLMLogonDetails getNtlmLogonDetailsFromSession(HttpServletRequest request)
    {
        Object detailsMap = request.getSession().getAttribute(NTLM_AUTH_DETAILS);
        if (detailsMap != null)
        {
            return ((Map<String, NTLMLogonDetails>) detailsMap).get(request.getRequestURI());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void removeNtlmLogonDetailsFromSession(HttpServletRequest request)
    {
        Object detailsMap = request.getSession().getAttribute(NTLM_AUTH_DETAILS);
        if (detailsMap != null)
        {
            ((Map<String, NTLMLogonDetails>) detailsMap).remove(request.getRequestURI());
        }
    }

}