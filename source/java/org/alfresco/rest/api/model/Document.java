/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Representation of a document node.
 * 
 * @author steveglover
 * @author janv
 *
 */
public class Document extends Node
{
    // TODO backward compat' - favourites etc
	private String mimeType;
	private BigInteger sizeInBytes;
    private String versionLabel;

    private ContentInfo contentInfo;

    public Document() {
        super();
    }

    public Document(NodeRef nodeRef, NodeRef parentNodeRef, Map<QName, Serializable> nodeProps, ServiceRegistry sr)
    {
        super(nodeRef, parentNodeRef, nodeProps, sr);

        Serializable val = nodeProps.get(ContentModel.PROP_CONTENT);

        if ((val != null) && (val instanceof ContentData)) {
            ContentData cd = (ContentData)val;
            String mimeType = cd.getMimetype();
            String mimeTypeName = sr.getMimetypeService().getDisplaysByMimetype().get(mimeType);
            this.contentInfo = new ContentInfo(mimeType, mimeTypeName, cd.getSize(), cd.getEncoding());
        }

        //this.versionLabel = (String)nodeProps.get(ContentModel.PROP_VERSION_LABEL);
    }

	public String getMimeType()
	{
		return mimeType;
	}

	public BigInteger getSizeInBytes()
	{
		return sizeInBytes;
	}

	public String getVersionLabel()
	{
		return versionLabel;
	}

	public Boolean getIsFolder()
	{
		return false;
	}

    public ContentInfo getContent()
    {
        return contentInfo;
    }

	@Override
	public String toString()
	{
		return "Document [contentInfo=" + contentInfo.toString() + ", nodeRef="
				+ nodeRef + ", name=" + name + ", createdAt=" + createdAt
				+ ", modifiedAt=" + modifiedAt + ", createdBy=" + createdBy
				+ ", modifiedBy=" + modifiedBy + "]";
	}
}
