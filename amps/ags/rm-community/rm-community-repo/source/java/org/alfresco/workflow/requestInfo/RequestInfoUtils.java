/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.workflow.requestInfo;

import java.util.List;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang3.StringUtils;

/**
 * Util class for the request info workflow
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public final class RequestInfoUtils
{
    private RequestInfoUtils()
    {
        // Will not be called
    }

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
                throw new AlfrescoRuntimeException(
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
     * @return Returns the initiator of the workflow. First it will be checked if
     * a rule creator exists, which means the the workflow was started via rule.
     * In this case the creator of the rule will receive the review task.
     * If a rule creator cannot be found the code will try to find the initiator
     * of the workflow. If also this is not the case the admin user will be returned.
     */
    public static String getInitiator(DelegateTask delegateTask)
    {
        ParameterCheck.mandatory("delegateTask", delegateTask);

        String userName = null;

        String ruleCreator = (String) delegateTask.getVariable("rmwf_ruleCreator");
        if (StringUtils.isBlank(ruleCreator))
        {
            ActivitiScriptNode initiator = (ActivitiScriptNode) delegateTask.getVariable("initiator");
            if (initiator.exists())
            {
                userName = (String) initiator.getProperties().get(ContentModel.PROP_USERNAME.toString());
            }
            else
            {
                userName = AuthenticationUtil.getAdminUserName();
            }
        }
        else
        {
            userName = ruleCreator;
        }

        return userName;
    }

}
