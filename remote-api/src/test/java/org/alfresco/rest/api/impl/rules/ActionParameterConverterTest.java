/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.impl.rules;

import static org.alfresco.repo.action.executer.CopyActionExecuter.PARAM_DESTINATION_FOLDER;
import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
import static org.alfresco.service.cmr.security.AccessStatus.ALLOWED;
import static org.alfresco.service.cmr.security.AccessStatus.DENIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.CheckInActionExecuter;
import org.alfresco.repo.action.executer.CheckOutActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.repo.action.executer.LinkCategoryActionExecuter;
import org.alfresco.repo.action.executer.MoveActionExecuter;
import org.alfresco.repo.action.executer.RemoveFeaturesActionExecuter;
import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuter;
import org.alfresco.repo.action.executer.SimpleWorkflowActionExecuter;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.logging.log4j.util.Strings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class ActionParameterConverterTest
{
    private static final String VERSIONABLE = "versionable";
    private static final String VERSIONABLE_ASPECT = NamespaceService.CONTENT_MODEL_PREFIX + QName.NAMESPACE_PREFIX + VERSIONABLE;
    private static final String CHECKOUT = "checkout";
    private static final String CHECKOUT_ASPECT = NamespaceService.CONTENT_MODEL_PREFIX + QName.NAMESPACE_PREFIX + CHECKOUT;
    private static final String CONTAINS = "contains";
    private static final String CONTAINS_ASPECT = NamespaceService.CONTENT_MODEL_PREFIX + QName.NAMESPACE_PREFIX + CONTAINS;
    private static final String CLASSIFIABLE = "generalclassifiable";
    private static final String CLASSIFIABLE_ASPECT = NamespaceService.CONTENT_MODEL_PREFIX + QName.NAMESPACE_PREFIX + CLASSIFIABLE;
    private static final String IDENTIFIER = "identifier";
    private static final String IDENTIFIER_ASPECT = NamespaceService.CONTENT_MODEL_PREFIX + QName.NAMESPACE_PREFIX + IDENTIFIER;

    private static final String DUMMY_FOLDER_NODE_ID = "dummy://folder/node";
    private static final NodeRef DUMMY_FOLDER_NODE = new NodeRef(DUMMY_FOLDER_NODE_ID);
    private static final String DUMMY_SCRIPT_NODE_ID = "dummy://script/ref";
    private static final NodeRef DUMMY_SCRIPT_NODE = new NodeRef(DUMMY_SCRIPT_NODE_ID);

    @Mock
    private DictionaryService dictionaryService;
    @Mock
    private ActionService actionService;
    @Mock
    private NamespaceService namespaceService;
    @Mock
    private PermissionService permissionService;
    @Mock
    private Nodes nodes;

    @Mock
    private ActionDefinition actionDefinition;
    @Mock
    private ParameterDefinition actionDefinitionParam1;
    @Mock
    private ParameterDefinition actionDefinitionParam2;
    @Mock
    private ParameterDefinition actionDefinitionParam3;
    @Mock
    private DataTypeDefinition dataTypeDefinition1;
    @Mock
    private DataTypeDefinition dataTypeDefinition2;
    @Mock
    private DataTypeDefinition dataTypeDefinition3;

    @InjectMocks
    private ActionParameterConverter objectUnderTest;

    @Before
    public void setUp()
    {
        given(nodes.validateOrLookupNode(DUMMY_FOLDER_NODE_ID)).willReturn(DUMMY_FOLDER_NODE);
        given(nodes.validateOrLookupNode(DUMMY_SCRIPT_NODE_ID)).willReturn(DUMMY_SCRIPT_NODE);
        given(permissionService.hasReadPermission(DUMMY_FOLDER_NODE)).willReturn(ALLOWED);
        given(permissionService.hasReadPermission(DUMMY_SCRIPT_NODE)).willReturn(ALLOWED);
    }

    @Test
    public void testAddAspectConversion()
    {
        final String name = AddFeaturesActionExecuter.NAME;
        final String aspectNameKey = AddFeaturesActionExecuter.PARAM_ASPECT_NAME;
        final Map<String, Serializable> params = Map.of(aspectNameKey, VERSIONABLE_ASPECT);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(aspectNameKey)).willReturn(actionDefinitionParam1);
        final QName qname = DataTypeDefinition.QNAME;
        given(actionDefinitionParam1.getType()).willReturn(qname);
        given(dictionaryService.getDataType(qname)).willReturn(dataTypeDefinition1);
        given(namespaceService.getNamespaceURI(any())).willReturn(NamespaceService.DICTIONARY_MODEL_1_0_URI);

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(aspectNameKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should().getDataType(qname);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).should().getNamespaceURI(any());
        then(namespaceService).shouldHaveNoMoreInteractions();

        final Serializable convertedParam = convertedParams.get(aspectNameKey);
        assertTrue(convertedParam instanceof QName);
        assertEquals(VERSIONABLE, ((QName) convertedParam).getLocalName());
        assertEquals(VERSIONABLE_ASPECT, ((QName) convertedParam).getPrefixString());
        assertEquals(NamespaceService.DICTIONARY_MODEL_1_0_URI, ((QName) convertedParam).getNamespaceURI());
    }

    @Test
    public void testCopyConversion()
    {
        final String name = CopyActionExecuter.NAME;
        final String destinationFolderKey = PARAM_DESTINATION_FOLDER;
        final String deepCopyKey = CopyActionExecuter.PARAM_DEEP_COPY;
        final Map<String, Serializable> params = Map.of(destinationFolderKey, DUMMY_FOLDER_NODE_ID, deepCopyKey, true);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(destinationFolderKey)).willReturn(actionDefinitionParam1);
        given(actionDefinition.getParameterDefintion(deepCopyKey)).willReturn(actionDefinitionParam2);
        final QName nodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam1.getType()).willReturn(nodeRef);
        final QName bool = DataTypeDefinition.BOOLEAN;
        given(actionDefinitionParam2.getType()).willReturn(bool);

        given(dictionaryService.getDataType(nodeRef)).willReturn(dataTypeDefinition1);
        given(dictionaryService.getDataType(bool)).willReturn(dataTypeDefinition2);
        given(dataTypeDefinition2.getJavaClassName()).willReturn(Boolean.class.getName());

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(destinationFolderKey);
        then(actionDefinition).should().getParameterDefintion(deepCopyKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should(times(2)).getDataType(bool);
        then(dictionaryService).should().getDataType(nodeRef);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).shouldHaveNoInteractions();

        final Serializable convertedCopyParam = convertedParams.get(destinationFolderKey);
        assertTrue(convertedCopyParam instanceof NodeRef);
        assertEquals(DUMMY_FOLDER_NODE, convertedCopyParam);
        final Serializable convertedDeepCopyParam = convertedParams.get(deepCopyKey);
        assertThat(convertedDeepCopyParam instanceof Boolean).isTrue();
        assertTrue(((Boolean) convertedDeepCopyParam));
    }

    @Test
    public void testExecuteScriptConversion()
    {
        final String name = ScriptActionExecuter.NAME;
        final String executeScriptKey = ScriptActionExecuter.PARAM_SCRIPTREF;
        final Map<String, Serializable> params = Map.of(executeScriptKey, DUMMY_SCRIPT_NODE_ID);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(executeScriptKey)).willReturn(actionDefinitionParam1);
        final QName scriptNodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam1.getType()).willReturn(scriptNodeRef);

        given(dictionaryService.getDataType(scriptNodeRef)).willReturn(dataTypeDefinition1);

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(executeScriptKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should().getDataType(scriptNodeRef);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).shouldHaveNoInteractions();

        final Serializable convertedCopyParam = convertedParams.get(executeScriptKey);
        assertTrue(convertedCopyParam instanceof NodeRef);
        assertEquals(DUMMY_SCRIPT_NODE, convertedCopyParam);
    }

    @Test
    public void testMoveConversion()
    {
        final String name = MoveActionExecuter.NAME;
        final String destinationFolderKey = MoveActionExecuter.PARAM_DESTINATION_FOLDER;
        final Map<String, Serializable> params = Map.of(destinationFolderKey, DUMMY_FOLDER_NODE_ID);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(destinationFolderKey)).willReturn(actionDefinitionParam1);
        final QName nodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam1.getType()).willReturn(nodeRef);

        given(dictionaryService.getDataType(nodeRef)).willReturn(dataTypeDefinition1);

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(destinationFolderKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should().getDataType(nodeRef);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).shouldHaveNoInteractions();

        final Serializable convertedCopyParam = convertedParams.get(destinationFolderKey);
        assertTrue(convertedCopyParam instanceof NodeRef);
        assertEquals(DUMMY_FOLDER_NODE, convertedCopyParam);
    }

    @Test
    public void testCheckInConversion()
    {
        final String name = CheckInActionExecuter.NAME;
        final String descriptionKey = CheckInActionExecuter.PARAM_DESCRIPTION;
        final String minorChangeKey = CheckInActionExecuter.PARAM_MINOR_CHANGE;
        String description = "dummy description";
        final Map<String, Serializable> params = Map.of(descriptionKey, description, minorChangeKey, true);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(descriptionKey)).willReturn(actionDefinitionParam1);
        given(actionDefinition.getParameterDefintion(minorChangeKey)).willReturn(actionDefinitionParam2);
        final QName text = DataTypeDefinition.TEXT;
        given(actionDefinitionParam1.getType()).willReturn(text);
        final QName bool = DataTypeDefinition.BOOLEAN;
        given(actionDefinitionParam2.getType()).willReturn(bool);

        given(dictionaryService.getDataType(text)).willReturn(dataTypeDefinition1);
        given(dataTypeDefinition1.getJavaClassName()).willReturn(String.class.getName());
        given(dictionaryService.getDataType(bool)).willReturn(dataTypeDefinition2);
        given(dataTypeDefinition2.getJavaClassName()).willReturn(Boolean.class.getName());

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(descriptionKey);
        then(actionDefinition).should().getParameterDefintion(minorChangeKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should(times(2)).getDataType(bool);
        then(dictionaryService).should(times(2)).getDataType(text);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).shouldHaveNoInteractions();

        final Serializable convertedDescriptionParam = convertedParams.get(descriptionKey);
        assertTrue(convertedDescriptionParam instanceof String);
        assertEquals(description, convertedDescriptionParam);
        final Serializable convertedMinorChangeParam = convertedParams.get(minorChangeKey);
        assertTrue(convertedMinorChangeParam instanceof Boolean);
        assertTrue((Boolean) convertedMinorChangeParam);
    }

    @Test
    public void testCheckOutConversion()
    {
        final String name = CheckOutActionExecuter.NAME;
        final String destinationFolderKey = CheckOutActionExecuter.PARAM_DESTINATION_FOLDER;
        final String assocNameKey = CheckOutActionExecuter.PARAM_ASSOC_QNAME;
        final String assocTypeKey = CheckOutActionExecuter.PARAM_ASSOC_TYPE_QNAME;
        final Map<String, Serializable> params =
                Map.of(destinationFolderKey, DUMMY_FOLDER_NODE_ID, assocNameKey, CHECKOUT_ASPECT, assocTypeKey, CONTAINS_ASPECT);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(destinationFolderKey)).willReturn(actionDefinitionParam1);
        final QName nodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam1.getType()).willReturn(nodeRef);
        given(actionDefinition.getParameterDefintion(assocNameKey)).willReturn(actionDefinitionParam2);
        final QName qname = DataTypeDefinition.QNAME;
        given(actionDefinitionParam2.getType()).willReturn(qname);
        given(actionDefinition.getParameterDefintion(assocTypeKey)).willReturn(actionDefinitionParam3);
        given(actionDefinitionParam3.getType()).willReturn(qname);

        given(dictionaryService.getDataType(nodeRef)).willReturn(dataTypeDefinition1);
        given(dictionaryService.getDataType(qname)).willReturn(dataTypeDefinition2);
        given(namespaceService.getNamespaceURI(any())).willReturn(NamespaceService.DICTIONARY_MODEL_1_0_URI);

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(destinationFolderKey);
        then(actionDefinition).should().getParameterDefintion(assocNameKey);
        then(actionDefinition).should().getParameterDefintion(assocTypeKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should(times(2)).getDataType(qname);
        then(dictionaryService).should().getDataType(nodeRef);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).should(times(2)).getNamespaceURI(any());
        then(namespaceService).shouldHaveNoMoreInteractions();

        final Serializable convertedDestinationParam = convertedParams.get(destinationFolderKey);
        assertTrue(convertedDestinationParam instanceof NodeRef);
        assertEquals(DUMMY_FOLDER_NODE, convertedDestinationParam);
        final Serializable convertedAssocNameParam = convertedParams.get(assocNameKey);
        assertTrue(convertedAssocNameParam instanceof QName);
        assertEquals(CHECKOUT, ((QName) convertedAssocNameParam).getLocalName());
        assertEquals(CHECKOUT_ASPECT, ((QName) convertedAssocNameParam).getPrefixString());
        assertEquals(NamespaceService.DICTIONARY_MODEL_1_0_URI, ((QName) convertedAssocNameParam).getNamespaceURI());
        final Serializable convertedAssocTypeParam = convertedParams.get(assocTypeKey);
        assertTrue(convertedAssocTypeParam instanceof QName);
        assertEquals(CONTAINS, ((QName) convertedAssocTypeParam).getLocalName());
        assertEquals(CONTAINS_ASPECT, ((QName) convertedAssocTypeParam).getPrefixString());
        assertEquals(NamespaceService.DICTIONARY_MODEL_1_0_URI, ((QName) convertedAssocTypeParam).getNamespaceURI());
    }

    @Test
    public void testCategoryLinkConversion()
    {
        final String name = LinkCategoryActionExecuter.NAME;
        final String categoryAspectKey = LinkCategoryActionExecuter.PARAM_CATEGORY_ASPECT;
        final String categoryValueKey = LinkCategoryActionExecuter.PARAM_CATEGORY_VALUE;
        final Map<String, Serializable> params = Map.of(categoryAspectKey, CLASSIFIABLE_ASPECT, categoryValueKey, DUMMY_FOLDER_NODE_ID);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(categoryAspectKey)).willReturn(actionDefinitionParam1);
        final QName qname = DataTypeDefinition.QNAME;
        given(actionDefinitionParam1.getType()).willReturn(qname);
        given(actionDefinition.getParameterDefintion(categoryValueKey)).willReturn(actionDefinitionParam2);
        final QName nodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam2.getType()).willReturn(nodeRef);

        given(dictionaryService.getDataType(nodeRef)).willReturn(dataTypeDefinition1);
        given(dictionaryService.getDataType(qname)).willReturn(dataTypeDefinition2);
        given(namespaceService.getNamespaceURI(any())).willReturn(NamespaceService.DICTIONARY_MODEL_1_0_URI);

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(categoryAspectKey);
        then(actionDefinition).should().getParameterDefintion(categoryValueKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should().getDataType(qname);
        then(dictionaryService).should().getDataType(nodeRef);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).should().getNamespaceURI(any());
        then(namespaceService).shouldHaveNoMoreInteractions();

        final Serializable convertedCatValueParam = convertedParams.get(categoryAspectKey);
        assertTrue(convertedCatValueParam instanceof QName);
        assertEquals(CLASSIFIABLE, ((QName) convertedCatValueParam).getLocalName());
        assertEquals(CLASSIFIABLE_ASPECT, ((QName) convertedCatValueParam).getPrefixString());
        assertEquals(NamespaceService.DICTIONARY_MODEL_1_0_URI, ((QName) convertedCatValueParam).getNamespaceURI());
        final Serializable convertedDestinationParam = convertedParams.get(categoryValueKey);
        assertTrue(convertedDestinationParam instanceof NodeRef);
        assertEquals(DUMMY_FOLDER_NODE, convertedDestinationParam);
    }

    @Test
    public void testRemoveAspectConversion()
    {
        final String name = RemoveFeaturesActionExecuter.NAME;
        final String aspectNameKey = RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME;
        final Map<String, Serializable> params = Map.of(aspectNameKey, VERSIONABLE_ASPECT);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(aspectNameKey)).willReturn(actionDefinitionParam1);
        final QName qname = DataTypeDefinition.QNAME;
        given(actionDefinitionParam1.getType()).willReturn(qname);
        given(dictionaryService.getDataType(qname)).willReturn(dataTypeDefinition1);
        given(namespaceService.getNamespaceURI(any())).willReturn(NamespaceService.DICTIONARY_MODEL_1_0_URI);

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(aspectNameKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should().getDataType(qname);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).should().getNamespaceURI(any());
        then(namespaceService).shouldHaveNoMoreInteractions();

        final Serializable convertedParam = convertedParams.get(aspectNameKey);
        assertTrue(convertedParam instanceof QName);
        assertEquals(VERSIONABLE, ((QName) convertedParam).getLocalName());
        assertEquals(VERSIONABLE_ASPECT, ((QName) convertedParam).getPrefixString());
        assertEquals(NamespaceService.DICTIONARY_MODEL_1_0_URI, ((QName) convertedParam).getNamespaceURI());
    }

    @Test
    public void testAddWorkflowConversion()
    {
        final String name = SimpleWorkflowActionExecuter.NAME;
        final String approveStepKey = SimpleWorkflowActionExecuter.PARAM_APPROVE_STEP;
        final String approveFolderKey = SimpleWorkflowActionExecuter.PARAM_APPROVE_FOLDER;
        final String approveMoveKey = SimpleWorkflowActionExecuter.PARAM_APPROVE_MOVE;
        final String rejectStepKey = SimpleWorkflowActionExecuter.PARAM_REJECT_STEP;
        final String rejectFolderKey = SimpleWorkflowActionExecuter.PARAM_REJECT_FOLDER;
        final String rejectMoveKey = SimpleWorkflowActionExecuter.PARAM_REJECT_MOVE;
        final String approve = "Approve";
        final String reject = "Reject";
        final Map<String, Serializable> params =
                Map.of(approveStepKey, approve, approveFolderKey, DUMMY_FOLDER_NODE_ID, approveMoveKey, true,
                        rejectStepKey, reject, rejectFolderKey, DUMMY_FOLDER_NODE_ID, rejectMoveKey, true);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(rejectStepKey)).willReturn(actionDefinitionParam1);
        given(actionDefinition.getParameterDefintion(approveStepKey)).willReturn(actionDefinitionParam1);
        final QName text = DataTypeDefinition.TEXT;
        given(actionDefinitionParam1.getType()).willReturn(text, text);
        given(actionDefinition.getParameterDefintion(rejectFolderKey)).willReturn(actionDefinitionParam2);
        given(actionDefinition.getParameterDefintion(approveFolderKey)).willReturn(actionDefinitionParam2);
        final QName nodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam2.getType()).willReturn(nodeRef, nodeRef);
        given(actionDefinition.getParameterDefintion(rejectMoveKey)).willReturn(actionDefinitionParam3);
        given(actionDefinition.getParameterDefintion(approveMoveKey)).willReturn(actionDefinitionParam3);
        final QName bool = DataTypeDefinition.BOOLEAN;
        given(actionDefinitionParam3.getType()).willReturn(bool, bool);

        given(dictionaryService.getDataType(nodeRef)).willReturn(dataTypeDefinition1);
        given(dictionaryService.getDataType(text)).willReturn(dataTypeDefinition2);
        given(dataTypeDefinition2.getJavaClassName()).willReturn(String.class.getName());
        given(dictionaryService.getDataType(bool)).willReturn(dataTypeDefinition3);
        given(dataTypeDefinition3.getJavaClassName()).willReturn(Boolean.class.getName());

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(approveStepKey);
        then(actionDefinition).should().getParameterDefintion(approveFolderKey);
        then(actionDefinition).should().getParameterDefintion(approveMoveKey);
        then(actionDefinition).should().getParameterDefintion(rejectStepKey);
        then(actionDefinition).should().getParameterDefintion(rejectFolderKey);
        then(actionDefinition).should().getParameterDefintion(rejectMoveKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should(times(4)).getDataType(text);
        then(dictionaryService).should(times(2)).getDataType(nodeRef);
        then(dictionaryService).should(times(4)).getDataType(bool);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).shouldHaveNoInteractions();

        final Serializable convertedApproveStepParam = convertedParams.get(approveStepKey);
        assertTrue(convertedApproveStepParam instanceof String);
        assertEquals(approve, convertedApproveStepParam);
        final Serializable convertedRejectStepParam = convertedParams.get(rejectStepKey);
        assertTrue(convertedRejectStepParam instanceof String);
        assertEquals(reject, convertedRejectStepParam);
        final Serializable convertedApproveFolderParam = convertedParams.get(approveFolderKey);
        assertTrue(convertedApproveFolderParam instanceof NodeRef);
        assertEquals(DUMMY_FOLDER_NODE, convertedApproveFolderParam);
        final Serializable convertedRejectFolderParam = convertedParams.get(rejectFolderKey);
        assertTrue(convertedRejectFolderParam instanceof NodeRef);
        assertEquals(DUMMY_FOLDER_NODE, convertedRejectFolderParam);
        final Serializable convertedApproveMoveParam = convertedParams.get(approveMoveKey);
        assertTrue(convertedApproveMoveParam instanceof Boolean);
        assertTrue((Boolean) convertedApproveMoveParam);
        final Serializable convertedRejectMoveParam = convertedParams.get(rejectMoveKey);
        assertTrue(convertedRejectMoveParam instanceof Boolean);
        assertTrue((Boolean) convertedRejectMoveParam);
    }

    @Test
    public void testSetPropertyConversion()
    {
        final String name = SetPropertyValueActionExecuter.NAME;
        final String propertyNameKey = SetPropertyValueActionExecuter.PARAM_PROPERTY;
        final String propertyValueKey = SetPropertyValueActionExecuter.PARAM_VALUE;
        final String propertyTypeKey = "prop_type";
        final String dummy_key_value = "dummy_key_value";
        final String propType = "d:text";
        final Map<String, Serializable> params =
                Map.of(propertyNameKey, IDENTIFIER_ASPECT, propertyValueKey, dummy_key_value, propertyTypeKey, propType);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(propertyNameKey)).willReturn(actionDefinitionParam1);
        final QName qname = DataTypeDefinition.QNAME;
        given(actionDefinitionParam1.getType()).willReturn(qname);
        given(actionDefinition.getParameterDefintion(propertyValueKey)).willReturn(actionDefinitionParam2);
        final QName any = DataTypeDefinition.ANY;
        given(actionDefinitionParam2.getType()).willReturn(any);
        given(actionDefinition.getParameterDefintion(propertyTypeKey)).willReturn(null);
        given(actionDefinition.getAdhocPropertiesAllowed()).willReturn(true);

        given(dictionaryService.getDataType(qname)).willReturn(dataTypeDefinition1);
        given(dictionaryService.getDataType(any)).willReturn(dataTypeDefinition2);
        given(dataTypeDefinition2.getJavaClassName()).willReturn(Object.class.getName());
        given(namespaceService.getNamespaceURI(any())).willReturn(NamespaceService.DICTIONARY_MODEL_1_0_URI);

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(propertyNameKey);
        then(actionDefinition).should().getParameterDefintion(propertyValueKey);
        then(actionDefinition).should().getParameterDefintion(propertyTypeKey);
        then(actionDefinition).should().getAdhocPropertiesAllowed();
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should().getDataType(qname);
        then(dictionaryService).should(times(2)).getDataType(any);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).should().getNamespaceURI(any());
        then(namespaceService).shouldHaveNoMoreInteractions();

        final Serializable convertedPropNameParam = convertedParams.get(propertyNameKey);
        assertTrue(convertedPropNameParam instanceof QName);
        assertEquals(IDENTIFIER, ((QName) convertedPropNameParam).getLocalName());
        assertEquals(IDENTIFIER_ASPECT, ((QName) convertedPropNameParam).getPrefixString());
        assertEquals(NamespaceService.DICTIONARY_MODEL_1_0_URI, ((QName) convertedPropNameParam).getNamespaceURI());

        final Serializable convertedPropValParam = convertedParams.get(propertyValueKey);
        assertTrue(convertedPropValParam instanceof String);
        assertEquals(dummy_key_value, convertedPropValParam);
        final Serializable convertedPropTypeParam = convertedParams.get(propertyTypeKey);
        assertTrue(convertedPropTypeParam instanceof String);
        assertEquals(propType, convertedPropTypeParam);
    }

    @Test
    public void testNonExistentNodeParam()
    {
        final String name = CopyActionExecuter.NAME;
        final Map<String, Serializable> params = Map.of(PARAM_DESTINATION_FOLDER, "non://existent/node");

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(PARAM_DESTINATION_FOLDER)).willReturn(actionDefinitionParam1);
        final QName nodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam1.getType()).willReturn(nodeRef);

        given(dictionaryService.getDataType(nodeRef)).willReturn(dataTypeDefinition1);

        //when
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> objectUnderTest.getConvertedParams(params, name));
    }

    @Test
    public void testNoReadPermissionForNodeParam()
    {
        final String name = CopyActionExecuter.NAME;
        String permissionDeniedNodeId = "permission://denied/node";
        final Map<String, Serializable> params = Map.of(PARAM_DESTINATION_FOLDER, permissionDeniedNodeId);
        NodeRef permissionDeniedNode = new NodeRef(permissionDeniedNodeId);
        given(nodes.validateOrLookupNode(permissionDeniedNodeId)).willReturn(permissionDeniedNode);
        given(permissionService.hasReadPermission(permissionDeniedNode)).willReturn(DENIED);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(PARAM_DESTINATION_FOLDER)).willReturn(actionDefinitionParam1);
        final QName nodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam1.getType()).willReturn(nodeRef);

        given(dictionaryService.getDataType(nodeRef)).willReturn(dataTypeDefinition1);

        //when
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(() -> objectUnderTest.getConvertedParams(params, name));
    }

    @Test
    public void testNullParamValue()
    {
        final String name = CheckOutActionExecuter.NAME;
        final String destinationFolderKey = CheckOutActionExecuter.PARAM_DESTINATION_FOLDER;
        final String assocNameKey = CheckOutActionExecuter.PARAM_ASSOC_QNAME;
        final String assocTypeKey = CheckOutActionExecuter.PARAM_ASSOC_TYPE_QNAME;
        final Map<String, Serializable> params =
                Map.of(destinationFolderKey, Strings.EMPTY, assocNameKey, Strings.EMPTY, assocTypeKey, Strings.EMPTY);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);

        //when
        assertThrows(InvalidArgumentException.class, () ->objectUnderTest.getConvertedParams(params, name));

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).shouldHaveNoInteractions();
        then(dictionaryService).shouldHaveNoInteractions();
        then(namespaceService).shouldHaveNoInteractions();
    }

    @Test
    public void testInvalidActionDefinitionConversion() {
        final String invalidName = "dummy-definition";
        final Map<String, Serializable> params = Map.of("dummy-key", "dummy-value");
        given(actionService.getActionDefinition(invalidName)).willReturn(null);
        //when-then
        assertThrows(NotFoundException.class, () ->objectUnderTest.getConvertedParams(params, invalidName));

        given(actionService.getActionDefinition(invalidName)).willThrow(NotFoundException.class);
        //when-then
        assertThrows(NotFoundException.class, () ->objectUnderTest.getConvertedParams(params, invalidName));
    }

    @Test
    public void testQnameServiceModelParamConversion() {
        given(namespaceService.getPrefixes(any())).willReturn(List.of("cm"));
        final String qname = "{cm-dummy-prefix}audio";
        final Serializable qnameParam = QName.createQName(qname);

        //when
        final Serializable convertedParam = objectUnderTest.convertParamFromServiceModel(qnameParam);

        then(namespaceService).should().getPrefixes(any());
        then(namespaceService).shouldHaveNoMoreInteractions();
        assertEquals("cm:audio", convertedParam);
    }

    @Test
    public void testNodeRefServiceModelParamConversion() {
        //given
        final Serializable nodeRefParam = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_FOLDER_NODE_ID);

        //when
        final Serializable convertedParam = objectUnderTest.convertParamFromServiceModel(nodeRefParam);

        then(namespaceService).shouldHaveNoInteractions();
        assertEquals(DUMMY_FOLDER_NODE_ID, convertedParam);
    }

    @Test
    public void testOtherServiceModelParamConversion() {
        //given
        final Serializable dummyStringParam = "dummy-param";
        //when
        final Serializable convertedParam = objectUnderTest.convertParamFromServiceModel(dummyStringParam);

        then(namespaceService).shouldHaveNoInteractions();
        assertEquals(dummyStringParam, convertedParam);
    }
}
