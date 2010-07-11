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
package org.alfresco.filesys.auth.cifs;

import java.security.NoSuchAlgorithmException;

import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.AuthContext;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.ICifsAuthenticator;
import org.alfresco.jlan.server.auth.NTLanManAuthContext;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.smb.server.SMBSrvSession;
import org.alfresco.jlan.util.HexDump;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.ntlm.NTLMPassthruToken;
import org.alfresco.repo.transaction.RetryingTransactionHelper;

/**
 * Alfresco Authenticator Class
 * 
 * <p>The Alfresco authenticator implementation enables user level security mode using the Alfresco authentication
 * component.
 * 
 * <p>Note: Switching off encrypted password support will cause later NT4 service pack releases and
 * Win2000 to refuse to connect to the server without a registry update on the client.
 *
 * @author gkspencer
 */
public class AlfrescoCifsAuthenticator extends CifsAuthenticatorBase
{
    /**
     * Default Constructor
     * 
     * <p>Default to user mode security with encrypted password support.
     */
    public AlfrescoCifsAuthenticator()
    {
    }

    /**
     * Validate that the authentication component supports the required mode
     * 
     * @return boolean
     */
    protected boolean validateAuthenticationMode()
    {
        try
        {
            // Make sure the authentication component supports MD4 hashed passwords or passthru mode

            if (getNTLMAuthenticator().getNTLMMode() != NTLMMode.MD4_PROVIDER
                    && getNTLMAuthenticator().getNTLMMode() != NTLMMode.PASS_THROUGH)
                return false;
            return true;
        }
        catch (IllegalStateException e)
        {
            return false;
        }
    }
    
    /**
     * Authenticate a user
     * 
     * @param client Client information
     * @param sess Server session
     * @param alg Encryption algorithm
     */
    public int authenticateUser(final ClientInfo client, final SrvSession sess, final int alg)
    {
        // Check that the client is an Alfresco client
      
        if ( client instanceof AlfrescoClientInfo == false)
            return ICifsAuthenticator.AUTH_DISALLOW;
        
        AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
        
        // Check if this is an SMB/CIFS null session logon.
        //
        // The null session will only be allowed to connect to the IPC$ named pipe share.

        if (client.isNullSession() && sess instanceof SMBSrvSession)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Null CIFS logon allowed");

            return ICifsAuthenticator.AUTH_ALLOW;
        }

        // Check if the client is already authenticated, and it is not a null logon
        
        try
        {
            if ( alfClient.hasAuthenticationTicket() && client.getLogonType() != ClientInfo.LogonNull)
            {
                // Use the existing authentication token
                
                getAuthenticationService().validate(alfClient.getAuthenticationTicket());
    
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Re-using existing authentication token");
                
                // Return the authentication status
                
                return client.getLogonType() != ClientInfo.LogonGuest ? AUTH_ALLOW : AUTH_GUEST; 
            }
        }
        catch (AuthenticationException ex)
        {
            // Ticket no longer valid or maximum tickets exceeded
            alfClient.setAuthenticationTicket(null);
        }
        
        // Check if this is a guest logon
        
        int authSts = AUTH_DISALLOW;
        
        try
        {
            if ( client.isGuest() || client.getUserName().equalsIgnoreCase(getGuestUserName()))
            {
                // Check if guest logons are allowed
                
                if ( allowGuest() == false)
                    return AUTH_DISALLOW;
                
                //  Get a guest authentication token
                
                doGuestLogon( client, sess);
                
                // Indicate logged on as guest
                
                authSts = AUTH_GUEST;
                
                // DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Authenticated user " + client.getUserName() + " sts=" + getStatusAsString(authSts));
                
                // Return the guest status
                
                return authSts;
            }
            
            // Check if MD4 or passthru mode is configured
            
            else if ( getNTLMAuthenticator().getNTLMMode() == NTLMMode.MD4_PROVIDER)
            {
                // Start a transaction
                authSts = doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>()
                {

                    public Integer execute() throws Throwable
                    {
                        // Perform local MD4 password check
                        return doMD4UserAuthentication(client, sess, alg);
                    }
                });
              
            }
            else
            {
                // Start a transaction
                authSts = doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>()
                {

                    public Integer execute() throws Throwable
                    {
                        // Perform passthru authentication password check
                        return doPassthruUserAuthentication(client, sess, alg);
                    }
                });
              
            }
            
            // Check if the logon status indicates a guest logon
            
            if ( authSts == AUTH_GUEST)
            {
                // Only allow the guest logon if user mapping is enabled
                
                if ( mapUnknownUserToGuest())
                {
                    // Logon as guest, setup the security context
                
                    doGuestLogon( client, sess);
                }
                else
                {
                    // Do not allow the guest logon
                    
                    authSts = AUTH_DISALLOW;
                }
            }
        }
        catch ( Exception ex)
        {
          if ( logger.isDebugEnabled())
            logger.debug( ex);
        }
        
        // Check for an administrator logon
        
        if ( authSts == AUTH_ALLOW && client.getLogonType() == ClientInfo.LogonNormal)
        	checkForAdminUserName( client);
        
        // DEBUG
        
        if ( logger.isDebugEnabled())
            logger.debug("Authenticated user " + client.getUserName() + " sts=" + getStatusAsString(authSts) +
                    " via " + (getNTLMAuthenticator().getNTLMMode() == NTLMMode.MD4_PROVIDER ? "MD4" : "Passthru"));
                    
        // Return the authentication status
        
        return authSts;
    }

    /**
     * Authenticate a connection to a share.
     * 
     * @param client User/client details from the tree connect request.
     * @param share Shared device the client wants to connect to.
     * @param pwd Share password.
     * @param sess Server session.
     * @return int Granted file permission level or disallow status if negative. See the
     *         FilePermission class.
     */
    public int authenticateShareConnect(ClientInfo client, SharedDevice share, String sharePwd, SrvSession sess)
    {
        // Allow write access
        //
        // Main authentication is handled by authenticateUser()
        
        return ICifsAuthenticator.Writeable;
    }

    /**
     * Return an authentication context for the new session
     * 
     * @return AuthContext
     */
    public AuthContext getAuthContext( SMBSrvSession sess)
    {
        // Check if the client is already authenticated, and it is not a null logon

    	AuthContext authCtx = null;
    	
        if ( sess.hasAuthenticationContext() && sess.getClientInformation().getLogonType() != ClientInfo.LogonNull)
        {
            // Return the previous challenge, user is already authenticated

            authCtx = sess.getAuthenticationContext();
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Re-using existing challenge, already authenticated");
        }
        else if ( getNTLMAuthenticator().getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            // Create a new authentication context for the session

            authCtx = new NTLanManAuthContext();
            sess.setAuthenticationContext( authCtx);
        }
        else
        {
            // Create an authentication token for the session
            
            NTLMPassthruToken authToken = new NTLMPassthruToken( mapClientAddressToDomain( sess.getRemoteAddress()));
            
            // Run the first stage of the passthru authentication to get the challenge
            
            getNTLMAuthenticator().authenticate( authToken);
            
            // Save the authentication token for the second stage of the authentication
            
            authCtx = new AuthTokenAuthContext( authToken);
            sess.setAuthenticationContext( authCtx);
        }

        // Return the authentication context
        
        return authCtx;
    }
    
    /**
     * Perform MD4 user authentication
     * 
     * @param client Client information
     * @param sess Server session
     * @param alg Encryption algorithm
     * @return int 
     */
    private final int doMD4UserAuthentication(ClientInfo client, SrvSession sess, int alg)
    {
        // Get the stored MD4 hashed password for the user, or null if the user does not exist
        
        String md4hash = getNTLMAuthenticator().getMD4HashedPassword(client.getUserName());
        
        if ( md4hash != null)
        {
            // Check if the client has supplied an NTLM hashed password, if not then do not allow access
            
            if ( client.getPassword() == null)
                return ICifsAuthenticator.AUTH_BADPASSWORD;
            
            try
            {
                // Generate the local encrypted password using the challenge that was sent to the client
                
                byte[] p21 = new byte[21];
                byte[] md4byts = null;
                
              	md4byts = m_md4Encoder.decodeHash(md4hash);
                System.arraycopy(md4byts, 0, p21, 0, 16);
                
                // Get the challenge that was sent to the client
                
                NTLanManAuthContext authCtx = null;
                
                if ( sess.hasAuthenticationContext() && sess.getAuthenticationContext() instanceof NTLanManAuthContext)
                    authCtx = (NTLanManAuthContext) sess.getAuthenticationContext();
                else
                    return ICifsAuthenticator.AUTH_DISALLOW;
                
                // Generate the local hash of the password using the same challenge
                
                byte[] localHash = getEncryptor().doNTLM1Encryption(p21, authCtx.getChallenge());
                
                // Validate the password
                
                byte[] clientHash = client.getPassword();
                if ( clientHash == null || clientHash.length != 24)
                {
                	// Use the secondary password hash from the client
                
                	clientHash = client.getANSIPassword();
                	
                	// DEBUG
                	
                	if ( logger.isDebugEnabled())
                	{
                		logger.debug( "Using secondary password hash - " + HexDump.hexString(clientHash));
                		logger.debug( "                   Local hash - " + HexDump.hexString( localHash));
                	}
                }

                if ( clientHash == null || clientHash.length != localHash.length)
                    return ICifsAuthenticator.AUTH_BADPASSWORD;
                
                for ( int i = 0; i < clientHash.length; i++)
                {
                    if ( clientHash[i] != localHash[i])
                        return ICifsAuthenticator.AUTH_BADPASSWORD;
                }
                
                // Logging
                
                if ( logger.isInfoEnabled())
                	logger.info( "Logged on user " + client.getUserName() + " (" + sess.getRemoteAddress() + ")");
                
                // Set the current user to be authenticated, save the authentication token

                AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
                getAuthenticationComponent().setCurrentUser(client.getUserName());
                alfClient.setAuthenticationTicket(getAuthenticationService().getCurrentTicket());
                
                // Get the users home folder node, if available
                
                getHomeFolderForUser( client);
                
                // Indicate this is a normal user logon
                
                client.setLogonType( ClientInfo.LogonNormal);
                
                // Passwords match, grant access
                
                return ICifsAuthenticator.AUTH_ALLOW;
            }
            catch (AuthenticationException ex)
            {
                // Ticket no longer valid or maximum tickets exceeded
            }
            catch (NoSuchAlgorithmException ex)
            {
            }
            
            // Error during password check, do not allow access
            
            return ICifsAuthenticator.AUTH_DISALLOW;
        }

        // Check if this is an SMB/CIFS null session logon.
        //
        // The null session will only be allowed to connect to the IPC$ named pipe share.

        if (client.isNullSession() && sess instanceof SMBSrvSession)
            return ICifsAuthenticator.AUTH_ALLOW;
        
        // User does not exist, check if guest access is allowed
            
        return allowGuest() ? ICifsAuthenticator.AUTH_GUEST : ICifsAuthenticator.AUTH_DISALLOW;
    }
    
    /**
     * Perform passthru user authentication
     * 
     * @param client Client information
     * @param sess Server session
     * @param alg Encryption algorithm
     * @return int 
     */
    private final int doPassthruUserAuthentication(ClientInfo client, SrvSession sess, int alg)
    {
    	  // Default logon status to disallow
    	
        int authSts = ICifsAuthenticator.AUTH_DISALLOW;

        // Get the authentication token for the session

        AuthContext authCtx = sess.getAuthenticationContext();
        if ( authCtx == null || authCtx instanceof AuthTokenAuthContext == false)
            return ICifsAuthenticator.AUTH_DISALLOW;

        AuthTokenAuthContext tokenCtx = (AuthTokenAuthContext) authCtx;
        NTLMPassthruToken authToken = tokenCtx.getToken();
        
        if ( authToken == null)
            return ICifsAuthenticator.AUTH_DISALLOW;

        // Get the appropriate hashed password for the algorithm
        
        byte[] hashedPassword = null;
        
        if ( alg == NTLM1)
            hashedPassword = client.getPassword();
        else if ( alg == LANMAN)
            hashedPassword = client.getANSIPassword();
        else
        {
            // Invalid/unsupported algorithm specified
            
            return ICifsAuthenticator.AUTH_DISALLOW;
        }
        
        // Set the username and hashed password in the authentication token
        
        authToken.setUserAndPassword( client.getUserName(), hashedPassword, alg);
        
        // Authenticate the user
        
        String ticket = null;
        
        try
        {
            // Run the second stage of the passthru authentication
            
            getNTLMAuthenticator().authenticate( authToken);
            ticket = getAuthenticationService().getCurrentTicket();
            
            // Check if the user has been logged on as a guest

            if (authToken.isGuestLogon())
            {

                // Check if the local server allows guest access

                if (allowGuest() == true)
                {

                    // Allow the user access as a guest

                    authSts = ICifsAuthenticator.AUTH_GUEST;
                    
                    // Indicate that this is a guest logon
                    
                    client.setLogonType( ClientInfo.LogonGuest);
                }
            }
            else
            {

                // Allow the user full access to the server

                authSts = ICifsAuthenticator.AUTH_ALLOW;
                
                // Indicate that this is a normal user logon
                
                client.setLogonType( ClientInfo.LogonNormal);
            }

            // Set the current user to be authenticated, save the authentication ticket

            AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
            alfClient.setAuthenticationTicket(ticket);
            
            // Get the users home folder node, if available
            
            getHomeFolderForUser( client);
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Auth ticket " + ticket);
        }
        catch (AuthenticationException ex)
        {
            // Ticket no longer valid or maximum tickets exceeded
        }
        catch ( Exception ex)
        {
            logger.error("Error during passthru authentication", ex);
        }
        
        // Clear the authentication context
        
        sess.setAuthenticationContext(null);
        
        // Return the authentication status
        
        return authSts;
    }
}