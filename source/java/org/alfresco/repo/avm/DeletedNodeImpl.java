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
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;

/**
 * Place holder for a deleted node.
 * @author britt
 */
public class DeletedNodeImpl extends AVMNodeImpl implements DeletedNode
{
    private static final long serialVersionUID = 7283526790174482993L;
    
    /**
     * The type of node that this is a deleted node for.
     */
    private int fDeletedType;
    
    /**
     * Default constructor.
     */
    public DeletedNodeImpl()
    {
    }
    
    /**
     * Create a new one from scratch.
     * @param store The store it's being created in.
     */
    public DeletedNodeImpl(AVMStore store, Acl acl)
    {
        super(store);
        this.setAcl(acl);
    }
    
    public DeletedNodeImpl(DeletedNode other,
                           AVMStore store, Long parentAcl, ACLCopyMode mode)
    {
        super(store);
        
        setDeletedType(other.getDeletedType());
        
        copyACLs(other, parentAcl, mode);
        copyCreationAndOwnerBasicAttributes(other);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        
        copyProperties(other);
        copyAspects(other);
    }
    
    /**
     * Setter.   
     */
    public void setDeletedType(int type)
    {
        fDeletedType = type;
    }

    /**
     * Getter.
     */
    public int getDeletedType()
    {
        return fDeletedType;
    }
    
    // TODO What happens when this is called? Does it muck anything up.
    /**
     * This is only called rarely.
     */
    public AVMNode copy(Lookup lPath)
    {
        DirectoryNode dir = lPath.getCurrentNodeDirectory();
        Long parentAclId = null;
        if((dir != null) && (dir.getAcl() != null))
        {
            parentAclId = dir.getAcl().getId();
        }
        AVMNode newMe = new DeletedNodeImpl(this, lPath.getAVMStore(), parentAclId, ACLCopyMode.COPY);
        newMe.setAncestor(this);
        return newMe;
    }

    /**
     * Get a descriptor.
     * @param lPath The Lookup to this node's parent.
     * @param name The name of this node.
     * @return An AVMNodeDescriptor
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
                                     AVMNodeType.DELETED_NODE,
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
                                     -1,
                                     false,
                                     -1,
                                     false,
                                     -1,
                                     getDeletedType());
    }

    /**
     * Get a descriptor.
     * @param lPath The full Lookup to this.
     * @return An AVMNodeDescriptor.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = lPath.getRepresentedPath();
        return new AVMNodeDescriptor(path,
                                     path.substring(path.lastIndexOf("/") + 1),
                                     AVMNodeType.DELETED_NODE,
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
                                     -1,
                                     false,
                                     -1,
                                     false,
                                     -1,
                                     getDeletedType());
    }

    /**
     * Get a descriptor.
     * @param parentPath 
     * @param name
     * @param parentIndirection Ignored.
     * @return An AVMNodeDescriptor.
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection, int parentIndirectionVersion)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = parentPath.endsWith("/") ? parentPath + name : parentPath + "/" + name;
        return new AVMNodeDescriptor(path,
                                     name,
                                     AVMNodeType.DELETED_NODE,
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
                                     -1,
                                     false,
                                     -1,
                                     false,
                                     -1, 
                                     getDeletedType());
    }

    /**
     * Get the type of this node.
     * @return The AVMNodeType of this.
     */
    public int getType()
    {
        return AVMNodeType.DELETED_NODE;
    }

    /**
     * Get a descriptive string representation.
     * @param lPath The lookup we've been found through.
     * @return A String representation.
     */
    public String toString(Lookup lPath)
    {
        return "[DN:" + getId() + "]";
    }
}
