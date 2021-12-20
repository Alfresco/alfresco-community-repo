/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.directurl.DirectAccessUrlDisabledException;
import org.alfresco.repo.content.directurl.SystemWideDirectUrlConfig;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Unit tests for content service implementation.
 *
 * @author Sara Aspery
 */
public class ContentServiceImplUnitTest
{
    private static final Boolean ENABLED = Boolean.TRUE;
    private static final Boolean DISABLED = Boolean.FALSE;

    private static final Long SYS_DEFAULT_EXPIRY_TIME_IN_SECS = 30L;
    private static final Long SYS_MAX_EXPIRY_TIME_IN_SECS = 300L;

    private static final NodeRef NODE_REF = new NodeRef("content://Node/Ref");
    public static final String SOME_CONTENT_URL = "someContentUrl";
    private static final NodeRef NODE_REF_2 = new NodeRef("content://Node/Ref2");;

    private static final String X_AMZ_HEADER_1 = "x-amz-header1";
    private static final String VALUE_1 = "value1";
    private static final String X_AMZ_HEADER_2 = "x-amz-header-2";
    private static final String VALUE_2 = "value2";

    private static final QName PROP_CONTENT_QNAME = ContentModel.PROP_CONTENT;

    @InjectMocks
    private ContentServiceImpl contentService;

    @Mock
    private ContentStore mockContentStore;

    @Mock
    private NodeService mockNodeService;

    @Mock
    private ContentData mockContentData;

    @Mock
    private DictionaryService mockDictionaryService;

    @Before
    public void setup()
    {
        openMocks(this);
        when(mockNodeService.getProperty(NODE_REF, ContentModel.PROP_CONTENT)).thenReturn(mockContentData);
        when(mockContentData.getContentUrl()).thenReturn(SOME_CONTENT_URL);
        when(mockContentData.getMimetype()).thenReturn("someMimetype");
        when(mockNodeService.getProperty(NODE_REF, ContentModel.PROP_NAME)).thenReturn("someFilename");
    }

    @Test
    public void testIsContentDirectUrlEnabled_SystemWideIsDisabled()
    {
        setupSystemWideDirectAccessConfig(DISABLED);
        assertFalse("Expected contentDirectUrl to be disabled", contentService.isContentDirectUrlEnabled());
        verify(mockContentStore, never()).isContentDirectUrlEnabled();
    }

    @Test
    public void testIsContentDirectUrlEnabled_SystemWideIsEnabledButStoreIsDisabled()
    {
        setupSystemWideDirectAccessConfig(ENABLED);
        when(mockContentStore.isContentDirectUrlEnabled()).thenReturn(DISABLED);
        assertFalse("Expected contentDirectUrl to be disabled", contentService.isContentDirectUrlEnabled());
    }

    @Test
    public void testIsContentDirectUrlEnabled_SystemWideIsEnabledAndStoreIsEnabled()
    {
        setupSystemWideDirectAccessConfig(ENABLED);
        when(mockContentStore.isContentDirectUrlEnabled()).thenReturn(ENABLED);
        assertTrue("Expected contentDirectUrl to be enabled", contentService.isContentDirectUrlEnabled());
    }

    @Test
    public void testRequestContentDirectUrl_SystemWideIsDisabled()
    {
        setupSystemWideDirectAccessConfig(DISABLED);
        assertThrows(DirectAccessUrlDisabledException.class, () -> {
            contentService.requestContentDirectUrl(NODE_REF, PROP_CONTENT_QNAME, true, 20L);
        });
        verify(mockContentStore, never()).isContentDirectUrlEnabled();
    }

    @Test
    public void testRequestContentDirectUrl_SystemWideIsEnabledButStoreIsDisabled()
    {
        setupSystemWideDirectAccessConfig(ENABLED);
        when(mockContentStore.isContentDirectUrlEnabled()).thenReturn(DISABLED);

        DirectAccessUrl directAccessUrl = contentService.requestContentDirectUrl(NODE_REF, PROP_CONTENT_QNAME,true, 20L);
        assertNull(directAccessUrl);
        verify(mockContentStore, never()).requestContentDirectUrl(anyString(), eq(true), anyString(), anyString(), anyLong());
    }

    @Test
    public void testRequestContentDirectUrl_StoreIsEnabledButNotImplemented()
    {
        setupSystemWideDirectAccessConfig(ENABLED);
        when(mockContentStore.isContentDirectUrlEnabled()).thenReturn(ENABLED);

        DirectAccessUrl directAccessUrl = contentService.requestContentDirectUrl(NODE_REF, PROP_CONTENT_QNAME, true, 20L);
        assertNull(directAccessUrl);
        verify(mockContentStore, times(1)).requestContentDirectUrl(anyString(), eq(true), anyString(), anyString(), anyLong());
    }

    @Test
    public void shouldReturnStoragePropertiesWhenTheyExist()
    {
        final Map<String, String> storageObjectPropsMap = Map.of(X_AMZ_HEADER_1, VALUE_1, X_AMZ_HEADER_2, VALUE_2);
        when(mockContentStore.getStorageProperties(SOME_CONTENT_URL)).thenReturn(storageObjectPropsMap);

        final Map<String, String> objectStorageProperties = contentService.getStorageProperties(NODE_REF, ContentModel.PROP_CONTENT);
        assertFalse(objectStorageProperties.isEmpty());
        assertEquals(storageObjectPropsMap, objectStorageProperties);
    }

    @Test
    public void shouldReturnEmptyStoragePropertiesWhenTheyDontExist()
    {
        when(mockContentStore.getStorageProperties(SOME_CONTENT_URL)).thenReturn(Collections.emptyMap());

        final Map<String, String> objectStorageProperties = contentService.getStorageProperties(NODE_REF, ContentModel.PROP_CONTENT);
        assertTrue(objectStorageProperties.isEmpty());
    }

    @Test
    public void getStoragePropertiesThrowsExceptionWhenNoContentFound()
    {
        final String dummyContentProperty = "dummy";
        when(mockNodeService.getProperty(NODE_REF_2, ContentModel.PROP_CONTENT)).thenReturn(dummyContentProperty);
        when(mockDictionaryService.getProperty(ContentModel.PROP_CONTENT)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            contentService.getStorageProperties(NODE_REF_2, ContentModel.PROP_CONTENT);
        });
    }

    @Test
    public void getStoragePropertiesThrowsExceptionWhenNoContentUrlFound()
    {
        final ContentData contentWithoutUrl = new ContentData(null, null, 0, null);
        when(mockNodeService.getProperty(NODE_REF_2, ContentModel.PROP_CONTENT)).thenReturn(contentWithoutUrl);

        assertThrows(IllegalArgumentException.class, () -> {
            contentService.getStorageProperties(NODE_REF_2, ContentModel.PROP_CONTENT);
        });
    }

    @Test
    public void shouldRequestSendContentToArchiveSucceed()
    {
        final boolean expectedResult = true;
        final Map<String, Serializable> archiveParams = Collections.emptyMap();
        when(mockContentStore.requestSendContentToArchive(SOME_CONTENT_URL, archiveParams)).thenReturn(expectedResult);
        boolean sendContentToArchive = contentService.requestSendContentToArchive(NODE_REF, ContentModel.PROP_CONTENT, archiveParams);
        assertEquals(expectedResult, sendContentToArchive);
    }

    @Test
    public void requestSendContentToArchiveThrowsExceptionWhenNotImplemented()
    {
        final Map<String, Serializable> archiveParams = Collections.emptyMap();
        when(mockContentStore.requestSendContentToArchive(SOME_CONTENT_URL, archiveParams)).thenCallRealMethod();
        assertThrows(UnsupportedOperationException.class, () -> {
            contentService.requestSendContentToArchive(NODE_REF, ContentModel.PROP_CONTENT, archiveParams);
        });
    }

    @Test
    public void requestSendContentToArchiveThrowsExceptionWhenContentNotFound()
    {
        final String dummyContentProperty = "dummy";
        final Map<String, Serializable> archiveParams = Collections.emptyMap();
        when(mockNodeService.getProperty(NODE_REF_2, ContentModel.PROP_CONTENT)).thenReturn(dummyContentProperty);
        when(mockDictionaryService.getProperty(ContentModel.PROP_CONTENT)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            contentService.requestSendContentToArchive(NODE_REF_2, ContentModel.PROP_CONTENT, archiveParams);
        });
    }

    @Test
    public void requestRestoreContentFromArchiveThrowsExceptionWhenContentNotFound()
    {
        final String dummyContentProperty = "dummy";
        when(mockNodeService.getProperty(NODE_REF_2, ContentModel.PROP_CONTENT)).thenReturn(dummyContentProperty);
        when(mockDictionaryService.getProperty(ContentModel.PROP_CONTENT)).thenReturn(null);
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), "High");

        assertThrows(IllegalArgumentException.class, () -> {
            contentService.requestRestoreContentFromArchive(NODE_REF_2, ContentModel.PROP_CONTENT, restoreParams);
        });
    }

    @Test
    public void shouldRequestRestoreContentFromArchiveSucceed()
    {
        final boolean expectedResult = true;
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), "High");
        when(mockContentStore.requestRestoreContentFromArchive(SOME_CONTENT_URL, restoreParams)).thenReturn(expectedResult);

        boolean restoreContentFromArchive =
                contentService.requestRestoreContentFromArchive(NODE_REF, ContentModel.PROP_CONTENT, restoreParams);

        assertEquals(expectedResult, restoreContentFromArchive);
    }

    @Test
    public void requestRestoreContentFromArchiveThrowsExceptionWhenNotImplemented()
    {
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), "High");
        when(mockContentStore.requestRestoreContentFromArchive(SOME_CONTENT_URL, restoreParams)).thenCallRealMethod();
        assertThrows(UnsupportedOperationException.class, () -> {
            contentService.requestRestoreContentFromArchive(NODE_REF, ContentModel.PROP_CONTENT, restoreParams);
        });
    }

    /* Helper method to set system-wide direct access url configuration settings */
    private void setupSystemWideDirectAccessConfig(Boolean isEnabled)
    {
        SystemWideDirectUrlConfig sysConfig = new SystemWideDirectUrlConfig();
        sysConfig.setEnabled(isEnabled);
        sysConfig.setDefaultExpiryTimeInSec(SYS_DEFAULT_EXPIRY_TIME_IN_SECS);
        sysConfig.setMaxExpiryTimeInSec(SYS_MAX_EXPIRY_TIME_IN_SECS);
        sysConfig.validate();
        contentService.setSystemWideDirectUrlConfig(sysConfig);
    }
}
