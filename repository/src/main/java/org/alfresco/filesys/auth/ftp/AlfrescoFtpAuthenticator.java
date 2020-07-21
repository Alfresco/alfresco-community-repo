/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.filesys.auth.ftp;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.jlan.ftp.FTPSrvSession;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.PasswordEncryptor;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MD4PasswordEncoder;
import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.alfresco.repo.security.authentication.ntlm.NLTMAuthenticator;

/**
 * Alfresco FTP Authenticator Class
 * 
 * @author gkspencer
 */
public class AlfrescoFtpAuthenticator extends FTPAuthenticatorBase {

  // MD4 hash decoder
  
  protected MD4PasswordEncoder m_md4Encoder = new MD4PasswordEncoderImpl();
  
  // Password encryptor, for MD4 hashing
  
  protected PasswordEncryptor m_encryptor = new PasswordEncryptor();
  
  /**
   * Authenticate the user
   * 
   * @param client ClientInfo
   * @param sess FTPSrvSession
   * @return boolean
   */
  public boolean authenticateUser( ClientInfo client, FTPSrvSession sess)
  {
    // Check that the client is an Alfresco client
    
    if ( client instanceof AlfrescoClientInfo == false)
        return false;
    
   // Check if this is a guest logon
    
    boolean authSts = false;
    UserTransaction tx = null;
    
    try
    {
        if ( client.isGuest())
        {
            //  Get a guest authentication token
            
            doGuestLogon((AlfrescoClientInfo) client, sess);
            
            // Indicate logged on as guest
            
            authSts = true;
            client.setLogonType( ClientInfo.LogonGuest);
            
            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Authenticated user " + client.getUserName() + " sts=" + authSts);
            
            // Return the guest status
            
            return authSts;
        }
        
        // Start a transaction
      
        tx = createTransaction();
        tx.begin();

        // Authenitcate using the authentication component, as we have the plaintext password
        
        getAuthenticationComponent().authenticate( client.getUserName(), client.getPasswordAsString().toCharArray());
        authSts = true;
        
        // Check if the user has been logged on successfully
        
        if ( authSts == true)
        	client.setLogonType( ClientInfo.LogonNormal);
        
        // Check if the logged on user is an administrator
        
        if ( client.getLogonType() == ClientInfo.LogonNormal)
        {
        	// Check for an administrator logon, update the logon type
        	
        	checkForAdminUserName( client);
        }
    }
    catch ( Exception ex)
    {
      if ( logger.isDebugEnabled())
        logger.debug( ex);
        try
        {
            tx.setRollbackOnly();
        }
        catch (SystemException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e);
            }
        }
    }
    finally
    {
        // Commit the transaction
        
        if ( tx != null)
        {
            try
            {
                // Commit or rollback the transaction
                
                if ( tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
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
            catch ( Exception ex)
            {
            }
        }
    }
    
    // DEBUG
    
    if (logger.isDebugEnabled())
    {
        AuthenticationComponent authenticationComponent = getAuthenticationComponent();
        logger
                .debug("Authenticated user "
                        + client.getUserName()
                        + " sts="
                        + authSts
                        + " via "
                        + (authenticationComponent instanceof NLTMAuthenticator
                                && ((NLTMAuthenticator) authenticationComponent).getNTLMMode() == NTLMMode.MD4_PROVIDER ? "MD4"
                                : "Passthru"));
    }
                
    // Return the authentication status
    
    return authSts;
  }

  /**
   * Logon using the guest user account
   * 
   * @param client AlfrescoClientInfo
   * @param sess SrvSession
   */
  protected void doGuestLogon( AlfrescoClientInfo client, SrvSession sess)
  {
      //  Get a guest authentication ticket
      
      getAuthenticationService().authenticateAsGuest();
      String ticket = getAuthenticationService().getCurrentTicket();
      
      client.setAuthenticationTicket( ticket);
      
      // Mark the client as being a guest logon
      
      client.setGuest( true);
  }
}
