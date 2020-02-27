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
package org.alfresco.heartbeat;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.dictionary.CustomModelsInfo;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author eknizat
 */
public class ModelUsageDataCollectorTest
{

    private ModelUsageDataCollector usageModelCollector;
    private HBDataCollectorService mockCollectorService;
    private DescriptorDAO mockDescriptorDAO;
    private List<HBData> collectedData;
    private HeartBeatJobScheduler mockScheduler;

    @Before
    public void setUp()
    {
        mockDescriptorDAO = mock(DescriptorDAO.class);
        mockCollectorService = mock(HBDataCollectorService.class);
        mockScheduler = mock(HeartBeatJobScheduler.class);

        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getId()).thenReturn("mock_id");
        when(mockDescriptorDAO.getDescriptor()).thenReturn(mockDescriptor);

        CustomModelsInfo mockCustomModelsInfo = mock(CustomModelsInfo.class);
        CustomModelService customModelService = mock(CustomModelService.class);
        when(customModelService.getCustomModelsInfo()).thenReturn(mockCustomModelsInfo);

        TransactionService mockTransactionService = mock(TransactionService.class);
        RetryingTransactionHelper mockRetryingTransactionHelper = mock(RetryingTransactionHelper.class);
        when(mockRetryingTransactionHelper.doInTransaction(any(RetryingTransactionHelper.RetryingTransactionCallback.class), anyBoolean())).thenReturn(mockCustomModelsInfo);
        when(mockTransactionService.getRetryingTransactionHelper()).thenReturn(mockRetryingTransactionHelper);

        usageModelCollector = new ModelUsageDataCollector("acs.repository.usage.model","1.0", "0 0 0 ? * *", mockScheduler);

        usageModelCollector.setHbDataCollectorService(mockCollectorService);
        usageModelCollector.setCurrentRepoDescriptorDAO(mockDescriptorDAO);
        usageModelCollector.setCustomModelService(customModelService);
        usageModelCollector.setTransactionService(mockTransactionService);

        collectedData = usageModelCollector.collectData();
    }

    @Test
    public void testHBDataFields()
    {
        for(HBData data : this.collectedData)
        {
            assertNotNull(data.getCollectorId());
            assertNotNull(data.getCollectorVersion());
            assertNotNull(data.getSchemaVersion());
            assertNotNull(data.getSystemId());
            assertNotNull(data.getTimestamp());
        }
    }

    @Test
    public void testModelUsageDataIsCollected()
    {
        HBData modelUsage = grabDataByCollectorId(usageModelCollector.getCollectorId());
        assertNotNull("Model usage data missing.", modelUsage);

        Map<String,Object> data = modelUsage.getData();
        assertTrue(data.containsKey("numOfActiveModels"));
        assertTrue(data.containsKey("numOfActiveTypes"));
        assertTrue(data.containsKey("numOfActiveAspects"));

    }

    private HBData grabDataByCollectorId(String collectorId)
    {
        for (HBData d : this.collectedData)
        {
            if(d.getCollectorId()!=null && d.getCollectorId().equals(collectorId))
            {
                return d;
            }
        }
        return null;
    }
}
