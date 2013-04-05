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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNodeList;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;

/**
 * An assignment handler for the request info workflow.
 * An RM manager/admin can select one or more user(s) and/or
 * one or more group(s) when starting the request info workflow.
 * This assignment handler assigns for everyone a task (it is a pooled task).
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RequestInfoAssignmentHandler implements TaskListener
{
    /**
     * @see org.activiti.engine.delegate.TaskListener#notify(org.activiti.engine.delegate.DelegateTask)
     */
    @Override
    public void notify(DelegateTask delegateTask)
    {
        ParameterCheck.mandatory("delegateTask", delegateTask);

        // Set the workflow description for the task
        delegateTask.setVariable("bpm_workflowDescription", getWorkflowDescription(delegateTask));

        // Get the list of user(s) and/or group(s)
        ActivitiScriptNodeList usersAndGroups = (ActivitiScriptNodeList) delegateTask.getVariable("rmwf_mixedAssignees");

        // Check if it was possible to extract the user(s) and/or group(s)
        if (usersAndGroups == null)
        {
            throw new AlfrescoRuntimeException("It was not possible to extract the user(s) and/or group(s)!!!");
        }

        // Define lists for candidate user(s)/group(s)
        List<String> candidateUsers = new ArrayList<String>();
        List<String> candidateGroups = new ArrayList<String>();

        // Iterate through the list add user(s)/group(s) to the lists
        for (ActivitiScriptNode activitiScriptNode : usersAndGroups)
        {
            // Get the node type
            String type = activitiScriptNode.getType();
            // Get the properties
            Map<String, Object> properties = activitiScriptNode.getProperties();

            // Check if it is a user or a group
            if (type.equalsIgnoreCase(ContentModel.TYPE_PERSON.toString()))
            {
                // Add the user
                candidateUsers.add((String) properties.get(ContentModel.PROP_USERNAME.toString()));
            }
            else if (type.equalsIgnoreCase(ContentModel.TYPE_AUTHORITY_CONTAINER.toString()))
            {
                // Add the group
                candidateGroups.add((String) properties.get(ContentModel.PROP_AUTHORITY_NAME.toString()));
            }
            else
            {
                throw new AlfrescoRuntimeException("The type '" + type + "' is neither a user nor a group!!!");
            }
        }

        // Check if there is at least one user or one group
        if (candidateUsers.size() == 0 && candidateGroups.size() == 0)
        {
            throw new AlfrescoRuntimeException("Neither a user nor a group was found!!!");
        }

        // Add the user(s) to the task
        if (candidateUsers.size() > 0)
        {
            delegateTask.addCandidateUsers(candidateUsers);
        }

        // Add the group(s) to the task
        if (candidateGroups.size() > 0)
        {
            delegateTask.addCandidateGroups(candidateGroups);
        }
    }

    private String getWorkflowDescription(DelegateTask delegateTask)
    {
        // FIXME: I18N!!!
        return "Information requested for record '" + getRecordName(delegateTask) + "'";
    }

    private String getRecordName(DelegateTask delegateTask)
    {
        ActivitiScriptNode scriptNode = (ActivitiScriptNode) delegateTask.getVariable("bpm_package");
        NodeService nodeService = getServiceRegistry().getNodeService();
        NodeRef docRef= nodeService.getChildAssocs(scriptNode.getNodeRef()).get(0).getChildRef();
        return (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
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
