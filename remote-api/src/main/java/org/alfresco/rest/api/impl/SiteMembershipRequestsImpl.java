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
package org.alfresco.rest.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.invitation.InvitationSearchCriteriaImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.NetworksService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.rest.antlr.WhereClauseParser;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.SiteMembershipRequests;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.api.model.SiteMembershipApproval;
import org.alfresco.rest.api.model.SiteMembershipRejection;
import org.alfresco.rest.api.model.SiteMembershipRequest;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryHelper;
import org.alfresco.rest.workflow.api.impl.MapBasedQueryWalker;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.Invitation.ResourceType;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria.InvitationType;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.ModeratedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.apache.commons.lang3.StringUtils;
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

    // List site memberships filtering (via where clause)
    private final static Set<String> LIST_SITE_MEMBERSHIPS_EQUALS_QUERY_PROPERTIES = new HashSet<>(Arrays.asList(new String[] { PARAM_SITE_ID, PARAM_PERSON_ID }));

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
		
		if(siteInfo == null)
		{
			// site does not exist
			throw new RelationshipResourceNotFoundException(inviteeId, siteId);
		}
		
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
		return getSiteMembershipRequest(moderatedInvitation, false);
    }

	private SiteMembershipRequest getSiteMembershipRequest(ModeratedInvitation moderatedInvitation, boolean includePersonDetails)
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

			if(siteInfo == null)
			{
				// site does not exist
				throw new EntityNotFoundException(siteId);
			}

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

                if (includePersonDetails)
                {
                    Person person = people.getPerson(moderatedInvitation.getInviteeUserName());
                    siteMembershipRequest.setPerson(person);
                }
			}
		}
		else
		{
			logger.warn("Unexpected resource type " + resourceType + " for site membership request");
		}

		return siteMembershipRequest;
    }

    private List<SiteMembershipRequest> toSiteMembershipRequests(List<Invitation> invitations)
    {
        return toSiteMembershipRequests(invitations, false);
    }

    private List<SiteMembershipRequest> toSiteMembershipRequests(List<Invitation> invitations, boolean includePersonDetails)
    {
		List<SiteMembershipRequest> siteMembershipRequests = new ArrayList<SiteMembershipRequest>(invitations.size());
		for(Invitation invitation : invitations)
		{
			if(invitation instanceof ModeratedInvitation)
			{
				ModeratedInvitation moderatedInvitation = (ModeratedInvitation)invitation;
				SiteMembershipRequest siteMembershipRequest = getSiteMembershipRequest(moderatedInvitation, includePersonDetails);
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
		
		return siteMembershipRequests;
    }

    private CollectionWithPagingInfo<SiteMembershipRequest> createPagedSiteMembershipRequests(List<SiteMembershipRequest> siteMembershipRequests, Paging paging)
    {
        int skipCount = paging.getSkipCount();
        int maxItems = paging.getMaxItems();
        int max = skipCount + maxItems + 1; // to detect hasMoreItems

        // unfortunately, need to sort in memory because there's no way to get site
        // membership requests sorted by title from
        // the workflow apis
        Collections.sort(siteMembershipRequests);

        int totalItems = siteMembershipRequests.size();

        if (skipCount >= totalItems)
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

    @Override
    public CollectionWithPagingInfo<SiteMembershipRequest> getPagedSiteMembershipRequests(String personId, Paging paging)
    {
        personId = people.validatePerson(personId, true);

        List<Invitation> invitations = getSiteInvitations(personId);
        return createPagedSiteMembershipRequests(toSiteMembershipRequests(invitations), paging);
    }

    @Override
    public CollectionWithPagingInfo<SiteMembershipRequest> getPagedSiteMembershipRequests(final Parameters parameters)
    {
        InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
        criteria.setInvitationType(InvitationType.MODERATED);
        criteria.setResourceType(ResourceType.WEB_SITE);

        // Parse where clause properties.
        Query q = parameters.getQuery();
        if (q != null)
        {
            // Filtering via "where" clause.
            MapBasedQueryWalker propertyWalker = createSiteMembershipRequestQueryWalker();
            QueryHelper.walk(q, propertyWalker);

            String siteId = propertyWalker.getProperty(PARAM_SITE_ID, WhereClauseParser.EQUALS, String.class);

            if (siteId != null && !siteId.isEmpty())
            {
                criteria.setResourceName(siteId);
            }

            String personId = propertyWalker.getProperty(PARAM_PERSON_ID, WhereClauseParser.EQUALS, String.class);

            if (personId != null && !personId.isEmpty())
            {
                criteria.setInvitee(personId);
            }
        }

        List<Invitation> invitations = invitationService.searchInvitation(criteria);
        return createPagedSiteMembershipRequests(toSiteMembershipRequests(invitations, true), parameters.getPaging());
    }

    @Override
    public void approveSiteMembershipRequest(String siteId, String inviteeId, SiteMembershipApproval siteMembershipApproval)
    {
        SiteInfo siteInfo = sites.validateSite(siteId);
        if (siteInfo == null)
        {
            throw new EntityNotFoundException(siteId);
        }

        // Set the site id to the short name (to deal with case sensitivity issues with
        // using the siteId from the url)
        siteId = siteInfo.getShortName();

        // Validate invitation.
        Invitation invitation = getSiteInvitation(inviteeId, siteId);
        if (invitation == null || !(invitation instanceof ModeratedInvitation))
        {
            throw new RelationshipResourceNotFoundException(siteId, inviteeId);
        }

        ModeratedInvitation moderatedInvitation = (ModeratedInvitation) invitation;
        ResourceType resourceType = moderatedInvitation.getResourceType();

        if (!resourceType.equals(ResourceType.WEB_SITE) || !SiteVisibility.MODERATED.equals(siteInfo.getVisibility()))
        {
            // note: security, no indication that this has a different visibility
            throw new RelationshipResourceNotFoundException(siteId, inviteeId);
        }

        try
        {
            invitationService.approve(invitation.getInviteId(), "");
        }
        catch (InvitationExceptionForbidden ex)
        {
            throw new PermissionDeniedException();
        }

        // Workflow doesn't allow changing the role, so a new update may be required if
        // approval role differs from default one.
        if (siteMembershipApproval != null && !(siteMembershipApproval.getRole() == null || siteMembershipApproval.getRole().isEmpty()))
        {
            String role = siteMembershipApproval.getRole();

            // Check if role chosen by moderator differs from the invite role.
            if (!moderatedInvitation.getRoleName().equals(role))
            {
                String currentUserId = AuthenticationUtil.getFullyAuthenticatedUser();

                // Update invitation with new role.
                try
                {
                    addSiteMembership(invitation.getInviteeUserName(), siteId, role, currentUserId);
                }
                catch (UnknownAuthorityException e)
                {
                    logger.debug("addSiteMember:  UnknownAuthorityException " + siteId + " person " + invitation.getInviteId() + " role " + role);
                    throw new InvalidArgumentException("Unknown role '" + role + "'");
                }
            }
        }
    }

    @Override
    public void rejectSiteMembershipRequest(String siteId, String inviteeId, SiteMembershipRejection siteMembershipRejection)
    {
        SiteInfo siteInfo = sites.validateSite(siteId);
        if (siteInfo == null)
        {
            throw new EntityNotFoundException(siteId);
        }

        // set the site id to the short name (to deal with case sensitivity issues with
        // using the siteId from the url)
        siteId = siteInfo.getShortName();

        // Validate invitation.
        Invitation invitation = getSiteInvitation(inviteeId, siteId);
        if (invitation == null || !(invitation instanceof ModeratedInvitation))
        {
            throw new RelationshipResourceNotFoundException(siteId, inviteeId);
        }

        ModeratedInvitation moderatedInvitation = (ModeratedInvitation) invitation;
        ResourceType resourceType = moderatedInvitation.getResourceType();

        if (!resourceType.equals(ResourceType.WEB_SITE) || !SiteVisibility.MODERATED.equals(siteInfo.getVisibility()))
        {
            // note: security, no indication that this has a different visibility
            throw new RelationshipResourceNotFoundException(siteId, inviteeId);
        }

        String reason = null;
        if (siteMembershipRejection != null && !(siteMembershipRejection.getComment() == null || siteMembershipRejection.getComment().isEmpty()))
        {
            reason = siteMembershipRejection.getComment();
        }

        try
        {
            invitationService.reject(invitation.getInviteId(), reason);
        }
        catch (InvitationExceptionForbidden ex)
        {
            throw new PermissionDeniedException();
        }
    }

    /**
     * Create query walker for <code>listChildren</code>.
     *
     * @return The created {@link MapBasedQueryWalker}.
     */
    private MapBasedQueryWalker createSiteMembershipRequestQueryWalker()
    {
        return new MapBasedQueryWalker(LIST_SITE_MEMBERSHIPS_EQUALS_QUERY_PROPERTIES, null);
    }

    private void addSiteMembership(final String invitee, final String siteName, final String role, final String runAsUser)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                siteService.setMembership(siteName, invitee, role);
                return null;
            }

        }, runAsUser);
    }
}
