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
package org.alfresco.repo.web.scripts.workflow;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
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
 *
 */
public class WorkflowRestApiTest extends BaseWebScriptTest
{
    private final static String USER1 = "Bob" + GUID.generate();
    private final static String USER2 = "Jane" + GUID.generate();
    private final static String USER3 = "Nick" + GUID.generate();
    private static final String URL_TASKS = "api/task-instances";
    private static final String URL_USER_TASKS = "api/task-instances?authority={0}";
    private static final String URL_WORKFLOW_TASKS = "api/workflow-instances/{0}/task-instances";
    private static final String URL_WORKFLOW_DEFINITIONS = "api/workflow-definitions";
    private static final String URL_WORKFLOW_INSTANCES = "api/workflow-instances";
    private static final String URL_WORKFLOW_INSTANCES_FOR_DEFINITION = "api/workflow-definitions/{0}/workflow-instances";
    private static final String URL_WORKFLOW_INSTANCES_FOR_NODE = "api/node/{0}/{1}/{2}/workflow-instances";

    private static final String COMPANY_HOME = "/app:company_home";
    private static final String TEST_CONTENT = "TestContent";

    private TestPersonManager personManager;
    private WorkflowService workflowService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private NodeRef packageRef;
    private NodeRef contentNodeRef;

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
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName("jbpm$wf:adhoc");
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.id, params);
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.id).get(0);
        workflowService.endTask(startTask.id, null);

        // Check USER2 now has one task.
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(USER2, WorkflowTaskState.IN_PROGRESS);
        WorkflowTask task = tasks.get(0);
        
        Map<QName, Serializable> updateParams = new HashMap<QName, Serializable>(1);
        updateParams.put(WorkflowModel.PROP_DUE_DATE, new Date());
        workflowService.updateTask(task.getId(), updateParams, null, null);

        personManager.setUser(USER2);
        response = sendRequest(new GetRequest(MessageFormat.format(URL_USER_TASKS, USER2)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertTrue(results.length() == tasks.size());
        JSONObject result = results.getJSONObject(0);
        
        int totalItems = results.length();

        String expUrl = "api/task-instances/" + task.id;
        assertEquals(expUrl, result.getString("url"));
        assertEquals(task.name, result.getString("name"));
        assertEquals(task.title, result.getString("title"));
        assertEquals(task.description, result.getString("description"));
        assertEquals(task.state.name(), result.getString("state"));
        assertEquals( "api/workflow-paths/" + adhocPath.getId(), result.getString("path"));
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

        // TODO: Add more tests to check property filtering and pooled actors.

        // filtering        
        checkFiltering(URL_TASKS + "?priority=2");

        //checkFiltering(URL_TASKS + "?dueAfter=" + ISO8601DateFormat.format(dueDate));

        //checkFiltering(URL_TASKS + "?dueBefore=" + ISO8601DateFormat.format(new Date()));
        
        // paging
        int maxItems = 3;        
        for (int skipCount = 0; skipCount < totalItems; skipCount += maxItems)
        {
            // one of this should test situation when skipCount + maxItems > totalItems
            checkPaging(MessageFormat.format(URL_USER_TASKS, USER2) + "&maxItems=" + maxItems + "&skipCount=" + skipCount, totalItems, maxItems, skipCount);
        }
        
        // testing when skipCount > totalItems
        checkPaging(MessageFormat.format(URL_USER_TASKS, USER2) + "&maxItems=" + maxItems + "&skipCount=" + (totalItems + 1), totalItems, maxItems, totalItems + 1);
        
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
        
        // retrieve tasks using the workflow instance
        String workflowInstanceId = adhocPath.getInstance().getId();
        response = sendRequest(new GetRequest(MessageFormat.format(URL_WORKFLOW_TASKS, workflowInstanceId)), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertTrue(results.length() > 0);
    }

    public void testTaskInstanceGet() throws Exception
    {
        //Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName("jbpm$wf:adhoc");
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.id, params);
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.id).get(0);

        Response response = sendRequest(new GetRequest(URL_TASKS + "/" + startTask.id), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);

        assertEquals(startTask.id, result.getString("id"));
        assertEquals(URL_TASKS + "/" + startTask.id, result.getString("url"));
        assertEquals(startTask.name, result.getString("name"));
        assertEquals(startTask.title, result.getString("title"));
        assertEquals(startTask.description, result.getString("description"));
        assertEquals(startTask.state.name(), result.getString("state"));
        assertEquals("api/workflow-paths/" + adhocPath.getId(), result.getString("path"));
        assertFalse(result.getBoolean("isPooled"));
        assertTrue(result.getBoolean("isEditable"));
        assertTrue(result.getBoolean("isReassignable"));
        assertFalse(result.getBoolean("isClaimable"));
        assertFalse(result.getBoolean("isReleasable"));

        JSONObject owner = result.getJSONObject("owner");
        assertEquals(USER1, owner.getString("userName"));
        assertEquals(personManager.getFirstName(USER1), owner.getString("firstName"));
        assertEquals(personManager.getLastName(USER1), owner.getString("lastName"));

        JSONObject properties = result.getJSONObject("properties");
        assertNotNull(properties);
        assertTrue(properties.has("bpm_priority"));
        assertTrue(properties.has("bpm_description"));
        assertTrue(properties.has("bpm_reassignable"));

        JSONObject instance = result.getJSONObject("workflowInstance");
        WorkflowInstance startInstance = startTask.path.instance;

        assertNotNull(instance);

        assertEquals(startInstance.id, instance.getString("id"));
        assertTrue(instance.has("url"));
        assertEquals(startInstance.definition.name, instance.getString("name"));
        assertEquals(startInstance.definition.title, instance.getString("title"));
        assertEquals(startInstance.definition.description, instance.getString("description"));
        assertEquals(startInstance.active, instance.getBoolean("isActive"));
        assertTrue(instance.has("startDate"));

        JSONObject initiator = instance.getJSONObject("initiator");

        assertEquals(USER1, initiator.getString("userName"));
        assertEquals(personManager.getFirstName(USER1), initiator.getString("firstName"));
        assertEquals(personManager.getLastName(USER1), initiator.getString("lastName"));

        JSONObject definition = result.getJSONObject("definition");
        WorkflowTaskDefinition startDefinitiont = startTask.definition;

        assertNotNull(definition);

        assertEquals(startDefinitiont.id, definition.getString("id"));
        assertTrue(definition.has("url"));

        JSONObject type = definition.getJSONObject("type");
        TypeDefinition startType = startDefinitiont.metadata;

        assertNotNull(type);

        assertEquals(startType.getName().toPrefixString(), type.getString("name"));
        assertEquals(startType.getTitle(), type.getString("title"));
        assertEquals(startType.getDescription(), type.getString("description"));
        assertTrue(type.has("url"));

        JSONObject node = definition.getJSONObject("node");
        WorkflowNode startNode = startDefinitiont.node;

        assertNotNull(node);

        assertEquals(startNode.name, node.getString("name"));
        assertEquals(startNode.title, node.getString("title"));
        assertEquals(startNode.description, node.getString("description"));
        assertEquals(startNode.isTaskNode, node.getBoolean("isTaskNode"));

        JSONArray transitions = node.getJSONArray("transitions");
        WorkflowTransition[] startTransitions = startNode.transitions;

        assertNotNull(transitions);

        assertEquals(startTransitions.length, transitions.length());

        for (int i = 0; i < transitions.length(); i++)
        {
            JSONObject transition = transitions.getJSONObject(i);
            WorkflowTransition startTransition = startTransitions[i];

            assertNotNull(transition);

            assertEquals(startTransition.id, transition.getString("id"));
            assertEquals(startTransition.title, transition.getString("title"));
            assertEquals(startTransition.description, transition.getString("description"));
            assertEquals(startTransition.isDefault, transition.getBoolean("isDefault"));
            assertTrue(transition.has("isHidden"));
        }

    }

    public void testTaskInstancePut() throws Exception
    {
        // Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName("jbpm$wf:adhoc");
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.id, params);
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.id).get(0);

        Response getResponse = sendRequest(new GetRequest(URL_TASKS + "/" + startTask.id), 200);

        JSONObject jsonProperties = new JSONObject(getResponse.getContentAsString()).getJSONObject("data").getJSONObject("properties");

        // make some changes        
        jsonProperties.remove(qnameToString(WorkflowModel.ASSOC_PACKAGE));
        jsonProperties.put(qnameToString(WorkflowModel.PROP_COMMENT), "Edited comment");
        jsonProperties.put(qnameToString(WorkflowModel.PROP_DUE_DATE), ISO8601DateFormat.format(new Date()));
        jsonProperties.put(qnameToString(WorkflowModel.PROP_DESCRIPTION), "Edited description");
        jsonProperties.put(qnameToString(WorkflowModel.PROP_PRIORITY), 1);

        // test USER3 can not update the task
        personManager.setUser(USER3);
        Response unauthResponse = sendRequest(new PutRequest(URL_TASKS + "/" + startTask.id, jsonProperties.toString(), "application/json"), 401);
        assertEquals(Status.STATUS_UNAUTHORIZED, unauthResponse.getStatus());

        // test USER1 (the task owner) can update the task
        personManager.setUser(USER1);
        Response putResponse = sendRequest(new PutRequest(URL_TASKS + "/" + startTask.id, jsonProperties.toString(), "application/json"), 200);

        assertEquals(Status.STATUS_OK, putResponse.getStatus());
        String jsonStr = putResponse.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);

        JSONObject editedJsonProperties = result.getJSONObject("properties");

        compareProperties(jsonProperties, editedJsonProperties);

        // get the next task where USER2 is the owner
        workflowService.endTask(startTask.id, null);
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(adhocPath.getInstance().getId());
        WorkflowTask nextTask = workflowService.getTasksForWorkflowPath(paths.get(0).getId()).get(0);

        // make sure USER1 (the workflow initiator) can update
        putResponse = sendRequest(new PutRequest(URL_TASKS + "/" + nextTask.id, jsonProperties.toString(), "application/json"), 200);
    }

    public void testWorkflowDefinitionsGet() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_WORKFLOW_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        JSONObject json = new JSONObject(response.getContentAsString());
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        assertTrue(results.length() > 0);

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
            assertTrue(workflowDefinitionJSON.getString("title").length() > 0);

            assertTrue(workflowDefinitionJSON.has("description"));
            assertTrue(workflowDefinitionJSON.getString("description").length() > 0);
        }
        
        // filter the workflow definitions and check they are not returned
        String exclude = "jbpm$wf:adhoc";
        response = sendRequest(new GetRequest(URL_WORKFLOW_DEFINITIONS + "?exclude=" + exclude), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        json = new JSONObject(response.getContentAsString());
        results = json.getJSONArray("data");
        assertNotNull(results);
        
        boolean adhocWorkflowPresent = false;
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
        exclude = "jbpm$wf:adhoc, jbpm$wcmwf:*";
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
            if (name.equals("jbpm$wf:adhoc"))
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

    public void testWorkflowInstanceGet() throws Exception
    {
        //Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName("jbpm$wf:adhoc");
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        params.put(WorkflowModel.PROP_CONTEXT, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.id, params);
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.id).get(0);
        startTask = workflowService.endTask(startTask.id, null);

        WorkflowInstance adhocInstance = startTask.path.instance;

        Response response = sendRequest(new GetRequest(URL_WORKFLOW_INSTANCES + "/" + adhocInstance.id + "?includeTasks=true"), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);

        assertEquals(adhocInstance.id, result.getString("id"));
        assertTrue(result.opt("message").equals(JSONObject.NULL));
        assertEquals(adhocInstance.definition.name, result.getString("name"));
        assertEquals(adhocInstance.definition.title, result.getString("title"));
        assertEquals(adhocInstance.definition.description, result.getString("description"));
        assertEquals(adhocInstance.active, result.getBoolean("isActive"));
        assertEquals(ISO8601DateFormat.format(adhocInstance.startDate), result.getString("startDate"));
        assertNotNull(result.getString("dueDate"));
        assertNotNull(result.getString("endDate"));
        assertEquals(1, result.getInt("priority"));
        JSONObject initiator = result.getJSONObject("initiator");

        assertEquals(USER1, initiator.getString("userName"));
        assertEquals(personManager.getFirstName(USER1), initiator.getString("firstName"));
        assertEquals(personManager.getLastName(USER1), initiator.getString("lastName"));

        assertEquals(adhocInstance.context.toString(), result.getString("context"));
        assertEquals(adhocInstance.workflowPackage.toString(), result.getString("package"));
        assertNotNull(result.getString("startTaskInstanceId"));

        JSONObject jsonDefinition = result.getJSONObject("definition");
        WorkflowDefinition adhocDefinition = adhocInstance.definition;

        assertNotNull(jsonDefinition);

        assertEquals(adhocDefinition.id, jsonDefinition.getString("id"));
        assertEquals(adhocDefinition.name, jsonDefinition.getString("name"));
        assertEquals(adhocDefinition.title, jsonDefinition.getString("title"));
        assertEquals(adhocDefinition.description, jsonDefinition.getString("description"));
        assertEquals(adhocDefinition.version, jsonDefinition.getString("version"));
        assertEquals(adhocDefinition.getStartTaskDefinition().metadata.getName().toPrefixString(namespaceService), jsonDefinition.getString("startTaskDefinitionType"));
        assertTrue(jsonDefinition.has("taskDefinitions"));

        JSONArray tasks = result.getJSONArray("tasks");
        assertTrue(tasks.length() > 1);
    }

    @SuppressWarnings("deprecation")
    public void testWorkflowInstancesGet() throws Exception
    {
        //Start workflow as USER1 and assign task to USER2.
        personManager.setUser(USER1);
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName("jbpm$wf:adhoc");
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        Date dueDate = new Date();
        params.put(WorkflowModel.PROP_WORKFLOW_DUE_DATE, dueDate);
        params.put(WorkflowModel.PROP_WORKFLOW_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        params.put(WorkflowModel.PROP_CONTEXT, packageRef);

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.getId(), params);
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
        Date anHourAgo = new Date(dueDate.getTime());
        anHourAgo.setHours(anHourAgo.getHours()-1);

        // filter by initiator
        checkFiltering(URL_WORKFLOW_INSTANCES + "?initiator=" + USER1);

        // filter by startedAfter
        checkFiltering(URL_WORKFLOW_INSTANCES + "?startedAfter=" + ISO8601DateFormat.format(anHourAgo));

        // filter by startedBefore
        checkFiltering(URL_WORKFLOW_INSTANCES + "?startedBefore=" + ISO8601DateFormat.format(adhocInstance.getStartDate()));

        // filter by dueAfter
        checkFiltering(URL_WORKFLOW_INSTANCES + "?dueAfter=" + ISO8601DateFormat.format(anHourAgo));

        // filter by dueBefore
        checkFiltering(URL_WORKFLOW_INSTANCES + "?dueBefore=" + ISO8601DateFormat.format(dueDate));

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
        checkFiltering(URL_WORKFLOW_INSTANCES + "?definitionName=jbpm$wf:adhoc");
        
        // paging
        int maxItems = 3;        
        for (int skipCount = 0; skipCount < totalItems; skipCount += maxItems)
        {
            checkPaging(URL_WORKFLOW_INSTANCES + "?maxItems=" + maxItems + "&skipCount=" + skipCount, totalItems, maxItems, skipCount);
        }
        
        // check the exclude filtering
        String exclude = "jbpm$wf:adhoc";
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
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName("jbpm$wf:adhoc");
        Map<QName, Serializable> params = new HashMap<QName, Serializable>();
        params.put(WorkflowModel.ASSOC_ASSIGNEE, personManager.get(USER2));
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);

        nodeService.addChild(packageRef, contentNodeRef, 
                WorkflowModel.ASSOC_PACKAGE_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                QName.createValidLocalName((String)nodeService.getProperty(
                        contentNodeRef, ContentModel.PROP_NAME))));

        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.id, params);

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
        WorkflowDefinition adhocDef = workflowService.getDefinitionByName("jbpm$wf:adhoc");
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
        Response unauthResponse = sendRequest(new DeleteRequest(URL_WORKFLOW_INSTANCES + "/" + adhocInstance.getId()), 403);
        assertEquals(Status.STATUS_FORBIDDEN, unauthResponse.getStatus());
        
        // make sure workflow instance is still present
        assertNotNull(workflowService.getWorkflowById(adhocInstance.getId()));

        // now delete as initiator of workflow
        personManager.setUser(USER1);
        Response response = sendRequest(new DeleteRequest(URL_WORKFLOW_INSTANCES + "/" + adhocInstance.getId()), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());

        assertNull(workflowService.getWorkflowById(adhocInstance.getId()));
    }

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
        personManager = new TestPersonManager(authenticationService, personService, nodeService);

        personManager.createPerson(USER1);
        personManager.createPerson(USER2);
        personManager.createPerson(USER3);

        packageRef = workflowService.createPackage(null);

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        NodeRef companyHome = searchService.selectNodes(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), COMPANY_HOME, null, namespaceService, false).get(0);

        contentNodeRef = fileFolderService.create(companyHome, TEST_CONTENT + System.currentTimeMillis(), ContentModel.TYPE_CONTENT).getNodeRef();

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        personManager.clearPeople();
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

    private void checkFiltering(String url) throws Exception
    {
        Response response = sendRequest(new GetRequest(url), 200);

        assertEquals(Status.STATUS_OK, response.getStatus());
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
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
