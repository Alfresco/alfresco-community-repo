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
 * DAO for DeletedChildren.
 * @author britt
 */
interface DeletedChildDAO
{
    /**
     * Save an unsaved DeletedChild.
     * @param child The DeletedChild to be saved.
     */
    public void save(DeletedChild child);
    
    /**
     * Delete one.
     * @param child The one to delete.
     */
    public void delete(DeletedChild child);
    
    /**
     * Delete all belonging to the given parent.
     * @param parent The parent.
     */
    public void deleteByParent(AVMNode parent);
    
    /**
     * Get by name and parent.
     * @param name The name of the deleted entry.
     * @param parent The parent.
     * @return A DeletedChild or null if not found.
     */
    public DeletedChild getByNameParent(String name, LayeredDirectoryNode parent);
    
    /**
     * Get all the deleted children of a given parent.
     * @param parent The parent.
     * @return A List of DeletedChildren.
     */
    public List<DeletedChild> getByParent(LayeredDirectoryNode parent);
}
