/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.heartbeat;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.admin.RepoServerMgmtMBean;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.descriptor.Descriptor;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SessionsUsageDataCollectorTest
{
    private SessionsUsageDataCollector sessionsUsageDataCollector;
    private HBDataCollectorService mockCollectorService;
    private DescriptorDAO mockDescriptorDAO;
    private List<HBData> collectedData;
    private HeartBeatJobScheduler mockScheduler;
    private RepoServerMgmtMBean repoServerMgmtMBean;

    private static final int TICKET_NON_EXPIRED = 10;

    @Before
    public void setUp()
    {
        mockDescriptorDAO = mock(DescriptorDAO.class);
        mockCollectorService = mock(HBDataCollectorService.class);
        mockScheduler = mock(HeartBeatJobScheduler.class);
        repoServerMgmtMBean = mock(RepoServerMgmtMBean.class);
        when(repoServerMgmtMBean.getTicketCountNonExpired()).thenReturn(TICKET_NON_EXPIRED);

        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getId()).thenReturn("mock_id");
        when(mockDescriptorDAO.getDescriptor()).thenReturn(mockDescriptor);

        sessionsUsageDataCollector = new SessionsUsageDataCollector("acs.repository.usage.sessions","1.0","0 0 0/1 ? * *", mockScheduler);
        sessionsUsageDataCollector.setHbDataCollectorService(mockCollectorService);
        sessionsUsageDataCollector.setCurrentRepoDescriptorDAO(mockDescriptorDAO);
        sessionsUsageDataCollector.setRepoServerMgmt(repoServerMgmtMBean);

        collectedData = sessionsUsageDataCollector.collectData();
    }

    @Test
    public void testHBDataFields()
    {
        for (HBData data : this.collectedData)
        {
            assertNotNull(data.getCollectorId());
            assertNotNull(data.getCollectorVersion());
            assertNotNull(data.getSchemaVersion());
            assertNotNull(data.getSystemId());
            assertNotNull(data.getTimestamp());
        }
    }

    @Test
    public void testSystemUsageDataIsCollected()
    {
        HBData sessionsUsage = grabDataByCollectorId(sessionsUsageDataCollector.getCollectorId());
        assertNotNull("Sessions usage data missing.", sessionsUsage);

        Map<String,Object> data = sessionsUsage.getData();
        assertTrue(data.containsKey("activeTickets"));
        assertEquals("Wrong number of active tickets", TICKET_NON_EXPIRED, data.get("activeTickets"));
    }

    private HBData grabDataByCollectorId(String collectorId)
    {
        for (HBData d : this.collectedData)
        {
            if (d.getCollectorId() != null && d.getCollectorId().equals(collectorId))
            {
                return d;
            }
        }
        return null;
    }
}
