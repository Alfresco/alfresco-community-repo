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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNameMap;

/**
 * Workflow and task support in FreeMarker templates.
 * 
 * @author Kevin Roast
 */
public class Workflow extends BaseTemplateProcessorExtension
{
    private ServiceRegistry services;

    /**
     * Sets the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * Return a list of objects representing the assigned tasks for the current user
     * 
     * @return list of WorkflowTaskItem bean objects {@link WorkflowTaskItem}
     */
    public List<WorkflowTaskItem> getAssignedTasks()
    {
        // get the "in progress" tasks for the current user
        List<WorkflowTask> tasks = getWorkflowService().getAssignedTasks(
              this.services.getAuthenticationService().getCurrentUserName(),
              WorkflowTaskState.IN_PROGRESS);
        
        return convertTasks(tasks);
    }
    
    /**
     * Return a list of objects representing the pooled tasks for the current user
     * 
     * @return list of WorkflowTaskItem bean objects {@link WorkflowTaskItem}
     */
    public List<WorkflowTaskItem> getPooledTasks()
    {
        // get the "pooled" tasks for the current user
        List<WorkflowTask> tasks = getWorkflowService().getPooledTasks(
              this.services.getAuthenticationService().getCurrentUserName());
        
        return convertTasks(tasks);
    }
    
    /**
     * Return a list of objects representing the completed tasks for the current user
     * 
     * @return list of WorkflowTaskItem bean objects {@link WorkflowTaskItem}
     */
    public List<WorkflowTaskItem> getCompletedTasks()
    {
        // get the "completed" tasks for the current user
        List<WorkflowTask> tasks = getWorkflowService().getAssignedTasks(
              this.services.getAuthenticationService().getCurrentUserName(),
              WorkflowTaskState.COMPLETED);
        
        return convertTasks(tasks);
    }
    
    private List<WorkflowTaskItem> convertTasks(List<WorkflowTask> tasks)
    {
        List<WorkflowTaskItem> items = new ArrayList<WorkflowTaskItem>(tasks.size());
        for (WorkflowTask task : tasks)
        {
            items.add(new WorkflowTaskItem(task));
        }
        
        return items;
    }
    
    private WorkflowService getWorkflowService()
    {
        return this.services.getWorkflowService();
    }
    
    
    /**
     * Simple bean wrapper around a WorkflowTask item 
     */
    public class WorkflowTaskItem
    {
        private WorkflowTask task;
        private QNameMap<String, Serializable> properties = null;
        
        WorkflowTaskItem(WorkflowTask task)
        {
            this.task = task;
        }
        
        public String getType()
        {
            return this.task.title;
        }
        
        public String getName()
        {
            return this.task.description;
        }
        
        public String getDescription()
        {
            return this.task.path.instance.description;
        }
        
        public String getId()
        {
            return this.task.id;
        }
        
        public boolean getIsCompleted()
        {
            return (this.task.state == WorkflowTaskState.COMPLETED);
        }
        
        public Date getStartDate()
        {
            return this.task.path.instance.startDate;
        }
        
        /**
         * @return A TemplateNode representing the initiator (person) of the workflow
         */
        public TemplateNode getInitiator()
        {
            return new TemplateNode(this.task.path.instance.initiator, services, getTemplateImageResolver());
        }
        
        /**
         * @return A TemplateNode representing the workflow package object
         */
        public TemplateNode getPackage()
        {
            NodeRef packageRef = (NodeRef)this.task.properties.get(WorkflowModel.ASSOC_PACKAGE);
            return new TemplateNode(packageRef, services, getTemplateImageResolver());
        }
        
        /**
         * @return the 'outcome' label from a completed task
         */
        public String getOutcome()
        {
            String outcome = null;
            if (task.state.equals(WorkflowTaskState.COMPLETED))
            {
                // add the outcome label for any completed task
                String transition = (String)task.properties.get(WorkflowModel.PROP_OUTCOME);
                if (transition != null)
                {
                    WorkflowTransition[] transitions = task.definition.node.transitions;
                    for (WorkflowTransition trans : transitions)
                    {
                        if (trans.id.equals(transition))
                        {
                            outcome = trans.title;
                            break;
                        }
                    }
                }
            }
            return outcome;
        }
        
        /**
         * @return A map of properties for the workflow task, includes all appropriate bpm model properties.
         */
        public Map<String, Serializable> getProperties()
        {
            if (this.properties == null)
            {
                // convert properties to a QName accessable Map with TemplateNode objects as required
                PropertyConverter converter = new PropertyConverter();
                this.properties = new QNameMap<String, Serializable>(services.getNamespaceService());
                for (QName qname : this.task.properties.keySet())
                {
                    Serializable value = converter.convertProperty(
                        this.task.properties.get(qname), qname, services, getTemplateImageResolver());
                    this.properties.put(qname.toString(), value);
                }
            }
            return this.properties;
        }
    }
}
