/*
 * #%L
 * Alfresco Remote API
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.impl;

import org.alfresco.repo.content.ContentRestoreParams;
import org.alfresco.rest.api.model.ArchiveContentRequest;
import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.rest.api.model.RestoreArchivedContentRequest;
import org.alfresco.rest.framework.core.exceptions.RestoreInProgressException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentStorageInformationImplTest
{
    private static final String DUMMY_NODE_ID = "dummy-node-id";
    private static final String CONTENT_PROP_NAME = "cm:content";
    private static final String STANDARD_PRIORITY = "Standard";

    private static final NodeRef DUMMY_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_NODE_ID);

    @Mock
    private ContentService contentService;
    @Mock
    private NamespaceService namespaceService;

    @InjectMocks
    private ContentStorageInformationImpl objectUnderTest;

    @Test
    public void shouldReturnStorageInfoResponseWithNonEmptyStorageProps()
    {
        final Map<String, String> storageProps = Map.of("x-amz-storage-class", "INTELLIGENT_TIERING", "x-alf-archived", "false");
        when(contentService.getStorageProperties(eq(DUMMY_NODE_REF), any())).thenReturn(storageProps);
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        when(namespaceService.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI))
                .thenReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));

        final ContentStorageInfo storageInfo = objectUnderTest.getStorageInfo(DUMMY_NODE_REF, CONTENT_PROP_NAME, null);

        assertEquals(storageProps, storageInfo.getStorageProperties());
        assertEquals(CONTENT_PROP_NAME, storageInfo.getId());
    }

    @Test
    public void shouldReturnStorageInfoResponseWithEmptyStorageProps()
    {
        when(contentService.getStorageProperties(eq(DUMMY_NODE_REF), any())).thenCallRealMethod();
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);
        when(namespaceService.getPrefixes(NamespaceService.CONTENT_MODEL_1_0_URI))
                .thenReturn(List.of(NamespaceService.CONTENT_MODEL_PREFIX));

        final ContentStorageInfo storageInfo = objectUnderTest.getStorageInfo(DUMMY_NODE_REF, CONTENT_PROP_NAME, null);

        assertEquals(Collections.emptyMap(), storageInfo.getStorageProperties());
        assertEquals(CONTENT_PROP_NAME, storageInfo.getId());
    }

    @Test
    public void shouldSucceedOnArchiveContent()
    {
        final Map<String, Serializable> archiveProps = Collections.emptyMap();
        final ArchiveContentRequest archiveParamsRequest = new ArchiveContentRequest();
        archiveParamsRequest.setArchiveParams(archiveProps);
        final boolean expectedResult = true;

        when(contentService.requestSendContentToArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(archiveProps))).thenReturn(expectedResult);
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        final boolean requestArchiveContent = objectUnderTest.requestArchiveContent(DUMMY_NODE_REF, CONTENT_PROP_NAME, archiveParamsRequest);

        assertEquals(expectedResult, requestArchiveContent);
    }

    @Test
    public void shouldSucceedOnArchiveContentWhenNoRequestBody()
    {
        final Map<String, Serializable> archiveProps = Collections.emptyMap();
        final boolean expectedResult = true;

        when(contentService.requestSendContentToArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(archiveProps))).thenReturn(expectedResult);
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        final boolean requestArchiveContent = objectUnderTest.requestArchiveContent(DUMMY_NODE_REF, CONTENT_PROP_NAME, null);

        assertEquals(expectedResult, requestArchiveContent);
    }

    @Test
    public void shouldNotSucceedOnArchiveContent()
    {
        final Map<String, Serializable> archiveProps = Collections.emptyMap();
        final ArchiveContentRequest archiveParamsRequest = new ArchiveContentRequest();
        archiveParamsRequest.setArchiveParams(archiveProps);
        final boolean expectedResult = false;

        when(contentService.requestSendContentToArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(archiveProps))).thenReturn(expectedResult);
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        final boolean requestArchiveContent = objectUnderTest.requestArchiveContent(DUMMY_NODE_REF, CONTENT_PROP_NAME, archiveParamsRequest);

        assertEquals(expectedResult, requestArchiveContent);
    }

    @Test
    public void shouldThrowExceptionOnArchiveContent()
    {
        final Map<String, Serializable> archiveProps = Collections.emptyMap();
        final ArchiveContentRequest archiveParamsRequest = new ArchiveContentRequest();
        archiveParamsRequest.setArchiveParams(archiveProps);

        when(contentService.requestSendContentToArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(archiveProps))).thenCallRealMethod();
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        assertThrows(UnsupportedOperationException.class,
                () -> objectUnderTest.requestArchiveContent(DUMMY_NODE_REF, CONTENT_PROP_NAME, archiveParamsRequest));
    }

    @Test
    public void shouldSucceedOnRestoreContentFromArchive()
    {
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), STANDARD_PRIORITY);
        final RestoreArchivedContentRequest restoreArchivedContentRequest = new RestoreArchivedContentRequest();
        restoreArchivedContentRequest.setRestorePriority(STANDARD_PRIORITY);
        final boolean expectedResult = true;

        when(contentService.requestRestoreContentFromArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(restoreParams))).thenReturn(expectedResult);
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        final boolean requestArchiveContent = objectUnderTest.requestRestoreContentFromArchive(DUMMY_NODE_REF, CONTENT_PROP_NAME, restoreArchivedContentRequest);

        assertEquals(expectedResult, requestArchiveContent);
    }

    @Test
    public void shouldSucceedOnRestoreContentFromArchiveWhenNoRequestBody()
    {
        final boolean expectedResult = true;

        when(contentService.requestRestoreContentFromArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(Collections.emptyMap())))
                .thenReturn(expectedResult);
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        final boolean requestArchiveContent = objectUnderTest.requestRestoreContentFromArchive(DUMMY_NODE_REF, CONTENT_PROP_NAME, null);

        assertEquals(expectedResult, requestArchiveContent);
    }

    @Test
    public void shouldNotSucceedOnRestoreContentFromArchive()
    {
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), STANDARD_PRIORITY);
        final RestoreArchivedContentRequest restoreArchivedContentRequest = new RestoreArchivedContentRequest();
        restoreArchivedContentRequest.setRestorePriority(STANDARD_PRIORITY);
        final boolean expectedResult = false;

        when(contentService.requestRestoreContentFromArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(restoreParams))).thenReturn(expectedResult);
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        final boolean requestArchiveContent = objectUnderTest.requestRestoreContentFromArchive(DUMMY_NODE_REF, CONTENT_PROP_NAME, restoreArchivedContentRequest);

        assertEquals(expectedResult, requestArchiveContent);
    }

    @Test
    public void shouldThrowExceptionRestoreContentFromArchive()
    {
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), STANDARD_PRIORITY);
        final RestoreArchivedContentRequest restoreArchivedContentRequest = new RestoreArchivedContentRequest();
        restoreArchivedContentRequest.setRestorePriority(STANDARD_PRIORITY);

        when(contentService.requestRestoreContentFromArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(restoreParams))).thenCallRealMethod();
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        assertThrows(UnsupportedOperationException.class,
                () -> objectUnderTest.requestRestoreContentFromArchive(DUMMY_NODE_REF, CONTENT_PROP_NAME, restoreArchivedContentRequest));
    }

    @Test
    public void shouldThrowRestoreInProgressExceptionRestoreContentFromArchive()
    {
        final Map<String, Serializable> restoreParams = Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), STANDARD_PRIORITY);
        final RestoreArchivedContentRequest restoreArchivedContentRequest = new RestoreArchivedContentRequest();
        restoreArchivedContentRequest.setRestorePriority(STANDARD_PRIORITY);

        when(contentService.requestRestoreContentFromArchive(eq(DUMMY_NODE_REF), any(QName.class), eq(restoreParams))).thenThrow(new org.alfresco.service.cmr.repository.RestoreInProgressException("Error"));
        when(namespaceService.getNamespaceURI(NamespaceService.CONTENT_MODEL_PREFIX)).thenReturn(NamespaceService.CONTENT_MODEL_1_0_URI);

        assertThrows(RestoreInProgressException.class, 
                () -> objectUnderTest.requestRestoreContentFromArchive(DUMMY_NODE_REF, CONTENT_PROP_NAME, restoreArchivedContentRequest));
    }
}
