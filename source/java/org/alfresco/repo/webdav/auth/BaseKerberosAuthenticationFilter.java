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

package org.alfresco.repo.webdav.auth;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.sasl.RealmCallback;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.jlan.server.auth.kerberos.KerberosDetails;
import org.alfresco.jlan.server.auth.kerberos.SessionSetupPrivilegedAction;
import org.alfresco.jlan.server.auth.spnego.NegTokenInit;
import org.alfresco.jlan.server.auth.spnego.NegTokenTarg;
import org.alfresco.jlan.server.auth.spnego.OID;
import org.alfresco.jlan.server.auth.spnego.SPNEGO;
import org.alfresco.repo.SessionUser;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.web.auth.KerberosCredentials;
import org.alfresco.repo.web.auth.TicketCredentials;
import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.Oid;

/**
 * Base class with common code and initialisation for Kerberos authentication filters.
 * 
 * @author gkspencer
 */
public abstract class BaseKerberosAuthenticationFilter extends BaseSSOAuthenticationFilter implements CallbackHandler {

	// Constants
    //
    // Default login configuration entry name
    
    private static final String LoginConfigEntry = "AlfrescoHTTP";
    
    // Kerberos settings
    //
    // Account name and password for server ticket
    //
    // The account name must be built from the HTTP server name, in the format :-
    //
    //      HTTP/<server_name>@<realm>
    
    private String m_accountName;
    private String m_password;
    
    // Kerberos realm
    
    private String m_krbRealm;
    
    // Login configuration entry name
    
    private String m_loginEntryName = LoginConfigEntry; 

    // Server login context
    
    private LoginContext m_loginContext;
        
    // Should we strip the @domain suffix from the Kerberos username?

    private boolean m_stripKerberosUsernameSuffix = true;

    /**
     * Sets the HTTP service account password. (the Principal should be configured in java.login.config)
     * 
     * @param password
     *            the password to set
     */
    public void setPassword(String password)
    {
        this.m_password = password;
    }

    /**
     * Sets the HTTP service account realm.
     * 
     * @param realm the realm to set
     */
    public void setRealm(String realm)
    {
        m_krbRealm = realm;
    }

    /**
     * Sets the HTTP service login configuration entry name. The default is <code>"AlfrescoHTTP"</code>.
     * 
     * @param loginEntryName
     *            the loginEntryName to set
     */
    public void setJaasConfigEntryName(String jaasConfigEntryName)
    {
        m_loginEntryName = jaasConfigEntryName;
    }
    
    /**
     * Indicates whether the @domain suffix should be removed from Kerberos user IDs
     * 
     * @param stripKerberosUsernameSuffix
     *            <code>true</code> if the @domain suffix should be removed from Kerberos user IDs
     */
    public void setStripKerberosUsernameSuffix(boolean stripKerberosUsernameSuffix)
    {
        m_stripKerberosUsernameSuffix = stripKerberosUsernameSuffix;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#init()
     */
    @Override
    protected void init() throws ServletException
    {
        super.init();

        if ( m_krbRealm == null)
        {
            throw new ServletException("Kerberos realm not specified");
        }
        
        if ( m_password == null)
        {
            throw new ServletException("HTTP service account password not specified");
        }
        
        if (m_loginEntryName == null)
        {
            throw new ServletException("Invalid login entry specified");
        }
        
        // Get the local host name        
        String localName = null;
        
        try
        {
        	localName = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch ( UnknownHostException ex)
        {
        	throw new ServletException( "Failed to get local host name");
        }
        
        // Create a login context for the HTTP server service
        
        try
        {
            // Login the HTTP server service
            
            m_loginContext = new LoginContext( m_loginEntryName, this);
            m_loginContext.login();
            
            // DEBUG
            
            if ( getLogger().isDebugEnabled())
            	getLogger().debug( "HTTP Kerberos login successful");
        }
        catch ( LoginException ex)
        {
            // Debug
            
            if ( getLogger().isErrorEnabled())
                getLogger().error("HTTP Kerberos web filter error", ex);
            
            throw new ServletException("Failed to login HTTP server service");
        }
        
        // Get the HTTP service account name from the subject
        
        Subject subj = m_loginContext.getSubject();
        Principal princ = subj.getPrincipals().iterator().next();
        
        m_accountName = princ.getName();
        
        // DEBUG
        
        if ( getLogger().isDebugEnabled())
        	getLogger().debug("Logged on using principal " + m_accountName);
        
        // Create the Oid list for the SPNEGO NegTokenInit, include NTLMSSP for fallback
        
        Vector<Oid> mechTypes = new Vector<Oid>();

        mechTypes.add(OID.KERBEROS5);
        mechTypes.add(OID.MSKERBEROS5);
    
        // Build the SPNEGO NegTokenInit blob

        try
        {
            // Build the mechListMIC principle
            //
            // Note: This field is not as specified
            
            String mecListMIC = null;
            
            StringBuilder mic = new StringBuilder();
            mic.append( localName);
            mic.append("$@");
            mic.append( m_krbRealm);
            
            mecListMIC = mic.toString();
            
            // Build the SPNEGO NegTokenInit that contains the authentication types that the HTTP server accepts
            
            NegTokenInit negTokenInit = new NegTokenInit(mechTypes, mecListMIC);
            
            // Encode the NegTokenInit blob
            negTokenInit.encode();
        }
        catch (IOException ex)
        {
            // Debug
            
            if ( getLogger().isErrorEnabled())
                getLogger().error("Error creating SPNEGO NegTokenInit blob", ex);
            
            throw new ServletException("Failed to create SPNEGO NegTokenInit blob");
        }
    }

    
    public boolean authenticateRequest(ServletContext context, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException
    {
        // Check if there is an authorization header with an SPNEGO security blob
        
        String authHdr = req.getHeader("Authorization");
        boolean reqAuth = false;
        
        if ( authHdr != null)
        {
        	// Check for a Kerberos/SPNEGO authorization header
        	
        	if ( authHdr.startsWith( "Negotiate"))
        		reqAuth = true;
        	else if ( authHdr.startsWith( "NTLM"))
        	{
        		if ( getLogger().isDebugEnabled())
        			getLogger().debug("Received NTLM logon from client");
        		
        		// Restart the authentication
        		
            	restartLoginChallenge(context, req, resp);
        		return false;
        	}
        }
        
        // Check if the user is already authenticated        
        SessionUser user = getSessionUser(context, req, resp, true);
        HttpSession httpSess = req.getSession(true);
        
        // If the user has been validated and we do not require re-authentication then continue to
        // the next filter
        if ( user != null && reqAuth == false)
        {
            // Filter validate hook
            onValidate( context, req, resp, new TicketCredentials(user.getTicket()));

            // Debug
            
            if ( getLogger().isDebugEnabled())
                getLogger().debug("Authentication not required (user), chaining ...");
            
            // Chain to the next filter
            
            return true;
        }

        // Check if the login page is being accessed, do not intercept the login page
        if (hasLoginPage() && req.getRequestURI().endsWith(getLoginPage()))
        {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Login page requested, chaining ...");
            
            // Chain to the next filter

            return true;
        }

        // Check the authorization header
        
        if ( authHdr == null) {

        	// If ticket based logons are allowed, check for a ticket parameter
        	
        	if ( allowsTicketLogons())
        	{
        		// Check if a ticket parameter has been specified in the reuqest
        		
        		if (checkForTicketParameter(context, req, resp))
        		{
                    // Filter validate hook
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("Authenticated with a ticket parameter.");
                    onValidate( context, req, resp, new TicketCredentials(user.getTicket()));

                    // Chain to the next filter
                    
        	        return true;
        		}
        	}
        	
            // Debug
            
            if ( getLogger().isDebugEnabled())
                getLogger().debug("New Kerberos auth request from " + req.getRemoteHost() + " (" +
                        req.getRemoteAddr() + ":" + req.getRemotePort() + ")");
            
            // Send back a request for SPNEGO authentication
            logonStartAgain(context, req, resp);
            return false;
        }
        else
        {
            // Decode the received SPNEGO blob and validate
            
            final byte[] spnegoByts = Base64.decodeBase64( authHdr.substring(10).getBytes());
         
            // Check if the client sent an NTLMSSP blob
            
            if ( isNTLMSSPBlob( spnegoByts, 0))
            {
            	if ( getLogger().isDebugEnabled())
            		getLogger().debug( "Client sent an NTLMSSP security blob");
            	
        		// Restart the authentication
        		
            	restartLoginChallenge(context, req, resp);
        		return false;
            }
            	
            //  Check the received SPNEGO token type

            int tokType = -1;
            
            try
            {
                tokType = SPNEGO.checkTokenType( spnegoByts, 0, spnegoByts.length);
            }
            catch ( IOException ex)
            {
            }

            // Check for a NegTokenInit blob
            
            if ( tokType == SPNEGO.NegTokenInit)
            {
                //  Parse the SPNEGO security blob to get the Kerberos ticket
                
                NegTokenInit negToken = new NegTokenInit();
                
                try
                {
                    // Decode the security blob
                    
                    negToken.decode( spnegoByts, 0, spnegoByts.length);

                    //  Determine the authentication mechanism the client is using and logon
                    
                    String oidStr = null;
                    if ( negToken.numberOfOids() > 0)
                        oidStr = negToken.getOidAt( 0).toString();
                    
                    if (  oidStr != null && (oidStr.equals( OID.ID_MSKERBEROS5) || oidStr.equals(OID.ID_KERBEROS5)))
                    {
                        //  Kerberos logon
                        
                        try
                        {
                            NegTokenTarg negTokenTarg = doKerberosLogon( negToken, req, resp, httpSess);
                            if ( negTokenTarg != null)
                            {
                                // Allow the user to access the requested page
                                onValidate(context, req, resp, new KerberosCredentials(negToken, negTokenTarg));
                                if (getLogger().isDebugEnabled())
                                    getLogger().debug("Authenticated through Kerberos.");
                                return true;
                            }
                            else
                            {
                                // Send back a request for SPNEGO authentication
                                if (getLogger().isDebugEnabled())
                                    getLogger().debug("Failed SPNEGO authentication.");
                            	restartLoginChallenge(context, req, resp);
                            	return false;
                            }
                        }
                        catch (AuthenticationException ex)
                        {
                            // Even though the user successfully authenticated, the ticket may not be granted, e.g. to
                            // max user limit
                            if (getLogger().isDebugEnabled())
                                getLogger().debug("Validate failed.", ex);
                            onValidateFailed(context, req, resp, httpSess, new TicketCredentials(user.getTicket()));
                            return false;
                        }
                    }
                    else
                    {
                        //  Unsupported mechanism, e.g. NegoEx
                        
                        if ( getLogger().isDebugEnabled())
                            getLogger().debug( "Unsupported SPNEGO mechanism " + oidStr);
    
                        // Try again!
                        
                        restartLoginChallenge(context, req,  resp);
                    }
                }
                catch ( IOException ex)
                {
                    // Log the error
                    
                	if ( getLogger().isDebugEnabled())
                    	getLogger().debug(ex);
                }
            }
            else
            {
                //  Unknown SPNEGO token type
                
            	if ( getLogger().isDebugEnabled())
            		getLogger().debug( "Unknown SPNEGO token type");

                // Send back a request for SPNEGO authentication
                
            	restartLoginChallenge(context, req,  resp);
            }
        }
        return false;
    }

    /**
     * JAAS callback handler
     * 
     * @param callbacks Callback[]
     * @exception IOException
     * @exception UnsupportedCallbackException
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        // Process the callback list
        if (getLogger().isDebugEnabled())
            getLogger().debug("Processing the JAAS callback list of " + callbacks.length + " items.");
        for (int i = 0; i < callbacks.length; i++)
        {
            // Request for user name
            
            if (callbacks[i] instanceof NameCallback)
            {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Request for user name.");
                NameCallback cb = (NameCallback) callbacks[i];
                cb.setName(m_accountName);
            }
            
            // Request for password
            else if (callbacks[i] instanceof PasswordCallback)
            {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Request for password.");
                PasswordCallback cb = (PasswordCallback) callbacks[i];
                cb.setPassword(m_password.toCharArray());
            }
            
            // Request for realm
            
            else if (callbacks[i] instanceof RealmCallback)
            {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("Request for realm.");
                RealmCallback cb = (RealmCallback) callbacks[i];
                cb.setText(m_krbRealm);
            }
            else
            {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }
    
    /**
     * Perform a Kerberos login and return an SPNEGO response
     * 
     * @param negToken NegTokenInit
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param httpSess HttpSession
     * @return NegTokenTarg
     */
    private final NegTokenTarg doKerberosLogon( NegTokenInit negToken, HttpServletRequest req, HttpServletResponse resp, HttpSession httpSess)
    {
        //  Authenticate the user
        
        KerberosDetails krbDetails = null;
        String userName = null;
        NegTokenTarg negTokenTarg = null;
        
        try
        {
            //  Run the session setup as a privileged action
            
            SessionSetupPrivilegedAction sessSetupAction = new SessionSetupPrivilegedAction( m_accountName, negToken.getMechtoken());
            Object result = Subject.doAs( m_loginContext.getSubject(), sessSetupAction);
    
            if ( result != null)
            {
                // Access the Kerberos response
                
                krbDetails = (KerberosDetails) result;
                userName = m_stripKerberosUsernameSuffix ? krbDetails.getUserName() : krbDetails.getSourceName();
                
                // Create the NegTokenTarg response blob
                
                negTokenTarg = new NegTokenTarg( SPNEGO.AcceptCompleted, OID.KERBEROS5, krbDetails.getResponseToken());
                
                // Check if the user has been authenticated, if so then setup the user environment
                
                if ( negTokenTarg != null)
                {
                    // Create and store the user authentication context

                    SessionUser user = createUserEnvironment(httpSess, userName);

                    // Debug

                    if (getLogger().isDebugEnabled())
                        getLogger().debug("User " + user.getUserName() + " logged on via Kerberos");
                }
            }
            else
            {
            	// Debug
            	
            	if ( getLogger().isDebugEnabled())
            		getLogger().debug( "No SPNEGO response, Kerberos logon failed");
            }
        }
        catch (AuthenticationException ex)
        {
            // Pass on validation failures
            if (getLogger().isDebugEnabled())
                getLogger().debug("Failed to validate user " + userName, ex);

            throw ex;
        }
        catch (Exception ex)
        {
            // Log the error

        	if ( getLogger().isDebugEnabled())
        		getLogger().debug("Kerberos logon error", ex);
        }
    
        // Return the response SPNEGO blob
        
        return negTokenTarg;
    }
    
    
    /**
     * Restart the Kerberos logon process
     * 
     * @param resp HttpServletResponse
     * @param httpSess HttpSession
     * @throws IOException
     */
    public void restartLoginChallenge(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        HttpSession session = req.getSession(false);
        if (session != null)
        {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Clearing session.");
            session.invalidate();
        }
        logonStartAgain(context, req, resp);
    }
    

    /**
     * The logon to start again
     * 
     * @param resp HttpServletResponse
     * @param httpSess HttpSession
     * @throws IOException
     */
    public void logonStartAgain(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
        if (getLogger().isDebugEnabled())
            getLogger().debug("Issuing login challenge to browser.");
        // Force the logon to start again
        resp.setHeader("WWW-Authenticate", "Negotiate");
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        writeLoginPageLink(context, req, resp);
        
        resp.flushBuffer();
    }
}
