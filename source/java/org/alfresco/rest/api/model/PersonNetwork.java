/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.rest.api.model;

import java.util.Date;
import java.util.List;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Represents network membership.
 * 
 * @author steveglover
 *
 */
public class PersonNetwork implements Network, Comparable<PersonNetwork>
{
	private Boolean homeNetwork;
	private Network network;

	public PersonNetwork()
	{
	}

	public PersonNetwork(Boolean homeNetwork, Network network)
	{
		super();
		this.homeNetwork = homeNetwork;
		this.network = network;
	}

	@UniqueId
    public String getId()
    {
    	return network.getId();
    }
    
    public Date getCreatedAt()
    {
    	return network.getCreatedAt();
    }

	public List<Quota> getQuotas()
    {
    	return network.getQuotas();
    }

    public Boolean getIsEnabled()
    {
    	return network.getIsEnabled();
    }
    
    public String getSubscriptionLevel()
    {
    	return network.getSubscriptionLevel();
    }
    
	public Boolean getPaidNetwork()
	{
    	return network.getPaidNetwork();		
	}

	@Override
	public int compareTo(PersonNetwork member)
	{
		int ret = getId().compareTo(member.getId());
		return ret;
	}
	
	public Boolean getHomeNetwork()
	{
		return homeNetwork;
	}

	@Override
	public int hashCode()
	{
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
		if (network == null)
		{
			if (other.network != null)
				return false;
		}
		else if (!network.equals(other.network))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "PersonNetwork [homeNetwork=" + homeNetwork + ", network="
				+ network + "]";
	}
}
