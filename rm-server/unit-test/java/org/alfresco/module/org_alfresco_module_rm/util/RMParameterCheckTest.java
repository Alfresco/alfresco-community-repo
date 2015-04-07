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

package org.alfresco.module.org_alfresco_module_rm.util;

import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils;
import org.junit.Test;

/**
 * Unit tests for the {@link RMParameter} utility class.
 * 
 * @author tpage
 */
public class RMParameterCheckTest
{
    @Test
    public void checkNotBlank()
    {
        // Check that supplying null causes an exception.
        ExceptionUtils.intercept(IllegalArgumentException.class, () -> {
            RMParameterCheck.checkNotBlank("name", null);
            return null;
        });

        // Check that supplying an empty string causes an exception.
        ExceptionUtils.intercept(IllegalArgumentException.class, () -> {
            RMParameterCheck.checkNotBlank("name", "");
            return null;
        });

        // Check that supplying a whitespace only string causes an exception.
        ExceptionUtils.intercept(IllegalArgumentException.class, () -> {
            RMParameterCheck.checkNotBlank("name", "\n\r \t");
            return null;
        });

        // Check that supplying a mainly whitespace string throws no exceptions.
        RMParameterCheck.checkNotBlank("name", "\n\r *\t");
    }
}
