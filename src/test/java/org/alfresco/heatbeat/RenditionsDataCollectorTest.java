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
package org.alfresco.heatbeat;

import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;
import org.alfresco.heartbeat.RenditionsDataCollector;
import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.descriptor.Descriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test the RenditionsDataCollector collects the correct data.
 */
public class RenditionsDataCollectorTest
{
    private RenditionsDataCollector renditionsDataCollector;
    private HBDataCollectorService mockCollectorService;
    private DescriptorDAO mockDescriptorDAO;
    private List<HBData> collectedData;
    private HeartBeatJobScheduler mockScheduler;

    private TransformationOptions options = new TransformationOptions();
    private ThumbnailDefinition doclib = new ThumbnailDefinition("png", options, "doclib");
    private ThumbnailDefinition preview = new ThumbnailDefinition("pdf", options, "preview");

    @Before
    public void setUp()
    {
        mockDescriptorDAO = mock(DescriptorDAO.class);
        mockCollectorService = mock(HBDataCollectorService.class);
        mockScheduler = mock(HeartBeatJobScheduler.class);

        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getId()).thenReturn("mock_id");
        when(mockDescriptorDAO.getDescriptor()).thenReturn(mockDescriptor);

        renditionsDataCollector = new RenditionsDataCollector("acs.repository.renditions","1.0","0 0 0 ? * *", mockScheduler);
        renditionsDataCollector.setHbDataCollectorService(mockCollectorService);
        renditionsDataCollector.setCurrentRepoDescriptorDAO(mockDescriptorDAO);
    }

    @Test
    public void testHBDataFields()
    {
        // record 2 renditions
        renditionsDataCollector.recordRenditionRequest(preview, "docx");
        renditionsDataCollector.recordRenditionRequest(doclib, "docx");
        collectedData = renditionsDataCollector.collectData();

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
    public void testCollectedDataInDetail()
    {
        // Record an initial batch of 4 renditions
        renditionsDataCollector.recordRenditionRequest(doclib, "xls");
        renditionsDataCollector.recordRenditionRequest(doclib, "xls");
        renditionsDataCollector.recordRenditionRequest(preview, "docx");
        renditionsDataCollector.recordRenditionRequest(doclib, "docx");
        collectedData = renditionsDataCollector.collectData();

        assertEquals("There should have been 3 data elements", 3, collectedData.size());

        Date firstTimestamp = null;
        for (HBData data : collectedData)
        {
            if (firstTimestamp == null)
            {
                firstTimestamp = data.getTimestamp();
            }
            else
            {
                assertEquals("All data in a batch should have the same timestamp", firstTimestamp, data.getTimestamp());
            }

            Map<String, Object> values = data.getData();
            assertEquals("There should have been 4 mapped values", 4, values.size());

            String rendition = (String)values.get("rendition");
            String sourceMimetype = (String)values.get("sourceMimetype");
            String targetMimetype = (String)values.get("targetMimetype");
            Integer count = (Integer)values.get("count");

            assertNotNull(rendition);
            assertNotNull(sourceMimetype);
            assertNotNull(targetMimetype);
            assertNotNull(count);
        }

        assertHBDataContains("doclib", "xls", "png", 2);
        assertHBDataContains("doclib", "docx", "png", 1);
        assertHBDataContains("preview", "docx", "pdf", 1);
    }

    @Test
    public void testMultipleCollections() throws InterruptedException
    {
        // A batch of 0 renditions
        collectedData = renditionsDataCollector.collectData();
        assertEquals("There should have been 0 data elements", 0, collectedData.size());

        // Record a batch of 4 renditions
        renditionsDataCollector.recordRenditionRequest(doclib, "xls");
        renditionsDataCollector.recordRenditionRequest(doclib, "xls");
        renditionsDataCollector.recordRenditionRequest(preview, "docx");
        renditionsDataCollector.recordRenditionRequest(doclib, "docx");
        collectedData = renditionsDataCollector.collectData();
        assertEquals("There should have been 3 data elements", 3, collectedData.size());
        assertHBDataContains("doclib", "xls", "png", 2);
        assertHBDataContains("doclib", "docx", "png", 1);
        assertHBDataContains("preview", "docx", "pdf", 1);
        Date prevTimestamp = collectedData.get(0).getTimestamp();
        Thread.sleep(10);

        // A batch of 3 renditions
        renditionsDataCollector.recordRenditionRequest(doclib, "jpg");
        renditionsDataCollector.recordRenditionRequest(doclib, "jpg");
        renditionsDataCollector.recordRenditionRequest(doclib, "jpg");
        collectedData = renditionsDataCollector.collectData();
        assertEquals("There should have been 1 data element", 1, collectedData.size());
        assertHBDataContains("doclib", "jpg", "png", 3);
        assertNotEquals("The timestamp should have changed", prevTimestamp, collectedData.get(0).getTimestamp());

        // A batch of 0 renditions
        collectedData = renditionsDataCollector.collectData();
        assertEquals("There should have been 0 data elements", 0, collectedData.size());
        
        // A batch of 1 rendition
        renditionsDataCollector.recordRenditionRequest(doclib, "xls");
        collectedData = renditionsDataCollector.collectData();
        assertEquals("There should have been 1 data element", 1, collectedData.size());
        assertHBDataContains("doclib", "xls", "png", 1);
    }

    private boolean assertHBDataContains(String rendition, String sourceMimetype, String targetMimetype, int count)
    {
        boolean found = false;
        for (HBData data : collectedData)
        {
            Map<String, Object> values = data.getData();

            if (rendition.equals(values.get("rendition")) &&
                sourceMimetype.equals(values.get("sourceMimetype")) &&
                targetMimetype.equals(values.get("targetMimetype")) &&
                 count == ((Integer)values.get("count")).intValue())
            {
                found = true;
                break;
            }
        }
        return found;
    }
}
