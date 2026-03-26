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

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link DispositionActionBatchProcessor}.
 */
public class DispositionActionBatchProcessorUnitTest
{
    private static final NodeRef ACTION_DEF = new NodeRef("workspace://SpacesStore/disp-action-def");

    private final BroadcastDispositionActionDefinitionUpdateAction action = mock(BroadcastDispositionActionDefinitionUpdateAction.class);
    private final DispositionSchedule dispositionSchedule = mock(DispositionSchedule.class);
    private final TransactionService transactionService = mock(TransactionService.class);
    private final RetryingTransactionHelper txHelper = mock(RetryingTransactionHelper.class);

    @Before
    public void setUp()
    {
        when(transactionService.getRetryingTransactionHelper()).thenReturn(txHelper);
    }

    @Test
    public void testProcessesWhenBatchSizeReached() throws Throwable
    {
        doAnswer(invocation -> {
            RetryingTransactionCallback<?> callback = invocation.getArgument(0);
            callback.execute();
            return null;
        }).when(txHelper).doInTransaction(any(RetryingTransactionCallback.class), eq(false), eq(true));

        DispositionActionBatchProcessor processor = new DispositionActionBatchProcessor(
                2, action, dispositionSchedule, ACTION_DEF, emptyList(), transactionService);

        NodeRef item1 = new NodeRef("workspace://SpacesStore/item-1");
        NodeRef item2 = new NodeRef("workspace://SpacesStore/item-2");

        processor.addItem(item1);
        processor.addItem(item2);

        verify(action).updateDisposableItem(dispositionSchedule, item1, ACTION_DEF, emptyList());
        verify(action).updateDisposableItem(dispositionSchedule, item2, ACTION_DEF, emptyList());
        assertEquals(2, processor.getTotalQueued());
        assertEquals(2, processor.getTotalProcessed());
        assertEquals(0, processor.getTotalFailed());
        assertFalse(processor.isHasError());
    }

    @Test
    public void testProcessesRemainingItems() throws Throwable
    {
        doAnswer(invocation -> {
            RetryingTransactionCallback<?> callback = invocation.getArgument(0);
            callback.execute();
            return null;
        }).when(txHelper).doInTransaction(any(RetryingTransactionCallback.class), eq(false), eq(true));

        DispositionActionBatchProcessor processor = new DispositionActionBatchProcessor(
                2, action, dispositionSchedule, ACTION_DEF, emptyList(), transactionService);

        NodeRef item1 = new NodeRef("workspace://SpacesStore/item-1");
        NodeRef item2 = new NodeRef("workspace://SpacesStore/item-2");
        NodeRef item3 = new NodeRef("workspace://SpacesStore/item-3");

        processor.addItem(item1);
        processor.addItem(item2);
        processor.addItem(item3);
        processor.processRemainingItems();

        verify(action).updateDisposableItem(dispositionSchedule, item1, ACTION_DEF, emptyList());
        verify(action).updateDisposableItem(dispositionSchedule, item2, ACTION_DEF, emptyList());
        verify(action).updateDisposableItem(dispositionSchedule, item3, ACTION_DEF, emptyList());
        assertEquals(3, processor.getTotalProcessed());
        assertEquals(0, processor.getTotalFailed());
        assertFalse(processor.isHasError());
    }

    @Test
    public void testMarksBatchAsFailedWhenTransactionFails()
    {
        when(txHelper.doInTransaction(any(RetryingTransactionCallback.class), eq(false), eq(true)))
                .thenThrow(new RuntimeException("failed"));

        DispositionActionBatchProcessor processor = new DispositionActionBatchProcessor(
                2, action, dispositionSchedule, ACTION_DEF, emptyList(), transactionService);

        processor.addItem(new NodeRef("workspace://SpacesStore/item-1"));
        processor.addItem(new NodeRef("workspace://SpacesStore/item-2"));

        verify(action, times(0)).updateDisposableItem(any(), any(), any(), any());
        assertTrue(processor.isHasError());
        assertEquals(2, processor.getTotalQueued());
        assertEquals(0, processor.getTotalProcessed());
        assertEquals(2, processor.getTotalFailed());
    }
}
