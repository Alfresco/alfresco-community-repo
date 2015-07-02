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
package org.alfresco.service.cmr.workflow;

import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;


/**
 * Workflow Task Query
 * 
 * Provides support for setting predicates and order by.
 * 
 * @author davidc
 */
@AlfrescoPublicApi
public class WorkflowTaskQuery
{
    // Engine Id
    private String engineId = null;
    
    // task predicates
    private String taskId;
    private WorkflowTaskState taskState = WorkflowTaskState.IN_PROGRESS;
    private QName taskName;
    private String actorId;    
    private Map<QName, Object> taskCustomProps; 
    
    // process predicates
    private String processId;
    private QName processName;
    private String workflowDefinitionName;
    private Boolean active = Boolean.TRUE;
    private Map<QName, Object> processCustomProps;
    
    // order by
    private OrderBy[] orderBy;
    
    // result set size
    private int limit = -1;
    
    /**
     * Order By Columns
     */
    public enum OrderBy
    {
        TaskId_Asc,
        TaskId_Desc,
        TaskCreated_Asc,
        TaskCreated_Desc,
        TaskDue_Asc,
        TaskDue_Desc,
        TaskName_Asc,
        TaskName_Desc,
        TaskActor_Asc,
        TaskActor_Desc,
        TaskState_Asc,
        TaskState_Desc;
    }
    
    
    /**
     * @param orderBy OrderBy[]
     */
    public void setOrderBy(OrderBy[] orderBy)
    {
        this.orderBy = orderBy; 
    }
    
    /**
     * @return OrderBy[]
     */
    public OrderBy[] getOrderBy()
    {
        return orderBy;
    }
    
    /**
     * @return String
     */
    public String getTaskId()
    {
        return taskId;
    }
    
    /** 
     * @param taskId String
     */
    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }
    
    /**
     * @return Map
     */
    public Map<QName, Object> getTaskCustomProps()
    {
        return taskCustomProps;
    }

    /**
     * @param taskCustomProps Map<QName, Object>
     */
    public void setTaskCustomProps(Map<QName, Object> taskCustomProps)
    {
        this.taskCustomProps = taskCustomProps;
    }

    /**
     * @return WorkflowTaskState
     */
    public WorkflowTaskState getTaskState()
    {
        return taskState;
    }
    
    /**
     * @param taskState WorkflowTaskState
     */
    public void setTaskState(WorkflowTaskState taskState)
    {
        this.taskState = taskState;
    }
    
    /**
     * @return QName
     */
    public QName getTaskName()
    {
        return taskName;
    }
    
    /**
     * @param taskName QName
     */
    public void setTaskName(QName taskName)
    {
        this.taskName = taskName;
    }
    
    /**
     * @return String
     */
    public String getActorId()
    {
        return actorId;
    }
    
    /**
     * @param actorId String
     */
    public void setActorId(String actorId)
    {
        this.actorId = actorId;
    }
    
    /**
     * @return String
     */
    public String getProcessId()
    {
        return processId;
    }

    /**
     * Filters ont he {@link WorkflowInstance} Id.
     * @param processId String
     */
    public void setProcessId(String processId)
    {
        this.processId = processId;
    }
    
    /**
     * @return QName
     */
    public QName getProcessName()
    {
        return processName;
    }

    /**
     * Use {@link WorkflowTaskQuery#setWorkflowDefinitionName(String)} instead.
     * Filters on the {@link WorkflowDefinition} name. When using Activiti,
     * the method {@link #setWorkflowDefinitionName(String)} should be used
     * instead of this method.
     * 
     * @param processName QName
     */
    @Deprecated
    public void setProcessName(QName processName)
    {
        this.processName = processName;
    }
    
    /**
     * @return String
     */
    public String getWorkflowDefinitionName()
    {
        return workflowDefinitionName;
    }
    
    /**
     * Filters on the {@link WorkflowDefinition} name.
     * @param workflowDefinitionName String
     */
    public void setWorkflowDefinitionName(String workflowDefinitionName)
    {
        this.workflowDefinitionName = workflowDefinitionName;
    }
    
    /**
     * @return Boolean
     */
    public Boolean isActive()
    {
        return active;
    }
    
    /**
     * @param active Boolean
     */
    public void setActive(Boolean active)
    {
        this.active = active;
    }

    /**
     * @return Map
     */
    public Map<QName, Object> getProcessCustomProps()
    {
        return processCustomProps;
    }

    /**
     * @param processCustomProps Map<QName, Object>
     */
    public void setProcessCustomProps(Map<QName, Object> processCustomProps)
    {
        this.processCustomProps = processCustomProps;
    }

    public int getLimit()
    {
        return this.limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }
    
    /**
     * @param engineId the engineId to set
     */
    public void setEngineId(String engineId)
    {
        this.engineId = engineId;
    }
    
    /**
     * @return the engineId
     */
    public String getEngineId()
    {
        return engineId;
    }
}
