package org.alfresco.repo.transfer.manifest;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object to represent the access control on a Manifest Node.
 *
 */
public class ManifestAccessControl
{
    private boolean isInherited;
    private List<ManifestPermission> permissions;
    
    public void setInherited(boolean isInherited)
    {
        this.isInherited = isInherited;
    }
    
    public boolean isInherited()
    {
        return isInherited;
    }
    
    public void setPermissions(List<ManifestPermission> permissions)
    {
        this.permissions = permissions;
    }
    
    public List<ManifestPermission> getPermissions()
    {
        return permissions;
    } 
    
    public void addPermission(ManifestPermission permission)
    {
        if(permissions == null)
        {
            permissions = new ArrayList<ManifestPermission>(20);
        }
        permissions.add(permission);
    }
}
