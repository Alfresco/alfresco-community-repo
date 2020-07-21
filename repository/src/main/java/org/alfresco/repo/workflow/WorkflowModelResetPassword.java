/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.repo.workflow;

import org.alfresco.service.namespace.QName;

/**
 * @author Jamal Kaabi-Mofrad
 * @since 5.2.1
 */
public interface WorkflowModelResetPassword
{
    // namespace
    String NAMESPACE_URI = "http://www.alfresco.org/model/workflow/resetpassword/1.0";

    // process name
    String WORKFLOW_DEFINITION_NAME = "activiti$resetPassword";

    // task names
    String TASK_RESET_PASSWORD = "resetPasswordTask";

    // timers
    QName WF_PROP_TIMER_END = QName.createQName(NAMESPACE_URI, "endTimer");

    // workflow properties
    QName WF_PROP_USERNAME = QName.createQName(NAMESPACE_URI, "userName");
    QName WF_PROP_USER_EMAIL = QName.createQName(NAMESPACE_URI, "userEmail");
    QName WF_PROP_KEY = QName.createQName(NAMESPACE_URI, "key");
    QName WF_PROP_PASSWORD = QName.createQName(NAMESPACE_URI, "password");
    QName WF_PROP_CLIENT_NAME = QName.createQName(NAMESPACE_URI, "clientName");

    // workflow execution context variable names
    String WF_PROP_USERNAME_ACTIVITI = "resetpasswordwf_userName";
    String WF_PROP_USER_EMAIL_ACTIVITI = "resetpasswordwf_userEmail";
    String WF_PROP_KEY_ACTIVITI = "resetpasswordwf_key";
    String WF_PROP_PASSWORD_ACTIVITI = "resetpasswordwf_password";
    String WF_PROP_CLIENT_NAME_ACTIVITI = "resetpasswordwf_clientName";
}
