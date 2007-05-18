/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.workflow;

import java.util.Map;

import org.alfresco.service.namespace.QName;


/**
 * Workflow Task Query
 * 
 * Provides support for setting predicates and order by.
 * 
 * @author davidc
 */
public class WorkflowTaskQuery
{
    // task predicates
    private String taskId;
    private WorkflowTaskState taskState = WorkflowTaskState.IN_PROGRESS;
    private QName taskName;
    private String actorId;    
    private Map<QName, Object> taskCustomProps; 
    
    // process predicates
    private String processId;
    private QName processName;
    private Boolean active = Boolean.TRUE;
    private Map<QName, Object> processCustomProps;

    // order by
    private OrderBy[] orderBy;
    
    
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
        TaskState_Desc
    };
    
    
    /**
     * @param orderBy
     */
    public void setOrderBy(OrderBy[] orderBy)
    {
        this.orderBy = orderBy; 
    }
    
    /**
     * @return
     */
    public OrderBy[] getOrderBy()
    {
        return orderBy;
    }
    
    /**
     * @return
     */
    public String getTaskId()
    {
        return taskId;
    }
    
    /** 
     * @param taskId
     */
    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }
    
    /**
     * @return
     */
    public Map<QName, Object> getTaskCustomProps()
    {
        return taskCustomProps;
    }

    /**
     * @param taskCustomProps
     */
    public void setTaskCustomProps(Map<QName, Object> taskCustomProps)
    {
        this.taskCustomProps = taskCustomProps;
    }

    /**
     * @return
     */
    public WorkflowTaskState getTaskState()
    {
        return taskState;
    }
    
    /**
     * @param taskState
     */
    public void setTaskState(WorkflowTaskState taskState)
    {
        this.taskState = taskState;
    }
    
    /**
     * @return
     */
    public QName getTaskName()
    {
        return taskName;
    }
    
    /**
     * @param taskName
     */
    public void setTaskName(QName taskName)
    {
        this.taskName = taskName;
    }
    
    /**
     * @return
     */
    public String getActorId()
    {
        return actorId;
    }
    
    /**
     * @param actorId
     */
    public void setActorId(String actorId)
    {
        this.actorId = actorId;
    }
    
    /**
     * @return
     */
    public String getProcessId()
    {
        return processId;
    }

    /**
     * @param processId
     */
    public void setProcessId(String processId)
    {
        this.processId = processId;
    }
    
    /**
     * @return
     */
    public QName getProcessName()
    {
        return processName;
    }

    /**
     * @param processName
     */
    public void setProcessName(QName processName)
    {
        this.processName = processName;
    }
    
    /**
     * @return
     */
    public Boolean isActive()
    {
        return active;
    }
    
    /**
     * @param active
     */
    public void setActive(Boolean active)
    {
        this.active = active;
    }

    /**
     * @return
     */
    public Map<QName, Object> getProcessCustomProps()
    {
        return processCustomProps;
    }

    /**
     * @param processCustomProps
     */
    public void setProcessCustomProps(Map<QName, Object> processCustomProps)
    {
        this.processCustomProps = processCustomProps;
    }

}
