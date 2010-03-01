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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Support to read and store the definition of node permissions
 * @author andyh
 */
public class NodePermission implements NodePermissionEntry, XMLModelInitialisable
{
    // XML Constants
    
    private static final String NODE_REF = "nodeRef";
    
    private static final String NODE_PERMISSION = "nodePermission";
    
    private static final String INHERIT_FROM_PARENT = "inheritFromParent";
    
    // Instance variables
    
    // If null then it is the root.
    private NodeRef nodeRef;
    
    private List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>();
    
    private boolean inheritPermissionsFromParent;
    
    public NodePermission()
    {
        super();
    }

    public NodeRef getNodeRef()
    {
       return nodeRef;
    }

    public boolean inheritPermissions()
    {
        return inheritPermissionsFromParent;
    }

    public List<PermissionEntry> getPermissionEntries()
    {
       return Collections.unmodifiableList(permissionEntries);
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
       Attribute nodeRefAttribute = element.attribute(NODE_REF);
       if(nodeRefAttribute != null)
       {
           nodeRef = new NodeRef(nodeRefAttribute.getStringValue());
       }
       
       Attribute inheritFromParentAttribute = element.attribute(INHERIT_FROM_PARENT);
       if(inheritFromParentAttribute != null)
       {
           inheritPermissionsFromParent = Boolean.parseBoolean(inheritFromParentAttribute.getStringValue());
       }
       else
       {
           inheritPermissionsFromParent = true;
       }
       
       // Node Permissions Entry

       for (Iterator npit = element.elementIterator(NODE_PERMISSION); npit.hasNext(); /**/)
       {
           Element permissionEntryElement = (Element) npit.next();
           ModelPermissionEntry permissionEntry = new ModelPermissionEntry(nodeRef);
           permissionEntry.initialise(permissionEntryElement, nspr, permissionModel);
           permissionEntries.add(permissionEntry);
       }
        
    }

    
    
}
