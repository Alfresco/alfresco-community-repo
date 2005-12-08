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
