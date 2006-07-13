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
 * DAO for NewInRepository markers.
 * @author britt
 */
public interface NewInRepositoryDAO
{
    /**
     * Save one.
     * @param newEntry The item to save.
     */
    public void save(NewInRepository newEntry);
    
    /**
     * Get one by Node.
     * @param node The node to lookup with.
     * @return The Entry or null if not found.
     */
    public NewInRepository getByNode(AVMNode node);

    /**
     * Get all that are in the given repository.
     * @param repository The Repository.
     * @return A List of NewInRepositorys.
     */
    public List<NewInRepository> getByRepository(Repository repository);
    
    /**
     * Delete the given entry.
     * @param newEntry The entry to delete.
     */
    public void delete(NewInRepository newEntry);
}
