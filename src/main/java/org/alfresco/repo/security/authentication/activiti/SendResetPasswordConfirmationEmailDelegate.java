/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.repo.security.authentication.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * This {@link JavaDelegate activiti delegate} is executed when a user is finished resetting his/her password.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.2.1
 */
public class SendResetPasswordConfirmationEmailDelegate extends AbstractResetPasswordDelegate
{
    private static final String EMAIL_SUBJECT_KEY = "reset-password-confirmation.email.subject";
    private static final String EMAIL_TEMPLATE_PATH = "alfresco/templates/reset-password-email-templates/reset-password-confirmation-email-template.ftl";

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception
    {
        resetPasswordService.sendResetPasswordConfirmationEmail(delegateExecution, EMAIL_TEMPLATE_PATH, EMAIL_SUBJECT_KEY);
    }
}
