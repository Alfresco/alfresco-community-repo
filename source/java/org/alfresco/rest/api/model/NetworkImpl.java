package org.alfresco.rest.api.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Represents a cloud network (account).
 * 
 * @author steveglover
 *
 */
public class NetworkImpl implements Comparable<NetworkImpl>, Network
{
	private String id;
	private Date createdAt;
	private List<Quota> quotas = new LinkedList<Quota>();
    private Boolean isEnabled;
    private String subscriptionLevel;
    private Boolean paidNetwork;
    
    public NetworkImpl(org.alfresco.repo.tenant.Network network)
    {
    	this.id = network.getTenantDomain();
    	this.createdAt = network.getCreatedAt();
    	this.isEnabled = network.isEnabled();
    	this.paidNetwork = network.getPaidNetwork();
    	this.subscriptionLevel = network.getSubscriptionLevel();
    }

    public NetworkImpl(String id, Date createdAt, Boolean isEnabled, String subscriptionLevel, Boolean paidNetwork)
    {
    	this.id = id;
    	this.createdAt = createdAt;
    	this.isEnabled = isEnabled;
    	this.subscriptionLevel = subscriptionLevel;
    	this.paidNetwork = paidNetwork;
    }

    /**
     * Get the account name
     * 
     * @return The name of the account
     */
    @UniqueId
    public String getId()
    {
    	return id;
    }
    
    /**
     * Gets the date the account was created
     *
     * @return  The account creation date
     */
    public Date getCreatedAt()
    {
    	return createdAt;
    }
    
    public List<Quota> getQuotas()
    {
		return quotas;
	}

	/**
     * Gets whether an account is enabled or not. 
     *
     * @return true = account is enabled, false = account is disabled
     */
    public Boolean getIsEnabled()
    {
    	return isEnabled;
    }
    
    /**
     * Gets the subscription level.
     * 
     * @return ths subscription level
     */
    public String getSubscriptionLevel()
    {
    	return subscriptionLevel;
    }
    
	public Boolean getPaidNetwork()
	{
		return paidNetwork;
	}

	@Override
	public int compareTo(NetworkImpl network)
	{
		return id.compareTo(network.getId());
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
		
		Network other = (Network) obj;
		return(id.equals(other.getId()));
	}

	@Override
	public String toString()
	{
		return "Network [id=" + id
				+ ", createdAt=" + createdAt + ", quotas=" + quotas
				+ ", isEnabled=" + isEnabled + ", subscriptionLevel="
				+ subscriptionLevel + "]";
	}

}
