package org.alfresco.repo.invitation;


import java.util.Map;

import org.alfresco.repo.invitation.site.AbstractInvitationAction;
import org.jbpm.graph.exe.ExecutionContext;

public class ModeratedActionApprove extends AbstractInvitationAction
{
    private static final long serialVersionUID = 4377660284993206875L;
    
    /**
     * {@inheritDoc}
     **/
    @SuppressWarnings("unchecked")
    public void execute(ExecutionContext executionContext) throws Exception
    {
        Map<String, Object> variables = executionContext.getContextInstance().getVariables();
        String siteName = (String) variables.get(WorkflowModelModeratedInvitation.wfVarResourceName);
        String invitee = (String) variables.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        String role = (String) variables.get(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        String reviewer = (String) variables.get(WorkflowModelModeratedInvitation.wfVarReviewer);

        invitationService.approveModeratedInvitation(siteName, invitee, role, reviewer);
    }
}