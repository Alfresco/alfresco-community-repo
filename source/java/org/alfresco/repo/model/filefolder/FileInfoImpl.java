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
 * http://www.alfresco.com/legal/licensing"
 */
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
    private Map<QName, Serializable> properties;

    /**
     * Package-level constructor
     */
    /* package */ FileInfoImpl(
            NodeRef nodeRef,
            boolean isFolder,
            Map<QName, Serializable> properties)
    {
        this.nodeRef = nodeRef;
        this.isFolder = isFolder;
        this.properties = properties;
        
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
}
