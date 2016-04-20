package org.alfresco.repo.transfer.manifest;

/**
 * Data transfer object for a permission of a Manifest Node
 * 
 * @author mrogers
 */
public class ManifestPermission
{
    private String authority;
    private String permission;
    private String status;
    public void setAuthority(String authority)
    {
        this.authority = authority;
    }
    public String getAuthority()
    {
        return authority;
    }
    public void setPermission(String permission)
    {
        this.permission = permission;
    }
    public String getPermission()
    {
        return permission;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    public String getStatus()
    {
        return status;
    }
    
    public int hashCode()
    {
        return authority.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        final ManifestPermission other = (ManifestPermission) obj;
        
        if (!status.equals(other.status))
            return false;
        
        if (authority == null)
        {
            if (other.authority != null)
                return false;
        }
        else if (!authority.equals(other.authority))
            return false;
        
        if (permission == null)
        {
            if (other.permission != null)
                return false;
        }
        else if (!permission.equals(other.permission))
            return false;
        
        return true;
    }
    
    public String toString()
    {
        return permission + ", " + authority + ", " + status;
    }

    
    
}
