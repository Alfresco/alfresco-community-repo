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

package org.alfresco.repo.avm;

import java.util.Date;
import java.util.List;

/**
 * DAO for VersionRoot objects.
 * @author britt
 */
public interface VersionRootDAO
{
    /**
     * Save an unsaved VersionRoot.
     * @param vr The VersionRoot to save.
     */
    public void save(VersionRoot vr);

    /**
     * Delete a VersionRoot.
     * @param vr The VersionRoot to delete.
     */
    public void delete(VersionRoot vr);
    
    /**
     * Get all the version roots in a given store.
     * @param store The store.
     * @return A List of VersionRoots.  In id order.
     */
    public List<VersionRoot> getAllInAVMStore(AVMStore store);

    /**
     * Get the VersionRoot corresponding to the given id.
     * @param store The store
     * @param id The version id.
     * @return The VersionRoot or null if not found.
     */
    public VersionRoot getByVersionID(AVMStore store, int id);
    
    /**
     * Get one from its root.
     * @param root The root to match.
     * @return The version root or null.
     */
    public VersionRoot getByRoot(AVMNode root);
    
    /**
     * Get the version of a store by dates.
     * @param store The store.
     * @param from The starting date.  May be null but not with to null also.
     * @param to The ending date.  May be null but not with from null also.
     * @return A List of VersionRoots.
     */
    public List<VersionRoot> getByDates(AVMStore store, Date from, Date to);
    
    /**
     * Get the highest numbered version in a store.
     * @param store The store.
     * @return The highest numbered version.
     */
    public VersionRoot getMaxVersion(AVMStore store);
    
    /**
     * Get the highest numbered id from all the versions in a store.
     * @param store The store.
     * @return The highest numbered id.
     */
    public Integer getMaxVersionID(AVMStore store);
}
