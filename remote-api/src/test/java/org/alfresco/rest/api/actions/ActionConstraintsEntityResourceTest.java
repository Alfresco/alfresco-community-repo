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

package org.alfresco.rest.api.actions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.Collections;
import java.util.List;

import org.alfresco.rest.api.Actions;
import org.alfresco.rest.api.model.ActionParameterConstraint;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActionConstraintsEntityResourceTest
{
    @Mock
    private Actions actionsMock;
    @Mock
    private Parameters parametersMock;

    @InjectMocks
    private ActionConstraintsEntityResource objectUnderTest;

    @Test
    public void testReadAll()
    {
        final CollectionWithPagingInfo<ActionParameterConstraint> pagedConstraints = CollectionWithPagingInfo.asPaged(null, Collections.emptyList());
        given(actionsMock.getActionConstraints(parametersMock)).willReturn(pagedConstraints);

        //when
        final CollectionWithPagingInfo<ActionParameterConstraint> result = objectUnderTest.readAll(parametersMock);

        then(actionsMock).should().getActionConstraints(parametersMock);
        then(actionsMock).shouldHaveNoMoreInteractions();
        assertThat(result).isNotNull().usingRecursiveComparison().isEqualTo(pagedConstraints);
    }
}
