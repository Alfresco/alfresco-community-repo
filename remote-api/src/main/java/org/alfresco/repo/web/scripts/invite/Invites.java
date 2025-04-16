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
package org.alfresco.repo.web.scripts.invite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.invitation.InvitationSearchCriteriaImpl;
import org.alfresco.repo.invitation.site.InviteInfo;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationSearchCriteria;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;

/**
 * Web Script which returns pending Site invitations matching at least one of
 * 
 * (1a) inviter (inviter user name). i.e. pending invitations which have been sent by that inviter, but which have not been responded to (accepted or rejected) by the invitee, and have not been cancelled by that inviter
 * 
 * (1b) invitee (invitee user name), i.e. pending invitations which have not been accepted or rejected yet by that inviter
 * 
 * (1c) site (site short name), i.e. pending invitations sent out to join that Site. If only the site is given, then all pending invites are returned, irrespective of who the inviters or invitees are
 * 
 * or
 * 
 * (2) matching the given invite ID
 * 
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class Invites extends DeclarativeWebScript
{
    // request parameter names
    private static final String PARAM_INVITER_USER_NAME = "inviterUserName";
    private static final String PARAM_INVITEE_USER_NAME = "inviteeUserName";
    private static final String PARAM_SITE_SHORT_NAME = "siteShortName";
    private static final String PARAM_INVITE_ID = "inviteId";

    // model key names
    private static final String MODEL_KEY_NAME_INVITES = "invites";

    // service instances
    private WorkflowService workflowService;
    private ServiceRegistry serviceRegistry;
    private SiteService siteService;
    private InvitationService invitationService;

    /**
     * Set the workflow service property
     * 
     * @param workflowService
     *            the workflow service to set
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setInvitationService(InvitationService invitationService)
    {
        this.invitationService = invitationService;
    }

    public InvitationService getInvitationService()
    {
        return invitationService;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco .web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse) */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();

        // Get parameter names
        String[] paramNames = req.getParameterNames();

        // handle no parameters given on URL
        if ((paramNames == null) || (paramNames.length == 0))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "No parameters have been provided on URL");
        }

        // get URL request parameters, checking if at least one has been provided

        // check if 'inviterUserName' parameter provided
        String inviterUserName = req.getParameter(PARAM_INVITER_USER_NAME);
        boolean inviterUserNameProvided = (inviterUserName != null)
                && (inviterUserName.length() != 0);

        // check if 'inviteeUserName' parameter provided
        String inviteeUserName = req.getParameter(PARAM_INVITEE_USER_NAME);
        boolean inviteeUserNameProvided = (inviteeUserName != null)
                && (inviteeUserName.length() != 0);

        // check if 'siteShortName' parameter provided
        String siteShortName = req.getParameter(PARAM_SITE_SHORT_NAME);
        boolean siteShortNameProvided = (siteShortName != null)
                && (siteShortName.length() != 0);

        // check if 'inviteId' parameter provided
        String inviteId = req.getParameter(PARAM_INVITE_ID);
        boolean inviteIdProvided = (inviteId != null)
                && (inviteId.length() != 0);

        // throw web script exception if at least one of 'inviterUserName',
        // 'inviteeUserName', 'siteShortName',
        // 'inviteId' URL request parameters has not been provided
        if (!(inviterUserNameProvided || inviteeUserNameProvided
                || siteShortNameProvided || inviteIdProvided))
        {
            throw new WebScriptException(
                    Status.STATUS_BAD_REQUEST,
                    "At least one of the following URL request parameters must be provided in URL "
                            + "'inviterUserName', 'inviteeUserName', 'siteShortName' or 'inviteId'");
        }

        // InviteInfo List to place onto model
        List<InviteInfo> inviteInfoList = new ArrayList<InviteInfo>();

        // if 'inviteId' has been provided then set that as the workflow query
        // process ID
        // - since this is unique don't bother about setting the other workflow
        // query properties
        if (inviteIdProvided)
        {
            NominatedInvitation invitation = (NominatedInvitation) invitationService.getInvitation(inviteId);
            Map<String, SiteInfo> siteInfoCache = new HashMap<String, SiteInfo>(2);
            inviteInfoList.add(toInviteInfo(siteInfoCache, invitation));
        }
        else
        // 'inviteId' has not been provided, so create the query properties from
        // the invite URL request
        // parameters
        // - because this web script class will terminate with a web script
        // exception if none of the required
        // request parameters are provided, at least one of these query
        // properties will be set
        // at this point
        {
            InvitationSearchCriteriaImpl criteria = new InvitationSearchCriteriaImpl();
            criteria.setInvitationType(InvitationSearchCriteria.InvitationType.NOMINATED);
            criteria.setResourceType(Invitation.ResourceType.WEB_SITE);

            if (inviterUserNameProvided)
            {
                criteria.setInviter(inviterUserName);
            }
            if (inviteeUserNameProvided)
            {
                criteria.setInvitee(inviteeUserName);
            }
            if (siteShortNameProvided)
            {
                criteria.setResourceName(siteShortName);
            }

            // MNT-9905 Pending Invites created by one site manager aren't visible to other site managers
            String currentUser = AuthenticationUtil.getRunAsUser();
            List<Invitation> invitations;

            if (siteShortNameProvided == true && (SiteModel.SITE_MANAGER).equals(siteService.getMembersRole(siteShortName, currentUser)) && inviterUserNameProvided == false && inviteeUserNameProvided == false)
            {
                final InvitationSearchCriteriaImpl crit = criteria;

                RunAsWork<List<Invitation>> runAsSystem = new RunAsWork<List<Invitation>>() {
                    @Override
                    public List<Invitation> doWork() throws Exception
                    {
                        return invitationService.searchInvitation(crit);
                    }
                };

                invitations = AuthenticationUtil.runAs(runAsSystem, AuthenticationUtil.getSystemUserName());
            }
            else
            {
                invitations = invitationService.searchInvitation(criteria);
            }

            // Put InviteInfo objects (containing workflow path properties
            // wf:inviterUserName, wf:inviteeUserName, wf:siteShortName,
            // and invite id property (from workflow instance id))
            // onto model for each invite workflow task returned by the query
            Map<String, SiteInfo> siteInfoCache = new HashMap<String, SiteInfo>(
                    invitations.size() * 2);
            for (Invitation invitation : invitations)
            {
                inviteInfoList.add(toInviteInfo(siteInfoCache, (NominatedInvitation) invitation));
            }
        }

        // put the list of invite infos onto model to be passed onto template
        // for rendering
        model.put(MODEL_KEY_NAME_INVITES, inviteInfoList);

        return model;
    }

    private InviteInfo toInviteInfo(Map<String, SiteInfo> siteInfoCache, final NominatedInvitation invitation)
    {
        // get the site info
        String resourceName = invitation.getResourceName();
        SiteInfo siteInfo = siteInfoCache.get(resourceName);
        if (siteInfo == null)
        {
            siteInfo = siteService.getSite(resourceName);
            siteInfoCache.put(resourceName, siteInfo);
        }
        String invitationStatus = InviteInfo.INVITATION_STATUS_PENDING;

        TemplateNode inviterPerson = getPersonIfAllowed(invitation.getInviterUserName());

        // fetch the person node for the invitee
        TemplateNode inviteePerson = getPersonIfAllowed(invitation.getInviteeUserName());

        InviteInfo ret = new InviteInfo(invitationStatus,
                invitation.getInviterUserName(),
                inviterPerson,
                invitation.getInviteeUserName(),
                inviteePerson,
                invitation.getRoleName(),
                invitation.getResourceName(),
                siteInfo,
                invitation.getSentInviteDate(),
                invitation.getInviteId());

        return ret;
    }

    private TemplateNode getPersonIfAllowed(final String userName)
    {
        final PersonService personService = serviceRegistry.getPersonService();
        NodeRef inviterRef = AuthenticationUtil.runAs(new RunAsWork<NodeRef>() {
            public NodeRef doWork() throws Exception
            {
                if (!personService.personExists(userName))
                {
                    return null;
                }
                return personService.getPerson(userName, false);
            }
        }, AuthenticationUtil.getSystemUserName());
        if (inviterRef != null
                && serviceRegistry.getPermissionService().hasPermission(inviterRef, PermissionService.READ_PROPERTIES)
                        .equals(AccessStatus.ALLOWED))
        {
            return new TemplateNode(inviterRef, serviceRegistry, null);
        }
        return null;
    }
}
