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
package org.alfresco.repo.security.permissions.impl.hibernate;

import java.util.Set;

import org.alfresco.repo.domain.NodeKey;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The interface to support persistence of node permission entries in hibernate
 * 
 * @author andyh
 */
public interface NodePermissionEntry
{
    /**
     * Get the node key.
     * 
     * @return
     */
    public NodeKey getNodeKey();

    /**
     * Set the node key.
     * 
     * @param key
     */
    public void setNodeKey(NodeKey key);
    
    /**
     * Get the node ref
     * 
     * @return
     */
    public NodeRef getNodeRef();
    
    /**
     * Get inheritance behaviour
     * @return
     */
    public boolean getInherits();
    
    /**
     * Set inheritance behaviour
     * @param inherits
     */
    public void setInherits(boolean inherits);
    
    /**
     * Get the permission entries set for the node
     * @return
     */
    public Set<PermissionEntry> getPermissionEntries();
    
}
