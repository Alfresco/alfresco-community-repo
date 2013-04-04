/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.workflow.requestInfo;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.notification.EMailNotificationProvider;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.notification.NotificationContext;
import org.alfresco.util.ParameterCheck;

/**
 * Request info workflow notifier.
 * After the pooled task has been finished the initiator of the workflow will
 * get a task to verify the information. The initiator will also receive an email.
 *
 * @author Tuna Aksoy
 * @since v2.1
 */
public class RequestInfoNotifier implements TaskListener
{
    /**
     * @see org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.delegate.DelegateTask)
     */
    @Override
    public void notify(DelegateTask delegateTask)
    {
        ParameterCheck.mandatory("delegateTask", delegateTask);

        // Set the workflow description for the task
        // FIXME: I18N!!!
        // FIXME: Record name!!!
        delegateTask.setVariable("bpm_workflowDescription", "Information provided for record '" + "test.doc" + "'");

        // Assign the task to the initiator
        String initiator = getInitiator(delegateTask);
        delegateTask.setAssignee(initiator);

        // Create the context and send an email to the initiator
        NotificationContext notificationContext = new NotificationContext();
        notificationContext.setAsyncNotification(true);
        notificationContext.setIgnoreNotificationFailure(true);
        notificationContext.addTo(initiator);
        // FIXME: I18N!!! and get the record name and the user name who provided the information
        notificationContext.setSubject("Information provided for the record '" + "" + "'.");
        notificationContext.setBody("The user '" + "' has provided the needed information for the record '" + "" + "'.");

        // Send the email
        getServiceRegistry().getNotificationService().sendNotification(EMailNotificationProvider.NAME, notificationContext);
    }

    /**
     * Helper method to extract the initiator from the task
     *
     * @param delegateTask  The delegate task
     * @return Returns the initiator of the workflow. If the initiator does not exist the admin user will be returned.
     */
    private String getInitiator(DelegateTask delegateTask)
    {
        String userName = null;
        ActivitiScriptNode initiator = (ActivitiScriptNode) delegateTask.getVariable("initiator");
        if (initiator.exists())
        {
            userName = (String) initiator.getProperties().get(ContentModel.PROP_USERNAME.toString());
        }
        else
        {
            userName = AuthenticationUtil.getAdminUserName();
        }
        return userName;
    }

    //FIXME: Is there a better way to call services?

    /**
     * Helper method for getting the service registry in order to call services
     *
     * @return Returns the service registry
     */
    private ServiceRegistry getServiceRegistry()
    {
        ProcessEngineConfigurationImpl config = Context.getProcessEngineConfiguration();
        if (config != null)
        {
            // Fetch the registry that is injected in the activiti spring-configuration
            ServiceRegistry registry = (ServiceRegistry) config.getBeans().get(ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
            if (registry == null)
            {
                throw new RuntimeException(
                        "Service-registry not present in ProcessEngineConfiguration beans, expected ServiceRegistry with key" +
                                ActivitiConstants.SERVICE_REGISTRY_BEAN_KEY);
            }
            return registry;
        }
        throw new IllegalStateException("No ProcessEngineCOnfiguration found in active context");
    }
}
