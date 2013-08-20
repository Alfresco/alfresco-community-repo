package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class SiteMember implements Serializable, ExpectedComparison, Comparable<SiteMember>
{
	private static final long serialVersionUID = 505331886661880389L;

	public static final String FIELD_USERNAME = "username";
    public static final String FIELD_SITE_ID = "siteId";
    public static final String FIELD_STATE = "status.state";

    private String memberId;
    private Person member;
    private String siteId;
	private String role;
	private Status status;

	public SiteMember()
	{
	}
	
	public SiteMember(String memberId)
	{
		this.memberId = memberId;
	}

	public SiteMember(String memberId, String role)
	{
		this.memberId = memberId;
		this.role = role;
	}

	public SiteMember(String memberId, Person member, String siteId, String role)
	{
		this.memberId = memberId;
		this.member = member;
		this.siteId = siteId;
		this.role = role;
	}
    
    public String getMemberId()
    {
		return memberId;
	}

	public Person getMember()
    {
		return member;
	}

	public void setMember(Person member)
	{
		this.member = member;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	public String getSiteId()
	{
		return siteId;
	}

	public void setSiteId(String siteId)
	{
		this.siteId = siteId;
	}

	public Status getStatus()
	{
		return status;
	}

	public void setStatus(Status status)
	{
		this.status = status;
	}

	public String getRole()
	{
		return role;
	}

	@Override
	public String toString()
	{
		return "SiteMember [memberId=" + memberId + ", member=" + member
				+ ", siteId=" + siteId + ", role=" + role + ", status="
				+ status + "]";
	}

	public static SiteMember parseSiteMember(String siteId, JSONObject jsonObject)
	{
		String id = (String)jsonObject.get("id");
		String role = (String)jsonObject.get("role");
		JSONObject personJSON = (JSONObject)jsonObject.get("person");
		Person member = Person.parsePerson(personJSON);
		SiteMember siteMember = new SiteMember(id, member, siteId, role);
		return siteMember;
	}

	public static ListResponse<SiteMember> parseSiteMembers(String siteId, JSONObject jsonObject)
	{
		List<SiteMember> siteMembers = new ArrayList<SiteMember>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			siteMembers.add(parseSiteMember(siteId, entry));
		}
		
		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);

		ListResponse<SiteMember> resp = new ListResponse<SiteMember>(paging, siteMembers);
		return resp;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject entry = new JSONObject();
		entry.put("id", getMemberId());
		if(getRole() != null)
		{
			entry.put("role", getRole());
		}

		return entry;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject postJSON()
	{
		JSONObject entry = new JSONObject();
		entry.put("id", getMemberId());
		entry.put("role", getRole());

		return entry;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((memberId == null) ? 0 : memberId.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((siteId == null) ? 0 : siteId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SiteMember other = (SiteMember) obj;
		if (memberId == null) {
			if (other.memberId != null)
				return false;
		} else if (!memberId.equals(other.memberId))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		if (siteId == null) {
			if (other.siteId != null)
				return false;
		} else if (!siteId.equals(other.siteId))
			return false;
		return true;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof SiteMember);
		
		SiteMember other = (SiteMember)o;
		
		AssertUtil.assertEquals("memberId", memberId, other.getMemberId());
		if(member != null)
		{
			member.expected(other.getMember());
		}
		AssertUtil.assertEquals("siteId", siteId, other.getSiteId());
		AssertUtil.assertEquals("role", role, other.getRole());
		if(status != null)
		{
			status.expected(other.getStatus());
		}
	}

	private Collator collator = Collator.getInstance();

	@Override
	public int compareTo(SiteMember o)
	{
		String firstName = member.getFirstName();
		String lastName = member.getLastName();
		int ret = collator.compare(lastName, o.getMember().getLastName());
		if(ret == 0)
		{
			ret = collator.compare(firstName, o.getMember().getFirstName());
		}
		if(ret == 0)
		{
			ret = SiteRole.valueOf(role).compareTo(SiteRole.valueOf(o.getRole()));
		}
		return ret;
	}

}
