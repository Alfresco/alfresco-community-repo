/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.invitation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ActivitiInvitationServiceImplTests extends AbstractInvitationServiceImplTest
{
    private WorkflowService workflowService;
    
    public void testWorkflowTaskContainsProps()
    {
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";
        NominatedInvitation nomInvite = invitationService.inviteNominated(USER_ONE,
                resourceType, resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        
        WorkflowTask task = getTaskForInvitation(nomInvite);
        Map<QName, Serializable> props = task.getProperties();
        assertEquals(inviteeRole, props.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE));
        assertEquals(nomInvite.getResourceDescription(), props.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_DESCRIPTION));
        assertEquals(nomInvite.getResourceTitle(), props.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TITLE));
        
        // Accept the invitation
        invitationService.accept(nomInvite.getInviteId(), nomInvite.getTicket());
        
        task = workflowService.getTaskById(task.getId());
        props = task.getProperties();
        assertEquals(inviteeRole, props.get(WorkflowModelNominatedInvitation.WF_PROP_INVITEE_ROLE));
        assertEquals(nomInvite.getResourceDescription(), props.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_DESCRIPTION));
        assertEquals(nomInvite.getResourceTitle(), props.get(WorkflowModelNominatedInvitation.WF_PROP_RESOURCE_TITLE));
    }

    private WorkflowTask getTaskForInvitation(Invitation invite)
    {
        String instanceId = invite.getInviteId();
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(instanceId);
        assertEquals(1, paths.size());
        WorkflowPath path = paths.get(0);
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask task = tasks.get(0);
        return task;
    }
    
    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        this.workflowService = (WorkflowService) applicationContext.getBean("WorkflowService");
        
        // Add a few Jbpm invitations to check they dont' interfere with Activiti invitations.
        workflowAdminService.setEnabledEngines(Arrays.asList(JBPMEngine.ENGINE_ID));
        
        String invitee = USER_ONE;
        Invitation.ResourceType resourceType = Invitation.ResourceType.WEB_SITE;
        String resourceName = SITE_SHORT_NAME_INVITE;
        String inviteeRole = SiteModel.SITE_COLLABORATOR;
        String serverPath = "wibble";
        String acceptUrl = "froob";
        String rejectUrl = "marshmallow";

        authenticationComponent.setCurrentUser(USER_MANAGER);

        // Start Nominated Invitation
        invitationService.inviteNominated(invitee, resourceType,
                resourceName, inviteeRole, serverPath, acceptUrl, rejectUrl);
        
        // Start Moderated Invitation
        invitationService.inviteModerated("", invitee, 
                resourceType, resourceName, inviteeRole);
        
        // Disable Jbpm and enable Activiti
        workflowAdminService.setEnabledEngines(Arrays.asList(ActivitiConstants.ENGINE_ID));
    }
}
