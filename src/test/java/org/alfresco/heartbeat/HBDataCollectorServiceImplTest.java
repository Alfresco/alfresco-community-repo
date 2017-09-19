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
import org.alfresco.heartbeat.datasender.HBDataSenderService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * @author eknizat
 */
public class HBDataCollectorServiceImplTest
{

    private HBDataCollectorServiceImpl dataCollectorService;
    private HBDataSenderService mockDataSenderService;

    @Before
    public void setUp()
    {
        mockDataSenderService = mock(HBDataSenderService.class);
        dataCollectorService = new HBDataCollectorServiceImpl(true);
        dataCollectorService.setHbDataSenderService(mockDataSenderService);
    }



    @Test
    public void testInitialEnabledEqualsDefaultState()
    {
        HBDataCollectorService dataCollectorService = new HBDataCollectorServiceImpl(true);
        assertTrue(dataCollectorService.isEnabledByDefault());

        dataCollectorService = new HBDataCollectorServiceImpl(false);
        assertFalse(dataCollectorService.isEnabledByDefault());
    }


    @Test
    public void testHBDataSenderServiceEnabledChange()
    {
        dataCollectorService.enabled(false);
        verify(mockDataSenderService).enable(false);

        dataCollectorService.enabled(true);
        verify(mockDataSenderService).enable(true);
    }

    @Test
    public void testCollectAndSendData()
    {
        // Set up dummy collector 1
        HBData c1Data = new HBData("sys", "c1","1.0",null);
        HBBaseDataCollector c1 = new DummyCollector(c1Data);
        c1.setHbDataCollectorService(dataCollectorService);
        c1.register();
        // Set up dummy collector 2
        HBData c2Data = new HBData("sys", "c2","1.0",null);
        HBBaseDataCollector c2 = new DummyCollector(c2Data);
        c2.setHbDataCollectorService(dataCollectorService);
        c2.register();
        // Set up dummy collector 3
        HBData c3Data = new HBData("sys", "c3","1.0",null);
        HBBaseDataCollector c3 = new DummyCollector(c3Data);
        c3.setHbDataCollectorService(dataCollectorService);
        c3.register();

        // Check that the collector service collects data from registered collectors and passes the data to data sender service
        dataCollectorService.collectAndSendData();

        // Check data is passed for each collector
        verify(mockDataSenderService, times(3)).sendData(any(List.class));
        verify(mockDataSenderService).sendData(c1.collectData());
        verify(mockDataSenderService).sendData(c2.collectData());
        verify(mockDataSenderService).sendData(c3.collectData());
    }


    class DummyCollector extends HBBaseDataCollector
    {
        private HBData data;

        public DummyCollector (HBData testData)
        {
         this.data = testData;
        }

        @Override
        public List<HBData> collectData() {
            return Arrays.asList(data);
        }
    }

}
