package org.alfresco.repo.security.permissions.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A simple object representation of a node permission entry
 * 
 * @author andyh
 */
public final class SimpleNodePermissionEntry extends AbstractNodePermissionEntry implements Serializable
{
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 8157870444595023347L;

    /*
     * The node 
     */
    private NodeRef nodeRef;
    
    /*
     * Are permissions inherited?
     */
    private boolean inheritPermissions;
    
    /*
     * The set of permission entries.
     */
    private List<? extends PermissionEntry> permissionEntries;
    
    
    public SimpleNodePermissionEntry(NodeRef nodeRef, boolean inheritPermissions, List<? extends PermissionEntry> permissionEntries)
    {
        super();
        this.nodeRef = nodeRef;
        this.inheritPermissions = inheritPermissions;
        this.permissionEntries = permissionEntries;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean inheritPermissions()
    {
        return inheritPermissions;
    }

    public List<? extends PermissionEntry> getPermissionEntries()
    {
       return permissionEntries;
    }

}
