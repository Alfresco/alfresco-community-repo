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
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.springframework.core.io.ClassPathResource;


/**
 * Review and Approve workflow specific Tests
 * 
 * @author davidc
 */
public class ReviewAndApproveTest extends BaseSpringTest
{
    AuthenticationComponent authenticationComponent;
    PersonService personService;
    WorkflowComponent workflowComponent;
    TaskComponent taskComponent;
    WorkflowDefinition testWorkflowDef;
    NodeRef testNodeRef;
    

        
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        personService = (PersonService)applicationContext.getBean("personService");
        BPMEngineRegistry registry = (BPMEngineRegistry)applicationContext.getBean("bpm_engineRegistry");
        workflowComponent = registry.getWorkflowComponent("jbpm");
        taskComponent = registry.getTaskComponent("jbpm");
        
        // deploy latest review and approve process definition
        ClassPathResource processDef = new ClassPathResource("org/alfresco/repo/workflow/jbpm/review_and_approve_processdefinition.xml");
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

    @Override
    protected void onTearDownInTransaction()
    {
        authenticationComponent.clearCurrentSecurityContext();
    }

    
    public void testSubmitForReview()
    {
        WorkflowDefinition workflowDef = testWorkflowDef;
        
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_PACKAGE, testNodeRef);
        Date reviewDueDate = new Date();
        params.put(QName.createQName("http://www.alfresco.org/model/workflow/1.0", "reviewDueDate"), reviewDueDate);
        NodeRef reviewer = personService.getPerson("admin");
        params.put(QName.createQName("http://www.alfresco.org/model/workflow/1.0", "reviewer"), reviewer);
        
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.id, params);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.id);
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());

        WorkflowTask task = tasks1.get(0);
        assertTrue(task.properties.containsKey(WorkflowModel.ASSOC_PACKAGE));
        taskComponent.endTask(task.id, null);
        
        List<WorkflowTask> assignedTasks = taskComponent.getAssignedTasks("admin", WorkflowTaskState.IN_PROGRESS);
        assertNotNull(assignedTasks);
        assignedTasks = filterTasksByWorkflowInstance(assignedTasks, path.instance.id);
        
        assertEquals(testNodeRef, assignedTasks.get(0).properties.get(WorkflowModel.ASSOC_PACKAGE));
        assertEquals(reviewDueDate, assignedTasks.get(0).properties.get(WorkflowModel.PROP_DUE_DATE));
    }

    /**
     * Filter task list by workflow instance
     * 
     * @param tasks
     * @param processInstanceId
     * @return
     */
    private List<WorkflowTask> filterTasksByWorkflowInstance(List<WorkflowTask> tasks, String workflowInstanceId)
    {
        List<WorkflowTask> filteredTasks = new ArrayList<WorkflowTask>();
        for (WorkflowTask task : tasks)
        {
            if (task.path.instance.id.equals(workflowInstanceId))
            {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }
    
}
