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
package org.alfresco.filesys.server;

import java.util.Vector;

/**
 * Network Server List Class
 */
public class NetworkServerList
{
    // List of network servers

    private Vector<NetworkServer> m_servers;

    /**
     * Class constructor
     */
    public NetworkServerList()
    {
        m_servers = new Vector<NetworkServer>();
    }

    /**
     * Return the number of servers in the list
     * 
     * @return int
     */
    public final int numberOfServers()
    {
        return m_servers.size();
    }

    /**
     * Add a server to the list
     * 
     * @param server NetworkServer
     */
    public final void addServer(NetworkServer server)
    {
        m_servers.add(server);
    }

    /**
     * Return the specified server
     * 
     * @param idx int
     * @return NetworkServer
     */
    public final NetworkServer getServer(int idx)
    {

        // Range check the index

        if (idx < 0 || idx >= m_servers.size())
            return null;
        return m_servers.get(idx);
    }

    /**
     * Find a server in the list by name
     * 
     * @param name String
     * @return NetworkServer
     */
    public final NetworkServer findServer(String name)
    {

        // Search for the required server

        for (int i = 0; i < m_servers.size(); i++)
        {

            // Get the current server from the list

            NetworkServer server = m_servers.get(i);

            if (server.getProtocolName().equals(name))
                return server;
        }

        // Server not found

        return null;
    }

    /**
     * Remove the server at the specified position within the list
     * 
     * @param idx int
     * @return NetworkServer
     */
    public final NetworkServer removeServer(int idx)
    {

        // Range check the index

        if (idx < 0 || idx >= m_servers.size())
            return null;

        // Remove the server from the list

        NetworkServer server = m_servers.get(idx);
        m_servers.remove(idx);
        return server;
    }

    /**
     * Remove the server with the specified protocol name
     * 
     * @param proto String
     * @return NetworkServer
     */
    public final NetworkServer removeServer(String proto)
    {

        // Search for the required server

        for (int i = 0; i < m_servers.size(); i++)
        {

            // Get the current server from the list

            NetworkServer server = m_servers.get(i);

            if (server.getProtocolName().equals(proto))
            {
                m_servers.remove(i);
                return server;
            }
        }

        // Server not found

        return null;
    }

    /**
     * Remove all servers from the list
     */
    public final void removeAll()
    {
        m_servers.removeAllElements();
    }
}
