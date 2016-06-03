package org.alfresco.repo.model.filefolder;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 * Common file information implementation.
 * 
 * @author Derek Hulley
 */
public class FileInfoImpl implements FileInfo
{
    private static final long serialVersionUID = 1915018521764853537L;

    private NodeRef nodeRef;
    private NodeRef linkNodeRef;
    private boolean isFolder;
    private boolean isLink;
    private boolean isHidden;
    private Map<QName, Serializable> properties;
    private QName typeQName;

    /**
     * Package-level constructor
     */
    /* package */ FileInfoImpl(
            NodeRef nodeRef,
            QName typeQName,
            boolean isFolder,
            boolean isHidden,
            Map<QName, Serializable> properties)
    {
        this.nodeRef = nodeRef;
        this.typeQName = typeQName;
        
        this.isFolder = isFolder;
        this.properties = properties;
        this.isHidden = isHidden;
        
        // Check if this is a link node
        if ( properties.containsKey( ContentModel.PROP_LINK_DESTINATION))
        {
        	isLink = true;
        	linkNodeRef = (NodeRef) properties.get( ContentModel.PROP_LINK_DESTINATION);
        }
    }
    
    /**
     * @see #getNodeRef()
     * @see NodeRef#equals(Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (this == obj)
        {
            return true;
        }
        else if (obj instanceof FileInfoImpl == false)
        {
            return false;
        }
        FileInfoImpl that = (FileInfoImpl) obj;
        return (this.getNodeRef().equals(that.getNodeRef()));
    }

    void setHidden(boolean isHidden)
    {
        this.isHidden = isHidden;
    }

    /**
     * @see #getNodeRef()
     * @see NodeRef#hashCode()
     */
    @Override
    public int hashCode()
    {
        return getNodeRef().hashCode();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(80);
        sb.append("FileInfo")
          .append("[name=").append(getName())
          .append(", isFolder=").append(isFolder)
          .append(", nodeRef=").append(nodeRef);
        
        if ( isLink())
        {
        	sb.append(", linkref=");
        	sb.append(linkNodeRef);
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    public boolean isFolder()
    {
        return isFolder;
    }

    public boolean isLink()
    {
    	return isLink;
    }
  
    public boolean isHidden() {
    	return isHidden;
    }

    public NodeRef getLinkNodeRef()
    {
    	return linkNodeRef;
    }
    
    public String getName()
    {
        return (String) properties.get(ContentModel.PROP_NAME);
    }

    public Date getCreatedDate()
    {
        return DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_CREATED));
    }

    public Date getModifiedDate()
    {
        return DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_MODIFIED));
    }
    
    public ContentData getContentData()
    {
        return DefaultTypeConverter.INSTANCE.convert(ContentData.class, properties.get(ContentModel.PROP_CONTENT));
    }

    public Map<QName, Serializable> getProperties()
    {
        return properties;
    }
    
    public QName getType()
    {
        return typeQName;
    }
}
