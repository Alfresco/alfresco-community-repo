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
package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
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
    
    /**
     * Return a single object representing a task of the given taskId for the current user
     * 
     * @return WorkflowTaskItem bean object {@link WorkflowTaskItem}
     */
    public WorkflowTaskItem getTaskById(String taskId)
    {
        // get the task corresponding to the given taskId
        WorkflowTask task = getWorkflowService().getTaskById(taskId);
        
        return new WorkflowTaskItem(this.services, getTemplateImageResolver(), task);
    }
    
    /**
     * Convert a list of WorkflowTask items into bean objects accessable from templates
     * 
     * @param tasks     List of WorkflowTask objects to convert
     * 
     * @return List of WorkflowTaskItem bean wrapper objects
     */
    private List<WorkflowTaskItem> convertTasks(List<WorkflowTask> tasks)
    {
        List<WorkflowTaskItem> items = new ArrayList<WorkflowTaskItem>(tasks.size());
        for (WorkflowTask task : tasks)
        {
            items.add(new WorkflowTaskItem(this.services, getTemplateImageResolver(), task));
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
    public static class WorkflowTaskItem implements NamespacePrefixResolverProvider
    {
        private WorkflowTask task;
        private QNameMap<String, Serializable> properties = null;
        private ServiceRegistry services;
        private TemplateImageResolver resolver;
        
        public WorkflowTaskItem(ServiceRegistry services, TemplateImageResolver resolver, WorkflowTask task)
        {
            this.task = task;
            this.services = services;
            this.resolver = resolver;
        }
        
        public String getType()
        {
            return this.task.title;
        }
        
        public String getQnameType()
        {
           return this.task.definition.metadata.getName().toString();
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
        
        public Map<String, String>[] getTransitions()
        {
           Map<String, String>[] tranMaps = null;
           WorkflowTransition[] transitions = this.task.definition.node.transitions;
           if (transitions != null)
           {
              tranMaps = new HashMap[transitions.length];
              for (int i=0; i<transitions.length; i++)
              {
                 tranMaps[i] = new HashMap<String, String>(2, 1.0f);
                 tranMaps[i].put("label", transitions[i].title);
                 tranMaps[i].put("id", transitions[i].id);
              }
           }
           return (tranMaps != null ? tranMaps : new HashMap[0]);
        }
        
        /**
         * @return A TemplateNode representing the initiator (person) of the workflow
         */
        public TemplateNode getInitiator()
        {
            return new TemplateNode(this.task.path.instance.initiator, this.services, this.resolver);
        }
        
        /**
         * @return The workflow package ref
         */
        public NodeRef getPackage()
        {
            return (NodeRef)this.task.properties.get(WorkflowModel.ASSOC_PACKAGE);
        }
        
        /** 
        * @return The workflow package ref as a TemplateNode 
        */ 
        public TemplateNode getPackageTemplateNode() 
        { 
            NodeRef nodeRef = this.getPackage(); 
            if (nodeRef != null) 
            { 
                return new TemplateNode(nodeRef, this.services, this.resolver); 
            }
            else
            { 
                return null; 
            } 
        } 
        
        /**
         * @return the resources from the package attached to this workflow task
         */
        public List<TemplateContent> getPackageResources()
        {
            List<TemplateContent> resources = new ArrayList<TemplateContent>();
            List<NodeRef> contents = this.services.getWorkflowService().getPackageContents(this.task.id);
            
            NodeService nodeService = this.services.getNodeService();
            DictionaryService ddService = this.services.getDictionaryService();
            
            for(NodeRef nodeRef : contents)
            {
                QName type = nodeService.getType(nodeRef);

                // make sure the type is defined in the data dictionary
                if (ddService.getType(type) != null)
                {
                    // look for content nodes or links to content
                    // NOTE: folders within workflow packages are ignored for now
                    if (ddService.isSubClass(type, ContentModel.TYPE_CONTENT) || 
                            ApplicationModel.TYPE_FILELINK.equals(type))
                    {
                        resources.add(new TemplateNode(nodeRef, this.services, this.resolver));
                    }
                }
            }
            return resources;
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
                    WorkflowTransition[] transitions = this.task.definition.node.transitions;
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
                this.properties = new QNameMap<String, Serializable>(this);
                for (QName qname : this.task.properties.keySet())
                {
                    Serializable value = converter.convertProperty(
                        this.task.properties.get(qname), qname, this.services, this.resolver);
                    this.properties.put(qname.toString(), value);
                }
            }
            return this.properties;
        }
        
        public NamespacePrefixResolver getNamespacePrefixResolver()
        {
            return this.services.getNamespaceService();
        }
    }
}
