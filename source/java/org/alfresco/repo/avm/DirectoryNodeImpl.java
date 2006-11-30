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

import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;

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
    protected DirectoryNodeImpl(long id, AVMStore repo)
    {
        super(id, repo);
    }
    
    /**
     * Dangerous version of link.
     * @param name The name to give the child.
     * @param toLink The child to link in.
     */
    public void link(String name, AVMNodeDescriptor toLink)
    {
        AVMNode node = AVMDAOs.Instance().fAVMNodeDAO.getByID(toLink.getId());
        if (node == null)
        {
            throw new AVMNotFoundException("Child node not found.");
        }
        if (node.getType() == AVMNodeType.LAYERED_DIRECTORY &&
            !((LayeredDirectoryNode)node).getPrimaryIndirection())
        {
            throw new AVMBadArgumentException("Non primary layered directories cannot be linked.");
        }
        // Make the new ChildEntry and save.
        ChildKey key = new ChildKey(this, name);
        ChildEntry newChild = new ChildEntryImpl(key, node);
        AVMDAOs.Instance().fChildEntryDAO.save(newChild);
    }    
}
