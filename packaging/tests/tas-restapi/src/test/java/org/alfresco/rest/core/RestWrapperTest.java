/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.core;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit tests for {@link RestWrapper}. */
public class RestWrapperTest
{
    private RestWrapper restWrapper;

    @BeforeMethod
    public void setUp()
    {
        restWrapper = new RestWrapper();
    }

    @Test
    public void testWithParams_noParams()
    {
        restWrapper.withParams();

        assertEquals(restWrapper.getParameters(), "", "Expected empty parameters");
    }

    @Test
    public void testWithParams_singleParam()
    {
        restWrapper.withParams("param=value");

        assertEquals(restWrapper.getParameters(), "param=value", "Unexpected parameter string");
    }

    @Test
    public void testWithParams_multipleParams()
    {
        restWrapper.withParams("param1=value1", "param2=value2");

        assertEquals(restWrapper.getParameters(), "param1=value1&param2=value2", "Unexpected parameter string");
    }

    @Test
    public void testWithParams_lastValueWins()
    {
        // The first call should be overwritten by the second.
        restWrapper.withParams("param1=value1");
        restWrapper.withParams("param2=value2");

        assertEquals(restWrapper.getParameters(), "param2=value2", "Unexpected parameter string");
    }
}
