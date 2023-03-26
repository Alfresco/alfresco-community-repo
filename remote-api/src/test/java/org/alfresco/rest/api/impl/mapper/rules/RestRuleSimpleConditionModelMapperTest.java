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

package org.alfresco.rest.api.impl.mapper.rules;

import static org.alfresco.rest.api.impl.mapper.rules.RestRuleSimpleConditionModelMapper.COMPARATOR_NOT_NULL;
import static org.alfresco.rest.api.impl.mapper.rules.RestRuleSimpleConditionModelMapper.FIELD_NOT_NULL;
import static org.alfresco.rest.api.impl.mapper.rules.RestRuleSimpleConditionModelMapper.INVALID_COMPARATOR_VALUE;
import static org.alfresco.rest.api.impl.mapper.rules.RestRuleSimpleConditionModelMapper.PARAMETER_NOT_NULL;
import static org.alfresco.rest.api.impl.mapper.rules.RestRuleSimpleConditionModelMapper.PARAM_CATEGORY;
import static org.alfresco.rest.api.impl.mapper.rules.RestRuleSimpleConditionModelMapper.PARAM_MIMETYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import org.alfresco.rest.api.model.rules.SimpleCondition;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class RestRuleSimpleConditionModelMapperTest
{
    private static final boolean NULL_RESULT = true;
    private static final String PARAMETER_DEFAULT = "value";

    @Mock
    private NamespaceService namespaceServiceMock;
    @Mock
    private Nodes nodesMock;

    @InjectMocks
    private RestRuleSimpleConditionModelMapper objectUnderTest;

    @Before
    public void setUp() throws Exception
    {
        given(namespaceServiceMock.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI)).willReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));
        given(namespaceServiceMock.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).willReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        given(namespaceServiceMock.getPrefixes(NamespaceService.AUDIO_MODEL_1_0_URI)).willReturn(List.of(NamespaceService.AUDIO_MODEL_PREFIX));
        given(namespaceServiceMock.getNamespaceURI(NamespaceService.AUDIO_MODEL_PREFIX)).willReturn(NamespaceService.AUDIO_MODEL_1_0_URI);
    }

    @Test
    public void testToRestModel()
    {
        for (TestData testData : getTestData())
        {
            final ActionCondition actionCondition = createActionCondition(testData.conditionDefinitionName);

            // when
            final SimpleCondition actualSimpleCondition = objectUnderTest.toRestModel(actionCondition);

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
    public void testToRestModelFromNullValue()
    {
        // when
        final ActionCondition actionCondition = null;
        final SimpleCondition actualSimpleCondition = objectUnderTest.toRestModel(actionCondition);

        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testToRestModelFromActionConditionWithoutDefinitionName()
    {
        final ActionCondition actionCondition = new ActionConditionImpl("fake-id", null, createParameterValues());

        // when
        final SimpleCondition actualSimpleCondition = objectUnderTest.toRestModel(actionCondition);

        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testToRestModelFromActionConditionWithoutParameterValues()
    {
        final ActionCondition actionCondition = new ActionConditionImpl("fake-id", "fake-def-name", null);

        // when
        final SimpleCondition actualSimpleCondition = objectUnderTest.toRestModel(actionCondition);

        assertThat(actualSimpleCondition).isNull();
    }

    @Test
    public void testToRestModelListOfEmptyActionConditions()
    {
        // when
        final List<SimpleCondition> actualSimpleConditions =  objectUnderTest.toRestModels(Collections.emptyList());

        assertThat(actualSimpleConditions).isEmpty();
    }

    @Test
    public void testToRestModelListOfNullActionConditions()
    {
        // when
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> objectUnderTest.toRestModels(
                (Collection<ActionCondition>) null));
    }

    @Test
    public void testToRestModelListOfActionConditionsContainingNull()
    {
        final List<ActionCondition> actionConditions = new ArrayList<>();
        actionConditions.add(null);

        // when
        final List<SimpleCondition> actualSimpleConditions =  objectUnderTest.toRestModels(actionConditions);

        assertThat(actualSimpleConditions).isEmpty();
    }

    @Test
    public void testToServiceModel_withSizeContentProperty()
    {
        final SimpleCondition simpleCondition = createSimpleCondition(ContentPropertyName.SIZE.toString().toLowerCase());

        // when
        final ActionCondition actualActionCondition = objectUnderTest.toServiceModel(simpleCondition);

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
        final String field = NamespaceService.CONTENT_MODEL_PREFIX + QName.NAMESPACE_PREFIX + ContentModel.PROP_DESCRIPTION.toPrefixString();
        final SimpleCondition simpleCondition = createSimpleCondition(field);

        // when
        final ActionCondition actualActionCondition = objectUnderTest.toServiceModel(simpleCondition);

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
        final SimpleCondition simpleCondition = createSimpleCondition(PARAM_MIMETYPE);

        // when
        final ActionCondition actualActionCondition = objectUnderTest.toServiceModel(simpleCondition);

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
        final String field = NamespaceService.AUDIO_MODEL_PREFIX + QName.NAMESPACE_PREFIX + NamespaceService.AUDIO_MODEL_PREFIX;
        final SimpleCondition simpleCondition = createSimpleCondition(HasAspectEvaluator.PARAM_ASPECT, field);

        // when
        final ActionCondition actualActionCondition = objectUnderTest.toServiceModel(simpleCondition);

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
        final ActionCondition actualActionCondition = objectUnderTest.toServiceModel(simpleCondition);

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
        final SimpleCondition simpleCondition = createSimpleCondition(PARAM_CATEGORY);
        final NodeRef defaultNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, PARAMETER_DEFAULT);
        given(nodesMock.validateOrLookupNode(PARAMETER_DEFAULT)).willReturn(defaultNodeRef);

        // when
        final ActionCondition actualActionCondition = objectUnderTest.toServiceModel(simpleCondition);

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
        final String field = NamespaceService.CONTENT_MODEL_PREFIX + QName.NAMESPACE_PREFIX + ContentModel.TYPE_FOLDER.toPrefixString();
        final SimpleCondition simpleCondition = createSimpleCondition(IsSubTypeEvaluator.PARAM_TYPE, field);

        // when
        final ActionCondition actualActionCondition = objectUnderTest.toServiceModel(simpleCondition);

        final Map<String, Serializable> expectedParameterValues = new HashMap<>();
        expectedParameterValues.put(IsSubTypeEvaluator.PARAM_TYPE, ContentModel.TYPE_FOLDER);
        final ActionCondition expectedActionCondition = new ActionConditionImpl(null, IsSubTypeEvaluator.NAME, expectedParameterValues);
        assertThat(actualActionCondition)
                .isNotNull().usingRecursiveComparison().ignoringFields("id", "parameterValues.type.prefix")
                .isEqualTo(expectedActionCondition);
    }

    @Test
    public void testToServiceModel_nullOrBlankParameter()
    {
        final SimpleCondition simpleConditionNullParam = createSimpleCondition(IsSubTypeEvaluator.PARAM_TYPE, null);

        // when
        assertThatThrownBy(() -> objectUnderTest.toServiceModel(simpleConditionNullParam))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(PARAMETER_NOT_NULL);

        final SimpleCondition simpleConditionEmptyParam = createSimpleCondition(IsSubTypeEvaluator.PARAM_TYPE, " ");

        assertThatThrownBy(() -> objectUnderTest.toServiceModel(simpleConditionEmptyParam))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(PARAMETER_NOT_NULL);
    }

    @Test
    public void testToServiceModel_nullOrEmptyField()
    {
        final SimpleCondition simpleConditionNullField = createSimpleCondition(null);

        // when
        assertThatThrownBy(() -> objectUnderTest.toServiceModel(simpleConditionNullField))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(FIELD_NOT_NULL);

        final SimpleCondition simpleConditionEmptyField = createSimpleCondition("");

        // when
        assertThatThrownBy(() -> objectUnderTest.toServiceModel(simpleConditionEmptyField))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(FIELD_NOT_NULL);
    }

    @Test
    public void testToServiceModel_nullOrEmptyComparatorWhenRequired()
    {
        final SimpleCondition simpleConditionNullComparator = SimpleCondition.builder()
                .field("size")
                .comparator(null)
                .parameter("65000")
                .create();

        // when
        assertThatThrownBy(() -> objectUnderTest.toServiceModel(simpleConditionNullComparator))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(COMPARATOR_NOT_NULL);

        final SimpleCondition simpleConditionEmptyComparator = SimpleCondition.builder()
                .field("size")
                .comparator(" ")
                .parameter("65000")
                .create();

        // when
        assertThatThrownBy(() -> objectUnderTest.toServiceModel(simpleConditionEmptyComparator))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(COMPARATOR_NOT_NULL);
    }

    @Test
    public void testToServiceModel_invalidComparator()
    {
        final String comparator = "greaterthan";
        final SimpleCondition simpleConditionNullComparator = SimpleCondition.builder()
                .field("size")
                .comparator(comparator)
                .parameter("65000")
                .create();

        // when
        assertThatThrownBy(() -> objectUnderTest.toServiceModel(simpleConditionNullComparator))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessageContaining(String.format(INVALID_COMPARATOR_VALUE, comparator));
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
