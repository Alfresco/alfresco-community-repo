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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;

/**
 * A plain directory.  No monkey tricks except for possiblyCopy.
 * @author britt
 */
class PlainDirectoryNodeImpl extends DirectoryNodeImpl implements PlainDirectoryNode
{
    static final long serialVersionUID = 9423813734583003L;

    /**
     * Make up a new directory with nothing in it.
     * @param store
     */
    public PlainDirectoryNodeImpl(AVMStore store)
    {
        super(store.getAVMRepository().issueID(), store);
        setVersionID(1);
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
    }
    
    /**
     * Anonymous constructor.
     */
    protected PlainDirectoryNodeImpl()
    {
    }
    
    /**
     * Copy like constructor.
     * @param other The other directory.
     * @param repos The AVMStore Object that will own us.
     */
    @SuppressWarnings("unchecked")
    public PlainDirectoryNodeImpl(PlainDirectoryNode other,
                                  AVMStore store)
    {
        super(store.getAVMRepository().issueID(), store);
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        for (ChildEntry child : AVMDAOs.Instance().fChildEntryDAO.getByParent(other))
        {
            ChildKey key = new ChildKey(this, child.getKey().getName());
            ChildEntry newChild = new ChildEntryImpl(key,
                                                     child.getChild());
            AVMDAOs.Instance().fChildEntryDAO.save(newChild);
        }
        setVersionID(other.getVersionID() + 1);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
        copyProperties(other);
        copyAspects(other);
        copyACLs(other);
    }

    /**
     * Does this directory directly contain the given node. 
     * @param node The node to check.
     * @return Whether it was found.
     */
    public boolean directlyContains(AVMNode node)
    {
        return AVMDAOs.Instance().fChildEntryDAO.getByParentChild(this, node) != null;
    }

    /**
     * Get a directory listing.
     * @param lPath The lookup path.
     * @return The listing.
     */
    @SuppressWarnings("unchecked")
    public Map<String, AVMNode> getListing(Lookup lPath, boolean includeDeleted)
    {
        Map<String, AVMNode> result = new HashMap<String, AVMNode>();
        List<ChildEntry> children = AVMDAOs.Instance().fChildEntryDAO.getByParent(this);
        for (ChildEntry child : children)
        {
            if (!includeDeleted && child.getChild().getType() == AVMNodeType.DELETED_NODE)
            {
                continue;
            }
            result.put(child.getKey().getName(), child.getChild());
        }
        return result;
    }

    /**
     * Get a listing of the nodes directly contained by a directory.
     * @param lPath The Lookup to this directory.
     * @return A Map of names to nodes.
     */
    public Map<String, AVMNode> getListingDirect(Lookup lPath, boolean includeDeleted)
    {
        return getListing(lPath, includeDeleted);
    }
    
    /**
     * Get a listing of the nodes directly contained by a directory.
     * @param dir The node's descriptor.
     * @param includeDeleted Whether to include deleted nodes.
     * @return A Map of Strings to descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListingDirect(AVMNodeDescriptor dir, 
                                                                 boolean includeDeleted)
    {
        return getListing(dir, includeDeleted);
    }

    /**
     * Get a listing of from a directory node descriptor.
     * @param dir The directory node descriptor.
     * @return A Map of names to node descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        if (dir.getPath() == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        SortedMap<String, AVMNodeDescriptor> result = new TreeMap<String, AVMNodeDescriptor>();
        List<ChildEntry> children = AVMDAOs.Instance().fChildEntryDAO.getByParent(this);
        for (ChildEntry child : children)
        {
            if (!includeDeleted && child.getChild().getType() == AVMNodeType.DELETED_NODE)
            {
                continue;
            }
            result.put(child.getKey().getName(), 
                       child.getChild().getDescriptor(dir.getPath(), 
                                                      child.getKey().getName(), 
                                                      dir.getIndirection()));
        }
        return result;
    }

    /**
     * Get the names of nodes deleted in this directory.
     * @return A List of names.
     */
    public List<String> getDeletedNames()
    {
        return new ArrayList<String>();
    }

    /**
     * Lookup a child by name.
     * @param lPath The lookup path so far.
     * @param name The name to lookup.
     * @param includeDeleted Whether to lookup deleted nodes.
     * @return The child or null.
     */
    @SuppressWarnings("unchecked")
    public AVMNode lookupChild(Lookup lPath, String name, boolean includeDeleted)
    {
        ChildKey key = new ChildKey(this, name);
        ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (entry == null || 
            (!includeDeleted && entry.getChild().getType() == AVMNodeType.DELETED_NODE))
        {
            return null;
        }
        // We're doing the hand unrolling of the proxy because
        // Hibernate/CGLIB proxies are broken.
        return AVMNodeUnwrapper.Unwrap(entry.getChild());
    }

    /**
     * Lookup a child using a node descriptor as context.
     * @param mine The node descriptor for this.
     * @param name The name of the child to lookup.
     * @return A node descriptor for the child.
     */
    public AVMNodeDescriptor lookupChild(AVMNodeDescriptor mine, String name, boolean includeDeleted)
    {
        if (mine.getPath() == null)
        {
            throw new AVMBadArgumentException("Path is null.");
        }
        ChildKey key = new ChildKey(this, name);
        ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (entry == null || 
            (!includeDeleted && entry.getChild().getType() == AVMNodeType.DELETED_NODE))
        {
            return null;
        }
        return entry.getChild().getDescriptor(mine.getPath(), name, (String)null);
    }

    /**
     * Remove a child, no copying.
     * @param lPath The path by which this was found.
     * @param name The name of the child to remove.
     */
    @SuppressWarnings("unchecked")
    public void removeChild(Lookup lPath, String name)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        ChildKey key = new ChildKey(this, name);
        ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (entry != null)
        {
            AVMNode child = entry.getChild();
            if (child.getType() == AVMNodeType.DELETED_NODE)
            {
                return;
            }
            AVMDAOs.Instance().fChildEntryDAO.delete(entry);
            if (child.getStoreNew() == null || child.getAncestor() != null)
            {
                DeletedNodeImpl ghost = new DeletedNodeImpl(lPath.getAVMStore().getAVMRepository().issueID(),
                                                    lPath.getAVMStore());
                AVMDAOs.Instance().fAVMNodeDAO.save(ghost);
                AVMDAOs.Instance().fAVMNodeDAO.flush();
                ghost.setAncestor(child);
                ghost.setDeletedType(child.getType());
                putChild(name, ghost);
            }
            else
            {
                AVMDAOs.Instance().fAVMNodeDAO.flush();
            }
        }
    }

    /**
     * Put a new child node into this directory.  No copy.
     * @param name The name of the child.
     * @param node The node to add.
     */
    public void putChild(String name, AVMNode node)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        ChildKey key = new ChildKey(this, name);
        ChildEntry existing = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (existing != null)
        {
            AVMDAOs.Instance().fChildEntryDAO.delete(existing);
        }
        ChildEntry entry = new ChildEntryImpl(key, node);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
        AVMDAOs.Instance().fChildEntryDAO.save(entry);
    }

    /**
     * Copy on write logic.
     * @param lPath The lookup path.
     * @return A brand new copied version.
     */
    public AVMNode copy(Lookup lPath)
    {
        DirectoryNode newMe = null;
        // In a layered context a copy on write creates a new 
        // layered directory.
        if (lPath.isLayered())
        {
            // Subtlety warning: This distinguishes the case of a 
            // Directory that was branched into the layer and one
            // that is indirectly seen in this layer.
            newMe = new LayeredDirectoryNodeImpl(this, lPath.getAVMStore(), lPath,
                                                 lPath.isInThisLayer());
            ((LayeredDirectoryNodeImpl)newMe).setLayerID(lPath.getTopLayer().getLayerID());
        }
        else
        {
            newMe = new PlainDirectoryNodeImpl(this, lPath.getAVMStore());
        }
        newMe.setAncestor(this);
        return newMe;
    }

    /**
     * Get the type of this node. 
     * @return The type of this node.
     */
    public int getType()
    {
        return AVMNodeType.PLAIN_DIRECTORY;
    }

    /**
     * Get a diagnostic String representation.
     * @param lPath The Lookup.
     * @return A diagnostic String representation.
     */
    public String toString(Lookup lPath)
    {
        return "[PD:" + getId() + "]";
    }    
 
    /**
     * Turn this into a primary indirection. This must be in a 
     * layered context.
     * @param lPath The Lookup.
     */
    public void turnPrimary(Lookup lPath)
    {
        assert false : "Should never happen.";
    }

    /**
     * Retarget this directory.  lPath must be in a layered context.
     * @param lPath The Lookup.
     * @param target The target path.
     */
    public void retarget(Lookup lPath, String target)
    {
        assert false : "Should never happen.";
    }

    /**
     * Get the descriptor for this node.
     * @param lPath The Lookup.
     * @param name The name of this node in this context.
     * @return A descriptor.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath, String name)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = lPath.getRepresentedPath();
        if (path.endsWith("/"))
        {
            path = path + name;
        }
        else
        {
            path = path + "/" + name;
        }
        return new AVMNodeDescriptor(path,
                                     name,
                                     AVMNodeType.PLAIN_DIRECTORY,
                                     attrs.getCreator(),
                                     attrs.getOwner(),
                                     attrs.getLastModifier(),
                                     attrs.getCreateDate(),
                                     attrs.getModDate(),
                                     attrs.getAccessDate(),
                                     getId(),
                                     getGuid(),
                                     getVersionID(),
                                     null,
                                     false,
                                     -1,
                                     false,
                                     -1,
                                     -1);
    }

    /**
     * Get the descriptor for this node.
     * @param lPath The Lookup.
     * @return A descriptor.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = lPath.getRepresentedPath();
        return new AVMNodeDescriptor(path,
                                     path.substring(path.lastIndexOf("/") + 1),
                                     AVMNodeType.PLAIN_DIRECTORY,
                                     attrs.getCreator(),
                                     attrs.getOwner(),
                                     attrs.getLastModifier(),
                                     attrs.getCreateDate(),
                                     attrs.getModDate(),
                                     attrs.getAccessDate(),
                                     getId(),
                                     getGuid(),
                                     getVersionID(),
                                     null,
                                     false,
                                     -1,
                                     false,
                                     -1, 
                                     -1);
    }

    /**
     * Get this node's descriptor.
     * @param parentPath The parent path.
     * @param name The name that we were looked up under.
     * @param parentIndirection The parent indirection.
     * @return This node's node descriptor
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = parentPath.endsWith("/") ? parentPath + name : parentPath + "/" + name;
        return new AVMNodeDescriptor(path,
                                     name,
                                     AVMNodeType.PLAIN_DIRECTORY,
                                     attrs.getCreator(),
                                     attrs.getOwner(),
                                     attrs.getLastModifier(),
                                     attrs.getCreateDate(),
                                     attrs.getModDate(),
                                     attrs.getAccessDate(),
                                     getId(),
                                     getGuid(),
                                     getVersionID(),
                                     null,
                                     false,
                                     -1,
                                     false,
                                     -1,
                                     -1);
    }
    
    /**
     * Link a node with the given id into this directory.
     * @param lPath The Lookup for this directory.
     * @param name The name to give the node.
     * @param toLink The node to link in.
     */
    public void link(Lookup lPath, String name, AVMNodeDescriptor toLink)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }        
        // Assure that the incoming node exists.
        AVMNode node = AVMDAOs.Instance().fAVMNodeDAO.getByID(toLink.getId());
        if (node == null)
        {
            throw new AVMNotFoundException("Node not found: " + toLink.getId());
        }
        if (node.getType() == AVMNodeType.LAYERED_DIRECTORY &&
            !((LayeredDirectoryNode)node).getPrimaryIndirection())
        {
            throw new AVMBadArgumentException("Non primary layered directories cannot be linked.");
        }
        // Check for an existing child by the given name.
        ChildKey key = new ChildKey(this, name);
        ChildEntry child = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (child != null)
        {
            if (child.getChild().getType() != AVMNodeType.DELETED_NODE)
            {
                // It's an error if there is a non DELETED_NODE child.
                throw new AVMExistsException(name + " exists.");
            }
            // Get rid of the DELETED_NODE child.
            AVMDAOs.Instance().fChildEntryDAO.delete(child);
            // Another &*#*&#$ flush.
            AVMDAOs.Instance().fAVMNodeDAO.flush();
        }
        // Make the new entry and save.
        ChildEntry newChild = new ChildEntryImpl(key, node);
        AVMDAOs.Instance().fChildEntryDAO.save(newChild);
    }
}

