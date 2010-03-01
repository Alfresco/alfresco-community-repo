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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Context information for node-related content.
 * 
 * @author Derek Hulley
 */
public class NodeContentContext extends ContentContext
{
    private static final long serialVersionUID = -1836714367516857907L;

    private NodeRef nodeRef;
    private QName propertyQName;
    
    /**
     * Construct the instance with the content URL.
     * 
     * @param   existingContentReader   content with which to seed the new writer - may be <tt>null</tt>
     * @param   contentUrl              the content URL - may be <tt>null</tt>
     * @param   nodeRef                 the node holding the content metadata - may not be <tt>null</tt>
     * @param   propertyQName           the property holding the content metadata  - may not be <tt>null</tt>
     */
    public NodeContentContext(
            ContentReader existingContentReader,
            String contentUrl,
            NodeRef nodeRef,
            QName propertyQName)
    {
        super(existingContentReader, contentUrl);
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("propertyQName", propertyQName);
        this.nodeRef = nodeRef;
        this.propertyQName = propertyQName;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("NodeContentContext")
          .append("[ contentUrl=").append(getContentUrl())
          .append(", existing=").append((getExistingContentReader() == null ? false : true))
          .append(", nodeRef=").append(nodeRef)
          .append(", propertyQName=").append(propertyQName)
          .append("]");
        return sb.toString();
    }

    /**
     * @return  Returns the node holding the content metadata
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    /**
     * 
     * @return  Returns the property holding the content metadata
     */
    public QName getPropertyQName()
    {
        return propertyQName;
    }
}
