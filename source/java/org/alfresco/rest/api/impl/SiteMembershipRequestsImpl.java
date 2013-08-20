package org.alfresco.rest.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.invitation.InvitationSearchCriteriaImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.NetworksService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.SiteMembershipRequests;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.SiteMembershipRequest;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria.InvitationType;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Public REST API: centralises access to site membership requests and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class SiteMembershipRequestsImpl implements SiteMembershipRequests
{
    private static final Log logger = LogFactory.getLog(SiteMembershipRequestsImpl.class);
	
	// Default role to assign to the site membership request
	public final static String DEFAULT_ROLE = SiteModel.SITE_CONSUMER;

	private People people;
	private Sites sites;
	private SiteService siteService;
	private NodeService nodeService;
	private InvitationService invitationService;
	private NetworksService networksService;

	public void setNetworksService(NetworksService networksService)
	{
		this.networksService = networksService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setPeople(People people)
	{
		this.people = people;
	}

	public void setSites(Sites sites)
	{
		this.sites = sites;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setInvitationService(InvitationService invitationService)
	{
		this.invitationService = invitationService;
	}

	private Invitation getSiteInvitation(String inviteeId, String siteId)
    {
		// Is there an outstanding site invite request for the invitee?
		InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
		criteria.setInvitationType(InvitationType.MODERATED);
		criteria.setInvitee(inviteeId);
		criteria.setResourceName(siteId);
		criteria.setResourceType(ResourceType.WEB_SITE);
		List<Invitation> invitations = invitationService.searchInvitation(criteria);
		if(invitations.size() > 1)
		{
			// TODO exception
			throw new AlfrescoRuntimeException("There should be only one outstanding site invitation");
		}
		return (invitations.size() == 0 ? null : invitations.get(0));
    }
	
	private List<Invitation> getSiteInvitations(String inviteeId)
    {
		InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
		criteria.setInvitationType(InvitationType.MODERATED);
		criteria.setInvitee(inviteeId);
		criteria.setResourceType(ResourceType.WEB_SITE);
		List<Invitation> invitations = invitationService.searchInvitation(criteria);
		return invitations;
    }
	
	private SiteMembershipRequest inviteToModeratedSite(final String message, final String inviteeId, final String siteId,
			final String inviteeRole)
	{
		ModeratedInvitation invitation = invitationService.inviteModerated(message, inviteeId, ResourceType.WEB_SITE, siteId, inviteeRole);

		SiteMembershipRequest ret = new SiteMembershipRequest();
		ret.setId(siteId);
		ret.setMessage(message);
		ret.setCreatedAt(invitation.getCreatedAt());
		return ret;
	}
	
	private SiteMembershipRequest inviteToSite(String siteId, String inviteeId, String inviteeRole, String message)
	{
		siteService.setMembership(siteId, inviteeId, inviteeRole);
		SiteMembershipRequest ret = new SiteMembershipRequest();
		ret.setId(siteId);
		ret.setMessage(message);
		Date createdAt = new Date();
		ret.setCreatedAt(createdAt);
		return ret;
	}
	
	private SiteMembershipRequest inviteToPublicSite(final SiteInfo siteInfo, final String message, final String inviteeId,
			final String inviteeRole)
	{
		SiteMembershipRequest siteMembershipRequest = null;

		final String siteId = siteInfo.getShortName();
		NodeRef siteNodeRef = siteInfo.getNodeRef();
		String siteCreator = (String)nodeService.getProperty(siteNodeRef, ContentModel.PROP_CREATOR);

		final String siteNetwork = networksService.getUserDefaultNetwork(siteCreator);
		if(StringUtils.isNotEmpty(siteNetwork))
		{
			// MT
			siteMembershipRequest = TenantUtil.runAsUserTenant(new TenantRunAsWork<SiteMembershipRequest>()
			{
				@Override
				public SiteMembershipRequest doWork() throws Exception
				{
					return inviteToSite(siteId, inviteeId, inviteeRole, message);
				}
			}, siteCreator, siteNetwork);
		}
		else
		{
			siteMembershipRequest = AuthenticationUtil.runAs(new RunAsWork<SiteMembershipRequest>()
			{
				@Override
				public SiteMembershipRequest doWork() throws Exception
				{
					return inviteToSite(siteId, inviteeId, inviteeRole, message);
				}
			}, siteCreator);
		}

		return siteMembershipRequest;
	}

    @Override
	public SiteMembershipRequest createSiteMembershipRequest(String inviteeId, final SiteMembershipRequest siteInvite)
	{
    	SiteMembershipRequest request = null;

    	inviteeId = people.validatePerson(inviteeId, true);

    	// Note that the order of error checking is important. The server first needs to check for the status 404 
    	// conditions before checking for status 400 conditions. Otherwise the server is open to a probing attack. 
		String siteId = siteInvite.getId();
		final SiteInfo siteInfo = sites.validateSite(siteId);
    	if(siteInfo == null)
    	{
    		// site does not exist
    		throw new RelationshipResourceNotFoundException(inviteeId, siteId);
    	}
		// set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
		siteId = siteInfo.getShortName();

		final SiteVisibility siteVisibility = siteInfo.getVisibility();

		if(siteVisibility.equals(SiteVisibility.PRIVATE))
		{
			// note: security, no indication that this is a private site
			throw new RelationshipResourceNotFoundException(inviteeId, siteId);
		}

		// Is the invitee already a member of the site?
		boolean isMember = siteService.isMember(siteId, inviteeId);
		if(isMember)
		{
			// yes
			throw new InvalidArgumentException(inviteeId + " is already a member of site " + siteId);
		}

		// Is there an outstanding site invite request for the (invitee, site)?
		Invitation invitation = getSiteInvitation(inviteeId, siteId);
		if(invitation != null)
		{
			// yes
			throw new InvalidArgumentException(inviteeId + " is already invited to site " + siteId);
		}

		final String inviteeRole = DEFAULT_ROLE;
		String message = siteInvite.getMessage();
		if(message == null)
		{
			// the invitation service ignores null messages so convert to an empty message.
			message = "";
		}

		if(siteVisibility.equals(SiteVisibility.MODERATED))
		{
			request = inviteToModeratedSite(message, inviteeId, siteId, inviteeRole);
		}
		else if(siteVisibility.equals(SiteVisibility.PUBLIC))
		{
			request = inviteToPublicSite(siteInfo, message, inviteeId, inviteeRole);
		}
		else
		{
			// note: security, no indication that this is a private site
			throw new RelationshipResourceNotFoundException(inviteeId, siteId);
		}

		return request;
	}

    @Override
	public SiteMembershipRequest updateSiteMembershipRequest(String inviteeId, final SiteMembershipRequest siteInvite)
	{
    	SiteMembershipRequest updatedSiteInvite = null;

		inviteeId = people.validatePerson(inviteeId, true);

		String siteId = siteInvite.getId();
		SiteInfo siteInfo = sites.validateSite(siteId);
    	if(siteInfo == null)
    	{
    		// site does not exist
    		throw new RelationshipResourceNotFoundException(inviteeId, siteId);
    	}
		// set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
		siteId = siteInfo.getShortName();

		String message = siteInvite.getMessage();
		if(message == null)
		{
			// the invitation service ignores null messages so convert to an empty message.
			message = "";
		}

		try
		{
			ModeratedInvitation updatedInvitation = invitationService.updateModeratedInvitation(inviteeId, siteId, message);
			if(updatedInvitation == null)
			{
	    		throw new RelationshipResourceNotFoundException(inviteeId, siteId);
			}
			updatedSiteInvite = getSiteMembershipRequest(updatedInvitation);
		}
		catch(InvitationExceptionNotFound e)
		{
			throw new RelationshipResourceNotFoundException(inviteeId, siteId);
		}

		if(updatedSiteInvite == null)
		{
			throw new RelationshipResourceNotFoundException(inviteeId, siteId);
		}
		
		return updatedSiteInvite;
	}

    @Override
	public void cancelSiteMembershipRequest(String inviteeId, String siteId)
	{
		inviteeId = people.validatePerson(inviteeId);
		SiteInfo siteInfo = sites.validateSite(siteId);
    	if(siteInfo == null)
    	{
    		// site does not exist
    		throw new RelationshipResourceNotFoundException(inviteeId, siteId);
    	}
		// set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
		siteId = siteInfo.getShortName();

		Invitation invitation = getSiteInvitation(inviteeId, siteId);
		if(invitation == null)
		{
			// no such invitation
    		throw new RelationshipResourceNotFoundException(inviteeId, siteId);
		}
		invitationService.cancel(invitation.getInviteId());
	}
	
	public SiteMembershipRequest getSiteMembershipRequest(String inviteeId, final String siteId)
	{
		inviteeId = people.validatePerson(inviteeId);

		SiteInfo siteInfo = AuthenticationUtil.runAsSystem(new RunAsWork<SiteInfo>()
		{
			@Override
			public SiteInfo doWork() throws Exception
			{
				SiteInfo siteInfo = sites.validateSite(siteId);
				return siteInfo;
			}
		});
		
		if(siteInfo.getVisibility().equals(SiteVisibility.MODERATED))
		{
			// set the site id to the short name (to deal with case sensitivity issues with using the siteId from the url)
			String normalizedSiteId = siteInfo.getShortName();
	
			Invitation invitation = getSiteInvitation(inviteeId, normalizedSiteId);
			if(invitation == null)
			{
				// no such invitation
	    		throw new RelationshipResourceNotFoundException(inviteeId, normalizedSiteId);
			}
			if(invitation instanceof ModeratedInvitation)
			{
				ModeratedInvitation moderatedInvitation = (ModeratedInvitation)invitation;
				SiteMembershipRequest siteInvite = getSiteMembershipRequest(moderatedInvitation);
				return siteInvite;
			}
			else
			{
				throw new InvalidArgumentException("Expected moderated invitation");
			}
		}
		else
		{
			// non-moderated sites cannot appear in a site membership request, so throw an exception
			throw new RelationshipResourceNotFoundException(inviteeId, siteId);
		}
	}
	
	private SiteMembershipRequest getSiteMembershipRequest(ModeratedInvitation moderatedInvitation)
	{
		SiteMembershipRequest siteMembershipRequest = null;

		ResourceType resourceType = moderatedInvitation.getResourceType();
		if(resourceType.equals(ResourceType.WEB_SITE))
		{
			final String siteId = moderatedInvitation.getResourceName();
			
			SiteInfo siteInfo = AuthenticationUtil.runAsSystem(new RunAsWork<SiteInfo>()
			{
				@Override
				public SiteInfo doWork() throws Exception
				{
					SiteInfo siteInfo = sites.validateSite(siteId);
					return siteInfo;
				}
			});

			if(siteInfo.getVisibility().equals(SiteVisibility.MODERATED))
			{
				// return a site membership request only if this is a moderated site
				siteMembershipRequest = new SiteMembershipRequest();
				String title = siteInfo.getTitle();
				siteMembershipRequest.setTitle(title);
				siteMembershipRequest.setId(siteId);
				siteMembershipRequest.setMessage(moderatedInvitation.getInviteeComments());
				siteMembershipRequest.setCreatedAt(moderatedInvitation.getCreatedAt());
				siteMembershipRequest.setModifiedAt(moderatedInvitation.getModifiedAt());
			}
		}
		else
		{
			logger.warn("Unexpected resource type " + resourceType + " for site membership request");
		}

		return siteMembershipRequest;
	}
	
    @Override
	public CollectionWithPagingInfo<SiteMembershipRequest> getPagedSiteMembershipRequests(String personId, Paging paging)
	{
    	personId = people.validatePerson(personId, true);

		int skipCount = paging.getSkipCount();
		int maxItems = paging.getMaxItems();
		int max = skipCount + maxItems + 1; // to detect hasMoreItems

		List<Invitation> invitations = getSiteInvitations(personId);
		List<SiteMembershipRequest> siteMembershipRequests = new ArrayList<SiteMembershipRequest>(invitations.size());
		for(Invitation invitation : invitations)
		{
			if(invitation instanceof ModeratedInvitation)
			{
				ModeratedInvitation moderatedInvitation = (ModeratedInvitation)invitation;
				SiteMembershipRequest siteMembershipRequest = getSiteMembershipRequest(moderatedInvitation);
				if(siteMembershipRequest != null)
				{
					// note: siteMembershipRequest may be null if the site is now no longer a moderated site
					// or if the invitation is malformed and does not refer to a site.
					siteMembershipRequests.add(siteMembershipRequest);
				}
			}
			else
			{
				// just ignore, shouldn't happen because getSiteInvitations filters by ModeratedInvitation
			}
		}

		// unfortunately, need to sort in memory because there's no way to get site membership requests sorted by title from
		// the workflow apis
		Collections.sort(siteMembershipRequests);

		int totalItems = siteMembershipRequests.size();

		if(skipCount >= totalItems)
		{
			List<SiteMembershipRequest> empty = Collections.emptyList();
			return CollectionWithPagingInfo.asPaged(paging, empty, false, totalItems);
		}
		else
		{
			int end = Math.min(max - 1, totalItems);
			boolean hasMoreItems = totalItems > end;

			siteMembershipRequests = siteMembershipRequests.subList(skipCount, end);
			return CollectionWithPagingInfo.asPaged(paging, siteMembershipRequests, hasMoreItems, totalItems);
		}
	}
}
