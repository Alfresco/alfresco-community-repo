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
