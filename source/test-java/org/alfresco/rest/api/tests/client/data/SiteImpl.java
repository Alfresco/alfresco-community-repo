package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.service.cmr.site.SiteInfo;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SiteImpl implements Serializable, Site, Comparable<SiteImpl>, ExpectedComparison
{
	private static final long serialVersionUID = -3774392026234649419L;

	public static final String FIELD_SITE_ID = "siteId";
	public static final String FIELD_CREATED = "created";
	public static final String FIELD_HAS_MEMBERS = "hasMembers";

    protected Boolean created = false;
	protected String networkId;
	protected String siteId;
	protected String guid;
	protected String title;
	protected String description;
	protected SiteRole role;
	protected String visibility; // one of (PUBLIC,MODERATED,PRIVATE), defaults to PUBLIC
	protected String type;

    public SiteImpl()
    {
    }
    
    public SiteImpl(String networkId, String siteId, String guid)
    {
		if(siteId == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		if(guid == null)
		{
			throw new java.lang.IllegalArgumentException();
		}
		this.networkId = networkId;
    	this.siteId = siteId;
    	this.guid = guid;
    }

    public SiteImpl(SiteInfo siteInfo, SiteRole siteRole, Boolean created)
    {
    	this.siteId = siteInfo.getShortName();
    	this.description = siteInfo.getDescription();
    	this.title = siteInfo.getTitle();
    	this.visibility = siteInfo.getVisibility().toString();
    	this.created = created;
    	this.guid = siteInfo.getNodeRef().getId();
    }

	public SiteImpl(String networkId, String siteId, String guid, String title, String description,
			String visibility, String type, SiteRole siteRole)
	{
		super();
		this.networkId = networkId;
		this.siteId = siteId;
		this.title = title;
		this.description = description;
		this.visibility = visibility;
		this.type = type;
		this.role = siteRole;
		this.guid = guid;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof SiteImpl);
		
		SiteImpl site = (SiteImpl)o;
		
		AssertUtil.assertEquals("siteId", getSiteId(), site.getSiteId());
		AssertUtil.assertEquals("guid", getGuid(), site.getGuid());
		AssertUtil.assertEquals("title", getTitle(), site.getTitle());
		AssertUtil.assertEquals("description", getDescription(), site.getDescription());
		AssertUtil.assertEquals("visibility", getVisibility(), site.getVisibility());
		AssertUtil.assertEquals("role", getRole(), site.getRole());
	}
	
	
	public Boolean getCreated()
	{
		return created;
	}

	public String getGuid()
	{
		return guid;
	}

	public String getNetworkId()
	{
		return networkId;
	}

	public Boolean isCreated()
	{
		return created;
	}

	public void setCreated(Boolean created)
	{
		this.created = created;
	}

	public String getSiteId()
	{
		return siteId;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getVisibility() 
	{
		return visibility;
	}
	
	public String getType()
	{
		return type;
	}

	public void setNetworkId(String networkId)
	{
		this.networkId = networkId;
	}

	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
	}
	
	public void setGuid(String guid)
	{
		this.guid = guid;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setVisibility(String visibility)
	{
		this.visibility = visibility;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public static Site parseSite(JSONObject jsonObject)
	{
		String id = (String)jsonObject.get("id");
		String guid = (String)jsonObject.get("guid");
		String title = (String)jsonObject.get("title");
		String description = (String)jsonObject.get("description");
		String visibility = (String)jsonObject.get("visibility");
		String roleStr = (String)jsonObject.get("role");
		SiteRole role = null;
		if(roleStr != null)
		{
			role = SiteRole.valueOf(roleStr);
		}
		SiteImpl site = new SiteImpl(null, id, guid, title, description, visibility, "st:site", role);
		return site;
	}
	
	public static ListResponse<Site> parseSites(JSONObject jsonObject)
	{
		List<Site> sites = new ArrayList<Site>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			sites.add(parseSite(entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);

		ListResponse<Site> resp = new ListResponse<Site>(paging, sites);
		return resp;
	}
	
	@Override
	public SiteRole getRole()
	{
		return role;
	}
	
	@Override
	public String toString()
	{
		return "SiteImpl [created=" + created + ", networkId=" + networkId
				+ ", siteId=" + siteId + ", guid=" + guid + ", title=" + title
				+ ", description=" + description + ", role=" + role
				+ ", visibility=" + visibility + ", type=" + type + "]";
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject siteJson = new JSONObject();
		siteJson.put("id", getSiteId());
		siteJson.put("guid", getGuid());
		return siteJson;
	}

	@Override
	public int compareTo(SiteImpl site)
	{
		return siteId.compareTo(site.getSiteId());
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((networkId == null) ? 0 : networkId.hashCode());
		result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
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
		SiteImpl other = (SiteImpl) obj;
		if (networkId == null)
		{
			if (other.networkId != null)
				return false;
		} else if (!networkId.equals(other.networkId))
			return false;
		if (siteId == null)
		{
			if (other.siteId != null)
				return false;
		} else if (!siteId.equals(other.siteId))
			return false;
		return true;
	}
}
