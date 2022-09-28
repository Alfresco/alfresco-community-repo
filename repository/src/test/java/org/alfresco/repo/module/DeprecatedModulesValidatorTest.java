/*
 * #%L
 * Alfresco Repository
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.module;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for the {@link DeprecatedModulesValidator} class.
 *
 * @author Domenico Sibilio
 */
@RunWith(MockitoJUnitRunner.class)
public class DeprecatedModulesValidatorTest
{

    private static final String DEPRECATED_MODULE_1 = "deprecated-module-1";
    private static final String DEPRECATED_MODULE_2 = "deprecated-module-2";
    private static final String VALID_MODULE = "valid-module";
    private static final List<String> DEPRECATED_MODULES = List.of(DEPRECATED_MODULE_1, DEPRECATED_MODULE_2);
    @Mock
    private ModuleService moduleService;
    @Mock
    private ModuleDetails moduleDetails;
    private DeprecatedModulesValidator deprecatedModulesValidator;

    @Before
    public void setUp()
    {
        deprecatedModulesValidator = new DeprecatedModulesValidator(moduleService, DEPRECATED_MODULES);
    }

    @Test
    public void shouldDoNothingWhenNoModulesAreFound()
    {
        when(moduleService.getAllModules()).thenReturn(null);

        deprecatedModulesValidator.onInit();

        verify(moduleService).getAllModules();
    }

    @Test
    public void shouldDoNothingWhenNoDeprecatedModulesAreFound()
    {
        when(moduleService.getAllModules()).thenReturn(List.of(moduleDetails));
        when(moduleDetails.getId()).thenReturn(VALID_MODULE);

        deprecatedModulesValidator.onInit();

        verify(moduleService).getAllModules();
        verify(moduleDetails).getId();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenADeprecatedModuleIsFound()
    {
        when(moduleService.getAllModules()).thenReturn(List.of(moduleDetails));
        when(moduleDetails.getId()).thenReturn(DEPRECATED_MODULE_1);

        deprecatedModulesValidator.onInit();
    }

    @Test
    public void shouldThrowExceptionWhenMultipleDeprecatedModulesAreFound()
    {
        when(moduleService.getAllModules()).thenReturn(List.of(moduleDetails, moduleDetails));
        when(moduleDetails.getId()).thenReturn(DEPRECATED_MODULE_1)
            .thenReturn(DEPRECATED_MODULE_2);

        assertThrows("IllegalStateException should be thrown.",
            IllegalStateException.class,
            () -> deprecatedModulesValidator.onInit());

        verify(moduleService).getAllModules();
        verify(moduleDetails, times(2)).getId();
    }

    @Test
    public void shouldThrowExceptionWhenBothValidAndDeprecatedModulesAreFound()
    {
        when(moduleService.getAllModules()).thenReturn(List.of(moduleDetails, moduleDetails));
        when(moduleDetails.getId()).thenReturn(VALID_MODULE)
            .thenReturn(DEPRECATED_MODULE_2);

        assertThrows("IllegalStateException should be thrown.",
            IllegalStateException.class,
            () -> deprecatedModulesValidator.onInit());

        verify(moduleService).getAllModules();
        verify(moduleDetails, times(2)).getId();
    }

}