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
