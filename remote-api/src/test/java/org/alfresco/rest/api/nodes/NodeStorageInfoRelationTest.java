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

package org.alfresco.rest.api.nodes;

import junit.framework.TestCase;
import org.alfresco.rest.api.ContentStorageInformation;
import org.alfresco.rest.api.model.ArchiveContentRequest;
import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.rest.api.model.RestoreArchivedContentRequest;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NodeStorageInfoRelationTest extends TestCase
{
    private static final String DUMMY_NODE_ID = "dummy-node-id";
    private static final String CONTENT_PROP_NAME = "cm:content";
    private static final NodeRef DUMMY_NODE_REF = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, DUMMY_NODE_ID);

    @Mock
    private ContentStorageInformation storageInformation;
    @Mock
    private Parameters params;
    @Mock
    private WithResponse withResponse;

    @InjectMocks
    private NodeStorageInfoRelation objectUnderTest;


    @Test
    public void shouldProperlyReturnStorageInfo()
    {
        final ContentStorageInfo expectedStorageInfo = new ContentStorageInfo();
        final Map<String, String> storageProps = Map.of("x-amz-storage-class", "INTELLIGENT_TIERING", "x-alf-archived", "false");
        expectedStorageInfo.setStorageProperties(storageProps);
        expectedStorageInfo.setId(CONTENT_PROP_NAME);

        when(storageInformation.getStorageInfo(DUMMY_NODE_REF, CONTENT_PROP_NAME, params)).thenReturn(expectedStorageInfo);

        final ContentStorageInfo storageInfo = objectUnderTest.readById(DUMMY_NODE_ID, CONTENT_PROP_NAME, params);

        Assert.assertEquals(storageProps, storageInfo.getStorageProperties());
        Assert.assertEquals(CONTENT_PROP_NAME, storageInfo.getId());
    }

    @Test
    public void shouldProperlyRequestArchiveContent()
    {
        final ArchiveContentRequest archiveContentRequest = new ArchiveContentRequest();
        when(storageInformation.requestArchiveContent(DUMMY_NODE_REF, CONTENT_PROP_NAME, archiveContentRequest)).thenReturn(true);

        objectUnderTest.requestArchiveContent(DUMMY_NODE_ID, CONTENT_PROP_NAME, archiveContentRequest, params, withResponse);

        verify(withResponse, times(1)).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void shouldFailsOnRequestArchiveContent()
    {
        final ArchiveContentRequest archiveContentRequest = new ArchiveContentRequest();
        when(storageInformation.requestArchiveContent(DUMMY_NODE_REF, CONTENT_PROP_NAME, archiveContentRequest)).thenReturn(false);

        objectUnderTest.requestArchiveContent(DUMMY_NODE_ID, CONTENT_PROP_NAME, archiveContentRequest, params, withResponse);

        verify(withResponse, times(1)).setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    @Test
    public void shouldThrowExceptionOnRequestArchiveContent()
    {
        final ArchiveContentRequest archiveContentRequest = new ArchiveContentRequest();
        when(storageInformation.requestArchiveContent(DUMMY_NODE_REF, CONTENT_PROP_NAME, archiveContentRequest))
                .thenThrow(UnsupportedOperationException.class);

        assertThrows(UnsupportedOperationException.class,
                () -> objectUnderTest.requestArchiveContent(DUMMY_NODE_ID, CONTENT_PROP_NAME, archiveContentRequest, params, withResponse));
        verify(withResponse, never()).setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        verify(withResponse, never()).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void shouldProperlyRequestRestoreContentFromArchive()
    {
        final RestoreArchivedContentRequest restoreArchivedContentRequest = new RestoreArchivedContentRequest();
        when(storageInformation.requestRestoreContentFromArchive(DUMMY_NODE_REF, CONTENT_PROP_NAME, restoreArchivedContentRequest))
                .thenReturn(true);

        objectUnderTest
                .requestRestoreContentFromArchive(DUMMY_NODE_REF.getId(), CONTENT_PROP_NAME, restoreArchivedContentRequest, params, withResponse);

        verify(withResponse, times(1)).setStatus(HttpServletResponse.SC_ACCEPTED);
    }

    @Test
    public void shouldFailsOnRequestRestoreContentFromArchive()
    {
        final RestoreArchivedContentRequest restoreArchivedContentRequest = new RestoreArchivedContentRequest();
        when(storageInformation.requestRestoreContentFromArchive(DUMMY_NODE_REF, CONTENT_PROP_NAME, restoreArchivedContentRequest))
                .thenReturn(false);

        objectUnderTest
                .requestRestoreContentFromArchive(DUMMY_NODE_REF.getId(), CONTENT_PROP_NAME, restoreArchivedContentRequest, params, withResponse);

        verify(withResponse, times(1)).setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    @Test
    public void shouldThrowExceptionOnRequestRestoreContentFromArchive()
    {
        final RestoreArchivedContentRequest restoreArchivedContentRequest = new RestoreArchivedContentRequest();
        when(storageInformation.requestRestoreContentFromArchive(DUMMY_NODE_REF, CONTENT_PROP_NAME, restoreArchivedContentRequest))
                .thenThrow(UnsupportedOperationException.class);

        assertThrows(UnsupportedOperationException.class, () -> objectUnderTest
                .requestRestoreContentFromArchive(DUMMY_NODE_ID, CONTENT_PROP_NAME, restoreArchivedContentRequest, params, withResponse));
        verify(withResponse, never()).setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        verify(withResponse, never()).setStatus(HttpServletResponse.SC_ACCEPTED);
    }
}
