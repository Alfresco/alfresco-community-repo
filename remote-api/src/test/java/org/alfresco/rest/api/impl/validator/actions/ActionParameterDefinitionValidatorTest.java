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

package org.alfresco.rest.api.impl.validator.actions;

import static org.alfresco.rest.api.impl.validator.actions.ActionParameterDefinitionValidator.EMPTY_ACTION_DEFINITION;
import static org.alfresco.rest.api.impl.validator.actions.ActionParameterDefinitionValidator.INVALID_ACTION_DEFINITION;
import static org.alfresco.rest.api.impl.validator.actions.ActionParameterDefinitionValidator.MISSING_PARAMETER;
import static org.alfresco.rest.api.impl.validator.actions.ActionParameterDefinitionValidator.MUST_NOT_CONTAIN_PARAMETER;
import static org.alfresco.rest.api.impl.validator.actions.ActionParameterDefinitionValidator.PARAMS_SHOULD_NOT_BE_EMPTY;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.BOOLEAN;
import static org.alfresco.service.cmr.dictionary.DataTypeDefinition.TEXT;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.model.rules.Action;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.service.Experimental;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Experimental
@RunWith(MockitoJUnitRunner.class)
public class ActionParameterDefinitionValidatorTest
{
    private static final String MANDATORY_PARAM_KEY = "paramKey";
    private static final String NON_MANDATORY_PARAM_KEY = "nonMandatoryParamKey";

    @Mock
    private Actions actionsMock;

    @InjectMocks
    private ActionParameterDefinitionValidator objectUnderTest;

    @Test
    public void testSimpleValidationPasses()
    {
        final Action action = new Action();
        final String actionDefinitionId = "properActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        action.setParams(Map.of(MANDATORY_PARAM_KEY, "paramValue"));
        final List<ActionDefinition.ParameterDefinition> parameterDefinitions =
                List.of(createParameterDefinition(MANDATORY_PARAM_KEY, TEXT, true, null));
        final ActionDefinition actionDefinition = createActionDefinition(actionDefinitionId, parameterDefinitions);
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willReturn(actionDefinition);

        //when
        objectUnderTest.validate(action);

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testValidationPassesWhenNoParametersNeeded()
    {
        final Action action = new Action();
        final String actionDefinitionId = "properActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        final ActionDefinition actionDefinition = createActionDefinition(actionDefinitionId, null);
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willReturn(actionDefinition);

        //when
        objectUnderTest.validate(action);

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testValidationPassesWhenNoMandatoryParametersNeeded()
    {
        final Action action = new Action();
        final String actionDefinitionId = "properActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        final ActionDefinition actionDefinition =
                createActionDefinition(actionDefinitionId, List.of(createParameterDefinition(NON_MANDATORY_PARAM_KEY, TEXT, false, null)));
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willReturn(actionDefinition);

        //when
        objectUnderTest.validate(action);

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testValidationPassesWhenOptionalParametersNotProvided()
    {
        final Action action = new Action();
        final String actionDefinitionId = "properActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        action.setParams(Map.of(MANDATORY_PARAM_KEY, "paramValue"));
        final List<ActionDefinition.ParameterDefinition> parameterDefinitions =
                List.of(createParameterDefinition(MANDATORY_PARAM_KEY, TEXT, true, null),
                        createParameterDefinition(NON_MANDATORY_PARAM_KEY, BOOLEAN, false, null));
        final ActionDefinition actionDefinition = createActionDefinition(actionDefinitionId, parameterDefinitions);
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willReturn(actionDefinition);

        //when
        objectUnderTest.validate(action);

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testValidationFailsWhenTooManyParameters()
    {
        final Action action = new Action();
        final String actionDefinitionId = "properActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        action.setParams(Map.of(MANDATORY_PARAM_KEY, "paramValue", NON_MANDATORY_PARAM_KEY, false));
        final List<ActionDefinition.ParameterDefinition> parameterDefinitions =
                List.of(createParameterDefinition(MANDATORY_PARAM_KEY, TEXT, true, null));
        final ActionDefinition actionDefinition = createActionDefinition(actionDefinitionId, parameterDefinitions);
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willReturn(actionDefinition);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(() -> objectUnderTest.validate(action))
                .withMessageContaining(String.format(MUST_NOT_CONTAIN_PARAMETER, actionDefinitionId, NON_MANDATORY_PARAM_KEY));

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testValidationFailsWhenMissingParameters()
    {
        final Action action = new Action();
        final String actionDefinitionId = "properActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        final List<ActionDefinition.ParameterDefinition> parameterDefinitions =
                List.of(createParameterDefinition(MANDATORY_PARAM_KEY, TEXT, true, null));
        final ActionDefinition actionDefinition = createActionDefinition(actionDefinitionId, parameterDefinitions);
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willReturn(actionDefinition);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(() -> objectUnderTest.validate(action))
                .withMessageContaining(String.format(PARAMS_SHOULD_NOT_BE_EMPTY, actionDefinitionId));

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testValidationFailsWhenMissingParameterValue()
    {
        final Action action = new Action();
        final String actionDefinitionId = "properActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        final Map<String, java.io.Serializable> params = new HashMap<>();
        params.put(MANDATORY_PARAM_KEY, null);
        action.setParams(params);
        final List<ActionDefinition.ParameterDefinition> parameterDefinitions =
                List.of(createParameterDefinition(MANDATORY_PARAM_KEY, TEXT, true, null));
        final ActionDefinition actionDefinition = createActionDefinition(actionDefinitionId, parameterDefinitions);
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willReturn(actionDefinition);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(() -> objectUnderTest.validate(action))
                .withMessageContaining(String.format(MISSING_PARAMETER, MANDATORY_PARAM_KEY));

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testValidationFailsWhenMandatoryParameterIsMissing()
    {
        final Action action = new Action();
        final String actionDefinitionId = "properActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        action.setParams(Map.of(NON_MANDATORY_PARAM_KEY, true));
        final List<ActionDefinition.ParameterDefinition> parameterDefinitions =
                List.of(createParameterDefinition(MANDATORY_PARAM_KEY, TEXT, true, null),
                        createParameterDefinition(NON_MANDATORY_PARAM_KEY, BOOLEAN, false, null));
        final ActionDefinition actionDefinition = createActionDefinition(actionDefinitionId, parameterDefinitions);
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willReturn(actionDefinition);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(() -> objectUnderTest.validate(action))
                .withMessageContaining(String.format(MISSING_PARAMETER, MANDATORY_PARAM_KEY));

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testValidationFailsWhenActionWithNullActionDefinition()
    {
        final Action action = new Action();
        action.setActionDefinitionId(null);
        action.setParams(Map.of(MANDATORY_PARAM_KEY, "paramValue"));

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(() -> objectUnderTest.validate(action))
                .withMessageContaining(EMPTY_ACTION_DEFINITION);

        then(actionsMock).shouldHaveNoInteractions();
    }

    @Test
    public void testValidationFailsWhenNotApplicableActionDefinition()
    {
        final Action action = new Action();
        final String actionDefinitionId = "notApplicableActionDefinition";
        action.setActionDefinitionId(actionDefinitionId);
        action.setParams(Map.of(MANDATORY_PARAM_KEY, "paramValue"));
        given(actionsMock.getRuleActionDefinitionById(actionDefinitionId)).willThrow(NotFoundException.class);

        //when
        assertThatExceptionOfType(InvalidArgumentException.class).isThrownBy(() -> objectUnderTest.validate(action))
                .withMessageContaining(String.format(INVALID_ACTION_DEFINITION, actionDefinitionId));

        then(actionsMock).should().getRuleActionDefinitionById(actionDefinitionId);
        then(actionsMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testHasProperPriority()
    {
        final int expectedPriority = Integer.MIN_VALUE;
        final int actualPriority = objectUnderTest.getPriority();

        assertEquals(expectedPriority, actualPriority);
    }

    private ActionDefinition createActionDefinition(final String actionDefinitionId,
                                                    List<ActionDefinition.ParameterDefinition> parameterDefinitions)
    {
        return new ActionDefinition(actionDefinitionId, actionDefinitionId, "title", "description", Collections.emptyList(), false, false,
                parameterDefinitions);
    }

    private ActionDefinition.ParameterDefinition createParameterDefinition(final String name, final QName qName, final boolean mandatory,
                                                                           final String constraint)
    {
        return new ActionDefinition.ParameterDefinition(name, qName.toPrefixString(), false, mandatory, "label", constraint);
    }

}
