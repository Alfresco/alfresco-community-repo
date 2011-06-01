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

package org.alfresco.repo.forms.processor.workflow;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.*;
import static org.alfresco.repo.workflow.WorkflowModel.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FieldDefinition;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.node.DefaultFieldProcessor;
import org.alfresco.repo.forms.processor.node.MockClassAttributeDefinition;
import org.alfresco.repo.forms.processor.node.MockFieldProcessorRegistry;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowException;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * 
 * @since 3.4
 * @author Nick Smith
 *
 */
public class TaskFormProcessorTest extends TestCase
{
    private static final String TASK_DEF_NAME = "TaskDef";
    private static final String TASK_ID = "foo$Real Id";
    private static final QName DESC_NAME = PROP_DESCRIPTION;
    private static final QName STATUS_NAME = PROP_STATUS;
    private static final QName PROP_WITH_ = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "some_prop");
    private static final QName ACTORS_NAME = ASSOC_POOLED_ACTORS;
    private static final QName ASSIGNEE_NAME = ASSOC_ASSIGNEE;
    private static final QName ASSOC_WITH_ = QName.createQName(NamespaceService.BPM_MODEL_1_0_URI, "some_assoc");
    private static final NodeRef FAKE_NODE = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakeNode");
    private static final NodeRef FAKE_NODE2 = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakeNode2");
    private static final NodeRef FAKE_NODE3 = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakeNode3");
    private static final NodeRef PCKG_NODE = new NodeRef(NamespaceService.BPM_MODEL_1_0_URI + "/FakePackage");
    private static final NodeRef USER_NODE = new NodeRef(NamespaceService.CONTENT_MODEL_1_0_URI + "/admin");

    private WorkflowService workflowService;
    private NodeService nodeService;
    private TaskFormProcessor processor;
    private WorkflowTask task;
    private NamespaceService namespaceService;
    private AuthenticationService authenticationService;
    private PersonService personService;
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
        assertEquals(TASK_ID, result.getId());
        
        // Check URI-encoded id.
        item = new Item("task", TASK_ID.replace('$', '_'));
        result = processor.getTypedItem(item);
        assertNotNull(result);
        assertEquals(TASK_ID, result.getId());
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
        assertEquals("api/task-instances/" + TASK_ID, formItem.getUrl());
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
        Form form = processForm();
        List<String> fieldDefs = form.getFieldDefinitionNames();
        assertTrue(fieldDefs.contains(ASSIGNEE_NAME.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(ASSOC_WITH_.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(DESC_NAME.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(STATUS_NAME.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(PROP_WITH_.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(PackageItemsFieldProcessor.KEY));
        assertTrue(fieldDefs.contains(TransitionFieldProcessor.KEY));

        // Check 'default ignored fields' are proerly removed from defaults.
        assertFalse(fieldDefs.contains(ACTORS_NAME.toPrefixString(namespaceService)));
        assertFalse(fieldDefs.contains(PROP_PACKAGE_ACTION_GROUP.toPrefixString(namespaceService)));
        assertFalse(fieldDefs.contains(PROP_PACKAGE_ITEM_ACTION_GROUP.toPrefixString(namespaceService)));

        Serializable fieldData = (Serializable) Arrays.asList(FAKE_NODE.toString());
        FormData formData = form.getFormData();
        assertEquals(fieldData, formData.getFieldData("assoc_bpm_assignee").getValue());
        checkPackageActionGroups(formData);
        assertEquals(WorkflowTaskState.IN_PROGRESS, formData.getFieldData("prop_bpm_status").getValue());
    }

    public void testGenerateTransitions() throws Exception
    {
        // Check empty transitions
        String fieldName = TransitionFieldProcessor.KEY;
        Form form = processForm(fieldName);
        String transitionValues = "";
        checkSingleProperty(form, fieldName, transitionValues);

        // Set up transitions
        WorkflowTransition transition1 = makeTransition("id1", "title1");
        WorkflowTransition transition2 = makeTransition("id2", "title2");
        WorkflowTransition transition3 = makeTransition("id3", "title3");
        task = makeTask(transition1, transition2, transition3);
        
        // Hide transition with id3.
        Serializable hiddenValue = (Serializable) Collections.singletonList("id3");
        task.getProperties().put(PROP_HIDDEN_TRANSITIONS, hiddenValue );
        
        form = processForm(fieldName);
        transitionValues = "id1|title1,id2|title2";
        checkSingleProperty(form, fieldName, transitionValues);
    }

    public void testGenerateMessage() throws Exception
    {
        String message = null;
        String fieldName = MessageFieldProcessor.KEY;
        Form form = processForm(fieldName);
        checkSingleProperty(form, fieldName, message);

        // add a description to the task and check it comes back
        message = "This is some text the user may have entered";
        this.task.getProperties().put(PROP_DESCRIPTION, message);
        
        form = processForm(fieldName);
        checkSingleProperty(form, fieldName, message);
        
        // set the description to the same as the task title
        // and make sure the message comes back as null
        this.task.getProperties().put(PROP_DESCRIPTION, this.task.getTitle());
        form = processForm(fieldName);
        checkSingleProperty(form, fieldName, null);
    }
    
    public void testGenerateTaskOwner() throws Exception
    {
        // check the task owner is null
        String fieldName = TaskOwnerFieldProcessor.KEY;
        Form form = processForm(fieldName);
        checkSingleProperty(form, fieldName, null);
        
        // set task owner
        this.task.getProperties().put(ContentModel.PROP_OWNER, "admin");
        
        // check the task owner property is correct
        form = processForm(fieldName);
        checkSingleProperty(form, fieldName, "admin|System|Administrator");
    }

    public void testGeneratePackageItems() throws Exception
    {
        // Check empty package
        String fieldName = PackageItemsFieldProcessor.KEY;
        Form form = processForm(fieldName);
        Serializable packageItems = (Serializable) Collections.emptyList();
        checkSingleAssociation(form, fieldName, packageItems);
        
        // Effectively add 3 items to package.
        List<NodeRef> value = Arrays.asList(FAKE_NODE, FAKE_NODE2, FAKE_NODE3);
        when(workflowService.getPackageContents(TASK_ID))
            .thenReturn(value);
        
        form = processForm(fieldName);
        packageItems = (Serializable) Arrays.asList(FAKE_NODE.toString(),
                    FAKE_NODE2.toString(),
                    FAKE_NODE3.toString());
        checkSingleAssociation(form, fieldName, packageItems);
    }

    private WorkflowTransition makeTransition(String id, String title)
    {
        return new WorkflowTransition(
                    id, title, null, false);
    }
    
    public void testPersistPropertyChanged() throws Exception
    {
        String fieldName = DESC_NAME.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName);
        String value = "New Description";

        processPersist(dataKey, value);

        assertEquals(1, actualProperties.size());
        assertEquals(value, actualProperties.get(DESC_NAME));
    }

    public void testPersistPropertyWith_() throws Exception
    {
        String fieldName = PROP_WITH_.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName);
        String value = "New _ Value";

        processPersist(dataKey, value);

        assertEquals(1, actualProperties.size());
        assertEquals(value, actualProperties.get(PROP_WITH_));
    }

    public void testPersistAssociationAdded() throws Exception
    {
        String fieldName = ACTORS_NAME.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, true);
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
        String dataKey = makeDataKeyName(fieldName, false);
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
        String dataKey = makeDataKeyName(fieldName, true);
        String value = FAKE_NODE + ", " + FAKE_NODE2;
        processPersist(dataKey, value);
        assertEquals(1, actualAdded.size());
        List<NodeRef> nodeRefs = actualAdded.get(ASSOC_WITH_);
        assertNotNull(nodeRefs);
        assertEquals(2, nodeRefs.size());
        assertTrue(nodeRefs.contains(FAKE_NODE));
        assertTrue(nodeRefs.contains(FAKE_NODE2));
    }
    
    @SuppressWarnings("unchecked")
    public void testPersistTransitions() throws Exception
    {
        // Check updates but doesn't transition if no transition prop set.
        processPersist("prop_bpm_foo", "foo");
        // Check endTask is never called.
        verify(workflowService, times(1)).updateTask(eq(TASK_ID), anyMap(), anyMap(), anyMap());
        verify(workflowService, never()).endTask(eq(TASK_ID), anyString());
        
        // Check default transition.
        String dataKey = makeDataKeyName(TransitionFieldProcessor.KEY);
        processPersist(dataKey, null);
        verify(workflowService, times(1)).endTask(TASK_ID, null);
        
        // Check specific transition.
        processPersist(dataKey, "foo");
        verify(workflowService, times(1)).endTask(TASK_ID, "foo");
    }
    
    @SuppressWarnings("unchecked")
    public void testPersistPropertyAndTransition() throws Exception
    {
        FormData data = new FormData();
        data.addFieldData("prop_bpm_foo", "bar");
        String dataKey = makeDataKeyName(TransitionFieldProcessor.KEY);
        data.addFieldData(dataKey, "foo");
        Item item = new Item("task", TASK_ID);
        WorkflowTask persistedItem = (WorkflowTask) processor.persist(item, data);
        
        // make sure task is correct and update and endTask were called
        assertEquals(newTask, persistedItem);
        verify(workflowService, times(1)).updateTask(eq(TASK_ID), anyMap(), anyMap(), anyMap());
        verify(workflowService, times(1)).endTask(TASK_ID, "foo");
    }
    
    public void testPersistPackageItemsAdded() throws Exception
    {
        mockPackageItems(FAKE_NODE3);
        String dataKey = makeDataKeyName(PackageItemsFieldProcessor.KEY, true); 
        String value = FAKE_NODE + ", " + FAKE_NODE2;
        processPersist(dataKey, value);
        checkAddPackageItem(FAKE_NODE, true);
        checkAddPackageItem(FAKE_NODE2, true);
        checkAddPackageItem(FAKE_NODE3, false);
    }
    
    public void testPersistPackageItemsRemoved() throws Exception
    {
        mockPackageItems(FAKE_NODE, FAKE_NODE2);
        String dataKey = makeDataKeyName(PackageItemsFieldProcessor.KEY, false); 
        String value = FAKE_NODE + ", " + FAKE_NODE2+ "," + FAKE_NODE3;
        processPersist(dataKey, value);
        
        // Check nodes 1 and 2 removed correctly.
        checkRemovedPackageItem(FAKE_NODE, true);
        checkRemovedPackageItem(FAKE_NODE2, true);
        
        // Check node 3 is not removed as it was not in the package to start with.
        checkRemovedPackageItem(FAKE_NODE3, false);
    }

    private void mockPackageItems(NodeRef... children)
    {
        ArrayList<ChildAssociationRef> results = new ArrayList<ChildAssociationRef>(children.length);
        for (NodeRef nodeRef : children)
        {
            ChildAssociationRef child = new ChildAssociationRef(ASSOC_PACKAGE_CONTAINS, PCKG_NODE, null, nodeRef);
            results.add(child);
        }
        when(nodeService.getChildAssocs(eq(PCKG_NODE), (QNamePattern)any(), (QNamePattern)any()))
        .thenReturn(results);
        
    }

    private void checkRemovedPackageItem(NodeRef child, boolean wasCalled)
    {
        int times = wasCalled ? 1 : 0;
        verify(nodeService, times(times))
            .removeChild(PCKG_NODE, child);
    }

    private void checkAddPackageItem(NodeRef child, boolean wasCalled)
    {
        int times = wasCalled ? 1 : 0;
        verify(nodeService, times(times))
            .addChild(eq(PCKG_NODE),
                        eq(child),
                        eq(ASSOC_PACKAGE_CONTAINS),
                        (QName)any());
    }
    
    private void processPersist(String dataKey, String value)
    {
        FormData data = new FormData();
        data.addFieldData(dataKey, value);
        Item item = new Item("task", TASK_ID);
        WorkflowTask persistedItem = (WorkflowTask) processor.persist(item, data);
        assertEquals(newTask, persistedItem);
    }

    private Form processForm(String... fields)
    {
        return processForm(Arrays.asList(fields));
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
        String expDataKey = makeDataKeyName(fieldName);
        checkSingleField(form, fieldName, fieldData, expDataKey);
    }

    private void checkSingleAssociation(Form form, String fieldName, Serializable fieldData)
    {
        String expDataKey = makeAssociationDataKey(fieldName);
        checkSingleField(form, fieldName, fieldData, expDataKey);
    }

    private void checkSingleField(Form form, String fieldName, Serializable fieldData, String expDataKey)
    {
        List<FieldDefinition> fieldDefs = form.getFieldDefinitions();
        assertEquals(1, fieldDefs.size());
        FieldDefinition fieldDef = fieldDefs.get(0);
        assertEquals(fieldName, fieldDef.getName());
        String dataKey = fieldDef.getDataKeyName();
        assertEquals(expDataKey, dataKey);
        FieldData data = form.getFormData().getFieldData(dataKey);
        if (fieldData != null)
        {
            assertEquals(fieldData, data.getValue());
        }
        else
        {
            assertNull(data);
        }
    }

    private String makeDataKeyName(String fieldName)
    {
        return PROP_DATA_PREFIX + fieldName.replace(":", "_");
    }

    private String makeDataKeyName(String fieldName, boolean added)
    {
        String assocDataKey = makeAssociationDataKey(fieldName);
        String suffix = added ? ASSOC_DATA_ADDED_SUFFIX : ASSOC_DATA_REMOVED_SUFFIX;
        return assocDataKey + suffix;
    }
    
    private String makeAssociationDataKey(String fieldName)
    {
        return ASSOC_DATA_PREFIX + fieldName.replace(":", "_");
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
        authenticationService = makeAuthenticationService();
        personService = makePersonService();
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
        processor1.setAuthenticationService(authenticationService);
        processor1.setPersonService(personService);
        processor1.setFieldProcessorRegistry(fieldProcessorRegistry);
        processor1.setBehaviourFilter(mock(BehaviourFilter.class));
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

    private WorkflowTask makeTask(WorkflowTransition... transitions)
    {
        String id = TASK_ID;
		String title = "Test";
		WorkflowTaskState state = WorkflowTaskState.IN_PROGRESS;
        WorkflowTaskDefinition taskDef = makeTaskDefinition(transitions);
        Map<QName, Serializable> properties = makeTaskProperties();

        WorkflowDefinition definition = new WorkflowDefinition("42", "Test", "1.0", "Test", "Test", null);
        NodeRef wfPackage = PCKG_NODE;
        WorkflowInstance instance = new WorkflowInstance(null,
                    definition, null,
                    null, wfPackage,
                    null, true, null, null);
        WorkflowNode node = new WorkflowNode("", "", "", "", true, new WorkflowTransition[0]);
        WorkflowPath path = new WorkflowPath(null, instance, node, true);
        return new WorkflowTask(id,
                    taskDef, null, title, null, state, path, properties);
		
    }

    private HashMap<QName, Serializable> makeTaskProperties()
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(STATUS_NAME, WorkflowTaskState.IN_PROGRESS);
        properties.put(ASSIGNEE_NAME, FAKE_NODE);
        return properties;
    }

    private WorkflowTaskDefinition makeTaskDefinition(WorkflowTransition... transitions)
    {
        String id = "DefinitionId";
        TypeDefinition metadata = makeTypeDef();
        WorkflowNode node = new WorkflowNode("", "", "", "", true, transitions);
        return new WorkflowTaskDefinition(id,
                    node, metadata);
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
        QName pckgActionGroup = PROP_PACKAGE_ACTION_GROUP;
        PropertyDefinition pckgAction = MockClassAttributeDefinition.mockPropertyDefinition(pckgActionGroup, textType,
                    "");
        properties.put(pckgActionGroup, pckgAction);

        // Add a Package Action property
        QName pckgItemActionGroup = PROP_PACKAGE_ITEM_ACTION_GROUP;
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
        nsService.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
        nsService.registerNamespace(NamespaceService.RENDITION_MODEL_PREFIX, NamespaceService.RENDITION_MODEL_1_0_URI);
        nsService.registerNamespace(NamespaceService.BPM_MODEL_PREFIX, NamespaceService.BPM_MODEL_1_0_URI);
        return nsService;
    }

    @SuppressWarnings("unchecked")
    private DictionaryService makeDictionaryService()
    {
        DictionaryService mock = mock(DictionaryService.class);
        when(mock.getAnonymousType((QName) any(), (Collection<QName>) any())).thenReturn(task.getDefinition().getMetadata());
        return mock;
    }

    private AuthenticationService makeAuthenticationService()
    {
        AuthenticationService mock = mock(AuthenticationService.class);
        when(mock.getCurrentUserName()).thenReturn("admin");
        return mock;
    }
    
    private PersonService makePersonService()
    {
        PersonService mock = mock(PersonService.class);
        when(mock.getPerson("admin")).thenReturn(USER_NODE);
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
        
        this.newTask = new WorkflowTask(TASK_ID, null, null, null, null, null, null, null);

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
        
        when(service.endTask(eq(TASK_ID), anyString()))
            .thenReturn(newTask);
        
        when(service.isTaskEditable((WorkflowTask)any(), anyString())).thenReturn(true);
        
        return service;
    }
    
    private NodeService makeNodeService()
    {
        NodeService service = mock(NodeService.class);
        when(service.hasAspect(PCKG_NODE, ASPECT_WORKFLOW_PACKAGE))
            .thenReturn(true);
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
        props.put(ContentModel.PROP_FIRSTNAME, "System");
        props.put(ContentModel.PROP_LASTNAME, "Administrator");
        
        when(service.getProperties(USER_NODE)).thenReturn(props);
        return service;
    }

}
