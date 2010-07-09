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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * @author Nick Smith
 *
 */
public class WorkflowRestApiTest extends BaseWebScriptTest
{
    private final static String USER1 = "Bob"+GUID.generate();
    private final static String USER2 = "Jane"+GUID.generate();
    private static final String URL_TASKS = "api/task-instance";
    private static final String URL_WORKFLOW_DEFINITIONS = "api/workflow-definitions";
    
    private TestPersonManager personManager;
    private WorkflowService workflowService;
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
        assertTrue(results.length()==0);
        
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
        assertTrue(results.length()==tasks.size());
        JSONObject result = results.getJSONObject(0);
        
        String expUrl = "api/task-instance/"+task.id;
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
        
        JSONObject properties = result.getJSONObject("properties");
        
        //TODO Add more tests to check property filtering and pooled actors.
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
            assertTrue(url.startsWith("api/workflow-definition/"));
            
            assertTrue(workflowDefinitionJSON.has("name"));
            assertTrue(workflowDefinitionJSON.getString("name").length() > 0);
            
            assertTrue(workflowDefinitionJSON.has("title"));
            assertTrue(workflowDefinitionJSON.getString("title").length() > 0);
            
            assertTrue(workflowDefinitionJSON.has("description"));
            assertTrue(workflowDefinitionJSON.getString("description").length() > 0);
        }
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();
        
        workflowService = (WorkflowService)appContext.getBean("WorkflowService");
        MutableAuthenticationService authenticationService = (MutableAuthenticationService)appContext.getBean("AuthenticationService");
        PersonService personService = (PersonService)appContext.getBean("PersonService");
        NodeService nodeService = (NodeService)appContext.getBean("NodeService");
        personManager = new TestPersonManager(authenticationService, personService, nodeService);
        
        personManager.createPerson(USER1);
        personManager.createPerson(USER2);
        
        packageRef =  workflowService.createPackage(null);
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
}
