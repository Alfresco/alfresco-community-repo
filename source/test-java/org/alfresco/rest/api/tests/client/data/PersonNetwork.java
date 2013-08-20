package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.tests.client.PublicApiClient.ExpectedPaging;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PersonNetwork implements Network, Comparable<PersonNetwork>, ExpectedComparison
{
	private String networkId;
	private Boolean homeNetwork;
	private NetworkImpl network;
	
	public PersonNetwork(String networkId)
	{
		super();
	}

	public PersonNetwork(Boolean homeNetwork, NetworkImpl network)
	{
		super();
		this.homeNetwork = homeNetwork;
		this.network = network;
		this.networkId = network.getId();
	}
	
	public String getId()
	{
		return networkId;
	}
	
	public Boolean isHomeNetwork()
	{
		return homeNetwork;
	}
	
	public NetworkImpl getNetwork()
	{
		return network;
	}
	
	public static PersonNetwork parseNetworkMember(JSONObject jsonObject)
	{
		Boolean homeNetwork = (Boolean)jsonObject.get("homeNetwork");
		PersonNetwork networkMember = new PersonNetwork(homeNetwork, NetworkImpl.parseNetwork(jsonObject));
		return networkMember;
	}
	
	public static ListResponse<PersonNetwork> parseNetworkMembers(JSONObject jsonObject)
	{
		List<PersonNetwork> networkMembers = new ArrayList<PersonNetwork>();

		JSONObject jsonList = (JSONObject)jsonObject.get("list");
		assertNotNull(jsonList);

		JSONArray jsonEntries = (JSONArray)jsonList.get("entries");
		assertNotNull(jsonEntries);

		for(int i = 0; i < jsonEntries.size(); i++)
		{
			JSONObject jsonEntry = (JSONObject)jsonEntries.get(i);
			JSONObject entry = (JSONObject)jsonEntry.get("entry");
			networkMembers.add(PersonNetwork.parseNetworkMember(entry));
		}

		ExpectedPaging paging = ExpectedPaging.parsePagination(jsonList);
		ListResponse<PersonNetwork> resp = new ListResponse<PersonNetwork>(paging, networkMembers);
		return resp;
	}

	@Override
	public String toString()
	{
		return "NetworkMember [id=" + getId() + ", homeNetwork=" + homeNetwork
				+ ", network=" + network + "]";
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject networkMemberJson = new JSONObject();
		networkMemberJson.put("id", getId());
		networkMemberJson.put("homeNetwork", isHomeNetwork());
		networkMemberJson.put("network", getNetwork());
		return networkMemberJson;
	}
	
	@Override
	public int compareTo(PersonNetwork member)
	{
		int ret = -1 * isHomeNetwork().compareTo(member.isHomeNetwork());
		if(ret == 0)
		{
			ret = getId().compareTo(member.getId());
		}
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((network == null) ? 0 : network.hashCode());
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
		PersonNetwork other = (PersonNetwork) obj;
		if (network == null) {
			if (other.network != null)
				return false;
		} else if (!network.equals(other.network))
			return false;
		return true;
	}

	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof PersonNetwork);
		
		PersonNetwork other = (PersonNetwork)o;
		
		AssertUtil.assertEquals("homeNetwork", homeNetwork, other.isHomeNetwork());
		if(network != null)
		{
			network.expected(other.getNetwork());
		}
	}

	@Override
	public Boolean getIsEnabled()
	{
		return network.getIsEnabled();
	}

	@Override
	public String getCreatedAt()
	{
		return network.getCreatedAt();
	}

	@Override
	public List<Quota> getQuotas()
	{
		return network.getQuotas();
	}

	@Override
	public String getSubscriptionLevel()
	{
		return network.getSubscriptionLevel();
	}

	@Override
	public Boolean isPaidNetwork()
	{
		return network.isPaidNetwork();
	}
}
