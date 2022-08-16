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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.executer.AddFeaturesActionExecuter;
import org.alfresco.repo.action.executer.CopyActionExecuter;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
    private static final StoreRef STORE_REF = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
    private static final String DUMMY_FOLDER_NODE_ID = "dummy-folder-node";
    private static final String DUMMY_FOLDER_NODE_REF = STORE_REF + "/" + DUMMY_FOLDER_NODE_ID;

    @Mock
    private DictionaryService dictionaryService;
    @Mock
    private ActionService actionService;
    @Mock
    private NamespaceService namespaceService;

    @Mock
    private ActionDefinition actionDefinition;
    @Mock
    private ParameterDefinition actionDefinitionParam;
    @Mock
    private DataTypeDefinition dataTypeDefinition;

    @InjectMocks
    private ActionParameterConverter objectUnderTest;

    @Test
    public void testAddAspectConversion()
    {
        final String name = AddFeaturesActionExecuter.NAME;
        final String aspectNameKey = AddFeaturesActionExecuter.PARAM_ASPECT_NAME;
        final Map<String, Serializable> params = new HashMap<>(1);
        params.put(aspectNameKey, VERSIONABLE_ASPECT);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(aspectNameKey)).willReturn(actionDefinitionParam);
        final QName qname = DataTypeDefinition.QNAME;
        given(actionDefinitionParam.getType()).willReturn(qname);
        given(dictionaryService.getDataType(qname)).willReturn(dataTypeDefinition);
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
        assertThat(convertedParam instanceof QName).isTrue();
        assertThat(((QName) convertedParam).getLocalName()).isEqualTo(VERSIONABLE);
        assertThat(((QName) convertedParam).getPrefixString()).isEqualTo(VERSIONABLE_ASPECT);
        assertThat(((QName) convertedParam).getNamespaceURI()).isEqualTo(NamespaceService.DICTIONARY_MODEL_1_0_URI);
    }

    @Test
    public void testCopyConversion()
    {
        final String name = CopyActionExecuter.NAME;
        final String destinationFolderKey = CopyActionExecuter.PARAM_DESTINATION_FOLDER;
        final String deepCopyKey = CopyActionExecuter.PARAM_DEEP_COPY;
        final Map<String, Serializable> params = new HashMap<>(2);
        params.put(destinationFolderKey, DUMMY_FOLDER_NODE_REF);
        params.put(deepCopyKey, true);

        given(actionService.getActionDefinition(name)).willReturn(actionDefinition);
        given(actionDefinition.getParameterDefintion(destinationFolderKey)).willReturn(actionDefinitionParam);
        given(actionDefinition.getParameterDefintion(deepCopyKey)).willReturn(actionDefinitionParam);
        final QName bool = DataTypeDefinition.BOOLEAN;
        final QName nodeRef = DataTypeDefinition.NODE_REF;
        given(actionDefinitionParam.getType()).willReturn(bool, nodeRef);

        given(dictionaryService.getDataType(bool)).willReturn(dataTypeDefinition);
        given(dictionaryService.getDataType(nodeRef)).willReturn(dataTypeDefinition);
        given(dataTypeDefinition.getJavaClassName()).willReturn(Boolean.class.getName(), NodeRef.class.getName());

        //when
        final Map<String, Serializable> convertedParams = objectUnderTest.getConvertedParams(params, name);

        then(actionService).should().getActionDefinition(name);
        then(actionService).shouldHaveNoMoreInteractions();
        then(actionDefinition).should().getParameterDefintion(destinationFolderKey);
        then(actionDefinition).should().getParameterDefintion(deepCopyKey);
        then(actionDefinition).shouldHaveNoMoreInteractions();
        then(dictionaryService).should(times(2)).getDataType(bool);
        then(dictionaryService).should(times(2)).getDataType(nodeRef);
        then(dictionaryService).shouldHaveNoMoreInteractions();
        then(namespaceService).shouldHaveNoInteractions();

        final Serializable convertedCopyParam = convertedParams.get(destinationFolderKey);
        assertThat(convertedCopyParam instanceof NodeRef).isTrue();
        assertThat(((NodeRef) convertedCopyParam).getStoreRef()).isEqualTo(STORE_REF);
        assertThat(((NodeRef) convertedCopyParam).getId()).isEqualTo(DUMMY_FOLDER_NODE_ID);
        final Serializable convertedDeepCopyParam = convertedParams.get(deepCopyKey);
        assertThat(convertedDeepCopyParam instanceof Boolean).isTrue();
        assertThat(((Boolean) convertedDeepCopyParam)).isTrue();
    }
}
