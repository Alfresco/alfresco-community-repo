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

import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;

/**
 * Util class for the request info workflow
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class RequestInfoUtils
{
    //FIXME: Is there a better way to call services?

    /**
     * Helper method to get the service registry in order to call services
     *
     * @return Returns the service registry
     */
    public static ServiceRegistry getServiceRegistry()
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

    /**
     * Helper method to extract the record name from the task
     *
     * @param delegateTask  The delegate task
     * @return Returns the name of the record or an empty string if the record name could not be found
               (may be because the record has been deleted in the mean time)
     */
    public static String getRecordName(DelegateTask delegateTask)
    {
        ParameterCheck.mandatory("delegateTask", delegateTask);

        String recordName = StringUtils.EMPTY;

        NodeService nodeService = getServiceRegistry().getNodeService();
        ActivitiScriptNode scriptNode = (ActivitiScriptNode) delegateTask.getVariable("bpm_package");
        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(scriptNode.getNodeRef());

        if (childAssocs.size() > 0)
        {
            NodeRef docRef= childAssocs.get(0).getChildRef();
            recordName = (String) nodeService.getProperty(docRef, ContentModel.PROP_NAME);
        }

        return recordName;
    }

    /**
     * Helper method to extract the initiator from the task
     *
     * @param delegateTask  The delegate task
     * @return Returns the initiator of the workflow. If the initiator does not exist the admin user name will be returned.
     */
    public static String getInitiator(DelegateTask delegateTask)
    {
        ParameterCheck.mandatory("delegateTask", delegateTask);

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

}
