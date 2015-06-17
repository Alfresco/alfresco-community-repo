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
import static org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.getDuplicateElements;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalAbbreviationChars;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalConfiguration;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingConfiguration;

/**
 * This class is responsible for validating {@link ClassificationLevel}s.
 *
 * @author Neil Mc Erlean
 * @since 3.0
 */
public class ClassificationLevelValidation
{
    /** The maximum number of characters allowed in a {@link ClassificationLevel#getId() level ID}. */
    private static final int ABBREVIATION_LENGTH_LIMIT = 10;

    /**
     * Illegal characters in a {@link ClassificationLevel#getId() level ID}.
     *  Equals to Alfresco's disallowed filename characters.
     */
    // See <constraint name="cm:filename" type="REGEX"> in contentModel.xml
    static final List<Character> ILLEGAL_ABBREVIATION_CHARS = asList('"', '*', '\\', '>', '<', '?', '/', ':', '|');

    /**
     * Validates the provided {@link ClassificationLevel}.
     * @param level the level to validate.
     * @throws MissingConfiguration if the level abbreviation is missing.
     * @throws IllegalConfiguration if the level abbreviation violates the standard restrictions.
     * @throws IllegalAbbreviationChars if the level abbreviation contains illegal characters.
     */
    void validateLevel(ClassificationLevel level)
    {
        final String levelId = level.getId();

        if (levelId == null || levelId.equals(""))
        {
            throw new MissingConfiguration("Classification level ID is missing.");
        }
        else if (levelId.equals(ClassificationLevelManager.UNCLASSIFIED_ID))
        {
            throw new IllegalConfiguration("The Unclassified ID abbreviation '" +
                                           ClassificationLevelManager.UNCLASSIFIED_ID + "' is reserved for system use.");
        }
        else if (levelId.length() > ABBREVIATION_LENGTH_LIMIT)
        {
            throw new IllegalConfiguration("Illegal classification level abbreviation. Length " +
                    levelId.length() + " > " + ABBREVIATION_LENGTH_LIMIT);
        }
        else if (!getIllegalAbbreviationChars(levelId).isEmpty())
        {
            throw new IllegalAbbreviationChars("Illegal character(s) in level abbreviation",
                    getIllegalAbbreviationChars(levelId));
        }
    }

    /**
     * Validates the provided {@link ClassificationLevel}s as a whole and individually.
     * @param levels the levels to validate.
     * @throws MissingConfiguration if the levels or any of their abbreviations are missing.
     * @throws IllegalConfiguration if any of the level abbreviations violate the standard restrictions.
     * @throws IllegalAbbreviationChars if the level abbreviation contains illegal characters.
     */
    public void validateLevels(List<ClassificationLevel> levels)
    {
        if (levels == null || levels.isEmpty())
        {
            throw new MissingConfiguration("Classification level configuration is missing.");
        }

        final List<ClassificationLevel> duplicateLevels = getDuplicateElements(levels);
        if (!duplicateLevels.isEmpty())
        {
            throw new IllegalConfiguration("Duplicate ID abbreviations are not allowed: " + duplicateLevels);
        }

        for (ClassificationLevel level : levels)
        {
            validateLevel(level);
        }
    }

    private List<Character> getIllegalAbbreviationChars(String s)
    {
        final List<Character> result = new ArrayList<>();
        for (Character c : ILLEGAL_ABBREVIATION_CHARS)
        {
            if (s.contains(c.toString())) result.add(c);
        }
        return result;
    }
}
