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

import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper class for updating and transitioning {@link WorkflowTask
 * WorkflowTasks}. This is a stateful object that accumulates a set of updates
 * to a task and then commits all the updates when either the update() or
 * transition() method is called.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class TaskUpdater
{
    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(TaskUpdater.class);

    private final String taskId;
    private final WorkflowService workflowService;
    private final PackageManager packageMgr;

    private final Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    private final Map<QName, List<NodeRef>> add = new HashMap<QName, List<NodeRef>>();
    private final Map<QName, List<NodeRef>> remove = new HashMap<QName, List<NodeRef>>();

    public TaskUpdater(String taskId,
                WorkflowService workflowService,
                NodeService nodeService,
                BehaviourFilter behaviourFilter)
    {
        this.taskId = taskId;
        this.workflowService = workflowService;
        this.packageMgr = new PackageManager(workflowService, nodeService, behaviourFilter, LOGGER);
    }


    public void addProperty(QName name, Serializable value)
    {
        properties.put(name, value);
    }

    public void addAssociation(QName name, List<NodeRef> value)
    {
        add.put(name, value);
    }

    public void removeAssociation(QName name, List<NodeRef> value)
    {
        remove.put(name, value);
    }

    public boolean changeAssociation(QName name, String nodeRefs, boolean isAdd)
    {
        List<NodeRef> value = NodeRef.getNodeRefs(nodeRefs, LOGGER);
        if (value == null)
        {
            return false;
        }
        Map<QName, List<NodeRef>> map = getAssociationMap(isAdd);
        if (map != null)
        {
            map.put(name, value);
            return true;
        }
        return false;
    }

    /**
     * @param suffix
     * @return
     */
    private Map<QName, List<NodeRef>> getAssociationMap(boolean isAdd)
    {
        Map<QName, List<NodeRef>> map = null;
        if (isAdd)
        {
            map = add;
        }
        else
        {
            map = remove;
        }
        return map;
    }
    
    public void addPackageItems(List<NodeRef> items)
    {
        packageMgr.addItems(items);
    }
    
    public void removePackageItems(List<NodeRef> items)
    {
        packageMgr.removeItems(items);
    }

    public WorkflowTask transition()
    {
        return transition(null);
    }
    
    public WorkflowTask transition(String transitionId)
    {
        update();
        return workflowService.endTask(taskId, transitionId);
    }
    
    public WorkflowTask update()
    {
        WorkflowTask task = workflowService.getTaskById(taskId);
        NodeRef packageNode = task.getPath().getInstance().getWorkflowPackage();
        packageMgr.update(packageNode);
        
        WorkflowTask result = workflowService.updateTask(taskId, properties, add, remove);
        properties.clear();
        add.clear();
        remove.clear();
        return result;
    }

}
