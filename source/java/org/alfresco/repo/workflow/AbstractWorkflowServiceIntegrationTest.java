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

package org.alfresco.repo.workflow;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestGroupManager;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowDeployment;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTimer;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * @since 3.4.e
 * @author Nick Smith
 * @author Frederik Heremans
 *
 */
public abstract class AbstractWorkflowServiceIntegrationTest extends BaseSpringTest
{
    private static final String XML = MimetypeMap.MIMETYPE_XML;
    protected final static String USER1 = "WFUser1" + GUID.generate();
    protected final static String USER2 = "WFUser2" + GUID.generate();
    protected final static String USER3 = "WFUser3" + GUID.generate();
    protected final static String GROUP = "WFGroup" + GUID.generate();
    protected final static String SUB_GROUP = "WFSubGroup" + GUID.generate();
    
    protected WorkflowService workflowService;
    protected AuthenticationComponent authenticationComponent;
    protected TestPersonManager personManager;
    protected TestGroupManager groupManager;
    protected NodeService nodeService;
    private NodeRef companyHome;
    
    public void testDeployWorkflowDefinition()
    {
        List<WorkflowDefinition> defs = workflowService.getDefinitions();
        
        WorkflowDefinition definition = deployDefinition(getTestDefinitionPath());
        String id = definition.getId();
        
        // Check the initial set of definitions doesn't contain the newly deployed definition.
        checkDefinitions(definition, false, defs.toArray(new WorkflowDefinition[0]));
        
        // Check getDefinitions().
        List<WorkflowDefinition> newDefs = workflowService.getDefinitions();
        checkDefinitions(definition, true, newDefs.toArray(new WorkflowDefinition[0]));
        assertEquals(defs.size()+1, newDefs.size());
        
        // Check getDefinitionById().
        WorkflowDefinition defById = workflowService.getDefinitionById(id);
        checkDefinition(definition, defById);
        
        // Check getDefinitionByName().
        WorkflowDefinition defByName = workflowService.getDefinitionByName(definition.getName());
        checkDefinition(definition, defByName);
    }

    public void testStartWorkflow()
    {
        WorkflowDefinition definition = deployDefinition(getTestDefinitionPath());
        
        WorkflowPath path = workflowService.startWorkflow(definition.getId(), null);
        assertNotNull(path);
        assertTrue(path.isActive());
        assertNotNull(path.getNode());
        WorkflowInstance instance = path.getInstance();
        assertNotNull(instance);
        assertEquals(definition.getId(), instance.getDefinition().getId());
    }

    public void testStartTask()
    {
        WorkflowDefinition workflowDef = deployDefinition(getAdhocDefinitionPath());
        WorkflowTaskDefinition startTaskDef = workflowDef.getStartTaskDefinition();
        assertNotNull(startTaskDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_DUE_DATE, dueDate);  // task instance field
        params.put(WorkflowModel.PROP_PRIORITY, 1);  // task instance field
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        
        String startTaskDefId = startTaskDef.getId();

        // Check start task was created properly.
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask startTask = tasks.get(0);
        assertEquals(startTaskDefId, startTask.getDefinition().getId());
        
        // Check getStartTask() returns correct task.
        startTask = workflowService.getStartTask(path.getInstance().getId());
        assertNotNull(startTask);
        assertEquals(startTaskDefId, startTask.getDefinition().getId());
        
        // End start task to progress workflow.
        workflowService.endTask(startTask.getId(), null);
        
        // Check start task was persisted and contains correct property values.
        WorkflowTask task = workflowService.getTaskById(startTask.getId());
        Map<QName, Serializable> props = task.getProperties();
        assertEquals(dueDate, props.get(WorkflowModel.PROP_DUE_DATE));
        assertEquals(1, props.get(WorkflowModel.PROP_PRIORITY));
        assertEquals(assignee, props.get(WorkflowModel.ASSOC_ASSIGNEE));
    }

    public void testGetPathProperties() throws Exception
    {
        WorkflowDefinition workflowDef = deployDefinition(getAdhocDefinitionPath());
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);  // task instance field
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);  // task instance field
        String description = "Some Description";
        params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, description);  // task instance field
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field
        
        personManager.setUser(USER1);
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        
        String instanceId = path.getInstance().getId();
        WorkflowTask startTask = workflowService.getStartTask(instanceId);
        workflowService.endTask(startTask.getId(), null);
        
        Map<QName, Serializable> properties = workflowService.getPathProperties(path.getId());
        assertNotNull(properties);
        
        assertEquals(false, properties.get(QName.createQName("{}" + WorkflowConstants.PROP_CANCELLED)));
        assertEquals(instanceId, properties.get(QName.createQName("{}" + WorkflowConstants.PROP_WORKFLOW_INSTANCE_ID)));

        NodeRef initiator = (NodeRef) properties.get(QName.createQName("{}" + WorkflowConstants.PROP_INITIATOR));
        assertEquals(personManager.get(USER1), initiator);
        
        NodeRef expInitiatorHome = (NodeRef) nodeService.getProperty(initiator, ContentModel.PROP_HOMEFOLDER);
        NodeRef initiatorHome = (NodeRef) properties.get(QName.createQName("{}" + WorkflowConstants.PROP_INITIATOR_HOME));
        assertEquals(expInitiatorHome, initiatorHome);
        
        NodeRef actualCompanyHome = (NodeRef) properties.get(QName.createQName("{}" + WorkflowConstants.PROP_COMPANY_HOME));
        assertEquals(companyHome, actualCompanyHome);
        
        assertEquals(wfPackage, properties.get(WorkflowModel.ASSOC_PACKAGE));
        assertEquals(dueDate, properties.get(WorkflowModel.PROP_WORKFLOW_DUE_DATE));
        assertEquals(1, properties.get(WorkflowModel.PROP_WORKFLOW_PRIORITY));
        assertEquals(assignee, properties.get(WorkflowModel.ASSOC_ASSIGNEE));
        assertEquals(description, properties.get(WorkflowModel.PROP_WORKFLOW_DESCRIPTION));
    }
    
    public void testAssociateWorkflowPackage()
    {
        // create workflow package
        authenticationComponent.setSystemUserAsCurrentUser();
        NodeRef rootNode = nodeService.getRootNode(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"));
        ChildAssociationRef childAssoc = nodeService.createNode(rootNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test"), ContentModel.TYPE_CONTENT, null);
        NodeRef contentNode = childAssoc.getChildRef();

        NodeRef pckgNode = workflowService.createPackage(null);
        assertNotNull(pckgNode);
        assertTrue(nodeService.hasAspect(pckgNode, WorkflowModel.ASPECT_WORKFLOW_PACKAGE));
        
        List<WorkflowInstance> existingInstances = workflowService.getWorkflowsForContent(contentNode, true);
        assertNotNull(existingInstances);
        assertEquals(0, existingInstances.size());
        existingInstances = workflowService.getWorkflowsForContent(contentNode, false);
        assertNotNull(existingInstances);
        assertEquals(0, existingInstances.size());
        
        // Add content to the package
        nodeService.addChild(pckgNode, contentNode, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "test123"));
        
        // start workflow
        WorkflowDefinition workflowDef = deployDefinition(getAdhocDefinitionPath());
        Map<QName, Serializable> parameters = new HashMap<QName, Serializable>();
        parameters.put(WorkflowModel.ASSOC_PACKAGE, pckgNode);
        parameters.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), parameters);
        
        assertNotNull(path);
        assertTrue(path.isActive());
        assertNotNull(path.getNode());
        assertNotNull(path.getInstance());
        assertEquals(workflowDef.getId(), path.getInstance().getDefinition().getId());
        String workflowDefId = (String)nodeService.getProperty(pckgNode, WorkflowModel.PROP_WORKFLOW_DEFINITION_ID);
        assertEquals(workflowDefId, workflowDef.getId());
        String workflowDefName = (String)nodeService.getProperty(pckgNode, WorkflowModel.PROP_WORKFLOW_DEFINITION_NAME);
        assertEquals(workflowDefName, workflowDef.getName());
        String workflowInstanceId = (String)nodeService.getProperty(pckgNode, WorkflowModel.PROP_WORKFLOW_INSTANCE_ID);
        assertEquals(workflowInstanceId, path.getInstance().getId());

        // get workflows for content
        List<WorkflowInstance> instances = workflowService.getWorkflowsForContent(contentNode, true);
        assertNotNull(instances);
        assertEquals(1, instances.size());
        
        WorkflowInstance newInstance = instances.get(0);
        assertNotNull(newInstance);
        assertEquals(path.getInstance().getId(), newInstance.getId());
        
        List<WorkflowInstance> completedInstances = workflowService.getWorkflowsForContent(contentNode, false);
        assertNotNull(completedInstances);
        assertEquals(0, completedInstances.size());
        
        // TODO End the workfow instance and test the completed workflow is returned
    }

    public void testTaskCapabilities()
    {
        // start Adhoc workflow as USER1 and assign to USER2
        personManager.setUser(USER1);
        
        // Get the workflow definition.
        WorkflowDefinition workflowDef = deployDefinition(getAdhocDefinitionPath());
        assertNotNull(workflowDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        String workflowInstanceId = path.getInstance().getId();
        
        // End start task to progress workflow
        WorkflowTask startTask = workflowService.getStartTask(workflowInstanceId);
        String startTaskId = startTask.getId();
        workflowService.endTask(startTaskId, null);
        
        // Fetch start task and check capabilities
        startTask = workflowService.getTaskById(startTaskId);
        assertNotNull(startTask);
        assertEquals(startTask.getState(), WorkflowTaskState.COMPLETED);
        
        // check nothing can be done to the task as its completed
        assertFalse(workflowService.isTaskClaimable(startTask, USER1));
        assertFalse(workflowService.isTaskEditable(startTask, USER1));
        assertFalse(workflowService.isTaskReassignable(startTask, USER1));
        assertFalse(workflowService.isTaskReleasable(startTask, USER1));
        
        // Fetch the current task in the workflow
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
        assertNotNull(paths);
        assertEquals(1, paths.size());
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask currentTask = tasks.get(0);
        assertEquals(currentTask.getState(), WorkflowTaskState.IN_PROGRESS);
        assertEquals(currentTask.getProperties().get(ContentModel.PROP_OWNER), USER2);
        
        // check the task is not claimable or releasable by any user as it is not a pooled task
        assertFalse(workflowService.isTaskClaimable(currentTask, USER1));
        assertFalse(workflowService.isTaskClaimable(currentTask, USER2));
        assertFalse(workflowService.isTaskClaimable(currentTask, USER3));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER1));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER2));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER3));
        
        // user1 (initiator) and user2 (owner) should be able to edit and reassign task
        assertTrue(workflowService.isTaskEditable(currentTask, USER1));
        assertTrue(workflowService.isTaskEditable(currentTask, USER2));
        assertTrue(workflowService.isTaskReassignable(currentTask, USER1));
        assertTrue(workflowService.isTaskReassignable(currentTask, USER2));
        
        // user3 should not be able to edit or reassign task
        assertFalse(workflowService.isTaskEditable(currentTask, USER3));
        assertFalse(workflowService.isTaskReassignable(currentTask, USER3));
        
        // cancel the workflow
        workflowService.cancelWorkflow(workflowInstanceId);
    }
    
    public void testPooledTaskCapabilities()
    {
        // make admin current user
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // start pooled review and approve workflow
        WorkflowDefinition workflowDef = deployDefinition(getPooledReviewDefinitionPath());
        assertNotNull(workflowDef);
        
        // Create workflow parameters
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        NodeRef group = groupManager.get(GROUP);
        assertNotNull(group);
        params.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, group);
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        String workflowInstanceId = path.getInstance().getId();
        
        // End start task to progress workflow
        WorkflowTask startTask = workflowService.getStartTask(workflowInstanceId);
        String startTaskId = startTask.getId();
        workflowService.endTask(startTaskId, null);
        
        // Fetch the current task in the workflow
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(workflowInstanceId);
        assertNotNull(paths);
        assertEquals(1, paths.size());
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path.getId());
        assertEquals(1, tasks.size());
        WorkflowTask currentTask = tasks.get(0);
        assertEquals(currentTask.getState(), WorkflowTaskState.IN_PROGRESS);
        assertNull(currentTask.getProperties().get(ContentModel.PROP_OWNER));
        
        // ensure the task is not reassignable by any user
        assertFalse(workflowService.isTaskReassignable(currentTask, USER1));
        assertFalse(workflowService.isTaskReassignable(currentTask, USER2));
        assertFalse(workflowService.isTaskReassignable(currentTask, USER3));
        
        // ensure the task is not releasable by any user
        assertFalse(workflowService.isTaskReleasable(currentTask, USER1));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER2));
        assertFalse(workflowService.isTaskReleasable(currentTask, USER3));
        
        // ensure the task is claimable by the members of the group and sub group
        assertTrue(workflowService.isTaskClaimable(currentTask, USER1));
        assertTrue(workflowService.isTaskClaimable(currentTask, USER2));
        
        // ensure the task is not claimable by members outside of the group
        assertFalse(workflowService.isTaskClaimable(currentTask, USER3));
        
        // ensure the task can be edited 
        assertTrue(workflowService.isTaskEditable(currentTask, USER1));
        assertTrue(workflowService.isTaskEditable(currentTask, USER2));
        assertFalse(workflowService.isTaskEditable(currentTask, USER3));
        
        // claim the task for USER1
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(8);
        properties.put(ContentModel.PROP_OWNER, USER1);
        workflowService.updateTask(currentTask.getId(), properties, null, null);
        currentTask = workflowService.getTaskById(currentTask.getId());
        
        // check flags are correct now USER1 is the owner
        assertFalse(workflowService.isTaskClaimable(currentTask, USER1));
        assertTrue(workflowService.isTaskReleasable(currentTask, USER1));
        assertTrue(workflowService.isTaskEditable(currentTask, USER1));
        assertFalse(workflowService.isTaskReassignable(currentTask, USER1));
        assertFalse(workflowService.isTaskClaimable(currentTask, USER2));
        assertFalse(workflowService.isTaskEditable(currentTask, USER2));
        
        // cancel the workflow
        workflowService.cancelWorkflow(workflowInstanceId);
    }

    public void testGetWorkflowTaskDefinitions()
    {
        //TODO Implement
        WorkflowDefinition definition = deployDefinition(getAdhocDefinitionPath());
        String workflowDefId = definition.getId();
        List<WorkflowTaskDefinition> taskDefs = workflowService.getTaskDefinitions(workflowDefId);
        assertEquals(3, taskDefs.size());
    }
    
    public void testGetTimers() 
    {
    	// Make admin current user
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        // Start process that will have a timer
        WorkflowDefinition workflowDef = deployDefinition(getTestTimerDefinitionPath());
        assertNotNull(workflowDef);
        
        // Create params
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        
        // Assign to USER2
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee); 
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        String workflowInstanceId = path.getInstance().getId();
        
        // End start task to progress workflow
        WorkflowTask startTask = workflowService.getStartTask(workflowInstanceId);
        String startTaskId = startTask.getId();
        workflowService.endTask(startTaskId, null);
        
        // Query the active task, where timer should be on
        WorkflowTask task = getNextTaskForWorkflow(workflowInstanceId);
        
        // Query for timers, timer should be active
        List<WorkflowTimer> timers = workflowService.getTimers(workflowInstanceId);
        assertNotNull(timers);
        assertEquals(1, timers.size());
        
        WorkflowTimer timer = timers.get(0);
        assertNotNull(timer.getId());
        assertNotNull(timer.getDueDate());
        assertNotNull(timer.getName());
        assertNull(timer.getError());
        
        // Check path, should be waiting in task-node
        assertNotNull(timer.getPath());
        assertEquals(task.getPath().getId(), timer.getPath().getId());
        assertEquals(workflowInstanceId, timer.getPath().getInstance().getId());
        assertNotNull(timer.getPath().getNode());
        assertTrue(timer.getPath().getNode().isTaskNode());
        
        // Check task
        assertNotNull(timer.getTask());
        assertEquals(task.getId(), timer.getTask().getId());
        
        // We finish the task, timer should be gone
        workflowService.endTask(task.getId(), null);
        timers = workflowService.getTimers(workflowInstanceId);
        assertNotNull(timers);
        assertEquals(0, timers.size());
    }
    
    public void testQueryTasks()
    {
    	// Start adhoc Workflow
        WorkflowDefinition workflowDef = deployDefinition(getAdhocDefinitionPath());
        assertNotNull(workflowDef);
        
        // Create params
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        Serializable wfPackage = workflowService.createPackage(null);
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage);
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "Test workflow description");
        QName customStringProp = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "customStringProp");
        params.put(customStringProp, "stringValue");
        
        // Assign to USER2
        NodeRef assignee = personManager.get(USER2);
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee); 
        
        // Start a workflow instance
        WorkflowPath path = workflowService.startWorkflow(workflowDef.getId(), params);
        assertNotNull(path);
        assertTrue(path.isActive());
        String workflowInstanceId = path.getInstance().getId();
        
        // End start task to progress workflow
        WorkflowTask startTask = workflowService.getStartTask(workflowInstanceId);
        String startTaskId = startTask.getId();
        workflowService.endTask(startTaskId, null);
        
        WorkflowTask theTask = getNextTaskForWorkflow(workflowInstanceId);
        
        // Set some custom properties on the task
        params = new HashMap<QName, Serializable>();
        params.put(customStringProp, "stringValue");
        workflowService.updateTask(theTask.getId(), params, null, null);
        
        // Test all query features for running tasks
        testTaskQueryInProgress(workflowInstanceId, theTask);

        // Test all query features for the start-task
        testTaskQueryStartTaskCompleted(workflowInstanceId, startTask);
        
        // Finish the task adhoc-task
        workflowService.endTask(theTask.getId(), null);
        
        // Test all query features for completed tasks
        testTaskQueryTaskCompleted(workflowInstanceId, theTask, startTask);
        
        // Finally end the workflow and check the querying isActive == false
        WorkflowTask lastTask = getNextTaskForWorkflow(workflowInstanceId);
        workflowService.endTask(lastTask.getId(), null);
        
        testQueryTasksInactiveWorkflow(workflowInstanceId);
    }
    
	protected void testQueryTasksInactiveWorkflow(String workflowInstanceId) {
		WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
		taskQuery.setActive(false);
		
		List<WorkflowTask> tasks = workflowService.queryTasks(taskQuery);
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        
        taskQuery = createWorkflowTaskQuery(WorkflowTaskState.COMPLETED);
		taskQuery.setActive(true);
		checkNoTasksFoundUsingQuery(taskQuery);
	}

	protected void testTaskQueryTaskCompleted(String workflowInstanceId,
			WorkflowTask theTask, WorkflowTask startTask) 
	{
		List<String> expectedTasks = Arrays.asList(theTask.getId(), startTask.getId());
        checkProcessIdQuery(workflowInstanceId, expectedTasks, WorkflowTaskState.COMPLETED);
        
        // Adhoc task should only be returned
        QName taskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "adhocTask");
        checkTaskNameQuery(taskName, Arrays.asList(theTask.getId()), WorkflowTaskState.COMPLETED);

        // Completed adhocTask is assigned to USER2
        checkActorIdQuery(USER2, Arrays.asList(theTask.getId()), WorkflowTaskState.COMPLETED);
        
        // Workflow is still active, both tasks will be returned
        checkIsActiveQuery(expectedTasks, WorkflowTaskState.COMPLETED);
       
        // Both tasks have custom property set
        checkTaskPropsQuery(expectedTasks, WorkflowTaskState.COMPLETED);
	}

	protected void testTaskQueryInProgress(String workflowInstanceId, WorkflowTask expectedTask)
    {
		List<String> expectedTasks = Arrays.asList(expectedTask.getId());
		
        checkProcessIdQuery(workflowInstanceId, expectedTasks, WorkflowTaskState.IN_PROGRESS);
        checkTaskIdQuery(expectedTask.getId(), WorkflowTaskState.IN_PROGRESS);
        
        QName taskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "adhocTask");
        checkTaskNameQuery(taskName, expectedTasks, WorkflowTaskState.IN_PROGRESS);
        checkActorIdQuery(USER2, expectedTasks, WorkflowTaskState.IN_PROGRESS);
        checkIsActiveQuery(expectedTasks, WorkflowTaskState.IN_PROGRESS);
        checkTaskPropsQuery(expectedTasks, WorkflowTaskState.IN_PROGRESS);
    }
	
	protected void testTaskQueryStartTaskCompleted(String workflowInstanceId, WorkflowTask startTask) {
		List<String> expectedTasks = Arrays.asList(startTask.getId());
		
		checkProcessIdQuery(workflowInstanceId, expectedTasks, WorkflowTaskState.COMPLETED);
	    checkTaskIdQuery(startTask.getId(), WorkflowTaskState.COMPLETED);
	    
	    QName startTaskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "submitAdhocTask");
	    checkTaskNameQuery(startTaskName, expectedTasks, WorkflowTaskState.COMPLETED);
	    checkActorIdQuery(USER1, expectedTasks, WorkflowTaskState.COMPLETED);
	    checkIsActiveQuery(expectedTasks, WorkflowTaskState.COMPLETED);
	    checkTaskPropsQuery(expectedTasks, WorkflowTaskState.COMPLETED);
	}
    
	public void testGetWorkflows() throws Exception
    {
	    String fakeDefId = getEngine() + "$9999999999999";
        List<WorkflowInstance> workflows = workflowService.getActiveWorkflows(fakeDefId);
        assertTrue(workflows.isEmpty());
        workflows = workflowService.getCompletedWorkflows(fakeDefId);
        assertTrue(workflows.isEmpty());
        workflows = workflowService.getWorkflows(fakeDefId);
        assertTrue(workflows.isEmpty());
        
        WorkflowDefinition definition = deployDefinition(getTestDefinitionPath());
        String defId = definition.getId();
        
        workflows = workflowService.getActiveWorkflows(defId);
        assertTrue(workflows.isEmpty());
        workflows = workflowService.getCompletedWorkflows(defId);
        assertTrue(workflows.isEmpty());
        workflows = workflowService.getWorkflows(defId);
        assertTrue(workflows.isEmpty());
        
        // Create workflow parameters
        Date dueDate = new Date();
        String description = "Some Description";
        NodeRef assignee = personManager.get(USER2);

        Serializable wfPackage1 = workflowService.createPackage(null);
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage1);
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);  // task instance field
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);  // task instance field
        params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, description);  // task instance field
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field
        
        // Start workflow 1
        WorkflowPath path1 = workflowService.startWorkflow(defId, params);
        String instance1 = path1.getInstance().getId();
        
        checkActiveWorkflows(defId, instance1);
        checkCompletedWorkflows(defId);
        checkWorkflows(defId, instance1);

        // Start workflow 2
        Serializable wfPackage2 = workflowService.createPackage(null);
        params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_PACKAGE, wfPackage2);
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);  // task instance field
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);  // task instance field
        params.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, description);  // task instance field
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);  // task instance field

        WorkflowPath path2 = workflowService.startWorkflow(defId, params);
        String instance2 = path2.getInstance().getId();
        
        checkActiveWorkflows(defId, instance1, instance2);
        checkCompletedWorkflows(defId);
        checkWorkflows(defId, instance1, instance2);
        
        // End workflow 1
        WorkflowTask startTask1 = workflowService.getStartTask(instance1);
        workflowService.endTask(startTask1.getId(), null);
        List<WorkflowTask> tasks = workflowService.getTasksForWorkflowPath(path1.getId());
        assertEquals(1, tasks.size());
        WorkflowTask task1 = tasks.get(0);
        workflowService.endTask(task1.getId(), null);
        
        checkActiveWorkflows(defId, instance2);
        checkCompletedWorkflows(defId, instance1);
        checkWorkflows(defId, instance1, instance2);
        
        // End workflow 2
        WorkflowTask startTask2 = workflowService.getStartTask(instance2);
        workflowService.endTask(startTask2.getId(), null);
        tasks = workflowService.getTasksForWorkflowPath(path2.getId());
        assertEquals(1, tasks.size());
        WorkflowTask task2 = tasks.get(0);
        workflowService.endTask(task2.getId(), null);
        
        checkActiveWorkflows(defId);
        checkCompletedWorkflows(defId, instance1, instance2);
        checkWorkflows(defId, instance1, instance2);
    }
	
	
	public void testDeleteWorkflow() throws Exception
    {
        //TODO Implement this test!
    }
	public void checkWorkflows(String defId, String... expectedIds)
	{
	    List<WorkflowInstance> workflows = workflowService.getWorkflows(defId);
	    checkWorkflows(workflows, expectedIds);
	}
	
	public void checkCompletedWorkflows(String defId, String... expectedIds)
	{
	    List<WorkflowInstance> workflows = workflowService.getCompletedWorkflows(defId);
	    checkWorkflows(workflows, expectedIds);
	}
	
	private void checkActiveWorkflows(String defId, String... expectedIds)
	{
	    List<WorkflowInstance> workflows = workflowService.getActiveWorkflows(defId);
	    checkWorkflows(workflows, expectedIds);
	}

    private void checkWorkflows(List<WorkflowInstance> workflows, String... expectedIds)
    {
        assertEquals(expectedIds.length, workflows.size());
	    List<String> expIds = Arrays.asList(expectedIds);
	    for (WorkflowInstance workflow : workflows)
        {
            String workflowId = workflow.getId();
            assertTrue("The id: "+workflowId +" was not expected! Expected Ids: "+expIds, expIds.contains(workflowId));
        }
    }
	
    protected void checkTaskNameQuery(QName taskName, List<String> expectedTaskIds, WorkflowTaskState state) {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setTaskName(taskName);
        checkTasksFoundUsingQuery(expectedTaskIds, taskQuery);
        
        QName unexistingTaskName = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "unexistingTask");
        taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setTaskName(unexistingTaskName);
        checkNoTasksFoundUsingQuery(taskQuery);
	}

	protected void checkProcessIdQuery(String workflowInstanceId, List<String> expectedTaskIds, WorkflowTaskState state)
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setProcessId(workflowInstanceId);
        checkTasksFoundUsingQuery(expectedTaskIds, taskQuery);
        
        taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setProcessId(BPMEngineRegistry.createGlobalId(getEngine(), "99999999999"));
        checkNoTasksFoundUsingQuery(taskQuery);
    }
    
    protected void checkTaskIdQuery(String expectedTaskId, WorkflowTaskState state)
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setTaskId(expectedTaskId);
        checkTasksFoundUsingQuery(Arrays.asList(expectedTaskId), taskQuery);
        
        taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setTaskId(BPMEngineRegistry.createGlobalId(getEngine(), "99999999999"));
        checkNoTasksFoundUsingQuery(taskQuery);
    }
    
    protected void checkIsActiveQuery(List<String> expectedTaskIds, WorkflowTaskState state)
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setActive(true);
        checkTasksFoundUsingQuery(expectedTaskIds, taskQuery);
        
        taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setActive(false);
        checkNoTasksFoundUsingQuery(taskQuery);
    }
    
    protected void checkActorIdQuery(String actorId, List<String> expectedTaskIds, WorkflowTaskState state)
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setActorId(actorId);
        checkTasksFoundUsingQuery(expectedTaskIds, taskQuery);
        
        taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setActorId(USER3);
        checkNoTasksFoundUsingQuery(taskQuery);
    }
    
    protected void checkTaskPropsQuery(List<String> expectedTaskIds, WorkflowTaskState state)
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(state);
        QName customStringProp = QName.createQName(NamespaceService.WORKFLOW_MODEL_1_0_URI, "customStringProp");
        Map<QName, Object> taskProps = new HashMap<QName, Object>();
        
        taskProps.put(customStringProp, "stringValue");
        
        taskQuery.setTaskCustomProps(taskProps);
        checkTasksFoundUsingQuery(expectedTaskIds, taskQuery);
        
        taskProps = new HashMap<QName, Object>();
        taskProps.put(customStringProp, "otherValue");
        
        taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setTaskCustomProps(taskProps);
        checkNoTasksFoundUsingQuery(taskQuery);
    }
    
    protected void checkProcessNameQuery(List<String> expectedTaskIds, WorkflowTaskState state)
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(state);
        
        taskQuery.setProcessName(getAdhocProcessName());
        checkTasksFoundUsingQuery(expectedTaskIds, taskQuery);
        
        taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setProcessName(QName.createQName("dummyProcessName"));
        checkNoTasksFoundUsingQuery(taskQuery);
    }
    
    protected void checkProcessPropsQuery(List<String> expectedTaskIds, WorkflowTaskState state)
    {
        WorkflowTaskQuery taskQuery = createWorkflowTaskQuery(state);
        
        Map<QName, Object> processProps = new HashMap<QName, Object>();
        processProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "Test workflow description");
        taskQuery.setProcessCustomProps(processProps);
        checkTasksFoundUsingQuery(expectedTaskIds, taskQuery);
        
        processProps = new HashMap<QName, Object>();
        processProps.put(WorkflowModel.PROP_WORKFLOW_DESCRIPTION, "Wrong workflow description");
        taskQuery = createWorkflowTaskQuery(state);
        taskQuery.setTaskCustomProps(processProps);
        checkNoTasksFoundUsingQuery(taskQuery);
    }
    
    protected WorkflowTaskQuery createWorkflowTaskQuery(WorkflowTaskState state)
    {
    	 WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
         taskQuery.setTaskState(state);
         return taskQuery;
    }
    
    protected void checkTasksFoundUsingQuery(List<String> taskIds, WorkflowTaskQuery workflowTaskQuery)
    {
    	 List<WorkflowTask> tasks = workflowService.queryTasks(workflowTaskQuery);
         assertNotNull(tasks);
         assertEquals(taskIds.size(), tasks.size());
         for(WorkflowTask task : tasks)
         {
        	 assertTrue(taskIds.contains(task.getId()));
         }
    }
    
    protected void checkNoTasksFoundUsingQuery(WorkflowTaskQuery workflowTaskQuery)
    {
    	 List<WorkflowTask> tasks = workflowService.queryTasks(workflowTaskQuery);
         assertNotNull(tasks);
         assertEquals(0, tasks.size());
    }
    
    
    protected WorkflowTask getNextTaskForWorkflow(String workflowInstanceId) 
    {
    	 WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
         taskQuery.setProcessId(workflowInstanceId);
         taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
         
         List<WorkflowTask> workflowTasks = workflowService.queryTasks(taskQuery);
         assertEquals(1, workflowTasks.size());
         return workflowTasks.get(0);
    }
    
    protected WorkflowDefinition deployDefinition(String resource)
    {
        InputStream input = getInputStream(resource);
        WorkflowDeployment deployment = workflowService.deployDefinition(getEngine(), input, XML);
        WorkflowDefinition definition = deployment.getDefinition();
        return definition;
    }
    
    protected abstract QName getAdhocProcessName();
    

    private InputStream getInputStream(String resource)
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input= classLoader.getResourceAsStream(resource);
        return input;
    }
    
    private void checkDefinitions(WorkflowDefinition expDefinition, boolean expContainsDef, WorkflowDefinition... definitions)
    {
        String id = expDefinition.getId();
        for (WorkflowDefinition definition : definitions)
        {
            if(expContainsDef)
            {
                if(definition.getId().equals(id))
                {
                    checkDefinition(expDefinition, definition);
                    return;
                }
                
            }
            else
            {
                if(definition.getId().equals(id))
                {
                    fail("The definitions unexpectedly contain id: "+id);
                }
            }
        }
        if(expContainsDef)
            fail("The definitions did not contain expected id: "+id);
    }

    private void checkDefinition(WorkflowDefinition expDef, WorkflowDefinition actualDef)
    {
        assertEquals(expDef.getId(), actualDef.getId());
        assertEquals(expDef.getName(), actualDef.getName());
        assertEquals(expDef.getDescription(), actualDef.getDescription());
        assertEquals(expDef.getTitle(), actualDef.getTitle());
        assertEquals(expDef.getVersion(), actualDef.getVersion());
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        ServiceRegistry registry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        this.workflowService = registry.getWorkflowService();
        this.authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        this.nodeService = registry.getNodeService();
        Repository repositoryHelper = (Repository) applicationContext.getBean("repositoryHelper");
        this.companyHome = repositoryHelper.getCompanyHome();

        MutableAuthenticationService authenticationService = registry.getAuthenticationService();
        AuthorityService authorityService = registry.getAuthorityService();
        PersonService personService = registry.getPersonService();
        SearchService searchService = registry.getSearchService();

        authenticationComponent.setSystemUserAsCurrentUser();

        // create test users
        this.personManager = new TestPersonManager(authenticationService, personService, nodeService);
        this.groupManager = new TestGroupManager(authorityService, searchService);
        
        personManager.createPerson(USER1);
        personManager.createPerson(USER2);
        personManager.createPerson(USER3);

        // create test groups
        groupManager.addGroupToParent(GROUP, SUB_GROUP);
        
        // add users to groups
        groupManager.addUserToGroup(GROUP, USER1);
        groupManager.addUserToGroup(SUB_GROUP, USER2);
        
        personManager.setUser(USER1);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        super.onTearDownInTransaction();

        authenticationComponent.setSystemUserAsCurrentUser();
        groupManager.clearGroups();
        personManager.clearPeople();
        authenticationComponent.clearCurrentSecurityContext();
    }

    protected abstract String getEngine();
    
    protected abstract String getTestDefinitionPath();
    
    protected abstract String getAdhocDefinitionPath();

    protected abstract String getPooledReviewDefinitionPath();
    
    protected abstract String getTestTimerDefinitionPath();
}
