/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.invitation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.invitation.Invitation;
import org.alfresco.service.cmr.invitation.NominatedInvitation;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class ActivitiInvitationServiceImplTests extends AbstractInvitationServiceImplTest
{
    private WorkflowService workflowService;
    
    /**
     * Nominated invites workflows finish without waiting for user accept
     */
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

        List<WorkflowPath> paths = workflowService.getWorkflowPaths(nomInvite.getInviteId());
        assertEquals(0, paths.size());
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
        
        // Enable Activiti
        workflowAdminService.setEnabledEngines(Arrays.asList(ActivitiConstants.ENGINE_ID));
    }
    
    public void testAddExistingUser() throws Exception
    {
        this.invitationServiceImpl.setNominatedInvitationWorkflowId(
                WorkflowModelNominatedInvitation.WORKFLOW_DEFINITION_NAME_ACTIVITI_ADD_DIRECT);
        testNominatedInvitationExistingUser(false);
        
        List<WorkflowTask> initiatorTasks = 
                this.workflowService.getAssignedTasks(USER_MANAGER, WorkflowTaskState.IN_PROGRESS);
        assertEquals(0, initiatorTasks.size());
    }
}
