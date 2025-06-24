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

package org.alfresco.repo.forms.processor.workflow;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.workflow.WorkflowModel.ASPECT_WORKFLOW_PACKAGE;
import static org.alfresco.repo.workflow.WorkflowModel.ASSOC_PACKAGE;
import static org.alfresco.repo.workflow.WorkflowModel.PROP_PACKAGE_ACTION_GROUP;
import static org.alfresco.repo.workflow.WorkflowModel.PROP_PACKAGE_ITEM_ACTION_GROUP;
import static org.alfresco.repo.workflow.WorkflowModel.PROP_WORKFLOW_PRIORITY;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.FormNotFoundException;
import org.alfresco.repo.forms.Item;
import org.alfresco.repo.forms.processor.node.DefaultFieldProcessor;
import org.alfresco.repo.forms.processor.node.MockClassAttributeDefinition;
import org.alfresco.repo.forms.processor.node.MockFieldProcessorRegistry;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.NamespaceServiceMemoryImpl;
import org.alfresco.service.namespace.QName;

/**
 * 
 * @since 3.4
 * @author Nick Smith
 *
 */
public class WorkflowFormProcessorTest extends FormProcessorTest
{
    private static final String WF_DEF_NAME = "foo$wf:bar";
    private static final QName PRIORITY_NAME = PROP_WORKFLOW_PRIORITY;

    private WorkflowInstance newInstance;
    private WorkflowDefinition definition;
    private Map<QName, Serializable> actualProperties = null;

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

        WorkflowDefinition result = ((WorkflowFormProcessor) processor).getTypedItem(item);
        assertNotNull(result);
        assertEquals(WF_DEF_NAME, result.getName());
    }

    public void testPersistPropertyComment() throws Exception
    {
        super.testPersistPropertyComment(item.getId());
    }

    public void testGenerateSetsItemAndUrl() throws Exception
    {
        Form form = ((WorkflowFormProcessor) processor).generate(item, null, null, null);
        Item formItem = form.getItem();
        assertEquals(item.getId(), formItem.getId());
        assertEquals(item.getKind(), formItem.getKind());
        assertEquals(WF_DEF_NAME, formItem.getType());
        assertEquals("api/workflow-definitions/" + definition.getId(), formItem.getUrl());
    }

    public void testGenerateSingleProperty()
    {
        // Check Status field is added to Form.
        String fieldName = PRIORITY_NAME.toPrefixString(namespaceService);
        List<String> fields = Arrays.asList(fieldName);
        Form form = processForm(fields);
        checkSingleProperty(form, fieldName, 2);

        // Check Status field is added to Form, when explicitly typed as a
        // property.
        String fullPropertyName = "prop:" + fieldName;
        fields = Arrays.asList(fullPropertyName);
        form = processForm(fields);
        checkSingleProperty(form, fieldName, 2);
        checkPackageActionGroups(form.getFormData());
    }

    public void testGenerateSingleAssociation()
    {
        Serializable values = (Serializable) Collections.emptyList();
        // Check Assignee field is added to Form.
        String fieldName = ASSIGNEE_NAME.toPrefixString(namespaceService);
        List<String> fields = Arrays.asList(fieldName);
        Form form = processForm(fields);
        checkSingleAssociation(form, fieldName, values);

        // Check Assignee field is added to Form, when explicitly typed as an
        // association.
        String fullAssociationName = "assoc:" + fieldName;
        fields = Arrays.asList(fullAssociationName);
        form = processForm(fields);
        checkSingleAssociation(form, fieldName, values);
        checkPackageActionGroups(form.getFormData());
    }

    public void testIgnoresUnknownFields() throws Exception
    {
        String fakeFieldName = NamespaceService.BPM_MODEL_PREFIX + ":" + "Fake Field";
        String priorityField = PRIORITY_NAME.toPrefixString(namespaceService);
        List<String> fields = Arrays.asList(fakeFieldName, priorityField);
        Form form = processForm(fields);
        checkSingleProperty(form, priorityField, 2);
        checkPackageActionGroups(form.getFormData());
    }

    public void testGenerateDefaultForm() throws Exception
    {
        Form form = processForm();
        List<String> fieldDefs = form.getFieldDefinitionNames();
        assertTrue(fieldDefs.contains(ASSIGNEE_NAME.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(PRIORITY_NAME.toPrefixString(namespaceService)));
        assertTrue(fieldDefs.contains(PackageItemsFieldProcessor.KEY));

        // Check 'default ignored fields' are proerly removed from defaults.
        assertFalse(fieldDefs.contains(ACTORS_NAME.toPrefixString(namespaceService)));
        assertFalse(fieldDefs.contains(PROP_PACKAGE_ACTION_GROUP.toPrefixString(namespaceService)));
        assertFalse(fieldDefs.contains(PROP_PACKAGE_ITEM_ACTION_GROUP.toPrefixString(namespaceService)));
        assertFalse(fieldDefs.contains(WorkflowModel.PROP_DESCRIPTION.toPrefixString(namespaceService)));
        assertFalse(fieldDefs.contains(WorkflowModel.PROP_DUE_DATE.toPrefixString(namespaceService)));
        assertFalse(fieldDefs.contains(WorkflowModel.PROP_PRIORITY.toPrefixString(namespaceService)));
        assertFalse(fieldDefs.contains(WorkflowModel.PROP_TASK_ID.toPrefixString(namespaceService)));

        Serializable fieldData = (Serializable) Collections.emptyList();
        FormData formData = form.getFormData();
        assertEquals(fieldData, formData.getFieldData("assoc_bpm_assignee").getValue());
        checkPackageActionGroups(formData);
        assertEquals(2, formData.getFieldData("prop_bpm_workflowPriority").getValue());
    }

    public void testGeneratePackageItems() throws Exception
    {
        // Check empty package
        String fieldName = PackageItemsFieldProcessor.KEY;
        Form form = processForm(fieldName);
        Serializable packageItems = (Serializable) Collections.emptyList();
        checkSingleAssociation(form, fieldName, packageItems);
    }

    public void testPersistPropertyChanged() throws Exception
    {
        String fieldName = DESC_NAME.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName);
        String value = "New Description";

        processPersist(dataKey, value);

        // Check adds description property and Package.
        assertEquals(2, actualProperties.size());
        assertEquals(value, actualProperties.get(DESC_NAME));
        assertEquals(PCKG_NODE, actualProperties.get(ASSOC_PACKAGE));
    }

    public void testPersistPropertyWith_() throws Exception
    {
        String fieldName = PROP_WITH_.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName);
        String value = "New _ Value";

        processPersist(dataKey, value);

        assertEquals(2, actualProperties.size());
        assertEquals(value, actualProperties.get(PROP_WITH_));
    }

    public void testPersistAssociationAdded() throws Exception
    {
        String fieldName = ACTORS_NAME.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, true);
        String value = FAKE_NODE + ", " + FAKE_NODE2;
        processPersist(dataKey, value);

        assertEquals(2, actualProperties.size());
        List<?> nodeRefs = (List<?>) actualProperties.get(ACTORS_NAME);
        assertNotNull(nodeRefs);
        assertEquals(2, nodeRefs.size());
        assertTrue(nodeRefs.contains(FAKE_NODE));
        assertTrue(nodeRefs.contains(FAKE_NODE2));
    }

    public void testIgnoreAssociationsRemoved() throws Exception
    {
        String fieldName = ASSIGNEE_NAME.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, false);
        String value = FAKE_NODE.toString();
        processPersist(dataKey, value);

        assertEquals(1, actualProperties.size());
        Serializable nodeRefs = actualProperties.get(ASSIGNEE_NAME);
        assertNull(nodeRefs);
    }

    public void testPersistAssociationAddedWith_() throws Exception
    {
        String fieldName = ASSOC_WITH_.toPrefixString(namespaceService);
        String dataKey = makeDataKeyName(fieldName, true);
        String value = FAKE_NODE + ", " + FAKE_NODE2;
        processPersist(dataKey, value);

        assertEquals(2, actualProperties.size());
        List<?> nodeRefs = (List<?>) actualProperties.get(ASSOC_WITH_);
        assertNotNull(nodeRefs);
        assertEquals(2, nodeRefs.size());
        assertTrue(nodeRefs.contains(FAKE_NODE));
        assertTrue(nodeRefs.contains(FAKE_NODE2));
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

    public void testPersistPackageItemsRemovedIgnored() throws Exception
    {
        mockPackageItems(FAKE_NODE, FAKE_NODE2);
        String dataKey = makeDataKeyName(PackageItemsFieldProcessor.KEY, false);
        String value = FAKE_NODE + ", " + FAKE_NODE2 + "," + FAKE_NODE3;
        processPersist(dataKey, value);

        // Check nodes 1 and 2 removed correctly.
        checkRemovedPackageItem(FAKE_NODE, false);
        checkRemovedPackageItem(FAKE_NODE2, false);
        checkRemovedPackageItem(FAKE_NODE3, false);
    }

    private void processPersist(String dataKey, String value)
    {
        FormData data = new FormData();
        data.addFieldData(dataKey, value);
        WorkflowInstance persistedItem = (WorkflowInstance) processor.persist(item, data);
        assertEquals(newInstance, persistedItem);
    }

    private Form processForm(String... fields)
    {
        return processForm(Arrays.asList(fields));
    }

    private Form processForm(List<String> fields)
    {
        return ((WorkflowFormProcessor) processor).generate(item, fields, null, null);
    }

    private void checkPackageActionGroups(FormData formData)
    {
        FieldData pckgActionData = formData.getFieldData("prop_bpm_packageActionGroup");
        assertNotNull(pckgActionData);
        assertEquals("add_package_item_actions", pckgActionData.getValue());
        FieldData pckgItemActionData = formData.getFieldData("prop_bpm_packageItemActionGroup");
        assertNotNull(pckgItemActionData);
        assertEquals("start_package_item_actions", pckgItemActionData.getValue());
    }

    /* @see junit.framework.TestCase#setUp() */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        super.item = new Item("workflow", WF_DEF_NAME);
        definition = makeWorkflowDefinition();
        super.workflowService = makeWorkflowService();
        super.nodeService = makeNodeService();
        DictionaryService dictionaryService = makeDictionaryService();
        super.namespaceService = makeNamespaceService();
        MockFieldProcessorRegistry fieldProcessorRegistry = new MockFieldProcessorRegistry(namespaceService,
                dictionaryService);
        DefaultFieldProcessor defaultProcessor = super.makeDefaultFieldProcessor(dictionaryService);
        super.processor = makeWorkflowFormProcessor(dictionaryService, fieldProcessorRegistry, defaultProcessor);
    }

    private WorkflowFormProcessor makeWorkflowFormProcessor(DictionaryService dictionaryService,
            MockFieldProcessorRegistry fieldProcessorRegistry, DefaultFieldProcessor defaultProcessor)
    {
        WorkflowFormProcessor processor1 = new WorkflowFormProcessor();
        processor1 = (WorkflowFormProcessor) super.makeTaskFormProcessor(processor1, dictionaryService,
                fieldProcessorRegistry, defaultProcessor);
        return processor1;
    }

    private WorkflowDefinition makeWorkflowDefinition()
    {
        String id = "foo$workflowDefId";
        String name = WF_DEF_NAME;
        String version = "1.0";
        String title = "Foo Bar Title";
        String description = "Foo Bar Description";
        WorkflowTaskDefinition startTaskDefinition = makeTaskDefinition();
        return new WorkflowDefinition(id, name, version, title, description, startTaskDefinition);
    }

    private WorkflowTaskDefinition makeTaskDefinition()
    {
        String id = "foo$startTaskDefId";
        TypeDefinition metadata = makeTypeDef();
        WorkflowNode node = new WorkflowNode("", "", "", "", false);
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
        QName intType = DataTypeDefinition.INT;
        MockClassAttributeDefinition priorityDef = MockClassAttributeDefinition.mockPropertyDefinition(PRIORITY_NAME, intType, "2");
        properties.put(PRIORITY_NAME, priorityDef);

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
                "add_package_item_actions");
        properties.put(pckgActionGroup, pckgAction);

        // Add a Package Action property
        QName pckgItemActionGroup = PROP_PACKAGE_ITEM_ACTION_GROUP;
        PropertyDefinition pckgItemAction = MockClassAttributeDefinition.mockPropertyDefinition(pckgItemActionGroup,
                textType, "start_package_item_actions");
        properties.put(pckgItemActionGroup, pckgItemAction);

        return properties;
    }

    private NamespaceService makeNamespaceService()
    {
        NamespaceServiceMemoryImpl nsService = new NamespaceServiceMemoryImpl();
        nsService.registerNamespace(NamespaceService.SYSTEM_MODEL_PREFIX, NamespaceService.SYSTEM_MODEL_1_0_URI);
        nsService.registerNamespace(NamespaceService.RENDITION_MODEL_PREFIX, NamespaceService.RENDITION_MODEL_1_0_URI);
        nsService.registerNamespace(NamespaceService.BPM_MODEL_PREFIX, NamespaceService.BPM_MODEL_1_0_URI);
        nsService.registerNamespace(NamespaceService.WORKFLOW_MODEL_PREFIX, NamespaceService.WORKFLOW_MODEL_1_0_URI);
        return nsService;
    }

    @SuppressWarnings("unchecked")
    private DictionaryService makeDictionaryService()
    {
        DictionaryService mock = mock(DictionaryService.class);
        TypeDefinition taskTypeDef = definition.getStartTaskDefinition().getMetadata();
        when(mock.getAnonymousType((QName) any(), (Collection<QName>) any())).thenReturn(taskTypeDef);
        return mock;
    }

    @SuppressWarnings("unchecked")
    private WorkflowService makeWorkflowService()
    {
        WorkflowService service = mock(WorkflowService.class);
        when(service.getDefinitionByName(WF_DEF_NAME)).thenReturn(definition);

        String instanceId = "foo$instanceId";
        newInstance = new WorkflowInstance(instanceId,
                definition, null, null, null,
                null, true, null, null);
        WorkflowTask startTask = new WorkflowTask("foo$taskId", null, null, null, null, null, null, null);
        String pathId = "foo$pathId";
        final WorkflowPath path = new WorkflowPath(pathId, newInstance, null, true);

        when(service.startWorkflow(eq(definition.getId()), anyMap()))
                .thenAnswer(new Answer<WorkflowPath>() {
                    public WorkflowPath answer(InvocationOnMock invocation) throws Throwable
                    {
                        Object[] arguments = invocation.getArguments();
                        actualProperties = (Map<QName, Serializable>) arguments[1];
                        return path;
                    }
                });
        when(service.getTasksForWorkflowPath(path.getId()))
                .thenReturn(Collections.singletonList(startTask));
        when(service.createPackage(null)).thenReturn(PCKG_NODE);
        return service;
    }

    private NodeService makeNodeService()
    {
        NodeService service = mock(NodeService.class);
        when(service.hasAspect(PCKG_NODE, ASPECT_WORKFLOW_PACKAGE))
                .thenReturn(true);
        return service;
    }

}
