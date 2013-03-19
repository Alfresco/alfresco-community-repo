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
package org.alfresco.repo.site;

import org.alfresco.query.AbstractCannedQueryFactory;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;

/**
 * A factory for creating site membership canned queries.
 * 
 * @author steveglover
 *
 */
public class SiteMembershipCannedQueryFactory extends AbstractCannedQueryFactory<SiteMembership>
{
	private NodeService nodeService;
	private PersonService personService;
    private AuthorityService authorityService;
    private SiteService siteService;
    
    public void setAuthorityService(AuthorityService authorityService)
    {
		this.authorityService = authorityService;
	}
        
    public void setSiteService(SiteService siteService)
    {
		this.siteService = siteService;
	}

	public void setNodeService(NodeService nodeService)
    {
		this.nodeService = nodeService;
	}

	public void setPersonService(PersonService personService)
	{
		this.personService = personService;
	}

	@Override
    public CannedQuery<SiteMembership> getCannedQuery(CannedQueryParameters parameters)
    {
    	Object parameterBean = parameters.getParameterBean();
    	CannedQuery<SiteMembership> cq = null;
    	if(parameterBean instanceof SitesCannedQueryParams)
    	{
    		cq = new SitesCannedQuery(authorityService, siteService, parameters);
    	}
    	else if(parameterBean instanceof SiteMembersCannedQueryParams)
    	{
    		cq = new SiteMembersCannedQuery(siteService, personService, nodeService, parameters);
    	}
        return (CannedQuery<SiteMembership>) cq;
    }

}
