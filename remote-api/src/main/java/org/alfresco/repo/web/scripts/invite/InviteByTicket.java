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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.invitation.site.InviteInfo;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;

/**
 * Web Script which returns invite information given an inviteId and inviteTicket.
 * 
 * Note: This Web Script is accessible without authentication.
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class InviteByTicket extends DeclarativeWebScript
{
    // request parameter names
    private static final String PARAM_INVITEE_USER_NAME = "inviteeUserName";

    // service instances
    private ServiceRegistry serviceRegistry;
    private SiteService siteService;
    private InvitationService invitationService;
    private TenantService tenantService;

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public void setInvitationService(InvitationService invitationService)
    {
        this.invitationService = invitationService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco .web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse) */
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status)
    {
        String tenantDomain = TenantService.DEFAULT_DOMAIN;

        if (tenantService.isEnabled())
        {
            String inviteeUserName = req.getParameter(PARAM_INVITEE_USER_NAME);
            if (inviteeUserName != null)
            {
                tenantDomain = tenantService.getUserDomain(inviteeUserName);
            }
        }

        // run as system user
        String mtAwareSystemUser = tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);

        Map<String, Object> ret = TenantUtil.runAsSystemTenant(new TenantRunAsWork<Map<String, Object>>() {
            public Map<String, Object> doWork() throws Exception
            {
                return execute(req, status);
            }
        }, tenantDomain);

        // authenticate as system for the rest of the webscript
        AuthenticationUtil.setRunAsUser(mtAwareSystemUser);

        return ret;
    }

    private Map<String, Object> execute(WebScriptRequest req, Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();

        // get inviteId and inviteTicket
        String inviteId = req.getServiceMatch().getTemplateVars().get("inviteId");
        String inviteTicket = req.getServiceMatch().getTemplateVars().get("inviteTicket");

        try
        {
            Invitation invitation = invitationService.getInvitation(inviteId);

            if (invitation instanceof NominatedInvitation)
            {
                NominatedInvitation theInvitation = (NominatedInvitation) invitation;
                String ticket = theInvitation.getTicket();
                if (ticket == null || (!ticket.equals(inviteTicket)))
                {
                    throw new WebScriptException(Status.STATUS_NOT_FOUND, "Ticket mismatch");
                }
                // return the invite info
                model.put("invite", toInviteInfo(theInvitation));
                return model;
            }
            else
            {
                // Not a nominated invitation
                throw new WebScriptException(Status.STATUS_FORBIDDEN, "Not a nominated invitation");
            }
        }
        catch (InvitationExceptionNotFound nfe)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "No invite found for given id");
        }
    }

    private InviteInfo toInviteInfo(NominatedInvitation invitation)
    {
        final PersonService personService = serviceRegistry.getPersonService();

        // get the site info
        SiteInfo siteInfo = siteService.getSite(invitation.getResourceName());
        String invitationStatus = getInvitationStatus(invitation);

        NodeRef inviterRef = personService.getPerson(invitation.getInviterUserName());
        TemplateNode inviterPerson = null;
        if (inviterRef != null)
        {
            inviterPerson = new TemplateNode(inviterRef, serviceRegistry, null);
        }

        // fetch the person node for the invitee
        NodeRef inviteeRef = personService.getPerson(invitation.getInviteeUserName());
        TemplateNode inviteePerson = null;
        if (inviteeRef != null)
        {
            inviteePerson = new TemplateNode(inviteeRef, serviceRegistry, null);
        }

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

    private String getInvitationStatus(NominatedInvitation invitation)
    {
        String invitee = invitation.getInviteeUserName();
        String site = invitation.getResourceName();
        // check is invitee is site member
        boolean isUserMember = serviceRegistry.getSiteService().isMember(site, invitee);

        WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setTaskName(WorkflowModelNominatedInvitation.WF_TASK_ACTIVIT_INVITE_PENDING);
        query.setProcessId(invitation.getInviteId());
        // query current workflow's task activitiInvitePendingTask
        List<WorkflowTask> pendingInvitationTasks = serviceRegistry.getWorkflowService().queryTasks(query, false);
        // if it's here - pending invitation
        if (!pendingInvitationTasks.isEmpty())
        {
            return InviteInfo.INVITATION_STATUS_PENDING;
        }
        else if (isUserMember)
        {
            return InviteInfo.INVITATION_STATUS_ACCEPTED;
        }
        else
        {
            return InviteInfo.INVITATION_STATUS_REJECTED;
        }
    }
}
