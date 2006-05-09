/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm.hibernate;

/**
 * This exists to share content across different versions.
 * @author britt
 */
public interface ContentBean
{
    /**
     * Set the object id.
     * @param id The object id.
     */
    public void setId(long id);
    
    /**
     * Get the object id.
     * @return The object id.
     */
    public long getId();
    
    /**
     * Set the reference count on this.
     * @param refCount The reference count to set.
     */
    public void setRefCount(int refCount);
    
    /**
     * Get the reference count on this.
     * @return The reference count.
     */
    public int getRefCount();
    
    /**
     * Set the version (for concurrency control).
     * @param vers The version.
     */
    public void setVers(long vers);
    
    /**
     * Get the version (for concurrency control).
     * @return The version.
     */
    public long getVers();
}
