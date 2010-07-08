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

import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.ContentData;

/**
 * A LayeredFileNode behaves like a copy on write symlink.
 * 
 * @author britt
 */
public class LayeredFileNodeImpl extends FileNodeImpl implements LayeredFileNode
{
    static final long serialVersionUID = 9208423010479156363L;

    /**
     * The indirection.
     */
    private String fIndirection;
    
    /**
     * The indirection version.
     */
    private int fIndirectionVersion;
    
    /**
     * Default constructor.
     */
    public LayeredFileNodeImpl()
    {
    }
    
    /**
     * Basically a copy constructor. Used when a branch is created from a layered file.
     * 
     * @param other
     *            The file to make a copy of.
     * @param store
     *            The store that contains us.
     */
    public LayeredFileNodeImpl(LayeredFileNode other, AVMStore store, Long parentAcl, ACLCopyMode mode)
    {
        super(store);
        setIndirection(other.getIndirection());
        setIndirectionVersion(-1);
        setVersionID(other.getVersionID() + 1);
        
        copyACLs(other, parentAcl, mode);
        copyCreationAndOwnerBasicAttributes(other);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        
        copyProperties(other);
        copyAspects(other);
    }

    /**
     * Make a brand new layered file node.
     * 
     * @param indirection
     *            The thing we point to.
     * @param store
     *            The store we belong to.
     */
    public LayeredFileNodeImpl(String indirection, AVMStore store, Acl acl)
    {
        super(store);
        setIndirection(indirection);
        setIndirectionVersion(-1);
        setVersionID(1);
        
        if (acl != null)
        {
            setAcl(acl);
        }
        else
        {
            if (indirection != null)
            {
                Lookup lookup = AVMRepository.GetInstance().lookup(-1, indirection, false);
                if (lookup != null)
                {
                    AVMNode node = lookup.getCurrentNode();
                    if (node.getAcl() != null)
                    {
                        setAcl(AVMDAOs.Instance().fAclDAO.createLayeredAcl(node.getAcl().getId()));
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
            else
            {
                setAcl(AVMDAOs.Instance().fAclDAO.createLayeredAcl(null));
            }
        }
    }

    /**
     * Copy on write logic.
     * 
     * @param lPath
     *            The path by which this was found.
     */
    public AVMNode copy(Lookup lPath)
    {
        // LayeredFileNodes are always copied.
        Lookup lookup = AVMRepository.GetInstance().lookup(-1, getIndirection(), false);
        if (lookup == null)
        {
            throw new AVMException("Unbacked layered file node.");
        }
        AVMNode indirect = lookup.getCurrentNode();
        if (indirect.getType() != AVMNodeType.LAYERED_FILE && indirect.getType() != AVMNodeType.PLAIN_FILE)
        {
            throw new AVMException("Unbacked layered file node.");
        }
        DirectoryNode dir = lPath.getCurrentNodeDirectory();
        Acl parentAcl = null;
        if ((dir != null) && (dir.getAcl() != null))
        {
            parentAcl = dir.getAcl();
        }
        // TODO This doesn't look quite right.
        PlainFileNodeImpl newMe = new PlainFileNodeImpl(lPath.getAVMStore(), getBasicAttributes(), getContentData(lPath), indirect.getProperties(), indirect.getAspects(), indirect
                .getAcl(), getVersionID(), parentAcl, ACLCopyMode.COPY);
        newMe.setAncestor(this);
        return newMe;
    }

    /**
     * Get the type of this node.
     * 
     * @return The type.
     */
    public int getType()
    {
        return AVMNodeType.LAYERED_FILE;
    }

    /**
     * Get the underlying path.
     * 
     * @param lookup
     *            The Lookup. (Unused here.)
     * @return The underlying path.
     */
    public String getUnderlying(Lookup lookup)
    {
        return getIndirection();
    }

    /**
     * Get a diagnostic String representation.
     * 
     * @param lPath
     *            The Lookup.
     * @return A diagnostic String representation.
     */
    public String toString(Lookup lPath)
    {
        return "[LF:" + getId() + ":" + getIndirection() + "]";
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
        if (path.endsWith("/"))
        {
            path = path + name;
        }
        else
        {
            path = path + "/" + name;
        }
        return new AVMNodeDescriptor(path, name, AVMNodeType.LAYERED_FILE, attrs.getCreator(), attrs.getOwner(), attrs.getLastModifier(), attrs.getCreateDate(),
                attrs.getModDate(), attrs.getAccessDate(), getId(), getGuid(), getVersionID(), getUnderlying(lPath), getUnderlyingVersion(lPath), false, -1, false, 0, -1);
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
        return new AVMNodeDescriptor(path, path.substring(path.lastIndexOf("/") + 1), AVMNodeType.LAYERED_FILE, attrs.getCreator(), attrs.getOwner(), attrs.getLastModifier(),
                attrs.getCreateDate(), attrs.getModDate(), attrs.getAccessDate(), getId(), getGuid(), getVersionID(), getUnderlying(lPath), getUnderlyingVersion(lPath), false, -1,
                false, 0, -1);
    }

    /**
     * Get the descriptor for this node.
     * 
     * @param parentPath
     *            The parent path.
     * @param name
     *            The name this was looked up with.
     * @param parentIndirection
     *            The parent indirection.
     * @return The descriptor.
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection, int parentIndirectionVersion)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = parentPath.endsWith("/") ? parentPath + name : parentPath + "/" + name;
        return new AVMNodeDescriptor(path, name, AVMNodeType.LAYERED_FILE, attrs.getCreator(), attrs.getOwner(), attrs.getLastModifier(), attrs.getCreateDate(),
                attrs.getModDate(), attrs.getAccessDate(), getId(), getGuid(), getVersionID(), getIndirection(), getIndirectionVersion(), false, -1, false, 0, -1);
    }

    /**
     * Get the indirection.
     * 
     * @return The indirection.
     */
    public String getIndirection()
    {
        return fIndirection;
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
     * Set the ContentData for this file.
     * 
     * @param contentData
     *            The value to set.
     */
    public void setContentData(ContentData contentData)
    {
        throw new AVMException("Should not be called.");
    }

    // TODO The lPath argument is unnecessary.
    /**
     * Get the ContentData for this file.
     * 
     * @return The ContentData object for this file.
     */
    public ContentData getContentData(Lookup lPath)
    {
        Lookup lookup = null;
        if (lPath != null)
        {
            lookup = lPath.getAVMStore().getAVMRepository().lookup(getUnderlyingVersion(lPath), getIndirection(), false);
        }
        else
        {
            lookup = AVMRepository.GetInstance().lookup(getUnderlyingVersion(null), getIndirection(), false);
        }
        
        if (lookup == null)
        {
            throw new AVMException("Invalid target.");
        }
        AVMNode node = lookup.getCurrentNode();
        if (!(node instanceof FileNode))
        {
            throw new AVMException("Invalid target.");
        }
        FileNode file = (FileNode) node;
        return file.getContentData(lookup);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.avm.Layered#getUnderlyingVersion(org.alfresco.repo.avm.Lookup)
     */
    public int getUnderlyingVersion(Lookup lookup)
    {
        if ((lookup != null) && (lookup.getVersion() == -1))
        {
            return -1;
        }
        return getIndirectionVersion();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.avm.LayeredFileNode#getIndirectionVersion()
     */
    public Integer getIndirectionVersion()
    {
        return fIndirectionVersion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.avm.LayeredFileNode#setIndirectionVersion(int)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.avm.LayeredFileNode#copyLiterally(org.alfresco.repo.avm.Lookup)
     */
    public LayeredFileNode copyLiterally(Lookup lookup)
    {
        DirectoryNode dir = lookup.getCurrentNodeDirectory();
        Long parentAclId = null;
        if ((dir != null) && (dir.getAcl() != null))
        {
            parentAclId = dir.getAcl().getId();
        }
        return new LayeredFileNodeImpl(this, lookup.getAVMStore(), parentAclId, ACLCopyMode.COPY);
    }
}
