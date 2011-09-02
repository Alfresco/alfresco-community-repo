/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.workflow.jbpm;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.TaskComponent;
import org.alfresco.repo.workflow.WorkflowComponent;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowPackageComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.PropertyMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * JBPM Engine Tests
 * 
 * @author davidc
 */
public class JBPMEngineTest extends BaseAlfrescoSpringTest
{
    private static final String USER1 = "JbpmEngineTestJohn";
    private static final String USER2 = "JbpmEngineTestJane";
    private static final String USER3 = "JbpmEngineTestJoe";

    private WorkflowComponent workflowComponent;
    private TaskComponent taskComponent;
    private WorkflowPackageComponent packageComponent;
    private PersonService personService;
    private WorkflowDefinition testWorkflowDef;
    private NodeRef person1;
    private NodeRef person2;
    private NodeRef person3;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();

        personService = (PersonService) applicationContext.getBean("PersonService");
        person1 = createPerson(USER1);
        person2 = createPerson(USER2);
        person3 = createPerson(USER3);
        
        BPMEngineRegistry registry = (BPMEngineRegistry)applicationContext.getBean("bpm_engineRegistry");
        workflowComponent = registry.getWorkflowComponent(JBPMEngine.ENGINE_ID);
        taskComponent = registry.getTaskComponent(JBPMEngine.ENGINE_ID);
        packageComponent = (WorkflowPackageComponent)applicationContext.getBean("workflowPackageImpl"); 
        
        // deploy test process messages
        I18NUtil.registerResourceBundle("jbpmresources/test-messages");
        
        // deploy test process definition
        ClassPathResource processDef = new ClassPathResource("jbpmresources/test_processdefinition.xml");
        assertFalse(workflowComponent.isDefinitionDeployed(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML));
        WorkflowDeployment deployment = workflowComponent.deployDefinition(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML); 
        testWorkflowDef = deployment.definition; 
        assertNotNull(testWorkflowDef);
        assertEquals("jbpm$test", testWorkflowDef.name);
        assertEquals("1", testWorkflowDef.version);
        assertTrue(workflowComponent.isDefinitionDeployed(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML));
        
        authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
    }

    public void todoTestGetStartTask() throws Exception
    {
        //TODO Implement
    }
    
    public void testGetWorkflowDefinitions()
    {
        List<WorkflowDefinition> workflowDefs = workflowComponent.getDefinitions();
        assertNotNull(workflowDefs);
        assertTrue(workflowDefs.size() > 0);
    }
    
    
    public void testDeployWorkflow() throws Exception
    {
        ClassPathResource processDef = new ClassPathResource("jbpmresources/test_processdefinition.xml");
        WorkflowDeployment deployment = workflowComponent.deployDefinition(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML); 
        testWorkflowDef = deployment.getDefinition(); 
        assertNotNull(testWorkflowDef);
        assertEquals("jbpm$test", testWorkflowDef.getName());
        assertEquals("2", testWorkflowDef.getVersion());
    }
    
    
    public void testStartWorkflow()
    {
        try
        {
            workflowComponent.startWorkflow("norfolknchance", null);
            fail("Failed to catch invalid definition id");
        }
        catch(WorkflowException e)
        {
            // Do nothing.
        }

        // TODO: Determine why process definition is loaded, even though it doesn't exist
//        try
//        {
//            workflowComponent.startProcess("1000", null);
//            fail("Failed to catch workflow definition id that does not exist");
//        }
//        catch(WorkflowException e)
//        {
//        }

        WorkflowDefinition workflowDef = getTestDefinition();
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), null);
        assertNotNull(path);
        assertTrue(path.getId().endsWith("-@"));
        assertNotNull(path.getNode());
        assertNotNull(path.getInstance());
        assertEquals(workflowDef.getId(), path.getInstance().getDefinition().getId());
    }

    
    public void testGetWorkflowById()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), null);
        assertNotNull(path);
        assertTrue(path.getId().endsWith("-@"));
        assertNotNull(path.getNode());
        assertNotNull(path.getInstance());
        assertEquals(workflowDef.getId(), path.getInstance().getDefinition().getId());
        WorkflowInstance instance = workflowComponent.getWorkflowById(path.getInstance().getId());
        assertNotNull(instance);
        assertEquals(path.getInstance().getId(), instance.getId());
        
        workflowComponent.cancelWorkflow(instance.getId());
        WorkflowInstance result = workflowComponent.getWorkflowById(instance.getId());
        assertNull("The workflow isntance should be null!", result);
    }
    
    
    public void testStartWorkflowParameters()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.PROP_TASK_ID, 3);  // protected - shouldn't be written
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());  // task instance field
        params.put(WorkflowModel.PROP_PRIORITY, 1);  // task instance field
        params.put(WorkflowModel.PROP_PERCENT_COMPLETE, 10);  // context variable
        params.put(QName.createQName("", "Message"), "Hello World");  // context variable outside of task definition
        params.put(QName.createQName("", "Array"), new String[] { "one", "two" });  // context variable outside of task definition
        params.put(QName.createQName("", "NodeRef"), new NodeRef("workspace://1/1001"));  // context variable outside of task definition
        params.put(ContentModel.PROP_OWNER, AuthenticationUtil.getAdminUserName());  // task assignment
        
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.getId().endsWith("-@"));
        assertNotNull(path.getNode());
        assertNotNull(path.getInstance());
        assertEquals(workflowDef.getId(), path.getInstance().getDefinition().getId());
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());

        WorkflowTask task = tasks1.get(0);
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_TASK_ID));
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_DUE_DATE));
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_PRIORITY));
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_PERCENT_COMPLETE));
        assertTrue(task.getProperties().containsKey(ContentModel.PROP_OWNER));
        
        NodeRef initiator = path.getInstance().getInitiator();
        String initiatorUsername = (String)nodeService.getProperty(initiator, ContentModel.PROP_USERNAME);
        assertEquals(AuthenticationUtil.getAdminUserName(), initiatorUsername);
    }

    
    public void testUpdateTask()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.PROP_TASK_ID, 3);  // protected - shouldn't be written
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());  // task instance field
        params.put(WorkflowModel.PROP_PRIORITY, 1);  // task instance field
        params.put(WorkflowModel.PROP_PERCENT_COMPLETE, 10);  // context variable
        params.put(QName.createQName("", "Message"), "Hello World");  // context variable outside of task definition
        params.put(QName.createQName("", "Array"), new String[] { "one", "two" });  // context variable outside of task definition
        params.put(QName.createQName("", "NodeRef"), new NodeRef("workspace://1/1001"));  // context variable outside of task definition
        params.put(ContentModel.PROP_OWNER, AuthenticationUtil.getAdminUserName());  // task assignment
        
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.getId().endsWith("-@"));
        assertNotNull(path.getNode());
        assertNotNull(path.getInstance());
        assertEquals(workflowDef.getId(), path.getInstance().getDefinition().getId());
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());

        WorkflowTask task = tasks1.get(0);
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_TASK_ID));
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_DUE_DATE));
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_PRIORITY));
        assertTrue(task.getProperties().containsKey(WorkflowModel.PROP_PERCENT_COMPLETE));
        assertTrue(task.getProperties().containsKey(ContentModel.PROP_OWNER));

        // update with null parameters
        try
        {
            WorkflowTask taskU1 = taskComponent.updateTask(task.getId(), null, null, null);
            assertNotNull(taskU1);
        }
        catch(Throwable e)
        {
            fail("Task update failed with null parameters");
        }
        
        // update property value
        Map<QName, Serializable> updateProperties2 = new HashMap<QName, Serializable>();
        updateProperties2.put(WorkflowModel.PROP_PERCENT_COMPLETE, 100);
        WorkflowTask taskU2 = taskComponent.updateTask(task.getId(), updateProperties2, null, null);
        assertEquals(100, taskU2.getProperties().get(WorkflowModel.PROP_PERCENT_COMPLETE));

        // add to assocation
        QName assocName = QName.createQName("", "TestAssoc");
        List<NodeRef> toAdd = new ArrayList<NodeRef>();
        toAdd.add(new NodeRef("workspace://1/1001"));
        toAdd.add(new NodeRef("workspace://1/1002"));
        toAdd.add(new NodeRef("workspace://1/1003"));
        Map<QName, List<NodeRef>> addAssocs = new HashMap<QName, List<NodeRef>>();
        addAssocs.put(assocName, toAdd);
        WorkflowTask taskU3 = taskComponent.updateTask(task.getId(), null, addAssocs, null);
        assertNotNull(taskU3.getProperties().get(assocName));
        assertEquals(3, ((List<?>)taskU3.getProperties().get(assocName)).size());
        
        // add to assocation again
        List<NodeRef> toAddAgain = new ArrayList<NodeRef>();
        toAddAgain.add(new NodeRef("workspace://1/1004"));
        toAddAgain.add(new NodeRef("workspace://1/1005"));
        Map<QName, List<NodeRef>> addAssocsAgain = new HashMap<QName, List<NodeRef>>();
        addAssocsAgain.put(assocName, toAddAgain);
        WorkflowTask taskU4 = taskComponent.updateTask(task.getId(), null, addAssocsAgain, null);
        assertNotNull(taskU4.getProperties().get(assocName));
        assertEquals(5, ((List<?>)taskU4.getProperties().get(assocName)).size());
        
        // remove assocation
        List<NodeRef> toRemove = new ArrayList<NodeRef>();
        toRemove.add(new NodeRef("workspace://1/1002"));
        toRemove.add(new NodeRef("workspace://1/1003"));
        Map<QName, List<NodeRef>> removeAssocs = new HashMap<QName, List<NodeRef>>();
        removeAssocs.put(assocName, toRemove);
        WorkflowTask taskU5 = taskComponent.updateTask(task.getId(), null, null, removeAssocs);
        assertNotNull(taskU5.getProperties().get(assocName));
        assertEquals(3, ((List<?>)taskU5.getProperties().get(assocName)).size());
    }
    
    
    public void testGetWorkflowInstances()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        workflowComponent.startWorkflow(workflowDef.getId(), null);
        workflowComponent.startWorkflow(workflowDef.getId(), null);
        List<WorkflowInstance> instances = workflowComponent.getActiveWorkflows(workflowDef.getId());
        assertNotNull(instances);
        assertEquals(2, instances.size());
        for (WorkflowInstance instance : instances)
        {
            assertEquals(workflowDef.getId(), instance.getDefinition().getId());
        }
    }
    
    
    public void testGetPositions()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        workflowComponent.startWorkflow(workflowDef.getId(), null);
        List<WorkflowInstance> instances = workflowComponent.getActiveWorkflows(workflowDef.getId());
        assertNotNull(instances);
        assertEquals(1, instances.size());
        List<WorkflowPath> paths = workflowComponent.getWorkflowPaths(instances.get(0).getId());
        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertEquals(instances.get(0).getId(), paths.get(0).getInstance().getId());
        assertTrue(paths.get(0).getId().endsWith("-@"));
    }

    
    public void testCancelWorkflowInstance() throws Exception
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        workflowComponent.startWorkflow(workflowDef.getId(), null);
        List<WorkflowInstance> instances1 = workflowComponent.getActiveWorkflows(workflowDef.getId());
        assertNotNull(instances1);
        assertEquals(1, instances1.size());
        List<WorkflowTask> tasks = taskComponent.getAssignedTasks(AuthenticationUtil.getAdminUserName(), WorkflowTaskState.IN_PROGRESS);
        assertNotNull(tasks);
        assertTrue(tasks.size() > 0);
        WorkflowInstance cancelledInstance = workflowComponent.cancelWorkflow(instances1.get(0).getId());
        assertNotNull(cancelledInstance);
        assertFalse(cancelledInstance.isActive());
        List<WorkflowInstance> instances2 = workflowComponent.getActiveWorkflows(workflowDef.getId());
        assertNotNull(instances2);
        assertEquals(0, instances2.size());
        List<WorkflowTask> tasks1 = taskComponent.getAssignedTasks(AuthenticationUtil.getAdminUserName(), WorkflowTaskState.IN_PROGRESS);
        assertNotNull(tasks1);
        tasks1 = filterTasksByWorkflowInstance(tasks1, cancelledInstance.getId());
        assertEquals(0, tasks1.size());
    }
    

    /**
     * See Alf-2764 in Jira.
     * @throws Exception
     */
    public void testCancelForEachFork() throws Exception
    {
        // Deploy Parallel Loop Review process definition.
        ClassPathResource processDef = new ClassPathResource("test/alfresco/parallel_loop_review_processdefinition.xml");
        WorkflowDeployment deployment = workflowComponent.deployDefinition(processDef.getInputStream(),
                    MimetypeMap.MIMETYPE_XML);
        WorkflowDefinition parallelDef = deployment.getDefinition();
        assertNotNull(parallelDef);
        
        // Set Current User to USER1.
        AuthenticationUtil.setFullyAuthenticatedUser(USER1);
        
        // Set up parameters
        QName approvePercentName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "requiredApprovePercent");
        NodeRef pckgNode = packageComponent.createPackage(null);
        List<NodeRef> assignees = Arrays.asList(person1, person2, person3);
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable) assignees);
        parameters.put(WorkflowModel.ASSOC_PACKAGE, pckgNode);
        parameters.put(approvePercentName, 60f );

        // Start workflow
        WorkflowPath path = workflowComponent.startWorkflow(parallelDef.getId(), parameters);
        WorkflowTask startTask = workflowComponent.getTasksForWorkflowPath(path.getId()).get(0);
        taskComponent.endTask(startTask.getId(), null);
        checkInstanceExists(path.getInstance().getId(), parallelDef.getId(), true);
        
        // Set all users to reject document.
        ParallelReject(USER1);
        ParallelReject(USER2);
        ParallelReject(USER3);
        
        // Send review back round the loop.
        List<WorkflowTask> tasks = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        taskComponent.endTask(tasks.get(0).getId(), "again");
        
        // Try to cancel workflow
        WorkflowInstance cancelledWf = workflowComponent.cancelWorkflow(path.getInstance().getId());
        checkInstanceExists(cancelledWf.getId(), parallelDef.getId(), false);
    }

    private void checkInstanceExists(String instanceId, String defId, boolean expected)
    {
        boolean match=false;
        List<WorkflowInstance> activeWfs = workflowComponent.getActiveWorkflows(defId);
        for (WorkflowInstance instance : activeWfs)
        {
            if(instance.getId().equals(instanceId))
            {
                match = true;
                break;
            }
        }
        assertEquals( expected, match);
    }

    private void ParallelReject(String user)
    {
        List<WorkflowTask> tasks = taskComponent.getAssignedTasks(user, WorkflowTaskState.IN_PROGRESS);
        assertEquals(1, tasks.size());
        WorkflowTask task = tasks.get(0);
        taskComponent.endTask(task.getId(), "reject");
    }
    
    public void testSignal()
    {
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "testNode"), rootNodeRef);
        WorkflowDefinition workflowDef = getTestDefinition();
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), parameters);
        assertNotNull(path);
        WorkflowPath updatedPath = workflowComponent.signal(path.getId(), path.getNode().getTransitions()[1].getId());
        assertNotNull(updatedPath);
    }
    

    public void testGetAssignedTasks()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "reviewer"), AuthenticationUtil.getAdminUserName());
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "testNode"), rootNodeRef);
        parameters.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "package"), packageComponent.createPackage(null));
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), parameters);
        assertNotNull(path);
        List<WorkflowTask> tasks = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        WorkflowTask updatedTask = taskComponent.endTask(tasks.get(0).getId(), path.getNode().getTransitions()[0].getId());
        assertNotNull(updatedTask);
        List<WorkflowTask> completedTasks = taskComponent.getAssignedTasks(AuthenticationUtil.getAdminUserName(), WorkflowTaskState.COMPLETED);
        assertNotNull(completedTasks);
        completedTasks = filterTasksByWorkflowInstance(completedTasks, path.getInstance().getId());
        assertEquals(1, completedTasks.size());
        List<WorkflowTask> assignedTasks = taskComponent.getAssignedTasks(AuthenticationUtil.getAdminUserName(), WorkflowTaskState.IN_PROGRESS);
        assertNotNull(assignedTasks);
        assignedTasks = filterTasksByWorkflowInstance(assignedTasks, path.getInstance().getId());
        assertEquals(1, assignedTasks.size());
        assertEquals("review", assignedTasks.get(0).getName());
    }

    
    public void xtestMultiAssign()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        List<String> bpm_assignees = new ArrayList<String>();
        bpm_assignees.add(AuthenticationUtil.getAdminUserName());
        bpm_assignees.add("bob");
        bpm_assignees.add("fred");
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "assignees"), (Serializable)bpm_assignees);
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "testNode"), rootNodeRef);
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), parameters);
        assertNotNull(path);
        List<WorkflowTask> tasks = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        WorkflowTask updatedTask = taskComponent.endTask(tasks.get(0).getId(), "multi");
        assertNotNull(updatedTask);
    }

    
    public void testEndTask()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "reviewer"), AuthenticationUtil.getAdminUserName());
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "testNode"), rootNodeRef);
        parameters.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "package"), packageComponent.createPackage(null));
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), parameters);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());
        assertEquals(WorkflowTaskState.IN_PROGRESS, tasks1.get(0).getState());
        WorkflowTask updatedTask = taskComponent.endTask(tasks1.get(0).getId(), null);
        assertNotNull(updatedTask);
        assertEquals(WorkflowTaskState.COMPLETED, updatedTask.getState());
        List<WorkflowTask> completedTasks = taskComponent.getAssignedTasks(AuthenticationUtil.getAdminUserName(), WorkflowTaskState.COMPLETED);
        assertNotNull(completedTasks);
        completedTasks = filterTasksByWorkflowInstance(completedTasks, path.getInstance().getId());
        assertEquals(1, completedTasks.size());
        assertEquals(WorkflowTaskState.COMPLETED, completedTasks.get(0).getState());
    }
    
    
    public void testGetTask()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "reviewer"), AuthenticationUtil.getAdminUserName());
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "testNode"), rootNodeRef);
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), parameters);
        assertNotNull(path);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());
        WorkflowTask getTask = taskComponent.getTaskById(tasks1.get(0).getId());
        assertNotNull(getTask);
        assertEquals(getTask.getId(), tasks1.get(0).getId());
    }

    
    public void testNodeRef()
    {
        WorkflowDefinition workflowDef = getTestDefinition();
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "reviewer"), AuthenticationUtil.getAdminUserName());
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "testNode"), rootNodeRef);
        parameters.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "package"), packageComponent.createPackage(null));
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), parameters);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());
        assertEquals(WorkflowTaskState.IN_PROGRESS, tasks1.get(0).getState());
        WorkflowTask updatedTask = taskComponent.endTask(tasks1.get(0).getId(), null);
        assertNotNull(updatedTask);
    }        

    
    public void testScript() throws IOException
    {
        // deploy test script definition
        ClassPathResource processDef = new ClassPathResource("jbpmresources/test_script.xml");
        assertFalse(workflowComponent.isDefinitionDeployed(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML));
        WorkflowDeployment deployment = workflowComponent.deployDefinition(processDef.getInputStream(), MimetypeMap.MIMETYPE_XML); 
        assertNotNull(deployment);
        
        WorkflowDefinition workflowDef = deployment.getDefinition();
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(QName.createQName(NamespaceService.DEFAULT_URI, "testNode"), rootNodeRef);
        parameters.put(QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "package"), packageComponent.createPackage(null));
        WorkflowPath path = workflowComponent.startWorkflow(workflowDef.getId(), parameters);
        assertNotNull(path);
        List<WorkflowTask> tasks1 = workflowComponent.getTasksForWorkflowPath(path.getId());
        assertNotNull(tasks1);
        assertEquals(1, tasks1.size());
        assertEquals(WorkflowTaskState.IN_PROGRESS, tasks1.get(0).getState());
        WorkflowTask updatedTask = taskComponent.endTask(tasks1.get(0).getId(), null);
        assertNotNull(updatedTask);
    }        
    
//    public void testAssignTaskVariablesWithScript() throws Exception
//    {
//        WorkflowDefinition definition = workflowComponent.getDefinitionByName("jbpm$testwf:testTaskVarScriptAssign");
//        assertNotNull(definition);
//        
//        String testwfUrl = "http://www.alfresco.org/model/workflow/test/1.0";
//        QName simpleTextName = QName.createQName(testwfUrl, "simpleText");
//        QName listConstrainedName = QName.createQName(testwfUrl, "listConstrainedText");
//        
//        String simpleTextValue = "Foo";
//        String listConstrainedValue = "Second";
//        
//        
//        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
//        params.put(simpleTextName, simpleTextValue);
//        params.put(listConstrainedName, listConstrainedValue);
//        params.put(WorkflowModel.ASSOC_PACKAGE, packageComponent.createPackage(null));
//        
//        WorkflowPath path = workflowComponent.startWorkflow(definition.getId(), params);
//        // End start task.
//        List<WorkflowTask> tasks = workflowComponent.getTasksForWorkflowPath(path.getId());
//        
//        // Get Start Task
//        assertEquals(1, tasks.size());
//        WorkflowTask startTask = tasks.get(0);
//        QName startTaskName = definition.getStartTaskDefinition().getMetadata().getName();
//        assertEquals("This is not the start task!", startTaskName, startTask.getDefinition().getMetadata().getName());
//
//        taskComponent.endTask(startTask.getId(), null);
//        
//        tasks = workflowComponent.getTasksForWorkflowPath(path.getId());
//        
//        // Get Task
//        assertEquals(1, tasks.size());
//        WorkflowTask task = tasks.get(0);
//        QName taskName = QName.createQName(testwfUrl, "assignVarTask");
//        assertEquals("This is not the start task!", taskName, task.getDefinition().getMetadata().getName());
//        
//        Map<QName, Serializable> props = task.getProperties();
//        assertEquals("Simple Text property value doesn't match!", simpleTextValue, props.get(simpleTextName));
//        assertEquals("List Constrained property value doesn't match!", listConstrainedValue, props.get(listConstrainedName));
//    }
    
    /**
     * Locate the Test Workflow Definition
     * 
     * @return  workflow definition
     */
    private WorkflowDefinition getTestDefinition()
    {
        return testWorkflowDef;
    }
    
    
    private NodeRef createPerson(String userName)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
        }

        // if person node with given user name doesn't already exist then create
        // person
        if (this.personService.personExists(userName) == false)
        {
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);

            // create person node for user
            return personService.createPerson(personProps);
        }
        return personService.getPerson(userName);
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
            if (task.getPath().getInstance().getId().equals(workflowInstanceId))
            {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }
    
}
