/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.invitation.activiti;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;

/**
 * Activiti delegate that is executed when a invitation request has been sent.
 * 
 * @author Constantin Popa
 */
public class SendModeratedInviteDelegate extends AbstractInvitationDelegate
{
    
    private String emailTemplatePath;
    public static final String ENTERPRISE_EMAIL_TEMPLATE_PATH = "app:company_home/app:dictionary/app:email_templates/cm:invite/cm:invite-email-moderated.html.ftl";
   
    public static final String EMAIL_SUBJECT_KEY =
            "invitation.moderated.email.subject";
    

    public void setEmailTemplatePath(String emailTemplatePath)
    {
        this.emailTemplatePath = emailTemplatePath;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        String invitationId = ActivitiConstants.ENGINE_ID + "$" + execution.getProcessInstanceId();
        Map<String, Object> variables = execution.getVariables();
        invitationService.sendModeratedInvitation(invitationId, emailTemplatePath, EMAIL_SUBJECT_KEY, variables);
    }
}
