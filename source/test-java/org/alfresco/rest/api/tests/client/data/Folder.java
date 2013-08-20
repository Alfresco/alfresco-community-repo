package org.alfresco.rest.api.tests.client.data;

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
 * Representation of a folder node.
 * 
 * @author steveglover
 *
 */
public class Folder extends Node implements ExpectedComparison, JSONAble
{
	private static final long serialVersionUID = 5020819866533183524L;

	/**
	 * For POSTs
	 * @param guid
	 */
	public Folder(String guid)
	{
		super(guid);
	}
	
	public Folder(String id, String guid)
	{
		super(id, guid);
	}

//	public Folder(String id, String guid, Map<String, Serializable> properties)
//	{
//		super(id, guid, properties);
//	}

	public static Folder getFolder(String id, String guid, Properties props)
	{
		Folder folder = new Folder(id, guid);

		Map<String, PropertyData<?>> properties = props.getProperties();
		folder.setName((String)properties.get(PropertyIds.NAME).getFirstValue());
		folder.setTitle((String)properties.get(ContentModel.PROP_TITLE.toString()).getFirstValue());
		folder.setCreatedBy((String)properties.get(PropertyIds.CREATED_BY).getFirstValue());
		folder.setModifiedBy((String)properties.get(PropertyIds.LAST_MODIFIED_BY).getFirstValue());
		GregorianCalendar modifiedAt = (GregorianCalendar)properties.get(PropertyIds.LAST_MODIFICATION_DATE).getFirstValue();
		folder.setModifiedAt(modifiedAt.getTime());
		GregorianCalendar createdAt = (GregorianCalendar)properties.get(PropertyIds.CREATION_DATE).getFirstValue();
		folder.setCreatedAt(createdAt.getTime());
		//document.setDescription((String)props.get(PropertyIds.DE).getFirstValue());
		return folder;
	}
	
	public JSONObject toJSON()
	{
		JSONObject json = super.toJSON();
		return json;
	}

	public static Folder parseFolder(JSONObject jsonObject) throws ParseException
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
		
		Folder folder = new Folder(id, guid);
		folder.setName(name);
		folder.setTitle(title);
		folder.setCreatedBy(createdBy);
		folder.setModifiedBy(modifiedBy);
		folder.setModifiedAt(modifiedAt);
		folder.setCreatedAt(createdAt);
		folder.setDescription(description);
		return folder;
	}

	@Override
	public void expected(Object o)
	{
		super.expected(o);
	}

	@Override
	public String toString()
	{
		return "Folder [nodeId=" + nodeId + ", guid=" + guid + ", name=" + name
				+ ", title=" + title + ", description=" + description
				+ ", createdAt=" + createdAt + ", modifiedAt=" + modifiedAt
				+ ", createdBy=" + createdBy + ", modifiedBy=" + modifiedBy
				+ "]";
	}
}
