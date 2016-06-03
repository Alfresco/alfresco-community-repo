
package org.alfresco.repo.workflow;

import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class WorkflowAuthorityManager
{
    private final AuthorityDAO authorityDAO;
    
    public WorkflowAuthorityManager(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }

    /**
     * Convert Alfresco authority to user id
     * 
     * @param authority NodeRef
     * @return actor id
     */
    public String mapAuthorityToName(NodeRef authority)
    {
        return authorityDAO.getAuthorityName(authority);
    }
    
    /**
     * Convert authority name to an Alfresco Authority
     * 
     * @param name
     *            the authority name to convert
     * @return the Alfresco authorities
     */
    public NodeRef mapNameToAuthority(String name)
    {
        NodeRef authority = null;
        if (name != null)
        {
            authority = authorityDAO.getAuthorityNodeRefOrNull(name);
        }
        return authority;
    }
    
    public boolean isUser(String authorityName)
    {
        AuthorityType type = AuthorityType.getAuthorityType(authorityName);
        return type == AuthorityType.USER ||
            type == AuthorityType.ADMIN ||
            type == AuthorityType.GUEST;
    }

    public String getAuthorityName(NodeRef authorityRef)
    {
        return authorityDAO.getAuthorityName(authorityRef);
    }
}
