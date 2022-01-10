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

package org.alfresco.rest.rm.community.util;

import java.util.UUID;

/**
 * A utility class to provide test methods that can be used by the REST and UI tests.
 *
 * @author Tom Page
 * @since 2.6
 */
public class CommonTestUtils
{
    /**
     * The default pattern used for the user full name when users are created with tas utility
     */
    public static final String USER_FULLNAME_PATTERN = "FN-%1$s LN-%1$s";

    /** Private constructor to prevent instantiation. */
    private CommonTestUtils()
    {
    }

    /**
     * Generate a prefix to namespace the objects in a test class. Note that four random hex digits should be good enough to avoid
     * collisions when running locally and should also be short enough to maintain readability.
     */
    public static String generateTestPrefix(Class<?> clazz)
    {
        return clazz.getSimpleName().substring(0, 7) + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
