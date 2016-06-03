package org.alfresco.repo.invitation.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;

/**
 * Activiti delegate that is executed when a invitation request has been rejected.
 * 
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 4.0
 */
public class RejectNominatedInviteDelegate extends AbstractInvitationDelegate
{
    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        // Get the invitee user name
        String invitee = (String) execution.getVariable(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        String invitationId = ActivitiConstants.ENGINE_ID + "$" + execution.getProcessInstanceId();
        invitationService.deleteAuthenticationIfUnused(invitee, invitationId);
    }
}
