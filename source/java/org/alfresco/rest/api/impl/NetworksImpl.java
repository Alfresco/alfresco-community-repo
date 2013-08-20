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
package org.alfresco.rest.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.query.PagingResults;
import org.alfresco.repo.tenant.NetworksService;
import org.alfresco.rest.api.Networks;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.model.Network;
import org.alfresco.rest.api.model.NetworkImpl;
import org.alfresco.rest.api.model.PersonNetwork;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;

/**
 * Centralises access to network services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class NetworksImpl implements Networks
{
	private People people;
	private NetworksService networksService;
	
	public void setPeople(People people)
	{
		this.people = people;
	}

	public void setNetworksService(NetworksService networksService)
	{
		this.networksService = networksService;
	}

	public Network validateNetwork(String networkId)
    {
		org.alfresco.repo.tenant.Network network = networksService.getNetwork(networkId);
		if(network == null)
		{
			throw new EntityNotFoundException(networkId);
		}
		Network restNetwork = new NetworkImpl(network);
		return restNetwork;
    }
	
    private PersonNetwork getPersonNetwork(org.alfresco.repo.tenant.Network network)
    {
		Network restNetwork = new NetworkImpl(network);
		PersonNetwork personNetwork = new PersonNetwork(network.getIsHomeNetwork(), restNetwork);
		return personNetwork;
    }
    
    public Network getNetwork(String networkId)
    {
    	Network network = validateNetwork(networkId);
    	return network;
    }
    
    public PersonNetwork getNetwork(String personId, String networkId)
    {
    	// check that personId is the current user
    	personId = people.validatePerson(personId, true);
    	Network network = validateNetwork(networkId);

    	org.alfresco.repo.tenant.Network tenantNetwork = networksService.getNetwork(network.getId());
		PersonNetwork personNetwork = getPersonNetwork(tenantNetwork);
		return personNetwork;
    }

	public CollectionWithPagingInfo<PersonNetwork> getNetworks(String personId, Paging paging)
    {
    	// check that personId is the current user
    	personId = people.validatePerson(personId, true);

    	PagingResults<org.alfresco.repo.tenant.Network> networks = networksService.getNetworks(Util.getPagingRequest(paging));
    	List<PersonNetwork> ret = new ArrayList<PersonNetwork>(networks.getPage().size());
    	for(org.alfresco.repo.tenant.Network network : networks.getPage())
		{
    		PersonNetwork personNetwork = getPersonNetwork(network);
    		ret.add(personNetwork);
		}
    	return CollectionWithPagingInfo.asPaged(paging, ret, networks.hasMoreItems(), networks.getTotalResultCount().getFirst());
    }
}
