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
	 * @return
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
