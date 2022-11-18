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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.content.replication;

import org.alfresco.repo.content.ContentRestoreParams;
import org.alfresco.repo.content.ContentStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests class for {@code AggregatingContentStore}
 * 
 * Currently does not cover all methods.
 * 
 * @author mpichura
 */
@RunWith(MockitoJUnitRunner.class)
public class AggregatingContentStoreUnitTest
{
    private static final String X_AMZ_HEADER_1 = "x-amz-header1";
    private static final String VALUE_1 = "value1";
    private static final String X_AMZ_HEADER_2 = "x-amz-header2";
    private static final String VALUE_2 = "value2";
    
    private List<ContentStore> secondaryStores;    
    @Mock
    ContentStore primaryStore;
    @Mock
    ContentStore secondaryStore;
    
    @InjectMocks
    private AggregatingContentStore objectUnderTest;
    
    @Before
    public void setUp() {
        secondaryStores = List.of(secondaryStore);
        objectUnderTest.setSecondaryStores(secondaryStores);
    }


    @Test
    public void shouldReturnStoragePropertiesFromPrimaryStore()
    {
        final String contentUrl = "url";
        final Map<String, String> primaryStorePropertiesMap = Map.of(X_AMZ_HEADER_1, VALUE_1, X_AMZ_HEADER_2, VALUE_2);;
        when(primaryStore.getStorageProperties(contentUrl)).thenReturn(primaryStorePropertiesMap);

        final Map<String, String> storageProperties = objectUnderTest.getStorageProperties(contentUrl);

        assertFalse(storageProperties.isEmpty());
        assertEquals(primaryStorePropertiesMap, storageProperties);
        verify(secondaryStore, times(0)).getStorageProperties(contentUrl);
    }

    @Test
    public void shouldReturnStoragePropertiesFromSecondaryStore()
    {
        final String contentUrl = "url";
        final Map<String, String> secondaryStorePropertiesMap = Map.of(X_AMZ_HEADER_1, VALUE_1, X_AMZ_HEADER_2, VALUE_2);;
        when(primaryStore.getStorageProperties(contentUrl)).thenReturn(Collections.emptyMap());
        when(secondaryStore.getStorageProperties(contentUrl)).thenReturn(secondaryStorePropertiesMap);

        final Map<String, String> storageProperties = objectUnderTest.getStorageProperties(contentUrl);

        assertFalse(storageProperties.isEmpty());
        assertEquals(secondaryStorePropertiesMap, storageProperties);
        verify(secondaryStore, times(1)).getStorageProperties(contentUrl);
        verify(primaryStore, times(1)).getStorageProperties(contentUrl);
    }

    @Test
    public void shouldReturnEmptyStorageProperties()
    {
        final String contentUrl = "url";
        when(primaryStore.getStorageProperties(contentUrl)).thenReturn(Collections.emptyMap());
        when(secondaryStore.getStorageProperties(contentUrl)).thenReturn(Collections.emptyMap());

        final Map<String, String> storageProperties = objectUnderTest.getStorageProperties(contentUrl);

        assertTrue(storageProperties.isEmpty());
        verify(primaryStore, times(1)).getStorageProperties(contentUrl);
    }

    @Test
    public void shouldRequestContentArchiveThroughPrimaryStore()
    {
        final String contentUrl = "url";
        final boolean expectedResult = true;
        final Map<String, Serializable> archiveParams = Collections.emptyMap();

        when(primaryStore.requestSendContentToArchive(contentUrl, archiveParams)).thenReturn(expectedResult);

        boolean sendContentToArchive = objectUnderTest.requestSendContentToArchive(contentUrl, archiveParams);

        assertEquals(expectedResult, sendContentToArchive);
        verify(secondaryStore, never()).requestSendContentToArchive(contentUrl,archiveParams);
    }

    @Test
    public void shouldRequestContentArchiveThroughSecondaryStore()
    {
        final String contentUrl = "url";
        final boolean expectedResult = true;
        final Map<String, Serializable> archiveParams = Collections.emptyMap();

        when(primaryStore.requestSendContentToArchive(contentUrl, archiveParams)).thenThrow(UnsupportedOperationException.class);
        when(secondaryStore.requestSendContentToArchive(contentUrl, archiveParams)).thenReturn(expectedResult);

        boolean sendContentToArchive = objectUnderTest.requestSendContentToArchive(contentUrl, archiveParams);

        assertEquals(expectedResult, sendContentToArchive);
        verify(primaryStore, times(1)).requestSendContentToArchive(contentUrl, archiveParams);
        verify(secondaryStore, times(1)).requestSendContentToArchive(contentUrl, archiveParams);
    }

    @Test
    public void shouldThrowExceptionWhenRequestContentArchiveNotImplemented()
    {
        final String contentUrl = "url";
        when(primaryStore.getStorageProperties(contentUrl)).thenReturn(Collections.emptyMap());
        when(secondaryStore.getStorageProperties(contentUrl)).thenReturn(Collections.emptyMap());

        final Map<String, String> storageProperties = objectUnderTest.getStorageProperties(contentUrl);

        assertTrue(storageProperties.isEmpty());
        verify(primaryStore, times(1)).getStorageProperties(contentUrl);
    }

    @Test
    public void shouldRequestRestoreContentFromArchiveThroughPrimaryStore()
    {
        final String contentUrl = "url";
        final boolean expectedResult = true;
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), "High");

        when(primaryStore.requestRestoreContentFromArchive(contentUrl, restoreParams)).thenReturn(expectedResult);

        boolean sendContentToArchive = objectUnderTest.requestRestoreContentFromArchive(contentUrl, restoreParams);

        assertEquals(expectedResult, sendContentToArchive);
        verify(secondaryStore, never()).requestRestoreContentFromArchive(contentUrl, restoreParams);
    }

    @Test
    public void shouldRequestRestoreContentFromArchiveThroughSecondaryStore()
    {
        final String contentUrl = "url";
        final boolean expectedResult = true;
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), "High");

        when(primaryStore.requestRestoreContentFromArchive(contentUrl, restoreParams)).thenThrow(UnsupportedOperationException.class);
        when(secondaryStore.requestRestoreContentFromArchive(contentUrl, restoreParams)).thenReturn(expectedResult);

        boolean sendContentToArchive = objectUnderTest.requestRestoreContentFromArchive(contentUrl, restoreParams);

        assertEquals(expectedResult, sendContentToArchive);
        verify(primaryStore, times(1)).requestRestoreContentFromArchive(contentUrl, restoreParams);
        verify(secondaryStore, times(1)).requestRestoreContentFromArchive(contentUrl, restoreParams);
    }

    @Test
    public void shouldThrowExceptionWhenRequestRestoreContentFromArchiveNotImplemented()
    {
        final String contentUrl = "url";
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), "High");
        when(primaryStore.requestRestoreContentFromArchive(contentUrl, restoreParams)).thenCallRealMethod();
        when(secondaryStore.requestRestoreContentFromArchive(contentUrl, restoreParams)).thenCallRealMethod();

        assertThrows(UnsupportedOperationException.class, () -> {
            objectUnderTest.requestRestoreContentFromArchive(contentUrl, restoreParams);
        });

        verify(primaryStore, times(1)).requestRestoreContentFromArchive(contentUrl, restoreParams);
        verify(secondaryStore, times(1)).requestRestoreContentFromArchive(contentUrl, restoreParams);
    }

}
