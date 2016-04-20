package org.alfresco.service.cmr.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Common file information.  The implementations may store the properties for the lifetime
 * of this instance; i.e. the values are transient and can be used as read-only values for
 * a short time only.
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface FileInfo extends PermissionCheckValue, Serializable
{
    /**
     * @return Returns a reference to the low-level node representing this file
     */
    @Override
    public NodeRef getNodeRef();
    
    /**
     * @return Return true if this instance represents a folder, false if this represents a file
     */
    public boolean isFolder();
    
    /**
     * @return true if this instance represents a link to a node
     */
    public boolean isLink();
    
    /**
     * @return true if this instance represents a hidden file
     */
    public boolean isHidden();
    
    /**
     * @return Return the reference to the node that this node is linked to
     */
    public NodeRef getLinkNodeRef();
    
    /**
     * @return Returns the name of the file or folder within the parent folder
     */
    public String getName();
    
    /**
     * @return Returns the date the node was created
     */
    public Date getCreatedDate();
    
    /**
     * @return Returns the modified date
     */
    public Date getModifiedDate();
    
    /**
     * Get the content data.  This is only valid for {@link #isFolder() files}.
     * 
     * @return Returns the content data
     */
    public ContentData getContentData();
    
    /**
     * @return Returns all the node properties
     */
    public Map<QName, Serializable> getProperties();
    
    /**
     * @return Returns (sub-)type of folder or file
     */
    public QName getType();
}
