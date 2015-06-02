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
package org.alfresco.module.org_alfresco_module_rm.classification;

import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for the {@link ClassificationLevelConstraint}.
 * 
 * @author tpage
 */
public class ClassificationLevelConstraintUnitTest
{
    private static final ClassificationLevel LEVEL_ONE = new ClassificationLevel("id1", "DisplayKey1");
    private static final ClassificationLevel LEVEL_TWO = new ClassificationLevel("id2", "DisplayKey2");
    private static final List<ClassificationLevel> DEFAULT_LEVELS = Arrays.asList(LEVEL_ONE, LEVEL_TWO);

    @InjectMocks ClassificationLevelConstraint classificationLevelConstraint;
    @Mock ClassificationSchemeService mockClassificationSchemeService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        // Currently this list of levels suffices for all the tests.
        doReturn(DEFAULT_LEVELS).when(mockClassificationSchemeService).getClassificationLevels();
    }

    /** Check that evaluateSingleValue throws no exceptions when an id is found. */
    @Test
    public void evaluateSingleValue_valid()
    {
        classificationLevelConstraint.evaluateSingleValue("id1");
    }

    /** Check that evaluateSingleValue throws an exception when an id is not found. */
    @Test(expected = ConstraintException.class)
    public void evaluateSingleValue_stringNotFound()
    {
        classificationLevelConstraint.evaluateSingleValue("non-existant id");
    }

    /** Check that evaluateSingleValue throws an exception when supplied with something that isn't a String. */
    @Test(expected = ConstraintException.class)
    public void evaluateSingleValue_notString()
    {
        classificationLevelConstraint.evaluateSingleValue(Integer.valueOf(123));
    }
}
