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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;

/**
 * @since 3.4
 * @author Nick Smith
 */
public class WorkflowModelBuilderTest extends TestCase
{
    private static final String URI = "http://test";
    private static final String userName = "joeBloggs";
    private static final String firstName = "Joe";
    private static final String lastName = "Bloggs";
    private static final NodeRef person = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, userName);
    
    private NamespaceService namespaceService;
    private PersonService personService;
    private NodeService nodeService;
    private WorkflowModelBuilder builder;

    @SuppressWarnings("unchecked")
    public void testBuildWorkflowTask() throws Exception
    {
        Date date = new Date();
        WorkflowTask task = makeTask(date);
        
        Map<String, Object> model = builder.buildSimple(task, null);
        Object id = model.get(WorkflowModelBuilder.TASK_ID);
        assertEquals(task.id, id);
        Object url = model.get(WorkflowModelBuilder.TASK_URL);
        assertEquals("api/task-instances/" + task.getId(), url);
        assertEquals(task.getName(), model.get(WorkflowModelBuilder.TASK_NAME));
        assertEquals(task.getTitle(), model.get(WorkflowModelBuilder.TASK_TITLE));
        assertEquals(task.getDescription(), model.get(WorkflowModelBuilder.TASK_DESCRIPTION));
        assertEquals(task.getState().name(), model.get(WorkflowModelBuilder.TASK_STATE));
        assertEquals(task.getDefinition().getMetadata().getTitle(), model.get(WorkflowModelBuilder.TASK_TYPE_DEFINITION_TITLE));
        assertEquals(false, model.get(WorkflowModelBuilder.TASK_IS_POOLED));
        
        Map<String, Object> owner = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_OWNER);
        assertEquals(userName, owner.get(WorkflowModelBuilder.PERSON_USER_NAME));
        assertEquals(firstName, owner.get(WorkflowModelBuilder.PERSON_FIRST_NAME));
        assertEquals(lastName, owner.get(WorkflowModelBuilder.PERSON_LAST_NAME));
        
        Map<String, Object> props = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_PROPERTIES);
        assertEquals(task.getProperties().size(), props.size());
        assertEquals(5, props.get("test_int"));
        assertEquals(false, props.get("test_boolean"));
        assertEquals("foo bar", props.get("test_string"));
        String dateStr = (String) props.get("test_date");
        assertEquals(date, ISO8601DateFormat.parse(dateStr));
        
        task.getProperties().put(WorkflowModel.ASSOC_POOLED_ACTORS, new ArrayList<NodeRef>(0));
        model = builder.buildSimple(task, null);
        assertEquals(false, model.get(WorkflowModelBuilder.TASK_IS_POOLED));
        
        ArrayList<NodeRef> actors = new ArrayList<NodeRef>(1);
        actors.add(person);
        task.getProperties().put(WorkflowModel.ASSOC_POOLED_ACTORS, actors);
        model = builder.buildSimple(task, null);
        assertEquals(true, model.get(WorkflowModelBuilder.TASK_IS_POOLED));

        model = builder.buildSimple(task, Arrays.asList("test_int", "test_string"));
        //Check task owner still created properly.
        owner = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_OWNER);
        assertEquals(userName, owner.get(WorkflowModelBuilder.PERSON_USER_NAME));

        // Check properties populated correctly
        props = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_PROPERTIES);
        assertEquals(2, props.size());
        assertEquals(5, props.get("test_int"));
        assertEquals("foo bar", props.get("test_string"));
    }

    @SuppressWarnings("unchecked")
    public void testBuildWorkflowTaskDetailed() throws Exception
    {
        Date date = new Date();
        WorkflowTask workflowTask = makeTask(date);
        
        Map<String, Object> model = builder.buildDetailed(workflowTask);

        Object id = model.get(WorkflowModelBuilder.TASK_ID);
        assertEquals(workflowTask.getId(), id);
        Object url = model.get(WorkflowModelBuilder.TASK_URL);
        assertEquals("api/task-instances/" + workflowTask.id, url);
        assertEquals(workflowTask.getName(), model.get(WorkflowModelBuilder.TASK_NAME));
        assertEquals(workflowTask.getTitle(), model.get(WorkflowModelBuilder.TASK_TITLE));
        assertEquals(workflowTask.getDescription(), model.get(WorkflowModelBuilder.TASK_DESCRIPTION));
        assertEquals(workflowTask.getState().name(), model.get(WorkflowModelBuilder.TASK_STATE));
        assertEquals(false, model.get(WorkflowModelBuilder.TASK_IS_POOLED));

        Map<String, Object> owner = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_OWNER);
        assertEquals(userName, owner.get(WorkflowModelBuilder.PERSON_USER_NAME));
        assertEquals(firstName, owner.get(WorkflowModelBuilder.PERSON_FIRST_NAME));
        assertEquals(lastName, owner.get(WorkflowModelBuilder.PERSON_LAST_NAME));

        Map<String, Object> props = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_PROPERTIES);
        assertEquals(workflowTask.getProperties().size(), props.size());

        Map<String, Object> workflowInstance = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE);
        
        WorkflowInstance instance = workflowTask.getPath().getInstance();
        assertEquals(instance.getId(), workflowInstance.get(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_ID));
        assertEquals(instance.isActive(), workflowInstance.get(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_IS_ACTIVE));
        String startDateStr = ISO8601DateFormat.format(instance.getStartDate());
        assertEquals(startDateStr, workflowInstance.get(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_START_DATE));        
        
        WorkflowDefinition workflowDef = instance.getDefinition();
        assertEquals(workflowDef.getName(), workflowInstance.get(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_NAME));
        assertEquals(workflowDef.getTitle(), workflowInstance.get(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_TITLE));
        assertEquals(workflowDef.getDescription(), workflowInstance.get(WorkflowModelBuilder.TASK_WORKFLOW_INSTANCE_DESCRIPTION));
        
        Map<String, Object> actualDefinition = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_DEFINITION);
        WorkflowTaskDefinition taskDef = workflowTask.getDefinition();
        assertEquals(taskDef.getId(), actualDefinition.get(WorkflowModelBuilder.TASK_DEFINITION_ID));

        Map<String, Object> actualType = (Map<String, Object>) actualDefinition.get(WorkflowModelBuilder.TASK_DEFINITION_TYPE);
        TypeDefinition taskType = taskDef.getMetadata();
        assertEquals(taskType.getName(), actualType.get(WorkflowModelBuilder.TYPE_DEFINITION_NAME));
        assertEquals(taskType.getTitle(), actualType.get(WorkflowModelBuilder.TYPE_DEFINITION_TITLE));
        assertEquals(taskType.getDescription(), actualType.get(WorkflowModelBuilder.TYPE_DEFINITION_DESCRIPTION));

        Map<String, Object> actualNode = (Map<String, Object>) actualDefinition.get(WorkflowModelBuilder.TASK_DEFINITION_NODE);
        WorkflowNode taskNode = taskDef.getNode();
        assertEquals(taskNode.getName(), actualNode.get(WorkflowModelBuilder.WORKFLOW_NODE_NAME));
        assertEquals(taskNode.getTitle(), actualNode.get(WorkflowModelBuilder.WORKFLOW_NODE_TITLE));
        assertEquals(taskNode.getDescription(), actualNode.get(WorkflowModelBuilder.WORKFLOW_NODE_DESCRIPTION));
        assertEquals(taskNode.isTaskNode(), actualNode.get(WorkflowModelBuilder.WORKFLOW_NODE_IS_TASK_NODE));

        List<Map<String, Object>> transitions = (List<Map<String, Object>>) actualNode.get(WorkflowModelBuilder.WORKFLOW_NODE_TRANSITIONS);

        WorkflowTransition[] taskTransitions = taskNode.getTransitions();
        int i = 0;
        for (Map<String, Object> transition : transitions)
        {
            WorkflowTransition workflowTransition = taskTransitions[i];
            assertEquals(workflowTransition.getId(), transition.get(WorkflowModelBuilder.WORKFLOW_NODE_TRANSITION_ID));
            assertEquals(workflowTransition.getTitle(), transition.get(WorkflowModelBuilder.WORKFLOW_NODE_TRANSITION_TITLE));
            assertEquals(workflowTransition.getDescription(), transition.get(WorkflowModelBuilder.WORKFLOW_NODE_TRANSITION_DESCRIPTION));
            assertEquals(workflowTransition.isDefault(), transition.get(WorkflowModelBuilder.WORKFLOW_NODE_TRANSITION_IS_DEFAULT));
            assertEquals(false, transition.get(WorkflowModelBuilder.WORKFLOW_NODE_TRANSITION_IS_HIDDEN));
            i++;
        }
    }
    
    public void testBuildWorkflowDefinition() throws Exception
    {
        WorkflowTaskDefinition workflowTaskDefinition = new WorkflowTaskDefinition();
        WorkflowDefinition workflowDefinition = new WorkflowDefinition("The Id", "The Name", "The Version", "The Title", "The Description", workflowTaskDefinition);
        
        Map<String, Object> model = builder.buildSimple(workflowDefinition);
        assertEquals(workflowDefinition.id, model.get(WorkflowModelBuilder.WORKFLOW_DEFINITION_ID));
        assertEquals("api/workflow-definitions/" + workflowDefinition.id, model.get(WorkflowModelBuilder.WORKFLOW_DEFINITION_URL));
        assertEquals(workflowDefinition.name, model.get(WorkflowModelBuilder.WORKFLOW_DEFINITION_NAME));
        assertEquals(workflowDefinition.title, model.get(WorkflowModelBuilder.WORKFLOW_DEFINITION_TITLE));
        assertEquals(workflowDefinition.description, model.get(WorkflowModelBuilder.WORKFLOW_DEFINITION_DESCRIPTION));
    }
    
    private WorkflowNode makeNode()
    {
        WorkflowNode node = new WorkflowNode();
        node.name = "The Node Name";
        node.title = "The Node Title";
        node.description = "The Node Description";
        node.isTaskNode = true;
        
        WorkflowTransition workflowTransition = makeTransition();
        node.transitions = new WorkflowTransition[] { workflowTransition };
        return node;
    }

    private WorkflowTransition makeTransition()
    {
        WorkflowTransition workflowTransition = new WorkflowTransition();
        workflowTransition.id = "The Transition Id";
        workflowTransition.title = "The Transition Title";
        workflowTransition.description = "The Transition Description";
        workflowTransition.isDefault = true;
        return workflowTransition;
    }

    private WorkflowTaskDefinition makeTaskDefinition()
    {
        WorkflowTaskDefinition definition = new WorkflowTaskDefinition();
        definition.id = "The Definition Id";
        definition.metadata = makeTypeDefinition();
        definition.node = makeNode();
        return definition;
    }

    private TypeDefinition makeTypeDefinition()
    {
        TypeDefinition typeDef = mock(TypeDefinition.class);
        when(typeDef.getName()).thenReturn(QName.createQName("The Type Name"));
        when(typeDef.getTitle()).thenReturn("The Type Title");
        when(typeDef.getDescription()).thenReturn("The Type Description");
        return typeDef;
    }

    private WorkflowPath makePath()
    {
        WorkflowPath path = new WorkflowPath();
        path.id = "pathId$1";
        path.instance = new WorkflowInstance();
        path.instance.id = "";
        path.instance.active = true;
        path.instance.startDate = new Date();
        path.instance.definition = new WorkflowDefinition(
                "The Id", "The Name", "1", "The Title", "The Description", null);
        return path;
    }
    
    private WorkflowTask makeTask(Date date)
    {
        WorkflowTask task = new WorkflowTask();
        task.description = "Task Desc";
        task.id = "testId$1";
        task.name = "Task Name";
        task.state = WorkflowTaskState.IN_PROGRESS;
        task.title = "Task Title";
        task.path = makePath();
        task.definition = makeTaskDefinition();
        task.properties = makeTaskProperties(date);
        return task;
    }

    private HashMap<QName, Serializable> makeTaskProperties(Date date)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_OWNER, userName);
        QName testInt = QName.createQName(URI, "int");
        properties.put(testInt, 5);
        QName testBoolean = QName.createQName(URI, "boolean");
        properties.put(testBoolean, false);
        QName testString = QName.createQName(URI, "string");
        properties.put(testString, "foo bar");
        QName testDate = QName.createQName(URI, "date");
        properties.put(testDate, date);
        return properties;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        namespaceService = new NamespaceServiceMemoryImpl();
        namespaceService.registerNamespace("test", URI);
        namespaceService.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX, NamespaceService.CONTENT_MODEL_1_0_URI);
        namespaceService.registerNamespace(NamespaceService.BPM_MODEL_PREFIX, NamespaceService.BPM_MODEL_1_0_URI);
        
        personService = mock(PersonService.class);
        when(personService.getPerson(userName)).thenReturn(person);
        
        nodeService = mock(NodeService.class);
        Map<QName, Serializable> personProps = new HashMap<QName, Serializable>();
        personProps.put(ContentModel.PROP_USERNAME, userName);
        personProps.put(ContentModel.PROP_FIRSTNAME, firstName);
        personProps.put(ContentModel.PROP_LASTNAME, lastName);
        when(nodeService.getProperties(person)).thenReturn(personProps);
        
        builder = new WorkflowModelBuilder(namespaceService, nodeService, personService);
    }
}
