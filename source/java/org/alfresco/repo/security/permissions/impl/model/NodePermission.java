/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
