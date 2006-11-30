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

import java.util.List;

/**
 * DAO for Repositories.
 * @author britt
 */
public interface AVMStoreDAO
{
    /**
     * Save an AVMStore, never before saved.
     * @param store The AVMStore
     */
    public void save(AVMStore store);
    
    /**
     * Delete the given AVMStore.
     * @param store The AVMStore.
     */
    public void delete(AVMStore store);
    
    /**
     * Get all AVMStores.
     * @return A List of all the AVMStores.
     */
    public List<AVMStore> getAll();

    /**
     * Get an AVMStore by name.
     * @param name The name of the AVMStore.
     * @return The AVMStore or null if not found.
     */
    public AVMStore getByName(String name);
    
    /**
     * Get the AVM Store that has the given root as HEAD.
     * @param root The root to query.
     * @return The matching store or null.
     */
    public AVMStore getByRoot(AVMNode root);
    
    /**
     * Update the given AVMStore record.
     * @param rep The dirty AVMStore.
     */
    public void update(AVMStore rep);
}
