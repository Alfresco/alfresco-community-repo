package org.alfresco.repo.security.person;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Utility bean to set/check permissions on a node
 * @author andyh
 *
 */
public interface PermissionsManager
{
    /**
     * Set the permission as defined on the given node
     * 
     * @param nodeRef - the nodeRef 
     * @param owner - which should be set as the owner of the node (if configured to be set)
     */
    public void setPermissions(NodeRef nodeRef, String owner, String user);
    
    /**
     * Validate that permissions are set on a node as defined.
     * 
     * @param nodeRef NodeRef
     * @param owner String
     * @param user String
     * @return - true if correct, false if they are not set as defined.
     */
    public boolean validatePermissions(NodeRef nodeRef, String owner, String user);
}
