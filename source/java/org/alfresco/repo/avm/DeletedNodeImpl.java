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
    
    /**
     * This should never be called.
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
                                     getVersionID(),
                                     null,
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
                                     getVersionID(),
                                     null,
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
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection)
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
                                     getVersionID(),
                                     null,
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
