/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */

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
        AVMDAOs.Instance().fAVMNodeDAO.flush();
        AVMDAOs.Instance().fChildEntryDAO.evict(newChild);
        AVMDAOs.Instance().fAVMNodeDAO.evict(node);
    }
}
