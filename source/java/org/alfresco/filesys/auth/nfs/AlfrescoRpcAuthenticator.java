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
package org.alfresco.filesys.auth.nfs;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.filesys.AlfrescoConfigSection;
import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.jlan.oncrpc.AuthType;
import org.alfresco.jlan.oncrpc.Rpc;
import org.alfresco.jlan.oncrpc.RpcAuthenticationException;
import org.alfresco.jlan.oncrpc.RpcAuthenticator;
import org.alfresco.jlan.oncrpc.RpcPacket;
import org.alfresco.jlan.oncrpc.nfs.NFS;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.config.ConfigElement;

/**
 * Alfresco RPC Authenticator Class
 * 
 * <p>Provides authentication support for the NFS server.
 * 
 * @author gkspencer
 */
public class AlfrescoRpcAuthenticator implements RpcAuthenticator, InitializingBean {

    // Debug logging

    private static final Log logger = LogFactory.getLog("org.alfresco.nfs.protocol.auth");

    // Authentication types supported by this implementation

    private int[] _authTypes = { AuthType.Unix };

    // UID/GID to username conversions
    
    private Map<Integer, String> m_idMap = Collections.emptyMap();
    
    private List<UserMapping> userMappings;

    private AuthenticationComponent authenticationComponent;
    
    private MutableAuthenticationService authenticationService;

    private TransactionService transactionService;

    public void setUserMappings(List<UserMapping> userMappings)
    {
        this.userMappings = userMappings;
    }

    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }

    public void setAuthenticationService (MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Authenticate an RPC client and create a unique session id key.
     * 
     * @param authType int
     * @param rpc RpcPacket
     * @return Object
     * @throws RpcAuthenticationException
     */
    public Object authenticateRpcClient(int authType, RpcPacket rpc)
            throws RpcAuthenticationException {

        // Create a unique session key depending on the authentication type

        Object sessKey = null;

        if (authType == AuthType.Unix) {

            // Get the gid and uid from the credentials data in the request

            rpc.positionAtCredentialsData();
            rpc.skipBytes(4);
            int nameLen = rpc.unpackInt();
            rpc.skipBytes(nameLen);

            int uid = rpc.unpackInt();
            int gid = rpc.unpackInt();

            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug( "RpcAuth: Type=Unix uid=" + uid + ", gid=" + gid);
            
            // Check that there is a user name mapping for the uid/gid
            
            Integer idKey = new Integer((gid << 16) + uid);
            String userName = m_idMap.get( idKey);
            
            if ( userName == null)
                throw new RpcAuthenticationException( NFS.StsAccess);
            
            // Check if the Unix authentication session table is valid

            sessKey = new Long((((long) rpc.getClientAddress().hashCode()) << 32) + (gid << 16) + uid);
        }
        
        // Check if the session key is valid, if not then the authentication
        // type is unsupported

        if (sessKey == null)
            throw new RpcAuthenticationException(Rpc.AuthBadCred, "Unsupported auth type, " + authType);

        // DEBUG

        if (logger.isDebugEnabled())
            logger.debug("RpcAuth: RPC from " + rpc.getClientDetails()
                    + ", authType=" + AuthType.getTypeAsString(authType)
                    + ", sessKey=" + sessKey);

        // Return the session key

        return sessKey;
    }

    /**
     * Return the authentication types that are supported by this
     * implementation.
     * 
     * @return int[]
     */
    public int[] getRpcAuthenticationTypes() {
        return _authTypes;
    }

    /**
     * Return the client information for the specified RPC request
     * 
     * @param sessKey Object
     * @param rpc RpcPacket
     * @return ClientInfo
     */
    public ClientInfo getRpcClientInformation(Object sessKey, RpcPacket rpc)
    {
        // Create a client information object to hold the client details

        ClientInfo cInfo = null;

        // Get the authentication type

        int authType = rpc.getCredentialsType();

        // Unpack the client details from the RPC request

        if ( authType == AuthType.Unix) {

            // Unpack the credentials data

            rpc.positionAtCredentialsData();
            rpc.skipBytes(4); // stamp id

            String clientAddr = rpc.unpackString();
            int uid = rpc.unpackInt();
            int gid = rpc.unpackInt();

            // Check for an additional groups list

            int grpLen = rpc.unpackInt();
            int[] groups = null;
            
            if (grpLen > 0) {
                groups = new int[grpLen];
                rpc.unpackIntArray(groups);
            }

            // Get the user name mapping for the uid/gid and authenticate
            
            Integer idKey = new Integer((gid << 16) + uid);
            String userName = m_idMap.get( idKey);

            // DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug( "RpcClientInfo: username=" + userName + ", uid=" + uid + ", gid=" + gid);
            
            // Create the client information if there is a valid mapping

            if ( userName != null)
            {
                // Create the client information and fill in relevant fields
                
                cInfo = ClientInfo.getFactory().createInfo( userName, null);
                
                cInfo.setNFSAuthenticationType( authType);
                cInfo.setClientAddress( clientAddr);
                cInfo.setUid( uid);
                cInfo.setGid( gid);

                cInfo.setGroupsList(groups);
            }
            
            // DEBUG
            
            if (logger.isDebugEnabled())
                logger.debug("RpcAuth: Client info, type=" + AuthType.getTypeAsString(authType) + ", name="
                        + clientAddr + ", uid=" + uid + ", gid=" + gid + ", groups=" + grpLen);
        }
        else if ( authType == AuthType.Null)
        {
            // Create the client information
            
            cInfo = ClientInfo.getFactory().createInfo( "", null);
            cInfo.setClientAddress(rpc.getClientAddress().getHostAddress());

            // DEBUG

            if (logger.isDebugEnabled())
                logger.debug("RpcAuth: Client info, type=" + AuthType.getTypeAsString(authType) + ", addr="
                        + rpc.getClientAddress().getHostAddress());
        }

        // Return the client information

        return cInfo;
    }

    /**
     * Set the current authenticated user context for this thread
     * 
     * @param sess SrvSession
     * @param client ClientInfo
     */
    public void setCurrentUser(SrvSession sess, final ClientInfo client)
    {
        try
        {
          // start the transaction
    
          doInTransaction(new RetryingTransactionCallback<Void>()
          {
              @Override
              public Void execute() throws Throwable
              {
                  // Check the account type and setup the authentication context
                  
                  if ( client == null || client.isNullSession() || client instanceof AlfrescoClientInfo == false)
                  {
                      // Clear the authentication, null user should not be allowed to do any service calls
                      
                      getAuthenticationComponent().clearCurrentSecurityContext();
                      
                      // DEBUG
                      
                      if ( logger.isDebugEnabled())
                          logger.debug("Clear security context, client=" + client);
                  }
                  else if ( client.isGuest() == false)
                  {
                    // Access the Alfresco client
                    
                    AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
                    
                      // Check if the authentication token has been set for the client
                      
                      if ( !alfClient.hasAuthenticationTicket() )
                      {
                          // ALF-9793: It's possible that the user we're about to accept doesn't even exist, yet we
                          // are using alfresco authentication. In such cases we must automatically create
                          // authentication (using a randomized password) in order to successfully authenticate.
          
                          if (!authenticationService.authenticationExists( client.getUserName()) && authenticationService.isAuthenticationCreationAllowed())
                          {
                              authenticationService.createAuthentication( client.getUserName(), GUID.generate().toCharArray());
                          }
                          // Set the current user and retrieve the authentication token
                          
                          getAuthenticationComponent().setCurrentUser( client.getUserName());
                          alfClient.setAuthenticationTicket(getAuthenticationService().getCurrentTicket());
          
                          // DEBUG
                          
                          if ( logger.isDebugEnabled())
                              logger.debug("Set user name=" + client.getUserName() + ", ticket=" + alfClient.getAuthenticationTicket());
                      }
                      else
                      {
                          // Set the authentication context for the request
                          
                          getAuthenticationService().validate(alfClient.getAuthenticationTicket());
                          
                          // DEBUG
                          
                          if ( logger.isDebugEnabled())
                              logger.debug("Set user using auth ticket, ticket=" + alfClient.getAuthenticationTicket());
                      }
                  }
                  else
                  {
                      // Enable guest access for the request
                      
                    getAuthenticationComponent().setGuestUserAsCurrentUser();
                      
                    // DEBUG
                      
                    if ( logger.isDebugEnabled())
                      logger.debug("Set guest user");
                  }
                  return null;
              }
          });

      }
      catch ( Exception ex)
      {
        if ( logger.isErrorEnabled())
          logger.error( "Error in RPC authenticator setting current user", ex);
      }
    }
    
    /**
     * Initialize the RPC authenticator
     * 
     * @param config ServerConfiguration
     * @param params NameValueList
     * @throws InvalidConfigurationException
     */
    public void initialize(ServerConfiguration config, ConfigElement params)
        throws InvalidConfigurationException {
        
        // Get the alfresco configuration section, required to get hold of various services/components        
        AlfrescoConfigSection alfrescoConfig = (AlfrescoConfigSection) config.getConfigSection( AlfrescoConfigSection.SectionName);
        
        // Copy over relevant bean properties for backward compatibility
        setAuthenticationComponent(alfrescoConfig.getAuthenticationComponent());
        setAuthenticationService((MutableAuthenticationService) alfrescoConfig.getAuthenticationService());
        setTransactionService(alfrescoConfig.getTransactionService());

        // Check for the user mappings
        
        ConfigElement userMappings = params.getChild("userMappings");
        if ( userMappings != null)
        {
            // Allocate the id mappings table
            List <UserMapping> mappings = new LinkedList<UserMapping>();
            
            // Get the user map elements
            
            List<ConfigElement> userMaps = userMappings.getChildren();
            
            // Process the user list
            
            for ( ConfigElement userElem : userMaps)
            {
                // Validate the element type
                
                if ( userElem.getName().equalsIgnoreCase( "user"))
                {
                    // Get the user name, user id and group id
                    
                    String userName = userElem.getAttribute("name");
                    String uidStr   = userElem.getAttribute("uid");
                    String gidStr   = userElem.getAttribute("gid");
                    
                    if ( userName == null || userName.length() == 0)
                        throw new InvalidConfigurationException("Empty user name, or name not specified");
                    
                    if ( uidStr == null || uidStr.length() == 0)
                        throw new InvalidConfigurationException("Invalid uid, or uid not specified, for user " + userName);
                    
                    if ( gidStr == null || gidStr.length() == 0)
                        throw new InvalidConfigurationException("Invalid gid, or gid not specified, for user " + userName);
                    
                    // Parse the uid/gid
                    
                    int uid = -1;
                    int gid = -1;
                    
                    try
                    {
                        uid = Integer.parseInt( uidStr);
                    }
                    catch ( NumberFormatException ex)
                    {
                        throw new InvalidConfigurationException("Invalid uid value, " + uidStr + " for user " + userName);
                    }
                    
                    try
                    {
                        gid = Integer.parseInt( gidStr);
                    }
                    catch ( NumberFormatException ex)
                    {
                        throw new InvalidConfigurationException("Invalid gid value, " + gidStr + " for user " + userName);
                    }
                    
                    mappings.add(new UserMapping(userName,uid ,gid ));
                }                    
            }
            setUserMappings(mappings);
        }
        
        afterPropertiesSet();       
    }

    /**
     * Initialize the RPC authenticator
     * @throws InvalidConfigurationException
     */
    public void afterPropertiesSet() throws InvalidConfigurationException
    {
        // Check for the user mappings
        
        if ( this.userMappings != null)
        {
            // Allocate the id mappings table
            
            m_idMap = new HashMap<Integer, String>(this.userMappings.size() * 2);
                        
            // Process the user list
            
            for ( UserMapping userElem : this.userMappings)
            {
                // Get the user name, user id and group id
                
                String userName = userElem.getName();
                
                if ( userName == null || userName.length() == 0)
                    throw new InvalidConfigurationException("Empty user name, or name not specified");
                
                // Check if the mapping already exists
                
                Integer idKey = new Integer(( userElem.getGid() << 16) + userElem.getUid());
                if ( m_idMap.containsKey( idKey) == false)
                {
                    // Add the username uid/gid mapping
                    
                    m_idMap.put( idKey, userName);
                    
                    // DEBUG
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Added RPC user mapping for user " + userName + " uid=" + userElem.getUid() + ", gid=" + userElem.getGid());
                }
                else if ( logger.isDebugEnabled())
                {
                    // DEBUG
                    
                    logger.debug("Ignored duplicate mapping for uid=" + userElem.getUid() + ", gid=" + userElem.getGid());
                }
            }
        }
    }
    
    /**
     * Does work in a transaction. This will be a writeable transaction unless the system is in read-only mode.
     * 
     * @param callback
     *            a callback that does the work
     * @return the result, or <code>null</code> if not applicable
     */
    protected <T> T doInTransaction(RetryingTransactionHelper.RetryingTransactionCallback<T> callback)
    {
        // Get the transaction service
        
        TransactionService txService = getTransactionService(); 
        
        // DEBUG
        
        if ( logger.isDebugEnabled())
            logger.debug("Using " + (txService.isReadOnly() ? "ReadOnly" : "Write") + " transaction");
        
        return txService.getRetryingTransactionHelper().doInTransaction(callback, txService.isReadOnly());
    }
    
    protected AuthenticationComponent getAuthenticationComponent()
    {
        return this.authenticationComponent;
    }
    
    protected MutableAuthenticationService getAuthenticationService()
    {
        return this.authenticationService;
    }
    
    protected TransactionService getTransactionService()
    {
        return this.transactionService;
    }
}
