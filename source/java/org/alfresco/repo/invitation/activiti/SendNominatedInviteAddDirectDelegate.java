package org.alfresco.repo.invitation.activiti;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;

/**
 * Activiti delegate that is executed when a invitation request has
 * been sent.
 *
 * @author Nick Smith
 * @author Frederik Heremans
 * @author Ray Gauss II
 * @since 4.0
 */
public class SendNominatedInviteAddDirectDelegate extends AbstractInvitationDelegate
{
    public static final String EMAIL_TEMPLATE_XPATH = 
            "app:company_home/app:dictionary/app:email_templates/cm:invite/cm:invite-email-add-direct.html.ftl";
    public static final String EMAIL_SUBJECT_KEY = 
            "invitation.invitesender.emailAddDirect.subject";

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        String invitationId = ActivitiConstants.ENGINE_ID + "$" + execution.getProcessInstanceId();
        Map<String, Object> variables = execution.getVariables();
        invitationService.sendNominatedInvitation(invitationId, EMAIL_TEMPLATE_XPATH, EMAIL_SUBJECT_KEY, variables);
    }
}
