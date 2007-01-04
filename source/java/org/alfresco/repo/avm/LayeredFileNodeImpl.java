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

import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.ContentData;

/**
 * A LayeredFileNode behaves like a copy on write symlink.
 * @author britt
 */
class LayeredFileNodeImpl extends FileNodeImpl implements LayeredFileNode
{
    static final long serialVersionUID = 9208423010479156363L;

    /**
     * The indirection.
     */
    private String fIndirection;
    
    /**
     * Anonymous constructor.
     */
    protected LayeredFileNodeImpl()
    {
    }
    
    /**
     * Basically a copy constructor. Used when a branch is created
     * from a layered file.
     * @param other The file to make a copy of.
     * @param store The store that contains us.
     */
    public LayeredFileNodeImpl(LayeredFileNode other, AVMStore store)
    {
        super(store.getAVMRepository().issueID(), store);
        fIndirection = other.getIndirection();
        setVersionID(other.getVersionID() + 1);
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
        copyProperties(other);
        copyAspects(other);
        copyACLs(other);
    }

    /**
     * Make a brand new layered file node.
     * @param indirection The thing we point to.
     * @param store The store we belong to.
     */
    public LayeredFileNodeImpl(String indirection, AVMStore store)
    {
        super(store.getAVMRepository().issueID(), store);
        fIndirection = indirection;
        setVersionID(1);
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
    }
    
    /**
     * Copy on write logic.
     * @param lPath The path by which this was found.
     */
    public AVMNode copy(Lookup lPath)
    {
        // LayeredFileNodes are always copied.
        Lookup lookup = AVMRepository.GetInstance().lookup(-1, fIndirection, false);
        if (lookup == null)
        {
            throw new AVMException("Unbacked layered file node.");
        }
        AVMNode indirect = lookup.getCurrentNode();
        if (indirect.getType() != AVMNodeType.LAYERED_FILE &&
            indirect.getType() != AVMNodeType.PLAIN_FILE)
        {
            throw new AVMException("Unbacked layered file node.");
        }
        // TODO This doesn't look quite right.
        PlainFileNodeImpl newMe = new PlainFileNodeImpl(lPath.getAVMStore(),
                                                        getBasicAttributes(),
                                                        getContentData(lPath),
                                                        indirect.getProperties(),
                                                        AVMDAOs.Instance().fAVMAspectNameDAO.get(indirect),
                                                        indirect.getAcl(),
                                                        getVersionID());
        newMe.setAncestor(this);
        return newMe;
    }
    
    /**
     * Get the type of this node.
     * @return The type.
     */
    public int getType()
    {
        return AVMNodeType.LAYERED_FILE;
    }

    /**
     * Get the underlying path.
     * @param lookup The Lookup. (Unused here.)
     * @return The underlying path.
     */
    public String getUnderlying(Lookup lookup)
    {
        return fIndirection;
    }

    /**
     * Get a diagnostic String representation.
     * @param lPath The Lookup.
     * @return A diagnostic String representation.
     */
    public String toString(Lookup lPath)
    {
        return "[LF:" + getId() + ":" + fIndirection + "]";
    }

    /**
     * Get the descriptor for this node.
     * @param lPath The Lookup.
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
                                     AVMNodeType.LAYERED_FILE,
                                     attrs.getCreator(),
                                     attrs.getOwner(),
                                     attrs.getLastModifier(),
                                     attrs.getCreateDate(),
                                     attrs.getModDate(),
                                     attrs.getAccessDate(),
                                     getId(),
                                     getVersionID(),
                                     getUnderlying(lPath),
                                     false,
                                     -1,
                                     false,
                                     0,
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
                                     AVMNodeType.LAYERED_FILE,
                                     attrs.getCreator(),
                                     attrs.getOwner(),
                                     attrs.getLastModifier(),
                                     attrs.getCreateDate(),
                                     attrs.getModDate(),
                                     attrs.getAccessDate(),
                                     getId(),
                                     getVersionID(),
                                     getUnderlying(lPath),
                                     false,
                                     -1,
                                     false,
                                     0, 
                                     -1);
    }

    /**
     * Get the descriptor for this node.
     * @param parentPath The parent path.
     * @param name The name this was looked up with.
     * @param parentIndirection The parent indirection.
     * @return The descriptor.
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = parentPath.endsWith("/") ? parentPath + name : parentPath + "/" + name;
        return new AVMNodeDescriptor(path,
                                     name,
                                     AVMNodeType.LAYERED_FILE,
                                     attrs.getCreator(),
                                     attrs.getOwner(),
                                     attrs.getLastModifier(),
                                     attrs.getCreateDate(),
                                     attrs.getModDate(),
                                     attrs.getAccessDate(),
                                     getId(),
                                     getVersionID(),
                                     fIndirection,
                                     false,
                                     -1,
                                     false,
                                     0,
                                     -1);
    }

    /**
     * Get the indirection.
     * @return The indirection.
     */
    public String getIndirection()
    {
        return fIndirection;
    }
    
    /**
     * Set the indirection.
     * @param indirection
     */
    public void setIndirection(String indirection)
    {
        fIndirection = indirection;
    }

    /**
     * Set the ContentData for this file.
     * @param contentData The value to set.
     */
    public void setContentData(ContentData contentData)
    {
        throw new AVMException("Should not be called.");
    }
    
    // TODO The lPath argument is unnecessary.
    /**
     * Get the ContentData for this file.
     * @return The ContentData object for this file.
     */
    public ContentData getContentData(Lookup lPath)
    {
        Lookup lookup = lPath.getAVMStore().getAVMRepository().lookup(-1, getIndirection(), false);
        if (lookup == null)
        {
            throw new AVMException("Invalid target.");
        }
        AVMNode node = lookup.getCurrentNode();
        if (!(node instanceof FileNode))
        {
            throw new AVMException("Invalid target.");
        }
        FileNode file = (FileNode)node;
        return file.getContentData(lookup);
    }
}
