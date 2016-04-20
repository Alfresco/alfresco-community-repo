package org.alfresco.repo.invitation.site;

import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarInviteeUserName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarResourceName;
import static org.alfresco.repo.invitation.WorkflowModelNominatedInvitation.wfVarWorkflowInstanceId;

import java.util.Map;

import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets cancelled
 * along the "cancel" transition
 * 
 * @author glen johnson at alfresco com
 */
public class CancelInviteAction extends AbstractInvitationAction
{
    private static final long serialVersionUID = -7603494389312553072L;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void execute(ExecutionContext executionContext) throws Exception
    {
        Map<String, Object> executionVariables = executionContext.getContextInstance().getVariables();
        String currentInviteId = JBPMEngine.ENGINE_ID + "$" + executionContext.getContextInstance().getProcessInstance().getId();

        // Get the invitee user name and site short name variables off the execution context
        String invitee = (String) executionVariables.get(wfVarInviteeUserName);
        String siteName = (String) executionVariables.get(wfVarResourceName);
        String inviteId = (String) executionVariables.get(wfVarWorkflowInstanceId);

        invitationService.cancelInvitation(siteName, invitee, inviteId, currentInviteId);
    }
}
