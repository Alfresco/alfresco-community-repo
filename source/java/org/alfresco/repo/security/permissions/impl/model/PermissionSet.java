/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.security.permissions.impl.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Store and read the definition of a permission set
 * @author andyh
 */
public final class PermissionSet implements XMLModelInitialisable
{
    private static final String TYPE = "type";
    private static final String PERMISSION_GROUP = "permissionGroup";
    private static final String PERMISSION = "permission";
    private static final String EXPOSE = "expose";
    private static final String EXPOSE_ALL = "all";
    //private static final String EXPOSE_SELECTED = "selected";
    
    
    private QName qname;
    
    private boolean exposeAll;
    
    private Set<PermissionGroup> permissionGroups = new LinkedHashSet<PermissionGroup>(32, 1.0f);
    
    private Set<Permission> permissions = new HashSet<Permission>(32, 1.0f);
    
    public PermissionSet()
    {
        super();
    }
    
    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        qname = QName.createQName(element.attributeValue(TYPE), nspr);
        
        Attribute exposeAttribute = element.attribute(EXPOSE);
        if(exposeAttribute != null)
        {
            exposeAll = exposeAttribute.getStringValue().equalsIgnoreCase(EXPOSE_ALL);
        }
        else
        {
            exposeAll = true;
        }
        
        for(Iterator pgit = element.elementIterator(PERMISSION_GROUP); pgit.hasNext(); /**/)
        {
            Element permissionGroupElement = (Element)pgit.next();
            PermissionGroup permissionGroup = new PermissionGroup(qname);
            permissionGroup.initialise(permissionGroupElement, nspr, permissionModel);
            permissionGroups.add(permissionGroup);
        }
        
        for(Iterator pit = element.elementIterator(PERMISSION); pit.hasNext(); /**/)
        {
            Element permissionElement = (Element)pit.next();
            Permission permission = new Permission(qname);
            permission.initialise(permissionElement, nspr, permissionModel);
            permissions.add(permission);
        }
        
    }

    public Set<PermissionGroup> getPermissionGroups()
    {
        return Collections.unmodifiableSet(permissionGroups);
    }

    public Set<Permission> getPermissions()
    {
        return Collections.unmodifiableSet(permissions);
    }

    public QName getQName()
    {
        return qname;
    }

    public boolean exposeAll()
    {
        return exposeAll;
    }
}
