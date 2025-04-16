/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.permissions.impl.model;

import org.dom4j.Attribute;
import org.dom4j.Element;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespacePrefixResolver;

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
        if (authorityAttribute != null)
        {
            authority = authorityAttribute.getStringValue();
        }
        Attribute permissionAttribute = element.attribute(PERMISSION);
        if (permissionAttribute != null)
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
