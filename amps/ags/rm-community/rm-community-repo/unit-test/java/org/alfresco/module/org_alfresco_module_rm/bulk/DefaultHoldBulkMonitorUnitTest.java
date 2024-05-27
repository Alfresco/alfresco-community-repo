///*
// * #%L
// * Alfresco Records Management Module
// * %%
// * Copyright (C) 2005 - 2024 Alfresco Software Limited
// * %%
// * This file is part of the Alfresco software.
// * -
// * If the software was purchased under a paid Alfresco license, the terms of
// * the paid license agreement will prevail.  Otherwise, the software is
// * provided under the following open source license terms:
// * -
// * Alfresco is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// * -
// * Alfresco is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// * -
// * You should have received a copy of the GNU Lesser General Public License
// * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
// * #L%
// */
//package org.alfresco.module.org_alfresco_module_rm.bulk;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.when;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//
//import org.alfresco.module.org_alfresco_module_rm.bulk.hold.DefaultHoldBulkMonitor;
//import org.alfresco.module.org_alfresco_module_rm.bulk.hold.HoldBulkProcessDetails;
//import org.alfresco.repo.cache.SimpleCache;
//import org.alfresco.rm.rest.api.model.HoldBulkStatus;
//import org.alfresco.service.cmr.repository.NodeRef;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//
//public class DefaultHoldBulkMonitorUnitTest
//{
//
//    @Mock
//    private SimpleCache<String, HoldBulkStatus> holdProgressCache;
//
//    @Mock
//    private SimpleCache<String, List<HoldBulkProcessDetails>> holdProcessRegistry;
//
//    private DefaultHoldBulkMonitor holdBulkMonitor;
//
//    @Before
//    public void setUp()
//    {
//        MockitoAnnotations.openMocks(this);
//        holdBulkMonitor = new DefaultHoldBulkMonitor();
//        holdBulkMonitor.setHoldProgressCache(holdProgressCache);
//        holdBulkMonitor.setHoldProcessRegistry(holdProcessRegistry);
//    }
//
//    @Test
//    public void testUpdateBulkStatus()
//    {
//        HoldBulkStatus status = new HoldBulkStatus("bulkStatusId", null, null, 0L, 0L, 0L, null);
//
//        holdBulkMonitor.updateBulkStatus(status);
//
//        Mockito.verify(holdProgressCache).put("bulkStatusId", status);
//    }
//
//    @Test
//    public void testRegisterProcess()
//    {
//        NodeRef holdRef = new NodeRef("workspace://SpacesStore/holdId");
//        String processId = "processId";
//        when(holdProcessRegistry.get(holdRef.getId())).thenReturn(null);
//
//        holdBulkMonitor.registerProcess(holdRef, processId);
//
//        Mockito.verify(holdProcessRegistry)
//            .put(holdRef.getId(), Arrays.asList(new HoldBulkProcessDetails(processId, null)));
//    }
//
//    @Test
//    public void testGetBulkStatusesForHoldReturnsEmptyListWhenNoProcesses()
//    {
//        when(holdProcessRegistry.get("holdId")).thenReturn(null);
//        assertEquals(Collections.emptyList(), holdBulkMonitor.getBulkStatusesForHold("holdId"));
//    }
//
//    @Test
//    public void testGetBulkStatusesForHoldReturnsSortedStatuses()
//    {
//        HoldBulkStatus status1 = new HoldBulkStatus(null, new Date(1000), new Date(2000), 0L, 0L, 0L, null);
//        HoldBulkStatus status2 = new HoldBulkStatus(null, new Date(3000), null, 0L, 0L, 0L, null);
//        HoldBulkStatus status3 = new HoldBulkStatus(null, new Date(4000), null, 0L, 0L, 0L, null);
//        HoldBulkStatus status4 = new HoldBulkStatus(null, new Date(500), new Date(800), 0L, 0L, 0L, null);
//        HoldBulkStatus status5 = new HoldBulkStatus(null, null, null, 0L, 0L, 0L, null);
//
//        when(holdProcessRegistry.get("holdId")).thenReturn(
//            Arrays.asList("process1", "process2", "process3", "process4", "process5")
//                .stream().map(bulkStatusId -> new HoldBulkProcessDetails(bulkStatusId, null)).toList());
//        when(holdProgressCache.get("process1")).thenReturn(status1);
//        when(holdProgressCache.get("process2")).thenReturn(status2);
//        when(holdProgressCache.get("process3")).thenReturn(status3);
//        when(holdProgressCache.get("process4")).thenReturn(status4);
//        when(holdProgressCache.get("process5")).thenReturn(status5);
//
//        assertEquals(Arrays.asList(status5, status3, status2, status1, status4),
//            holdBulkMonitor.getBulkStatusesForHold("holdId"));
//    }
//}