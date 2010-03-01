/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;
import java.util.Properties;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Generic patch that re-deploys a workflow definition
 * 
 * @author David Caruana
 */
public class GenericWorkflowPatch extends AbstractPatch implements ApplicationContextAware
{
    private static final String MSG_DEPLOYED = "patch.genericWorkflow.result.deployed";
    
    private ApplicationContext applicationContext;
    private List<Properties> workflowDefinitions;

    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * Sets the Workflow Definitions
     * 
     * @param workflowDefinitions
     */
    public void setWorkflowDefinitions(List<Properties> workflowDefinitions)
    {
        this.workflowDefinitions = workflowDefinitions;
    }

    @Override
    protected void checkProperties()
    {
        checkPropertyNotNull(workflowDefinitions, "workflowDefinitions");
        super.checkProperties();
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        WorkflowDeployer deployer = (WorkflowDeployer)applicationContext.getBean("workflowPatchDeployer");
        
        for (Properties props : workflowDefinitions)
        {
            props.put(WorkflowDeployer.REDEPLOY, "true");
        }
        deployer.setWorkflowDefinitions(workflowDefinitions);
        deployer.init();
        
        // done
        return I18NUtil.getMessage(MSG_DEPLOYED, workflowDefinitions.size());
    }

}
