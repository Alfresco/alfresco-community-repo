package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.FolderTypeDefinition;
import org.json.simple.JSONObject;

public class CMISNode implements Serializable, ExpectedComparison
{
	private static final long serialVersionUID = -5027938359868278498L;

	protected String nodeId;
	protected String guid;
	protected Map<String, Serializable> properties;

	/**
	 * For POSTs
	 * @param guid
	 */
	public CMISNode(String guid)
	{
		this.guid = guid;
	}
	
	public CMISNode(String id, String guid)
	{
		this.nodeId = id;
		this.guid = guid;
	}

	public CMISNode(String nodeId, String guid, Map<String, Serializable> properties)
	{
		this.nodeId = nodeId;
		this.guid = nodeId;
		this.properties = properties;
	}

	public void setGuid(String guid)
	{
		this.guid = guid;
	}

	public String getGuid()
	{
		return guid;
	}

	public String getRawNodeId()
	{
		return nodeId;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public boolean isFolder()
	{
		return false;
	}

	public static Map<String, Serializable> getProperties(Map<QName, Serializable> properties)
	{
		Map<String, Serializable> propertiesMap = new HashMap<String, Serializable>();
		for(QName propName : properties.keySet())
		{
			propertiesMap.put(propName.toPrefixString(), properties.get(propName));
		}
		return propertiesMap;
	}
	
	public static Map<String, Serializable> getProperties(List<Property<?>> properties)
	{
		Map<String, Serializable> propertiesMap = new HashMap<String, Serializable>();
		for(Property<?> p : properties)
		{
			propertiesMap.put(p.getId(), p.getValueAsString());
		}
		return propertiesMap;
	}
	
	public static Map<String, Serializable> getProperties(Properties properties)
	{
		Map<String, Serializable> propertiesMap = new HashMap<String, Serializable>();
		for(PropertyData<?> p : properties.getPropertyList())
		{
			propertiesMap.put(p.getId(), p.getFirstValue().toString());
		}
		return propertiesMap;
	}

	public Serializable getProperty(String name)
	{
		return properties.get(name);
	}

	public Map<String, Serializable> getProperties()
	{
		return properties;
	}
	
	// TODO getFirstValue replace with getValues
	// how to determine type of result to choose Node or FolderNode
	public static CMISNode createNode(QueryResult qr)
	{
		List<PropertyData<?>> props = qr.getProperties();
		Map<String, Serializable> properties = new HashMap<String, Serializable>();

		for(PropertyData<?> p : props)
		{
			properties.put(p.getId(), (Serializable)p.getFirstValue());
		}

		String objectId = (String)qr.getPropertyById(PropertyIds.OBJECT_ID).getFirstValue();
		CMISNode n = new CMISNode(objectId, objectId, properties);
		return n;
	}

	public static CMISNode createNode(CmisObject o)
	{
		CMISNode n = null;

		Map<String, Serializable> properties = CMISNode.getProperties(o.getProperties());

		if(o.getBaseType() instanceof FolderTypeDefinition)
		{
			n = new FolderNode(o.getId(), o.getId(), properties);
		}
		else
		{
			n = new CMISNode(o.getId(), o.getId(), properties);
		}

		return n;
	}
	
	public static CMISNode createNode(String objectId)
	{
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		CMISNode n = new CMISNode(objectId, objectId, properties);
		return n;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CMISNode other = (CMISNode) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();
		json.put("guid", getGuid());
		json.put("id", getNodeId());
		return json;
	}
	
	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof CMISNode);
		
		CMISNode other = (CMISNode)o;

		AssertUtil.assertEquals("id", nodeId, other.getNodeId());
		AssertUtil.assertEquals("guid", guid, other.getGuid());
		for(String propertyName : properties.keySet())
		{
			Serializable expected = properties.get(propertyName);
			Serializable actual = other.getProperty(propertyName);
			AssertUtil.assertEquals(propertyName, expected, actual);
		}
	}

	@Override
	public String toString()
	{
		return "CMISNode [nodeId=" + nodeId + ", guid=" + guid + ", properties="
				+ properties + "]";
	}
}
