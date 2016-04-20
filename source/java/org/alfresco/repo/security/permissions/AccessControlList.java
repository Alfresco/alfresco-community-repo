package org.alfresco.repo.security.permissions;

import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;

public interface AccessControlList extends Serializable
{
    /**
     * Get the properties
     * @return AccessControlListProperties
     */
    public AccessControlListProperties getProperties();
    
    /**
     * Get the members of the ACL in order
     * Ordered by:
     * position, 
     * then deny followed by allow, 
     * then by authority type 
     * then ....
     * 
     * To make permission evaluation faster for the common cases
     * 
     * @return List<AccessControlEntry>
     */
    public List<AccessControlEntry> getEntries();
    
    public SimpleNodePermissionEntry getCachedSimpleNodePermissionEntry();
    
    public void setCachedSimpleNodePermissionEntry(SimpleNodePermissionEntry cachedSimpleNodePermissionEntry);
}
