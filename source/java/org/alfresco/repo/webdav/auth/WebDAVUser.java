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

package org.alfresco.repo.webdav.auth;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * WebDAV User Class
 * 
 * <p>Contains the details of an authenticated WebDAV user
 * 
 * @author GKSpencer
 */
public class WebDAVUser
{
    // User name
    
    private String m_userName;
    
    //  Authentication ticket
    
    private String m_ticket;
    
    // User home node
    
    private NodeRef m_homeNode;
    
    /**
     * Class constructor
     * 
     * @param user String
     * @param ticket String
     * @param homeNode NodeRef
     */
    public WebDAVUser(String user, String ticket, NodeRef homeNode)
    {
        m_userName = user;
        m_ticket   = ticket;
        m_homeNode = homeNode;
    }
    
    /**
     * Return the user name
     * 
     * @return String
     */
    public final String getUserName()
    {
        return m_userName;
    }
    
    /**
     * Return the ticket
     * 
     * @return String
     */
    public final String getTicket()
    {
        return m_ticket;
    }
    
    /**
     * Check if the user has a home node
     * 
     * @return boolean
     */
    public final boolean hasHomeNode()
    {
        return m_homeNode != null ? true : false;
    }

    /**
     * Return the user home node
     * 
     * @return NodeRef
     */
    public final NodeRef getHomeNode()
    {
        return m_homeNode;
    }
    
    /**
     * Set the home folder node for this user
     * 
     * @param homeNode NodeRef
     */
    protected final void setHomeNode(NodeRef homeNode)
    {
        m_homeNode = homeNode;
    }
    
    /**
     * Return the user details as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[");
        str.append(getUserName());
        str.append(":");
        str.append(getTicket());
        
        if ( hasHomeNode())
        {
            str.append(",Home=");
            str.append(getHomeNode());
        }
        str.append("]");
        
        return str.toString();
    }
}
