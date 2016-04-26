package org.alfresco.repo.security.permissions.impl;

import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * A simple object representation of a permission entry.
 *  
 * @author andyh
 */
public final class SimplePermissionEntry extends AbstractPermissionEntry
{
    
    /*
     * The node ref to which the permissoin applies
     */
    private NodeRef nodeRef;
    
    /*
     * The permission reference - as a simple permission reference
     */
    private PermissionReference permissionReference;
    
    /*
     * The authority to which the permission aplies
     */
    private String authority;
    
    /*
     * The access mode for the permission
     */
    private AccessStatus accessStatus;
    
    private int position;
    
    public SimplePermissionEntry(NodeRef nodeRef, PermissionReference permissionReference, String authority, AccessStatus accessStatus)
    {
       this(nodeRef, permissionReference, authority, accessStatus, 0);
    }
    
    public SimplePermissionEntry(NodeRef nodeRef, PermissionReference permissionReference, String authority, AccessStatus accessStatus, int position)
    {
        super();
        this.nodeRef = nodeRef;
        this.permissionReference = permissionReference;
        this.authority = authority;
        this.accessStatus = accessStatus;
        this.position = position;
    }

    public PermissionReference getPermissionReference()
    {
        return permissionReference;
    }

    public String getAuthority()
    {
       return authority;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean isDenied()
    {
        return accessStatus == AccessStatus.DENIED;
    }

    public boolean isAllowed()
    {
        return accessStatus == AccessStatus.ALLOWED;
    }

    public AccessStatus getAccessStatus()
    {
        return accessStatus;
    }

    public int getPosition()
    {
        return position;
    }

    public boolean isInherited()
    {
        return position > 0;
    }

}
