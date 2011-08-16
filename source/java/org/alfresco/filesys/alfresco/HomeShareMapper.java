/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
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

package org.alfresco.filesys.alfresco;

import java.util.Enumeration;

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.filesys.AlfrescoConfigSection;
import org.alfresco.filesys.config.ServerConfigurationBean;
import org.alfresco.filesys.repo.ContentContext;
import org.alfresco.filesys.repo.ContentDiskDriver;
import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.auth.InvalidUserException;
import org.alfresco.jlan.server.config.InvalidConfigurationException;
import org.alfresco.jlan.server.config.ServerConfiguration;
import org.alfresco.jlan.server.config.ServerConfigurationAccessor;
import org.alfresco.jlan.server.core.ShareMapper;
import org.alfresco.jlan.server.core.ShareType;
import org.alfresco.jlan.server.core.SharedDevice;
import org.alfresco.jlan.server.core.SharedDeviceList;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Home Share Mapper Class
 * 
 * <p>Maps disk share lookup requests to the list of shares defined in the server
 * configuration and provides a dynamic home share mapped to the users home node.
 * 
 * @author GKSpencer
 */
public class HomeShareMapper implements ShareMapper
{
    // Logging
    
    private static final Log logger = LogFactory.getLog("org.alfresco.smb.protocol");
    
    //  Home folder share name
    
    public static final String HOME_FOLDER_SHARE = "HOME";
    
    // Server configuration

    private ServerConfigurationAccessor m_config;
    
    private ServerConfigurationBean serverConfigurationBean;
    
    private DiskInterface m_repoDiskInterface;
    
    // Home folder share name
    
    private String m_homeShareName = HOME_FOLDER_SHARE;
    
    // Debug enable flag

    private boolean m_debug;

    
    public void setConfig(ServerConfiguration config)
    {
        m_config = config;
    }

    public void setRepoDiskInterface(DiskInterface diskInterface)
    {
        m_repoDiskInterface = diskInterface;
    }

    public void setHomeShareName(String shareName)
    {
        m_homeShareName = shareName;
    }

    public void setDebug(boolean m_debug)
    {
        this.m_debug = m_debug;
    }

    /**
     * Default constructor
     */
    public HomeShareMapper()
    {
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

        setConfig(config);
        
        setRepoDiskInterface(((AlfrescoConfigSection) m_config.getConfigSection( AlfrescoConfigSection.SectionName)).getRepoDiskInterface());
        
        // Check if the home share name has been specified
        
        String homeName = params.getAttribute("name");
        if ( homeName != null && homeName.length() > 0)
            setHomeShareName(homeName);

        // Check if debug is enabled

        if (params != null && params.getChild("debug") != null)
            setDebug(true);
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
            AlfrescoClientInfo client = (AlfrescoClientInfo) sess.getClientInformation();
            if ( client.hasHomeFolder())
            {
                // Create the home folder share
                
                DiskSharedDevice homeShare = createHomeDiskShare(client);
                sess.addDynamicShare(homeShare);
                
                // Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Added " + getHomeFolderName() + " share to list of shares for " + client.getUserName());
            }
        }
        
        // Make a copy of the global share list and add the per session dynamic shares
        
        SharedDeviceList shrList = new SharedDeviceList(getFilesystemsConfigSection().getShares());
        
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
                    sess.getClientInformation() != null && sess.getClientInformation() instanceof AlfrescoClientInfo) {

            //  Get the client details
            
            AlfrescoClientInfo client = (AlfrescoClientInfo) sess.getClientInformation();
                        
            //  DEBUG
            
            if ( logger.isDebugEnabled())
                logger.debug("Map share " + name + ", type=" + ShareType.TypeAsString(typ) + ", client=" + client);
                
            //  Check if the user has a home folder node

            if ( client != null && client.hasHomeFolder()) {
                
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
                    
                    DiskSharedDevice diskShare = createHomeDiskShare(client);
                    
                    // Add the new share to the sessions dynamic share list

                    sess.addDynamicShare(diskShare);                    
                    share = diskShare;
                    
                    //  DEBUG
                    
                    if (logger.isDebugEnabled())
                        logger.debug("  Mapped share " + name + " to " + client.getHomeFolder());
                }
            }
            else
                throw new InvalidUserException("No home directory");
        }
        else {
        
            //  Find the required share by name/type. Use a case sensitive search first, if that fails use a case
            //  insensitive search.
            FilesystemsConfigSection filesystemsConfigSection = getFilesystemsConfigSection();
            share = filesystemsConfigSection.getShares().findShare(name, typ, false);
            
            if ( share == null) {
                
                //  Try a case insensitive search for the required share
                
                share = filesystemsConfigSection.getShares().findShare(name, typ, true);
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
    	// Nothing to do
    }

    /**
     * Create a disk share for the home folder
     * 
     * @param client AlfrescoClientInfo
     * @return DiskSharedDevice
     */
    private final DiskSharedDevice createHomeDiskShare(AlfrescoClientInfo client)
    {
        //  Create the disk driver and context
        
        ExtendedDiskInterface diskDrv = (ExtendedDiskInterface) getRepoDiskInterface();
        ContentContext diskCtx = new ContentContext( getHomeFolderName(), "", "", client.getHomeFolder());
        
        if(diskDrv instanceof ExtendedDiskInterface)
        {
            diskCtx.enableStateCache(serverConfigurationBean, true);
        }
      

        //  Create a temporary shared device for the users home directory
        
        return new DiskSharedDevice(getHomeFolderName(), diskDrv, diskCtx, SharedDevice.Temporary);
    }
    
    protected DiskInterface getRepoDiskInterface()
    {
        return m_repoDiskInterface;
    }
    
    protected FilesystemsConfigSection getFilesystemsConfigSection()
    {
        return (FilesystemsConfigSection)m_config.getConfigSection(FilesystemsConfigSection.SectionName);
    }

    public void setServerConfigurationBean(ServerConfigurationBean serverConfigurationBean)
    {
        this.serverConfigurationBean = serverConfigurationBean;
    }

    public ServerConfigurationBean getServerConfigurationBean()
    {
        return serverConfigurationBean;
    }
}
