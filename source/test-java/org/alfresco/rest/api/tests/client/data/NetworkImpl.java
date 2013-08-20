package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.rest.api.tests.PublicApiDateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class NetworkImpl implements Network, Serializable, ExpectedComparison
{
	private static final long serialVersionUID = -5748677903173568273L;

	protected String id;
	protected Boolean isEnabled;
	protected String createdAt;
	protected List<Quota> quotas = new LinkedList<Quota>();
	protected String subscriptionLevel;
    protected Boolean paidNetwork;

    public NetworkImpl(org.alfresco.repo.tenant.Network network)
    {
    	this.id = network.getTenantDomain();
    	this.createdAt = (network.getCreatedAt() != null ? PublicApiDateFormat.getDateFormat().format(network.getCreatedAt()) : null);
    	this.isEnabled = network.isEnabled();
    	this.paidNetwork = network.getPaidNetwork();
    	this.subscriptionLevel = network.getSubscriptionLevel();
    }
    
	public NetworkImpl(String id, Boolean isEnabled)
	{
		super();
		this.id = id;
		this.isEnabled = isEnabled;
	}
	
	public NetworkImpl(String id, Boolean isEnabled, String createdAt,
			List<Quota> quotas, String subscriptionLevel, Boolean paidNetwork)
	{
		super();
		this.id = id;
		this.isEnabled = isEnabled;
		this.createdAt = createdAt;
		this.quotas = quotas;
		this.subscriptionLevel = subscriptionLevel;
		this.paidNetwork = paidNetwork;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Boolean getIsEnabled()
	{
		return isEnabled;
	}

	public void setIsEnabled(Boolean isEnabled)
	{
		this.isEnabled = isEnabled;
	}
	
	public Boolean getPaidNetwork()
	{
		return paidNetwork;
	}

	public void setPaidNetwork(Boolean paidNetwork)
	{
		this.paidNetwork = paidNetwork;
	}

	public void setCreatedAt(String createdAt)
	{
		this.createdAt = createdAt;
	}

	public void setQuotas(List<Quota> quotas)
	{
		this.quotas = quotas;
	}

	public void setSubscriptionLevel(String subscriptionLevel)
	{
		this.subscriptionLevel = subscriptionLevel;
	}

	@SuppressWarnings("rawtypes")
	public static NetworkImpl parseNetwork(JSONObject jsonObject)
	{
		String id = (String)jsonObject.get("id");
		Boolean isEnabled = (Boolean)jsonObject.get("isEnabled");
		Boolean paidNetwork = (Boolean)jsonObject.get("paidNetwork");
		String createdAt = (String)jsonObject.get("createdAt");
		String subscriptionLevel = (String)jsonObject.get("subscriptionLevel");
		JSONArray quotasJSON = (JSONArray)jsonObject.get("quotas");
		List<org.alfresco.rest.api.tests.client.data.Quota> quotas = new ArrayList<org.alfresco.rest.api.tests.client.data.Quota>(quotasJSON.size());
		Iterator it = quotasJSON.iterator();
		while(it.hasNext())
		{
			JSONObject quotaJSON = (JSONObject)it.next();
			org.alfresco.rest.api.tests.client.data.Quota quota = org.alfresco.rest.api.tests.client.data.Quota.parseQuota(quotaJSON);
			quotas.add(quota);
		}
		NetworkImpl network = new NetworkImpl(id, isEnabled, createdAt, quotas, subscriptionLevel, paidNetwork);
		return network;
	}
	
	public String getCreatedAt()
	{
		return createdAt;
	}

	public List<Quota> getQuotas()
	{
		return quotas;
	}

	public String getSubscriptionLevel()
	{
		return subscriptionLevel;
	}

	public Boolean isPaidNetwork()
	{
		return paidNetwork;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject networkJson = new JSONObject();
		networkJson.put("id", getId());
		networkJson.put("isEnabled", getIsEnabled());
		
		return networkJson;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		NetworkImpl other = (NetworkImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
	@Override
	public void expected(Object o)
	{
		assertTrue(o instanceof NetworkImpl);
		
		NetworkImpl other = (NetworkImpl)o;

		AssertUtil.assertEquals("id", id, other.getId());
		AssertUtil.assertEquals("isEnabled", isEnabled, other.isEnabled);
		AssertUtil.assertEquals("paidNetwork", paidNetwork, other.paidNetwork);
		AssertUtil.assertEquals("subscriptionLevel", subscriptionLevel, other.subscriptionLevel);
		
		if(createdAt != null)
		{
			try
			{
				Date created = PublicApiDateFormat.getDateFormat().parse(createdAt);
				Date otherCreatedAt = PublicApiDateFormat.getDateFormat().parse(other.getCreatedAt());
				assertTrue(otherCreatedAt.after(created) || otherCreatedAt.equals(created));
			}
			catch(ParseException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String toString()
	{
		return "NetworkImpl [id=" + id + ", isEnabled=" + isEnabled
				+ ", createdAt=" + createdAt + ", quotas=" + quotas
				+ ", subscriptionLevel=" + subscriptionLevel + ", paidNetwork="
				+ paidNetwork + "]";
	}
}
