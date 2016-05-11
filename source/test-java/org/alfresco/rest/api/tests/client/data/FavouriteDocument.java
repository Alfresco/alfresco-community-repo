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

package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.rest.api.tests.PublicApiDateFormat;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.json.simple.JSONObject;

/**
 * Representation of a document node (as returned by Favourites API)
 * 
 * @author steveglover
 *
 */
public class FavouriteDocument extends FavouriteNode implements ExpectedComparison, JSONAble
{
	private static final long serialVersionUID = -5890002728061039516L;

	private String mimeType;
	private BigInteger sizeInBytes;
	private String versionLabel;

	/**
	 * For POSTs
	 * @param guid String
	 */
	public FavouriteDocument(String guid)
	{
		super(guid);
	}

	public FavouriteDocument(String id, String guid)
	{
		super(id, guid);
	}

//	public Document(String id, String guid, Map<String, Serializable> properties)
//	{
//		super(id, guid, properties);
//	}

	public static FavouriteDocument getDocument(String id, String guid, Properties props)
	{
		FavouriteDocument document = new FavouriteDocument(id, guid);

		Map<String, PropertyData<?>> properties = props.getProperties();
		document.setName((String)properties.get(PropertyIds.NAME).getFirstValue());
		document.setTitle((String)properties.get(ContentModel.PROP_TITLE.toString()).getFirstValue());
		document.setCreatedBy((String)properties.get(PropertyIds.CREATED_BY).getFirstValue());
		document.setModifiedBy((String)properties.get(PropertyIds.LAST_MODIFIED_BY).getFirstValue());
		GregorianCalendar modifiedAt = (GregorianCalendar)properties.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue();
		document.setModifiedAt(modifiedAt.getTime());
		GregorianCalendar createdAt = (GregorianCalendar)properties.get(PropertyIds.CREATION_DATE).getFirstValue();
		document.setCreatedAt(createdAt.getTime());
		//document.setDescription((String)props.get(PropertyIds.DE).getFirstValue());
		document.setMimeType((String)properties.get(PropertyIds.CONTENT_STREAM_MIME_TYPE).getFirstValue());
		document.setSizeInBytes((BigInteger)properties.get(PropertyIds.CONTENT_STREAM_LENGTH).getFirstValue());
		document.setVersionLabel((String)properties.get(PropertyIds.VERSION_LABEL).getFirstValue());
		return document;
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
	
	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public void setSizeInBytes(BigInteger sizeInBytes)
	{
		this.sizeInBytes = sizeInBytes;
	}

	public void setVersionLabel(String versionLabel)
	{
		this.versionLabel = versionLabel;
	}

	public JSONObject toJSON()
	{
		JSONObject json = super.toJSON();
		return json;
	}
	
	@Override
	public void expected(Object o)
	{
		super.expected(o);

		assertTrue(o instanceof FavouriteDocument);

		FavouriteDocument other = (FavouriteDocument)o;
		
		AssertUtil.assertEquals("mimeType", mimeType, other.getMimeType());
		AssertUtil.assertEquals("sizeInBytes", sizeInBytes, other.getSizeInBytes());
		AssertUtil.assertEquals("versionLabel", versionLabel, other.getVersionLabel());
	}

	public static FavouriteDocument parseDocument(JSONObject jsonObject) throws ParseException
	{
		String id = (String)jsonObject.get("id");
		String guid = (String)jsonObject.get("guid");
		String name = (String)jsonObject.get("name");
		String title = (String)jsonObject.get("title");
		String description = (String)jsonObject.get("description");
		Date createdAt = PublicApiDateFormat.getDateFormat().parse((String)jsonObject.get("createdAt"));
		Date modifiedAt = PublicApiDateFormat.getDateFormat().parse((String)jsonObject.get("modifiedAt"));
		String createdBy = (String)jsonObject.get("createdBy");
		String modifiedBy = (String)jsonObject.get("modifiedBy");
		String mimeType = (String)jsonObject.get("mimeType");
		Long sizeInBytes = (Long)jsonObject.get("sizeInBytes");
		String versionLabel = (String)jsonObject.get("versionLabel");

		FavouriteDocument document = new FavouriteDocument(id, guid);
		document.setName(name);
		document.setTitle(title);
		document.setCreatedBy(createdBy);
		document.setModifiedBy(modifiedBy);
		document.setModifiedAt(modifiedAt);
		document.setCreatedAt(createdAt);
		document.setDescription(description);
		document.setMimeType(mimeType);
		document.setSizeInBytes(BigInteger.valueOf(sizeInBytes));
		document.setVersionLabel(versionLabel);
		return document;
	}

	@Override
	public String toString()
	{
		return "FavouriteDocument [mimeType=" + mimeType + ", sizeInBytes="
				+ sizeInBytes + ", versionLabel=" + versionLabel + ", nodeId="
				+ nodeId + ", guid=" + guid + ", name=" + name + ", title="
				+ title + ", description=" + description + ", createdAt="
				+ createdAt + ", modifiedAt=" + modifiedAt + ", createdBy="
				+ createdBy + ", modifiedBy=" + modifiedBy + "]";
	}
}
