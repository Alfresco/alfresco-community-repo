/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.auth;

import java.security.NoSuchAlgorithmException;
import net.sf.acegisecurity.Authentication;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.AuthContext;
import org.alfresco.filesys.server.auth.CifsAuthenticator;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.auth.NTLanManAuthContext;
import org.alfresco.filesys.smb.server.SMBSrvSession;
import org.alfresco.filesys.util.HexDump;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.ntlm.NTLMPassthruToken;

/**
 * Alfresco Authenticator Class
 * 
 * <p>The Alfresco authenticator implementation enables user level security mode using the Alfresco authentication
 * component.
 * 
 * <p>Note: Switching off encrypted password support will cause later NT4 service pack releases and
 * Win2000 to refuse to connect to the server without a registry update on the client.
 * 
 * @author GKSpencer
 */
public class AlfrescoAuthenticator extends CifsAuthenticator
{
    /**
     * Default Constructor
     * 
     * <p>Default to user mode security with encrypted password support.
     */
    public AlfrescoAuthenticator()
    {
    }

    /**
     * Validate that the authentication component supports the required mode
     * 
     * @return boolean
     */
    protected boolean validateAuthenticationMode()
    {
        // Make sure the authentication component supports MD4 hashed passwords or passthru mode
        
        if ( m_authComponent.getNTLMMode() != NTLMMode.MD4_PROVIDER &&
                m_authComponent.getNTLMMode() != NTLMMode.PASS_THROUGH)
            return false;
        return true;
    }
    
    /**
     * Authenticate a user
     * 
     * @param client Client information
     * @param sess Server session
     * @param alg Encryption algorithm
     */
    public int authenticateUser(ClientInfo client, SrvSession sess, int alg)
    {
        // Check if this is an SMB/CIFS null session logon.
        //
        // The null session will only be allowed to connect to the IPC$ named pipe share.

        if (client.isNullSession() && sess instanceof SMBSrvSession)
        {
            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Null CIFS logon allowed");

            return CifsAuthenticator.AUTH_ALLOW;
        }

        // Check if the client is already authenticated, and it is not a null logon
        
        if ( client.getAuthenticationToken() != null && client.getLogonType() != ClientInfo.LogonNull)
        {
            // Use the existing authentication token
            
            m_authComponent.setCurrentUser(client.getUserName());

            // Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Re-using existing authentication token");
            
            // Return the authentication status
            
            return client.getLogonType() != ClientInfo.LogonGuest ? AUTH_ALLOW : AUTH_GUEST; 
        }
        
        // Check if this is a guest logon
        
        int authSts = AUTH_DISALLOW;
        
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
        
        else if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            // Perform local MD4 password check
            
            authSts = doMD4UserAuthentication(client, sess, alg);
        }
        else
        {
            // Perform passthru authentication password check
            
            authSts = doPassthruUserAuthentication(client, sess, alg);
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
        
        // DEBUG
        
        if ( logger.isDebugEnabled())
            logger.debug("Authenticated user " + client.getUserName() + " sts=" + getStatusAsString(authSts) +
                    " via " + (m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER ? "MD4" : "Passthru"));
                    
        // Return the authentication status
        
        return authSts;
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
    	
        if ( sess.hasAuthenticationContext() && sess.hasAuthenticationToken() &&
                sess.getClientInformation().getLogonType() != ClientInfo.LogonNull)
        {
            // Return the previous challenge, user is already authenticated

            authCtx = (NTLanManAuthContext) sess.getAuthenticationContext();
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Re-using existing challenge, already authenticated");
        }
        else if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            // Create a new authentication context for the session

            authCtx = new NTLanManAuthContext();
            sess.setAuthenticationContext( authCtx);
        }
        else
        {
            // Create an authentication token for the session
            
            NTLMPassthruToken authToken = new NTLMPassthruToken();
            
            // Run the first stage of the passthru authentication to get the challenge
            
            m_authComponent.authenticate( authToken);
            
            // Save the authentication token for the second stage of the authentication
            
            sess.setAuthenticationToken(authToken);
            
            // Get the challenge from the token
            
            if ( authToken.getChallenge() != null)
            {
            	authCtx = new NTLanManAuthContext( authToken.getChallenge().getBytes());
            	sess.setAuthenticationContext( authCtx);
            }
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
    	// Start a transaction
    	
    	sess.beginReadTransaction( m_transactionService);
    	
        // Get the stored MD4 hashed password for the user, or null if the user does not exist
        
        String md4hash = m_authComponent.getMD4HashedPassword(client.getUserName());
        
        if ( md4hash != null)
        {
            // Check if the client has supplied an NTLM hashed password, if not then do not allow access
            
            if ( client.getPassword() == null)
                return CifsAuthenticator.AUTH_BADPASSWORD;
            
            try
            {
                // Generate the local encrypted password using the challenge that was sent to the client
                
                byte[] p21 = new byte[21];
                byte[] md4byts = m_md4Encoder.decodeHash(md4hash);
                System.arraycopy(md4byts, 0, p21, 0, 16);
                
                // Get the challenge that was sent to the client
                
                NTLanManAuthContext authCtx = null;
                
                if ( sess.hasAuthenticationContext() && sess.getAuthenticationContext() instanceof NTLanManAuthContext)
                    authCtx = (NTLanManAuthContext) sess.getAuthenticationContext();
                else
                    return CifsAuthenticator.AUTH_DISALLOW;
                
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
                    return CifsAuthenticator.AUTH_BADPASSWORD;
                
                for ( int i = 0; i < clientHash.length; i++)
                {
                    if ( clientHash[i] != localHash[i])
                        return CifsAuthenticator.AUTH_BADPASSWORD;
                }
                
                // Set the current user to be authenticated, save the authentication token
                
                client.setAuthenticationToken( m_authComponent.setCurrentUser(client.getUserName()));
                
                // Get the users home folder node, if available
                
                getHomeFolderForUser( client);
                
                // Passwords match, grant access
                
                return CifsAuthenticator.AUTH_ALLOW;
            }
            catch (NoSuchAlgorithmException ex)
            {
            }
            
            // Error during password check, do not allow access
            
            return CifsAuthenticator.AUTH_DISALLOW;
        }

        // Check if this is an SMB/CIFS null session logon.
        //
        // The null session will only be allowed to connect to the IPC$ named pipe share.

        if (client.isNullSession() && sess instanceof SMBSrvSession)
            return CifsAuthenticator.AUTH_ALLOW;
        
        // User does not exist, check if guest access is allowed
            
        return allowGuest() ? CifsAuthenticator.AUTH_GUEST : CifsAuthenticator.AUTH_DISALLOW;
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
    	// Start a transaction
    	
    	sess.beginReadTransaction( m_transactionService);
    	
        // Get the authentication token for the session

        NTLMPassthruToken authToken = (NTLMPassthruToken) sess.getAuthenticationToken();
        
        if ( authToken == null)
            return CifsAuthenticator.AUTH_DISALLOW;

        // Get the appropriate hashed password for the algorithm
        
        int authSts = CifsAuthenticator.AUTH_DISALLOW;
        byte[] hashedPassword = null;
        
        if ( alg == NTLM1)
            hashedPassword = client.getPassword();
        else if ( alg == LANMAN)
            hashedPassword = client.getANSIPassword();
        else
        {
            // Invalid/unsupported algorithm specified
            
            return CifsAuthenticator.AUTH_DISALLOW;
        }
        
        // Set the username and hashed password in the authentication token
        
        authToken.setUserAndPassword( client.getUserName(), hashedPassword, alg);
        
        // Authenticate the user
        
        Authentication genAuthToken = null;
        
        try
        {
            // Run the second stage of the passthru authentication
            
            genAuthToken = m_authComponent.authenticate( authToken);
            
            // Check if the user has been logged on as a guest

            if (authToken.isGuestLogon())
            {

                // Check if the local server allows guest access

                if (allowGuest() == true)
                {

                    // Allow the user access as a guest

                    authSts = CifsAuthenticator.AUTH_GUEST;
                }
            }
            else
            {

                // Allow the user full access to the server

                authSts = CifsAuthenticator.AUTH_ALLOW;
            }

            // Set the current user to be authenticated, save the authentication token
            
            client.setAuthenticationToken( genAuthToken);
            
            // Get the users home folder node, if available
            
            getHomeFolderForUser( client);
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Auth token " + genAuthToken);
        }
        catch ( Exception ex)
        {
            logger.error("Error during passthru authentication", ex);
        }
        
        // Clear the authentication token
        
        sess.setAuthenticationToken(null);
        
        // Return the authentication status
        
        return authSts;
    }
}