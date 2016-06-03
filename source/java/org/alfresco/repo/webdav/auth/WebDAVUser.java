
package org.alfresco.repo.webdav.auth;

import org.alfresco.repo.SessionUser;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * WebDAV User Class
 * 
 * <p>Contains the details of an authenticated WebDAV user
 * 
 * @author GKSpencer
 */
public class WebDAVUser implements SessionUser
{
    private static final long serialVersionUID = -6948146071131901345L;

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
