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
package org.alfresco.rest.requests;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.openMocks;

import org.alfresco.rest.core.RestWrapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Unit tests for {@link ModelRequest}. */
public class ModelRequestTest
{
    /** Abstract class, so test with mock passing through to real methods. */
    @InjectMocks
    private ModelRequest<Object> modelRequest = mock(ModelRequest.class, CALLS_REAL_METHODS);
    @Mock
    private RestWrapper restWrapperMock;

    @BeforeMethod
    public void setUp()
    {
        openMocks(this);
    }

    @Test
    public void testInclude_empty()
    {
        modelRequest.include();

        then(restWrapperMock).should().withParams("include=");
        then(restWrapperMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testInclude_singleInclude()
    {
        modelRequest.include("field1");

        then(restWrapperMock).should().withParams("include=field1");
        then(restWrapperMock).shouldHaveNoMoreInteractions();
    }


    @Test
    public void testInclude_multipleIncludes()
    {
        modelRequest.include("field1", "field2", "field3");

        then(restWrapperMock).should().withParams("include=field1,field2,field3");
        then(restWrapperMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testIncludePath()
    {
        modelRequest.includePath();

        then(restWrapperMock).should().withParams("include=path");
        then(restWrapperMock).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testUsingParams()
    {
        modelRequest.usingParams("param1=value1", "param2=value2");

        then(restWrapperMock).should().withParams("param1=value1", "param2=value2");
        then(restWrapperMock).shouldHaveNoMoreInteractions();
    }
}
