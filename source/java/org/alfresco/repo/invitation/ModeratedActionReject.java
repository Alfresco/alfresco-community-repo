package org.alfresco.repo.invitation;


import java.util.Map;

import org.alfresco.repo.invitation.activiti.RejectModeratedInviteDelegate;
import org.alfresco.repo.invitation.site.AbstractInvitationAction;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * JBPM Action fired when a moderated invitation is rejected.
 * Note - uses a classpath template, rather than a data dictionary template,
 *  so behaves slightly differently to many other mail actions, and can't
 *  currently be localised easily.
 *  
 * <b>Same behaviour as {@link RejectModeratedInviteDelegate}</b>
 */
public class ModeratedActionReject extends AbstractInvitationAction
{
    private static final long serialVersionUID = 4377660284993206875L;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        Map<String, Object> vars = executionContext.getContextInstance().getVariables();
        String siteName = (String) vars.get(WorkflowModelModeratedInvitation.wfVarResourceName);
        String invitee = (String) vars.get(WorkflowModelModeratedInvitation.wfVarInviteeUserName);
        String role = (String) vars.get(WorkflowModelModeratedInvitation.wfVarInviteeRole);
        String reviewer = (String) vars.get(WorkflowModelModeratedInvitation.wfVarReviewer);
        String resourceType = (String) vars.get(WorkflowModelModeratedInvitation.wfVarResourceType);
        String reviewComments = (String) vars.get(WorkflowModelModeratedInvitation.wfVarReviewComments);

        invitationService.rejectModeratedInvitation(siteName, invitee, role, reviewer, resourceType, reviewComments);
    }
}