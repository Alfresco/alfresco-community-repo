package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MemberOfSite implements Serializable, ExpectedComparison, Comparable<MemberOfSite>
{
	private static final long serialVersionUID = -5834300883854366123L;

	private SiteRole role;
	private String siteId;
	private String siteGuid;
	private Site site;

	public MemberOfSite()
	{
	}

    public MemberOfSite(String siteId, String siteGuid, SiteRole role)
    {
		super();
		if(siteId == null)
		{
			throw new IllegalArgumentException();
		}
		if(siteGuid == null)
		{
			throw new IllegalArgumentException();
		}
		if(role == null)
		{
			throw new IllegalArgumentException();
		}
		this.role = role;
		this.siteId = siteId;
		this.siteGuid = siteGuid;
	}

    public MemberOfSite(Site site, SiteRole role)
    {
		super();
		if(site == null)
		{
			throw new IllegalArgumentException();
		}
		if(role == null)
		{
			throw new IllegalArgumentException();
		}
		this.role = role;
		this.site = site;
		this.siteId = site.getSiteId();
		this.siteGuid = site.getGuid();
	}

    public String getSiteId()
    {
		return siteId;
	}

	public String getGuid()
	{
		return siteGuid;
	}

	public Site getSite()
	{
		return site;
	}

	public SiteRole getRole()
	{
		return role;
	}
	
	public void setRole(SiteRole role)
	{
		if(role == null)
		{
			throw new IllegalArgumentException();
		}
		this.role = role;
	}

	public void setSite(SiteImpl site)
	{
		if(site == null)
		{
			throw new IllegalArgumentException();
		}
		this.site = site;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result
				+ site.getSiteId().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj == null)
		{
			return false;
		}
		
		if (getClass() != obj.getClass())
		{
			return false;
		}
		
		MemberOfSite other = (MemberOfSite) obj;
		if (role != other.role)
		{
			return false;
		}

		return site.equals(other.site);
	}

	public static MemberOfSite parseMemberOfSite(JSONObject jsonObject)
	{
		String role = (String)jsonObject.get("role");
		JSONObject siteJSON = (JSONObject)jsonObject.get("site");
		Site site = SiteImpl.parseSite(siteJSON);
		MemberOfSite siteMember = new MemberOfSite(site, SiteRole.valueOf(role));
		return siteMember;
	}

	public static ListResponse<MemberOfSite> parseMemberOfSites(JSONObject jsonObject)
	{
		List<MemberOfSite> siteMembers = new ArrayList<MemberOfSite>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			siteMembers.add(parseMemberOfSite(entry));
		}
		
		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);

		ListResponse<MemberOfSite> resp = new ListResponse<MemberOfSite>(paging, siteMembers);
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject memberOfSiteJson = new JSONObject();
		memberOfSiteJson.put("role", getRole());
		memberOfSiteJson.put("id", getSiteId());
		memberOfSiteJson.put("guid", getGuid());
		return memberOfSiteJson;
	}

	@Override
	public String toString()
	{
		return "MemberOfSite [role=" + role + ", siteShortName="
				+ site + "]";
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof MemberOfSite);
		
		MemberOfSite other = (MemberOfSite)o;
		
		if(siteId != null)
		{
			AssertUtil.assertEquals("siteId", siteId, other.getSiteId());
		}
		AssertUtil.assertEquals("role", role, other.getRole());
		site.expected(other.getSite());
	}

	@Override
	public int compareTo(MemberOfSite o)
	{
        int i = site.getTitle().compareTo(o.getSite().getTitle());
        if(i == 0)
        {
        	i = role.compareTo(o.getRole());
        }
        return i;
	}
}
