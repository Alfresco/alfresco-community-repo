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
 * A plain old file. Contains a Content object.
 * @author britt
 */
class PlainFileNodeImpl extends FileNodeImpl implements PlainFileNode
{
    static final long serialVersionUID = 8720376837929735294L;

    /**
     * The file content.
     */
    private FileContent fContent;
    
    /**
     * Default constructor.
     */
    protected PlainFileNodeImpl()
    {
    }

    /**
     * Make one from just a repository.
     * This is the constructor used when a brand new plain file is being made.
     * @param repos A Repository.
     * @param source A possibly null stream from which to get data.
     */
    public PlainFileNodeImpl(Repository repos)
    {
        super(repos.getSuperRepository().issueID(), repos);
        fContent = new FileContentImpl(SuperRepository.GetInstance().issueContentID());
        repos.getSuperRepository().getSession().save(this);
    }
    
    /**
     * Copy on write constructor.
     * @param other The node we are being copied from.
     * @param repos The Repository.
     */
    public PlainFileNodeImpl(PlainFileNode other,
                             Repository repos)
    {
        super(repos.getSuperRepository().issueID(), repos);
        fContent = other.getContent();
        fContent.setRefCount(fContent.getRefCount() + 1);
        repos.getSuperRepository().getSession().save(this);
    }

    /**
     * Constructor that takes a FileContent to share.
     * @param content The FileContent to share.
     * @param repos The Repository.
     */
    public PlainFileNodeImpl(FileContent content,
                             Repository repos,
                             BasicAttributes oAttrs)
    {
        super(repos.getSuperRepository().issueID(), repos);
        fContent = content;
        fContent.setRefCount(fContent.getRefCount() + 1);
        repos.getSuperRepository().getSession().save(this);
    }

    /**
     * Copy on write logic.
     * @param lPath The lookup path. 
     */
    public AVMNode copy(Lookup lPath)
    {
        PlainFileNodeImpl newMe = new PlainFileNodeImpl(this, lPath.getRepository());
        newMe.setAncestor(this);
        return newMe;
    }

    /**
     * Get the type of this node.
     * @return The type.
     */
    public int getType()
    {
        return AVMNodeType.PLAIN_FILE;
    }

    /**
     * Get content for reading.
     */
    public FileContent getContentForRead()
    {
        return fContent;
    }

    /**
     * Get content for writing.
     */
    public FileContent getContentForWrite()
    {
        if (fContent.getRefCount() > 1)
        {
            fContent = new FileContentImpl(fContent, SuperRepository.GetInstance().issueContentID());
        }
        return fContent;
    }

    /**
     * Get a diagnostic string representation.
     * @param lPath The Lookup.
     * @return A diagnostic String representation.
     */
//    @Override
    public String toString(Lookup lPath)
    {
        return "[PF:" + getId() + "]";
    }

    /**
     * Get the descriptor for this node.
     * @param The Lookup.
     * @return A descriptor.
     */
    public AVMNodeDescriptor getDescriptor(Lookup lPath)
    {
        BasicAttributes attrs = getBasicAttributes();
        return new AVMNodeDescriptor(lPath.getRepresentedPath(),
                                     AVMNodeType.PLAIN_FILE,
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
                                     getContentForRead().getLength());
    }

    /**
     * Get the descriptor for this.
     * @param parentPath The parent path.
     * @param name The name this was looked up with.
     * @param parentIndirection The parent indirection.
     * @return The descriptor for this.
     */
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection)
    {
        BasicAttributes attrs = getBasicAttributes();
        String path = parentPath.endsWith("/") ? parentPath + name : parentPath + "/" + name;
        return new AVMNodeDescriptor(path,
                                     AVMNodeType.PLAIN_FILE,
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
                                     getContentForRead().getLength());
    }

    /**
     * Get the file content of this node.
     * @return The file content object.
     */
    public FileContent getContent()
    {
        return fContent;
    }
    
    /**
     * Set the FileContent for this file.
     * @param content
     */
    protected void setContent(FileContent content)
    {
        fContent = content;
    }
}

