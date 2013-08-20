package org.alfresco.rest.api.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

/**
 * Representation of a document node.
 * 
 * @author steveglover
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
	
	public Document(NodeRef nodeRef, Properties properties)
	{
		super(nodeRef, properties);

		Map<String, PropertyData<?>> props = properties.getProperties();
		this.mimeType = (String)getValue(props, PropertyIds.CONTENT_STREAM_MIME_TYPE);
		this.sizeInBytes = (BigInteger)getValue(props, PropertyIds.CONTENT_STREAM_LENGTH);
		this.versionLabel = (String)getValue(props, PropertyIds.VERSION_LABEL);
	}

	public Document(NodeRef nodeRef, Map<QName, Serializable> nodeProps)
	{
		super(nodeRef, nodeProps);
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
