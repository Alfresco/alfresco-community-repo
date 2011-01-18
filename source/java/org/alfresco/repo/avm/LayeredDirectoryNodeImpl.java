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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.alfresco.repo.avm.util.AVMUtil;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMExistsException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;

/**
 * A layered directory node. A layered directory node points at an underlying directory, which may or may not exist. The
 * visible contents of a layered directory node is the contents of the underlying node pointed at plus those nodes added
 * to or modified in the layered directory node minus those nodes which have been deleted in the layered directory node.
 *
 * @author britt
 */
public class LayeredDirectoryNodeImpl extends DirectoryNodeImpl implements LayeredDirectoryNode
{
    static final long serialVersionUID = 4623043057918181724L;

    /**
     * The layer id.
     */
    private long fLayerID;

    /**
     * The pointer to the underlying directory.
     */
    private String fIndirection;

    /**
     * Whether this is a primary indirection node.
     */
    private boolean fPrimaryIndirection;

    /**
     * Whether this is opaque.
     */
    private boolean fOpacity;

    /**
     * The indirection version.
     */
    private int fIndirectionVersion;
    
    /**
     * Default constructor.
     */
    public LayeredDirectoryNodeImpl()
    {
    }
    
    /**
     * Make a new one from a specified indirection path.
     *
     * @param indirection
     *            The indirection path to set.
     * @param store
     *            The store that owns this node.
     */
    public LayeredDirectoryNodeImpl(String indirection, AVMStore store, AVMNode toCopy, Long parentAcl, ACLCopyMode mode)
    {
        super(store);
        
        setLayerID(-1);
        setIndirection(indirection);
        setIndirectionVersion(-1);
        setPrimaryIndirection(true);
        setOpacity(false);
        
        if (toCopy != null)
        {
            setVersionID(toCopy.getVersionID() + 1);
            
            copyACLs(toCopy, parentAcl, mode);
            copyCreationAndOwnerBasicAttributes(toCopy);
        }
        else
        {
            setVersionID(1);
            
            if (indirection != null)
            {
                Lookup lookup = AVMRepository.GetInstance().lookupDirectory(-1, indirection);
                if (lookup != null)
                {
                    DirectoryNode dir = (DirectoryNode) lookup.getCurrentNode();
                    if (dir.getAcl() != null)
                    {
                        setAcl(AVMDAOs.Instance().fAclDAO.createLayeredAcl(dir.getAcl().getId()));
                    }
                    else
                    {
                        // TODO: Will not pick up changes if we start with no permission on the target node - may need
                        // to add
                        setAcl(AVMDAOs.Instance().fAclDAO.createLayeredAcl(null));
                    }
                }
                else
                {
                    setAcl(AVMDAOs.Instance().fAclDAO.createLayeredAcl(null));
                }
            }
            else
            {
                setAcl(AVMDAOs.Instance().fAclDAO.createLayeredAcl(null));
            }
        }
        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        
        if (toCopy != null)
        {
            copyProperties(toCopy);
            copyAspects(toCopy);
        }
    }

    /**
     * Kind of copy constructor, sort of.
     *
     * @param other
     *            The LayeredDirectoryNode we are copied from.
     * @param repos
     *            The AVMStore object we use.
     */
    public LayeredDirectoryNodeImpl(LayeredDirectoryNode other, AVMStore repos, Lookup lookup, boolean copyAll, Long parentAcl, ACLCopyMode mode)
    {
        super(repos);
        
        setLayerID(-1);
        setIndirection(other.getIndirection());
        setIndirectionVersion(-1);
        setPrimaryIndirection(other.getPrimaryIndirection());
        setOpacity(other.getOpacity());
        
        setVersionID(other.getVersionID() + 1);
        
        copyACLs(other, parentAcl, mode);
        copyCreationAndOwnerBasicAttributes(other);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        
        copyProperties(other);
        copyAspects(other);
        
        Map<String, AVMNode> children = null;
        if (copyAll)
        {
            children = other.getListing(lookup, true);
        }
        else
        {
            children = other.getListingDirect(lookup, true);
        }
        for (Map.Entry<String, AVMNode> child : children.entrySet())
        {
            ChildKey key = new ChildKey(this, child.getKey());
            ChildEntry entry = new ChildEntryImpl(key, child.getValue());
            AVMDAOs.Instance().fChildEntryDAO.save(entry);
        }
    }

    /**
     * Construct one from a PlainDirectoryNode. Called when a COW is performed in a layered context.
     *
     * @param other
     *            The PlainDirectoryNode.
     * @param store
     *            The AVMStore we should belong to.
     * @param lPath
     *            The Lookup object.
     */
    public LayeredDirectoryNodeImpl(PlainDirectoryNode other, AVMStore store, Lookup lPath, boolean copyContents, Long parentAcl, ACLCopyMode mode)
    {
        super(store);
        
        setLayerID(-1);
        setIndirection(null);
        setIndirectionVersion(-1);
        setPrimaryIndirection(false);
        setOpacity(false);
        
        setVersionID(other.getVersionID() + 1);
        
        copyACLs(other, parentAcl, mode);
        copyCreationAndOwnerBasicAttributes(other);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        
        copyProperties(other);
        copyAspects(other);
        
        if (copyContents)
        {
            for (ChildEntry child : AVMDAOs.Instance().fChildEntryDAO.getByParent(other, null))
            {
                ChildKey key = new ChildKey(this, child.getKey().getName());
                ChildEntryImpl newChild = new ChildEntryImpl(key, child.getChild());
                AVMDAOs.Instance().fChildEntryDAO.save(newChild);
            }
        }
    }

    /**
     * Create a new layered directory based on a directory we are being named from that is in not in the layer of the
     * source lookup.
     *
     * @param dir
     *            The directory
     * @param store
     *            The store
     * @param srcLookup
     *            The source lookup.
     * @param name
     *            The name of the target.
     */
    public LayeredDirectoryNodeImpl(DirectoryNode dir, AVMStore store, Lookup srcLookup, String name, Long inheritedAcl, ACLCopyMode mode)
    {
        super(store);
        
        setLayerID(-1);
        setIndirection(srcLookup.getIndirectionPath() + "/" + name);
        setIndirectionVersion(-1);
        setPrimaryIndirection(true);
        setOpacity(false);
        
        setVersionID(dir.getVersionID() + 1);
        
        copyACLs(dir, inheritedAcl, mode);
        copyCreationAndOwnerBasicAttributes(dir);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        
        copyProperties(dir);
        copyAspects(dir);
        
        Map<String, AVMNode> children = dir.getListing(srcLookup, true);
        for (Map.Entry<String, AVMNode> child : children.entrySet())
        {
            ChildKey key = new ChildKey(this, child.getKey());
            ChildEntry entry = new ChildEntryImpl(key, child.getValue());
            AVMDAOs.Instance().fChildEntryDAO.save(entry);
        }
    }

    /**
     * Is this a primary indirection node.
     *
     * @return Whether this is a primary indirection.
     */
    public boolean getPrimaryIndirection()
    {
        return fPrimaryIndirection;
    }

    /**
     * Set the primary indirection state of this.
     *
     * @param has
     *            Whether this is a primary indirection node.
     */
    public void setPrimaryIndirection(boolean has)
    {
        fPrimaryIndirection = has;
    }

    /**
     * Get the indirection path.
     *
     * @return The indirection path.
     */
    public String getIndirection()
    {
        return fIndirection;
    }

    /**
     * Get the underlying path in the Lookup's context.
     *
     * @param lPath
     *            The Lookup.
     * @return The underlying path.
     */
    public String getUnderlying(Lookup lPath)
    {
        if (getPrimaryIndirection())
        {
            return getIndirection();
        }
        return lPath.getCurrentIndirection();
    }

    /**
     * Get the underlying version in the lookup path context.
     *
     * @param lPath
     *            The Lookup.
     * @return The effective underlying version.
     */
    public int getUnderlyingVersion(Lookup lPath)
    {
        if (lPath.getVersion() == -1)
        {
            return -1;
        }
        if (getPrimaryIndirection())
        {
            return getIndirectionVersion();
        }
        return lPath.getCurrentIndirectionVersion();
    }

    /**
     * Get the layer id.
     *
     * @return The layer id.
     */
    public long getLayerID()
    {
        return fLayerID;
    }

    /**
     * Set the layer id.
     *
     * @param id
     *            The id to set.
     */
    public void setLayerID(long id)
    {
        fLayerID = id;
    }

    /**
     * Copy on write logic.
     *
     * @param lPath
     * @return The copy or null.
     */
    public AVMNode copy(Lookup lPath)
    {
        DirectoryNode dir = lPath.getCurrentNodeDirectory();
        Long parentAclId = null;
        if ((dir != null) && (dir.getAcl() != null))
        {
            parentAclId = dir.getAcl().getId();
        }
        // Capture the store.
        AVMStore store = lPath.getAVMStore();
        LayeredDirectoryNodeImpl newMe = null;
        if (!lPath.isInThisLayer())
        {
            // This means that this is being seen indirectly through the topmost
            // layer. The following creates a node that will inherit its
            // indirection from its parent.
            newMe = new LayeredDirectoryNodeImpl((String) null, store, this, parentAclId, ACLCopyMode.COW);
            newMe.setPrimaryIndirection(false);
            newMe.setLayerID(lPath.getTopLayer().getLayerID());
        }
        else
        {
            // A simple copy is made.
            newMe = new LayeredDirectoryNodeImpl(this, store, lPath, false, parentAclId, ACLCopyMode.COW);
            newMe.setLayerID(getLayerID());
        }
        newMe.setAncestor(this);
        
        AVMDAOs.Instance().fAVMNodeDAO.update(newMe);
        
        return newMe;
    }

    /**
     * Insert a child node without COW.
     *
     * @param name
     *            The name to give the child.
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
        AVMDAOs.Instance().fChildEntryDAO.save(entry);
    }
    
    /**
     * Get a listing of the virtual contents of this directory.
     *
     * @param lPath
     *            The Lookup.
     * @return A Map from names to nodes. This is a sorted Map.
     */
    public Map<String, AVMNode> getListing(Lookup lPath, boolean includeDeleted)
    {
        return getListing(lPath, null, includeDeleted);
    }
    
    /**
     * Get a listing of the virtual contents of this directory.
     *
     * @param lPath
     *            The Lookup.
     * @return A Map from names to nodes. This is a sorted Map.
     */
    public Map<String, AVMNode> getListing(Lookup lPath, String childNamePattern, boolean includeDeleted)
    {
        Map<String, AVMNode> listing = new HashMap<String, AVMNode>();
        Map<String, String> baseLowerKeyName = new HashMap<String, String>();
        
        if (!getOpacity())
        {
            // If we are not opaque, get the underlying base listing (from the thing we indirect to)
            Lookup lookup = AVMRepository.GetInstance().lookupDirectory(getUnderlyingVersion(lPath), getUnderlying(lPath));
            if (lookup != null)
            {
                DirectoryNode dir = (DirectoryNode) lookup.getCurrentNode();
                Map<String, AVMNode> underListing = dir.getListing(lookup, childNamePattern, includeDeleted);
                for (Map.Entry<String, AVMNode> entry : underListing.entrySet())
                {
                    if (entry.getValue().getType() == AVMNodeType.LAYERED_DIRECTORY ||
                        entry.getValue().getType() == AVMNodeType.PLAIN_DIRECTORY)
                    {
                        if (!AVMRepository.GetInstance().can(lookup.getAVMStore(), entry.getValue(), PermissionService.READ_CHILDREN, false))
                        {
                            continue;
                        }
                    }
                    listing.put(entry.getKey(), entry.getValue());
                    baseLowerKeyName.put(entry.getKey().toLowerCase(), entry.getKey());
                }
            }
        }
        for (ChildEntry entry : AVMDAOs.Instance().fChildEntryDAO.getByParent(this, childNamePattern))
        {
            if (entry.getChild().getType() == AVMNodeType.LAYERED_DIRECTORY ||
                entry.getChild().getType() == AVMNodeType.PLAIN_DIRECTORY)
            {
                if (!AVMRepository.GetInstance().can(lPath.getAVMStore(), entry.getChild(), PermissionService.READ_CHILDREN, lPath.getDirectlyContained()))
                {
                    continue;
                }
            }
            if (!includeDeleted && entry.getChild().getType() == AVMNodeType.DELETED_NODE)
            {
                listing.remove(entry.getKey().getName());
            }
            else
            {
                String keyName = baseLowerKeyName.get(entry.getKey().getName().toLowerCase());
                if (keyName != null)
                {
                    // specific rename 'case' only
                    listing.remove(keyName);
                }
                
                listing.put(entry.getKey().getName(), entry.getChild());
            }
        }
        return listing;
    }

    /**
     * Get a listing of the nodes directly contained by a directory.
     *
     * @param lPath
     *            The Lookup to this directory.
     * @return A Map of names to nodes.
     */
    public Map<String, AVMNode> getListingDirect(Lookup lPath, boolean includeDeleted)
    {
        Map<String, AVMNode> listing = new HashMap<String, AVMNode>();
        for (ChildEntry entry : AVMDAOs.Instance().fChildEntryDAO.getByParent(this, null))
        {
            if (entry.getChild().getType() == AVMNodeType.LAYERED_DIRECTORY ||
                entry.getChild().getType() == AVMNodeType.PLAIN_DIRECTORY)
            {
                if (!AVMRepository.GetInstance().can(lPath != null ? lPath.getAVMStore() : null, 
                		                             entry.getChild(), PermissionService.READ_CHILDREN, 
                		                             lPath != null ? lPath.getDirectlyContained() : false))
                {
                    continue;
                }
            }
            if (includeDeleted || entry.getChild().getType() != AVMNodeType.DELETED_NODE)
            {
                listing.put(entry.getKey().getName(), entry.getChild());
            }
        }
        return listing;
    }

    /**
     * Get the direct contents of this directory.
     *
     * @param dir
     *            The descriptor that describes us.
     * @param includeDeleted
     *            Whether to inlude deleted nodes.
     * @return A Map of Strings to descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListingDirect(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        List<ChildEntry> children = AVMDAOs.Instance().fChildEntryDAO.getByParent(this, null);
        SortedMap<String, AVMNodeDescriptor> listing = new TreeMap<String, AVMNodeDescriptor>(String.CASE_INSENSITIVE_ORDER);
        for (ChildEntry child : children)
        {
            AVMNode childNode = child.getChild();
            if (childNode.getType() == AVMNodeType.LAYERED_DIRECTORY ||
                childNode.getType() == AVMNodeType.PLAIN_DIRECTORY)
            {
                if (!AVMRepository.GetInstance().can(null, childNode, PermissionService.READ_CHILDREN, false))
                {
                    continue;
                }
            }
            if (!includeDeleted && childNode.getType() == AVMNodeType.DELETED_NODE)
            {
                continue;
            }
            AVMNodeDescriptor childDesc = childNode.getDescriptor(dir.getPath(), child.getKey().getName(), dir.getIndirection(), dir.getIndirectionVersion());
            listing.put(child.getKey().getName(), childDesc);
        }
        return listing;
    }

    /**
     * Get a listing from a directory node descriptor.
     *
     * @param dir
     *            The directory node descriptor.
     * @param includeDeleted
     *            Should DeletedNodes be shown.
     * @return A Map of names to node descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(AVMNodeDescriptor dir, boolean includeDeleted)
    {
        return getListing(dir, null, includeDeleted);
    }
    
    /**
     * Get a listing from a directory node descriptor.
     *
     * @param dir
     *            The directory node descriptor.
     * @param childNamePattern
     *            Pattern to match for child names - may be null
     * @param includeDeleted
     *            Should DeletedNodes be shown.
     * @return A Map of names to node descriptors.
     */
    public SortedMap<String, AVMNodeDescriptor> getListing(AVMNodeDescriptor dir, String childNamePattern, boolean includeDeleted)
    {
        if (dir.getPath() == null || dir.getIndirection() == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        SortedMap<String, AVMNodeDescriptor> listing = new TreeMap<String, AVMNodeDescriptor>(String.CASE_INSENSITIVE_ORDER);
        Map<String, String> baseLowerKeyName = new HashMap<String, String>();
        
        if (!getOpacity())
        {
            // If we are not opaque, get the underlying base listing (from the thing we indirect to)
            Lookup lookup = AVMRepository.GetInstance().lookupDirectory(dir.getIndirectionVersion(), dir.getIndirection());
            if (lookup != null)
            {
                DirectoryNode dirNode = (DirectoryNode) lookup.getCurrentNode();
                Map<String, AVMNode> underListing = dirNode.getListing(lookup, childNamePattern, includeDeleted);
                for (Map.Entry<String, AVMNode> entry : underListing.entrySet())
                {
                    if (entry.getValue().getType() == AVMNodeType.LAYERED_DIRECTORY ||
                        entry.getValue().getType() == AVMNodeType.PLAIN_DIRECTORY)
                    {
                        if (!AVMRepository.GetInstance().can(null, entry.getValue(), PermissionService.READ_CHILDREN, false))
                        {
                            continue;
                        }
                    }
                    listing.put(entry.getKey(),
                                    entry.getValue().getDescriptor(dir.getPath(), entry.getKey(),
                                                                   lookup.getCurrentIndirection(),
                                                                   lookup.getCurrentIndirectionVersion()));
                    
                    baseLowerKeyName.put(entry.getKey().toLowerCase(), entry.getKey());
                }
            }
        }
        for (ChildEntry entry : AVMDAOs.Instance().fChildEntryDAO.getByParent(this, childNamePattern))
        {
            if (entry.getChild().getType() == AVMNodeType.LAYERED_DIRECTORY ||
                entry.getChild().getType() == AVMNodeType.PLAIN_DIRECTORY)
            {
                if (!AVMRepository.GetInstance().can(null, entry.getChild(), PermissionService.READ_CHILDREN, false))
                {
                    continue;
                }
            }
            if (!includeDeleted && entry.getChild().getType() == AVMNodeType.DELETED_NODE)
            {
                listing.remove(entry.getKey().getName());
            }
            else
            {
                String keyName = baseLowerKeyName.get(entry.getKey().getName().toLowerCase());
                if (keyName != null)
                {
                    // specific rename 'case' only
                    listing.remove(keyName);
                }
                
                listing.put(entry.getKey().getName(), entry.getChild()
                        .getDescriptor(dir.getPath(), entry.getKey().getName(), dir.getIndirection(), dir.getIndirectionVersion()));
            }
        }
        return listing;
    }
    
    /**
     * Get the names of nodes deleted in this directory.
     *
     * @return A List of names.
     */
    public List<String> getDeletedNames()
    {
        List<ChildEntry> children = AVMDAOs.Instance().fChildEntryDAO.getByParent(this, null);
        List<String> listing = new ArrayList<String>();
        for (ChildEntry entry : children)
        {
            if (entry.getChild().getType() == AVMNodeType.DELETED_NODE)
            {
                listing.add(entry.getKey().getName());
            }
        }
        return listing;
    }
    
    /**
     * Lookup a child entry by name.
     *
     * @param lPath
     *            The Lookup.
     * @param name
     *            The name we are looking.
     * @param version
     *            The version in which we are looking.
     * @param write
     *            Whether this lookup is occurring in a write context.
     * @return The child entry or null if not found.
     */
    public Pair<ChildEntry, Boolean> lookupChildEntry(Lookup lPath, String name, boolean includeDeleted)
    {
        ChildKey key = new ChildKey(this, name);
        ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (entry != null)
        {
            if (!includeDeleted && entry.getChild().getType() == AVMNodeType.DELETED_NODE)
            {
                return null;
            }
            Pair<ChildEntry, Boolean> result = new Pair<ChildEntry, Boolean>(entry, true);
            return result;
        }
        // Don't check our underlying directory if we are opaque.
        if (getOpacity())
        {
            return null;
        }
        // Not here so check our indirection.
        Lookup lookup = AVMRepository.GetInstance().lookupDirectory(getUnderlyingVersion(lPath), getUnderlying(lPath));
        if (lookup != null)
        {
            DirectoryNode dir = (DirectoryNode) lookup.getCurrentNode();
            Pair<ChildEntry, Boolean> retVal = dir.lookupChildEntry(lookup, name, includeDeleted);
            if (retVal != null)
            {
                retVal.setSecond(false);
            }
            lPath.setFinalStore(lookup.getFinalStore());
            return retVal;
        }
        else
        {
            return null;
        }
    }

    /**
     * Lookup a child using a node descriptor as context.
     *
     * @param mine
     *            The node descriptor for this,
     * @param name
     *            The name to lookup,
     * @return The node descriptor.
     */
    public AVMNodeDescriptor lookupChild(AVMNodeDescriptor mine, String name, boolean includeDeleted)
    {
        if (mine.getPath() == null || mine.getIndirection() == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        ChildKey key = new ChildKey(this, name);
        ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (entry != null)
        {
            if (!includeDeleted && entry.getChild().getType() == AVMNodeType.DELETED_NODE)
            {
                return null;
            }
            AVMNodeDescriptor desc = entry.getChild().getDescriptor(mine.getPath(), name, mine.getIndirection(), mine.getIndirectionVersion());
            return desc;
        }
        // Don't check our underlying directory if we are opaque.
        if (getOpacity())
        {
            return null;
        }
        // Not here so check our indirection.
        Lookup lookup = AVMRepository.GetInstance().lookupDirectory(mine.getIndirectionVersion(), mine.getIndirection());
        if (lookup != null)
        {
            DirectoryNode dir = (DirectoryNode) lookup.getCurrentNode();
            Pair<AVMNode, Boolean> child = dir.lookupChild(lookup, name, includeDeleted);
            if (child == null)
            {
                return null;
            }
            AVMNodeDescriptor desc = child.getFirst().getDescriptor(lookup);
            return desc;
        }
        else
        {
            return null;
        }
    }

    /**
     * Directly remove a child. Do not COW. Do not pass go etc.
     *
     * @param lPath
     *            The lookup that arrived at this.
     * @param name
     *            The name of the child to remove.
     */
    public void removeChild(Lookup lPath, String name)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        ChildKey key = new ChildKey(this, name);
        ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
        AVMNode child = null;
        boolean indirect = false;
        if (entry != null)
        {
            child = entry.getChild();
            if (child.getType() == AVMNodeType.DELETED_NODE)
            {
                return;
            }
            AVMDAOs.Instance().fChildEntryDAO.delete(entry);
            
            Lookup lookup = AVMRepository.GetInstance().lookup(-1, AVMUtil.extendAVMPath(lPath.getRepresentedPath(), name), true);
            
            if (((AVMNodeType.PLAIN_FILE == child.getType()) || 
                 (AVMNodeType.LAYERED_DIRECTORY == child.getType()) || 
                 (AVMNodeType.PLAIN_DIRECTORY == child.getType())) && 
                ((lookup == null) || (lookup.getIndirectionPath() == null)) && (lookupChild(lPath, name, true) == null))
            {
                return;
            }
        }
        else
        {
            Pair<AVMNode, Boolean> temp = lookupChild(lPath, name, false);
            if (temp == null)
            {
                child = null;
            }
            else
            {
                child = temp.getFirst();
            }
            indirect = true;
        }
        if (child != null && (indirect || child.getStoreNew() == null || child.getAncestor() != null))
        {
            DeletedNodeImpl ghost = new DeletedNodeImpl(lPath.getAVMStore(), child.getAcl());
            AVMDAOs.Instance().fAVMNodeDAO.save(ghost);
            
            ghost.setAncestor(child);
            ghost.setDeletedType(child.getType());
            ghost.copyCreationAndOwnerBasicAttributes(child);
            ghost.copyAspects(child);
            ghost.copyProperties(child);
            
            AVMDAOs.Instance().fAVMNodeDAO.update(ghost);
            
            this.putChild(name, ghost);
        }
    }

    /**
     * Get the type of this node.
     *
     * @return The type of this node.
     */
    public int getType()
    {
        return AVMNodeType.LAYERED_DIRECTORY;
    }

    /**
     * For diagnostics. Get a String representation.
     *
     * @param lPath
     *            The Lookup.
     * @return A String representation.
     */
    public String toString(Lookup lPath)
    {
        return "[LD:" + getId() + (lPath != null ? ":" + getUnderlying(lPath) : "") + "]";
    }
    
    /**
     * Set the primary indirection. No COW. Cascade resetting of acls also does not COW
     *
     * @param path
     *            The indirection path.
     */
    public void rawSetPrimary(Lookup lPath, String path)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        
        setIndirection(path);
        setPrimaryIndirection(true);
        
        // Need to change the permission we point to ....
        if (getIndirection() != null)
        {
            if ((getAcl() == null) || (getAcl().getAclType() == ACLType.LAYERED))
            {
                Acl acl = null;
                Lookup lookup = AVMRepository.GetInstance().lookupDirectory(-1, getIndirection());
                if (lookup != null)
                {
                    DirectoryNode dir = (DirectoryNode) lookup.getCurrentNode();
                    if (dir.getAcl() != null)
                    {
                        if (getAcl() == null)
                        {
                            acl = AVMDAOs.Instance().fAclDAO.createLayeredAcl(dir.getAcl().getId());
                        }
                        else
                        {
                            
                            acl = AVMDAOs.Instance().fAclDAO.getAclCopy(getAcl().getId(), dir.getAcl().getId(), ACLCopyMode.REDIRECT);
                        }
                    }
                    else
                    {
                        if (getAcl() == null)
                        {
                            acl = AVMDAOs.Instance().fAclDAO.createLayeredAcl(null);
                        }
                        else
                        {
                            acl = AVMDAOs.Instance().fAclDAO.getAclCopy(getAcl().getId(), null, ACLCopyMode.REDIRECT);
                        }
                    }
                }
                setAclAndInherit(this, acl, null);
            }
        }
        else
        {
            if (getAcl().getAclType() == ACLType.LAYERED)
            {
                Acl acl = null;
                if (getAcl() == null)
                {
                    acl = AVMDAOs.Instance().fAclDAO.createLayeredAcl(null);
                }
                else
                {
                    acl = AVMDAOs.Instance().fAclDAO.getAclCopy(getAcl().getId(), null, ACLCopyMode.REDIRECT);
                }
                setAclAndInherit(this, acl, null);
            }
        }
    }

    public void setAclAndInherit(LayeredDirectoryNode layeredDirectory, Acl acl, String name)
    {
        // Note ACLS may COW on next ACL change
        layeredDirectory.setAcl(acl);
        
        AVMDAOs.Instance().fAVMNodeDAO.update(layeredDirectory);
        
        Map<String, AVMNode> directChildren = layeredDirectory.getListingDirect((Lookup) null, true);
        for (String key : directChildren.keySet())
        {
            AVMNode node = directChildren.get(key);

            if (node instanceof LayeredDirectoryNode)
            {
                LayeredDirectoryNode childNode = (LayeredDirectoryNode) node;
                Acl currentAcl = node.getAcl();
                if (currentAcl == null)
                {
                    if (acl == null)
                    {
                        childNode.setAclAndInherit(childNode, null, key);
                    }
                    else
                    {
                        childNode.setAclAndInherit(childNode, AVMDAOs.Instance().fAclDAO.getAclCopy(acl.getId(), acl.getId(), ACLCopyMode.REDIRECT), key);
                    }
                }
                else
                {
                    if (acl == null)
                    {
                        childNode.setAclAndInherit(childNode, currentAcl, key);
                    }
                    else
                    {
                        childNode.setAclAndInherit(childNode, AVMDAOs.Instance().fAclDAO.getAclCopy(currentAcl.getId(), acl.getId(), ACLCopyMode.REDIRECT), key);
                    }
                }
            }
            else if (node instanceof PlainFileNode)
            {
                PlainFileNode childNode = (PlainFileNode) node;
                Acl currentAcl = node.getAcl();
                if (currentAcl == null)
                {
                    if (acl == null)
                    {
                        childNode.setAcl(null);
                    }
                    else
                    {
                        childNode.setAcl(AVMDAOs.Instance().fAclDAO.getAclCopy(acl.getId(), acl.getId(), ACLCopyMode.REDIRECT));
                    }
                }
                else
                {
                    if (acl == null)
                    {
                        childNode.setAcl(currentAcl);
                    }
                    else
                    {
                        childNode.setAcl(AVMDAOs.Instance().fAclDAO.getAclCopy(currentAcl.getId(), acl.getId(), ACLCopyMode.REDIRECT));
                    }
                }
                
                AVMDAOs.Instance().fAVMNodeDAO.update(childNode);
            }
        }
    }

    /**
     * Make this node become a primary indirection. COW.
     *
     * @param lPath
     *            The Lookup.
     */
    public void turnPrimary(Lookup lPath)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        String path = lPath.getCurrentIndirection();
        rawSetPrimary(lPath, path);
    }

    /**
     * Make this point at a new target.
     *
     * @param lPath
     *            The Lookup.
     */
    public void retarget(Lookup lPath, String target)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        rawSetPrimary(lPath, target);
    }

    /**
     * Let anything behind name in this become visible.
     *
     * @param lPath
     *            The Lookup.
     * @param name
     *            The name to uncover.
     */
    public void uncover(Lookup lPath, String name)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        ChildKey key = new ChildKey(this, name);
        ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (entry.getChild().getType() != AVMNodeType.DELETED_NODE)
        {
            throw new AVMException("One can only uncover deleted nodes.");
        }
        if (entry != null)
        {
            AVMDAOs.Instance().fChildEntryDAO.delete(entry);
        }
    }

    /**
     * Get the descriptor for this node.
     *
     * @param lPath
     *            The Lookup.
     * @return A descriptor.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath, String name)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = lPath.getRepresentedPath();
        path = AVMNodeConverter.ExtendAVMPath(path, name);
        
        int indirectionVersion = getUnderlyingVersion(lPath);
        String indirect = null;
        if (getPrimaryIndirection())
        {
            indirect = getIndirection();
        }
        else
        {
            indirect = AVMNodeConverter.ExtendAVMPath(lPath.getCurrentIndirection(), name);
        }
        
        return new AVMNodeDescriptor(path, name, AVMNodeType.LAYERED_DIRECTORY, attrs.getCreator(), attrs.getOwner(), attrs.getLastModifier(), attrs.getCreateDate(), attrs
                .getModDate(), attrs.getAccessDate(), getId(), getGuid(), getVersionID(), indirect, indirectionVersion, getPrimaryIndirection(), getLayerID(), getOpacity(), -1, -1);
    }

    /**
     * Get the descriptor for this node.
     *
     * @param lPath
     *            The Lookup.
     * @return A descriptor.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = lPath.getRepresentedPath();
        String name = path.substring(path.lastIndexOf("/") + 1);
        return new AVMNodeDescriptor(path, name, AVMNodeType.LAYERED_DIRECTORY, attrs.getCreator(), attrs.getOwner(), attrs.getLastModifier(), attrs.getCreateDate(), attrs
                .getModDate(), attrs.getAccessDate(), getId(), getGuid(), getVersionID(), getUnderlying(lPath), getUnderlyingVersion(lPath), getPrimaryIndirection(), getLayerID(),
                getOpacity(), -1, -1);
    }

    /**
     * Get a descriptor for this.
     *
     * @param parentPath
     *            The parent path.
     * @param name
     *            The name this was looked up with.
     * @param parentIndirection
     *            The indirection of the parent.
     * @return The descriptor.
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection, int parentIndirectionVersion)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = parentPath.endsWith("/") ? parentPath + name : parentPath + "/" + name;
        String indirection = null;
        int indirectionVersion = -1;
        if (getPrimaryIndirection())
        {
            indirection = getIndirection();
            indirectionVersion = getIndirectionVersion();
        }
        else
        {
            indirection = parentIndirection.endsWith("/") ? parentIndirection + name : parentIndirection + "/" + name;
            indirectionVersion = parentIndirectionVersion;
        }
        return new AVMNodeDescriptor(path, name, AVMNodeType.LAYERED_DIRECTORY, attrs.getCreator(), attrs.getOwner(), attrs.getLastModifier(), attrs.getCreateDate(), attrs
                .getModDate(), attrs.getAccessDate(), getId(), getGuid(), getVersionID(), indirection, indirectionVersion, getPrimaryIndirection(), getLayerID(), getOpacity(), -1, -1);
    }

    /**
     * Set the indirection.
     *
     * @param indirection
     */
    public void setIndirection(String indirection)
    {
        fIndirection = indirection;
    }

    /**
     * Does nothing because LayeredDirectoryNodes can't be roots.
     *
     * @param isRoot
     */
    public void setIsRoot(boolean isRoot)
    {
    }

    /**
     * Get the opacity of this.
     *
     * @return The opacity.
     */
    public boolean getOpacity()
    {
        return fOpacity;
    }

    /**
     * Set the opacity of this, ie, whether it blocks things normally seen through its indirection.
     *
     * @param opacity
     */
    public void setOpacity(boolean opacity)
    {
        fOpacity = opacity;
    }

    /**
     * Link a node with the given id into this directory.
     *
     * @param lPath
     *            The Lookup for this.
     * @param name
     *            The name to give the node.
     * @param toLink
     *            The node to link in.
     */
    public void link(Lookup lPath, String name, AVMNodeDescriptor toLink)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        AVMNode node = AVMDAOs.Instance().fAVMNodeDAO.getByID(toLink.getId());
        if (node == null)
        {
            throw new AVMNotFoundException("Not Found: " + toLink.getId());
        }
        if (node.getType() == AVMNodeType.LAYERED_DIRECTORY && !((LayeredDirectoryNode) node).getPrimaryIndirection())
        {
            throw new AVMBadArgumentException("Non primary layered directories cannot be linked.");
        }
        // Look for an existing child of that name.
        Pair<AVMNode, Boolean> temp = lookupChild(lPath, name, true);
        AVMNode existing = (temp == null) ? null : temp.getFirst();
        ChildKey key = new ChildKey(this, name);
        if (existing != null)
        {
            if (existing.getType() != AVMNodeType.DELETED_NODE)
            {
                // If the existing child is not a DELETED_NODE it's an error.
                throw new AVMExistsException(name + " exists.");
            }
            // Only if the existing DELETED_NODE child exists directly in this
            // directory do we delete it.
            if (directlyContains(existing))
            {
                ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
                AVMDAOs.Instance().fChildEntryDAO.delete(entry);
            }
        }
        // Make the new ChildEntry and save.
        ChildEntry newChild = new ChildEntryImpl(key, node);
        AVMDAOs.Instance().fChildEntryDAO.save(newChild);
    }

    /**
     * Remove name without leaving behind a deleted node.
     *
     * @param name
     *            The name of the child to flatten.
     */
    public void flatten(String name)
    {
        if (DEBUG)
        {
            checkReadOnly();
        }
        ChildKey key = new ChildKey(this, name);
        ChildEntry entry = AVMDAOs.Instance().fChildEntryDAO.get(key);
        if (entry != null)
        {
            AVMDAOs.Instance().fChildEntryDAO.delete(entry);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.alfresco.repo.avm.LayeredDirectoryNode#setIndirectionVersion(int)
     */
    public void setIndirectionVersion(Integer version)
    {
        if (version == null)
        {
            fIndirectionVersion = -1;
        }
        else
        {
            fIndirectionVersion = version;
        }
    }

    /**
     * Get the indirection version.
     *
     * @return The indirection version.
     */
    public Integer getIndirectionVersion()
    {
        return fIndirectionVersion;
    }

}
