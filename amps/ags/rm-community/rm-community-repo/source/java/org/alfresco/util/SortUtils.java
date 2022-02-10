/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.util;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;

/**
 * Helper class to provide functionality related to sorting.
 *
 * @author Tom Page
 * @since 2.6
 */
public class SortUtils
{
    /** The collator used for comparing Strings. */
    private static Collator collator;

    /** Private constructor for util class. */
    private SortUtils()
    {
    }

    /**
     * Get a string comparator that sorts strings according to the locale of the server, and which treats spaces as
     * earlier than alphanumeric characters.
     *
     * @return The comparator.
     */
    public static synchronized Collator getStringComparator()
    {
        if (collator == null)
        {
            String rules = ((RuleBasedCollator) Collator.getInstance()).getRules();
            try
            {
                collator = new RuleBasedCollator(rules.replaceAll("<'\u005f'", "<' '<'\u005f'"));
            }
            catch (ParseException e)
            {
                throw new IllegalStateException(e);
            }
        }
        return collator;
    }
}
