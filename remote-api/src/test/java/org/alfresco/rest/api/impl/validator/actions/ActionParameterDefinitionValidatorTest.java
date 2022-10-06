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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActionParameterDefinitionValidatorTest
{
    @InjectMocks
    private ActionParameterDefinitionValidator objectUnderTest;

    @Test
    public void testGetActionDefinitions()
    {
        final List<String> expectedList =
                List.of("add-features", "check-in", "check-out", "composite-action", "copy", "count-children", "counter", "create-version",
                        "execute-all-rules", "export", "transform-image", "import", "link-category", "mail", "move", "remove-features",
                        "repository-export", "script", "set-property-value", "simple-workflow", "specialise-type", "take-ownership",
                        "transform");
        final List<String> actionDefinitionIds = objectUnderTest.getActionDefinitionIds();
        assertEquals(expectedList, actionDefinitionIds);
    }
}
