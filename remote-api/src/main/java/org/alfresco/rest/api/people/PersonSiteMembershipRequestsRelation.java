/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.people;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.rest.api.SiteMembershipRequests;
import org.alfresco.rest.api.model.SiteMembershipRequest;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

@RelationshipResource(name = "site-membership-requests", entityResource = PeopleEntityResource.class, title = "Site Membership Requests")
public class PersonSiteMembershipRequestsRelation implements RelationshipResourceAction.Read<SiteMembershipRequest>, RelationshipResourceAction.Delete, RelationshipResourceAction.Update<SiteMembershipRequest>, 
RelationshipResourceAction.Create<SiteMembershipRequest>, RelationshipResourceAction.ReadById<SiteMembershipRequest>, InitializingBean
{
    private static final Log logger = LogFactory.getLog(PersonSiteMembershipRequestsRelation.class);

	private SiteMembershipRequests siteMembershipRequests;

	public void setSiteMembershipRequests(SiteMembershipRequests siteMembershipRequests)
	{
		this.siteMembershipRequests = siteMembershipRequests;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
	}

    @Override
    @WebApiDescription(title = "The site membership request for personId and siteId, if it exists.")
	public SiteMembershipRequest readById(String personId, String siteId, Parameters parameters) throws RelationshipResourceNotFoundException
	{
		SiteMembershipRequest siteInvite = siteMembershipRequests.getSiteMembershipRequest(personId, siteId);
		return siteInvite;
	}

	@Override
    @WebApiDescription(title = "Create a site membership request for personId and siteIds. The personId will be invited to the site as a SiteConsumer.")
	public List<SiteMembershipRequest> create(String personId, List<SiteMembershipRequest> invites, Parameters parameters)
	{
        List<SiteMembershipRequest> result = new ArrayList<SiteMembershipRequest>(invites.size());
		for(SiteMembershipRequest invite : invites)
		{
			SiteMembershipRequest siteInvite = null;
			if(invite.getClient() != null)
			{
				siteInvite = siteMembershipRequests.createSiteMembershipRequest(personId, invite, invite.getClient());
			} else
			{
				siteInvite = siteMembershipRequests.createSiteMembershipRequest(personId, invite);
			}
			result.add(siteInvite);
		}
		return result;
	}

	@Override
    @WebApiDescription(title = "Remove an existing site membership request for personId and siteId, if it exists.")
	public void delete(String personId, String siteId, Parameters parameters)
	{
		siteMembershipRequests.cancelSiteMembershipRequest(personId, siteId);
	}

	@Override
    @WebApiDescription(title = "A paged list of site membership requests for personId.")
	public CollectionWithPagingInfo<SiteMembershipRequest> readAll(String personId, Parameters parameters)
	{
    	return siteMembershipRequests.getPagedSiteMembershipRequests(personId, parameters.getPaging());
	}

	@Override
    @WebApiDescription(title = "Update the comment for the site membership request for personId and siteId.")
	public SiteMembershipRequest update(String personId, SiteMembershipRequest siteInvite, Parameters parameters)
	{
		return siteMembershipRequests.updateSiteMembershipRequest(personId, siteInvite);
	}
}