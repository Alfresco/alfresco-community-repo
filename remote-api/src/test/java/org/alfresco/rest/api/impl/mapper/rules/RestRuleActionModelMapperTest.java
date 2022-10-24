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

import static java.util.Collections.emptyMap;

import static org.alfresco.repo.action.access.ActionAccessRestriction.ACTION_CONTEXT_PARAM_NAME;
import static org.alfresco.repo.action.executer.SetPropertyValueActionExecuter.PARAM_PROPERTY;
import static org.alfresco.repo.action.executer.SetPropertyValueActionExecuter.PARAM_VALUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.rest.api.actions.ActionValidator;
import org.alfresco.rest.api.impl.rules.ActionParameterConverter;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class RestRuleActionModelMapperTest
{

    private static final String ACTION_DEFINITION_NAME = "actionDefName";
    private static final Map<String, Serializable> parameters =
            Map.of(PARAM_PROPERTY, "propertyName", PARAM_VALUE, "propertyValue", ACTION_CONTEXT_PARAM_NAME, "rule");

    @Mock
    private ActionParameterConverter parameterConverter;
    @Mock
    private ActionValidator sampleValidatorMock;

    private RestRuleActionModelMapper objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest = new RestRuleActionModelMapper(parameterConverter, List.of(sampleValidatorMock));
    }

    @Test
    public void testToRestModel()
    {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "ruleId");
        final org.alfresco.service.cmr.action.Action actionServiceModel =
                new ActionImpl(nodeRef, "actionId", ACTION_DEFINITION_NAME, parameters);
        given(parameterConverter.convertParamFromServiceModel(any())).willAnswer(a -> a.getArgument(0));

        //when
        final Action actualAction = objectUnderTest.toRestModel(actionServiceModel);

        then(parameterConverter).should(times(3)).convertParamFromServiceModel(any());
        then(parameterConverter).shouldHaveNoMoreInteractions();
        final Map<String, Serializable> expectedParameters = Map.of(PARAM_PROPERTY, "propertyName", PARAM_VALUE, "propertyValue");
        final Action expectedAction = Action.builder().actionDefinitionId(ACTION_DEFINITION_NAME).params(expectedParameters).create();
        assertThat(actualAction).isNotNull().usingRecursiveComparison().isEqualTo(expectedAction);
    }

    @Test
    public void testToRestModelWithNullValues()
    {
        final org.alfresco.service.cmr.action.Action actionServiceModel = new ActionImpl(null, null, null);
        final Action expectedAction = Action.builder().params(emptyMap()).create();

        //when
        final Action actualAction = objectUnderTest.toRestModel(actionServiceModel);

        then(parameterConverter).shouldHaveNoInteractions();
        assertThat(actualAction).isNotNull().usingRecursiveComparison().isEqualTo(expectedAction);
    }

    @Test
    public void testToServiceModel() {
        final Action action = Action.builder().actionDefinitionId(ACTION_DEFINITION_NAME).params(parameters).create();
        final List<Action> actions = List.of(action);
        given(parameterConverter.getConvertedParams(parameters, ACTION_DEFINITION_NAME)).willAnswer(a -> a.getArgument(0));

        //when
        final org.alfresco.service.cmr.action.Action serviceModelAction = objectUnderTest.toServiceModel(actions);
        then(parameterConverter).should().getConvertedParams(parameters, ACTION_DEFINITION_NAME);
        then(parameterConverter).shouldHaveNoMoreInteractions();
        assertThat(serviceModelAction).isNotNull();
    }

    @Test
    public void testToServiceModelFromEmptyActions() {
        final List<Action> actions = Collections.emptyList();

        //when
        final org.alfresco.service.cmr.action.Action serviceModelAction = objectUnderTest.toServiceModel(actions);

        then(parameterConverter).shouldHaveNoInteractions();
        assertThat(serviceModelAction).isNull();
    }

    @Test
    public void testToServiceModelWithNullParams()
    {
        final Action action = Action.builder().actionDefinitionId(ACTION_DEFINITION_NAME).params(null).create();
        final List<Action> actions = List.of(action);

        //when
        final org.alfresco.service.cmr.action.Action serviceModelAction = objectUnderTest.toServiceModel(actions);
        then(parameterConverter).should().getConvertedParams(emptyMap(), ACTION_DEFINITION_NAME);
        then(parameterConverter).shouldHaveNoMoreInteractions();
        assertThat(serviceModelAction).isNotNull();
    }
}
