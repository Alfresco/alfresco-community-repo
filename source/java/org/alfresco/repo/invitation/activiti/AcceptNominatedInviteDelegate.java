package org.alfresco.repo.invitation.activiti;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;

/**
 * Activiti delegate that is executed when a nominated invitation request has been accepted.
 * 
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 4.0
 */
public class AcceptNominatedInviteDelegate extends AbstractInvitationDelegate
{

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        Map<String, Object> executionVariables = execution.getVariables();
        String invitee = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        String siteName = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarResourceName);
        String inviter = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviterUserName);
        String role = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarRole);

        invitationService.acceptNominatedInvitation(siteName, invitee, role, inviter);
    }
}
