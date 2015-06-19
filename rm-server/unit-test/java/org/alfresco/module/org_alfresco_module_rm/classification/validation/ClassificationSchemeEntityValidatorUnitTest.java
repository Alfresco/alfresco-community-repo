/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.classification.validation;

import static java.util.Arrays.asList;
import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Unit tests for the {@link ClassificationSchemeEntityValidator}.
 *
 * @author Neil Mc Erlean
 * @author tpage
 * @since 3.0
 */
public class ClassificationSchemeEntityValidatorUnitTest
{
    private static final String ENTITY_NAME = "ENTITY_NAME";
    @Mock
    private EntityFieldsValidator<ClassificationLevel> mockFieldsValidator;
    /** The class under test. */
    private ClassificationSchemeEntityValidator<ClassificationLevel> classificationEntitySchemeValidator = new ClassificationSchemeEntityValidator<>(mockFieldsValidator );

    @Before
    public void setUp()
    {
        initMocks(this);
    }

    @Test(expected=MissingConfiguration.class)
    public void classificationLevelsAreRequired()
    {
        classificationEntitySchemeValidator.validate(Collections.emptyList(), ENTITY_NAME);
    }

    @Test public void ensureUniquenessOfAbbreviationIds()
    {
        IllegalConfiguration e = expectedException(IllegalConfiguration.class, () ->
        {
            List<ClassificationLevel> objects = asList(new ClassificationLevel("FOO", "value.does.not.matter"),
                                                       new ClassificationLevel("BAR", "value.does.not.matter"),
                                                       new ClassificationLevel("---", "value.does.not.matter"),
                                                       new ClassificationLevel("BAR", "value.does.not.matter"),
                                                       new ClassificationLevel("FOO", "value.does.not.matter"));
            classificationEntitySchemeValidator.validate(objects, ENTITY_NAME);
            return null;
        });
        assertThat("Exception message did not identify the duplicate IDs", e.getMessage(),
                   allOf(containsString("FOO"), containsString("BAR")));
    }
}
