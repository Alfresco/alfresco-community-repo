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

import java.util.List;
import java.util.Map;

import org.alfresco.repo.avm.util.RawServices;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.service.cmr.avm.AVMException;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;


/**
 * A plain old file. Contains a Content object.
 * @author britt
 */
class PlainFileNodeImpl extends FileNodeImpl implements PlainFileNode
{
    static final long serialVersionUID = 8720376837929735294L;

    /**
     * The Content URL.
     */
    private String fContentURL;
    
    /**
     * The Mime type.
     */
    private String fMimeType;
    
    /**
     * The character encoding.
     */
    private String fEncoding;
    
    /**
     * The length of the file.
     */
    private long fLength;
    
    /**
     * Default constructor.
     */
    protected PlainFileNodeImpl()
    {
    }

    /**
     * Make one from just an AVMStore.
     * This is the constructor used when a brand new plain file is being made.
     * @param store An AVMStore.
     */
    public PlainFileNodeImpl(AVMStore store)
    {
        super(store.getAVMRepository().issueID(), store);
        setVersionID(1);        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
    }
    
    /**
     * Copy on write constructor.
     * @param other The node we are being copied from.
     * @param store The AVMStore.
     */
    public PlainFileNodeImpl(PlainFileNode other,
                             AVMStore store)
    {
        super(store.getAVMRepository().issueID(), store);
        // The null is OK because the Lookup argument is only use by
        // layered files.
        setContentData(other.getContentData(null));
        setVersionID(other.getVersionID() + 1);
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
        copyProperties(other);
        copyAspects(other);
        copyACLs(other);
    }

    // TODO Is there a reason for passing all these parameters instead
    // of just the LayeredFileNode?
    /**
     * Construct a new one. This is called when a LayeredFileNode
     * is copied.
     * @param store
     * @param attrs
     * @param content
     */
    public PlainFileNodeImpl(AVMStore store,
                             BasicAttributes attrs,
                             ContentData content,
                             Map<QName, PropertyValue> props,
                             List<AVMAspectName> aspects,
                             DbAccessControlList acl,
                             int versionID)
    {
        super(store.getAVMRepository().issueID(), store);
        setContentData(content);
        setBasicAttributes(attrs);
        setVersionID(versionID + 1);
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        AVMDAOs.Instance().fAVMNodeDAO.flush();
        setProperties(props);
        for (AVMAspectName name : aspects)
        {
            AVMAspectName newName =
                new AVMAspectNameImpl();
            newName.setName(name.getName());
            newName.setNode(this);
            AVMDAOs.Instance().fAVMAspectNameDAO.save(newName);
        }
        if (acl != null)
        {
            setAcl(acl.getCopy());
        }
    }
    
    /**
     * Copy on write logic.
     * @param lPath The lookup path. 
     */
    public AVMNode copy(Lookup lPath)
    {
        PlainFileNodeImpl newMe = new PlainFileNodeImpl(this, lPath.getAVMStore());
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
                                     false,
                                     getLength(),
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
                                     false,
                                     getFileLength(),
                                     -1);
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
                                     name,
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
                                     false,
                                     getFileLength(),
                                     -1);
    }

    /**
     * Get the Content URL.
     * @return The content URL.
     */
    public String getContentURL()
    {
        return fContentURL;
    }

    /**
     * Set the Content URL.
     * @param contentURL
     */
    protected void setContentURL(String contentURL)
    {
        fContentURL = contentURL;
    }

    /**
     * Get the character encoding.
     * @return The encoding.
     */
    public String getEncoding()
    {
        return fEncoding;
    }

    /**
     * Set the character encoding.
     * @param encoding The encoding to set.
     */
    protected void setEncoding(String encoding)
    {
        fEncoding = encoding;
    }

    /**
     * Get the file length.
     * @return The file length or null if unknown.
     */
    public long getLength()
    {
        return fLength;
    }
    
    /**
     * Get the actual file length.
     * @return The actual file length;
     */
    private long getFileLength()
    {
        if (fContentURL == null)
        {
            return 0L;
        }
        ContentReader reader = RawServices.Instance().getContentStore().getReader(fContentURL);
        return reader.getSize();
    }

    /**
     * Set the file length.
     * @param length The length of the file.
     */
    protected void setLength(long length)
    {
        fLength = length;
    }

    /**
     * Get the mime type of the content.
     * @return The Mime Type of the content.
     */
    public String getMimeType()
    {
        return fMimeType;
    }

    /**
     * Set the Mime Type of the content.
     * @param mimeType The Mime Type to set.
     */
    protected void setMimeType(String mimeType)
    {
        fMimeType = mimeType;
    }
    
    /**
     * Set the ContentData for this file.
     * @param contentData The value to set.
     */
    public void setContentData(ContentData contentData)
    {
        fContentURL = contentData.getContentUrl();
        fMimeType = contentData.getMimetype();
        if (fMimeType == null)
        {
            throw new AVMException("Null mime type.");
        }
        fEncoding = contentData.getEncoding();
        fLength = contentData.getSize();
    }

    /**
     * Get the ContentData for this file.
     * @param lPath The lookup path used to get here.  Unused here.
     * @return The ContentData object for this file.
     */
    public ContentData getContentData(Lookup lPath)
    {
        return new ContentData(fContentURL, fMimeType, fLength, fEncoding);
    }
}

