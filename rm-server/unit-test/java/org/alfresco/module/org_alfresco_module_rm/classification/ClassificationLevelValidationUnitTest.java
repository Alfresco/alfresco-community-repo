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

import static java.util.Arrays.asList;
import static org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevelValidation.ILLEGAL_ABBREVIATION_CHARS;
import static org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils.expectedException;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalAbbreviationChars;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Test public void nonEmptyAbbreviationsAreMandatory()
    {
        // A missing or empty level ID is illegal.
        for (String illegalID : asList(null, "", "   ", "\t"))
        {
            expectedException(IllegalArgumentException.class, () ->
            {
                validation.validateLevel(new ClassificationLevel(illegalID, "value.does.not.matter"));
                return null;
            });
        }
    }

    @Test(expected=IllegalConfiguration.class)
    public void longAbbreviationsAreIllegal()
    {
        validation.validateLevel(new ClassificationLevel("12345678901", "value.does.not.matter"));
    }

    /**
     * This test ensures that validation will catch any and all illegal characters in a
     * {@link ClassificationLevel#getId() level ID} and report them all.
     */
    @Test public void someCharactersAreBannedInAbbreviations()
    {
        for (Character illegalChar : ILLEGAL_ABBREVIATION_CHARS)
        {
            IllegalAbbreviationChars e = expectedException(IllegalAbbreviationChars.class, () ->
            {
                validation.validateLevel(new ClassificationLevel("Hello" + illegalChar, "value.does.not.matter"));
                return null;
            });
            assertTrue("Exception did not contain helpful example of illegal character",
                       e.getIllegalChars().contains(illegalChar));
        }

        // We also expect an abbreviation with multiple illegal chars in it to have them all reported in the exception.
        final List<Character> someIllegalChars = ILLEGAL_ABBREVIATION_CHARS.subList(0, 3);

        IllegalAbbreviationChars e = expectedException(IllegalAbbreviationChars.class, () ->
        {
            validation.validateLevel(new ClassificationLevel(someIllegalChars.toString(),
                                                             "value.does.not.matter"));
            return null;
        });

        // Construct a sequence of Matchers - one for each illegal char we expect to see.
        // Apologies for the Java generics madness here. This is really a List of Matchers of List<Character>
        List<Matcher<? super Iterable<? super Character>>> containsCharMatchers = someIllegalChars.stream()
                                                                                          .map(c -> hasItem(c))
                                                                                          .collect(Collectors.toList());
        assertThat(e.getIllegalChars(), allOf(containsCharMatchers));
    }
}
