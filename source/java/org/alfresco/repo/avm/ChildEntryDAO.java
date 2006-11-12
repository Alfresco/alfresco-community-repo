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
 * DAO for ChildEntries.
 * @author britt
 */
public interface ChildEntryDAO
{
    /**
     * Save an unsaved ChildEntry.
     * @param entry The entry to save.
     */
    public void save(ChildEntry entry);
    
    /**
     * Get an entry by name and parent.
     * @param name The name of the child to find.
     * @param parent The parent to look in.
     * @return The ChildEntry or null if not foun.
     */
    public ChildEntry get(ChildKey key);
    
    /**
     * Get all the children of a given parent.
     * @param parent The parent.
     * @return A List of ChildEntries.
     */
    public List<ChildEntry> getByParent(DirectoryNode parent);
    
    /**
     * Get the entry for a given child in a given parent.
     * @param parent The parent.
     * @param child The child.
     * @return The ChildEntry or null.
     */
    public ChildEntry getByParentChild(DirectoryNode parent, AVMNode child);
    
    /**
     * Get all the ChildEntries corresponding to the given child.
     * @param child The child for which to look up entries.
     * @return The matching entries.
     */
    public List<ChildEntry> getByChild(AVMNode child);
    
    /**
     * Update a dirty ChildEntry.
     * @param child The dirty entry.
     */
    public void update(ChildEntry child);
    
    /**
     * Delete one.
     * @param child The one to delete.
     */
    public void delete(ChildEntry child);
    
    /**
     * Delete all children of the given parent.
     * @param parent The parent.
     */
    public void deleteByParent(AVMNode parent);
}
