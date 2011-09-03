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
package org.alfresco.repo.web.scripts.workflow;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.person.TestGroupManager;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;


/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 3.4.e
 */
public abstract class AbstractWorkflowRestApiTest extends BaseWebScriptTest
{
    protected final static String USER1 = "Bob" + GUID.generate();
    protected final static String USER2 = "Jane" + GUID.generate();
    protected final static String USER3 = "Nick" + GUID.generate();
    protected final static String GROUP ="Group" + GUID.generate();
    protected static final String URL_TASKS = "api/task-instances";
    protected static final String URL_USER_TASKS = "api/task-instances?authority={0}";
    protected static final String URL_USER_TASKS_PROPERTIES = "api/task-instances?authority={0}&properties={1}";
    protected static final String URL_TASKS_DUE_BEFORE = "api/task-instances?dueBefore={0}";
    protected static final String URL_TASKS_DUE_AFTER = "api/task-instances?dueAfter={0}";
    protected static final String URL_WORKFLOW_TASKS = "api/workflow-instances/{0}/task-instances";
    protected static final String URL_WORKFLOW_DEFINITIONS = "api/workflow-definitions";
    protected static final String URL_WORKFLOW_DEFINITION = "api/workflow-definitions/{0}";
    protected static final String URL_WORKFLOW_INSTANCES = "api/workflow-instances";
    protected static final String URL_WORKFLOW_INSTANCES_FOR_DEFINITION = "api/workflow-definitions/{0}/workflow-instances";
    protected static final String URL_WORKFLOW_INSTANCES_FOR_NODE = "api/node/{0}/{1}/{2}/workflow-instances";

    protected static final String COMPANY_HOME = "/app:company_home";
    protected static final String TEST_CONTENT = "TestContent";
    protected static final String ADHOC_START_TASK_TYPE = "wf:submitAdhocTask";
    protected static final String ADHOC_TASK_TYPE = "wf:adhocTask";
    protected static final String ADHOC_TASK_COMPLETED_TYPE = "wf:completedAdhocTask";


    private TestPersonManager personManager;
    private TestGroupManager groupManager;
    
    protected WorkflowService workflowService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private NodeRef packageRef;
    private NodeRef contentNodeRef;
    private AuthenticationComponent authenticationComponent;

    private List<String> workflows = new LinkedList<String>(); 

    public void testTaskInstancesGet() throws Exception
    {
        // Check USER2 starts with no tasks.
        personManager.setUser(USER2);
        Response response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER2)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        assertTrue(results.length() == 0);

        // Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Calendar dueDateCal = Calendar.getInstance();
        Date dueDate = dueDateCal.getTime();

        params.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
        String workflowId = adhocPath.getInstance().getId();
        workflows.add(workflowId);

        WorkflowTask startTask = workflowService.getStartTask(workflowId);
        workflowService.endTask(startTask.getId(), null);

        // Check USER2 now has one task.
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(USER2, WorkflowTaskState.IN_PROGRESS);
        WorkflowTask task = tasks.get(0);

        Map<QName, Serializable> updateParams = new HashMap<QName, Serializable>(1);
        updateParams.put(WorkflowModel.PROP_DUE_DATE, new Date());
        workflowService.updateTask(task.getId(), updateParams, null, null);

        personManager.setUser(USER2);
        response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER2)), 200);
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertTrue(results.length() == tasks.size());
        JSONObject result = results.getJSONObject(0);

        int totalItems = results.length();

        String expUrl = "api/task-instances/" + task.getId();
        assertEquals(expUrl, result.getString("url"));
        assertEquals(task.getName(), result.getString("name"));
        assertEquals(task.getTitle(), result.getString("title"));
        assertEquals(task.getDescription(), result.getString("description"));
        assertEquals(task.getState().name(), result.getString("state"));
        assertEquals("api/workflow-paths/" + adhocPath.getId(), result.getString("path"));
        assertFalse(result.getBoolean("isPooled"));
        assertTrue(result.getBoolean("isEditable"));
        assertTrue(result.getBoolean("isReassignable"));
        assertFalse(result.getBoolean("isClaimable"));
        assertFalse(result.getBoolean("isReleasable"));

        JSONObject owner = result.getJSONObject("owner");
        assertEquals(USER2, owner.getString("userName"));
        assertEquals(personManager.getFirstName(USER2), owner.getString("firstName"));
        assertEquals(personManager.getLastName(USER2), owner.getString("lastName"));

        JSONObject properties = result.getJSONObject("properties");
        assertNotNull(properties);

        JSONObject instance = result.getJSONObject("workflowInstance");
        assertNotNull(instance);

        // Check state filtering
        checkTasksState(URL_TASKS + "?state=completed", WorkflowTaskState.COMPLETED);
        checkTasksState(URL_TASKS + "?state=in_progress", WorkflowTaskState.IN_PROGRESS);

        // TODO: Add more tests to check pooled actors.

        // Check for priority filtering
        checkPriorityFiltering(URL_TASKS + "?priority=2");

        // Due after yesterday, started task should be in it
        dueDateCal.add(Calendar.DAY_OF_MONTH, -1);
        checkTasksPresent(MessageFormat.format(URL_TASKS_DUE_AFTER, ISO8601DateFormat.format(dueDateCal.getTime())),
                true, task.getId());

        // Due before yesterday, started task shouldn't be in it
        checkTasksPresent(MessageFormat.format(URL_TASKS_DUE_BEFORE, ISO8601DateFormat.format(dueDateCal.getTime())),
                false, task.getId());

        // Due before tomorrow, started task should be in it
        dueDateCal.add(Calendar.DAY_OF_MONTH, 2);
        checkTasksPresent(MessageFormat.format(URL_TASKS_DUE_BEFORE, ISO8601DateFormat.format(dueDateCal.getTime())),
                true, task.getId());

        // Due after tomorrow, started task shouldn't be in it
        checkTasksPresent(MessageFormat.format(URL_TASKS_DUE_AFTER, ISO8601DateFormat.format(dueDateCal.getTime())),
                false, task.getId());

        // checkFiltering(URL_TASKS + "?dueAfter=" +
        // ISO8601DateFormat.format(dueDate));

        // checkFiltering(URL_TASKS + "?dueBefore=" +
        // ISO8601DateFormat.format(new Date()));

        // Check property filtering on the task assigned to USER2
        String customProperties = "bpm_description,bpm_priority";
        checkTaskPropertyFiltering(customProperties, Arrays.asList("bpm_description", "bpm_priority"));

        // Properties that aren't explicitally present on task should be
        // returned as wel
        customProperties = "bpm_unexistingProperty,bpm_description,bpm_priority";
        checkTaskPropertyFiltering(customProperties,
                Arrays.asList("bpm_description", "bpm_priority", "bpm_unexistingProperty"));

        // Check paging
        int maxItems = 3;
        for (int skipCount = 0; skipCount < totalItems; skipCount += maxItems)
        {
            // one of this should test situation when skipCount + maxItems >
            // totalItems
            checkPaging(MessageFormat.format(URL_USER_TASKS, USER2) + "&maxItems=" + maxItems + "&skipCount="
                    + skipCount, totalItems, maxItems, skipCount);
        }

        // testing when skipCount > totalItems
        checkPaging(MessageFormat.format(URL_USER_TASKS, USER2) + "&maxItems=" + maxItems + "&skipCount="
                + (totalItems + 1), totalItems, maxItems, totalItems + 1);

        // check the exclude filtering
        String exclude = "wf:submitAdhocTask";
        response = sendRequest(new GetRequest(URL_TASKS + "?exclude=" + exclude), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);

        boolean adhocTasksPresent = false;
        for (int i = 0; i < results.length(); i++)
        {
            JSONObject taskJSON = results.getJSONObject(i);

            String type = taskJSON.getString("name");
            if (exclude.equals(type))
            {
                adhocTasksPresent = true;
                break;
            }
        }
        assertFalse("Found wf:submitAdhocTask when they were supposed to be excluded", adhocTasksPresent);
    }
    
    public void testTaskInstancesForWorkflowGet() throws Exception
    {
        // Check starts with no workflow.
        personManager.setUser(USER2);
        sendRequest(new GetRequest(MessageFormat.format(URL_WORKFLOW_TASKS, "Foo")), Status.STATUS_INTERNAL_SERVER_ERROR);

        // Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Calendar dueDateCal = Calendar.getInstance();
        Date dueDate = dueDateCal.getTime();

        params.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
        String workflowId = adhocPath.getInstance().getId();
        workflows.add(workflowId);

        // End start task.
        WorkflowTask startTask = workflowService.getStartTask(workflowId);
        String startTaskId = startTask.getId();
        workflowService.endTask(startTaskId, null);

        // Check USER2 now has one task.
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(USER2, WorkflowTaskState.IN_PROGRESS);
        assertEquals(1, tasks.size());
        WorkflowTask task = tasks.get(0);

        // Retrieve tasks using the workflow instance
        String baseUrl = MessageFormat.format(URL_WORKFLOW_TASKS, workflowId);

        // Check returns the completed start task and the current task.
        String adhocTaskId = task.getId();
        checkTasksMatch(baseUrl, startTaskId, adhocTaskId);

        String completedUrl = baseUrl + "?state=" + WorkflowTaskState.COMPLETED;
        checkTasksMatch(completedUrl, startTaskId);

        String inProgressUrl = baseUrl + "?state=" + WorkflowTaskState.IN_PROGRESS;
        checkTasksMatch(inProgressUrl, adhocTaskId);

        String user1Url = baseUrl + "?authority=" + USER1;
        checkTasksMatch(user1Url, startTaskId);

        String user2Url = baseUrl + "?authority=" + USER2;
        checkTasksMatch(user2Url, adhocTaskId);

        String user1CompletedURL = user1Url + "&state=" + WorkflowTaskState.COMPLETED;
        checkTasksMatch(user1CompletedURL, startTaskId);

        String user1InProgressURL = user1Url + "&state=" + WorkflowTaskState.IN_PROGRESS;
        checkTasksMatch(user1InProgressURL);

        String user2CompletedURL = user2Url + "&state=" + WorkflowTaskState.COMPLETED;
        checkTasksMatch(user2CompletedURL);

        String user2InProgressURL = user2Url + "&state=" + WorkflowTaskState.IN_PROGRESS;
        checkTasksMatch(user2InProgressURL, adhocTaskId);
    }

    public void testTaskInstanceGet() throws Exception
    {
        //Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        
        Calendar dueDateCal = Calendar.getInstance();
        dueDateCal.clear(Calendar.MILLISECOND);
        Date dueDate = dueDateCal.getTime();
        
        NodeRef assignee = personManager.get(USER2);
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, assignee);
        params.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
        String workflowId = adhocPath.getInstance().getId();
        workflows.add(workflowId);
        
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.getId()).get(0);

        // Get the start-task
        Response response = sendRequest(new GetRequest(URL_TASKS + "/" + startTask.getId()), Status.STATUS_OK);
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);

        assertEquals(startTask.getId(), result.getString("id"));
        assertEquals(URL_TASKS + "/" + startTask.getId(), result.getString("url"));
        assertEquals(startTask.getName(), result.getString("name"));
        assertEquals(startTask.getTitle(), result.getString("title"));
        assertEquals(startTask.getDescription(), result.getString("description"));

        assertEquals(startTask.getState().name(), result.getString("state"));
        assertEquals("api/workflow-paths/" + adhocPath.getId(), result.getString("path"));
        
        checkWorkflowTaskEditable(result);
        checkWorkflowTaskOwner(result, USER1);
        checkWorkflowTaskPropertiesPresent(result);

        JSONObject properties = result.getJSONObject("properties");
        assertEquals(1, properties.getInt("bpm_priority"));
        String dueDateStr = ISO8601DateFormat.format(dueDate);
        assertEquals(dueDateStr, properties.getString("bpm_dueDate"));
        assertEquals(assignee.toString(), properties.getString("bpm_assignee"));
        assertEquals(packageRef.toString(), properties.getString("bpm_package"));
        
        checkWorkflowInstance(startTask.getPath().getInstance(), result.getJSONObject("workflowInstance"));
        checkWorkflowTaskDefinition(startTask.getDefinition(), result.getJSONObject("definition"));
        
        // Finish the start-task, and fetch it again
        workflowService.endTask(startTask.getId(), null);
        startTask = workflowService.getTaskById(startTask.getId()); 

        response = sendRequest(new GetRequest(URL_TASKS + "/" + startTask.getId()), Status.STATUS_OK);
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        result = json.getJSONObject("data");
        assertNotNull(result);
        
        assertEquals(startTask.getId(), result.getString("id"));
        assertEquals(URL_TASKS + "/" + startTask.getId(), result.getString("url"));
        assertEquals(startTask.getName(), result.getString("name"));
        assertEquals(startTask.getTitle(), result.getString("title"));
        assertEquals(startTask.getDescription(), result.getString("description"));
        
        assertEquals(startTask.getState().name(), result.getString("state"));
        assertEquals("api/workflow-paths/" + adhocPath.getId(), result.getString("path"));

        checkWorkflowTaskReadOnly(result);
        checkWorkflowTaskOwner(result, USER1);
        checkWorkflowTaskPropertiesPresent(result);

        checkWorkflowInstance(startTask.getPath().getInstance(), result.getJSONObject("workflowInstance"));
        checkWorkflowTaskDefinition(startTask.getDefinition(), result.getJSONObject("definition"));
        
        // Get the next active task
        WorkflowTask firstTask = workflowService.getTasksForWorkflowPath(adhocPath.getId()).get(0);
        
        response = sendRequest(new GetRequest(URL_TASKS + "/" + firstTask.getId()), 200);
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        result = json.getJSONObject("data");
        assertNotNull(result);
        
        assertEquals(firstTask.getId(), result.getString("id"));
        assertEquals(URL_TASKS + "/" + firstTask.getId(), result.getString("url"));
        assertEquals(firstTask.getName(), result.getString("name"));
        assertEquals(firstTask.getTitle(), result.getString("title"));
        assertEquals(firstTask.getDescription(), result.getString("description"));
       
        // Task should be in progress
        assertEquals(firstTask.getState().name(), result.getString("state"));
        assertEquals(WorkflowTaskState.IN_PROGRESS.toString(), result.getString("state"));
        assertEquals("api/workflow-paths/" + adhocPath.getId(), result.getString("path"));
        
        checkWorkflowTaskEditable(result);
        checkWorkflowTaskOwner(result, USER2);
        checkWorkflowTaskPropertiesPresent(result);
        
        checkWorkflowInstance(firstTask.getPath().getInstance(), result.getJSONObject("workflowInstance"));
        checkWorkflowTaskDefinition(firstTask.getDefinition(), result.getJSONObject("definition"));
        
        // Finish the task, and fetch it again
        workflowService.endTask(firstTask.getId(), null);
        firstTask = workflowService.getTaskById(firstTask.getId()); 
        
        response = sendRequest(new GetRequest(URL_TASKS + "/" + firstTask.getId()), 200);
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        result = json.getJSONObject("data");
        assertNotNull(result);
        
        assertEquals(firstTask.getId(), result.getString("id"));
        assertEquals(URL_TASKS + "/" + firstTask.getId(), result.getString("url"));
        assertEquals(firstTask.getName(), result.getString("name"));
        assertEquals(firstTask.getTitle(), result.getString("title"));
        assertEquals(firstTask.getDescription(), result.getString("description"));
        
        // The task should be completed
        assertEquals(firstTask.getState().name(), result.getString("state"));
        assertEquals(WorkflowTaskState.COMPLETED.toString(), result.getString("state"));
        assertEquals("api/workflow-paths/" + adhocPath.getId(), result.getString("path"));

        checkWorkflowTaskReadOnly(result);
        checkWorkflowTaskOwner(result, USER2);
        checkWorkflowTaskPropertiesPresent(result);
        
        checkWorkflowInstance(firstTask.getPath().getInstance(), result.getJSONObject("workflowInstance"));
        checkWorkflowTaskDefinition(firstTask.getDefinition(), result.getJSONObject("definition"));
    }

    private void checkWorkflowTaskPropertiesPresent(JSONObject taskJson) throws Exception
    {
        JSONObject properties = taskJson.getJSONObject("properties");
        assertNotNull(properties);
        assertTrue(properties.has("bpm_priority"));
        assertTrue(properties.has("bpm_description"));
        assertTrue(properties.has("bpm_reassignable"));
    }

    private void checkWorkflowTaskReadOnly(JSONObject taskJson) throws Exception
    {
        // Task shouldn't be editable and reassignable, since it's completed
        assertFalse(taskJson.getBoolean("isPooled"));
        assertFalse(taskJson.getBoolean("isEditable"));
        assertFalse(taskJson.getBoolean("isReassignable"));
        assertFalse(taskJson.getBoolean("isClaimable"));
        assertFalse(taskJson.getBoolean("isReleasable"));
    }

    private void checkWorkflowTaskOwner(JSONObject taskJson, String user) throws Exception
    {
        JSONObject owner = taskJson.getJSONObject("owner");
        assertEquals(user, owner.getString("userName"));
        assertEquals(personManager.getFirstName(user), owner.getString("firstName"));
        assertEquals(personManager.getLastName(user), owner.getString("lastName"));
    }

    private void checkWorkflowTaskEditable(JSONObject taskJson) throws Exception
    {
        assertFalse(taskJson.getBoolean("isPooled"));
        assertTrue(taskJson.getBoolean("isEditable"));
        assertTrue(taskJson.getBoolean("isReassignable"));
        assertFalse(taskJson.getBoolean("isClaimable"));
        assertFalse(taskJson.getBoolean("isReleasable"));
    }

    private void checkWorkflowInstance(WorkflowInstance wfInstance, JSONObject instance) throws Exception
    {
        assertNotNull(instance);
        assertEquals(wfInstance.getId(), instance.getString("id"));
        assertTrue(instance.has("url"));
        assertEquals(wfInstance.getDefinition().getName(), instance.getString("name"));
        assertEquals(wfInstance.getDefinition().getTitle(), instance.getString("title"));
        assertEquals(wfInstance.getDefinition().getDescription(), instance.getString("description"));
        assertEquals(wfInstance.isActive(), instance.getBoolean("isActive"));
        assertTrue(instance.has("startDate"));

        JSONObject initiator = instance.getJSONObject("initiator");

        assertEquals(USER1, initiator.getString("userName"));
        assertEquals(personManager.getFirstName(USER1), initiator.getString("firstName"));
        assertEquals(personManager.getLastName(USER1), initiator.getString("lastName"));
    }

    private void checkWorkflowTaskDefinition(WorkflowTaskDefinition wfDefinition, JSONObject definition) throws Exception
    {
        assertNotNull(definition);

        assertEquals(wfDefinition.getId(), definition.getString("id"));
        assertTrue(definition.has("url"));

        JSONObject type = definition.getJSONObject("type");
        TypeDefinition startType = (wfDefinition).getMetadata();

        assertNotNull(type);

        assertEquals(startType.getName().toPrefixString(), type.getString("name"));
        assertEquals(startType.getTitle(), type.getString("title"));
        assertEquals(startType.getDescription(), type.getString("description"));
        assertTrue(type.has("url"));

        JSONObject node = definition.getJSONObject("node");
        WorkflowNode startNode = wfDefinition.getNode();

        assertNotNull(node);

        assertEquals(startNode.getName(), node.getString("name"));
        assertEquals(startNode.getTitle(), node.getString("title"));
        assertEquals(startNode.getDescription(), node.getString("description"));
        assertEquals(startNode.isTaskNode(), node.getBoolean("isTaskNode"));

        JSONArray transitions = node.getJSONArray("transitions");
        WorkflowTransition[] startTransitions = startNode.getTransitions();

        assertNotNull(transitions);

        assertEquals(startTransitions.length, transitions.length());

        for (int i = 0; i < transitions.length(); i++)
        {
            JSONObject transition = transitions.getJSONObject(i);
            WorkflowTransition startTransition = startTransitions[i];

            assertNotNull(transition);

            if (startTransition.getId() == null)
            {
                assertEquals("", transition.getString("id"));
            }
            else
            {
                assertEquals(startTransition.getId(), transition.getString("id"));
            }
            assertEquals(startTransition.getTitle(), transition.getString("title"));
            assertEquals(startTransition.getDescription(), transition.getString("description"));
            assertEquals(startTransition.isDefault(), transition.getBoolean("isDefault"));
            assertTrue(transition.has("isHidden"));
        }

    }

    public void testTaskInstancePut() throws Exception
    {
        // Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        
        Calendar dueDate = Calendar.getInstance();
        dueDate.set(Calendar.MILLISECOND, 0);
        
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());
        params.put(WorkflowModel.PROP_PRIORITY, 2);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
        String worfklowId = adhocPath.getInstance().getId();
        workflows.add(worfklowId);
        
        WorkflowTask startTask = workflowService.getStartTask(adhocPath.getInstance().getId());
        
        // Finish the start-task
        workflowService.endTask(startTask.getId(), null);
        
        WorkflowTask firstTask = workflowService.getTasksForWorkflowPath(adhocPath.getId()).get(0);

        Response response = sendRequest(new GetRequest(URL_TASKS + "/" + firstTask.getId()), 200);

        JSONObject jsonProperties = new JSONObject(response.getContentAsString()).getJSONObject("data").getJSONObject("properties");

        // make some changes in existing properties
        jsonProperties.remove(qnameToString(WorkflowModel.ASSOC_PACKAGE));
        jsonProperties.put(qnameToString(WorkflowModel.PROP_COMMENT), "Edited comment");
        
        Calendar newDueDate = Calendar.getInstance();
        newDueDate.set(Calendar.MILLISECOND, 0);
        
        jsonProperties.put(qnameToString(WorkflowModel.PROP_DUE_DATE), ISO8601DateFormat.format(newDueDate.getTime()));
        jsonProperties.put(qnameToString(WorkflowModel.PROP_DESCRIPTION), "Edited description");
        jsonProperties.put(qnameToString(WorkflowModel.PROP_PRIORITY), 1);

        // Add some custom properties, which are not defined in typeDef
        jsonProperties.put("customIntegerProperty", 1234);
        jsonProperties.put("customBooleanProperty", Boolean.TRUE);
        jsonProperties.put("customStringProperty", "Property value");

        // test USER3 can not update the task
        personManager.setUser(USER3);
        Response unauthResponse = sendRequest(new PutRequest(URL_TASKS + "/" + firstTask.getId(), jsonProperties.toString(), "application/json"), 401);
        assertEquals(Status.STATUS_UNAUTHORIZED, unauthResponse.getStatus());


        // test USER2 (the task owner) can update the task
        personManager.setUser(USER2);
        Response putResponse = sendRequest(new PutRequest(URL_TASKS + "/" + firstTask.getId(), jsonProperties.toString(), "application/json"), 200);

        assertEquals(Status.STATUS_OK, putResponse.getStatus());
        String jsonStr = putResponse.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);

        JSONObject editedJsonProperties = result.getJSONObject("properties");
        compareProperties(jsonProperties, editedJsonProperties);
        
        // test USER1 (the task workflow initiator) can update the task
        personManager.setUser(USER1);
        putResponse = sendRequest(new PutRequest(URL_TASKS + "/" + firstTask.getId(), jsonProperties.toString(), "application/json"), 200);

        assertEquals(Status.STATUS_OK, putResponse.getStatus());
        jsonStr = putResponse.getContentAsString();
        json = new JSONObject(jsonStr);
        result = json.getJSONObject("data");
        assertNotNull(result);

        editedJsonProperties = result.getJSONObject("properties");
        compareProperties(jsonProperties, editedJsonProperties);

        // Reassign the task to USER3 using taskInstance PUT
        jsonProperties = new JSONObject();
        jsonProperties.put(qnameToString(ContentModel.PROP_OWNER), USER3);
        putResponse = sendRequest(new PutRequest(URL_TASKS + "/" + firstTask.getId(), jsonProperties.toString(), "application/json"), 200);
        assertEquals(Status.STATUS_OK, putResponse.getStatus());
        
        // test USER3 (now the task owner) can update the task
        personManager.setUser(USER3);
        
        jsonProperties.put(qnameToString(WorkflowModel.PROP_COMMENT), "Edited comment by USER3");
        putResponse = sendRequest(new PutRequest(URL_TASKS + "/" + firstTask.getId(), jsonProperties.toString(), "application/json"), 200);
        
        assertEquals(Status.STATUS_OK, putResponse.getStatus());
        
        jsonStr = putResponse.getContentAsString();
        json = new JSONObject(jsonStr);
        result = json.getJSONObject("data");
        assertNotNull(result);
        
        editedJsonProperties = result.getJSONObject("properties");
        compareProperties(jsonProperties, editedJsonProperties);
    }

    public void testTaskInstancePutCompletedTask() throws Exception
    {
        // Start workflow as USER1 and assign to self
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER1));
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
        String WorkflowId = adhocPath.getInstance().getId();
        workflows.add(WorkflowId);
        
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.getId()).get(0);
        
        // Finish the start-task
        workflowService.endTask(startTask.getId(), null);
        
        Response getResponse = sendRequest(new GetRequest(URL_TASKS + "/" + startTask.getId()), 200);

        JSONObject jsonProperties = new JSONObject(getResponse.getContentAsString()).getJSONObject("data").getJSONObject("properties");

        // Make a change
        jsonProperties.put(qnameToString(WorkflowModel.PROP_DESCRIPTION), "Edited description");

        // Update task. An error is expected, since the task is completed (and not editable)
       sendRequest(new PutRequest(URL_TASKS + "/" + startTask.getId(), jsonProperties.toString(), "application/json"), Status.STATUS_UNAUTHORIZED);
    }

    public void testWorkflowDefinitionsGet() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_WORKFLOW_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        JSONObject json = new JSONObject(response.getContentAsString());
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        assertTrue(results.length() > 0);

        boolean adhocWorkflowPresent = false;
        
        String adhocDefName = getAdhocWorkflowDefinitionName();
        for (int i = 0; i < results.length(); i++)
        {
            JSONObject workflowDefinitionJSON = results.getJSONObject(i);

            assertTrue(workflowDefinitionJSON.has("id"));
            assertTrue(workflowDefinitionJSON.getString("id").length() > 0);

            assertTrue(workflowDefinitionJSON.has("url"));
            String url = workflowDefinitionJSON.getString("url");
            assertTrue(url.length() > 0);
            assertTrue(url.startsWith("api/workflow-definitions/"));

            assertTrue(workflowDefinitionJSON.has("name"));
            assertTrue(workflowDefinitionJSON.getString("name").length() > 0);

            assertTrue(workflowDefinitionJSON.has("title"));
            String title = workflowDefinitionJSON.getString("title");
            assertTrue(title.length() > 0);

            assertTrue(workflowDefinitionJSON.has("description"));
            String description = workflowDefinitionJSON.getString("description");
            assertTrue(description.length() > 0);

            if(adhocDefName.equals(workflowDefinitionJSON.getString("name"))) 
            {
                assertEquals(getAdhocWorkflowDefinitionTitle(), title);
                assertEquals(getAdhocWorkflowDefinitionDescription(), description);
                adhocWorkflowPresent = true;
            }
        }
        
        assertTrue("Adhoc workflow definition was not present!", adhocWorkflowPresent);
        
        // filter the workflow definitions and check they are not returned
        String exclude = adhocDefName;
        response = sendRequest(new GetRequest(URL_WORKFLOW_DEFINITIONS + "?exclude=" + exclude), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        json = new JSONObject(response.getContentAsString());
        results = json.getJSONArray("data");
        assertNotNull(results);
        
        adhocWorkflowPresent = false;
        for (int i = 0; i < results.length(); i++)
        {
            JSONObject workflowDefinitionJSON = results.getJSONObject(i);
            
            String name = workflowDefinitionJSON.getString("name");
            if (exclude.equals(name))
            {
                adhocWorkflowPresent = true;
                break;
            }
        }
        
        assertFalse("Found adhoc workflow when it was supposed to be excluded", adhocWorkflowPresent);
        
        // filter with a wildcard and ensure they all get filtered out
        exclude = adhocDefName + ", jbpm$wcmwf:*";
        response = sendRequest(new GetRequest(URL_WORKFLOW_DEFINITIONS + "?exclude=" + exclude), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        json = new JSONObject(response.getContentAsString());
        results = json.getJSONArray("data");
        assertNotNull(results);
        
        adhocWorkflowPresent = false;
        boolean wcmWorkflowsPresent = false;
        for (int i = 0; i < results.length(); i++)
        {
            JSONObject workflowDefinitionJSON = results.getJSONObject(i);
            
            String name = workflowDefinitionJSON.getString("name");
            if (name.equals(adhocDefName))
            {
                adhocWorkflowPresent = true;
            }
            if (name.startsWith("jbpm$wcmwf:"))
            {
                wcmWorkflowsPresent = true;
            }
        }
        
        assertFalse("Found adhoc workflow when it was supposed to be excluded", adhocWorkflowPresent);
        assertFalse("Found a WCM workflow when they were supposed to be excluded", wcmWorkflowsPresent);
    }

    public void testWorkflowDefinitionGet() throws Exception
    {
        // Get the latest definition for the adhoc-workflow
        WorkflowDefinition wDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());

        String responseUrl = MessageFormat.format(URL_WORKFLOW_DEFINITION, wDef.getId());

        Response response = sendRequest(new GetRequest(responseUrl), Status.STATUS_OK);
        JSONObject json = new JSONObject(response.getContentAsString());
        JSONObject workflowDefinitionJSON = json.getJSONObject("data");
        assertNotNull(workflowDefinitionJSON);
        
        // Check fields
        assertTrue(workflowDefinitionJSON.has("id"));
        assertTrue(workflowDefinitionJSON.getString("id").length() > 0);

        assertTrue(workflowDefinitionJSON.has("url"));
        String url = workflowDefinitionJSON.getString("url");
        assertTrue(url.length() > 0);
        assertTrue(url.startsWith("api/workflow-definitions/"));

        assertTrue(workflowDefinitionJSON.has("name"));
        assertTrue(workflowDefinitionJSON.getString("name").length() > 0);
        assertEquals(getAdhocWorkflowDefinitionName(), workflowDefinitionJSON.getString("name"));

        assertTrue(workflowDefinitionJSON.has("title"));
        assertTrue(workflowDefinitionJSON.getString("title").length() > 0);

        assertTrue(workflowDefinitionJSON.has("description"));
        assertTrue(workflowDefinitionJSON.getString("description").length() > 0);
        
        assertTrue(workflowDefinitionJSON.has("startTaskDefinitionUrl"));
        String startTaskDefUrl = workflowDefinitionJSON.getString("startTaskDefinitionUrl");
        assertEquals(startTaskDefUrl, "api/classes/" + getSafeDefinitionName(ADHOC_START_TASK_TYPE));
        
        assertTrue(workflowDefinitionJSON.has("startTaskDefinitionType"));
        assertEquals(ADHOC_START_TASK_TYPE, workflowDefinitionJSON.getString("startTaskDefinitionType"));
        
        // Check task-definitions
        JSONArray taskDefinitions = workflowDefinitionJSON.getJSONArray("taskDefinitions");
        assertNotNull(taskDefinitions);
        
        // Two task definitions should be returned. Start-task is not included
        assertEquals(2, taskDefinitions.length());
        
        // Should be adhoc-task
        JSONObject firstTaskDefinition  = (JSONObject) taskDefinitions.get(0);
        checkTaskDefinitionTypeAndUrl(ADHOC_TASK_TYPE, firstTaskDefinition);
                
        // Should be adhoc completed task
        JSONObject secondTaskDefinition  = (JSONObject) taskDefinitions.get(1);
        checkTaskDefinitionTypeAndUrl(ADHOC_TASK_COMPLETED_TYPE, secondTaskDefinition);
    }
    
    private void checkTaskDefinitionTypeAndUrl(String expectedTaskType, JSONObject taskDefinition) throws Exception
    {
        // Check type
        assertTrue(taskDefinition.has("type"));
        assertEquals(expectedTaskType, taskDefinition.getString("type"));
        
        // Check URL
        assertTrue(taskDefinition.has("url"));
        assertEquals("api/classes/" + 
                    getSafeDefinitionName(expectedTaskType), taskDefinition.getString("url"));
    }

    private String getSafeDefinitionName(String definitionName) 
    {
        return definitionName.replace(":", "_");
    }

    public void testWorkflowInstanceGet() throws Exception
    {
        //Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        params.put(WorkflowModel.PROP_CONTEXT, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
        String WorkflowId = adhocPath.getInstance().getId();
        workflows.add(WorkflowId);
        
        // End start task.
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.getId()).get(0);
        startTask = workflowService.endTask(startTask.getId(), null);

        WorkflowInstance adhocInstance = startTask.getPath().getInstance();

        Response response = sendRequest(new GetRequest(URL_WORKFLOW_INSTANCES + "/" + adhocInstance.getId() + "?includeTasks=true"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);

        assertEquals(adhocInstance.getId(), result.getString("id"));
        assertTrue(result.opt("message").equals(JSONObject.NULL));
        assertEquals(adhocInstance.getDefinition().getName(), result.getString("name"));
        assertEquals(adhocInstance.getDefinition().getTitle(), result.getString("title"));
        assertEquals(adhocInstance.getDefinition().getDescription(), result.getString("description"));
        assertEquals(adhocInstance.isActive(), result.getBoolean("isActive"));
        assertEquals(ISO8601DateFormat.format(adhocInstance.getStartDate()), result.getString("startDate"));
        assertNotNull(result.getString("dueDate"));
        assertNotNull(result.getString("endDate"));
        assertEquals(1, result.getInt("priority"));
        JSONObject initiator = result.getJSONObject("initiator");

        assertEquals(USER1, initiator.getString("userName"));
        assertEquals(personManager.getFirstName(USER1), initiator.getString("firstName"));
        assertEquals(personManager.getLastName(USER1), initiator.getString("lastName"));

        assertEquals(adhocInstance.getContext().toString(), result.getString("context"));
        assertEquals(adhocInstance.getWorkflowPackage().toString(), result.getString("package"));
        assertNotNull(result.getString("startTaskInstanceId"));

        JSONObject jsonDefinition = result.getJSONObject("definition");
        WorkflowDefinition adhocDefinition = adhocInstance.getDefinition();

        assertNotNull(jsonDefinition);

        assertEquals(adhocDefinition.getId(), jsonDefinition.getString("id"));
        assertEquals(adhocDefinition.getName(), jsonDefinition.getString("name"));
        assertEquals(adhocDefinition.getTitle(), jsonDefinition.getString("title"));
        assertEquals(adhocDefinition.getDescription(), jsonDefinition.getString("description"));
        assertEquals(adhocDefinition.getVersion(), jsonDefinition.getString("version"));
        assertEquals(adhocDefinition.getStartTaskDefinition().getMetadata().getName().toPrefixString(namespaceService), jsonDefinition.getString("startTaskDefinitionType"));
        assertTrue(jsonDefinition.has("taskDefinitions"));

        JSONArray tasks = result.getJSONArray("tasks");
        assertTrue(tasks.length() > 1);
    }

    public void testWorkflowInstancesGet() throws Exception
    {
        //Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        params.put(WorkflowModel.PROP_CONTEXT, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
        String WorkflowId = adhocPath.getInstance().getId();
        workflows.add(WorkflowId);
        
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.getId()).get(0);
        WorkflowInstance adhocInstance = startTask.getPath().getInstance();
        workflowService.endTask(startTask.getId(), null);

        // Get Workflow Instance Collection 
        Response response = sendRequest(new GetRequest(URL_WORKFLOW_INSTANCES), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONArray result = json.getJSONArray("data");
        assertNotNull(result);

        int totalItems = result.length();
        for (int i = 0; i < result.length(); i++)
        {
            checkSimpleWorkflowInstanceResponse(result.getJSONObject(i));
        }

        Response forDefinitionResponse = sendRequest(new GetRequest(MessageFormat.format(URL_WORKFLOW_INSTANCES_FOR_DEFINITION, adhocDef.getId())), 200);
        assertEquals(Status.STATUS_OK, forDefinitionResponse.getStatus());
        String forDefinitionJsonStr = forDefinitionResponse.getContentAsString();
        JSONObject forDefinitionJson = new JSONObject(forDefinitionJsonStr);
        JSONArray forDefinitionResult = forDefinitionJson.getJSONArray("data");
        assertNotNull(forDefinitionResult);

        for (int i = 0; i < forDefinitionResult.length(); i++)
        {
            checkSimpleWorkflowInstanceResponse(forDefinitionResult.getJSONObject(i));
        }
        
        // create a date an hour ago to test filtering
        Calendar hourAgoCal = Calendar.getInstance();
        hourAgoCal.setTime(dueDate);
        hourAgoCal.add(Calendar.HOUR_OF_DAY, -1);
        Date anHourAgo = hourAgoCal.getTime();
        
        Calendar hourLater = Calendar.getInstance();
        hourLater.setTime(dueDate);
        hourLater.add(Calendar.HOUR_OF_DAY, 1);
        Date anHourLater = hourLater.getTime();

        // filter by initiator
        checkFiltering(URL_WORKFLOW_INSTANCES + "?initiator=" + USER1);

        // filter by startedAfter
        checkFiltering(URL_WORKFLOW_INSTANCES + "?startedAfter=" + ISO8601DateFormat.format(anHourAgo));

        // filter by startedBefore
        checkFiltering(URL_WORKFLOW_INSTANCES + "?startedBefore=" + ISO8601DateFormat.format(anHourLater));

        // filter by dueAfter
        checkFiltering(URL_WORKFLOW_INSTANCES + "?dueAfter=" + ISO8601DateFormat.format(anHourAgo));

        // filter by dueBefore
        checkFiltering(URL_WORKFLOW_INSTANCES + "?dueBefore=" + ISO8601DateFormat.format(anHourLater));

        if (adhocInstance.getEndDate() != null)
        {
            // filter by completedAfter
            checkFiltering(URL_WORKFLOW_INSTANCES + "?completedAfter=" + ISO8601DateFormat.format(adhocInstance.getEndDate()));

            // filter by completedBefore
            checkFiltering(URL_WORKFLOW_INSTANCES + "?completedBefore=" + ISO8601DateFormat.format(adhocInstance.getEndDate()));
        }

        // filter by priority        
        checkFiltering(URL_WORKFLOW_INSTANCES + "?priority=1");

        // filter by state
        checkFiltering(URL_WORKFLOW_INSTANCES + "?state=active");
        
        // filter by definition name
        checkFiltering(URL_WORKFLOW_INSTANCES + "?definitionName=" + getAdhocWorkflowDefinitionName());
        
        // paging
        int maxItems = 3;        
        for (int skipCount = 0; skipCount < totalItems; skipCount += maxItems)
        {
            checkPaging(URL_WORKFLOW_INSTANCES + "?maxItems=" + maxItems + "&skipCount=" + skipCount, totalItems, maxItems, skipCount);
        }
        
        // check the exclude filtering
        String exclude = getAdhocWorkflowDefinitionName();
        response = sendRequest(new GetRequest(URL_WORKFLOW_INSTANCES + "?exclude=" + exclude), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        
        boolean adhocWorkflowPresent = false;
        for (int i = 0; i < results.length(); i++)
        {
            JSONObject workflowInstanceJSON = results.getJSONObject(i);
            
            String type = workflowInstanceJSON.getString("name");
            if (exclude.equals(type))
            {
                adhocWorkflowPresent = true;
                break;
            }
        }
        
        assertFalse("Found adhoc workflows when they were supposed to be excluded", adhocWorkflowPresent);
    }

    public void testWorkflowInstancesForNodeGet() throws Exception
    {
        //Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        nodeService.addChild(packageRef, contentNodeRef, 
                WorkflowModel.ASSOC_PACKAGE_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName((String)nodeService.getProperty(
                        contentNodeRef, ContentModel.PROP_NAME))));

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);

        String url = MessageFormat.format(URL_WORKFLOW_INSTANCES_FOR_NODE, contentNodeRef.getStoreRef().getProtocol(), contentNodeRef.getStoreRef().getIdentifier(), contentNodeRef.getId());
        Response response = sendRequest(new GetRequest(url), 200);

        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONArray result = json.getJSONArray("data");
        assertNotNull(result);

        assertTrue(result.length() > 0);

        workflowService.cancelWorkflow(adhocPath.getInstance().getId());

        Response afterCancelResponse = sendRequest(new GetRequest(url), 200);

        assertEquals(Status.STATUS_OK, afterCancelResponse.getStatus());
        String afterCancelJsonStr = afterCancelResponse.getContentAsString();
        JSONObject afterCancelJson = new JSONObject(afterCancelJsonStr);
        JSONArray afterCancelResult = afterCancelJson.getJSONArray("data");
        assertNotNull(afterCancelResult);

        assertTrue(afterCancelResult.length() == 0);
    }

    public void testWorkflowInstanceDelete() throws Exception
    {
        //Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName(getAdhocWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        params.put(WorkflowModel.PROP_CONTEXT, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.getId()).get(0);
        startTask = workflowService.endTask(startTask.getId(), null);

        WorkflowInstance adhocInstance = startTask.getPath().getInstance();
        
        // attempt to delete workflow as a user that is not the initiator
        personManager.setUser(USER3);
        String instanceId = adhocInstance.getId();
        sendRequest(new DeleteRequest(URL_WORKFLOW_INSTANCES + "/" + instanceId), Status.STATUS_FORBIDDEN);
        
        // make sure workflow instance is still present
        assertNotNull(workflowService.getWorkflowById(instanceId));

        // now delete as initiator of workflow
        personManager.setUser(USER1);
        sendRequest(new DeleteRequest(URL_WORKFLOW_INSTANCES + "/" + instanceId), Status.STATUS_OK);

        WorkflowInstance instance = workflowService.getWorkflowById(instanceId);
        if (instance != null)
        {
            assertFalse("The deleted workflow is still active!", instance.isActive());
        }
        
        List<WorkflowInstance> instances = workflowService.getActiveWorkflows(adhocInstance.getDefinition().getId());
        for (WorkflowInstance activeInstance : instances)
        {
            assertFalse(instanceId.equals(activeInstance.getId()));
        }
        
        // Try deleting an non-existent workflow instance, should result in 404
        sendRequest(new DeleteRequest(URL_WORKFLOW_INSTANCES + "/" + instanceId), Status.STATUS_NOT_FOUND);
    }

    public void testReviewProcessFlow() throws Exception 
    {
        // Approve path
        runReviewFlow(true);
        
        // Create package again, since WF is deleteds
        packageRef = workflowService.createPackage(null);
        
        // Reject path
        runReviewFlow(false);
    }
    
    public void testReviewPooledProcessFlow() throws Exception 
    {
        // Approve path
        runReviewPooledFlow(true);
        
        // Create package again, since WF is deleteds
        packageRef = workflowService.createPackage(null);
        
        // Reject path
        runReviewPooledFlow(false);
    }
    
    protected void runReviewFlow(boolean approve) throws Exception
    {
        // Start workflow as USER1
        personManager.setUser(USER1);
        WorkflowDefinition reviewDef = workflowService.getDefinitionByName(getReviewWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        // Reviewer is USER2
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        params.put(WorkflowModel.PROP_CONTEXT, packageRef);

        WorkflowPath reviewPath = workflowService.startWorkflow(reviewDef.getId(), params);
        String workflowId = reviewPath.getInstance().getId();
        workflows.add(workflowId);
        
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(reviewPath.getId()).get(0);

        // End start task
        startTask = workflowService.endTask(startTask.getId(), null);

        // Check of task is available in list of reviewer, USER2
        personManager.setUser(USER2);
        Response response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER2)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());

        String taskId = results.getJSONObject(0).getString("id");

        // Delegate approval/rejection to implementing engine-test
        if (approve)
        {
            approveTask(taskId);
        }
        else
        {
            rejectTask(taskId);
        }

        // 'Approved'/'Rejected' task should be available for initiator
        personManager.setUser(USER1);
        response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER1)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());

        // Correct task type check
        String taskType = results.getJSONObject(0).getString("name");
        if (approve)
        {
            assertEquals("wf:approvedTask", taskType);
        }
        else
        {
            assertEquals("wf:rejectedTask", taskType);
        }
        workflowService.cancelWorkflow(workflowId);
    }
    
    protected void runReviewPooledFlow(boolean approve) throws Exception
    {
        // Start workflow as USER1
        personManager.setUser(USER1);
        WorkflowDefinition reviewDef = workflowService.getDefinitionByName(getReviewPooledWorkflowDefinitionName());
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();

        // Reviewer is group GROUP
        params.put(WorkflowModel.ASSOC_GROUP_ASSIGNEE, groupManager.get(GROUP));
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        params.put(WorkflowModel.PROP_CONTEXT, packageRef);

        WorkflowPath reviewPath = workflowService.startWorkflow(reviewDef.getId(), params);
        String workflowId = reviewPath.getInstance().getId();
        workflows.add(workflowId);
        
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(reviewPath.getId()).get(0);

        // End start task
        startTask = workflowService.endTask(startTask.getId(), null);

        // Check if task is NOT available in list USER3, not a member of the
        // group
        personManager.setUser(USER3);
        Response response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER3)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(0, results.length());

        // Check if task is available in list of reviewer, member of GROUP:
        // USER2
        personManager.setUser(USER2);
        response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER2)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());

        // Check if task is claimable and pooled
        JSONObject taskJson = results.getJSONObject(0);
        String taskId = taskJson.getString("id");
        assertTrue(taskJson.getBoolean("isClaimable"));
        assertTrue(taskJson.getBoolean("isPooled"));

        // Claim task, using PUT, updating the owner
        JSONObject properties = new JSONObject();
        properties.put(qnameToString(ContentModel.PROP_OWNER), USER2);
        sendRequest(new PutRequest(URL_TASKS + "/" + taskId, properties.toString(), "application/json"), 200);

        // Check if task insn't claimable anymore
        response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER2)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());

        taskJson = results.getJSONObject(0);
        assertFalse(taskJson.getBoolean("isClaimable"));
        assertTrue(taskJson.getBoolean("isPooled"));

        // Delegate approval/rejection to implementing engine-test
        if (approve)
        {
            approveTask(taskId);
        }
        else
        {
            rejectTask(taskId);
        }

        // 'Approved'/'Rejected' task should be available for initiator
        personManager.setUser(USER1);
        response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER1)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertEquals(1, results.length());

        // Correct task type check
        String taskType = results.getJSONObject(0).getString("name");
        if (approve)
        {
            assertEquals("wf:approvedTask", taskType);
        }
        else
        {
            assertEquals("wf:rejectedTask", taskType);
        }
        workflowService.cancelWorkflow(workflowId);
    }

    protected abstract void approveTask(String taskId) throws Exception;

    protected abstract void rejectTask(String taskId) throws Exception;

    protected abstract String getAdhocWorkflowDefinitionName();
    
    protected abstract String getAdhocWorkflowDefinitionTitle();
    
    protected abstract String getAdhocWorkflowDefinitionDescription();
    
    protected abstract String getReviewWorkflowDefinitionName();
    
    protected abstract String getReviewPooledWorkflowDefinitionName();
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();

        namespaceService = (NamespaceService) appContext.getBean("NamespaceService");
        workflowService = (WorkflowService) appContext.getBean("WorkflowService");
        MutableAuthenticationService authenticationService = (MutableAuthenticationService) appContext.getBean("AuthenticationService");
        PersonService personService = (PersonService) appContext.getBean("PersonService");
        SearchService searchService = (SearchService) appContext.getBean("SearchService");
        FileFolderService fileFolderService = (FileFolderService) appContext.getBean("FileFolderService");
        nodeService = (NodeService) appContext.getBean("NodeService");
        
        AuthorityService authorityService = (AuthorityService) appContext.getBean("AuthorityService");
        personManager = new TestPersonManager(authenticationService, personService, nodeService);
        groupManager = new TestGroupManager(authorityService, searchService);

        authenticationComponent = (AuthenticationComponent) appContext.getBean("authenticationComponent");

        personManager.createPerson(USER1);
        personManager.createPerson(USER2);
        personManager.createPerson(USER3);

        authenticationComponent.setSystemUserAsCurrentUser();

        groupManager.addUserToGroup(GROUP, USER2);

        packageRef = workflowService.createPackage(null);

        NodeRef companyHome = searchService.selectNodes(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), COMPANY_HOME, null, namespaceService, false).get(0);

        contentNodeRef = fileFolderService.create(companyHome, TEST_CONTENT + System.currentTimeMillis(), ContentModel.TYPE_CONTENT).getNodeRef();

        authenticationComponent.clearCurrentSecurityContext();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        authenticationComponent.setSystemUserAsCurrentUser();
        for (String id: workflows)
        {
            try 
            {
                workflowService.cancelWorkflow(id);
            }
            catch (Throwable t)
            {
                // Do nothing
            }
        }
        groupManager.clearGroups();
        personManager.clearPeople();
        authenticationComponent.clearCurrentSecurityContext();
    }

    private String qnameToString(QName qName)
    {
        String separator = Character.toString(QName.NAMESPACE_PREFIX);

        return qName.toPrefixString(namespaceService).replaceFirst(separator, "_");
    }

    private void compareProperties(JSONObject before, JSONObject after) throws JSONException
    {
        for (String name : JSONObject.getNames(after))
        {
            if (before.has(name))
            {
                if (before.get(name) instanceof JSONArray)
                {
                    for (int i = 0; i < before.getJSONArray(name).length(); i++)
                    {
                        assertEquals(before.getJSONArray(name).get(i), after.getJSONArray(name).get(i));
                    }
                }
                else
                {
                    assertEquals(before.get(name), after.get(name));
                }
            }
        }
    }

    private void checkSimpleWorkflowInstanceResponse(JSONObject json) throws JSONException
    {
        assertTrue(json.has("id"));
        assertTrue(json.getString("id").length() > 0);

        assertTrue(json.has("url"));
        assertTrue(json.getString("url").startsWith(URL_WORKFLOW_INSTANCES));

        assertTrue(json.has("name"));
        assertTrue(json.getString("name").length() > 0);

        assertTrue(json.has("title"));
        assertTrue(json.getString("title").length() > 0);

        assertTrue(json.has("description"));
        assertTrue(json.getString("description").length() > 0);

        assertTrue(json.has("isActive"));

        assertTrue(json.has("startDate"));
        assertTrue(json.getString("startDate").length() > 0);

        assertTrue(json.has("endDate"));

        assertTrue(json.has("initiator"));
        Object initiator = json.get("initiator");
        if (!initiator.equals(JSONObject.NULL))
        {
            assertTrue(((JSONObject) initiator).has("userName"));
            assertTrue(((JSONObject) initiator).has("firstName"));
            assertTrue(((JSONObject) initiator).has("lastName"));
        }

        assertTrue(json.has("definitionUrl"));
        assertTrue(json.getString("definitionUrl").startsWith(URL_WORKFLOW_DEFINITIONS));
    }

    private void checkPriorityFiltering(String url) throws Exception 
    {
        JSONObject json = getDataFromRequest(url);
        JSONArray result = json.getJSONArray("data");
        assertNotNull(result);
        assertTrue(result.length() > 0);

        for (int i=0; i<result.length(); i++)
        {
            JSONObject taskObject = result.getJSONObject(i);
            assertEquals("2", taskObject.getJSONObject("properties").getString("bpm_priority"));
        }
    }
    
    private void checkTasksPresent(String url, boolean mustBePresent, String... ids) throws Exception 
    {
        List<String> taskIds = Arrays.asList(ids);
        JSONObject json = getDataFromRequest(url);
        JSONArray result = json.getJSONArray("data");
        assertNotNull(result);

        ArrayList<String> resultIds = new ArrayList<String>(result.length());
        for (int i=0; i<result.length(); i++)
        {
            JSONObject taskObject = result.getJSONObject(i);
            String taskId = taskObject.getString("id");
            resultIds.add(taskId);
            if (mustBePresent == false && taskIds.contains(taskId))
            {
                fail("The results should not contain id: "+taskId);
            }
        }
        if (mustBePresent && resultIds.containsAll(taskIds) == false)
        {
            fail("Not all task Ids were present!\nExpected: "+taskIds +"\nActual: "+resultIds); 
        }
    }
    
    private void checkTasksMatch(String url, String... ids) throws Exception 
    {
        List<String> taskIds = Arrays.asList(ids);
        JSONObject json = getDataFromRequest(url);
        JSONArray result = json.getJSONArray("data");
        assertNotNull(result);
        
        ArrayList<String> resultIds = new ArrayList<String>(result.length());
        for (int i=0; i<result.length(); i++)
        {
            JSONObject taskObject = result.getJSONObject(i);
            String taskId = taskObject.getString("id");
            resultIds.add(taskId);
        }
        assertTrue("Expected: "+taskIds +"\nActual: "+resultIds, resultIds.containsAll(taskIds));
        assertTrue("Expected: "+taskIds +"\nActual: "+resultIds, taskIds.containsAll(resultIds));
    }
    
    private void checkTasksState(String url, WorkflowTaskState expectedState) throws Exception 
    {
        JSONObject json = getDataFromRequest(url);
        JSONArray result = json.getJSONArray("data");
        assertNotNull(result);

        for (int i=0; i<result.length(); i++)
        {
            JSONObject taskObject = result.getJSONObject(i);
            String state = taskObject.getString("state");
            assertEquals(expectedState.toString(), state);
        }
    }

    private JSONObject getDataFromRequest(String url) throws Exception
    {
        Response response = sendRequest(new GetRequest(url), Status.STATUS_OK);
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        return json;
    }
    
    private void checkTaskPropertyFiltering(String propertiesParamValue, List<String> expectedProperties) throws Exception
    {
        JSONObject data = getDataFromRequest(MessageFormat.format(URL_USER_TASKS_PROPERTIES, USER2, propertiesParamValue));
        JSONArray taskArray = data.getJSONArray("data");
        assertEquals(1, taskArray.length());

        JSONObject taskProperties = taskArray.getJSONObject(0).getJSONObject("properties");
        assertNotNull(taskProperties);

        int expectedNumberOfProperties = 0;
        if(expectedProperties != null)
        {
            expectedNumberOfProperties = expectedProperties.size();
        }
        // Check right number of properties
        assertEquals(expectedNumberOfProperties, taskProperties.length());

        // Check if all properties are present
        if (expectedProperties != null)
        {
            for (String prop : expectedProperties)
            {
                assertTrue(taskProperties.has(prop));
            }
        }
    }
    
    private void checkFiltering(String url) throws Exception
    {
        JSONObject json = getDataFromRequest(url);
        JSONArray result = json.getJSONArray("data");
        assertNotNull(result);

        assertTrue(result.length() > 0);
    }
    
    private void checkPaging(String url, int totalItems, int maxItems, int skipCount) throws Exception
    {
        Response response = sendRequest(new GetRequest(url), 200);

        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        
        JSONArray data = json.getJSONArray("data");
        JSONObject paging = json.getJSONObject("paging");
        
        assertNotNull(data);
        assertNotNull(paging);

        assertTrue(data.length() >= 0);
        assertTrue(data.length() <= maxItems);
        
        assertEquals(totalItems, paging.getInt("totalItems"));
        assertEquals(maxItems, paging.getInt("maxItems"));
        assertEquals(skipCount, paging.getInt("skipCount"));
    }
}
