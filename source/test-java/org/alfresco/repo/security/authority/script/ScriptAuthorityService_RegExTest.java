/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.repo.security.authority.script;

import static org.alfresco.repo.security.authority.script.ScriptAuthorityService.ON_FIRST_SPACE;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Simple sanity tests for regular expression {@link ScriptAuthorityService#ON_FIRST_SPACE}.
 * 
 * @author Neil Mc Erlean
 * @since 4.1.3
 */
public class ScriptAuthorityService_RegExTest
{
    @Test public void validateFirstNameLastNameRegEx() throws Exception
    {
        assertArrayEquals(new String[] {"Luke", "Skywalker"}, "Luke Skywalker".split(ON_FIRST_SPACE, 2));
        
        // Surnames with spaces in - yes, this is wrong (I think), but it's what we expect our naive algorithm to do.
        assertArrayEquals(new String[] {"Jar", "Jar Binks"}, "Jar Jar Binks".split(ON_FIRST_SPACE, 2));
        
        // Too short names (no surname)
        assertArrayEquals(new String[] {"C-3PO"}, "C-3PO".split(ON_FIRST_SPACE, 2));
    }
}
