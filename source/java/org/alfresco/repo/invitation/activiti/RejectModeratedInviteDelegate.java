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
import org.alfresco.repo.invitation.ModeratedActionReject;
import org.alfresco.repo.invitation.WorkflowModelModeratedInvitation;

/**
 * Activiti delegate that is executed when a invitation-moderated process is reviewed 
 * and rejected. 
 * 
 * <b>Same behaviour as {@link ModeratedActionReject}</b>
 *
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 4.0
 */
public class RejectModeratedInviteDelegate extends AbstractInvitationDelegate
{
    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        Map<String, Object> vars = execution.getVariables();
        String siteName = (String) vars.get(WorkflowModelModeratedInvitation.wfVarResourceName);
        String invitee = (String) vars.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        String role = (String) vars.get(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        String reviewer = (String) vars.get(WorkflowModelModeratedInvitation.wfVarReviewer);
        String resourceType = (String) vars.get(WorkflowModelModeratedInvitation.wfVarResourceType);
        String reviewComments = (String) vars.get(WorkflowModelModeratedInvitation.wfVarReviewComments);
        
        invitationService.rejectModeratedInvitation(siteName, invitee, role, reviewer, resourceType, reviewComments);
    }
}
