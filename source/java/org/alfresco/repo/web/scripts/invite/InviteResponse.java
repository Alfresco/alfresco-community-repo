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
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
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

    // properties for services
    private WorkflowService workflowService;

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
        
        String inviteId = req.getServiceMatch().getTemplateVars().get("inviteId");
        String inviteTicket = req.getServiceMatch().getTemplateVars().get("inviteTicket");
        
        // fetch the start task - it might not exist if the workflow has been finished/cancelled already
        WorkflowTask inviteStartTask = InviteHelper.findInviteStartTask(inviteId, workflowService);
        if (inviteStartTask == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                    "No invite workflow for given id found"); 
        }
        
        // check the ticket for a match
        String ticket = (String) inviteStartTask.properties.get(InviteWorkflowModel.WF_PROP_INVITE_TICKET);
        if (ticket == null || (! ticket.equals(inviteTicket)))
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND,
                "Response to invite has supplied an invalid ticket. The response to the "
                    + "invitation could thus not be processed"); 
        }
        
        // process response
        String action = req.getServiceMatch().getTemplateVars().get("action");
        if (action.equals("accept"))
        {
            acceptInvite(model, inviteId, inviteStartTask);
        }
        else if (action.equals("reject"))
        {
            rejectInvite(model, inviteId, inviteStartTask);
        }
        else
        {
            /* handle unrecognised method */
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "action " + action + " is not supported by this webscript.");
        }

        return model;
    }

    /**
     * Processes 'accept invite' response from invitee
     * 
     * @param model
     *            model to add objects to, which will be passed to the template
     *            for rendering
     * @param inviteId
     *            ID of invite
     * @param inviteStartTask
     *            wf:inviteToSiteTask instance containing the invite parameters the
     *            invite workflow instance (it belongs to) was started with 
     */
    private void acceptInvite(Map<String, Object> model, String inviteId, WorkflowTask inviteStartTask)
    {
        String siteShortName = (String) inviteStartTask.properties.get(
                InviteWorkflowModel.WF_PROP_SITE_SHORT_NAME);
        
        // complete the wf:invitePendingTask along the 'accept' transition because the invitation has been accepted
        completeInviteTask(inviteId, InviteWorkflowModel.WF_TASK_INVITE_PENDING, InviteWorkflowModel.WF_TRANSITION_ACCEPT);
        
        // add model properties for template to render
        model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_ACCEPT);
        model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, siteShortName);
    }

    /**
     * Processes 'reject' invite response from invitee
     * 
     * @param model
     *            model to add objects to, which will be passed to the template
     *            for rendering
     * @param inviteId
     *            ID of invite
     * @param inviteeUserName
     *            user name of invitee
     * @param siteShortName
     *            short name of site for which invitee is rejecting
     *            invitation to join
     */
    private void rejectInvite(Map<String, Object> model, String inviteId, WorkflowTask inviteStartTask)
    {
        String siteShortName = (String) inviteStartTask.properties.get(
                InviteWorkflowModel.WF_PROP_SITE_SHORT_NAME);
        
        // complete the wf:invitePendingTask task along the 'reject' transition because the invitation has been rejected
        completeInviteTask(inviteId, InviteWorkflowModel.WF_TASK_INVITE_PENDING, InviteWorkflowModel.WF_TRANSITION_REJECT);
        
        // add model properties for template to render
        model.put(MODEL_PROP_KEY_RESPONSE, RESPONSE_REJECT);
        model.put(MODEL_PROP_KEY_SITE_SHORT_NAME, siteShortName);
    }
    
    /**
     * Complete the specified Invite Workflow Task for the invite workflow 
     * instance associated with the given invite ID, and follow the given
     * transition upon completing the task
     * 
     * @param inviteId the invite ID of the invite workflow instance for which
     *          we want to complete the given task
     * @param fullTaskName qualified name of invite workflow task to complete
     * @param transitionId the task transition to take on completion of 
     *          the task (or null, for the default transition)
     */
    private void completeInviteTask(String inviteId, QName fullTaskName, String transitionId)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();
        
        // set the given invite ID as the workflow process ID in the workflow query
        wfTaskQuery.setProcessId(inviteId);

        // find incomplete invite workflow tasks with given task name 
        wfTaskQuery.setActive(Boolean.TRUE);
        wfTaskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
        wfTaskQuery.setTaskName(fullTaskName);

        // set process name to "wf:invite" so that only
        // invite workflow instances are considered by this query
        wfTaskQuery.setProcessName(InviteWorkflowModel.WF_PROCESS_INVITE);

        // query for invite workflow tasks with the constructed query
        List<WorkflowTask> wf_invite_tasks = this.workflowService
                .queryTasks(wfTaskQuery);
        
        // end all tasks found with this name 
        for (WorkflowTask workflowTask : wf_invite_tasks)
        {
            this.workflowService.endTask(workflowTask.id, transitionId);
        }
    }
}
