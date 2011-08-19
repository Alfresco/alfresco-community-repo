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

package org.alfresco.filesys.repo;

import java.util.Enumeration;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.InvalidUserException;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.InvalidDeviceInterfaceException;
import org.alfresco.jlan.server.core.ShareMapper;
import org.alfresco.jlan.server.core.ShareType;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.core.SharedDeviceList;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.springframework.extensions.config.ConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.alfresco.AlfrescoClientInfo;
import org.alfresco.filesys.config.ServerConfigurationBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Home Share Mapper Class
 * 
 * <p>Maps disk share lookup requests to the list of shares defined in the server
 * configuration and provides a dynamic home share mapped to the users home node.
 * 
 * @author GKSpencer
 */
public class HomeShareMapper implements ShareMapper, InitializingBean
{
    // Logging
    
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");
    
    //  Home folder share name
    
    public static final String HOME_FOLDER_SHARE = "HOME";
    
    // Server configuration and sections

    private ServerConfiguration m_config;
    private FilesystemsConfigSection m_filesysConfig;

    // Filesystem driver to be used to create home shares
    
    private ContentDiskDriver m_driver;
    
    // Home folder share name
    
    private String m_homeShareName = HOME_FOLDER_SHARE;
    
    // Debug enable flag

    private boolean m_debug;

    /**
     * Default constructor
     */
    public HomeShareMapper()
    {
    }
    
    
    public void setServerConfiguration(ServerConfiguration config)
    {
        this.m_config = config;
    }

    public void setName(String shareName)
    {
        m_homeShareName = shareName;
    }

    public void setDebug(boolean debug)
    {
        this.m_debug = debug;
    }

    /**
     * Initialize the share mapper
     * 
     * @param config ServerConfiguration
     * @param params ConfigElement
     * @exception InvalidConfigurationException
     */
    public void initializeMapper(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
        // Save the server configuration

        setServerConfiguration(config);
        
        // Check if the home share name has been specified
        
        String homeName = params.getAttribute("name");
        if ( homeName != null && homeName.length() > 0)
            setName(homeName);

        // Check if debug is enabled

        if (params != null && params.getChild("debug") != null)
            setDebug(true);
        
        // Complete initialization
        afterPropertiesSet();        
    }

    
    public void afterPropertiesSet()
    {
        // Save the server configuration
        m_filesysConfig = (FilesystemsConfigSection) m_config.getConfigSection(FilesystemsConfigSection.SectionName);
        
        // Search for a filesystem that uses the content driver to use the driver when creating the home shares
        
        SharedDeviceList shares = m_filesysConfig.getShares();
        
        if ( shares != null)
        {
            Enumeration<SharedDevice> shrEnum = shares.enumerateShares();
            
            while ( shrEnum.hasMoreElements() && m_driver == null)
            {
                try
                {
                    SharedDevice curShare = shrEnum.nextElement();
                    if ( curShare.getInterface() instanceof ContentDiskDriver)
                        m_driver = (ContentDiskDriver) curShare.getInterface();
                }
                catch (InvalidDeviceInterfaceException ex)
                {
                }
            }
        }
    }

    /**
     * Check if debug output is enabled
     * 
     * @return boolean
     */
    public final boolean hasDebug()
    {
        return m_debug;
    }

    /**
     * Return the home folder share name
     * 
     * @return String
     */
    public final String getHomeFolderName()
    {
        return m_homeShareName;
    }
    
    /**
     * Return the list of available shares.
     * 
     * @param host String
     * @param sess SrvSession
     * @param allShares boolean
     * @return SharedDeviceList
     */
    public SharedDeviceList getShareList(String host, SrvSession sess, boolean allShares)
    {
        // Check if the user has a home folder, and the session does not currently have any
        // dynamic shares defined
        
        if ( sess != null && sess.hasClientInformation() && sess.hasDynamicShares() == false &&
             sess.getClientInformation() instanceof AlfrescoClientInfo)
        {
            AlfrescoClientInfo alfClient = (AlfrescoClientInfo) sess.getClientInformation();
            if ( alfClient.hasHomeFolder())
            {
                // Create the home folder share
                
                DiskSharedDevice homeShare = createHomeDiskShare( alfClient);
                sess.addDynamicShare(homeShare);
                
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Added " + getHomeFolderName() + " share to list of shares for " + alfClient.getUserName());
            }
        }
        
        // Make a copy of the global share list and add the per session dynamic shares
        
        SharedDeviceList shrList = new SharedDeviceList(m_filesysConfig.getShares());
        
        if ( sess != null && sess.hasDynamicShares()) {
            
            // Add the per session dynamic shares
            
            shrList.addShares(sess.getDynamicShareList());
        }
          
        // Remove unavailable shares from the list and return the list

        if ( allShares == false)
            shrList.removeUnavailableShares();
        return shrList;
    }

    /**
     * Find a share using the name and type for the specified client.
     * 
     * @param host String
     * @param name String
     * @param typ int
     * @param sess SrvSession
     * @param create boolean
     * @return SharedDevice
     * @exception InvalidUserException
     */
    public SharedDevice findShare(String tohost, String name, int typ, SrvSession sess, boolean create)
            throws Exception
    {
        
        //  Check for the special HOME disk share
        
        SharedDevice share = null;
        
        if (( typ == ShareType.DISK || typ == ShareType.UNKNOWN) && name.equalsIgnoreCase(getHomeFolderName()) &&
                    sess.getClientInformation() != null && m_driver != null) {

            //  Get the client details
            
            ClientInfo client = sess.getClientInformation();
                        
            //  DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Map share " + name + ", type=" + ShareType.TypeAsString(typ) + ", client=" + client);
                
            //  Check if the user has a home folder node

            if ( client != null && client instanceof AlfrescoClientInfo) {
              
                //  Access the extended client information
              
                AlfrescoClientInfo alfClient = (AlfrescoClientInfo) client;
                
                if ( alfClient.hasHomeFolder()) {
                
                  //  Check if the share has already been created for the session
  
                  if ( sess.hasDynamicShares()) {
                      
                      //  Check if the required share exists in the sessions dynamic share list
                      
                      share = sess.getDynamicShareList().findShare(name, typ, false);
                      
                      //  DEBUG
                      
                      if ( logger.isDebugEnabled())
                          logger.debug("  Reusing existing dynamic share for " + name);
                  }
  
                  //  Check if we found a share, if not then create a new dynamic share for the home directory
                                  
                  if ( share == null && create == true) {
                      
                      // Create the home share mapped to the users home folder
                      
                      DiskSharedDevice diskShare = createHomeDiskShare( alfClient);
                      
                      // Add the new share to the sessions dynamic share list
  
                      sess.addDynamicShare(diskShare);                    
                      share = diskShare;
                      
                      //  DEBUG
                      
                      if (logger.isDebugEnabled())
                          logger.debug("  Mapped share " + name + " to " + alfClient.getHomeFolder());
                  }
              }
              else
                  throw new InvalidUserException("No home directory");
            }
        }
        else {
        
            //  Find the required share by name/type. Use a case sensitive search first, if that fails use a case
            //  insensitive search.
            
            share = m_filesysConfig.getShares().findShare(name, typ, false);
            
            if ( share == null) {
                
                //  Try a case insensitive search for the required share
                
                share = m_filesysConfig.getShares().findShare(name, typ, true);
            }
        }
        
        //  Check if the share is available
        
        if ( share != null && share.getContext() != null && share.getContext().isAvailable() == false)
            share = null;
        
        //  Return the shared device, or null if no matching device was found
        
        return share;
    }

    /**
     * Delete temporary shares for the specified session
     * 
     * @param sess SrvSession
     */
    public void deleteShares(SrvSession sess)
    {

        //  Check if the session has any dynamic shares
        
        if ( sess.hasDynamicShares() == false)
            return;
            
        //  Delete the dynamic shares
        
        SharedDeviceList shares = sess.getDynamicShareList();
        Enumeration<SharedDevice> enm = shares.enumerateShares();
        
        while ( enm.hasMoreElements()) {

            //  Get the current share from the list
            
            SharedDevice shr = enm.nextElement();
            
            //  Close the shared device
            
            shr.getContext().CloseContext();
            
            //  DEBUG
            
            if (logger.isDebugEnabled())
                logger.debug("Deleted dynamic share " + shr);
        }
        
        // Clear the dynamic share list
        
        shares.removeAllShares();
    }

    /**
     * Close the share mapper, release any resources.
     */
    public void closeMapper()
    {
        // TODO Auto-generated method stub

    }

    /**
     * Create a disk share for the home folder
     * 
     * @param alfClient AlfrescoClientInfo
     * @return DiskSharedDevice
     */
    private final DiskSharedDevice createHomeDiskShare(AlfrescoClientInfo alfClient)
    {
        //  Make sure the client is an Alfresco client
      
        if ( alfClient != null) {
        
          //  Create the disk driver and context
          
          ContentContext diskCtx = new ContentContext( getHomeFolderName(), "", "", alfClient.getHomeFolder());
          
          if(m_config instanceof ServerConfigurationBean)
          {
              ServerConfigurationBean config = (ServerConfigurationBean)m_config;
              
              config.initialiseRuntimeContext(diskCtx);
              
              // Enable file state caching          
              // diskCtx.enableStateCache(serverConfigurationBean, true);
          }
          else
          {
              throw new AlfrescoRuntimeException("configuration error, unknown configuration bean");
          }

  
          //  Create a temporary shared device for the users home directory
          
          return new DiskSharedDevice(getHomeFolderName(), m_driver, diskCtx, SharedDevice.Temporary);
        }
        
        //  Invalid client type
        
        return null;
    }

}
