package org.alfresco.repo.security.permissions;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Encapsulate how permissions are globally inherited between nodes.
 * 
 * @author andyh
 */
public interface NodePermissionEntry
{
    /**
     * Get the node ref.
     * 
     * @return NodeRef
     */
    public NodeRef getNodeRef();
    
    /**
     * Does the node inherit permissions from its primary parent?
     * 
     * @return boolean
     */
    public boolean inheritPermissions();
    
    
    /**
     * Get the permission entries set for this node.
     * 
     * @return List
     */
    public List<? extends PermissionEntry> getPermissionEntries();
}
