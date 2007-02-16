/*
 * Copyright (C) 2005 Alfresco, Inc.
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
