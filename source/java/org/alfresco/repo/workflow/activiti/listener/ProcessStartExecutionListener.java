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

package org.alfresco.repo.workflow.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;

/**
 * An {@link ExecutionListener} that set all additional variables that are needed 
 * when process starts.
 * 
 * @author Frederik Heremans
 * @since 4.0
 */
public class ProcessStartExecutionListener implements ExecutionListener
{
    public void notify(DelegateExecution execution) throws Exception
    {
        // Add the workflow ID
        execution.setVariable(WorkflowConstants.PROP_WORKFLOW_INSTANCE_ID, BPMEngineRegistry
                    .createGlobalId(ActivitiConstants.ENGINE_ID, execution.getId()));
    }
}
