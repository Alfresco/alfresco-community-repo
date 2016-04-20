
package org.alfresco.repo.invitation.site;

import java.util.Map;

import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ExecutionContext;

public class SendInviteAction extends AbstractInvitationAction
{
    private static final long serialVersionUID = 8133039174866049136L;
    
    public static final String EMAIL_TEMPLATE_XPATH = 
            "app:company_home/app:dictionary/app:email_templates/cm:invite/cm:invite-email.html.ftl";
    public static final String EMAIL_SUBJECT_KEY = 
            "invitation.invitesender.email.subject";

    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext context) throws Exception
    {
        ContextInstance contextInstance = context.getContextInstance();
        long processId = contextInstance.getProcessInstance().getId();
        String inviteId = JBPMEngine.ENGINE_ID + "$" + processId;
        Map<String, Object> executionVariables = contextInstance.getVariables();
        invitationService.sendNominatedInvitation(inviteId, EMAIL_TEMPLATE_XPATH, EMAIL_SUBJECT_KEY, executionVariables);
    }
}
