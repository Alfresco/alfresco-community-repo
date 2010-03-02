/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.invite;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.invitation.site.InviteHelper;
import org.alfresco.repo.invitation.site.InviteInfo;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationExceptionNotFound;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Web Script which returns invite information given an inviteId and inviteTicket.
 * 
 * Note: This Web Script is accessible without authentication.
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class InviteByTicket extends DeclarativeWebScript
{

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

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
    
	/*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco
     * .web.scripts.WebScriptRequest,
     * org.alfresco.web.scripts.WebScriptResponse)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req,
            Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();

        // get inviteId and inviteTicket
        String inviteId = req.getServiceMatch().getTemplateVars().get("inviteId");
        String inviteTicket = req.getServiceMatch().getTemplateVars().get("inviteTicket");
        
        // authenticate as system for the rest of the webscript
        AuthenticationUtil.setRunAsUserSystem();
        
        try 
        {
        	Invitation invitation = invitationService.getInvitation(inviteId);
        	
        	if (invitation instanceof NominatedInvitation)
        	{
        		NominatedInvitation theInvitation = (NominatedInvitation)invitation;
        		String ticket = theInvitation.getTicket();
        		if (ticket == null || (! ticket.equals(inviteTicket)))
        		{
        			throw new WebScriptException(Status.STATUS_NOT_FOUND,
        			"Ticket mismatch");
        		}
                // return the invite info
                model.put("invite", toInviteInfo(theInvitation));
                return model;
        	}
        	else
        	{
        		// Not a nominated invitation
    			throw new WebScriptException(Status.STATUS_FORBIDDEN,
    			"Not a nominated invitation");
        	}
        }
        catch (InvitationExceptionNotFound nfe)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
            "No invite found for given id");
        }
    }

	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	public InvitationService getInvitationService() {
		return invitationService;
	}
	
    private InviteInfo toInviteInfo(NominatedInvitation invitation)
    {
        final PersonService personService = serviceRegistry.getPersonService();
        
        // get the site info
        SiteInfo siteInfo = siteService.getSite(invitation.getResourceName());
        String invitationStatus = InviteInfo.INVITATION_STATUS_PENDING;
        
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
         		invitation.getInviteId()
         		);
         
         return ret;
    }

}
