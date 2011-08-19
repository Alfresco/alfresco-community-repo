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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.avm;

import java.util.Enumeration;

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.core.InvalidDeviceInterfaceException;
import org.alfresco.jlan.server.core.ShareMapper;
import org.alfresco.jlan.server.core.ShareType;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.core.SharedDeviceList;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.alfresco.jlan.util.StringList;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * AVM Filesystem Share Mapper Class
 * 
 * <p>Provides access to store versions using the share name '<storename>_<version>'.
 *
 * @author gkspencer
 */
public class AVMShareMapper implements ShareMapper, InitializingBean {

	// Logging
    
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");
    
    // Regular expression to test for AVM versioned share name

    private static final String AVM_SHAREPATTERN = "[a-zA-Z0-9-]*_[0-9]+";
    
    // Server configuration

    private ServerConfiguration m_config;
    private FilesystemsConfigSection m_filesysConfig;

    // List of available AVM shares
    
    private StringList m_avmShareNames;
    
    // Filesystem driver to be used to create home shares
    
    private AVMDiskDriver m_driver;

    // Debug enable flag

    private boolean m_debug;

    /**
     * Default constructor
     */
    public AVMShareMapper()
    {
    }
    
    public void setServerConfiguration(ServerConfiguration config)
    {
        this.m_config = config;
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

        // Check if debug is enabled

        if (params != null && params.getChild("debug") != null)
            setDebug(true);
        
        // Complete the initialization
        afterPropertiesSet();
    }        

    public void afterPropertiesSet()
    {
        // Save the server configuration

        m_filesysConfig = (FilesystemsConfigSection) m_config.getConfigSection(FilesystemsConfigSection.SectionName);
        
 
        // Build the list of available AVM share names
        
        m_avmShareNames = new StringList();
        
        SharedDeviceList shrList = m_filesysConfig.getShares();
        Enumeration<SharedDevice> shrEnum = shrList.enumerateShares();
        
        while ( shrEnum.hasMoreElements())
        {
            // Get the current shared device and check if it is an AVM filesystem device
            
            SharedDevice curShare = shrEnum.nextElement();
            
            try
            {
                if ( curShare.getInterface() instanceof AVMDiskDriver)
                {
                    // Add the shared filesystem name to the list of AVM shares
                  
                      m_avmShareNames.addString( curShare.getName());
                      
                      // Set the AVM filesystem driver to be used when creating dynamic shares
                      
                      if ( m_driver == null)
                          m_driver = (AVMDiskDriver) curShare.getInterface();
                }
            }
            catch ( InvalidDeviceInterfaceException ex)
            {
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
     * Return the list of available shares.
     * 
     * @param host String
     * @param sess SrvSession
     * @param allShares boolean
     * @return SharedDeviceList
     */
    public SharedDeviceList getShareList(String host, SrvSession sess, boolean allShares)
    {
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
        //  Find the required share by name/type. Use a case sensitive search first, if that fails use a case
        //  insensitive search.
        
        SharedDevice share = m_filesysConfig.getShares().findShare(name, typ, false);
        
        if ( share == null)
        {
            
            //  Try a case insensitive search for the required share
            
            share = m_filesysConfig.getShares().findShare(name, typ, true);
        }
        
        //  If the share was not found then check if the share is in the AVM versioned share format - '<storename>_<version>'

        if ( share == null && ( typ == ShareType.DISK || typ == ShareType.UNKNOWN))
        {
            //  Check if the share has already been created for the session

            if ( sess.hasDynamicShares())
            {
                
                //  Check if the required share exists in the sessions dynamic share list
                
                share = sess.getDynamicShareList().findShare(name, typ, false);
                
                //  DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("  Reusing existing dynamic share for " + name);
            }

        	// Check if the share name matches the AVM versioned share name pattern
        	
        	if ( share == null && create == true && name.matches( AVM_SHAREPATTERN))
        	{
	            //  DEBUG
	            
	            if ( logger.isDebugEnabled())
	                logger.debug("Map dynamic share " + name + ", type=" + ShareType.TypeAsString(typ));
                
	            //  Split the store name and version id from the share name
	            
	            int pos = name.indexOf( '_');

	            String storePath = name.substring(0, pos) + ":/";
	            int storeVersion = -1;
	            
	            try
	            {
	            	String storeVer = name.substring( pos + 1);
	            	storeVersion = Integer.parseInt( storeVer);
	            	
	            	if ( storeVersion < 0)
	            		storeVersion = -1;
	            }
	            catch ( NumberFormatException ex)
	            {
	            	logger.error( "Invalid store version id, name=" + name);
	            }
	            
                //  Create the disk driver and context

	            if ( storePath.length() > 0 && storeVersion != -1)
	            {
	            	  // Validate the store name and version
	            	
	                AVMService avmService = m_driver.getAvmService();

	                m_driver.beginReadTransaction( sess);
	                
	                try
	                {
	                	// Validate the store name/version

	                	avmService.lookup( storeVersion, storePath);
	                
	                	// Create a dynamic share mapped to the AVM store/version
	                	
		                AVMContext avmCtx = new AVMContext( name, storePath, storeVersion);
		                
		                if(m_config instanceof ServerConfigurationBean)
		                {
		                    ServerConfigurationBean config = (ServerConfigurationBean)m_config;
		                    
		                    config.initialiseRuntimeContext(avmCtx);
		                    
		                    // Enable file state caching          
		                    // diskCtx.enableStateCache(serverConfigurationBean, true);
		                }
		                else
		                {
		                    throw new AlfrescoRuntimeException("configuration error, unknown configuration bean");
		                }
		
		                //  Create a dynamic shared device for the store version
		                
		                DiskSharedDevice diskShare = new DiskSharedDevice( name, m_driver, avmCtx, SharedDevice.Temporary);
		                
		                // Add the new share to the sessions dynamic share list
		
		                sess.addDynamicShare(diskShare);                    
		                share = diskShare;
		                    
		                //  DEBUG
		                    
		                if (logger.isDebugEnabled())
		                    logger.debug("  Mapped share " + name + " - " + diskShare);
	                }
	                catch ( AVMNotFoundException ex)
	                {
	                	// DEBUG
	                	
	                	if ( logger.isDebugEnabled())
	                		logger.debug( "Failed to map share to " + name + ", not such store/version");
	                }
	                catch ( AVMWrongTypeException ex)
	                {
	                	// DEBUG
	                	
	                	if ( logger.isDebugEnabled())
	                		logger.debug( "Failed to map share to " + name + ", wrong type");
	                }
	            }
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
            
            SharedDevice shr = (SharedDevice) enm.nextElement();
            
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
}
