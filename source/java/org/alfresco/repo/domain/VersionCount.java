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
 * Represents a version count entity for a particular store.
 * 
 * @author Derek Hulley
 */
public interface VersionCount
{
    /**
     * @return Returns the key for the version counter
     */
    public StoreKey getKey();

    /**
     * @param key the key uniquely identifying this version counter
     */
    public void setKey(StoreKey key);
    
    /**
     * Increments and returns the next version counter associated with this
     * store.
     * 
     * @return Returns the next version counter in the sequence
     * 
     * @see #getVersionCount()
     */
    public int incrementVersionCount();
    
    /**
     * Reset the store's version counter
     */
    public void resetVersionCount();
    
    /**
     * Retrieve the current version counter
     * 
     * @return Returns a current version counter
     * 
     * @see #incrementVersionCount()
     */
    public int getVersionCount();
}
