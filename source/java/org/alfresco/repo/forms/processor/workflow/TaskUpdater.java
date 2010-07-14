/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A utility class for updating workflow tasks. This is a stateful object that
 * accumulates a set of updates to a task and then commits all the updates when
 * the update() method is called.
 * 
 * @author Nick Smith
 */
public class TaskUpdater
{
    /** Logger */
    private static final Log LOGGER = LogFactory.getLog(TaskUpdater.class);

    private final String taskId;
    private final WorkflowService workflowService;
    private Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
    private Map<QName, List<NodeRef>> add = new HashMap<QName, List<NodeRef>>();
    private Map<QName, List<NodeRef>> remove = new HashMap<QName, List<NodeRef>>();

    public TaskUpdater(String taskId, WorkflowService workflowService)
    {
        this.taskId = taskId;
        this.workflowService = workflowService;
    }

    public WorkflowTask update()
    {
        WorkflowTask result = workflowService.updateTask(taskId, properties, add, remove);
        properties = new HashMap<QName, Serializable>();
        add = new HashMap<QName, List<NodeRef>>();
        remove = new HashMap<QName, List<NodeRef>>();
        return result;
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
        List<NodeRef> value = getNodeRefs(nodeRefs);
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

    private List<NodeRef> getNodeRefs(Object value)
    {
        String[] nodeRefIds = ((String) value).split(",");
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>(nodeRefIds.length);
        for (String nodeRefString : nodeRefIds)
        {
            String nodeRefId = nodeRefString.trim();
            if (NodeRef.isNodeRef(nodeRefId))
            {
                NodeRef nodeRef = new NodeRef(nodeRefId);
                nodeRefs.add(nodeRef);
            }
            else
            {
                logNodeRefError(nodeRefId);
            }
        }
        return nodeRefs;
    }

    private void logNodeRefError(String nodeRefId)
    {
        if (LOGGER.isWarnEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Target Node: ").append(nodeRefId);
            msg.append(" is not a valid NodeRef and has been ignored.");
            LOGGER.warn(msg.toString());
        }
    }
}
