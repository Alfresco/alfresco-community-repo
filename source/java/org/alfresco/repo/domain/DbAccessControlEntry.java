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
package org.alfresco.repo.domain;



/**
 * The interface against which permission entries are persisted
 * 
 * @author andyh
 */

public interface DbAccessControlEntry
{
    /**
     * @return Returns the identifier for this object
     */
    public long getId();
    
    /**
     * @return Returns the containing access control list
     */
    public DbAccessControlList getAccessControlList();
    
    /**
     * @param acl the accession control list to which entry belongs
     */
    public void setAccessControlList(DbAccessControlList acl);
    
    /**
     * @return Returns the permission to which this entry applies
     */
    public DbPermission getPermission();
    
    /**
     * @param permission the permission to which the entry applies
     */
    public void setPermission(DbPermission permission);
    
    /**
     * @return Returns the authority to which this entry applies
     */
    public DbAuthority getAuthority();
    
    /**
     * @param authority the authority to which this entry applies
     */
    public void setAuthority(DbAuthority authority);
    
    /**
     * @return Returns <tt>true</tt> if this permission is allowed 
     */
    public boolean isAllowed();
    
    /**
     * Set if this permission is allowed, otherwise it is denied.
     * 
     * @param allowed
     */
    public void setAllowed(boolean allowed);
}
