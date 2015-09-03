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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.IllegalAbbreviationChars;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;

/**
 * Check that a field is suitable to be used as part of a filename.
 *
 * @author tpage
 * @since 2.4.a
 */
public class FilenameFieldValidator implements FieldValidator<String>
{
    /**
     * Illegal characters in a {@link ClassificationLevel#getId() level ID}.
     *  Equals to Alfresco's disallowed filename characters.
     */
    // See <constraint name="cm:filename" type="REGEX"> in contentModel.xml
    static final List<Character> ILLEGAL_ABBREVIATION_CHARS = asList('"', '*', '\\', '>', '<', '?', '/', ':', '|');

    private List<Character> getIllegalAbbreviationChars(String s)
    {
        final List<Character> result = new ArrayList<>();
        for (Character c : ILLEGAL_ABBREVIATION_CHARS)
        {
            if (s.contains(c.toString())) result.add(c);
        }
        return result;
    }

    @Override
    public void validate(String field, String fieldName) throws ClassificationException
    {
        List<Character> illegalAbbreviationChars = getIllegalAbbreviationChars(field);
        if (!illegalAbbreviationChars.isEmpty())
        {
            throw new IllegalAbbreviationChars("Illegal character(s) in " + fieldName, illegalAbbreviationChars);
        }
    }
}
