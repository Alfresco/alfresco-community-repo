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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.site.SiteInfo;
import org.alfresco.repo.site.SiteService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Helper class to house utility methods common to 
 * more than one Invite Service Web Script 
 */
public class InviteHelper
{   
    /**
     * Find an invite start task by the given task id.
     * 
     * @return a WorkflowTask or null if not found.
     */
    public static WorkflowTask findInviteStartTask(String inviteId, WorkflowService workflowService)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();
        
        wfTaskQuery.setProcessId(inviteId);
        
        // set process name to "wf:invite" so that only tasks associated with
        // invite workflow instances are returned by query
        wfTaskQuery.setProcessName(QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "invite"));

        // filter to find only the invite start task
        wfTaskQuery.setTaskState(WorkflowTaskState.COMPLETED);
        wfTaskQuery.setTaskName(InviteWorkflowModel.WF_INVITE_TASK_INVITE_TO_SITE);

        // query for invite workflow task associate
        List<WorkflowTask> inviteStartTasks = workflowService
                .queryTasks(wfTaskQuery);

        // should also be 0 or 1
        if (inviteStartTasks.size() < 1)
        {
            return null;
        }
        else
        {
            return inviteStartTasks.get(0);
        }
    }
    
    /**
     * Find invitePending tasks (in-progress) by the given invitee user name
     * 
     * @return a list of workflow tasks
     */
    public static List<WorkflowTask> findInvitePendingTasks(String inviteeUserName, WorkflowService workflowService)
    {
            // create workflow task query
            WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();
            
            // set process name to "wf:invite" so that only tasks associated with
            // invite workflow instances are returned by query
            wfTaskQuery.setProcessName(QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "invite"));
            
            // set query to only pick up invite workflow instances
            // associated with the given invitee user name
            Map<QName, Object> processCustomProps = new HashMap<QName, Object>(1, 1.0f);
            processCustomProps.put(InviteWorkflowModel.WF_PROP_INVITEE_USER_NAME, inviteeUserName);
            wfTaskQuery.setProcessCustomProps(processCustomProps);

            // set query to only pick up in-progress invite pending tasks 
            wfTaskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
            wfTaskQuery.setTaskName(InviteWorkflowModel.WF_TASK_INVITE_PENDING);

            // query for invite workflow task associate
            List<WorkflowTask> inviteStartTasks = workflowService
                    .queryTasks(wfTaskQuery);

            return inviteStartTasks;
    }
    
    /**
     * Returns an InviteInfo instance for the given startInvite task
     * (used for rendering the response).
     * 
     * @param startInviteTask startInvite task to get invite info properties from
     * @param serviceRegistry service registry instance
     * @param siteService site service instance
     * 
     * @return InviteInfo instance containing invite information
     */
    public static InviteInfo getPendingInviteInfo(final WorkflowTask startInviteTask,
            final ServiceRegistry serviceRegistry, final SiteService siteService)
    {
        final PersonService personService = serviceRegistry.getPersonService();
        
        // get the inviter, invitee, role and site short name
        final String inviterUserNameProp = (String) startInviteTask.properties.get(
                InviteWorkflowModel.WF_PROP_INVITER_USER_NAME);
        final String inviteeUserNameProp = (String) startInviteTask.properties.get(
                InviteWorkflowModel.WF_PROP_INVITEE_USER_NAME);
        final String role = (String) startInviteTask.properties.get(
                InviteWorkflowModel.WF_PROP_INVITEE_SITE_ROLE);
        final String siteShortNameProp = (String) startInviteTask.properties.get(
                InviteWorkflowModel.WF_PROP_SITE_SHORT_NAME);

        // get the site info
        SiteInfo siteInfo = siteService.getSite(siteShortNameProp);
        
        // get workflow instance id (associated with workflow task) to place
        // as "inviteId" onto model
        String workflowId = startInviteTask.path.instance.id;

        // set the invite start date to the time the workflow instance
        // (associated with the task) was started
        Date sentInviteDate = startInviteTask.path.instance.startDate;
        
        // TODO: glen johnson at alfresco com - as this web script only returns
        // pending invites, this is hard coded to "pending" for now
        String invitationStatus = InviteInfo.INVITATION_STATUS_PENDING;
        
        // fetch the person node for the inviter
        NodeRef inviterRef = personService.getPerson(inviterUserNameProp);
        ScriptNode inviterPerson = null;
        if (inviterRef != null)
        {
            inviterPerson = new ScriptNode(inviterRef, serviceRegistry); 
        }
        
        // fetch the person node for the invitee
        NodeRef inviteeRef = personService.getPerson(inviteeUserNameProp);
        ScriptNode inviteePerson = null;
        if (inviteeRef != null)
        {
            inviteePerson = new ScriptNode(inviteeRef, serviceRegistry); 
        }
        
        // create and return a invite info
        InviteInfo inviteInfo = new InviteInfo(invitationStatus, inviterUserNameProp, inviterPerson,
                inviteeUserNameProp, inviteePerson, role, siteShortNameProp, siteInfo, sentInviteDate, workflowId);

        return inviteInfo;
    }
}
