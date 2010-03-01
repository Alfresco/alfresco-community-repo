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

package org.alfresco.repo.workflow.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.taskmgmt.TaskInstanceFactory;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * jBPM factory for creating Alfresco derived Task Instances
 * 
 * @author davidc
 */
public class WorkflowTaskInstanceFactory implements TaskInstanceFactory
{
    private static final long serialVersionUID = -8097108150047415711L;

    private String jbpmEngineName;

    /**
     * @param jbpmEngine the jbpmEngine to set
     */
    public void setJbpmEngine(String jbpmEngine)
    {
        this.jbpmEngineName = jbpmEngine;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.jbpm.taskmgmt.TaskInstanceFactory#createTaskInstance(org.jbpm.graph
     * .exe.ExecutionContext)
     */
    public TaskInstance createTaskInstance(ExecutionContext executionContext)
    {
        WorkflowTaskInstance taskInstance = new WorkflowTaskInstance();
        taskInstance.setJbpmEngineName(jbpmEngineName);
        return taskInstance;
    }
}
