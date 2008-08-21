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
import java.util.List;

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
     * Gets the invitee site role from the invite
     * workflow instance associated with the given invite ID.
     * i.e. if the inviter 'starts' an invite (which is allocated some invite ID
     * '12345' when it is processed), and that invite is requesting an invitee to
     * to join some site under a given site role, then that site role is returned 
     * by this method when invite ID '12345' is passed in.
     * 
     * @param inviteId the ID of the invitation (invite workflow instance)
     *          from which to retrieve the invitee site role
     * @return the site role under which the invitee was invited to 
     *          join the site. Returns <pre>null</pre> if no invite
     *          workflow instance was found matching the given invite ID
     */
    static String getInviteeSiteRoleFromInvite(String inviteId, WorkflowService workflowService,
                        NamespaceService namespaceService)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();

        wfTaskQuery.setProcessId(inviteId);
        
        // set process name to "wf:invite" so that only tasks associated with
        // invite workflow instances are returned by query
        wfTaskQuery.setProcessName(QName.createQName("wf:invite", namespaceService));

        // pick up the start task because it has the "wf:inviteeSiteRole" property set with the
        // site role value that we want to retrieve 
        wfTaskQuery.setTaskState(WorkflowTaskState.COMPLETED);
        wfTaskQuery.setTaskName(QName.createQName(Invite.WF_INVITE_TASK_INVITE_TO_SITE, namespaceService));

        // query for invite workflow task associate
        List<WorkflowTask> inviteStartTasks = workflowService
                .queryTasks(wfTaskQuery);

        // if no results were returned for given inviteID, then return
        // site role as null
        if (inviteStartTasks.size() == 0)
        {
            return null;
        }
        else
        {
            // there should be only one start task returned for the given invite ID
            // so just take the first one in the list
            WorkflowTask inviteStartTask = inviteStartTasks.get(0);
            
            String inviteeSiteRole = (String) inviteStartTask.properties.get(
                    QName.createQName(Invite.WF_PROP_INVITEE_SITE_ROLE, namespaceService));
            
            return inviteeSiteRole;
        }
    }

    /**
     * Gets the date that the invite, with the given invite ID, was sent to the invitee
     * 
     * @param inviteId the ID of the invitation
     *          from which to retrieve the sent date
     * @return the date that the invite was sent to the invitee 
     *          Returns <pre>null</pre> if no invitation
     *          found matching the given invite ID
     */
    static Date getSentDateFromInvite(String inviteId, WorkflowService workflowService,
                        NamespaceService namespaceService)
    {
        // create workflow task query
        WorkflowTaskQuery wfTaskQuery = new WorkflowTaskQuery();

        wfTaskQuery.setProcessId(inviteId);
        
        // set process name to "wf:invite" so that only tasks associated with
        // invite workflow instances are returned by query
        wfTaskQuery.setProcessName(QName.createQName("wf:invite", namespaceService));

        // pick up the start task because it has the "wf:inviteeSiteRole" property set with the
        // site role value that we want to retrieve 
        wfTaskQuery.setTaskState(WorkflowTaskState.COMPLETED);
        wfTaskQuery.setTaskName(QName.createQName(Invite.WF_INVITE_TASK_INVITE_TO_SITE, namespaceService));

        // query for invite workflow task associate
        List<WorkflowTask> inviteStartTasks = workflowService
                .queryTasks(wfTaskQuery);

        // if no results were returned for given inviteID, then return
        // site role as null
        if (inviteStartTasks.size() == 0)
        {
            return null;
        }
        else
        {
            // there should be only one start task returned for the given invite ID
            // so just take the first one in the list
            WorkflowTask inviteStartTask = inviteStartTasks.get(0);
            
            Date sentInviteDate = (Date) inviteStartTask.properties.get(
                    QName.createQName(Invite.WF_PROP_SENT_INVITE_DATE, namespaceService));
            
            return sentInviteDate;
        }
    }
}
