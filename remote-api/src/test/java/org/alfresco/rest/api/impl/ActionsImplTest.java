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

package org.alfresco.rest.api.impl;

import static org.alfresco.rest.api.impl.ActionsImpl.CONSTRAINT_NOT_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.alfresco.repo.action.constraint.FolderContentsParameterConstraint;
import org.alfresco.rest.api.impl.rules.ActionParameterConverter;
import org.alfresco.rest.api.model.ActionParameterConstraint;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterConstraint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActionsImplTest
{
    private static final String NAME = "name";
    private static final String CONSTRAINT = "constraint";
    private static final String LABEL = "label";
    private static final String DISPLAY = "display ";

    @Mock
    private ActionService actionServiceMock;
    @Mock
    private Parameters parametersMock;
    @Mock
    private ActionParameterConverter parameterConverterMock;

    @InjectMocks
    private ActionsImpl objectUnderTest;

    @Test
    public void testGetSingleActionConstraint()
    {
        final String name = "name";
        final String value = CONSTRAINT;
        final String label = LABEL;
        final Map<String, String> values = Map.of(value, label);
        final ParameterConstraint testConstraint = createTestConstraint(name, values);
        given(actionServiceMock.getParameterConstraint(name)).willReturn(testConstraint);

        //when
        final ActionParameterConstraint actualConstraint = objectUnderTest.getActionConstraint(name);

        then(parametersMock).shouldHaveNoInteractions();
        then(actionServiceMock).should().getParameterConstraint(name);
        then(actionServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualConstraint).isNotNull();
        assertThat(actualConstraint.getConstraintName()).isEqualTo(testConstraint.getName());
        ActionParameterConstraint.ConstraintData expectedConstraintData = new ActionParameterConstraint.ConstraintData(value, label);
        assertThat(actualConstraint.getConstraintValues()).isNotNull().hasSize(1);
        ActionParameterConstraint.ConstraintData actualConstraintData = actualConstraint.getConstraintValues().get(0);
        assertThat(actualConstraintData).usingRecursiveComparison().isEqualTo(expectedConstraintData);
    }

    @Test
    public void testGetSingleActionNodeConstraint()
    {
        final String name = "name1";
        final String dummyNodeId = "dummy-node-id";
        final String value = "workspace://DummyStore/" + dummyNodeId;
        final Map<String, String> values = Map.of(value, LABEL);
        final FolderContentsParameterConstraint testConstraint = mock(FolderContentsParameterConstraint.class);
        given(testConstraint.getName()).willReturn(name);
        given(testConstraint.getValues()).willReturn(values);
        given(actionServiceMock.getParameterConstraint(name)).willReturn(testConstraint);
        given(parameterConverterMock.convertParamFromServiceModel(any())).willReturn(dummyNodeId);

        //when
        final ActionParameterConstraint actualConstraint = objectUnderTest.getActionConstraint(name);

        then(parametersMock).shouldHaveNoInteractions();
        then(actionServiceMock).should().getParameterConstraint(name);
        then(actionServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actualConstraint).isNotNull();
        assertThat(actualConstraint.getConstraintName()).isEqualTo(testConstraint.getName());
        ActionParameterConstraint.ConstraintData expectedConstraintData = new ActionParameterConstraint.ConstraintData(dummyNodeId, LABEL);
        assertThat(actualConstraint.getConstraintValues()).isNotNull().hasSize(1);
        ActionParameterConstraint.ConstraintData actualConstraintData = actualConstraint.getConstraintValues().get(0);
        assertThat(actualConstraintData).usingRecursiveComparison().isEqualTo(expectedConstraintData);
    }

    @Test
    public void testGetActionConstraintsWithNameFilterNonExistingConstraint()
    {
        final String name = "name";
        given(actionServiceMock.getParameterConstraint(name)).willReturn(null);

        //when
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> objectUnderTest.getActionConstraint(name))
                .withMessageContaining(String.format(CONSTRAINT_NOT_EXISTS, name));

        then(parametersMock).shouldHaveNoInteractions();
        then(actionServiceMock).should().getParameterConstraint(name);
        then(actionServiceMock).shouldHaveNoMoreInteractions();
    }

    private ParameterConstraint createTestConstraint(final String name, final Map<String, String> values)
    {
        return new ParameterConstraint()
        {
            @Override
            public String getName()
            {
                return name;
            }

            @Override
            public boolean isValidValue(String value)
            {
                return true;
            }

            @Override
            public String getValueDisplayLabel(String value)
            {
                return DISPLAY + name;
            }

            @Override
            public Map<String, String> getAllowableValues()
            {
                return values;
            }
        };
    }
}
