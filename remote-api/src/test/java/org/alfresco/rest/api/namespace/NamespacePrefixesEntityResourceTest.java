/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.rest.api.namespace;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.rest.api.impl.NamespacePrefixesImpl;
import org.alfresco.rest.api.model.NamespacePrefixEntry;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.namespace.NamespaceService;

/**
 * Unit tests for NamespacePrefixesEntityResource v1 REST endpoint. Verifies EntityResource properly delegates to NamespacePrefixesImpl and returns complete namespace-prefix mapping.
 */
public class NamespacePrefixesEntityResourceTest
{
    private NamespacePrefixesEntityResource resource;
    private NamespaceService mockNamespaceService;
    private Parameters mockParameters;

    @Before
    public void setUp()
    {
        NamespacePrefixesImpl implementation = new NamespacePrefixesImpl();
        mockNamespaceService = mock(NamespaceService.class);
        mockParameters = mock(Parameters.class);
        implementation.setNamespaceService(mockNamespaceService);

        resource = new NamespacePrefixesEntityResource();
        resource.setNamespacePrefixes(implementation);
    }

    @Test
    public void testReadAllReturnsNamespacePrefixes()
    {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("cm", "http://www.alfresco.org/model/content/1.0");
        prefixes.put("sys", "http://www.alfresco.org/model/system/1.0");

        when(mockNamespaceService.getPrefixes()).thenReturn(prefixes.keySet());
        when(mockNamespaceService.getNamespaceURI("cm"))
                .thenReturn("http://www.alfresco.org/model/content/1.0");
        when(mockNamespaceService.getNamespaceURI("sys"))
                .thenReturn("http://www.alfresco.org/model/system/1.0");

        CollectionWithPagingInfo<NamespacePrefixEntry> result = resource.readAll(mockParameters);

        assertNotNull("Result should not be null", result);
        assertEquals("Result should contain 1 entry", (long) 1, (long) result.getTotalItems());

        NamespacePrefixEntry entry = result.getCollection().iterator().next();
        assertNotNull("Entry should not be null", entry);
        assertNotNull("Entry should have prefixUriMap", entry.getPrefixUriMap());
        assertEquals("prefixUriMap should have 2 entries", (long) 2, (long) entry.getPrefixUriMap().size());
        assertEquals("Should map content namespace URI to cm", "cm",
                entry.getPrefixUriMap().get("http://www.alfresco.org/model/content/1.0"));
        assertEquals("Should map system namespace URI to sys", "sys",
                entry.getPrefixUriMap().get("http://www.alfresco.org/model/system/1.0"));
    }

    @Test
    public void testReadAllHandlesNullUris()
    {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("cm", "http://www.alfresco.org/model/content/1.0");
        prefixes.put("invalid", null);

        when(mockNamespaceService.getPrefixes()).thenReturn(prefixes.keySet());
        when(mockNamespaceService.getNamespaceURI("cm"))
                .thenReturn("http://www.alfresco.org/model/content/1.0");
        when(mockNamespaceService.getNamespaceURI("invalid"))
                .thenReturn(null);

        CollectionWithPagingInfo<NamespacePrefixEntry> result = resource.readAll(mockParameters);

        assertNotNull("Result should not be null", result);
        NamespacePrefixEntry entry = result.getCollection().iterator().next();
        assertEquals("Should skip null URIs and include only valid ones", (long) 1, (long) entry.getPrefixUriMap().size());
    }

    @Test
    public void testReadAllThrowsExceptionOnServiceFailure()
    {
        when(mockNamespaceService.getPrefixes()).thenThrow(new RuntimeException("Service error"));

        assertThrows("Should throw AlfrescoRuntimeException on service failure",
                AlfrescoRuntimeException.class,
                () -> resource.readAll(mockParameters));
    }
}
