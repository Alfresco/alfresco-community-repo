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
