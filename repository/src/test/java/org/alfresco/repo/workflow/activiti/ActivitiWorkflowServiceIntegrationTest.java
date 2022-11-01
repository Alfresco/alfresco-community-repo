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

package org.alfresco.repo.workflow.activiti;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricProcessInstance;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.RepositoryLocation;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.workflow.AbstractWorkflowServiceIntegrationTest;
import org.alfresco.repo.workflow.WorkflowBuilder;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.testing.category.LuceneTests;
import org.alfresco.util.testing.category.RedundantTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
@Category(LuceneTests.class)
public class ActivitiWorkflowServiceIntegrationTest extends AbstractWorkflowServiceIntegrationTest
{
    private final static String USER_RECREATED = "WFUserRecreated" + GUID.generate();
    public static final String ACTIVITI_TEST_TRANSACTION_BPMN20_XML = "activiti/testTransaction.bpmn20.xml";
    public static final String ALFRESCO_WORKFLOW_ADHOC_BPMN20_XML = "alfresco/workflow/adhoc.bpmn20.xml";
    public static final String ALFRESCO_WORKFLOW_REVIEW_POOLED_BPMN20_XML = "alfresco/workflow/review-pooled.bpmn20.xml";
    public static final String ALFRESCO_WORKFLOW_PARALLEL_REVIEW_BPMN20_XML = "alfresco/workflow/parallel-review.bpmn20.xml";
    public static final String ACTIVITI_TEST_TIMER_BPMN20_XML = "activiti/testTimer.bpmn20.xml";
    public static final String ACTIVITI_TEST_WITH_SUB_PROCESS_XML = "activiti/testWorkflowWithSubprocess.xml";

    @Test
    public void testOutcome() throws Exception
    {
        WorkflowDefinition definition = deployDefinition("alfresco/workflow/review.bpmn20.xml");
        
        personManager.setUser(USER1);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field

        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
        String instanceId = path.getInstance().getId();
        
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        workflowService.endTask(startTask.getId(), null);
        
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(instanceId);
        assertEquals(1, paths.size());
        path = paths.get(0);
        
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask reviewTask = tasks.get(0);
        
        // Set the transition property
        QName outcomePropName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "reviewOutcome");
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(outcomePropName, "Approve");
        workflowService.updateTask(reviewTask.getId(), props, null, null);

        // End task and check outcome property
        WorkflowTask result = workflowService.endTask(reviewTask.getId(), null);
        Serializable outcome = result.getProperties().get(WorkflowModel.PROP_OUTCOME);
        assertEquals("Approve", outcome);
    }

    @Test
    public void testStartTaskEndsAutomatically()
    {
        // Deploy the test workflow definition which uses the 
        // default Start Task type, so it should end automatically.
        WorkflowDefinition definition = deployDefinition(getTestDefinitionPath());
        
        // Start the Workflow
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), null);
        String instanceId = path.getInstance().getId();

        // Check the Start Task is completed.
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        assertEquals(WorkflowTaskState.COMPLETED, startTask.getState());
        
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        String taskName = tasks.get(0).getName();
        assertEquals("bpm_foo_task", taskName);
    }
    
    /**
     * Actually tests if the priority is the default value.  This is based on the assumption that custom
     * tasks are defaulted to a priority of 50 (which is invalid).  I'm testing that the code I wrote decides this is an
     * invalid number and sets it to the default value (2).
     */
    @Test
    public void testPriorityIsValid()
    {
        WorkflowDefinition definition = deployDefinition("activiti/testCustomActiviti.bpmn20.xml");
        
        personManager.setUser(USER1);
        
        // Start the Workflow
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), null);
        String instanceId = path.getInstance().getId();

        // Check the Start Task is completed.
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        assertEquals(WorkflowTaskState.COMPLETED, startTask.getState());
        
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        for (WorkflowTask workflowTask : tasks)
        {
            Map<QName, Serializable> props = workflowTask.getProperties();
            TypeDefinition typeDefinition = workflowTask.getDefinition().getMetadata();
            Map<QName, PropertyDefinition> propertyDefs = typeDefinition.getProperties();        
            PropertyDefinition priorDef =  propertyDefs.get(WorkflowModel.PROP_PRIORITY);
            assertEquals(props.get(WorkflowModel.PROP_PRIORITY),Integer.valueOf(priorDef.getDefaultValue()));        
        }
    }

    @Test
    public void testReviewAndPooledNotModifiedDate()
    {
        authenticationComponent.setSystemUserAsCurrentUser();
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, "MNT-11522-testfile.txt");
        final ChildAssociationRef childAssoc = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "MNT-11522-test"), ContentModel.TYPE_CONTENT, props);
        NodeRef addedNodeRef = childAssoc.getChildRef();
        Date lastModifiedDate = (Date)nodeService.getProperty(addedNodeRef, ContentModel.PROP_MODIFIED);        
        WorkflowDefinition definition = deployDefinition(getPooledReviewDefinitionPath());
        
        assertNotNull(definition);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        NodeRef workflowPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, workflowPackage);
        NodeRef group = groupManager.get(GROUP);
        assertNotNull(group);
        params.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, group);
        
        nodeService.addChild(workflowPackage, addedNodeRef, WorkflowModel.ASSOC_PACKAGE_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName((String) nodeService.getProperty(addedNodeRef, ContentModel.PROP_NAME))));
        
        WorkflowPath workflowPath = workflowService.startWorkflow(definition.getId(), params);
        assertNotNull(workflowPath);
        assertTrue(workflowPath.isActive());
        final String workflowInstanceId = workflowPath.getInstance().getId();

        List<WorkflowTask> pooledTasks = workflowService.getPooledTasks(GROUP);
        assertNotNull(pooledTasks);

        // End start task to progress workflow
        WorkflowTask startTask = workflowService.getStartTask(workflowInstanceId);
        String startTaskId = startTask.getId();
        workflowService.endTask(startTaskId, null);
        
        assertEquals(lastModifiedDate, nodeService.getProperty(addedNodeRef, ContentModel.PROP_MODIFIED));
    }
    
    @Test
    public void testGetWorkflowTaskDefinitionsWithMultiInstanceTask()
    {
    	// Test added to validate fix for ALF-14224
        WorkflowDefinition definition = deployDefinition(getParallelReviewDefinitionPath());
        String workflowDefId = definition.getId();
        List<WorkflowTaskDefinition> taskDefs = workflowService.getTaskDefinitions(workflowDefId);
        assertEquals(4, taskDefs.size());
        
        // The first task is the start-task, the second one is a multi-instance UserTask. This should have the right form-key
        WorkflowTaskDefinition taskDef = taskDefs.get(1);
        assertEquals("wf:activitiReviewTask", taskDef.getId());
    }

    // Added after MNT-17601. Failed to find any completed tasks when the workflow had a sub process.
    @Test
    public void testCompletedTaskInWorkflowWithSubProcess()
    {
        WorkflowDefinition definition = deployDefinition(ACTIVITI_TEST_WITH_SUB_PROCESS_XML);
        String workflowDefId = definition.getId();
        List<WorkflowTaskDefinition> taskDefs = workflowService.getTaskDefinitions(workflowDefId);
        assertEquals(2, taskDefs.size()); // Prior to the fix for MNT-17601 this list only contained "Alfresco start".
        assertEquals("Alfresco start", taskDefs.get(0).getNode().getTitle());
        assertEquals("Alfresco User Task",   taskDefs.get(1).getNode().getTitle());
    }

    @Test
    public void testAccessStartTaskAsAssigneeFromTaskPartOfProcess()
    {
        // Test added to validate fix for CLOUD-1929 - start-task can be accesses by assignee of a task
        // part of that process
        WorkflowDefinition definition = deployDefinition(getAdhocDefinitionPath());
        
        // Start process as USER1
        personManager.setUser(USER1);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        NodeRef wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field

        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
        String instanceId = path.getInstance().getId();

        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        workflowService.endTask(startTask.getId(), null);

        List<NodeRef> taskPackage = workflowService.getPackageContents(startTask.getId());
        assertTrue(taskPackage.isEmpty());
        List<NodeRef> packageContents = workflowService.getPackageContents(wfPackage);
        assertTrue(packageContents.isEmpty());

        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());

        // Assign task to user3
        workflowService.updateTask(tasks.get(0).getId(), Collections.singletonMap(WorkflowModel.ASSOC_ASSIGNEE,
                (Serializable) personManager.get(USER3)), null, null);
        
        // Authenticate as user3
        personManager.setUser(USER3);
        
        // When fetchin the start-task, no exception should be thrown
        startTask = workflowService.getStartTask(instanceId);
        assertNotNull(startTask);
        startTask = workflowService.getTaskById(startTask.getId());
        assertNotNull(startTask);
        
        // Accessing by user4 shouldn't be possible
        personManager.setUser(USER4);
        try
        {
            workflowService.getStartTask(instanceId);
            fail("AccessDeniedException expected");
        }
        catch(AccessDeniedException expected) 
        {
            // Expected excaption
        }
        
        try
        {
            workflowService.getTaskById(startTask.getId());
            fail("AccessDeniedException expected");
        }
        catch(AccessDeniedException expected) 
        {
            // Expected exception
        }
    }
    
    /**
     * Test to validate fix for ALF-19822
     */
    @Test
    public void testMultiInstanceListenersCalled() throws Exception
    {
        // start pooled review and approve workflow
        WorkflowDefinition workflowDef = deployDefinition(getParallelReviewDefinitionPath());
        assertNotNull(workflowDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "This is the description");
        NodeRef group = groupManager.get(GROUP);
        assertNotNull(group);
        
        List<NodeRef> assignees = Arrays.asList(personManager.get(USER2), personManager.get(USER3));
        params.put(WorkflowModel.ASSOC_ASSIGNEES, (Serializable) assignees);
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        String instnaceId = path.getInstance().getId();
        
        WorkflowTask startTask = workflowService.getStartTask(instnaceId);
        workflowService.endTask(startTask.getId(), null);
        
        personManager.setUser(USER2);
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(USER2, WorkflowTaskState.IN_PROGRESS);
        assertEquals(1, tasks.size());

        //Assert task description
        assertEquals("Documents for review and approval", tasks.get(0).getDescription());

        //Assert workflow link name
        assertEquals("This is the description", tasks.get(0).getProperties().get(WorkflowModel.PROP_DESCRIPTION));
    }
    
    /**
     * Test to validate fix for WOR-107
     */
    @Test
    public void testLongTextValues() throws Exception
    {
        String veryLongTextValue = getLongString(10000);
        // start pooled review and approve workflow
        WorkflowDefinition workflowDef = deployDefinition(getAdhocDefinitionPath());
        assertNotNull(workflowDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.PROP_COMMENT, veryLongTextValue);
        
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);

        // No exception should be thrown when using *very* long String variables (in this case, 10000)
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        
        WorkflowTask startTask = workflowService.getStartTask(path.getInstance().getId());
        assertNotNull(startTask);
        
        assertEquals(veryLongTextValue, startTask.getProperties().get(WorkflowModel.PROP_COMMENT));
    }
    
    /**
     * Test for MNT-11247
     */
    @Test
    public void testAssignmentListener()
    {
        WorkflowDefinition definition = deployDefinition(getAssignmentListenerDefinitionPath());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(ContentModel.PROP_OWNER, USER1);
        NodeRef assignee = personManager.get(USER1);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);

        // end start task
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        workflowService.endTask(tasks.get(0).getId(), null);

        // end user task 1
        tasks = workflowService.getTasksForWorkflowPath(path.getId());
        workflowService.updateTask(tasks.get(0).getId(), params, null, null);
        workflowService.endTask(tasks.get(0).getId(), null);

        WorkflowTask result = workflowService.getTaskById(tasks.get(0).getId());
        Map<QName, Serializable> props = result.getProperties();
        Double create1 = (Double) props.get(QName.createQName("http://www.alfresco.org/model/bpm/1.0", "create1"));
        assertEquals("Create listener was not triggered", new Double(1), create1);
        Double complete1 = (Double) props.get(QName.createQName("http://www.alfresco.org/model/bpm/1.0", "complete1"));
        assertEquals("Complete listener was not triggered", new Double(1), complete1);
        Double assignment1 = (Double) props.get(QName.createQName("http://www.alfresco.org/model/bpm/1.0", "assignment1"));
        assertEquals("Assign listener was not triggered", new Double(1), assignment1);
    }
    
    /**
     * Test for MNT-14366
     */
    @Test
    public void testWorkflowRecreatedUser()
    {
        WorkflowDefinition definition = deployDefinition("alfresco/workflow/review.bpmn20.xml");
        
        personManager.createPerson(USER_RECREATED);
        personManager.setUser(USER_RECREATED);
        
        //create an workfow as USER_RECREATED
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), params);
        String instanceId = path.getInstance().getId();
        
        //check if workflow owner property value is the same as initiator username
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        String owner = (String)startTask.getProperties().get(ContentModel.PROP_OWNER);
        assertEquals(owner, USER_RECREATED);

        //delete and recreate user
        personManager.deletePerson(USER_RECREATED);
        personManager.createPerson(USER_RECREATED);
        personManager.setUser(USER_RECREATED);

        //check workflow owner after user deletion and recreation
        startTask = workflowService.getStartTask(instanceId);
        owner = (String)startTask.getProperties().get(ContentModel.PROP_OWNER);
        //owner is now null as nodeRef pointed by initiator property no longer exists;
        //user has access to wokflow because owner value is extracted after fix from initiatorhome noderef
        assertNull(owner);        
        workflowService.endTask(startTask.getId(), null);
    }
    
    protected String getLongString(int numberOfCharacters) {
        StringBuffer stringBuffer = new StringBuffer();
        for(int i=0; i<numberOfCharacters/10;i++) {
            stringBuffer.append("ABCDEFGHIJ");
        }
        return stringBuffer.toString();
    }
    
    
    @Override
    protected void checkTaskQueryStartTaskCompleted(String workflowInstanceId, WorkflowTask startTask) 
    {
        // In activiti, start-tasks only show up when the taskId or workflowInstanceId is passed in.
        List<String> expectedTasks = Arrays.asList(startTask.getId());
        
        checkProcessIdQuery(workflowInstanceId, expectedTasks, WorkflowTaskState.COMPLETED);
        checkTaskIdQuery(startTask.getId(), WorkflowTaskState.COMPLETED);
        
        // Check additional filtering, when workflowInstanceId is passed
        QName startTaskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "submitAdhocTask");
        checkTaskNameQuery(startTaskName, expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
        checkActorIdQuery(USER1, expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
        checkIsActiveQuery(expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
        checkTaskPropsQuery(expectedTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
    }
    
    @Override
    protected void checkTaskQueryTaskCompleted(String workflowInstanceId, WorkflowTask theTask, WorkflowTask startTask) 
    {
        List<String> withoutStartTask = Arrays.asList(theTask.getId());
        List<String> bothTasks= Arrays.asList(theTask.getId(), startTask.getId());
        
        checkProcessIdQuery(workflowInstanceId, bothTasks, WorkflowTaskState.COMPLETED);
        
        // Adhoc task should only be returned
        QName taskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "adhocTask");
        checkTaskNameQuery(taskName, withoutStartTask, WorkflowTaskState.COMPLETED, null);

        // Completed adhocTask is assigned to USER2
        checkActorIdQuery(USER2, withoutStartTask, WorkflowTaskState.COMPLETED, null);
        
        checkIsActiveQuery(bothTasks, WorkflowTaskState.COMPLETED, workflowInstanceId);
       
        // Task has custom property set
        checkTaskPropsQuery(withoutStartTask, WorkflowTaskState.COMPLETED, null);
        
        // Process properties
        checkProcessPropsQuery(withoutStartTask, WorkflowTaskState.COMPLETED);
    }
    
    @Override
    protected void checkQueryTasksInactiveWorkflow(String workflowInstanceId) 
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setActive(false);
        taskQuery.setProcessId(workflowInstanceId);

        List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery);
        assertNotNull(tasks);
        
        assertEquals(3, tasks.size());
        
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        taskQuery.setActive(true);
        taskQuery.setProcessId(workflowInstanceId);
        checkNoTasksFoundUsingQuery(taskQuery);
    }

    @Category(RedundantTests.class)
    @Test
    public void testStartWorkflowFromTaskListener() throws Exception
    {
        WorkflowDefinition testDefinition = deployDefinition("activiti/testStartWfFromListener.bpmn20.xml");
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, "MNT-11926-testfile.txt");
        final ChildAssociationRef childAssoc = nodeService.createNode(companyHome, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "MNT-11926-test"), ContentModel.TYPE_CONTENT, props);

        try
        {
            // Create workflow parameters
            Map<QName, Serializable> params = new HashMap<QName, Serializable>();
            Serializable wfPackage = workflowService.createPackage(null);
            params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
            NodeRef assignee = personManager.get(USER1);
            params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee); // task instance field

            WorkflowPath path = workflowService.startWorkflow(testDefinition.getId(), params);
            String instanceId = path.getInstance().getId();

            WorkflowTask startTask = workflowService.getStartTask(instanceId);
            workflowService.endTask(startTask.getId(), null);
        }
        finally
        {
            // tidy up
            nodeService.deleteNode(childAssoc.getChildRef());
        }
    }

    @Test
    public void testWorkflowWithNodes() throws Exception
    {
        authenticationComponent.setSystemUserAsCurrentUser();

        FileInfo incorrectNode = serviceRegistry.getFileFolderService().create(companyHome, "NO_WORKFLOW" + GUID.generate() + ".xml", ContentModel.TYPE_CONTENT);
        FileInfo fileInfo = serviceRegistry.getFileFolderService().create(companyHome, "workflow" + GUID.generate() + ".xml", WorkflowModel.TYPE_WORKFLOW_DEF);
        NodeRef workflowNode = fileInfo.getNodeRef();
        nodeService.setProperty(workflowNode, WorkflowModel.PROP_WORKFLOW_DEF_ENGINE_ID, getEngine());
        InputStream input = getInputStream("activiti/testDiagram.bpmn20.xml");
        ContentWriter contentWriter = serviceRegistry.getContentService().getWriter(fileInfo.getNodeRef(), ContentModel.TYPE_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent(input);

        try
        {
            workflowService.isDefinitionDeployed(incorrectNode.getNodeRef());
            fail("The content type is incorrect, it should not get here");
        }
        catch (WorkflowException we)
        {
            assertTrue(we.getMessage().contains(" is not of type 'bpm:workflowDefinition'"));
        }

        boolean isDeployed = workflowService.isDefinitionDeployed(workflowNode);

        try
        {
            workflowService.deployDefinition(incorrectNode.getNodeRef());
            fail("The content type is incorrect, it should not get here");
        }
        catch (WorkflowException we)
        {
            assertTrue(we.getMessage().contains(" is not of type 'bpm:workflowDefinition'"));
        }

        assertFalse(workflowService.isDefinitionDeployed(workflowNode));
        WorkflowDeployment workflowDeployment =  workflowService.deployDefinition(workflowNode);
        assertNotNull(workflowDeployment);
        assertTrue(workflowService.isDefinitionDeployed(workflowNode));

        List<WorkflowDefinition> defs =  workflowService.getAllDefinitionsByName(workflowDeployment.getDefinition().getName());
        assertNotNull(defs);
        assertEquals(1, defs.size());

        workflowService.undeployDefinition(workflowDeployment.getDefinition().getId());
        assertFalse(workflowService.isDefinitionDeployed(workflowNode));

        try
        {
            workflowService.startWorkflowFromTemplate(incorrectNode.getNodeRef());
            fail("This method hasn't be implemented");
        }
        catch (UnsupportedOperationException we)
        {
        }

        boolean multi = workflowService.isMultiTenantWorkflowDeploymentEnabled();
        assertTrue(multi);
    }

    @Test
    public void testWorkflowVarious() throws Exception
    {
        WorkflowDefinition definition = deployDefinition(getTestDefinitionPath());

        // Start the Workflow
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), null);
        String instanceId = path.getInstance().getId();

        List<WorkflowTask> workflowTasks = workflowService.getStartTasks(Arrays.asList(instanceId), true);
        assertNotNull(workflowTasks);
        assertEquals(1, workflowTasks.size());

        assertFalse(workflowService.hasWorkflowImage(instanceId));
        assertNull(workflowService.getWorkflowImage(instanceId));

        List<WorkflowInstance> instances = workflowService.cancelWorkflows(null);
        assertTrue(instances.isEmpty());

        WorkflowInstance wfi = workflowService.deleteWorkflow(instanceId);
        assertEquals(instanceId, wfi.getId());

        try
        {
            byte[] image = workflowService.getDefinitionImage(definition.getId());
        }
        catch (WorkflowException we )
        {
            assertTrue(we.getMessage().contains("Failed to retrieve workflow definition"));
        }

    }

    @Test
    public void testWorkflowQueries() throws Exception
    {
        WorkflowDefinition definition = deployDefinition(getTestDefinitionPath());

        WorkflowInstanceQuery workflowInstanceQuery = new WorkflowInstanceQuery(true);
        long active = workflowService.countWorkflows(workflowInstanceQuery);
        assertNotNull(active);

        workflowInstanceQuery.setWorkflowDefinitionId(definition.getId());
        workflowInstanceQuery.setEngineId(getEngine());
        active = workflowService.countWorkflows(workflowInstanceQuery);
        assertNotNull(active);

        WorkflowTaskQuery workflowTaskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
        long completed = workflowService.countTasks(workflowTaskQuery);
        assertNotNull(completed);

        workflowTaskQuery.setEngineId(getEngine());
        completed = workflowService.countTasks(workflowTaskQuery);
        assertNotNull(completed);
    }

    @Test
    public void testBuildWorkflowWithNoUserTasks() throws Exception 
    {
        // Deploy a definition containing only a service task
        WorkflowDefinition testDefinition = deployDefinition("activiti/testWorkflowNoUserTasks.bpmn20.xml");
        WorkflowBuilder builder = new WorkflowBuilder(testDefinition, workflowService, nodeService, null);
        // Build a workflow
        WorkflowInstance builtInstance = builder.build();
        assertNotNull(builtInstance);
        
        // Check that there is no active workflow for the deployed definition(it should have finished already due to absence of user tasks)
        List<WorkflowInstance> activeInstances = workflowService.getActiveWorkflows(testDefinition.getId());
        assertNotNull(activeInstances);
        assertEquals(0, activeInstances.size());
        
        // Check that there's a historic record of our 'only service task' workflow being run.
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .finishedAfter(builtInstance.getStartDate())
                .singleResult();
        assertNotNull(historicProcessInstance);
    }
    
    @Test
    public void testNonAdminCannotDeployWorkflowBySwitchingNodeType()
    {
        // Test precondition
        assertNull(workflowService.getDefinitionByName("activiti$testProcess"));
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER1);
        NodeRef person = serviceRegistry.getPersonService().getPerson(USER1);
        NodeRef home = (NodeRef) nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
        
        WorkflowDefinition workflowDef = createContentAndSwitchToWorkflow(
                "activiti$testProcess",
                "alfresco/workflow/test-security.bpmn20.xml",
                home);
        
        assertNull("Workflow should not be deployed", workflowDef);
    }
    
    @Test
    public void testNonAdminCannotDeployWorkflowBySwitchingNodeTypeEvenInCorrectLocation()
    {
        // Test precondition
        assertNull(workflowService.getDefinitionByName("activiti$testProcess"));
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER1);
        NodeRef workflowParent = findWorkflowParent();
        
        try
        {
            createContentAndSwitchToWorkflow(
                    "activiti$testProcess",
                    "alfresco/workflow/test-security.bpmn20.xml",
                    workflowParent);
            fail("User should not be able to create a node in the 'correct location'.");
        }
        catch (AccessDeniedException e)
        {
            // Good!
        }
    }
    
    @Test
    public void testAdminCanDeployBySwitchingContentTypeToWorkflow()
    {
        // This test should pass, as the workflow is in the correct location
        // and being created by admin.
        
        // Test precondition
        assertNull(workflowService.getDefinitionByName("activiti$testProcess"));
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        NodeRef workflowParent = findWorkflowParent();
        WorkflowDefinition workflowDef = createContentAndSwitchToWorkflow(
                "activiti$testProcess",
                "alfresco/workflow/test-security.bpmn20.xml",
                workflowParent);
        assertNotNull(workflowDef);

        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, new Date());
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        NodeRef group = groupManager.get(GROUP);
        assertNotNull(group);
        params.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, group);

        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
    }
    
    @Test
    public void testAdminCannotDeployBySwitchingContentTypeToWorkflowWhenLocationIsNotValid()
    {
        // This should fail to deploy the workflow as it is in the wrong location.
        
        // Test precondition
        assertNull(workflowService.getDefinitionByName("activiti$testProcess"));
        
        NodeRef rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        WorkflowDefinition workflowDef = createContentAndSwitchToWorkflow(
                "activiti$testProcess",
                "alfresco/workflow/test-security.bpmn20.xml",
                rootNode);
        assertNull("Workflow should not be deployed", workflowDef);
    }

    /**
     * 
     */
    @Test
    public void testMNT21638_1()
    {
        WorkflowDefinition definition = deployDefinition("activiti/test-MNT21638-1.bpmn20.xml");

        personManager.setUser(USER1);

        // Start the Workflow
        try
        {
            WorkflowPath path = workflowService.startWorkflow(definition.getId(), null);
            fail("Workflow should not have been executed");
        }
        catch (Exception e)
        {
            // Do nothing
        }
    }

    /**
     * 
     */
    @Test
    public void testMNT21638_2()
    {
        WorkflowDefinition definition = deployDefinition("activiti/test-MNT21638-2.bpmn20.xml", "MNT21638", true);

        personManager.setUser(USER1);

        // Start the Workflow
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), null);
        String instanceId = path.getInstance().getId();

        assertNotNull(instanceId);
    }
    
    private NodeRef findWorkflowParent()
    {
        RepositoryLocation workflowLocation = (RepositoryLocation)
                applicationContext.getBean("customWorkflowDefsRepositoryLocation");
        NodeRef rootNode = nodeService.getRootNode(workflowLocation.getStoreRef());
        List<NodeRef> workflowParents = serviceRegistry.getSearchService().selectNodes(
                rootNode,
                workflowLocation.getPath(),
                null,
                serviceRegistry.getNamespaceService(),
                false);
        if (workflowParents.size() == 0)
        {
            throw new IllegalStateException("Unable to find workflow location: "+workflowLocation.getPath());
        }
        if (workflowParents.size() > 1)
        {
            throw new IllegalStateException("More than one workflow location? ["+workflowLocation.getPath()+"]");
        }
        
        return workflowParents.get(0);
    }

    /**
     * Deploy as a normal content node, then switch the type to bpm:workflowDefinition.
     * <p>
     * This should not be allowed to happen if you are non-admin.
     */
    private WorkflowDefinition createContentAndSwitchToWorkflow(String processName, String resource, NodeRef parent)
    {
        InputStream input = getInputStream(resource);

        ChildAssociationRef childAssoc = nodeService.createNode(
                parent,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test"),
                ContentModel.TYPE_CONTENT,
                null);
        NodeRef workflowNode = childAssoc.getChildRef();

        ContentWriter writer = serviceRegistry.getContentService().getWriter(workflowNode, ContentModel.PROP_CONTENT, true);
        writer.putContent(input);
        
        // Now change to WorkflowModel.TYPE_WORKFLOW_DEF
        nodeService.setType(workflowNode, WorkflowModel.TYPE_WORKFLOW_DEF);
        // Activate it
        nodeService.setProperty(workflowNode, WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED, true);

        return workflowService.getDefinitionByName(processName);
    }
    
    @Override
    protected String getEngine()
    {
        return ActivitiConstants.ENGINE_ID;
    }

    @Override
    protected String getTestDefinitionPath()
    {
        return ACTIVITI_TEST_TRANSACTION_BPMN20_XML;
    }

    @Override
    protected String getAdhocDefinitionPath()
    {
        return ALFRESCO_WORKFLOW_ADHOC_BPMN20_XML;
    }

    @Override
    protected String getPooledReviewDefinitionPath()
    {
        return ALFRESCO_WORKFLOW_REVIEW_POOLED_BPMN20_XML;
    }
    
    @Override
    protected String getParallelReviewDefinitionPath()
    {
        return ALFRESCO_WORKFLOW_PARALLEL_REVIEW_BPMN20_XML;
    }

    @Override
    protected String getTestTimerDefinitionPath() 
    {
        return ACTIVITI_TEST_TIMER_BPMN20_XML;
    }

    protected String getAssignmentListenerDefinitionPath()
    {
        return "activiti/testAssignmentListener.bmn20.xml";
    }
    
    @Override
    protected QName getAdhocProcessName() 
    {
        return QName.createQName("activitiAdhoc");
    }
}
