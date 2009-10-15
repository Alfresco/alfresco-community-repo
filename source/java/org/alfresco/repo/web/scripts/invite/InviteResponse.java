/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.web.scripts.invite;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.invitation.site.InviteHelper;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.InvitationExceptionForbidden;
import org.alfresco.service.cmr.invitation.InvitationExceptionUserError;
import org.alfresco.service.cmr.invitation.InvitationService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;

/**
 * Web Script invoked by Invitee to either accept (response='accept') an
 * invitation from a Site Manager (Inviter) to join a Site as a Site
 * Collaborator, or to reject (response='reject') an invitation that has already
 * been sent out
 * 
 * @author glen dot johnson at alfresco dot com
 */
public class InviteResponse extends DeclarativeWebScript
{
    private static final String RESPONSE_ACCEPT = "accept";
    private static final String RESPONSE_REJECT = "reject";
    private static final String MODEL_PROP_KEY_RESPONSE = "response";
    private static final String MODEL_PROP_KEY_SITE_SHORT_NAME = "siteShortName";
    
    // request parameter names
    private static final String PARAM_INVITEE_USER_NAME = "inviteeUserName";

    // properties for services
    private WorkflowService workflowService;
    private InvitationService invitationService;
    private TenantService tenantService;

    /**
     * Sets the workflow service property
     * 
     * @param workflowService
     *            the workflow service instance assign to the property
     */
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    /**
     * Sets the tenant service property
     * 
     * @param tenantService
     *            the tenant service instance assign to the property
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
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
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status)
    {
        if (tenantService.isEnabled())
        {
            final String tenantDomain;
            
            String inviteeUserName = req.getParameter(PARAM_INVITEE_USER_NAME);
            if (inviteeUserName != null)
            {
                tenantDomain = tenantService.getUserDomain(inviteeUserName);
            }
            else
            {
                tenantDomain = "";
            }
                
            return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Map<String, Object>>()
            {
                public Map<String, Object> doWork() throws Exception
                {
                    return execute(req, status);
                }
            }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
        }
        else
        {
            return execute(req, status);
        }
    }
    
    private Map<String, Object> execute(WebScriptRequest req, Status status)
    {
        // initialise model to pass on for template to render
        Map<String, Object> model = new HashMap<String, Object>();
        
        String inviteId = req.getServiceMatch().getTemplateVars().get("inviteId");
        String inviteTicket = req.getServiceMatch().getTemplateVars().get("inviteTicket");
               
        // Check that the task is still open.
        //if(inviteStart)
        
        // process response
        String action = req.getServiceMatch().getTemplateVars().get("action");
        if (action.equals("accept"))
        {
        	try
        	{
        		Invitation invitation = invitationService.accept(inviteId, inviteTicket);
                // add model properties for template to render
                model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_ACCEPT);
                model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, invitation.getResourceName());
        	}
            catch (InvitationExceptionForbidden fe)
            {
        		throw new WebScriptException(Status.STATUS_FORBIDDEN, fe.toString());            
            }
            catch (InvitationExceptionUserError fe)
            {
                throw new WebScriptException(Status.STATUS_CONFLICT, fe.toString());            
            }
        }
        else if (action.equals("reject"))
        {
        	try 
        	{
        		Invitation invitation = invitationService.reject(inviteId, "Rejected");
                // add model properties for template to render
                model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_REJECT);
                model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, invitation.getResourceName());
        	}
            catch (InvitationExceptionForbidden fe)
            {
        		throw new WebScriptException(Status.STATUS_FORBIDDEN, fe.toString());            
            }
            catch (InvitationExceptionUserError fe)
            {
                throw new WebScriptException(Status.STATUS_CONFLICT, fe.toString());            
            }
        }
        else
        {
            /* handle unrecognised method */
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "action " + action + " is not supported by this webscript.");
        }

        return model;
    }

	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	public InvitationService getInvitationService() {
		return invitationService;
	}    
}
