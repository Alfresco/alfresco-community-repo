/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.invitation.activiti;

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;

/**
 * Activiti delegate that is executed when a invitation request has been cancelled.
 * 
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 4.0
 */
public class CancelNominatedInviteDelegate extends AbstractInvitationDelegate
{
    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        Map<String, Object> executionVariables = execution.getVariables();
        String currentInviteId = ActivitiConstants.ENGINE_ID + "$" + execution.getProcessInstanceId();

        // Get the invitee user name and site short name variables off the execution context
        String invitee = (String) executionVariables.get(wfVarInviteeUserName);
        String siteName = (String) executionVariables.get(wfVarResourceName);
        String inviteId = (String) executionVariables.get(wfVarWorkflowInstanceId);

        invitationService.cancelInvitation(siteName, invitee, inviteId, currentInviteId);
    }
}
