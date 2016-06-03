package org.alfresco.repo.invitation.site;

import org.alfresco.repo.invitation.WorkflowModelNominatedInvitation;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * This class contains logic that gets executed when
 * the wf:invitePendingTask in the invite workflow gets completed
 * along the "reject" transition
 * 
 * @author Nick Smith
 */
public class RejectInviteAction extends AbstractInvitationAction
{
    private static final long serialVersionUID = 4377660284993206875L;

    /**
    * {@inheritDoc}
     */
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        // get the invitee user name
        String invitee = (String) executionContext.getVariable(WorkflowModelNominatedInvitation.wfVarInviteeUserName);
        String invitationId = JBPMEngine.ENGINE_ID + "$" + executionContext.getContextInstance().getProcessInstance().getId();
        
        invitationService.deleteAuthenticationIfUnused(invitee, invitationId);
    }
}
