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
package org.alfresco.repo.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;


/**
 * Workflow Service Implementation Tests
 * 
 * @author davidc
 */
public class WorkflowServiceImplTest extends BaseSpringTest
{
    WorkflowService workflowService;
    NodeService nodeService;

    //@Override
    protected void onSetUpInTransaction() throws Exception
    {
        workflowService = (WorkflowService)applicationContext.getBean(ServiceRegistry.WORKFLOW_SERVICE.getLocalName());
        nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
                
        // authenticate
        AuthenticationComponent auth = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        auth.setSystemUserAsCurrentUser();
    }

    public void testGetWorkflowDefinitions()
    {
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
    }
    
    public void testStartWorkflow()
    {
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
        WorkflowDefinition workflowDef = workflowDefs.get(0);
        WorkflowPath path = workflowService.startWorkflow(workflowDef.id, null);
        assertNotNull(path);
        assertTrue(path.active);
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.id, path.instance.definition.id);
    }
    
    public void testWorkflowPackage()
    {
        NodeRef nodeRef = workflowService.createPackage(null);
        assertNotNull(nodeRef);
        assertTrue(nodeService.hasAspect(nodeRef, WorkflowModel.ASPECT_WORKFLOW_PACKAGE));
    }
    
    public void testQueryTasks()
    {
        WorkflowTaskQuery filter = new WorkflowTaskQuery();
        filter.setTaskName(QName.createQName("{http://www.alfresco.org/model/wcmworkflow/1.0}submitpendingTask"));
        filter.setTaskState(WorkflowTaskState.COMPLETED);
        Map<QName, Object> taskProps = new HashMap<QName, Object>();
        taskProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}workflowDescription"), "Test5");
        filter.setTaskCustomProps(taskProps);
        filter.setProcessId("jbpm$48");
        filter.setProcessName(QName.createQName("{http://www.alfresco.org/model/wcmworkflow/1.0}submit"));
        Map<QName, Object> procProps = new HashMap<QName, Object>();
        procProps.put(QName.createQName("{http://www.alfresco.org/model/bpm/1.0}workflowDescription"), "Test5");
        procProps.put(QName.createQName("companyhome"), new NodeRef("workspace://SpacesStore/3df8a9d0-ff04-11db-98da-a3c3f3149ea5"));
        filter.setProcessCustomProps(procProps);
        filter.setOrderBy(new WorkflowTaskQuery.OrderBy[] { WorkflowTaskQuery.OrderBy.TaskName_Asc, WorkflowTaskQuery.OrderBy.TaskState_Asc });
        List<WorkflowTask> tasks = workflowService.queryTasks(filter);
        System.out.println("Found " + tasks.size() + " tasks.");
        for (WorkflowTask task : tasks)
        {
            System.out.println(task.toString());
        }
    }
    
    public void testAssociateWorkflowPackage()
    {
        // create workflow package
        NodeRef rootRef = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "spacesStore"));
        NodeRef nodeRef = workflowService.createPackage(null);
        assertNotNull(nodeRef);
        assertTrue(nodeService.hasAspect(nodeRef, WorkflowModel.ASPECT_WORKFLOW_PACKAGE));
        ChildAssociationRef childAssoc = nodeService.createNode(rootRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test"), ContentModel.TYPE_CONTENT, null);
        nodeService.addChild(nodeRef, childAssoc.getChildRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test"));
        
        // start workflow
        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
        WorkflowDefinition workflowDef = workflowDefs.get(0);
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(WorkflowModel.ASSOC_PACKAGE, nodeRef);
        WorkflowPath path = workflowService.startWorkflow(workflowDef.id, parameters);
        assertNotNull(path);
        assertTrue(path.active);
        assertNotNull(path.node);
        assertNotNull(path.instance);
        assertEquals(workflowDef.id, path.instance.definition.id);
        String workflowDefId = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_ID);
        assertEquals(workflowDefId, workflowDef.id);
        String workflowDefName = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME);
        assertEquals(workflowDefName, workflowDef.name);
        String workflowInstanceId = (String)nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
        assertEquals(workflowInstanceId, path.instance.id);

        // get workflows for content
        List<WorkflowInstance> instances = workflowService.getWorkflowsForContent(childAssoc.getChildRef(), true);
        assertNotNull(instances);
        assertEquals(1, instances.size());
        assertEquals(instances.get(0).id, path.instance.id);
        List<WorkflowInstance> completedInstances = workflowService.getWorkflowsForContent(childAssoc.getChildRef(), false);
        assertNotNull(completedInstances);
        assertEquals(0, completedInstances.size());
    }
    
}
