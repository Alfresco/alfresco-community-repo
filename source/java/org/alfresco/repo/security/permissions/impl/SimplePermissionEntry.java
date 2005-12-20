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

import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * A simple object representation of a permission entry.
 *  
 * @author andyh
 */
public final class SimplePermissionEntry extends AbstractPermissionEntry
{
    
    /*
     * The node ref to which the permissoin applies
     */
    private NodeRef nodeRef;
    
    /*
     * The permission reference - as a simple permission reference
     */
    private PermissionReference permissionReference;
    
    /*
     * The authority to which the permission aplies
     */
    private String authority;
    
    /*
     * The access mode for the permission
     */
    private AccessStatus accessStatus;
    
    
    
    public SimplePermissionEntry(NodeRef nodeRef, PermissionReference permissionReference, String authority, AccessStatus accessStatus)
    {
        super();
        this.nodeRef = nodeRef;
        this.permissionReference = permissionReference;
        this.authority = authority;
        this.accessStatus = accessStatus;
    }

    public PermissionReference getPermissionReference()
    {
        return permissionReference;
    }

    public String getAuthority()
    {
       return authority;
    }

    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean isDenied()
    {
        return accessStatus == AccessStatus.DENIED;
    }

    public boolean isAllowed()
    {
        return accessStatus == AccessStatus.ALLOWED;
    }

    public AccessStatus getAccessStatus()
    {
        return accessStatus;
    }

}
