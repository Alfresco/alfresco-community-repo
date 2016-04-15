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
