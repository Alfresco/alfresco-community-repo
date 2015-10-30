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

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.invitation.WorkflowModelModeratedInvitation;

/**
 * Activiti delegate that is executed when a invitation-moderated process is reviewed and approved.
 * 
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 4.0
 */
public class ApproveModeratedInviteDelegate extends AbstractInvitationDelegate
{
    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        Map<String, Object> variables = execution.getVariables();
        String siteName = (String) variables.get(WorkflowModelModeratedInvitation.wfVarResourceName);
        String invitee = (String) variables.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        String role = (String) variables.get(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        String reviewer = (String) variables.get(WorkflowModelModeratedInvitation.wfVarReviewer);

        invitationService.approveModeratedInvitation(siteName, invitee, role, reviewer);
    }
}
