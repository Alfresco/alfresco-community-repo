/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow.activiti;

import org.alfresco.repo.workflow.WorkflowAuthorityManager;
import org.alfresco.repo.workflow.WorkflowNodeConverter;
import org.alfresco.repo.workflow.WorkflowPropertyHandlerRegistry;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiWorkflowManager
{
    private final ActivitiPropertyConverter propertyConverter;
    private final WorkflowNodeConverter nodeConverter;
    private final WorkflowPropertyHandlerRegistry handlerRegistry;
    private final ActivitiWorkflowEngine workflowEngine;
    private final WorkflowAuthorityManager workflowAuthorityManager;
    
    public ActivitiWorkflowManager(ActivitiWorkflowEngine workflowEngine,
            ActivitiPropertyConverter propertyConverter,
            WorkflowPropertyHandlerRegistry handlerRegistry, 
            WorkflowNodeConverter nodeConverter,
            WorkflowAuthorityManager workflowAuthorityManager)
    {
        this.workflowEngine = workflowEngine;
        this.propertyConverter = propertyConverter;
        this.handlerRegistry = handlerRegistry;
        this.nodeConverter = nodeConverter;
        this.workflowAuthorityManager = workflowAuthorityManager;
    }

    /**
     * @return the propertyConverter
     */
    public ActivitiPropertyConverter getPropertyConverter()
    {
        return propertyConverter;
    }

    /**
     * @return the nodeConverter
     */
    public WorkflowNodeConverter getNodeConverter()
    {
        return nodeConverter;
    }

    /**
     * @return the handlerRegistry
     */
    public WorkflowPropertyHandlerRegistry getPropertyHandlerRegistry()
    {
        return handlerRegistry;
    }

    /**
     * @return the workflowEngine
     */
    public ActivitiWorkflowEngine getWorkflowEngine()
    {
        return workflowEngine;
    }

    /**
     * @return the workflowAuthorityManager
     */
    public WorkflowAuthorityManager getWorkflowAuthorityManager()
    {
        return workflowAuthorityManager;
    }
}
