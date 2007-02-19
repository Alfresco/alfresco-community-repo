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
package org.alfresco.filesys.server.core;

import org.alfresco.config.ConfigElement;
import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;

/**
 * Share Mapper Interface
 * <p>
 * The share mapper interface is used to allocate a share of the specified name and type. It is
 * called by the SMB server to allocate disk and print type shares.
 */
public interface ShareMapper
{

    /**
     * Initialize the share mapper
     * 
     * @param config ServerConfiguration
     * @param params ConfigElement
     * @exception InvalidConfigurationException
     */
    public void initializeMapper(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException;

    /**
     * Return the share list for the specified host. The host name can be used to implement virtual
     * hosts.
     * 
     * @param host
     * @param sess SrvSession
     * @param allShares boolean
     * @return SharedDeviceList
     */
    public SharedDeviceList getShareList(String host, SrvSession sess, boolean allShares);

    /**
     * Find the share of the specified name/type
     * 
     * @param tohost String
     * @param name String
     * @param typ int
     * @param sess SrvSession
     * @param create boolean
     * @return SharedDevice
     * @exception Exception
     */
    public SharedDevice findShare(String tohost, String name, int typ, SrvSession sess, boolean create)
            throws Exception;

    /**
     * Delete any temporary shares created for the specified session
     * 
     * @param sess SrvSession
     */
    public void deleteShares(SrvSession sess);

    /**
     * Close the share mapper, release any resources. Called when the server is shutting down.
     */
    public void closeMapper();
}
