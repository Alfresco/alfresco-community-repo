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
package org.alfresco.filesys.server.filesys;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Tree Connection Hash Class
 * <p>
 * Hashtable of TreeConnections for the available disk shared devices. TreeConnections are indexed
 * using the hash of the share name to allow mounts to be persistent across server restarts.
 */
public class TreeConnectionHash
{

    // Share name hash to tree connection

    private Hashtable<Integer, TreeConnection> m_connections;

    /**
     * Class constructor
     */
    public TreeConnectionHash()
    {
        m_connections = new Hashtable<Integer, TreeConnection>();
    }

    /**
     * Return the number of tree connections in the hash table
     * 
     * @return int
     */
    public final int numberOfEntries()
    {
        return m_connections.size();
    }

    /**
     * Add a connection to the list of available connections
     * 
     * @param tree TreeConnection
     */
    public final void addConnection(TreeConnection tree)
    {
        m_connections.put(tree.getSharedDevice().getName().hashCode(), tree);
    }

    /**
     * Delete a connection from the list
     * 
     * @param shareName String
     * @return TreeConnection
     */
    public final TreeConnection deleteConnection(String shareName)
    {
        return (TreeConnection) m_connections.get(shareName.hashCode());
    }

    /**
     * Find a connection for the specified share name
     * 
     * @param shareName String
     * @return TreeConnection
     */
    public final TreeConnection findConnection(String shareName)
    {

        // Get the tree connection for the associated share name

        TreeConnection tree = m_connections.get(shareName.hashCode());

        // Return the tree connection

        return tree;
    }

    /**
     * Find a connection for the specified share name hash code
     * 
     * @param hashCode int
     * @return TreeConnection
     */
    public final TreeConnection findConnection(int hashCode)
    {

        // Get the tree connection for the associated share name

        TreeConnection tree = m_connections.get(hashCode);

        // Return the tree connection

        return tree;
    }

    /**
     * Enumerate the connections
     * 
     * @return Enumeration<TreeConnection>
     */
    public final Enumeration<TreeConnection> enumerateConnections()
    {
        return m_connections.elements();
    }
}
