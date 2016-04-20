package org.alfresco.repo.security.permissions;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;

public class SimpleAccessControlList implements AccessControlList
{
    /**
     * 
     */
    private static final long serialVersionUID = -1859514919998903150L;

    private AccessControlListProperties properties;
    
    private List<AccessControlEntry> entries = new ArrayList<AccessControlEntry>();
    
    private transient SimpleNodePermissionEntry cachedSimpleNodePermissionEntry;
    
    public List<AccessControlEntry> getEntries()
    {
        return entries;
    }

    public AccessControlListProperties getProperties()
    {
       return properties;
    }

    public void setEntries(List<AccessControlEntry> entries)
    {
        this.entries = entries;
    }

    public void setProperties(AccessControlListProperties properties)
    {
        this.properties = properties;
    }

    public synchronized SimpleNodePermissionEntry getCachedSimpleNodePermissionEntry()
    {
        return cachedSimpleNodePermissionEntry;
    }

    public synchronized void setCachedSimpleNodePermissionEntry(SimpleNodePermissionEntry cachedSimpleNodePermissionEntry)
    {
        this.cachedSimpleNodePermissionEntry = cachedSimpleNodePermissionEntry;
    }
    
    

}
