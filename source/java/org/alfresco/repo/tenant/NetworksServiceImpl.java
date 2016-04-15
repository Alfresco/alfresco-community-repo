/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.tenant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.query.PageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.util.Pair;

public class NetworksServiceImpl implements NetworksService
{
	public static final Network DEFAULT_NETWORK = new Network(TenantUtil.DEFAULT_TENANT, true, null, null);

    private TenantAdminService tenantAdminService;

    public NetworksServiceImpl()
    {
    }

	public void setTenantAdminService(TenantAdminService tenantAdminService)
	{
		this.tenantAdminService = tenantAdminService;
	}
	
	private boolean hasAccess(String networkId)
	{
		String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
    	String authNetworkId = tenantAdminService.getUserDomain(currentUser);
		// check that the currently authenticated user is in the same network as that being requested.
		// Allow only if this is the case.
		return authNetworkId.equalsIgnoreCase(networkId);
	}
	
	public Network getNetwork(String networkId)
	{
		Network network = null;

        if(networkId.equals(TenantUtil.SYSTEM_TENANT) || networkId.equals(TenantUtil.DEFAULT_TENANT))
        {
        	return DEFAULT_NETWORK;
        }
        else if(tenantAdminService.existsTenant(networkId))
		{
			Tenant tenant = tenantAdminService.getTenant(networkId);
			if(hasAccess(networkId))
			{
				// if the user has access, then this must be their home network
				network = new Network(tenant, true, null, null, null, null);
			}
			else
			{
				throw new AccessDeniedException("Cannot get network, no permission");
			}
		}

		return network;
	}

	public PagingResults<Network> getNetworks(PagingRequest pagingRequest)
	{
		String username = AuthenticationUtil.getFullyAuthenticatedUser();
        
        // remap tenant admin to system admin
        String admin = tenantAdminService.getBaseNameUser(AuthenticationUtil.getAdminUserName());
        String user = tenantAdminService.getBaseNameUser(username);

        List<Network> networks = null;
        if (user.equalsIgnoreCase(admin))
        {
            // admin
        	networks = new ArrayList<Network>(1);
        	String tenantId = tenantAdminService.getUserDomain(username);
        	if(tenantId != null && tenantId.equals(""))
        	{
	        	Network network = DEFAULT_NETWORK;
	        	networks.add(network);        		
        	}
        	else
        	{
            	Tenant tenant = tenantAdminService.getTenant(tenantId);
    			Network network = new Network(tenant, false, null, null, null, null);
            	networks.add(network);        		
        	}
        }
        else
        {
        	// For Enterprise, the user has at most one network (their home network/tenant)
        	String userDomain = tenantAdminService.getUserDomain(username);

        	networks = new ArrayList<Network>(1);

        	if(userDomain != null && userDomain.equals(""))
        	{
	        	Network network = DEFAULT_NETWORK;
	        	networks.add(network);
        	}
        	else
        	{
	        	Tenant tenant = tenantAdminService.getTenant(userDomain);
	        	Network network = new Network(tenant, true, null, null, null, null);
	        	networks.add(network);
        	}
        }

        final int totalSize = networks.size();
        final PageDetails pageDetails = PageDetails.getPageDetails(pagingRequest, totalSize);

		final List<Network> page = new ArrayList<Network>(pageDetails.getPageSize());
		Iterator<Network> it = networks.iterator();
        for(int counter = 0; counter < pageDetails.getEnd() && it.hasNext(); counter++)
        {
        	Network network = it.next();

			if(counter < pageDetails.getSkipCount())
			{
				continue;
			}
			
			if(counter > pageDetails.getEnd() - 1)
			{
				break;
			}


			page.add(network);
        }

        return new PagingResults<Network>()
        {
			@Override
			public List<Network> getPage()
			{
				return page;
			}

			@Override
			public boolean hasMoreItems()
			{
				return pageDetails.hasMoreItems();
			}

			@Override
			public Pair<Integer, Integer> getTotalResultCount()
			{
				Integer total = Integer.valueOf(totalSize);
				return new Pair<Integer, Integer>(total, total);
			}

			@Override
			public String getQueryExecutionId()
			{
				return null;
			}
        };
	}

    public String getUserDefaultNetwork(String user)
    {
    	Pair<String, String> pair = AuthenticationUtil.getUserTenant(user);
    	return pair.getSecond();
    }
}
