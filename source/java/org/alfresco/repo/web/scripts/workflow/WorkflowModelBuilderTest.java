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
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;

/**
 * @author Nick Smith
 *
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
        WorkflowTask task = new WorkflowTask();
        task.definition = new WorkflowTaskDefinition();
        task.definition.metadata = mock(TypeDefinition.class);
        when(task.definition.metadata.getTitle()).thenReturn("Type Title");
        task.description = "Task Desc";
        task.id = "testId$1";
        task.name = "Task Name";
        task.state = WorkflowTaskState.IN_PROGRESS;
        task.title = "Task Title";
        task.properties = new HashMap<QName, Serializable>();
        task.properties.put(ContentModel.PROP_OWNER, userName);
        
        QName testInt = QName.createQName(URI, "int");
        task.properties.put(testInt, 5);
        QName testBoolean = QName.createQName(URI, "boolean");
        task.properties.put(testBoolean, false);
        QName testString = QName.createQName(URI, "string");
        task.properties.put(testString, "foo bar");
        QName testDate = QName.createQName(URI, "date");
        Date date = new Date();
        task.properties.put(testDate, date);
        
        Map<String, Object> model = builder.buildSimple(task, null);
        Object id = model.get(WorkflowModelBuilder.TASK_ID);
        assertEquals(task.id, id);
        Object url = model.get(WorkflowModelBuilder.TASK_URL);
        assertEquals("api/task-instance/"+task.id, url);
        assertEquals(task.name, model.get(WorkflowModelBuilder.TASK_NAME));
        assertEquals(task.title, model.get(WorkflowModelBuilder.TASK_TITLE));
        assertEquals(task.description, model.get(WorkflowModelBuilder.TASK_DESCRIPTION));
        assertEquals(task.state.name(), model.get(WorkflowModelBuilder.TASK_STATE));
        assertEquals(task.definition.metadata.getTitle(), model.get(WorkflowModelBuilder.TASK_TYPE_DEFINITION_TITLE));
        assertEquals(false, model.get(WorkflowModelBuilder.TASK_IS_POOLED));
        
        Map<String, Object> owner = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_OWNER);
        assertEquals( userName, owner.get(WorkflowModelBuilder.PERSON_USER_NAME));
        assertEquals( firstName, owner.get(WorkflowModelBuilder.PERSON_FIRST_NAME));
        assertEquals( lastName, owner.get(WorkflowModelBuilder.PERSON_LAST_NAME));
        
        Map<String, Object> props = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_PROPERTIES);
        assertEquals(task.properties.size(), props.size());
        assertEquals(5, props.get("test_int"));
        assertEquals(false, props.get("test_boolean"));
        assertEquals("foo bar", props.get("test_string"));
        String dateStr = (String) props.get("test_date");
        assertEquals(date, ISO8601DateFormat.parse(dateStr));
        
        task.properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, new ArrayList<NodeRef>(0));
        model = builder.buildSimple(task, null);
        assertEquals(false, model.get(WorkflowModelBuilder.TASK_IS_POOLED));
        
        ArrayList<NodeRef> actors = new ArrayList<NodeRef>(1);
        actors.add(person);
        task.properties.put(WorkflowModel.ASSOC_POOLED_ACTORS, actors);
        model = builder.buildSimple(task, null);
        assertEquals(true, model.get(WorkflowModelBuilder.TASK_IS_POOLED));

        model = builder.buildSimple(task, Arrays.asList("test_int", "test_string"));
        //Check task owner still created properly.
        owner = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_OWNER);
        assertEquals( userName, owner.get(WorkflowModelBuilder.PERSON_USER_NAME));

        // Check properties populated correctly
        props = (Map<String, Object>) model.get(WorkflowModelBuilder.TASK_PROPERTIES);
        assertEquals(2, props.size());
        assertEquals(5, props.get("test_int"));
        assertEquals("foo bar", props.get("test_string"));
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
