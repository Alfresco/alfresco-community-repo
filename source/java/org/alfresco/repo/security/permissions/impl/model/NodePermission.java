/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.permissions.impl.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
    
    private Set<PermissionEntry> permissionEntries = new HashSet<PermissionEntry>();
    
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

    public Set<PermissionEntry> getPermissionEntries()
    {
       return Collections.unmodifiableSet(permissionEntries);
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
