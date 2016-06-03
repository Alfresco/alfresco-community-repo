package org.alfresco.repo.security.permissions;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * A single permission entry defined against a node.
 * 
 * @author andyh
 */
public interface PermissionEntry
{
    /**
     * Get the permission definition.
     * 
     * This may be null. Null implies that the settings apply to all permissions
     * 
     * @return PermissionReference
     */
    public PermissionReference getPermissionReference();

    /**
     * Get the authority to which this entry applies This could be the string
     * value of a username, group, role or any other authority assigned to the
     * authorisation.
     * 
     * If null then this applies to all.
     * 
     * @return String
     */
    public String getAuthority();

    /**
     * Get the node ref for the node to which this permission applies.
     * 
     * This can only be null for a global permission 
     * 
     * @return NodeRef
     */
    public NodeRef getNodeRef();

    /**
     * Is permissions denied?
     *
     */
    public boolean isDenied();

    /**
     * Is permission allowed?
     *
     */
    public boolean isAllowed();
    
    /**
     * Get the Access enum value
     * 
     * @return AccessStatus
     */
    public AccessStatus getAccessStatus();
    
    /**
     * Is this permission inherited?
     * @return boolean
     */
    public boolean isInherited();
    
    /**
     * Return the position in the inhertance chain (0 is not inherited and set on the object)
     * @return int
     */
    public int getPosition();
}
