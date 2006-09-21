/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.TaskComponent;
import org.alfresco.repo.workflow.WorkflowComponent;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.springframework.core.io.ClassPathResource;


/**
 * JBPM Engine Tests
 * 
 * @author davidc
 */
public class NodeListConverterTest extends BaseSpringTest
{
    AuthenticationComponent authenticationComponent;
    PersonService personService;
    WorkflowComponent workflowComponent;
    TaskComponent taskComponent;
    WorkflowDefinition testWorkflowDef;
    NodeRef testNodeRef;

    private static String taskId = null;
    
        
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        personService = (PersonService)applicationContext.getBean("personService");
        BPMEngineRegistry registry = (BPMEngineRegistry)applicationContext.getBean("bpm_engineRegistry");
        workflowComponent = registry.getWorkflowComponent("jbpm");
        taskComponent = registry.getTaskComponent("jbpm");
        
        // deploy latest review and approve process definition
        ClassPathResource processDef = new ClassPathResource("alfresco/workflow/review_processdefinition.xml");
        WorkflowDeployment deployment = workflowComponent.deployDefinition(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML); 
        testWorkflowDef = deployment.definition; 
        assertNotNull(testWorkflowDef);

        // run as system
        authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();
        
        // get valid node ref
        NodeService nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        testNodeRef = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "spacesStore"));
    }

    
    public void testStep1Start()
    {
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_PACKAGE, testNodeRef);
        Date reviewDueDate = new Date();
        params.put(QName.createQName("http://www.alfresco.org/model/workflow/1.0", "reviewDueDate"), reviewDueDate);
        NodeRef reviewer = personService.getPerson("admin");
        params.put(QName.createQName("http://www.alfresco.org/model/workflow/1.0", "reviewer"), reviewer);
        
        WorkflowPath path = workflowComponent.startWorkflow(testWorkflowDef.id, params);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.id);
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());
        
        setComplete();
        taskId = tasks1.get(0).id;
    }

    
    public void testSetNodeRefList()
    {
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
        nodeRefs.add(testNodeRef);
        nodeRefs.add(testNodeRef);
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.PROP_COMPLETED_ITEMS, (Serializable)nodeRefs);
        
        WorkflowTask task = taskComponent.getTaskById(taskId);
        assertNull(task.properties.get(WorkflowModel.PROP_COMPLETED_ITEMS));
        
        WorkflowTask updatedTask = taskComponent.updateTask(taskId, params, null, null);
        assertNotNull(updatedTask);
        assertTrue(updatedTask.properties.containsKey(WorkflowModel.PROP_COMPLETED_ITEMS));
        assertEquals(2, ((List)updatedTask.properties.get(WorkflowModel.PROP_COMPLETED_ITEMS)).size());
        
        setComplete();
    }

    
    public void testUpdateNodeRefList()
    {
        List<NodeRef> nodeRefs = new ArrayList<NodeRef>();
//        nodeRefs.add(testNodeRef);
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.PROP_COMPLETED_ITEMS, (Serializable)nodeRefs);
        
//        WorkflowTask task = taskComponent.getTaskById(taskId);
//        assertNotNull(task);
//        assertTrue(task.properties.containsKey(WorkflowModel.PROP_COMPLETED_ITEMS));
//        assertEquals(2, ((List)task.properties.get(WorkflowModel.PROP_COMPLETED_ITEMS)).size());
        
        WorkflowTask updatedTask = taskComponent.updateTask(taskId, params, null, null);
        assertNotNull(updatedTask);
        assertTrue(updatedTask.properties.containsKey(WorkflowModel.PROP_COMPLETED_ITEMS));
        assertEquals(0, ((List)updatedTask.properties.get(WorkflowModel.PROP_COMPLETED_ITEMS)).size());
        
        setComplete();
    }
    
}
