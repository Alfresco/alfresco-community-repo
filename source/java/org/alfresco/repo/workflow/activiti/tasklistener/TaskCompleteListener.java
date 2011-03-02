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

package org.alfresco.repo.workflow.activiti.tasklistener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.pvm.delegate.TaskListener;
import org.activiti.engine.impl.task.IdentityLinkEntity;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowQNameConverter;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.activiti.properties.ActivitiPropertyConverter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Tasklistener that is notified when a task completes.
 * 
 * This will set a few properties on the task, indicating it is complete
 * and preparing it for historic usage.
 *
 * @author Frederik Heremans
 */
public class TaskCompleteListener implements TaskListener
{
    private ActivitiPropertyConverter propertyConverter;
    private WorkflowQNameConverter qNameConverter;
    
    @Override
    public void notify(DelegateTask task)
    {
    	// Check all mandatory properties are set. This is checked here instead of in
    	// the completeTask() to allow taskListeners to set variable values before checking
    	propertyConverter.checkMandatoryProperties(task);
    	
        // Set properties for ended task
        Map<String, Object> endTaskVariables = new HashMap<String, Object>();

        // Set task status to completed
        String statusKey = qNameConverter.mapQNameToName(WorkflowModel.PROP_STATUS);
        endTaskVariables.put(statusKey, "Completed");
        
        // Add pooled actors to task-variables to be preserved in history (if any)
        addPooledActorsAsVariable(task, endTaskVariables);
        
        // Set variables locally on the task
        task.setVariablesLocal(endTaskVariables);
    }

    private void addPooledActorsAsVariable(DelegateTask task,
			Map<String, Object> variables) {

    	List<IdentityLinkEntity> links = ((TaskEntity)task).getIdentityLinks();
        if(links.size() > 0)
        {
        	// Add to list of IdentityLink
        	List<IdentityLink> identityLinks = new ArrayList<IdentityLink>();
        	identityLinks.addAll(links);
        	
        	List<NodeRef> pooledActorRefs = propertyConverter.getPooledActorsReference(identityLinks);
        	
        	// Save references as a variable
        	List<String> nodeIds = new ArrayList<String>();
        	for(NodeRef ref : pooledActorRefs)
        	{
        		nodeIds.add(ref.toString());
        	}
        	variables.put(ActivitiConstants.PROP_POOLED_ACTORS_HISTORY, nodeIds);
        }
	}
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.qNameConverter = new WorkflowQNameConverter(namespaceService);
    }
    
    /**
     * @param propertyConverter the propertyConverter to set
     */
    public void setPropertyConverter(ActivitiPropertyConverter propertyConverter)
    {
        this.propertyConverter = propertyConverter;
    }
}

