package org.alfresco.repo.policy;

import java.util.Collection;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * @author David Caruana
 */
/*package*/ @AlfrescoPublicApi interface PolicyList<P extends Policy>
{
	/**
	 * @return the set of policies within this policy set
	 */
	public Collection<P> getPolicies();
}
