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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

/**
 * Representation of a document node.
 * 
 * @author steveglover
 * @author janv
 *
 */
public class Document extends Node
{
	private String mimeType;
	private BigInteger sizeInBytes;
	private String versionLabel;

	public Document()
	{
		super();
	}

	/*
	public Document(NodeRef nodeRef, Properties properties)
	{
		super(nodeRef, properties);

		Map<String, PropertyData<?>> props = properties.getProperties();
		this.mimeType = (String)getValue(props, PropertyIds.CONTENT_STREAM_MIME_TYPE);
		this.sizeInBytes = (BigInteger)getValue(props, PropertyIds.CONTENT_STREAM_LENGTH);
		this.versionLabel = (String)getValue(props, PropertyIds.VERSION_LABEL);
	}
	*/

	public Document(NodeRef nodeRef, Map<QName, Serializable> nodeProps, NamespaceService namespaceService)
	{
		super(nodeRef, nodeProps, namespaceService);
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

	@Override
	public String toString()
	{
		return "Document [mimeType=" + mimeType + ", sizeInBytes="
				+ sizeInBytes + ", versionLabel=" + versionLabel + ", nodeRef="
				+ nodeRef + ", name=" + name + ", title=" + title
				+ ", description=" + description + ", createdAt=" + createdAt
				+ ", modifiedAt=" + modifiedAt + ", createdBy=" + createdBy
				+ ", modifiedBy=" + modifiedBy + "]";
	}
}
