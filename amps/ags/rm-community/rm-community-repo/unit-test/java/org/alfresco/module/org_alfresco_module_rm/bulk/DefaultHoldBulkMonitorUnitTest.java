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
package org.alfresco.module.org_alfresco_module_rm.bulk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.alfresco.module.org_alfresco_module_rm.bulk.hold.DefaultHoldBulkMonitor;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkProcessDetails;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkStatus;
import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkStatusAndProcessDetails;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class DefaultHoldBulkMonitorUnitTest
{

    @Mock
    private SimpleCache<String, HoldBulkStatus> holdProgressCache;

    @Mock
    private SimpleCache<Pair<String, String>, HoldBulkProcessDetails> holdProcessRegistry;

    private DefaultHoldBulkMonitor holdBulkMonitor;

    @Before
    public void setUp()
    {
        MockitoAnnotations.openMocks(this);
        holdBulkMonitor = new DefaultHoldBulkMonitor();
        holdBulkMonitor.setHoldProgressCache(holdProgressCache);
        holdBulkMonitor.setHoldProcessRegistry(holdProcessRegistry);
    }

    @Test
    public void testUpdateBulkStatus()
    {
        HoldBulkStatus status = new HoldBulkStatus("bulkStatusId", null, null, 0L, 0L, 0L, null, false, null);

        holdBulkMonitor.updateBulkStatus(status);

        Mockito.verify(holdProgressCache).put("bulkStatusId", status);
    }

    @Test
    public void testRegisterProcess()
    {
        NodeRef holdRef = new NodeRef("workspace://SpacesStore/holdId");
        String processId = "processId";
        when(holdProcessRegistry.get(new Pair<>(holdRef.getId(), processId))).thenReturn(null);

        holdBulkMonitor.registerProcess(holdRef, processId, null);

        Mockito.verify(holdProcessRegistry)
            .put(new Pair<>(holdRef.getId(), processId), new HoldBulkProcessDetails(processId, null, null));
    }

    @Test
    public void testGetBulkStatusesWithProcessDetailsReturnsEmptyListWhenNoProcessesWithProcessDetails()
    {
        when(holdProcessRegistry.getKeys()).thenReturn(Collections.emptyList());
        assertEquals(Collections.emptyList(), holdBulkMonitor.getBulkStatusesWithProcessDetails("holdId"));
    }

    @Test
    public void testGetBulkStatus()
    {
        BulkOperation bulkOperation = mock(BulkOperation.class);
        HoldBulkStatus status1 = new HoldBulkStatus("process1", new Date(1000), new Date(2000), 0L, 0L, 0L, null, false,
            null);
        when(holdProcessRegistry.get(new Pair<>("holdId", "process1"))).thenReturn(
            new HoldBulkProcessDetails("process1", null, bulkOperation));
        when(holdProgressCache.get("process1")).thenReturn(status1);

        assertEquals(new HoldBulkStatusAndProcessDetails(status1,
                new HoldBulkProcessDetails(status1.bulkStatusId(), null, bulkOperation)),
            holdBulkMonitor.getBulkStatusWithProcessDetails("holdId", "process1"));
    }

    @Test
    public void testGetNonExistingBulkStatus()
    {
        BulkOperation bulkOperation = mock(BulkOperation.class);
        when(holdProcessRegistry.get(new Pair<>("holdId", "process1"))).thenReturn(
            new HoldBulkProcessDetails("process1", null, bulkOperation));
        when(holdProgressCache.get("process1")).thenReturn(null);

        assertNull(holdBulkMonitor.getBulkStatusWithProcessDetails("holdId", "process1"));
    }

    @Test
    public void testGetBulkStatusesForHoldReturnsSortedStatusesWithProcessDetails()
    {
        BulkOperation bulkOperation = mock(BulkOperation.class);
        HoldBulkStatus status1 = new HoldBulkStatus("process1", new Date(1000), new Date(2000), 0L, 0L, 0L, null, false,
            null);
        HoldBulkStatus status2 = new HoldBulkStatus("process2", new Date(3000), null, 0L, 0L, 0L, null, false, null);
        HoldBulkStatus status3 = new HoldBulkStatus("process3", new Date(4000), null, 0L, 0L, 0L, null, false, null);
        HoldBulkStatus status4 = new HoldBulkStatus("process4", new Date(500), new Date(800), 0L, 0L, 0L, null, false,
            null);
        HoldBulkStatus status5 = new HoldBulkStatus("process5", null, null, 0L, 0L, 0L, null, false, null);

        when(holdProcessRegistry.getKeys()).thenReturn(
            Arrays.asList(new Pair<>("holdId", "process1"), new Pair<>("holdId", "process2"),
                new Pair<>("holdId", "process3"), new Pair<>("holdId", "process4"), new Pair<>("holdId", "process5"))
                                                      );
        when(holdProcessRegistry.get(new Pair<>("holdId", "process1"))).thenReturn(
            new HoldBulkProcessDetails("process1", null, bulkOperation));
        when(holdProcessRegistry.get(new Pair<>("holdId", "process2"))).thenReturn(
            new HoldBulkProcessDetails("process2", null, bulkOperation));
        when(holdProcessRegistry.get(new Pair<>("holdId", "process3"))).thenReturn(
            new HoldBulkProcessDetails("process3", null, bulkOperation));
        when(holdProcessRegistry.get(new Pair<>("holdId", "process4"))).thenReturn(
            new HoldBulkProcessDetails("process4", null, bulkOperation));
        when(holdProcessRegistry.get(new Pair<>("holdId", "process5"))).thenReturn(
            new HoldBulkProcessDetails("process5", null, bulkOperation));
        when(holdProgressCache.get("process1")).thenReturn(status1);
        when(holdProgressCache.get("process2")).thenReturn(status2);
        when(holdProgressCache.get("process3")).thenReturn(status3);
        when(holdProgressCache.get("process4")).thenReturn(status4);
        when(holdProgressCache.get("process5")).thenReturn(status5);

        assertEquals(Arrays.asList(status5, status3, status2, status1, status4).stream().map(
                status -> new HoldBulkStatusAndProcessDetails(status,
                    new HoldBulkProcessDetails(status.bulkStatusId(), null, bulkOperation))).toList(),
            holdBulkMonitor.getBulkStatusesWithProcessDetails("holdId"));
    }
}
