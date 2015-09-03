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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.alfresco.service.namespace.QName;
import org.junit.Test;

/**
 * Unit tests for the {@link UserQueryParams}.
 *
 * @author tpage
 * @since 2.4.a
 */
public class UserQueryParamsUnitTest
{
    private static final QName QNAME1 = QName.createQName("1");
    private static final QName QNAME2 = QName.createQName("2");

    /** Check that the constructor escapes backslashes correctly. */
    @Test
    public void testConstructor_backSlashes()
    {
        UserQueryParams userQueryParams = new UserQueryParams("\\Hello\\\\World!");
        assertEquals("\\\\Hello\\\\\\\\World!", userQueryParams.getSearchTerm());
    }

    /** Check that the constructor rejects null. */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_null()
    {
        new UserQueryParams(null);
    }

    /** Check that providing two properties results in a list being returned. */
    @Test
    public void testWithFilterProps_twoProperties()
    {
        UserQueryParams userQueryParams = new UserQueryParams("Search term");
        userQueryParams.withFilterProps(QNAME1, QNAME2);
        assertEquals(Arrays.asList(QNAME1, QNAME2), userQueryParams.getFilterProps());
    }

    /** Check that the first parameter can't be null. */
    @Test(expected = IllegalArgumentException.class)
    public void testWithFilterProps_firstPropertyNull()
    {
        UserQueryParams userQueryParams = new UserQueryParams("Search term");
        userQueryParams.withFilterProps(null);
    }

    /** Check that providing a null after the first argument fails. */
    @Test(expected = IllegalArgumentException.class)
    public void testWithFilterProps_containsNull()
    {
        UserQueryParams userQueryParams = new UserQueryParams("Search term");
        userQueryParams.withFilterProps(QNAME1, (QName) null);
    }
}
