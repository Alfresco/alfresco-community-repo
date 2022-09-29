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

import static org.alfresco.rest.api.impl.ActionsImpl.NAMES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.alfresco.rest.api.model.ActionParameterConstraint;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
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

    @InjectMocks
    private ActionsImpl objectUnderTest;

    @Test
    public void testGetAllActionConstraints()
    {
        given(parametersMock.getParameter(NAMES)).willReturn(null);
        final int constraintsCount = 3;
        final List<ParameterConstraint> constraints = createConstraints(constraintsCount);
        given(actionServiceMock.getParameterConstraints()).willReturn(constraints);

        //when
        final CollectionWithPagingInfo<ActionParameterConstraint> actionConstraints = objectUnderTest.getActionConstraints(parametersMock);

        then(parametersMock).should().getParameter(NAMES);
        then(parametersMock).should().getPaging();
        then(parametersMock).shouldHaveNoMoreInteractions();
        then(actionServiceMock).should().getParameterConstraints();
        then(actionServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actionConstraints).isNotNull();
        assertThat(actionConstraints.getCollection()).hasSameSizeAs(constraints);
        IntStream.rangeClosed(1, constraintsCount).forEach(i -> {
            ActionParameterConstraint actualConstraint =
                    actionConstraints.getCollection().stream()
                            .filter(c -> (NAME + i).equals(c.getName()))
                            .findFirst()
                            .get();
            final ParameterConstraint parameterConstraint = constraints.get(i - 1);
            assertThat(actualConstraint).isNotNull();
            assertThat(actualConstraint.getName()).isEqualTo(parameterConstraint.getName());
            assertThat(actualConstraint.getConstraintsMap()).isNotNull().usingRecursiveComparison()
                    .isEqualTo(parameterConstraint.getAllowableValues());
        });
    }

    @Test
    public void testGetActionConstraintsWithNameFilter()
    {
        final String name = "name1";
        final String names = name;
        given(parametersMock.getParameter(NAMES)).willReturn(names);
        final Map<String, String> values = Map.of(CONSTRAINT + "1", LABEL + "1");
        final ParameterConstraint testConstraint = createTestConstraint(name, values);
        given(actionServiceMock.getParameterConstraint(name)).willReturn(testConstraint);
        //when
        final CollectionWithPagingInfo<ActionParameterConstraint> actionConstraints = objectUnderTest.getActionConstraints(parametersMock);

        then(parametersMock).should().getParameter(NAMES);
        then(parametersMock).should().getPaging();
        then(parametersMock).shouldHaveNoMoreInteractions();
        then(actionServiceMock).should().getParameterConstraint(name);
        then(actionServiceMock).shouldHaveNoMoreInteractions();
        assertThat(actionConstraints).isNotNull();
        assertThat(actionConstraints.getCollection()).hasSize(1);
        final ActionParameterConstraint actualConstraint = actionConstraints.getCollection().stream().findFirst().get();
        assertThat(actualConstraint).isNotNull();
        assertThat(actualConstraint.getName()).isEqualTo(testConstraint.getName());
        assertThat(actualConstraint.getConstraintsMap()).isNotNull().usingRecursiveComparison()
                .isEqualTo(testConstraint.getAllowableValues());
    }

    @Test
    public void testGetActionConstraintsWithNameFilterNonExistingConstraint()
    {
        final String name = "name1";
        final String names = name;
        given(parametersMock.getParameter(NAMES)).willReturn(names);
        given(actionServiceMock.getParameterConstraint(name)).willReturn(null);

        //when
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> objectUnderTest.getActionConstraints(parametersMock))
                .withMessageContaining("Action parameter constraints (name1) do not exist");
    }

    private List<ParameterConstraint> createConstraints(int constraintsCount)
    {
        return IntStream.rangeClosed(1, constraintsCount)
                .mapToObj(i -> {
                    final Map<String, String> values = Map.of(CONSTRAINT + i, LABEL + i);
                    return createTestConstraint(NAME + i, values);
                })
                .collect(Collectors.toList());
    }

    private ParameterConstraint createTestConstraint(final String name, final Map<String, String> values)
    {
        final ParameterConstraint constraint = new ParameterConstraint()
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
        return constraint;
    }
}
