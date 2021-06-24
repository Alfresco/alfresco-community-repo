/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.rest.api.tests;

import static org.alfresco.rest.api.tests.util.RestApiUtil.parseRestApiEntries;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.AbstractSingleNetworkSiteTest;
import org.alfresco.rest.api.model.StorageClass;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.service.cmr.repository.ContentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * V1 REST API tests for Storage Classes
 *
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/storage-classes} </li>
 * </ul>
 */
public class StorageClassesTest extends AbstractSingleNetworkSiteTest
{
    private static final String STORAGE_CLASSES = "storage-classes";
    private ContentService contentService;
    private ContentStore originalStore;
    @Mock
    private ContentStore mockStore;
    
    
    @Override
    @Before
    public void setup() throws Exception
    {
        super.setup();
        contentService = applicationContext.getBean("contentService", ContentService.class);
        originalStore = (ContentStore) ReflectionTestUtils.getField(contentService, "store");
        
        setRequestContext(user1);
        AuthenticationUtil.setFullyAuthenticatedUser(user1);
    }

    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        ReflectionTestUtils.setField(contentService, "store", originalStore);
    }

    @Test
    public void testGetDefaultStorageClasses() throws Exception
    {
        PublicApiClient.Paging paging = getPaging(0, 100);

        HttpResponse response = getAll(STORAGE_CLASSES, paging, 200);
        List<StorageClass> nodes = parseRestApiEntries(response.getJsonResponse(), StorageClass.class);

        assertNotNull(nodes);
    }

    @Test
    public void testGetStorageClasses() throws Exception
    {
        ReflectionTestUtils.setField(contentService, "store", mockStore);
        
        Set<org.alfresco.repo.content.StorageClass> expectedStorageClasses =
            Set.of(ContentStore.DEFAULT_SC, new org.alfresco.repo.content.StorageClass("archive", "and-a-2nd-class"));
        when(mockStore.getSupportedStorageClasses()).thenReturn(expectedStorageClasses);

        PublicApiClient.Paging paging = getPaging(0, 100);
        HttpResponse response = getAll(STORAGE_CLASSES, paging, 200);
        List<StorageClass> nodes = parseRestApiEntries(response.getJsonResponse(), StorageClass.class);

        assertNotNull(nodes);
        assertEquals(3, nodes.size());
    }
}