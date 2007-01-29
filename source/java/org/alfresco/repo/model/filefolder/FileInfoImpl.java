/*
 * Copyright (C) 2005 Alfresco, Inc.
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
    private NodeRef nodeRef;
    private NodeRef linkNodeRef;
    private boolean isFolder;
    private boolean isLink;
    private Map<QName, Serializable> properties;

    /**
     * Package-level constructor
     */
    /* package */ FileInfoImpl(NodeRef nodeRef, boolean isFolder, Map<QName, Serializable> properties)
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
