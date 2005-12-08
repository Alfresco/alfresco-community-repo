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
package org.alfresco.repo.security.permissions.impl;

import java.io.Serializable;
import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A simple object representation of a node permission entry
 * 
 * @author andyh
 */
public class SimpleNodePermissionEntry extends AbstractNodePermissionEntry implements Serializable
{
    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 8157870444595023347L;

    /*
     * The node 
     */
    private NodeRef nodeRef;
    
    /*
     * Are permissions inherited?
     */
    private boolean inheritPermissions;
    
    /*
     * The set of permission entries.
     */
    private Set<? extends PermissionEntry> permissionEntries;
    
    
    public SimpleNodePermissionEntry(NodeRef nodeRef, boolean inheritPermissions, Set<? extends PermissionEntry> permissionEntries)
    {
        super();
        this.nodeRef = nodeRef;
        this.inheritPermissions = inheritPermissions;
        this.permissionEntries = permissionEntries;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean inheritPermissions()
    {
        return inheritPermissions;
    }

    public Set<? extends PermissionEntry> getPermissionEntries()
    {
       return permissionEntries;
    }

}
