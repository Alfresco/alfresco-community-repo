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

package org.alfresco.rest.api.model.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.action.evaluator.CompareMimeTypeEvaluator;
import org.alfresco.repo.action.evaluator.ComparePropertyValueEvaluator;
import org.alfresco.repo.action.evaluator.HasAspectEvaluator;
import org.alfresco.repo.action.evaluator.HasChildEvaluator;
import org.alfresco.repo.action.evaluator.HasTagEvaluator;
import org.alfresco.repo.action.evaluator.HasVersionHistoryEvaluator;
import org.alfresco.repo.action.evaluator.InCategoryEvaluator;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.action.evaluator.NoConditionEvaluator;
import org.alfresco.repo.action.evaluator.compare.ComparePropertyValueOperation;
import org.alfresco.repo.action.evaluator.compare.ContentPropertyName;
import org.alfresco.rest.api.Nodes;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class SimpleConditionTest
{
    private static final boolean NULL_RESULT = true;
    private static final boolean NOT_INVERTED = false;
    private static final String PARAMETER_DEFAULT = "value";

    private final Nodes nodes = mock(Nodes.class);
    private final NamespaceService namespaceService = mock(NamespaceService.class);

    @Before
    public void setUp() throws Exception
    {
        given(namespaceService.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI)).willReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));
        given(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).willReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        given(namespaceService.getPrefixes(NamespaceService.AUDIO_MODEL_1_0_URI)).willReturn(List.of(NamespaceService.AUDIO_MODEL_PREFIX));
        given(namespaceService.getNamespaceURI(NamespaceService.AUDIO_MODEL_PREFIX)).willReturn(NamespaceService.AUDIO_MODEL_1_0_URI);
    }

    private static List<TestData> getTestData() {
        return List.of(
            TestData.of(ComparePropertyValueEvaluator.NAME),
            TestData.of(CompareMimeTypeEvaluator.NAME),
            TestData.of(HasAspectEvaluator.NAME),
            TestData.of(HasChildEvaluator.NAME, NULL_RESULT),
            TestData.of(HasTagEvaluator.NAME),
            TestData.of(HasVersionHistoryEvaluator.NAME, NULL_RESULT),
            TestData.of(InCategoryEvaluator.NAME),
            TestData.of(IsSubTypeEvaluator.NAME),
            TestData.of(NoConditionEvaluator.NAME, NULL_RESULT),
            TestData.of("fake-definition-name", NULL_RESULT),
            TestData.of("", NULL_RESULT),
            TestData.of(null, NULL_RESULT)
        );
    }

    @Test
    public void testFrom()
    {
        for (TestData testData : getTestData())
        {
            final ActionCondition actionCondition = createActionCondition(testData.conditionDefinitionName);

            // when
            final SimpleCondition actualSimpleCondition = SimpleCondition.from(actionCondition, namespaceService);

            assertThat(Objects.isNull(actualSimpleCondition)).isEqualTo(testData.isNullResult);
            if (!testData.isNullResult)
            {
                assertThat(actualSimpleCondition.getField()).isNotEmpty();
                assertThat(actualSimpleCondition.getComparator()).isNotEmpty();
                assertThat(actualSimpleCondition.getParameter()).isNotEmpty();
            }
        }
    }

    @Test
    public void testFromNullValue()
    {
        // when
        final SimpleCondition actualSimpleCondition = SimpleCondition.from(null, namespaceService);

        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testFromActionConditionWithoutDefinitionName()
    {
        final ActionCondition actionCondition = new ActionConditionImpl("fake-id", null, createParameterValues());

        // when
        final SimpleCondition actualSimpleCondition = SimpleCondition.from(actionCondition, namespaceService);

        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testFromActionConditionWithoutParameterValues()
    {
        final ActionCondition actionCondition = new ActionConditionImpl("fake-id", "fake-def-name", null);

        // when
        final SimpleCondition actualSimpleCondition = SimpleCondition.from(actionCondition, namespaceService);

        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testListOf()
    {
        final List<ActionCondition> actionConditions = List.of(
            createActionCondition(ComparePropertyValueEvaluator.NAME),
            createActionCondition(CompareMimeTypeEvaluator.NAME)
        );

        // when
        final List<SimpleCondition> actualSimpleConditions = SimpleCondition.listOf(actionConditions, namespaceService);

        final List<SimpleCondition> expectedSimpleConditions = List.of(
            SimpleCondition.builder()
                .field("content-property")
                .comparator("operation")
                .parameter("value")
                .create(),
            SimpleCondition.builder()
                .field(SimpleCondition.PARAM_MIMETYPE)
                .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
                .parameter("value")
                .create()
        );
        assertThat(actualSimpleConditions)
            .isNotNull()
            .containsExactlyElementsOf(expectedSimpleConditions);
    }

    @Test
    public void testListOfEmptyActionConditions()
    {
        // when
        final List<SimpleCondition> actualSimpleConditions = SimpleCondition.listOf(Collections.emptyList(), namespaceService);

        assertThat(actualSimpleConditions).isNull();
    }

    @Test
    public void testListOfNullActionConditions()
    {
        // when
        final List<SimpleCondition> actualSimpleConditions = SimpleCondition.listOf(null, namespaceService);

        assertThat(actualSimpleConditions).isNull();
    }

    @Test
    public void testListOfActionConditionsContainingNull()
    {
        final List<ActionCondition> actionConditions = new ArrayList<>();
        actionConditions.add(null);

        // when
        final List<SimpleCondition> actualSimpleConditions = SimpleCondition.listOf(actionConditions, namespaceService);

        assertThat(actualSimpleConditions).isNotNull().isEmpty();
    }

    @Test
    public void testToServiceModel_withSizeContentProperty()
    {
        final SimpleCondition simpleCondition = createSimpleCondition(ContentPropertyName.SIZE.toString().toLowerCase());

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(NOT_INVERTED, nodes, namespaceService);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, ContentPropertyName.SIZE.toString());
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.TYPE_CONTENT);
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS.toString());
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, PARAMETER_DEFAULT);
        ActionCondition expectedActionCondition = new ActionConditionImpl(null, ComparePropertyValueEvaluator.NAME, expectedParameterValues);
        assertThat(actualActionCondition)
            .isNotNull().usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(expectedActionCondition);
    }

    @Test
    public void testToServiceModel_withoutContentProperty()
    {
        final SimpleCondition simpleCondition = createSimpleCondition(ContentModel.PROP_DESCRIPTION.toPrefixString(namespaceService));

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(NOT_INVERTED, nodes, namespaceService);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_DESCRIPTION);
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS.toString());
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, PARAMETER_DEFAULT);
        final ActionCondition expectedActionCondition = new ActionConditionImpl(null, ComparePropertyValueEvaluator.NAME, expectedParameterValues);
        assertThat(actualActionCondition)
            .isNotNull().usingRecursiveComparison().ignoringFields("id", "parameterValues.property.prefix")
            .isEqualTo(expectedActionCondition);
    }

    @Test
    public void testToServiceModel_compareMimetype()
    {
        final SimpleCondition simpleCondition = createSimpleCondition(SimpleCondition.PARAM_MIMETYPE);

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(NOT_INVERTED, nodes, namespaceService);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.TYPE_CONTENT);
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, PARAMETER_DEFAULT);
        final ActionCondition expectedActionCondition = new ActionConditionImpl(null, CompareMimeTypeEvaluator.NAME, expectedParameterValues);
        assertThat(actualActionCondition)
            .isNotNull().usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(expectedActionCondition);
    }

    @Test
    public void testToServiceModel_hasAspect()
    {
        final QName audioAspect = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, NamespaceService.AUDIO_MODEL_PREFIX);
        final SimpleCondition simpleCondition = createSimpleCondition(HasAspectEvaluator.PARAM_ASPECT, audioAspect.toPrefixString(namespaceService));

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(NOT_INVERTED, nodes, namespaceService);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(HasAspectEvaluator.PARAM_ASPECT, audioAspect);
        final ActionCondition expectedActionCondition = new ActionConditionImpl(null, HasAspectEvaluator.NAME, expectedParameterValues);
        assertThat(actualActionCondition)
            .isNotNull().usingRecursiveComparison().ignoringFields("id", "parameterValues.aspect.prefix")
            .isEqualTo(expectedActionCondition);
    }

    @Test
    public void testToServiceModel_hasTag()
    {
        final String tag = "some tag";
        final SimpleCondition simpleCondition = createSimpleCondition(HasTagEvaluator.PARAM_TAG, tag);

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(NOT_INVERTED, nodes, namespaceService);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(HasTagEvaluator.PARAM_TAG, tag);
        final ActionCondition expectedActionCondition = new ActionConditionImpl(null, HasTagEvaluator.NAME, expectedParameterValues);
        assertThat(actualActionCondition)
            .isNotNull().usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(expectedActionCondition);
    }

    @Test
    public void testToServiceModel_inCategory()
    {
        final SimpleCondition simpleCondition = createSimpleCondition(SimpleCondition.PARAM_CATEGORY);
        final NodeRef defaultNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PARAMETER_DEFAULT);
        given(nodes.validateOrLookupNode(PARAMETER_DEFAULT, null)).willReturn(defaultNodeRef);

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(NOT_INVERTED, nodes, namespaceService);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_ASPECT, ContentModel.ASPECT_GEN_CLASSIFIABLE);
        expectedParameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_VALUE, defaultNodeRef);
        final ActionCondition expectedActionCondition = new ActionConditionImpl(null, InCategoryEvaluator.NAME, expectedParameterValues);
        assertThat(actualActionCondition)
            .isNotNull().usingRecursiveComparison().ignoringFields("id")
            .isEqualTo(expectedActionCondition);
    }

    @Test
    public void testToServiceModel_isSubType()
    {
        final SimpleCondition simpleCondition = createSimpleCondition(IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_FOLDER.toPrefixString(namespaceService));

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(NOT_INVERTED, nodes, namespaceService);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_FOLDER);
        final ActionCondition expectedActionCondition = new ActionConditionImpl(null, IsSubTypeEvaluator.NAME, expectedParameterValues);
        assertThat(actualActionCondition)
            .isNotNull().usingRecursiveComparison().ignoringFields("id", "parameterValues.type.prefix")
            .isEqualTo(expectedActionCondition);
    }

    @Test
    public void testToServiceModel_inverted()
    {
        final SimpleCondition simpleCondition = createSimpleCondition(ContentModel.PROP_DESCRIPTION.toPrefixString(namespaceService));

        // when
        final ActionCondition actualActionCondition = simpleCondition.toServiceModel(!NOT_INVERTED, nodes, namespaceService);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.PROP_DESCRIPTION);
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, ComparePropertyValueOperation.EQUALS.toString());
        expectedParameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, PARAMETER_DEFAULT);
        final ActionCondition expectedActionCondition = new ActionConditionImpl(null, ComparePropertyValueEvaluator.NAME, expectedParameterValues);
        expectedActionCondition.setInvertCondition(!NOT_INVERTED);
        assertThat(actualActionCondition)
            .isNotNull().usingRecursiveComparison().ignoringFields("id", "parameterValues.property.prefix")
            .isEqualTo(expectedActionCondition);
    }

    private static SimpleCondition createSimpleCondition(final String field)
    {
        return createSimpleCondition(field, PARAMETER_DEFAULT);
    }

    private static SimpleCondition createSimpleCondition(final String field, final String parameter)
    {
        return SimpleCondition.builder()
            .field(field)
            .comparator(ComparePropertyValueOperation.EQUALS.toString().toLowerCase())
            .parameter(parameter)
            .create();
    }

    private static ActionCondition createActionCondition(final String actionDefinitionName)
    {
        return new ActionConditionImpl("fake-id", actionDefinitionName, createParameterValues());
    }

    private static Map<String, Serializable> createParameterValues() {
        final QName audioAspect = QName.createQName(NamespaceService.AUDIO_MODEL_1_0_URI, NamespaceService.AUDIO_MODEL_PREFIX);
        final NodeRef defaultNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PARAMETER_DEFAULT);
        final Map<String, Serializable> parameterValues = new HashMap<>();
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_CONTENT_PROPERTY, "content-property");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_PROPERTY, ContentModel.TYPE_CONTENT);
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_OPERATION, "operation");
        parameterValues.put(ComparePropertyValueEvaluator.PARAM_VALUE, "value");
        parameterValues.put(HasAspectEvaluator.PARAM_ASPECT, audioAspect);
        parameterValues.put(HasTagEvaluator.PARAM_TAG, "tag");
        parameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_ASPECT, "category-aspect");
        parameterValues.put(InCategoryEvaluator.PARAM_CATEGORY_VALUE, defaultNodeRef);
        parameterValues.put(IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_FOLDER);

        return parameterValues;
    }

    private static class TestData
    {
        String conditionDefinitionName;
        boolean isNullResult;

        public TestData(String conditionDefinitionName, boolean isNullResult)
        {
            this.conditionDefinitionName = conditionDefinitionName;
            this.isNullResult = isNullResult;
        }

        public static TestData of(String conditionDefinitionName) {
            return new TestData(conditionDefinitionName, false);
        }

        public static TestData of(String conditionDefinitionName, boolean isNullResult) {
            return new TestData(conditionDefinitionName, isNullResult);
        }
    }
}