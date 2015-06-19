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

import java.util.Collections;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;
import org.junit.Test;

/**
 * Unit tests for the {@link ClassificationLevelValidation}.
 *
 * @author Neil Mc Erlean
 */
public class ClassificationLevelValidationUnitTest
{
    private final ClassificationLevelValidation validation = new ClassificationLevelValidation();

    @Test(expected=MissingConfiguration.class)
    public void classificationLevelsAreRequired()
    {
        validation.validateLevels(Collections.emptyList());
    }

    @Test public void ensureUniquenessOfAbbreviationIds()
    {
        IllegalConfiguration e = expectedException(IllegalConfiguration.class, () ->
        {
            validation.validateLevels(asList(new ClassificationLevel("FOO", "value.does.not.matter"),
                                             new ClassificationLevel("BAR", "value.does.not.matter"),
                                             new ClassificationLevel("---", "value.does.not.matter"),
                                             new ClassificationLevel("BAR", "value.does.not.matter"),
                                             new ClassificationLevel("FOO", "value.does.not.matter")));
            return null;
        });
        assertThat("Exception message did not identify the duplicate IDs", e.getMessage(),
                   allOf(containsString("FOO"), containsString("BAR")));
    }
}
