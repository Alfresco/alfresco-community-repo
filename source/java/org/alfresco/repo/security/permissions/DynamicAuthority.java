package org.alfresco.repo.security.permissions;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The interface for a dynamic authority provider e.g. for the owner of a node
 * or any other authority that is determined by the context rather than just a
 * node.
 * 
 * @author Andy Hind
 */
public interface DynamicAuthority
{
    /**
     * Is this authority granted to the given user for this node ref?
     * 
     * @param nodeRef NodeRef
     * @param userName String
     * @return true if the current user has the authority
     */
    public boolean hasAuthority(NodeRef nodeRef, String userName);

    /**
     * If this authority is granted this method provides the string
     * representation of the granted authority.
     * 
     * @return the authority taht may be assigned
     */
    public String getAuthority();
    
    /**
     * For what permission checks is this dynamic authority required?
     * If null, it is required for all checks. 
     * 
     * @return the set of permissions for which this dynamic authority should be evaluated
     */
    public Set<PermissionReference> requiredFor();
}
