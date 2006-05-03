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
package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;

import net.sf.acegisecurity.BadCredentialsException;

import org.alfresco.config.ConfigService;
import org.alfresco.filesys.server.auth.PasswordEncryptor;
import org.alfresco.filesys.server.auth.ntlm.NTLM;
import org.alfresco.filesys.server.auth.ntlm.NTLMLogonDetails;
import org.alfresco.filesys.server.auth.ntlm.NTLMMessage;
import org.alfresco.filesys.server.auth.ntlm.TargetInfo;
import org.alfresco.filesys.server.auth.ntlm.Type1NTLMMessage;
import org.alfresco.filesys.server.auth.ntlm.Type2NTLMMessage;
import org.alfresco.filesys.server.auth.ntlm.Type3NTLMMessage;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.MD4PasswordEncoder;
import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.ntlm.NTLMPassthruToken;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.LanguagesConfigElement;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * NTLM Authentication Filter Class
 * 
 * @author GKSpencer
 */
public class NTLMAuthenticationFilter extends AbstractAuthenticationFilter implements Filter 
{
    // NTLM authentication session object names
    
    public static final String NTLM_AUTH_SESSION = "_alfNTLMAuthSess";
    public static final String NTLM_AUTH_DETAILS = "_alfNTLMDetails";

    // Locale object stored in the session
    
    private static final String LOCALE = "locale";
    public static final String MESSAGE_BUNDLE = "alfresco.messages.webclient";
    
    // NTLM flags mask, used to mask out features that are not supported
    
    private static final int NTLM_FLAGS = NTLM.Flag56Bit + NTLM.FlagLanManKey + NTLM.FlagNegotiateNTLM +
                                          NTLM.FlagNegotiateOEM + NTLM.FlagNegotiateUnicode;
    
    // Debug logging
    
    private static Log logger = LogFactory.getLog(NTLMAuthenticationFilter.class);
    
    // Servlet context, required to get authentication service
    
    private ServletContext m_context;
    
    // File server configuration
    
    private ServerConfiguration m_srvConfig;
    
    // Various services required by NTLM authenticator
    
    private AuthenticationService m_authService;
    private AuthenticationComponent m_authComponent;
    private PersonService m_personService;
    private NodeService m_nodeService;
    private TransactionService m_transactionService;
    private ConfigService m_configService;
    
    // Password encryptor
    
    private PasswordEncryptor m_encryptor = new PasswordEncryptor();
    
    // Allow guest access
    
    private boolean m_allowGuest;
    
    // Login page address
    
    private String m_loginPage;

    // Random number generator used to generate challenge keys

    private Random m_random = new Random(System.currentTimeMillis());
    
    // MD4 hash decoder
    
    private MD4PasswordEncoder m_md4Encoder = new MD4PasswordEncoderImpl();
    
    // Local server name, from either the file servers config or DNS host name
    
    private String m_srvName;
    
    // List of available locales (from the web-client configuration)
    
    private List<String> m_languages;
    
    /**
     * Initialize the filter
     * 
     * @param args FilterConfig
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

        m_authService = (AuthenticationService) ctx.getBean("authenticationService");
        m_authComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        m_personService = (PersonService) ctx.getBean("personService");
        m_configService = (ConfigService) ctx.getBean("webClientConfigService");
        
        m_srvConfig = (ServerConfiguration) ctx.getBean(ServerConfiguration.SERVER_CONFIGURATION);
        
        // Check that the authentication component supports the required mode
        
        if ( m_authComponent.getNTLMMode() != NTLMMode.MD4_PROVIDER &&
                m_authComponent.getNTLMMode() != NTLMMode.PASS_THROUGH)
        {
            throw new ServletException("Required authentication mode not available");
        }
        
        // Get the local server name, try the file server config first
        
        if ( m_srvConfig != null)
        {
            m_srvName = m_srvConfig.getServerName();
            
            if ( m_srvName == null)
            {
                // CIFS server may not be running so the local server name has not been set, generate
                // a server name
                
                m_srvName = m_srvConfig.getLocalServerName(true) + "_A";
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
                if ( pos != -1)
                    m_srvName = m_srvName.substring(0, pos - 1);
            }
            catch (UnknownHostException ex)
            {
                // Log the error
                
                if ( logger.isErrorEnabled())
                    logger.error("NTLM filter, error getting local host name", ex);
            }
            
        }
        
        // Check if the server name is valid
        
        if ( m_srvName == null || m_srvName.length() == 0)
            throw new ServletException("Failed to get local server name");
        
        // Check if guest access is to be allowed
        
        String guestAccess = args.getInitParameter("AllowGuest");
        if ( guestAccess != null)
        {
            m_allowGuest = Boolean.parseBoolean(guestAccess);
            
            // Debug
            
            if ( logger.isDebugEnabled() && m_allowGuest)
                logger.debug("NTLM filter guest access allowed");
        }

        // Get a list of the available locales
        
        LanguagesConfigElement config = (LanguagesConfigElement) m_configService.
              getConfig("Languages").getConfigElement(LanguagesConfigElement.CONFIG_ELEMENT_ID);
  
        m_languages = config.getLanguages();
    }

    /**
     * Run the filter
     * 
     * @param sreq ServletRequest
     * @param sresp ServletResponse
     * @param chain FilterChain
     * @exception IOException
     * @exception ServletException
     */
    public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException,
            ServletException
    {
        // Get the HTTP request/response/session
        
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sresp;
        
        HttpSession httpSess = req.getSession(true);

        // Check if there is an authorization header with an NTLM security blob
        
        String authHdr = req.getHeader("Authorization");
        boolean reqAuth = false;
        
        if ( authHdr != null && authHdr.startsWith("NTLM"))
            reqAuth = true;
        
        // Check if the user is already authenticated
        
        User user = (User) httpSess.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
        
        if ( user != null && reqAuth == false)
        {
            try
            {
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("User " + user.getUserName() + " validate ticket");
                
                // Validate the user ticket
                
                m_authService.validate( user.getTicket());
                reqAuth = false;
                
                // Set the current locale
                
                I18NUtil.setLocale(Application.getLanguage(httpSess));
            }
            catch (AuthenticationException ex)
            {
                if ( logger.isErrorEnabled())
                    logger.error("Failed to validate user " + user.getUserName(), ex);
                
                reqAuth = true;
            }
        }

        // If the user has been validated and we do not require re-authentication then continue to
        // the next filter
        
        if ( reqAuth == false && user != null)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Authentication not required, chaining ...");
            
            // Chain to the next filter
            
            chain.doFilter(sreq, sresp);
            return;
        }

        // Check if the login page is being accessed, do not intercept the login page
        
        if ( req.getRequestURI().endsWith(getLoginPage()) == true)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Login page requested, chaining ...");
            
            // Chain to the next filter
            
            chain.doFilter( sreq, sresp);
            return;
        }
        
        // Check if the browser is Opera, if so then display the login page as Opera does not
        // support NTLM and displays an error page if a request to use NTLM is sent to it
        
        String userAgent = req.getHeader("user-agent");
        
        if ( userAgent != null && userAgent.indexOf("Opera ") != -1)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Opera detected, redirecting to login page");

            // Redirect to the login page
            
            resp.sendRedirect(req.getContextPath() + "/faces" + getLoginPage());
            return;
        }
        
        // Check the authorization header
        
        if ( authHdr == null) {

            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("New NTLM auth request from " + req.getRemoteHost() + " (" +
                        req.getRemoteAddr() + ":" + req.getRemotePort() + ")");
            
            // Send back a request for NTLM authentication
            
            resp.setHeader("WWW-Authenticate", "NTLM");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            resp.flushBuffer();
        }
        else
        {
            // Decode the received NTLM blob and validate
            
            final byte[] ntlmByts = Base64.decodeBase64( authHdr.substring(5).getBytes());
            int ntlmTyp = NTLMMessage.isNTLMType(ntlmByts);
         
            if ( ntlmTyp == NTLM.Type1)
            {
                // Process the type 1 NTLM message
                
                Type1NTLMMessage type1Msg = new Type1NTLMMessage(ntlmByts);
                processType1(type1Msg, req, resp, httpSess);
            }
            else if ( ntlmTyp == NTLM.Type3)
            {
                // Process the type 3 NTLM message
                
                Type3NTLMMessage type3Msg = new Type3NTLMMessage(ntlmByts);
                processType3(type3Msg, req, resp, httpSess, chain);
            }
            else
            {
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("NTLM not handled, redirecting to login page");
                
                // Redirect to the login page
                
                resp.sendRedirect(req.getContextPath() + "/faces" + getLoginPage());
            }
        }
    }

    /**
     * Return the login page address
     * 
     * @return String
     */
    private String getLoginPage()
    {
       if (m_loginPage == null)
       {
          m_loginPage = Application.getLoginPage(m_context);
       }
       
       return m_loginPage;
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
     * @param resp HttpServletResponse
     * @param httpSess HttpSession
     * @exception IOException
     */
    private void processType1(Type1NTLMMessage type1Msg, HttpServletRequest req, HttpServletResponse resp,
            HttpSession httpSess) throws IOException
    {
        // Debug
        
        if ( logger.isDebugEnabled())
            logger.debug("Received type1 " + type1Msg);
        
        // Get the existing NTLM details
        
        NTLMLogonDetails ntlmDetails = null;
        
        if ( httpSess != null)
        {
            ntlmDetails = (NTLMLogonDetails) httpSess.getAttribute(NTLM_AUTH_DETAILS);
        }
        
        // Check if cached logon details are available
        
        if ( ntlmDetails != null && ntlmDetails.hasType2Message() && ntlmDetails.hasNTLMHashedPassword() &&
                ntlmDetails.hasAuthenticationToken())
        {
            // Get the authentication server type2 response
            
            Type2NTLMMessage cachedType2 = ntlmDetails.getType2Message();

            byte[] type2Bytes = cachedType2.getBytes();
            String ntlmBlob = "NTLM " + new String(Base64.encodeBase64(type2Bytes));

            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Sending cached NTLM type2 to client - " + cachedType2);
            
            // Send back a request for NTLM authentication
            
            resp.setHeader("WWW-Authenticate", ntlmBlob);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            resp.flushBuffer();
            return;
        }
        else
        {
            // Clear any cached logon details
            
            httpSess.removeAttribute(NTLM_AUTH_DETAILS);

            // Set the 8 byte challenge for the new logon request
            
            byte[] challenge = null;
            NTLMPassthruToken authToken = null;
            
            if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
            {
                // Generate a random 8 byte challenge

                challenge = new byte[8];
                DataPacker.putIntelLong(m_random.nextLong(), challenge, 0);
            }
            else
            {
                // Create an authentication token for the new logon
                
                authToken = new NTLMPassthruToken();
                
                // Run the first stage of the passthru authentication to get the challenge
                
                m_authComponent.authenticate( authToken);
                
                // Get the challenge from the token
                
                if ( authToken.getChallenge() != null)
                    challenge = authToken.getChallenge().getBytes();
            }
            
            // Get the flags from the client request and mask out unsupported features
            
            int ntlmFlags = type1Msg.getFlags() & NTLM_FLAGS;
            
            // Build a type2 message to send back to the client, containing the challenge
            
            List<TargetInfo> tList = new ArrayList<TargetInfo>();
            tList.add(new TargetInfo(NTLM.TargetServer, m_srvName));
            
            Type2NTLMMessage type2Msg = new Type2NTLMMessage();
            type2Msg.buildType2(ntlmFlags, m_srvName, challenge, null, tList);
            
            // Store the NTLM logon details, cache the type2 message, and token if using passthru
            
            ntlmDetails = new NTLMLogonDetails();
            ntlmDetails.setType2Message( type2Msg);
            ntlmDetails.setAuthenticationToken(authToken);
            
            httpSess.setAttribute(NTLM_AUTH_DETAILS, ntlmDetails);
            
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Sending NTLM type2 to client - " + type2Msg);
            
            // Send back a request for NTLM authentication
            
            byte[] type2Bytes = type2Msg.getBytes();
            String ntlmBlob = "NTLM " + new String(Base64.encodeBase64(type2Bytes));

            resp.setHeader("WWW-Authenticate", ntlmBlob);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            
            resp.flushBuffer();
            return;
        }
    }
    
    /**
     * Process a type 3 NTLM message
     * 
     * @param type3Msg Type3NTLMMessage
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param httpSess HttpSession
     * @param chain FilterChain
     * @exception IOException
     * @exception ServletException
     */
    private void processType3(Type3NTLMMessage type3Msg, HttpServletRequest req, HttpServletResponse resp,
            HttpSession httpSess, FilterChain chain) throws IOException, ServletException
    {
        // Debug
        
        if ( logger.isDebugEnabled())
            logger.debug("Received type3 " + type3Msg);
        
        // Get the existing NTLM details
        
        NTLMLogonDetails ntlmDetails = null;
        User user = null;
        
        if ( httpSess != null)
        {
            ntlmDetails = (NTLMLogonDetails) httpSess.getAttribute(NTLM_AUTH_DETAILS);
            user = (User) httpSess.getAttribute(AuthenticationHelper.AUTHENTICATION_USER);
        }
        
        // Get the NTLM logon details
        
        String userName = type3Msg.getUserName();
        String workstation = type3Msg.getWorkstation();
        String domain = type3Msg.getDomain();
        
        boolean authenticated = false;
        boolean useNTLM = true;
        
        // Check if we are using cached details for the authentication
        
        if ( user != null && ntlmDetails != null && ntlmDetails.hasNTLMHashedPassword())
        {
            // Check if the received NTLM hashed password matches the cached password
            
            byte[] ntlmPwd = type3Msg.getNTLMHash();
            byte[] cachedPwd = ntlmDetails.getNTLMHashedPassword();
            
            if ( ntlmPwd != null)
            {
                if ( ntlmPwd.length == cachedPwd.length)
                {
                    authenticated = true;
                    for ( int i = 0; i < ntlmPwd.length; i++)
                    {
                        if ( ntlmPwd[i] != cachedPwd[i])
                            authenticated = false;
                    }
                }
            }
            
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Using cached NTLM hash, authenticated = " + authenticated);
            
            try
            {
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("User " + user.getUserName() + " validate ticket");
                
                // Validate the user ticket
                
                m_authService.validate( user.getTicket());
                
                // Set the current locale
                
                I18NUtil.setLocale(Application.getLanguage(httpSess));
            }
            catch (AuthenticationException ex)
            {
                if ( logger.isErrorEnabled())
                    logger.error("Failed to validate user " + user.getUserName(), ex);
                
                // Redirect to the login page
                
                resp.sendRedirect(req.getContextPath() + "/faces" + getLoginPage());
                return;
            }
            
            // Allow the user to access the requested page
            
            chain.doFilter( req, resp);
            return;
        }
        else
        {
            // Check if we are using local MD4 password hashes or passthru authentication
            
            if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
            {
                // Get the stored MD4 hashed password for the user, or null if the user does not exist
                
                String md4hash = m_authComponent.getMD4HashedPassword(userName);
                
                if ( md4hash != null)
                {
                    // Generate the local encrypted password using the challenge that was sent to the client
                    
                    byte[] p21 = new byte[21];
                    byte[] md4byts = m_md4Encoder.decodeHash(md4hash);
                    System.arraycopy(md4byts, 0, p21, 0, 16);
                    
                    // Generate the local hash of the password using the same challenge
                    
                    byte[] localHash = null;
                    
                    try
                    {
                        localHash = m_encryptor.doNTLM1Encryption(p21, ntlmDetails.getChallengeKey());
                    }
                    catch (NoSuchAlgorithmException ex)
                    {
                    }
                    
                    // Validate the password
                    
                    byte[] clientHash = type3Msg.getNTLMHash();

                    if ( clientHash != null && localHash != null && clientHash.length == localHash.length)
                    {
                        int i = 0;

                        while ( i < clientHash.length && clientHash[i] == localHash[i])
                            i++;
                        
                        if ( i == clientHash.length)
                            authenticated = true;
                    }
                }
                else
                {
                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("User " + userName + " does not have Alfresco account");
                    
                    // Bypass NTLM authentication and display the logon screen, user account does not
                    // exist in Alfresco
                    
                    authenticated = false;
                }
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
                }
                catch (BadCredentialsException ex)
                {
                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Authentication failed, " + ex.getMessage());
                }
                catch (AuthenticationException ex)
                {
                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Authentication failed, " + ex.getMessage());
                }
                finally
                {
                    // Clear the authentication token from the NTLM details
                    
                    ntlmDetails.setAuthenticationToken(null);
                }
            }
            
            // Check if the user has been authenticated, if so then setup the user environment
            
            if ( authenticated == true)
            {
                UserTransaction tx = m_transactionService.getUserTransaction();
                NodeRef homeSpaceRef = null;
                
                try
                {
                    tx.begin();

                    // Get user details for the authenticated user
                    m_authComponent.setCurrentUser(userName.toLowerCase());
                    
                    // The user name used may be a different case to the NTLM supplied user name, read the current
                    // user and use that name
                    userName = m_authComponent.getCurrentUserName();
                    
                    // Setup User object and Home space ID etc.
                    NodeRef personNodeRef = m_personService.getPerson(userName);
                    String currentTicket = m_authService.getCurrentTicket();
                    user = new User(userName, currentTicket, personNodeRef);
                    
                    homeSpaceRef = (NodeRef) m_nodeService.getProperty(
                            personNodeRef,
                            ContentModel.PROP_HOMEFOLDER);
                    user.setHomeSpaceId(homeSpaceRef.getId());
                    
                    // commit
                    tx.commit();
                }
                catch (Throwable ex)
                {
                    try
                    {
                        tx.rollback();
                    }
                    catch (Exception ex2)
                    {
                        logger.error("Failed to rollback transaction", ex2);
                    }
                    if(ex instanceof RuntimeException)
                    {
                        throw (RuntimeException)ex;
                    }
                    else if(ex instanceof IOException)
                    {
                        throw (IOException)ex;
                    }
                    else if(ex instanceof ServletException)
                    {
                        throw (ServletException)ex;
                    }
                    else
                    {
                        throw new RuntimeException("Authentication setup failed", ex);
                    }
                }
                
                // Store the user
                
                httpSess.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
                httpSess.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);

                // Set the current locale from the Accept-Lanaguage header if available
                
                Locale userLocale = parseAcceptLanguageHeader(req, m_languages);
                
                if ( userLocale != null)
                {
                    httpSess.setAttribute(LOCALE, userLocale);
                    httpSess.removeAttribute(MESSAGE_BUNDLE);
                }

                // Set the locale using the session
                
                I18NUtil.setLocale(Application.getLanguage(httpSess));

                // Update the NTLM logon details in the session
                
                if ( ntlmDetails == null)
                {
                    // No cached NTLM details
                    
                    ntlmDetails = new NTLMLogonDetails( userName, workstation, domain, false, m_srvName);
                    
                    httpSess.setAttribute(NTLM_AUTH_DETAILS, ntlmDetails);
                    
                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("No cached NTLM details, created");
                    
                }
                else
                {
                    // Update the cached NTLM details
                    
                    ntlmDetails.setDetails(userName, workstation, domain, false, m_srvName);
                    ntlmDetails.setNTLMHashedPassword(type3Msg.getNTLMHash());

                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Updated cached NTLM details");
                }
                
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("User logged on via NTLM, " + ntlmDetails);

                // If the original URL requested was the login page then redirect to the browse view
                
                if (req.getRequestURI().endsWith(getLoginPage()) == true)
                {
                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Login page requested, redirecting to browse page");

                    //  Redirect to the browse view
                    
                    resp.sendRedirect(req.getContextPath() + "/faces/jsp/browse/browse.jsp");
                    return;
                }
                else
                {
                    // Allow the user to access the requested page
                    
                    chain.doFilter( req, resp);
                    return;
                }
            }
            else
            {
                // Check if NTLM should be used, switched off if the user does not exist in the Alfresco
                // user database
                
                if (useNTLM == true)
                {
                    // Remove any existing session and NTLM details from the session
                    
                    httpSess.removeAttribute(NTLM_AUTH_SESSION);
                    httpSess.removeAttribute(NTLM_AUTH_DETAILS);
                    
                    // Force the logon to start again
                    
                    resp.setHeader("WWW-Authenticate", "NTLM");
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    
                    resp.flushBuffer();
                    return;
                }
                else
                {
                    // Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Redirecting to login page");

                    // Redirect to the login page
                    
                    resp.sendRedirect(req.getContextPath() + "/faces" + getLoginPage());
                    return;
                }
            }
        }
    }
}
