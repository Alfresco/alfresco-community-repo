package org.alfresco.repo.security.permissions;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Interface for objects that can provide information to allow permission checks
 * <p/>
 * Implement this interface to enable the permission filtering layers to extract
 * information in order to check permissions.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface PermissionCheckValue
{
    /**
     * Get the underlying node value that needs to be permission checked.
     * 
     * @return              the underlying value to filter
     */
    NodeRef getNodeRef();
}
