/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DisposableActionBatchWorkProvider}.
 */
public class DisposableActionBatchWorkProviderUnitTest
{
    private static final NodeRef ROOT      = new NodeRef("workspace://SpacesStore/root");
    private static final NodeRef FOLDER_1  = new NodeRef("workspace://SpacesStore/folder-1");
    private static final NodeRef FOLDER_2  = new NodeRef("workspace://SpacesStore/folder-2");
    private static final NodeRef FOLDER_3  = new NodeRef("workspace://SpacesStore/folder-3");
    private static final NodeRef RECORD_1  = new NodeRef("workspace://SpacesStore/record-1");
    private static final NodeRef RECORD_2  = new NodeRef("workspace://SpacesStore/record-2");
    private static final NodeRef CATEGORY  = new NodeRef("workspace://SpacesStore/category");

    private final FilePlanService filePlanService           = mock(FilePlanService.class);
    private final RecordFolderService recordFolderService   = mock(RecordFolderService.class);
    private final RecordService recordService = mock(RecordService.class);
    private final DispositionService dispositionService = mock(DispositionService.class);
    private final RetryingTransactionHelper retryingTransactionHelper     = mock(RetryingTransactionHelper.class);

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        doAnswer(invocation -> {
            RetryingTransactionCallback<?> callback = invocation.getArgument(0);
            return callback.execute();
        }).when(retryingTransactionHelper).doInTransaction(any(RetryingTransactionCallback.class), eq(false), eq(true));

        // By default nothing is a record folder or category
        when(filePlanService.getAllContained(ROOT)).thenReturn(emptyList());
    }

    private DisposableActionBatchWorkProvider provider(boolean recordLevel, int batchSize)
    {
        return new DisposableActionBatchWorkProvider(
                recordLevel, ROOT, batchSize,
                filePlanService, recordFolderService, recordService, dispositionService, retryingTransactionHelper);
    }

    @Test
    public void testGetNextWork_folderLevelDisposition_returnsFolders()
    {
        when(filePlanService.getAllContained(ROOT)).thenReturn(asList(FOLDER_1, FOLDER_2));
        when(recordFolderService.isRecordFolder(FOLDER_1)).thenReturn(true);
        when(recordFolderService.isRecordFolder(FOLDER_2)).thenReturn(true);

        DisposableActionBatchWorkProvider workProvider = provider(false, 10);

        Collection<NodeRef> batch = workProvider.getNextWork();
        assertEquals(asList(FOLDER_1, FOLDER_2), batch);
        assertTrue(workProvider.getNextWork().isEmpty());
    }

    @Test
    public void testGetNextWork_recordLevelDisposition_returnsRecords()
    {
        when(filePlanService.getAllContained(ROOT)).thenReturn(singletonList(FOLDER_1));
        when(recordFolderService.isRecordFolder(FOLDER_1)).thenReturn(true);
        when(recordService.getRecords(FOLDER_1)).thenReturn(asList(RECORD_1, RECORD_2));

        DisposableActionBatchWorkProvider workProvider = provider(true, 10);

        Collection<NodeRef> batch = workProvider.getNextWork();
        assertEquals(asList(RECORD_1, RECORD_2), batch);
        assertTrue(workProvider.getNextWork().isEmpty());
    }

    @Test
    public void testGetNextWork_expandsNestedCategoryWithoutSchedule()
    {
        when(filePlanService.getAllContained(ROOT)).thenReturn(singletonList(CATEGORY));
        when(filePlanService.isRecordCategory(CATEGORY)).thenReturn(true);
        when(dispositionService.getAssociatedDispositionSchedule(CATEGORY)).thenReturn(null);

        when(filePlanService.getAllContained(CATEGORY)).thenReturn(singletonList(FOLDER_1));
        when(recordFolderService.isRecordFolder(FOLDER_1)).thenReturn(true);

        DisposableActionBatchWorkProvider workProvider = provider(false, 10);

        Collection<NodeRef> batch = workProvider.getNextWork();
        assertEquals(singletonList(FOLDER_1), batch);
        assertTrue(workProvider.getNextWork().isEmpty());
    }

    @Test
    public void testGetNextWork_skipsNestedCategoryWithOwnSchedule()
    {
        when(filePlanService.getAllContained(ROOT)).thenReturn(singletonList(CATEGORY));
        when(filePlanService.isRecordCategory(CATEGORY)).thenReturn(true);
        when(dispositionService.getAssociatedDispositionSchedule(CATEGORY)).thenReturn(mock(DispositionSchedule.class));

        DisposableActionBatchWorkProvider workProvider = provider(false, 10);

        assertTrue(workProvider.getNextWork().isEmpty());
    }

    @Test
    public void testGetNextWork_returnsEmptyWhenExhausted()
    {
        when(filePlanService.getAllContained(ROOT)).thenReturn(singletonList(FOLDER_1));
        when(recordFolderService.isRecordFolder(FOLDER_1)).thenReturn(true);

        DisposableActionBatchWorkProvider workProvider = provider(false, 10);

        workProvider.getNextWork();
        assertTrue(workProvider.getNextWork().isEmpty());
    }

    @Test
    public void testGetNextWork_respectsBatchSize()
    {
        when(filePlanService.getAllContained(ROOT)).thenReturn(asList(FOLDER_1, FOLDER_2, FOLDER_3));
        when(recordFolderService.isRecordFolder(FOLDER_1)).thenReturn(true);
        when(recordFolderService.isRecordFolder(FOLDER_2)).thenReturn(true);
        when(recordFolderService.isRecordFolder(FOLDER_3)).thenReturn(true);

        DisposableActionBatchWorkProvider workProvider = provider(false, 2);

        List<NodeRef> firstBatch = (List<NodeRef>) workProvider.getNextWork();
        assertEquals(2, firstBatch.size());

        List<NodeRef> secondBatch = (List<NodeRef>) workProvider.getNextWork();
        assertEquals(1, secondBatch.size());

        assertTrue(workProvider.getNextWork().isEmpty());
    }
}
