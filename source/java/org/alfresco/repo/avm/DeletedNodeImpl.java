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
     * For Hibernate's use.
     */
    protected DeletedNodeImpl()
    {
    }
    
    /**
     * Create a new one from scratch.
     * @param id The node id.
     * @param store The store it's being created in.
     */
    public DeletedNodeImpl(long id,
                           AVMStore store)
    {
        super(id, store);
    }
    
    public DeletedNodeImpl(DeletedNode other,
                           AVMStore store)
    {
        super(store.getAVMRepository().issueID(), store);
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
        copyProperties(other);
        copyAspects(other);
        copyACLs(other);        
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
        AVMNode newMe = new DeletedNodeImpl(this, lPath.getAVMStore());
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
                                     fDeletedType);
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
                                     fDeletedType);
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
                                     fDeletedType);
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
