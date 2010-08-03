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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
    private static final String URL_WORKFLOW_DEFINITIONS = "api/workflow-definitions";
    private static final String URL_WORKFLOW_INSTANCES = "api/workflow-instances";
    
    private TestPersonManager personManager;
    private WorkflowService workflowService;
    private NamespaceService namespaceService;
    private NodeRef packageRef;
    
    public void testTaskInstancesGet() throws Exception
    {
        // Check USER2 starts with no tasks.
        personManager.setUser(USER2);
        Response response = sendRequest(new GetRequest(URL_TASKS), 200);
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
        params.put(WorkflowModel.PROP_DUE_DATE, new Date());
        params.put(WorkflowModel.PROP_PRIORITY, 1);
        params.put(WorkflowModel.ASSOC_PACKAGE, packageRef);
        
        WorkflowPath adhocPath = workflowService.startWorkflow(adhocDef.id, params);
        WorkflowTask startTask = workflowService.getTasksForWorkflowPath(adhocPath.id).get(0);
        workflowService.endTask(startTask.id, null);
        
        // Check USER2 now has one task.
        List<WorkflowTask> tasks = workflowService.getAssignedTasks(USER2, WorkflowTaskState.IN_PROGRESS);
        WorkflowTask task = tasks.get(0);
        
        personManager.setUser(USER2);
        response = sendRequest(new GetRequest(URL_TASKS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        jsonStr = response.getContentAsString();
        System.out.println(jsonStr);
        json = new JSONObject(jsonStr);
        results = json.getJSONArray("data");
        assertNotNull(results);
        assertTrue(results.length() == tasks.size());
        JSONObject result = results.getJSONObject(0);
        
        String expUrl = "api/task-instances/" + task.id;
        assertEquals(expUrl, result.getString("url"));
        assertEquals(task.name, result.getString("name"));
        assertEquals(task.title, result.getString("title"));
        assertEquals(task.description, result.getString("description"));
        assertEquals(task.state.name(), result.getString("state"));
        assertEquals(task.definition.metadata.getTitle(), result.getString("typeDefinitionTitle"));
        assertEquals(false, result.getBoolean("isPooled"));
        
        JSONObject owner = result.getJSONObject("owner");
        assertEquals(USER2, owner.getString("userName"));
        assertEquals(personManager.getFirstName(USER2), owner.getString("firstName"));
        assertEquals(personManager.getLastName(USER2), owner.getString("lastName"));
        
//        JSONObject properties = result.getJSONObject("properties");
        
        //TODO Add more tests to check property filtering and pooled actors.
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
        assertEquals( "api/workflow-paths/" + adhocPath.getId(), result.getString("path"));
        assertEquals(false, result.getBoolean("isPooled"));

        JSONObject owner = result.getJSONObject("owner");
        assertEquals(USER1, owner.getString("userName"));
        assertEquals(personManager.getFirstName(USER1), owner.getString("firstName"));
        assertEquals(personManager.getLastName(USER1), owner.getString("lastName"));

        JSONObject properties = result.getJSONObject("properties");

        assertNotNull(properties);
        
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
        
        Response getResponse = sendRequest(new GetRequest(URL_TASKS + "/" + startTask.id), 200);        
        
        JSONObject jsonProperties = new JSONObject(getResponse.getContentAsString()).getJSONObject("data").getJSONObject("properties");
        
        // make some changes        
        jsonProperties.remove(qnameToString(WorkflowModel.ASSOC_PACKAGE));
        jsonProperties.put(qnameToString(WorkflowModel.PROP_COMMENT), "Edited comment");
        jsonProperties.put(qnameToString(WorkflowModel.PROP_DUE_DATE), ISO8601DateFormat.format(new Date()));
        jsonProperties.put(qnameToString(WorkflowModel.PROP_DESCRIPTION), "Edited description");
        jsonProperties.put(qnameToString(WorkflowModel.PROP_PRIORITY), 1);
        
        personManager.setUser(USER3);
        Response unauthResponse = sendRequest(new PutRequest(URL_TASKS + "/" + startTask.id, jsonProperties.toString(), "application/json"), 401);
        assertEquals(Status.STATUS_UNAUTHORIZED, unauthResponse.getStatus());
        
        personManager.setUser(USER1);
        Response putResponse = sendRequest(new PutRequest(URL_TASKS + "/" + startTask.id, jsonProperties.toString(), "application/json"), 200);
        
        assertEquals(Status.STATUS_OK, putResponse.getStatus());
        String jsonStr = putResponse.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject result = json.getJSONObject("data");
        assertNotNull(result);
        
        JSONObject editedJsonProperties = result.getJSONObject("properties");
        
        compareProperties(jsonProperties, editedJsonProperties);
    }
    
    public void testWorkflowDefinitionsGet() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_WORKFLOW_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        JSONObject json = new JSONObject(response.getContentAsString());
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        
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
    }
    
    public void testWorkflowInstanceGet() throws Exception
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
        assertEquals(adhocInstance.definition.name, result.getString("name"));
        assertEquals(adhocInstance.definition.title, result.getString("title"));
        assertEquals(adhocInstance.definition.description, result.getString("description"));
        assertEquals(adhocInstance.active, result.getBoolean("isActive"));
        assertEquals(ISO8601DateFormat.format(adhocInstance.startDate), result.getString("startDate"));
        assertNotNull(result.getString("dueDate"));
        assertNotNull(result.getString("endDate"));
        assertEquals(2, result.getInt("priority"));
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
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();
        
        namespaceService = (NamespaceService)appContext.getBean("NamespaceService");
        workflowService = (WorkflowService)appContext.getBean("WorkflowService");
        MutableAuthenticationService authenticationService = (MutableAuthenticationService)appContext.getBean("AuthenticationService");
        PersonService personService = (PersonService)appContext.getBean("PersonService");
        NodeService nodeService = (NodeService)appContext.getBean("NodeService");
        personManager = new TestPersonManager(authenticationService, personService, nodeService);
        
        personManager.createPerson(USER1);
        personManager.createPerson(USER2);
        personManager.createPerson(USER3);
        
        packageRef = workflowService.createPackage(null);
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
}
