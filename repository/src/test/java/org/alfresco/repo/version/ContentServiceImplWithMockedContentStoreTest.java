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
package org.alfresco.repo.version;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.repo.content.ContentServiceImpl;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.StorageClass;
import org.alfresco.service.cmr.repository.ContentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for the CachingContentStore class. Tests use mock backing store and cache.
 *
 * @author Lucian Tuca
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentServiceImplWithMockedContentStoreTest
{
    @Mock
    private ContentStore store;

    private ContentService contentService;

    @Before
    public void setUp() throws Exception
    {
        contentService = new ContentServiceImpl();
        ReflectionTestUtils.setField(contentService, "store", store);
    }

    @Test
    public void testStoreIsCalledForIsStorageClassesSupported()
    {
        final StorageClass sc = new StorageClass();
        when(store.isStorageClassSupported(sc)).thenReturn(true);
        assertTrue(contentService.isStorageClassSupported(sc));
        verify(store, times(1)).isStorageClassSupported(sc);
    }

    @Test
    public void testStoreIsCalledForGetSupportedStorageClasses()
    {
        when(store.getSupportedStorageClasses()).thenReturn(emptySet());
        assertTrue(contentService.getSupportedStorageClasses().isEmpty());
        verify(store, times(1)).getSupportedStorageClasses();
    }

    @Test
    public void testStoreIsCalledForGetStorageClassesTransitions()
    {
        when(store.getStorageClassesTransitions()).thenReturn(emptyMap());
        assertTrue(contentService.getStorageClassesTransitions().isEmpty());
        verify(store, times(1)).getStorageClassesTransitions();
    }
}
