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
package org.alfresco.rest.api;

import org.alfresco.rest.api.model.SiteMembershipRequest;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;

/**
 * Public REST API: centralises access to site membership requests and maps between representations.
 * 
 * @author steveglover
 *
 */
public interface SiteMembershipRequests
{
	/**
	 * Create a site membership request for the user 'inviteeId'
	 * @param inviteeId the site inviteee id
	 * @param siteInvite the site invite
	 * @return SiteMembershipRequest
	 */
	SiteMembershipRequest createSiteMembershipRequest(String inviteeId, final SiteMembershipRequest siteInvite);
	
	/**
	 * Update the site membership request for inviteeId and site
	 * @param inviteeId the site inviteee id
	 * @param siteInvite the site invite
	 * @return the updated siteMembershipRequest
	 */
	SiteMembershipRequest updateSiteMembershipRequest(String inviteeId, final SiteMembershipRequest siteInvite);
	
	/**
	 * Cancel site membership request for invitee and site.
	 * 
	 * @param inviteeId the site inviteee id
	 * @param siteId the site id
	 */
	void cancelSiteMembershipRequest(String inviteeId, String siteId);
	
	/**
	 * Get the site membership request for inviteeId and siteId, if it exists.
	 * 
	 * @param inviteeId the site inviteee id
	 * @param siteId the site id
	 * @return the site membership request
	 */
	SiteMembershipRequest getSiteMembershipRequest(String inviteeId, String siteId);
	
	/**
	 * Get a paged list of site membership requests for inviteeId.
	 * 
	 * @param inviteeId the site inviteee id
	 * @param paging paging information
	 * @return a paged list of site membership requests
	 */
	CollectionWithPagingInfo<SiteMembershipRequest> getPagedSiteMembershipRequests(String inviteeId, Paging paging);
}
