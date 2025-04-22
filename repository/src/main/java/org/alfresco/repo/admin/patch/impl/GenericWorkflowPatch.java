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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;

/**
 * Generic patch that re-deploys a workflow definition
 * 
 * @author David Caruana
 */
public class GenericWorkflowPatch extends AbstractPatch implements ApplicationContextAware
{
    private static final String MSG_DEPLOYED = "patch.genericWorkflow.result.deployed";
    private static final String MSG_UNDEPLOYED = "patch.genericWorkflow.result.undeployed";
    private static final String ERR_PROPERTY_REQUIRED = "patch.genericWorkflow.property_required";
    private static final String MSG_ERROR_ENGINE_DEACTIVATED = "patch.genericWorkflow.error_engine_deactivated";

    private ApplicationContext applicationContext;
    private List<Properties> workflowDefinitions;
    private List<String> undeployWorkflowNames;

    /* (non-Javadoc)
     * 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext) */
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets the Workflow Definitions
     * 
     */
    public void setWorkflowDefinitions(List<Properties> workflowDefinitions)
    {
        this.workflowDefinitions = workflowDefinitions;
    }

    /**
     * Sets the Workflow Names to be undeployed
     * 
     * @param undeployWorkflowNames
     *            list with names
     */
    public void setUndeployWorkflowNames(List<String> undeployWorkflowNames)
    {
        this.undeployWorkflowNames = undeployWorkflowNames;
    }

    @Override
    protected void checkProperties()
    {
        if ((workflowDefinitions == null) && (undeployWorkflowNames == null))
        {
            throw new PatchException(ERR_PROPERTY_REQUIRED, "workflowDefinitions", "undeployWorkflowNames", this);
        }
        super.checkProperties();
    }

    @Override
    protected String applyInternal() throws Exception
    {
        WorkflowDeployer deployer = (WorkflowDeployer) applicationContext.getBean("workflowPatchDeployer");
        WorkflowAdminService workflowAdminService = (WorkflowAdminService) applicationContext.getBean("workflowAdminService");

        if (workflowDefinitions != null)
        {
            for (Properties props : workflowDefinitions)
            {
                props.put(WorkflowDeployer.REDEPLOY, "true");
            }
            deployer.setWorkflowDefinitions(workflowDefinitions);
            deployer.init();
        }

        int undeployed = 0;
        StringBuilder errorMessages = new StringBuilder();
        if (undeployWorkflowNames != null)
        {
            List<String> undeployableWorkflows = new ArrayList<String>(undeployWorkflowNames);
            for (String workflowName : undeployWorkflowNames)
            {
                String engineId = BPMEngineRegistry.getEngineId(workflowName);
                if (workflowAdminService.isEngineEnabled(engineId))
                {
                    undeployableWorkflows.add(workflowName);
                }
                else
                {
                    errorMessages.append(I18NUtil.getMessage(MSG_ERROR_ENGINE_DEACTIVATED, workflowName, engineId));
                }
            }
            undeployed = deployer.undeploy(undeployableWorkflows);
        }

        // done
        StringBuilder msg = new StringBuilder();
        if (workflowDefinitions != null)
        {
            msg.append(I18NUtil.getMessage(MSG_DEPLOYED, workflowDefinitions.size()));
        }
        if (undeployWorkflowNames != null)
        {
            if (msg.length() > 0)
            {
                msg.append(' ');
            }
            msg.append(I18NUtil.getMessage(MSG_UNDEPLOYED, undeployed));
        }
        if (errorMessages.length() > 0)
        {
            msg.append(errorMessages);
        }
        return msg.toString();
    }

}
