package org.alfresco.repo.tenant;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Network extends Tenant implements Comparable<Network>
{
	protected Date createdAt; // Cloud only
	protected Boolean isHomeNetwork;
	protected List<Quota> quotas = new LinkedList<Quota>(); // Cloud only
	protected String subscriptionLevel; // Cloud only
    protected Boolean paidNetwork; // Cloud only

    public Network(String tenantDomain, boolean enabled, String rootContentStoreDir, String dbUrl)
    {
    	super(tenantDomain, enabled, rootContentStoreDir, dbUrl);
    }

	public Network(Tenant tenant, Boolean isHomeNetwork, Date createdAt, String subscriptionLevel, Boolean paidNetwork, List<Quota> quotas)
	{
		super(tenant.getTenantDomain(), tenant.isEnabled(), tenant.getRootContentStoreDir(), tenant.getDbUrl());		
		this.isHomeNetwork = isHomeNetwork;
		this.createdAt = createdAt;
		this.subscriptionLevel = subscriptionLevel;
		this.paidNetwork = paidNetwork;
		this.quotas = quotas;
	}
	
	public Date getCreatedAt()
	{
		return createdAt;
	}

	public Boolean getIsHomeNetwork()
	{
		return isHomeNetwork;
	}

	public String getSubscriptionLevel()
	{
		return subscriptionLevel;
	}

	public Boolean getPaidNetwork()
	{
		return paidNetwork;
	}
	
	public List<Quota> getQuotas()
	{
		return quotas;
	}

	@Override
	public String toString()
	{
		return "Network [createdAt=" + createdAt + ", isHomeNetwork="
				+ isHomeNetwork + ", quotas=" + quotas + ", subscriptionLevel="
				+ subscriptionLevel + ", paidNetwork=" + paidNetwork + "]";
	}

	@Override
	public int compareTo(Network o)
	{
		int ret = getTenantDomain().compareTo(o.getTenantDomain());
		return ret;
	}
}
