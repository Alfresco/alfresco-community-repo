package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.dom4j.Attribute;
import org.dom4j.Element;

public class GlobalPermissionEntry implements XMLModelInitialisable, PermissionEntry
{
    private static final String AUTHORITY = "authority";
    
    private static final String PERMISSION = "permission";
    
    private String authority;
    
    private PermissionReference permissionReference;
    
    public GlobalPermissionEntry()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        Attribute authorityAttribute = element.attribute(AUTHORITY);
        if(authorityAttribute != null)
        {
            authority = authorityAttribute.getStringValue();
        }
        Attribute permissionAttribute = element.attribute(PERMISSION);
        if(permissionAttribute != null)
        {
            permissionReference = permissionModel.getPermissionReference(null, permissionAttribute.getStringValue());
        }

    }
    
    public String getAuthority()
    {
        return authority;
    }
    
    public PermissionReference getPermissionReference()
    {
        return permissionReference;
    }

    public NodeRef getNodeRef()
    {
        return null;
    }

    public boolean isDenied()
    {
        return false;
    }

    public boolean isAllowed()
    {
        return true;
    }

    public AccessStatus getAccessStatus()
    {
        return AccessStatus.ALLOWED;
    }

    public int getPosition()
    {
        return 0;
    }

    public boolean isInherited()
    {
        return false;
    }

}
