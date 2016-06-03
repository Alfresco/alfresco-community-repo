package org.alfresco.repo.admin;

import java.util.List;

/**
 * An interface for retrieving configurable system parameters.
 * 
 * @author dward
 */
public interface SysAdminParams
{

    /**
     * Do we allow write operations by non-system users on the repository?.
     * 
     * @return <code>true</code> if we allow write operations by non-system users on the repository
     */
    public boolean getAllowWrite();

    /**
     * Gets the list of users who are allowed to log in.
     * 
     * @return the allowed user list or <code>null</code> if all users are allowed to log in
     */
    public List<String> getAllowedUserList();

    /**
     * Gets the maximum number of users who are allowed to log in.
     * 
     * @return the the maximum number of users who are allowed to log in
     */
    public int getMaxUsers();

    /**
     * Gets Alfresco context.
     * 
     * @return Alfresco context
     */
    public String getAlfrescoContext();

    /**
     * Gets Alfresco host.
     * 
     * @return Alfresco host
     */
    public String getAlfrescoHost();

    /**
     * Gets Alfresco port.
     * 
     * @return Alfresco port
     */
    public int getAlfrescoPort();

    /**
     * Gets Alfresco protocole.
     * 
     * @return Alfresco protocole
     */
    public String getAlfrescoProtocol();

    /**
     * Gets Share context.
     * 
     * @return Share context
     */
    public String getShareContext();

    /**
     * Gets Share host.
     * 
     * @return Share host
     */
    public String getShareHost();

    /**
     * Gets Share port.
     * 
     * @return Share port
     */
    public int getSharePort();

    /**
     * Gets Share protocol.
     * 
     * @return Share protocol
     */
    public String getShareProtocol();
    
    /**
     * Gets the group name used for public site visibility.
     * Only members of this group will have SiteConsumer access to 'public' share sites.
     * 
     * @return the name of the public site group.
     * @since 3.4
     */
    public String getSitePublicGroup();
    
    /**
     * Expands the special ${localname} token within a host name using the resolved DNS name for the local host.
     * 
     * @param hostName
     *            the host name
     * @return the string
     */
    public String subsituteHost(String hostName);
}