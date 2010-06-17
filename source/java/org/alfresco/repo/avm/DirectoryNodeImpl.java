/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.util.Pair;

/**
 * Base class for Directories.
 * @author britt
 */
public abstract class DirectoryNodeImpl extends AVMNodeImpl implements DirectoryNode
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
     * @param repo
     */
    protected DirectoryNodeImpl(AVMStore repo)
    {
        super(repo);
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
    
    /**
     * Does this node directly contain the indicated node.
     *
     * @param node
     *            The node we are checking.
     * @return Whether node is directly contained.
     */
    public boolean directlyContains(AVMNode node)
    {
        return AVMDAOs.Instance().fChildEntryDAO.existsParentChild(this, node);
    }
    
    /**
     * Lookup a child node by name.
     * @param lPath The lookup path so far.
     * @param name The name to lookup.
     * @param includeDeleted Whether to lookup deleted nodes.
     * @return The child node or null.
     */
    public Pair<AVMNode, Boolean> lookupChild(Lookup lPath, String name, boolean includeDeleted)
    {
        Pair<ChildEntry, Boolean> result = lookupChildEntry(lPath, name, includeDeleted);
        if (result == null)
        {
            return null;
        }
        return new Pair<AVMNode, Boolean>(result.getFirst().getChild(), result.getSecond());
    }
}
