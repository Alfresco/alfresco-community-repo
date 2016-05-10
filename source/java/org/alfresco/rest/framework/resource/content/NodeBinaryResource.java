/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.framework.resource.content;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * A binary resource based on a Node reference.
 * 
 * @author Gethin James
 */
public class NodeBinaryResource implements BinaryResource
{

    final NodeRef nodeRef;
    final QName propertyQName;
    final ContentInfo contentInfo;
    final String attachFileName;
    
    public NodeBinaryResource(NodeRef nodeRef, QName propertyQName, ContentInfo contentInfo, String attachFileName)
    {
        super();

        this.nodeRef = nodeRef;
        this.propertyQName = propertyQName;
        this.contentInfo = contentInfo;
        this.attachFileName = attachFileName;
    }

    public NodeRef getNodeRef()
    {
        return this.nodeRef;
    }

    public QName getPropertyQName()
    {
        return this.propertyQName;
    }

    public ContentInfo getContentInfo()
    {
        return this.contentInfo;
    }

    public String getAttachFileName()
    {
        return this.attachFileName;
    }
}
