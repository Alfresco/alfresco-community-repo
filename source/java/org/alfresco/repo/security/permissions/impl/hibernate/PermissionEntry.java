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

/**
 * The interface against which permission entries are persisted
 * 
 * @author andyh
 */

public interface PermissionEntry
{
    /**
     * Get the identifier for this object.
     * 
     * @return
     */
    public long getId();
    
    /**
     * Get the containing node permission entry.
     * 
     * @return
     */
    public NodePermissionEntry getNodePermissionEntry();
    
    /**
     * Get the permission to which this entry applies.
     * 
     * @return
     */
    public PermissionReference getPermissionReference();
    
    /**
     * Get the recipient to which this entry applies.
     * 
     * @return
     */
    public Recipient getRecipient();
    
    /**
     * Is this permission allowed?
     * @return
     */
    public boolean isAllowed();
    
    /**
     * Set if this permission is allowed, otherwise it is denied.
     * 
     * @param allowed
     */
    public void setAllowed(boolean allowed);
    
    /**
     * Delete this permission entry - allows for deleting of the bidirectional relationship to the node permission entry.
     *
     */
    public void delete();
}
