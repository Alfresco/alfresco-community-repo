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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * A single permission entry defined against a node.
 * 
 * @author andyh
 */
public interface PermissionEntry
{
    /**
     * Get the permission definition.
     * 
     * This may be null. Null implies that the settings apply to all permissions
     * 
     * @return
     */
    public PermissionReference getPermissionReference();

    /**
     * Get the authority to which this entry applies This could be the string
     * value of a username, group, role or any other authority assigned to the
     * authorisation.
     * 
     * If null then this applies to all.
     * 
     * @return
     */
    public String getAuthority();

    /**
     * Get the node ref for the node to which this permission applies.
     * 
     * This can only be null for a global permission 
     * 
     * @return
     */
    public NodeRef getNodeRef();

    /**
     * Is permissions denied?
     *
     */
    public boolean isDenied();

    /**
     * Is permission allowed?
     *
     */
    public boolean isAllowed();
    
    /**
     * Get the Access enum value
     * 
     * @return
     */
    public AccessStatus getAccessStatus();
}
