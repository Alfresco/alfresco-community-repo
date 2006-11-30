/*
 * Copyright (C) 2005 Alfresco, Inc.
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

package org.alfresco.filesys.smb.server.repo;

import java.util.Enumeration;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.auth.InvalidUserException;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.ShareMapper;
import org.alfresco.filesys.server.core.ShareType;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;
import org.alfresco.filesys.server.filesys.DiskSharedDevice;
import org.alfresco.filesys.smb.server.repo.ContentContext;
import org.alfresco.filesys.smb.server.repo.ContentDiskDriver;
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

    private ServerConfiguration m_config;

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

        m_config = config;
        
        // Check if the home share name has been specified
        
        String homeName = params.getAttribute("name");
        if ( homeName != null && homeName.length() > 0)
            m_homeShareName = homeName;

        // Check if debug is enabled

        if (params != null && params.getChild("debug") != null)
            m_debug = true;
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
        
        if ( sess != null && sess.hasClientInformation() && sess.hasDynamicShares() == false)
        {
            ClientInfo client = sess.getClientInformation();
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
        
        SharedDeviceList shrList = new SharedDeviceList(m_config.getShares());
        
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
                    sess.getClientInformation() != null) {

            //  Get the client details
            
            ClientInfo client = sess.getClientInformation();
                        
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
            
            share = m_config.getShares().findShare(name, typ, false);
            
            if ( share == null) {
                
                //  Try a case insensitive search for the required share
                
                share = m_config.getShares().findShare(name, typ, true);
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

    /**
     * Create a disk share for the home folder
     * 
     * @param client ClientInfo
     * @return DiskSharedDevice
     */
    private final DiskSharedDevice createHomeDiskShare(ClientInfo client)
    {
        //  Create the disk driver and context
        
        ContentDiskDriver diskDrv = ( ContentDiskDriver) m_config.getDiskInterface();
        ContentContext diskCtx = new ContentContext( getHomeFolderName(), "", "", client.getHomeFolder());
        
        diskCtx.enableStateTable( true, diskDrv.getStateReaper());

        //  Create a temporary shared device for the users home directory
        
        return new DiskSharedDevice(getHomeFolderName(), diskDrv, diskCtx, SharedDevice.Temporary);
    }
}
