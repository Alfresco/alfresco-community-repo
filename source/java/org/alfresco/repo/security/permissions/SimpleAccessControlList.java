package org.alfresco.repo.security.permissions;

import java.util.ArrayList;
import java.util.List;

public class SimpleAccessControlList implements AccessControlList
{
    private AccessControlListProperties properties;
    
    private List<AccessControlEntry> entries = new ArrayList<AccessControlEntry>();
    
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
    
    

}
