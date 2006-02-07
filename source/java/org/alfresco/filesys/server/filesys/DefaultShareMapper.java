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
package org.alfresco.filesys.server.filesys;

import java.util.Enumeration;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.auth.InvalidUserException;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.server.core.ShareMapper;
import org.alfresco.filesys.server.core.SharedDevice;
import org.alfresco.filesys.server.core.SharedDeviceList;

/**
 * Default Share Mapper Class
 * 
 * <p>Maps disk and print share lookup requests to the list of shares defined in the server
 * configuration.
 * 
 * @author GKSpencer
 */
public class DefaultShareMapper implements ShareMapper
{
    // Server configuration

    private ServerConfiguration m_config;

    // Debug enable flag

    private boolean m_debug;

    /**
     * Default constructor
     */
    public DefaultShareMapper()
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
    public SharedDevice findShare(String host, String name, int typ, SrvSession sess, boolean create)
            throws InvalidUserException
    {

        // Check for the special HOME disk share

        SharedDevice share = null;

        // Search the sessions dynamic share list first

        if ( sess.hasDynamicShares()) {
            
            //  Check if the required share exists in the sessions dynamic share list
            
            share = sess.getDynamicShareList().findShare(name, typ, true);
        }

        // If we did not find a share then search the global share list
        
        if ( share == null)
        {
            // Find the required share by name/type. Use a case sensitive search first, if that fails
            // use a case insensitive search.
    
            share = m_config.getShares().findShare(name, typ, false);
    
            if (share == null)
            {
    
                // Try a case insensitive search for the required share
    
                share = m_config.getShares().findShare(name, typ, true);
            }
        }
        
        // Check if the share is available

        if (share != null && share.getContext() != null && share.getContext().isAvailable() == false)
            share = null;

        // Return the shared device, or null if no matching device was found

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
        }
        
        // Clear the dynamic share list
        
        shares.removeAllShares();
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

        // Check if the session is valid, if so then check if the session has any dynamic shares

        // Make a copy of the global share list and add the per session dynamic shares

        SharedDeviceList shrList = new SharedDeviceList(m_config.getShares());

        if ( sess != null && sess.hasDynamicShares()) {
            
            // Add the per session dynamic shares
            
            shrList.addShares(sess.getDynamicShareList());
        }

        // Remove unavailable shares from the list and return the list

        if (allShares == false)
            shrList.removeUnavailableShares();
        return shrList;
    }

    /**
     * Close the share mapper, release any resources.
     */
    public void closeMapper()
    {
    }
}
