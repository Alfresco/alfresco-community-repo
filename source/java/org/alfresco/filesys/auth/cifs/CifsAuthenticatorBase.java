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
package org.alfresco.filesys.auth.cifs;

import javax.transaction.UserTransaction;

import net.sf.acegisecurity.Authentication;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.AlfrescoConfigSection;
import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.CifsAuthenticator;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.SrvDiskInfo;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MD4PasswordEncoder;
import org.alfresco.repo.security.authentication.MD4PasswordEncoderImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * CIFS Authenticator Base Class
 * 
 * <p>
 * Base class for Alfresco CIFS authenticator implementations.
 * 
 * @author gkspencer
 */
public abstract class CifsAuthenticatorBase extends CifsAuthenticator
{
    // Logging
    
    protected static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol.auth");

    // MD4 hash decoder
    
    protected MD4PasswordEncoder m_md4Encoder = new MD4PasswordEncoderImpl();
    
    // Alfresco configuration
    
    protected AlfrescoConfigSection m_alfrescoConfig;
    
    /**
     * Initialize the authenticator
     * 
     * @param config ServerConfiguration
     * @param params ConfigElement
     * @exception InvalidConfigurationException
     */
    public void initialize(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
        super.initialize(config, params);

        // Get the alfresco configuration section, required to get hold of various services/components
        
        m_alfrescoConfig = (AlfrescoConfigSection) config.getConfigSection( AlfrescoConfigSection.SectionName);
        
        // Check that the required authentication classes are available

        if ( m_alfrescoConfig == null || getAuthenticationComponent() == null)
            throw new InvalidConfigurationException("Authentication component not available");

        // Set the guest user name
        
        setGuestUserName( getAuthenticationComponent().getGuestUserName());
        
        // Check that the authentication component is the required type for this authenticator
        
        if ( validateAuthenticationMode() == false)
            throw new InvalidConfigurationException("Required authentication mode not available");
    }

    /**
     * Validate that the authentication component supports the required mode
     * 
     * @return boolean
     */
    protected boolean validateAuthenticationMode()
    {
        return true;
    }
    
    /**
     * Logon using the guest user account
     * 
     * @param client ClientInfo
     * @param sess SrvSession
     */
    protected void doGuestLogon( ClientInfo client, SrvSession sess)
    {
        //  Check that the client is an Alfresco client
      
        if ( client instanceof AlfrescoClientInfo == false)
            return;
        
        AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
        
        //  Get a guest authentication token
        
        getAuthenticationService().authenticateAsGuest();
        Authentication authToken = getAuthenticationComponent().getCurrentAuthentication();
        
        alfClient.setAuthenticationToken( authToken);
        
        // Set the home folder for the guest user
        
        client.setUserName( getGuestUserName());
        getHomeFolderForUser( client);
        
        // Mark the client as being a guest logon
        
        client.setGuest( true);

        // Create a dynamic share for the guest user, create the disk driver and context
        
        DiskInterface diskDrv = m_alfrescoConfig.getRepoDiskInterface();
        DiskDeviceContext diskCtx = new ContentContext(client.getUserName(), "", "", alfClient.getHomeFolder());

        //  Default the filesystem to look like an 80Gb sized disk with 90% free space

        diskCtx.setDiskInformation(new SrvDiskInfo(2560, 64, 512, 2304));
        
        //  Create a temporary shared device for the users home directory
        
        sess.addDynamicShare( new DiskSharedDevice( client.getUserName(), diskDrv, diskCtx, SharedDevice.Temporary));
    }
    
    /**
     * Get the home folder for the user
     * 
     * @param client ClientInfo
     */
    protected final void getHomeFolderForUser(ClientInfo client)
    {
        // Check if the client is an Alfresco client
      
        if ( client instanceof AlfrescoClientInfo == false)
          return;
        
        AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
        
        // Get the home folder for the user
        
        UserTransaction tx = getTransactionService().getUserTransaction();
        NodeRef homeSpaceRef = null;
        
        try
        {
            tx.begin();
            homeSpaceRef = (NodeRef) getNodeService().getProperty(getPersonService().getPerson(client.getUserName()), ContentModel.PROP_HOMEFOLDER);
            alfClient.setHomeFolder( homeSpaceRef);
            tx.commit();
        }
        catch (Throwable ex)
        {
            try
            {
                tx.rollback();
            }
            catch (Throwable ex2)
            {
                logger.error("Failed to rollback transaction", ex2);
            }
            
             //          Re-throw the exception
            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException) ex;
            }
            else
            {
                throw new RuntimeException("Error during execution of transaction.", ex);
            }
        }
    }
    
    /**
     * Map the case insensitive logon name to the internal person object user name
     * 
     * @param userName String
     * @return String
     */
    protected final String mapUserNameToPerson(String userName)
    {
        // Get the home folder for the user
        
        UserTransaction tx = m_alfrescoConfig.getTransactionService().getUserTransaction();
        String personName = null;
        
        try
        {
            tx.begin();
            personName = m_alfrescoConfig.getPersonService().getUserIdentifier( userName);
            tx.commit();
        }
        catch (Throwable ex)
        {
            try
            {
                tx.rollback();
            }
            catch (Throwable ex2)
            {
                logger.error("Failed to rollback transaction", ex2);
            }
            
            // Re-throw the exception
            
            if (ex instanceof RuntimeException)
            {
                throw (RuntimeException) ex;
            }
            else
            {
                throw new RuntimeException("Error during execution of transaction.", ex);
            }
        }
        
        // Return the person name
        
        return personName;
    }
    
    /**
     * Set the current authenticated user context for this thread
     * 
     * @param client ClientInfo
     */
    public void setCurrentUser(ClientInfo client) {

        // Check the account type and setup the authentication context

        if (client.isNullSession())
        {
            // Clear the authentication, null user should not be allowed to do any service calls

            getAuthenticationComponent().clearCurrentSecurityContext();
        }
        else if (client.isGuest() == false && client instanceof AlfrescoClientInfo)
        {
            // Set the authentication context for the request

            AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
            getAuthenticationComponent().setCurrentAuthentication(alfClient.getAuthenticationToken());
        }
        else
        {
            // Enable guest access for the request

            getAuthenticationComponent().setGuestUserAsCurrentUser();
        }
    }
    
    /**
     * Return the authentication component
     * 
     * @return AuthenticationComponent
     */
    protected final AuthenticationComponent getAuthenticationComponent()
    {
        return m_alfrescoConfig.getAuthenticationComponent();
    }
    
    /**
     * Return the authentication service
     * 
     * @return AuthenticationService
     */
    protected final AuthenticationService getAuthenticationService()
    {
        return m_alfrescoConfig.getAuthenticationService();
    }
    
    /**
     * Return the node service
     * 
     * @return NodeService
     */
    protected final NodeService getNodeService()
    {
        return m_alfrescoConfig.getNodeService();
    }
    
    /**
     * Return the person service
     * 
     * @return PersonService
     */
    protected final PersonService getPersonService()
    {
        return m_alfrescoConfig.getPersonService();
    }

    /**
     * Return the transaction service
     * 
     * @return TransactionService
     */
    protected final TransactionService getTransactionService()
    {
        return m_alfrescoConfig.getTransactionService();
    }
}