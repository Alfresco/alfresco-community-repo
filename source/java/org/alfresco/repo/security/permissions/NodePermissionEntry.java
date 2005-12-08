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
package org.alfresco.repo.security.permissions;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Encapsulate how permissions are globally inherited between nodes.
 * 
 * @author andyh
 */
public interface NodePermissionEntry
{
    /**
     * Get the node ref.
     * 
     * @return
     */
    public NodeRef getNodeRef();
    
    /**
     * Does the node inherit permissions from its primary parent?
     * 
     * @return
     */
    public boolean inheritPermissions();
    
    
    /**
     * Get the permission entries set for this node.
     * 
     * @return
     */
    public Set<? extends PermissionEntry> getPermissionEntries();
}
