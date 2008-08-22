/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.web.scripts.DeclarativeWebScript;
import org.alfresco.web.scripts.Status;
import org.alfresco.web.scripts.WebScriptException;
import org.alfresco.web.scripts.WebScriptRequest;

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

        // Extract inviteId and inviteTicket
        String extPath = req.getExtensionPath();
        int separatorIndex = extPath.indexOf('/'); 
        if (separatorIndex < 0)
        {
            // should not happen as descriptor would not match
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
            "Parameters missing");
        }
        String inviteId = extPath.substring(0, separatorIndex);
        String inviteTicket = extPath.substring(separatorIndex + 1);
        
        // authenticate as system for the rest of the webscript
        AuthenticationUtil.setSystemUserAsCurrentUser();
        
        // find the workflow for the given id
        WorkflowTask workflowTask = InviteHelper.findInviteStartTask(inviteId, workflowService);
        if (workflowTask == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "No invite found for given id");
        }

        // check whether tickets match, throw error otherwise
        String ticket = (String) workflowTask.properties.get(
                InviteWorkflowModel.WF_PROP_INVITE_TICKET);
        if (ticket == null || (! ticket.equals(inviteTicket)))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
            "Ticket mismatch");
        }
        
        // return the invite info
        InviteInfo inviteInfo = InviteHelper.getPendingInviteInfo(workflowTask, serviceRegistry, siteService);
        model.put("invite", inviteInfo);
        return model;
    }
}
