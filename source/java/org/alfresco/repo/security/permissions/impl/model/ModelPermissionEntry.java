/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.PermissionReferenceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Support to read and store the definion of a permission entry.
 * 
 * @author andyh
 */
public class ModelPermissionEntry implements PermissionEntry, XMLModelInitialisable
{
    // XML Constants
    
    private static final String PERMISSION_REFERENCE = "permissionReference";

    private static final String RECIPIENT = "recipient";

    private static final String ACCESS = "access";

    private static final String DENY = "deny";

    private static final String ALLOW = "allow";

    private static final String TYPE = "type";
    
    private static final String NAME = "name";

    // Instance variables
    
    private String recipient;

    private AccessStatus access;

    private PermissionReference permissionReference;

    private NodeRef nodeRef;

    /**
     * Create a permission model entry with the given node context
     * @param nodeRef (may be null)
     */
    public ModelPermissionEntry(NodeRef nodeRef)
    {
        super();
        this.nodeRef = nodeRef;
    }

    public PermissionReference getPermissionReference()
    {
        return permissionReference;
    }

    public String getAuthority()
    {
        return getRecipient();
    }

    /**
     * Synonym for authority
     * @return recipient/authority
     */
    public String getRecipient()
    {
        return recipient;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean isDenied()
    {
        return access == AccessStatus.DENIED;
    }

    public boolean isAllowed()
    {
        return access == AccessStatus.ALLOWED;
    }

    public AccessStatus getAccessStatus()
    {
        return access;
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        Attribute recipientAttribute = element.attribute(RECIPIENT);
        if (recipientAttribute != null)
        {
            recipient = recipientAttribute.getStringValue();
        }
        else
        {
            recipient = null;
        }

        Attribute accessAttribute = element.attribute(ACCESS);
        if (accessAttribute != null)
        {
            if (accessAttribute.getStringValue().equalsIgnoreCase(ALLOW))
            {
                access = AccessStatus.ALLOWED;
            }
            else if (accessAttribute.getStringValue().equalsIgnoreCase(DENY))
            {
                access = AccessStatus.DENIED;
            }
            else
            {
                throw new PermissionModelException("The default permission must be deny or allow");
            }
        }
        else
        {
            access = AccessStatus.DENIED;
        }
        
        
        Element permissionReferenceElement = element.element(PERMISSION_REFERENCE);
        QName typeQName = QName.createQName(permissionReferenceElement.attributeValue(TYPE), nspr);
        String name = permissionReferenceElement.attributeValue(NAME);
        permissionReference = PermissionReferenceImpl.getPermissionReference(typeQName, name);
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
