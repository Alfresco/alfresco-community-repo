/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.forms.processor.workflow;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.node.DefaultFieldProcessor;
import org.alfresco.repo.forms.processor.node.FormFieldConstants;
import org.alfresco.repo.forms.processor.node.MockClassAttributeDefinition;
import org.alfresco.repo.forms.processor.node.MockFieldProcessorRegistry;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;
import org.alfresco.service.namespace.QName;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author Nick Smith
 */
public class TaskFormProcessorTest extends TestCase
{
    private static final String TASK_DEF_NAME = "TaskDef";
    private static final String TASK_ID = "foo$Real Id";
    private static final QName DESC_NAME = WorkflowModel.PROP_DESCRIPTION;
    private static final QName STATUS_NAME = WorkflowModel.PROP_STATUS;
    private static final QName PROP_WITH_ = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "some_prop");
    private static final QName ACTORS_NAME = WorkflowModel.ASSOC_POOLED_ACTORS;
    private static final QName ASSIGNEE_NAME = WorkflowModel.ASSOC_ASSIGNEE;
    private static final QName ASSOC_WITH_ = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "some_assoc");
    private static final NodeRef FAKE_NODE = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakeNode");
    private static final NodeRef PCKG_NODE = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakePackage");

    private WorkflowService workflowService;
    private NodeService nodeService;
    private TaskFormProcessor processor;
    private WorkflowTask task;
    private NamespaceService namespaceService;
    private WorkflowTask newTask;
    private Map<QName, Serializable> actualProperties = null;
    private Map<QName, List<NodeRef>> actualAdded= null;
    private Map<QName, List<NodeRef>> actualRemoved= null;
    
    public void testGetTypedItem() throws Exception
    {
        try
        {
            processor.getTypedItem(null);
            fail("Should have thrown an Exception here!");
        }
        catch (FormNotFoundException e)
        {
            // Do nothing!
        }

        try
        {
            processor.getTypedItem(new Item("task", "bad id"));
            fail("Should have thrown an Exception here!");
        }
        catch (FormNotFoundException e)
        {
            // Do nothing!
        }

        Item item = new Item("task", TASK_ID);
        WorkflowTask result = processor.getTypedItem(item);
        assertNotNull(result);
        assertEquals(TASK_ID, result.id);
        
        // Check URI-encoded id.
        item = new Item("task", TASK_ID.replace('$', '_'));
        result = processor.getTypedItem(item);
        assertNotNull(result);
        assertEquals(TASK_ID, result.id);
    }

    public void testGenerateSetsItemAndUrl() throws Exception
    {
        Item item = new Item("task", TASK_ID);
        Form form = processor.generate(item, null, null, null);
        Item formItem = form.getItem();
        assertEquals(item.getId(), formItem.getId());
        assertEquals(item.getKind(), formItem.getKind());
        String expType = NamespaceService.BPM_MODEL_PREFIX + ":" + TASK_DEF_NAME;
        assertEquals(expType, formItem.getType());
        assertEquals("/api/task-instances/" + TASK_ID, formItem.getUrl());
    }

    public void testGenerateSingleProperty()
    {
        // Check Status field is added to Form.
        String fieldName = STATUS_NAME.toPrefixString(namespaceService);
        List<String> fields = Arrays.asList(fieldName);
        Form form = processForm(fields);
        checkSingleProperty(form, fieldName, WorkflowTaskState.IN_PROGRESS);

        // Check Status field is added to Form, when explicitly typed as a
        // property.
        String fullPropertyName = "prop:" + fieldName;
        fields = Arrays.asList(fullPropertyName);
        form = processForm(fields);
        checkSingleProperty(form, fieldName, WorkflowTaskState.IN_PROGRESS);
        checkPackageActionGroups(form.getFormData());
    }

    public void testGenerateSingleAssociation()
    {
        // Check Assignee field is added to Form.
        String fieldName = ASSIGNEE_NAME.toPrefixString(namespaceService);
        List<String> fields = Arrays.asList(fieldName);
        Form form = processForm(fields);
        Serializable fieldData = (Serializable) Arrays.asList(FAKE_NODE.toString());
        checkSingleAssociation(form, fieldName, fieldData);

        // Check Assignee field is added to Form, when explicitly typed as an
        // association.
        String fullAssociationName = "assoc:" + fieldName;
        fields = Arrays.asList(fullAssociationName);
        form = processForm(fields);
        checkSingleAssociation(form, fieldName, fieldData);
        checkPackageActionGroups(form.getFormData());
    }

    public void testIgnoresUnknownFields() throws Exception
    {
        String fakeFieldName = NamespaceService.BPM_MODEL_PREFIX + ":" + "Fake Field";
        String statusFieldName = STATUS_NAME.toPrefixString(namespaceService);
        List<String> fields = Arrays.asList(fakeFieldName, statusFieldName);
        Form form = processForm(fields);
        checkSingleProperty(form, statusFieldName, WorkflowTaskState.IN_PROGRESS);
        checkPackageActionGroups(form.getFormData());
    }

    public void testGenerateDefaultForm() throws Exception
    {
        Form form = processForm(null);
        List<String> fieldDefs = form.getFieldDefinitionNames();
        assertEquals(8, fieldDefs.size());
        assertTrue(fieldDefs.contains(ASSIGNEE_NAME.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(ACTORS_NAME.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(DESC_NAME.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(STATUS_NAME.toPrefixString(namespaceService)));

        Serializable fieldData = (Serializable) Arrays.asList(FAKE_NODE.toString());
        FormData formData = form.getFormData();
        assertEquals(6, formData.getNumberOfFields());
        assertEquals(fieldData, formData.getFieldData("assoc_bpm_assignee").getValue());
        checkPackageActionGroups(formData);
        assertEquals(WorkflowTaskState.IN_PROGRESS, formData.getFieldData("prop_bpm_status").getValue());
    }

    public void testPersistPropertyChanged() throws Exception
    {
        String fieldName = DESC_NAME.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, FormFieldConstants.PROP_DATA_PREFIX);
        String value = "New Description";

        processPersist(dataKey, value);

        assertEquals(1, actualProperties.size());
        assertEquals(value, actualProperties.get(DESC_NAME));
    }

    public void testPersistPropertyWith_() throws Exception
    {
        String fieldName = PROP_WITH_.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, FormFieldConstants.PROP_DATA_PREFIX);
        String value = "New _ Value";

        processPersist(dataKey, value);

        assertEquals(1, actualProperties.size());
        assertEquals(value, actualProperties.get(PROP_WITH_));
    }

    public void testPersistAssociationAdded() throws Exception
    {
        String fieldName = ACTORS_NAME.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, FormFieldConstants.ASSOC_DATA_PREFIX);
        dataKey = dataKey + FormFieldConstants.ASSOC_DATA_ADDED_SUFFIX;
        String nodeRef1 = FAKE_NODE.toString() + "1";
        String nodeRef2 = FAKE_NODE.toString() + "2";
        String value = nodeRef1 + ", " + nodeRef2;
        processPersist(dataKey, value);

        assertEquals(1, actualAdded.size());
        List<NodeRef> nodeRefs = actualAdded.get(ACTORS_NAME);
        assertNotNull(nodeRefs);
        assertEquals(2, nodeRefs.size());
        assertTrue(nodeRefs.contains(new NodeRef(nodeRef1)));
        assertTrue(nodeRefs.contains(new NodeRef(nodeRef2)));
    }

    public void testPersistAssociationsRemoved() throws Exception
    {
        String fieldName = ASSIGNEE_NAME.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, FormFieldConstants.ASSOC_DATA_PREFIX);
        dataKey = dataKey + FormFieldConstants.ASSOC_DATA_REMOVED_SUFFIX;
        String value = FAKE_NODE.toString();
        processPersist(dataKey, value);

        assertEquals(1, actualRemoved.size());
        List<NodeRef> nodeRefs = actualRemoved.get(ASSIGNEE_NAME);
        assertNotNull(nodeRefs);
        assertEquals(1, nodeRefs.size());
        assertTrue(nodeRefs.contains(FAKE_NODE));
    }

    public void testPersistAssociationAddedWith_() throws Exception
    {
        String fieldName = ASSOC_WITH_.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, FormFieldConstants.ASSOC_DATA_PREFIX);
        dataKey = dataKey + FormFieldConstants.ASSOC_DATA_ADDED_SUFFIX;
        String nodeRef1 = FAKE_NODE.toString() + "1";
        String nodeRef2 = FAKE_NODE.toString() + "2";
        String value = nodeRef1 + ", " + nodeRef2;
        processPersist(dataKey, value);

        assertEquals(1, actualAdded.size());
        List<NodeRef> nodeRefs = actualAdded.get(ASSOC_WITH_);
        assertNotNull(nodeRefs);
        assertEquals(2, nodeRefs.size());
        assertTrue(nodeRefs.contains(new NodeRef(nodeRef1)));
        assertTrue(nodeRefs.contains(new NodeRef(nodeRef2)));
    }

    public void testPackageItems() throws Exception
    {
        //TODO Implement test.
    }
    
    private void processPersist(String dataKey, String value)
    {
        FormData data = new FormData();
        data.addFieldData(dataKey, value);
        Item item = new Item("task", TASK_ID);
        WorkflowTask persistedItem = (WorkflowTask) processor.persist(item, data);
        assertEquals(newTask, persistedItem);
    }

    private Form processForm(List<String> fields)
    {
        Item item = new Item("task", TASK_ID);
        Form form = processor.generate(item, fields, null, null);
        return form;
    }

    private void checkPackageActionGroups(FormData formData)
    {
        FieldData pckgActionData = formData.getFieldData("prop_bpm_packageActionGroup");
        assertNotNull(pckgActionData);
        assertEquals("", pckgActionData.getValue());
        FieldData pckgItemActionData = formData.getFieldData("prop_bpm_packageItemActionGroup");
        assertNotNull(pckgItemActionData);
        assertEquals("read_package_item_actions", pckgItemActionData.getValue());
    }

    private void checkSingleProperty(Form form, String fieldName, Serializable fieldData)
    {
        checkSingleField(form, fieldName, fieldData, "prop_");

    }

    private void checkSingleAssociation(Form form, String fieldName, Serializable fieldData)
    {
        checkSingleField(form, fieldName, fieldData, "assoc_");

    }

    private void checkSingleField(Form form, String fieldName, Serializable fieldData, String prefix)
    {
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertEquals(1, fieldDefs.size());
        FieldDefinition fieldDef = fieldDefs.get(0);
        assertEquals(fieldName, fieldDef.getName());
        String dataKey = fieldDef.getDataKeyName();
        String expDataKey = makeDataKeyName(fieldName, prefix);
        assertEquals(expDataKey, dataKey);
        FieldData data = form.getFormData().getFieldData(dataKey);
        assertEquals(fieldData, data.getValue());
    }

    /**
     * @param fieldName
     * @param prefix
     * @return
     */
    private String makeDataKeyName(String fieldName, String prefix)
    {
        return prefix + fieldName.replace(":", "_");
    }

    /*
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        task = makeTask();
        workflowService = makeWorkflowService();
        nodeService = makeNodeService();
        DictionaryService dictionaryService = makeDictionaryService();
        namespaceService = makeNamespaceService();
        MockFieldProcessorRegistry fieldProcessorRegistry = new MockFieldProcessorRegistry(namespaceService,
                    dictionaryService);
        DefaultFieldProcessor defaultProcessor = makeDefaultFieldProcessor(dictionaryService);
        processor = makeTaskFormProcessor(dictionaryService, fieldProcessorRegistry, defaultProcessor);
    }

    private TaskFormProcessor makeTaskFormProcessor(DictionaryService dictionaryService,
                MockFieldProcessorRegistry fieldProcessorRegistry, DefaultFieldProcessor defaultProcessor)
    {
        TaskFormProcessor processor1 = new TaskFormProcessor();
        processor1.setWorkflowService(workflowService);
        processor1.setNodeService(nodeService);
        processor1.setNamespaceService(namespaceService);
        processor1.setDictionaryService(dictionaryService);
        processor1.setFieldProcessorRegistry(fieldProcessorRegistry);
        return processor1;
    }

    private DefaultFieldProcessor makeDefaultFieldProcessor(DictionaryService dictionaryService) throws Exception
    {
        DefaultFieldProcessor defaultProcessor = new DefaultFieldProcessor();
        defaultProcessor.setDictionaryService(dictionaryService);
        defaultProcessor.setNamespaceService(namespaceService);
        defaultProcessor.afterPropertiesSet();
        return defaultProcessor;
    }

    private WorkflowTask makeTask()
    {
        WorkflowTask result = new WorkflowTask();
        result.id = TASK_ID;
        result.state = WorkflowTaskState.IN_PROGRESS;
        result.definition = makeTaskDefinition();
        result.properties = makeTaskProperties();

        result.path = new WorkflowPath();
        result.path.instance = new WorkflowInstance();
        result.path.instance.workflowPackage = PCKG_NODE;
        return result;
    }

    private HashMap<QName, Serializable> makeTaskProperties()
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(STATUS_NAME, WorkflowTaskState.IN_PROGRESS);
        properties.put(ASSIGNEE_NAME, FAKE_NODE);
        return properties;
    }

    private WorkflowTaskDefinition makeTaskDefinition()
    {
        WorkflowTaskDefinition definition = new WorkflowTaskDefinition();
        definition.id = "DefinitionId";
        definition.metadata = makeTypeDef();
        definition.node = mock(WorkflowNode.class);
        return definition;
    }

    private TypeDefinition makeTypeDef()
    {
        TypeDefinition typeDef = mock(TypeDefinition.class);
        QName name = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, TASK_DEF_NAME);
        when(typeDef.getName()).thenReturn(name);

        // Set up task property definitions
        Map<QName, PropertyDefinition> propertyDefs = makeTaskPropertyDefs();
        when(typeDef.getProperties()).thenReturn(propertyDefs);

        // Set up task association definitions.
        Map<QName, AssociationDefinition> associationDefs = makeTaskAssociationDefs();
        when(typeDef.getAssociations()).thenReturn(associationDefs);
        return typeDef;
    }

    private Map<QName, PropertyDefinition> makeTaskPropertyDefs()
    {
        Map<QName, PropertyDefinition> properties = new HashMap<QName, PropertyDefinition>();
        QName textType = DataTypeDefinition.TEXT;

        // Add a Description property
        PropertyDefinition descValue = MockClassAttributeDefinition.mockPropertyDefinition(DESC_NAME, textType);
        properties.put(DESC_NAME, descValue);

        // Add a Status property
        PropertyDefinition titleValue = MockClassAttributeDefinition.mockPropertyDefinition(STATUS_NAME, textType);
        properties.put(STATUS_NAME, titleValue);

        // Add a Status property
        PropertyDefinition with_ = MockClassAttributeDefinition.mockPropertyDefinition(PROP_WITH_, textType);
        properties.put(PROP_WITH_, with_);

        // Add a Package Action property
        QName pckgActionGroup = WorkflowModel.PROP_PACKAGE_ACTION_GROUP;
        PropertyDefinition pckgAction = MockClassAttributeDefinition.mockPropertyDefinition(pckgActionGroup, textType,
                    "");
        properties.put(pckgActionGroup, pckgAction);

        // Add a Package Action property
        QName pckgItemActionGroup = WorkflowModel.PROP_PACKAGE_ITEM_ACTION_GROUP;
        PropertyDefinition pckgItemAction = MockClassAttributeDefinition.mockPropertyDefinition(pckgItemActionGroup,
                    textType, "read_package_item_actions");
        properties.put(pckgItemActionGroup, pckgItemAction);

        return properties;
    }

    private Map<QName, AssociationDefinition> makeTaskAssociationDefs()
    {
        Map<QName, AssociationDefinition> associations = new HashMap<QName, AssociationDefinition>();
        QName actorName = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "Actor");

        // Add Assigneee association
        MockClassAttributeDefinition assigneeDef = MockClassAttributeDefinition.mockAssociationDefinition(
                    ASSIGNEE_NAME, actorName);
        associations.put(ASSIGNEE_NAME, assigneeDef);

        // Add Assigneee association
        MockClassAttributeDefinition actorsDef = MockClassAttributeDefinition.mockAssociationDefinition(ACTORS_NAME,
                    actorName);
        associations.put(ACTORS_NAME, actorsDef);

        // Add association with _
        MockClassAttributeDefinition with_ = MockClassAttributeDefinition.mockAssociationDefinition(ASSOC_WITH_,
                    actorName);
        associations.put(ASSOC_WITH_, with_);

        return associations;
    }

    private NamespaceService makeNamespaceService()
    {
        NamespaceServiceMemoryImpl nsService = new NamespaceServiceMemoryImpl();
        nsService.registerNamespace(NamespaceService.BPM_MODEL_PREFIX, NamespaceService.BPM_MODEL_1_0_URI);
        return nsService;
    }

    @SuppressWarnings("unchecked")
    private DictionaryService makeDictionaryService()
    {
        DictionaryService mock = mock(DictionaryService.class);
        when(mock.getAnonymousType((QName) any(), (Collection<QName>) any())).thenReturn(task.definition.metadata);
        return mock;
    }

    @SuppressWarnings("unchecked")
    private WorkflowService makeWorkflowService()
    {
        WorkflowService service = mock(WorkflowService.class);
        when(service.getTaskById(anyString())).thenAnswer(new Answer<WorkflowTask>()
        {
            public WorkflowTask answer(InvocationOnMock invocation) throws Throwable
            {
                String id = (String) invocation.getArguments()[0];
                if (TASK_ID.equals(id))
                    return task;
                else
                    throw new WorkflowException("Task Id not found!");
            }
        });
        
        this.newTask = new WorkflowTask();
        newTask.id = TASK_ID;

        when(service.updateTask(anyString(), anyMap(), anyMap(), anyMap()))
        .thenAnswer(new Answer<WorkflowTask>()
        {
            public WorkflowTask answer(InvocationOnMock invocation) throws Throwable
            {
                Object[] args = invocation.getArguments();
                Map<QName, Serializable> props = (Map<QName, Serializable>) args[1];
                actualProperties = new HashMap<QName, Serializable>(props);
                Map<QName, List<NodeRef>> added = (Map<QName, List<NodeRef>>) args[2];
                actualAdded = new HashMap<QName, List<NodeRef>>(added);
                Map<QName, List<NodeRef>> removed = (Map<QName, List<NodeRef>>) args[3];
                actualRemoved = new HashMap<QName, List<NodeRef>>(removed);
                return newTask;
            }
        });
        return service;
    }
    
    private NodeService makeNodeService()
    {
        NodeService service = mock(NodeService.class);
        when(service.hasAspect(PCKG_NODE, WorkflowModel.ASPECT_WORKFLOW_PACKAGE))
            .thenReturn(true);
        return service;
    }

}
