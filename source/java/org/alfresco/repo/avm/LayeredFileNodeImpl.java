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
     * @param repo The repository that contains us.
     */
    public LayeredFileNodeImpl(LayeredFileNode other, Repository repo)
    {
        super(repo.getSuperRepository().issueID(), repo);
        fIndirection = other.getIndirection();
        AVMContext.fgInstance.fAVMNodeDAO.save(this);
    }

    /**
     * Make a brand new layered file node.
     * @param indirection The thing we point to.
     * @param repo The repository we belong to.
     */
    public LayeredFileNodeImpl(String indirection, Repository repo)
    {
        super(repo.getSuperRepository().issueID(), repo);
        fIndirection = indirection;
        AVMContext.fgInstance.fAVMNodeDAO.save(this);
    }
    
    /**
     * Copy on write logic.
     * @param lPath The path by which this was found.
     */
    public AVMNode copy(Lookup lPath)
    {
        // LayeredFileNodes are always copied.
        Lookup lookup = SuperRepository.GetInstance().lookup(-1, fIndirection);
        AVMNode indirect = lookup.getCurrentNode();
        if (indirect.getType() != AVMNodeType.LAYERED_FILE &&
            indirect.getType() != AVMNodeType.PLAIN_FILE)
        {
            throw new AVMException("Unbacked layered file node.");
        }
        // This is a mildly dirty trick.  We use getContentForRead so as not to startle
        // the ultimate destination content into copying itself prematurely.
        FileContent content = ((FileNode)indirect).getContentForRead();
        PlainFileNodeImpl newMe = new PlainFileNodeImpl(content, lPath.getRepository(), getBasicAttributes());
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
     * Get the content of the specified version.
     * @return A FileContent object.
     */
    public FileContent getContentForRead()
    {
        Lookup lookup = SuperRepository.GetInstance().lookup(-1, fIndirection);
        AVMNode node = lookup.getCurrentNode();
        if (node.getType() != AVMNodeType.LAYERED_FILE &&
            node.getType() != AVMNodeType.PLAIN_FILE)
        {
            throw new AVMException("Missing Link.");
        }
        FileNode file = (FileNode)node;
        return file.getContentForRead();
    }

    /**
     * Get File Content for writing.  Should never be called.
     * @return Always null.
     */
    public FileContent getContentForWrite()
    {
        assert false : "Never happens";
        return null;
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
                                     0);
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
                                     0);
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
                                     0);
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
}
