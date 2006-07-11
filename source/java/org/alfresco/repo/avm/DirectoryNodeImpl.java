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
 * Base class for Directories.
 * @author britt
 */
abstract class DirectoryNodeImpl extends AVMNodeImpl implements DirectoryNode
{
    /**
     * Default constructor.
     */
    protected DirectoryNodeImpl()
    {
    }
    
    /**
     * A pass through constructor. Called when a new concrete subclass
     * instance is created.
     * @param id
     * @param repo
     */
    protected DirectoryNodeImpl(long id, Repository repo)
    {
        super(id, repo);
    }
    
    /**
     * Retrieves the ChildEntry in this directory with the given name.
     * @param name The name to look for.
     * @param write Whether the child should be looked up for writing.
     * @return The ChildEntry or null if not found.
     */
    protected ChildEntry getChild(String name, boolean write)
    {
        return AVMContext.fgInstance.fChildEntryDAO.getByNameParent(name, this);
    }
    
    /**
     * Get all the children of this directory. NB, this should
     * really be considered an internal method but it needs to be
     * exposed through the interface.
     * @return A List of ChildEntries.
     */
    public List<ChildEntry> getChildren()
    {
        return AVMContext.fgInstance.fChildEntryDAO.getByParent(this);
    }
    
    /**
     * Get the ChildEntry that has the given child.
     * @param child The child node to look for.
     * @return The ChildEntry or null if not found.
     */
    protected ChildEntry getChild(AVMNode child)
    {
        return AVMContext.fgInstance.fChildEntryDAO.getByParentChild(this, child);
    }
}
