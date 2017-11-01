/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.repo.virtual.store;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsException;

public class TypeVirtualizationMethodUnitTest
{
    private static Log logger = LogFactory.getLog(TypeVirtualizationMethodUnitTest.class);

    @Test
    public void testQNameFiltersSetter_invalidFilters() throws Exception
    {
        assertIllegalQNameFilters(null);
        assertIllegalQNameFilters("");
    }

    @Test
    public void testQNameFiltersSetter_validFilters() throws Exception
    {
        assertQNameFilters("st:site");
        assertQNameFilters("st:site,cm:folder");
        assertQNameFilters("st:site,cm:test-folder");
        assertQNameFilters("st:*");
        assertQNameFilters("st:*,cm:*");
        assertQNameFilters("*");
        assertQNameFilters("none");
    }

    static NamespacePrefixResolver mockNamespacePrefixResolver()
    {
        NamespacePrefixResolver mockNamespacePrefixResolver = Mockito.mock(NamespacePrefixResolver.class,
                new ThrowsException(new NamespaceException("Mock exception ")));

        Mockito.doReturn(Arrays.<String> asList(SiteModel.SITE_MODEL_PREFIX)).when(mockNamespacePrefixResolver).getPrefixes(SiteModel.SITE_MODEL_URL);
        Mockito.doReturn(SiteModel.SITE_MODEL_URL).when(mockNamespacePrefixResolver).getNamespaceURI(SiteModel.SITE_MODEL_PREFIX);

        Mockito.doReturn(Arrays.<String> asList(NamespaceService.CONTENT_MODEL_PREFIX)).when(mockNamespacePrefixResolver)
                .getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI);
        Mockito.doReturn(NamespaceService.CONTENT_MODEL_1_0_URI).when(mockNamespacePrefixResolver)
                .getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX);

        Mockito.doReturn("mock(NamespacePrefixResolver)@" + TypeVirtualizationMethod.class.toString()).when(mockNamespacePrefixResolver).toString();

        return mockNamespacePrefixResolver;
    }

    static void assertIllegalQNameFilters(String filters)
    {
        TypeVirtualizationMethod tvm = new TypeVirtualizationMethod();
        try
        {
            tvm.setNamespacePrefixResolver(mockNamespacePrefixResolver());
            tvm.setQnameFilters(filters);
            fail("Should not be able to set filters string " + filters);
        }
        catch (IllegalArgumentException e)
        {
            // void as expected
            logger.info(e.getMessage());
        }
    }

    static void assertQNameFilters(String filters)
    {
        TypeVirtualizationMethod tvm = new TypeVirtualizationMethod();
        tvm.setNamespacePrefixResolver(mockNamespacePrefixResolver());
        tvm.setQnameFilters(filters);
    }

}
