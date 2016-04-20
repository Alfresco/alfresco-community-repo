package org.alfresco.repo.tenant;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;

/**
 * A service that provides information on networks.
 * 
 * @author steveglover
 *
 */
public interface NetworksService
{
	/**
	 * Get the currently authenticated user's specific network membership
	 * 
	 */
	Network getNetwork(String networkId);

	/**
	 * Get the currently authenticated user's network memberships, sorted in ascending order by networkId
	 * 
	 */
	PagingResults<Network> getNetworks(PagingRequest pagingRequest);
	
	String getUserDefaultNetwork(String user);
}
