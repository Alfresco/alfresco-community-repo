/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import static org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Unit tests for {@link NodesImpl#mapToNodeProperties(Map)}, in particular the handling of multi-valued {@code d:noderef} properties (MNT-25833).
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class NodesImplTest
{
    private static final String MULTI_NODE_REF_PROP = "{http://www.alfresco.org/test/repro/1.0}multiRefs";
    private static final String SINGLE_NODE_REF_PROP = "{http://www.alfresco.org/test/repro/1.0}singleRef";
    private static final QName MULTI_NODE_REF_QNAME = QName.createQName(MULTI_NODE_REF_PROP);
    private static final QName SINGLE_NODE_REF_QNAME = QName.createQName(SINGLE_NODE_REF_PROP);

    private static final String REF_1_UUID = "11111111-1111-1111-1111-111111111111";
    private static final String REF_2_UUID = "22222222-2222-2222-2222-222222222222";

    @Mock
    private DictionaryService dictionaryService;
    @Mock
    private PropertyDefinition multiValuedNodeRefPropDef;
    @Mock
    private PropertyDefinition singleValuedNodeRefPropDef;
    @Mock
    private DataTypeDefinition nodeRefDataType;

    @InjectMocks
    private NodesImpl nodesImpl;

    @Before
    public void setUp()
    {
        given(nodeRefDataType.getName()).willReturn(DataTypeDefinition.NODE_REF);

        given(multiValuedNodeRefPropDef.getDataType()).willReturn(nodeRefDataType);
        given(multiValuedNodeRefPropDef.isMultiValued()).willReturn(true);

        given(singleValuedNodeRefPropDef.getDataType()).willReturn(nodeRefDataType);
        given(singleValuedNodeRefPropDef.isMultiValued()).willReturn(false);

        given(dictionaryService.getProperty(MULTI_NODE_REF_QNAME)).willReturn(multiValuedNodeRefPropDef);
        given(dictionaryService.getProperty(SINGLE_NODE_REF_QNAME)).willReturn(singleValuedNodeRefPropDef);
    }

    @Test
    public void testMultiValuedNodeRef_bareUuids_convertedToNodeRefList()
    {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put(MULTI_NODE_REF_PROP, Arrays.asList(REF_1_UUID, REF_2_UUID));

        Map<QName, Serializable> result = nodesImpl.mapToNodeProperties(props);

        Serializable value = result.get(MULTI_NODE_REF_QNAME);
        assertThat(value).isInstanceOf(List.class);
        assertThat((List<NodeRef>) value).containsExactly(
                new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_1_UUID),
                new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_2_UUID));
    }

    @Test
    public void testMultiValuedNodeRef_fullNodeRefStrings_convertedToNodeRefList()
    {
        String fullRef1 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_1_UUID).toString();
        String fullRef2 = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_2_UUID).toString();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(MULTI_NODE_REF_PROP, Arrays.asList(fullRef1, fullRef2));

        Map<QName, Serializable> result = nodesImpl.mapToNodeProperties(props);

        assertThat((List<NodeRef>) result.get(MULTI_NODE_REF_QNAME)).containsExactly(
                new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_1_UUID),
                new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_2_UUID));
    }

    @Test
    public void testMultiValuedNodeRef_singleElementList_convertedToOneElementNodeRefList()
    {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put(MULTI_NODE_REF_PROP, Arrays.asList(REF_1_UUID));

        Map<QName, Serializable> result = nodesImpl.mapToNodeProperties(props);

        assertThat((List<NodeRef>) result.get(MULTI_NODE_REF_QNAME))
                .containsExactly(new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_1_UUID));
    }

    @Test
    public void testSingleValuedNodeRef_bareUuid_convertedToNodeRef()
    {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put(SINGLE_NODE_REF_PROP, REF_1_UUID);

        Map<QName, Serializable> result = nodesImpl.mapToNodeProperties(props);

        assertThat(result.get(SINGLE_NODE_REF_QNAME))
                .isEqualTo(new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_1_UUID));
    }

    @Test
    public void testSingleValuedNodeRef_fullNodeRefString_convertedToNodeRef()
    {
        String fullRef = new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_1_UUID).toString();

        Map<String, Object> props = new LinkedHashMap<>();
        props.put(SINGLE_NODE_REF_PROP, fullRef);

        Map<QName, Serializable> result = nodesImpl.mapToNodeProperties(props);

        assertThat(result.get(SINGLE_NODE_REF_QNAME))
                .isEqualTo(new NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, REF_1_UUID));
    }
}
