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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
                             Set<QName> aspects,
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
        setAspects(new HashSet<QName>(aspects));
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
                                     getGuid(),
                                     getVersionID(),
                                     null,
                                     -1,
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
                                     getGuid(),
                                     getVersionID(),
                                     null,
                                     -1,
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
    public AVMNodeDescriptor getDescriptor(String parentPath, String name, String parentIndirection, int parentIndirectionVersion)
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
                                     getGuid(),
                                     getVersionID(),
                                     null,
                                     -1,
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
    public void setEncoding(String encoding)
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
    public void setMimeType(String mimeType)
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

