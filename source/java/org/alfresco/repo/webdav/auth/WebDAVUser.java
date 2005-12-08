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
