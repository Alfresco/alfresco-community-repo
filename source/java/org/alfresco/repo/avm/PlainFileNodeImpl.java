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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.QName;

/**
 * A plain old file. Contains a Content object.
 * @author britt
 */
public class PlainFileNodeImpl extends FileNodeImpl implements PlainFileNode
{
    static final long serialVersionUID = 8720376837929735294L;
    
    private static final String PREFIX_CONTENT_DATA_ID = "id:";
    private static final String SUFFIX_CONTENT_DATA_NULL = "null";

    /**
     * The content URL <b>OR</b> the ID of the ContentData entity
     */
    private String contentURL;
    
    /**
     * The Mime type.
     */
    private String mimeType;
    
    /**
     * The character encoding.
     */
    private String encoding;
    
    /**
     * The length of the file.
     */
    private long length;
    
    /**
     * Default constructor.
     */
    public PlainFileNodeImpl()
    {
    }
    
    /**
     * Make one from just an AVMStore.
     * This is the constructor used when a brand new plain file is being made.
     * @param store An AVMStore.
     */
    public PlainFileNodeImpl(AVMStore store)
    {
        super(store);
        setVersionID(1);
    }
    
    /**
     * Copy on write constructor.
     * @param other The node we are being copied from.
     * @param store The AVMStore.
     */
    public PlainFileNodeImpl(PlainFileNode other,
                             AVMStore store, Long parentAcl, ACLCopyMode mode)
    {
        super(store);
        // The null is OK because the Lookup argument is only use by
        // layered files.
        setContentData(other.getContentData(null));
        setVersionID(other.getVersionID() + 1);
        
        copyACLs(other, parentAcl, mode);
        copyCreationAndOwnerBasicAttributes(other);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        
        copyProperties(other);
        copyAspects(other);
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
                             Acl acl,
                             int versionID,
                             Acl parentAcl,
                             ACLCopyMode mode)
    {
        super(store);
        setContentData(content);
        setBasicAttributes(attrs);
        setVersionID(versionID + 1);
        
        copyACLs(acl, parentAcl, mode);
        
        AVMDAOs.Instance().fAVMNodeDAO.save(this);
        
        addProperties(props);
        setAspects(new HashSet<QName>(aspects));
    }
    
    /**
     * Copy on write logic.
     * @param lPath The lookup path. 
     */
    public AVMNode copy(Lookup lPath)
    {
        DirectoryNode dir = lPath.getCurrentNodeDirectory();
        Long parentAclId = null;
        if((dir != null) && (dir.getAcl() != null))
        {
            parentAclId = dir.getAcl().getId();
        }
        PlainFileNodeImpl newMe = new PlainFileNodeImpl(this, lPath.getAVMStore(), parentAclId, ACLCopyMode.COW);
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
        ContentData contentData = getContentData();
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
                                     contentData == null ? 0L : contentData.getSize(),
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
        ContentData contentData = getContentData();
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
                                     contentData == null ? 0L : contentData.getSize(),
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
        ContentData contentData = getContentData();
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
                                     contentData == null ? 0L : contentData.getSize(),
                                     -1);
    }

    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public String getContentURL()
    {
        return contentURL;
    }

    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public void setContentURL(String contentURL)
    {
        this.contentURL = contentURL;
    }

    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public String getEncoding()
    {
        return encoding;
    }

    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public long getLength()
    {
        return length;
    }

    /**
     * DAO accessor only.  <b>DO NOT USE</b> in code.
     */
    public void setLength(long length)
    {
        this.length = length;
    }

    /**
     * Set the ContentData for this file.
     * @param contentData The value to set.
     */
    public void setContentData(ContentData contentData)
    {
        // Remove any legacy-stored attributes to avoid confusion
        if (isLegacyContentData())
        {
            // Wipe over the old values
            contentURL = PREFIX_CONTENT_DATA_ID + SUFFIX_CONTENT_DATA_NULL;
            encoding = null;
            length = 0L;
            mimeType = null;
        }
        
        Long oldContentDataId = getContentDataId();
        Long newContentDataId = null;
        if (oldContentDataId == null)
        {
            if (contentData != null)
            {
                // There was no reference before, so just create a new one
                newContentDataId = AVMDAOs.Instance().contentDataDAO.createContentData(contentData).getFirst();
            }
        }
        else
        {
            if (contentData != null)
            {
                // Update it.  The ID will remain the same.
                AVMDAOs.Instance().contentDataDAO.updateContentData(oldContentDataId, contentData);
                newContentDataId = oldContentDataId;
            }
            else
            {
                // Delete the old instance
                AVMDAOs.Instance().contentDataDAO.deleteContentData(oldContentDataId);
                newContentDataId = null;
            }
        }
        
        // Set the pointer to the ContentData instance
        if (newContentDataId == null)
        {
            contentURL = PREFIX_CONTENT_DATA_ID + SUFFIX_CONTENT_DATA_NULL;
        }
        else
        {
            contentURL = PREFIX_CONTENT_DATA_ID + newContentDataId;
        }
    }

    /**
     * Get the ContentData for this file.
     * @param lPath The lookup path used to get here.  Unused here.
     * @return The ContentData object for this file.
     */
    public ContentData getContentData(Lookup lPath)
    {
        return getContentData();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If the content URL contains the special prefix, <b>{@link PREFIX_CONTENT_DATA_ID}</b>,
     * then the data is pulled directly from the {@link ContentDataDAO}.
     */
    public ContentData getContentData()
    {
        if (contentURL != null && contentURL.startsWith(PREFIX_CONTENT_DATA_ID))
        {
            Long contentDataId = getContentDataId();
            try
            {
                if (contentDataId == null)
                {
                    return new ContentData(null, null, 0L, null);
                }
                else
                {
                    return AVMDAOs.Instance().contentDataDAO.getContentData(contentDataId).getSecond();
                }
            }
            catch (Throwable e)
            {
                throw new AlfrescoRuntimeException(
                        "AVM File node " + getId() + " has invalid ContentData id reference " + contentDataId,
                        e);
            }
        }
        else
        {
            // This deals with legacy data
            return new ContentData(contentURL, mimeType, length, encoding);
        }
    }
    
    /**
     * Checks the content URL and if it contains the {@link #PREFIX_CONTENT_DATA_ID prefix}
     * indicating the an new ContentData storage ID, returns <tt>true</tt>.
     */
    public boolean isLegacyContentData()
    {
        return (contentURL == null || !contentURL.startsWith(PREFIX_CONTENT_DATA_ID));
    }
    
    /**
     * Get the ID of the ContentData as given by the string in the ContentURL of
     * form <b>ID:12345</b>
     */
    public Long getContentDataId()
    {
        String idStr = contentURL.substring(3);
        if (idStr.equals(SUFFIX_CONTENT_DATA_NULL))
        {
            // Nothing has been stored against this file
            return null;
        }
        try
        {
            return Long.parseLong(idStr);
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "AVM File node " + getId() + " has malformed ContentData id reference " + idStr,
                    e);
        }
    }
}

