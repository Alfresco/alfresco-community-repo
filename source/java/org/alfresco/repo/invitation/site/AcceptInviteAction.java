package org.alfresco.repo.invitation.site;

import java.util.Map;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets completed
 * along the "accept" transition
 * 
 * @author glen johnson at alfresco com
 * @author Nick Smith
 */
public class AcceptInviteAction extends AbstractInvitationAction
{
    private static final long serialVersionUID = 8133039174866049136L;

    /**
    * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        Map<String, Object> executionVariables = executionContext.getContextInstance().getVariables();
        String invitee = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        String siteName = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarResourceName);
        String inviter = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarInviterUserName);
        String role = (String) executionVariables.get(WorkflowModelNominatedInvitation.wfVarRole);
        
        invitationService.acceptNominatedInvitation(siteName, invitee, role, inviter);
    }
}
