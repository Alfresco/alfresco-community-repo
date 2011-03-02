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

package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;

/**
 * A helper class used to start workflows. The builder is a stateful object that
 * accumulates the various parameters and package items used to start the
 * workflow. The workflow is started when the build() method is called.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class WorkflowBuilder
{
    private final WorkflowService workflowService;
    private final PackageManager packageMgr;
    private final WorkflowDefinition definition;

    private final Map<QName, Serializable> params = new HashMap<QName, Serializable>();
    private NodeRef packageNode = null;
    
    public WorkflowBuilder(WorkflowDefinition definition, WorkflowService workflowService, NodeService nodeService)
    {
        this.workflowService = workflowService;
        this.packageMgr = new PackageManager(workflowService, nodeService, null);
        this.definition = definition;
    }
    
    public void addParameter(QName name, Serializable value)
    {
        params.put(name, value);
    }
    
    public void addAssociationParameter(QName name, List<NodeRef> values)
    {
        if (values instanceof Serializable)
        {
            params.put(name, (Serializable) values);
        }
    }
    
    /**
     * @param packageNode the packageNode to set
     */
    public void setPackageNode(NodeRef packageNode)
    {
        this.packageNode = packageNode;
    }
    
    public void addPackageItems(List<NodeRef> items)
    {
        packageMgr.addItems(items);
    }
    
    public WorkflowInstance build()
    {
        NodeRef packageRef = packageMgr.create(packageNode);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
        signalStartTask(path);
        return path.getInstance();
    }

    private void signalStartTask(WorkflowPath path)
    {
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        if (tasks.size() == 1)
        {
            WorkflowTask startTask = tasks.get(0);
            workflowService.endTask(startTask.getId(), null);
        }
        else
        {
            throw new WorkflowException("Start task not found! Expected 1 task but found: " + tasks.size());
        }
    }

}
