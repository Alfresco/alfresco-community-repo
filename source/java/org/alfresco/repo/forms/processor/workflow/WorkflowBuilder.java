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

package org.alfresco.repo.forms.processor.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A helper class used to start workflows. The builder accumuates all the
 * changes to be made to the list of parameters and package items, then starts a
 * new workflow once the build() method is called.
 * 
 * @author Nick Smith
 */
public class WorkflowBuilder
{
    private final WorkflowService workflowService;
    private final WorkflowDefinition definition;
    private final NodeService nodeService;

    private final Map<QName, Serializable> params = new HashMap<QName, Serializable>();
    private final Set<NodeRef> packageItems = new HashSet<NodeRef>();
    private NodeRef packageNode = null;
    
    
    public WorkflowBuilder(WorkflowDefinition definition, WorkflowService workflowService, NodeService nodeService)
    {
        this.workflowService = workflowService;
        this.nodeService = nodeService;
        this.definition = definition;
    }
    
    public void addParameter(QName name, Serializable value)
    {
        params.put(name, value);
    }
    
    public void addAssociationParameter(QName name, List<NodeRef> values)
    {
        if(values instanceof Serializable)
        {
            params.put(name, (Serializable) values);
        }
    }
    
    public void addPackageItems(List<NodeRef> items)
    {
        packageItems.addAll(items);
    }

    /**
     * Takes a comma-separated list of {@link NodeRef} ids and adds the
     * specified NodeRefs to the package.
     * 
     * @param items
     */
    public void addPackageItems(String items)
    {
        List<NodeRef> nodes = NodeRef.getNodeRefs(items);
        addPackageItems(nodes);
    }
    
    public void addPackageItemsAsStrings(List<String> itemStrs)
    {
        for (String itemStr : itemStrs)
        {
            addPackageItem(itemStr);
        }
    }

    public void addPackageItem(NodeRef item)
    {
        packageItems.add(item);
    }
    
    public void addPackageItem(String itemStr)
    {
        packageItems.add(new NodeRef(itemStr));
    }
    
    /**
     * @param packageNode the packageNode to set
     */
    public void setPackageNode(NodeRef packageNode)
    {
        this.packageNode = packageNode;
    }
    
    public WorkflowInstance build()
    {
        buildPackage();
        WorkflowPath path = workflowService.startWorkflow(definition.id, params);
        signalStartTask(path);
        return path.instance;
    }

    private void signalStartTask(WorkflowPath path)
    {
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.id);
        if(tasks.size() == 1)
        {
            WorkflowTask startTask = tasks.get(0);
            workflowService.endTask(startTask.id, null);
        }
        else
            throw new WorkflowException("Start task not found! Expected 1 task but found: " + tasks.size());
    }

    private void buildPackage()
    {
        final NodeRef packageRef = workflowService.createPackage(packageNode);
        final String url = NamespaceService.CONTENT_MODEL_1_0_URI;
        final QName packageContains = WorkflowModel.ASSOC_PACKAGE_CONTAINS;
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                for (NodeRef item : packageItems)
                {
                    String name = 
                        (String) nodeService.getProperty(item, ContentModel.PROP_NAME);
                    String localName = QName.createValidLocalName(name);
                    QName qName = QName.createQName(url, localName);
                    nodeService.addChild(packageRef, item, packageContains, qName);
                }
                return null;
            }
            
        }, AuthenticationUtil.getSystemUserName());
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
    }
}
