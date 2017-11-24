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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.util.VersionNumber;
import org.junit.Before;
import org.junit.Test;

/**
 * @author eknizat
 */
public class InfoDataCollectorTest
{

    private InfoDataCollector infoCollector;
    private HBDataCollectorService mockCollectorService;
    private DescriptorDAO mockDescriptorDAO;
    private DescriptorDAO mockServerDescriptorDAO;
    private List<HBData> collectedData;
    private VersionNumber versionNumber;

    @Before
    public void setUp()
    {
        mockDescriptorDAO = mock(DescriptorDAO.class);
        mockServerDescriptorDAO = mock(DescriptorDAO.class);
        mockCollectorService = mock(HBDataCollectorService.class);

        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getId()).thenReturn("mock_id");
        when(mockServerDescriptorDAO.getDescriptor()).thenReturn(mockDescriptor);
        when(mockDescriptorDAO.getDescriptor()).thenReturn(mockDescriptor);

        infoCollector = new InfoDataCollector("acs.repository.info", "1.0", "0 0 0 ? * *");
        infoCollector.setHbDataCollectorService(mockCollectorService);
        infoCollector.setCurrentRepoDescriptorDAO(mockDescriptorDAO);
        infoCollector.setServerDescriptorDAO(mockServerDescriptorDAO);

        versionNumber = new VersionNumber("5.1.2");
        when(mockDescriptor.getName()).thenReturn("repository");
        when(mockDescriptor.getVersion()).thenReturn("5.1.2.4");
        when(mockDescriptor.getVersionNumber()).thenReturn(versionNumber);
        when(mockDescriptor.getVersionMajor()).thenReturn("5");
        when(mockDescriptor.getVersionMinor()).thenReturn("1");
        when(mockDescriptor.getVersionRevision()).thenReturn("2");
        when(mockDescriptor.getVersionLabel()).thenReturn("4");
        when(mockDescriptor.getSchema()).thenReturn(1000);
        when(mockDescriptor.getEdition()).thenReturn("Community");
        collectedData = infoCollector.collectData();
    }

    @Test
    public void testHBDataFields()
    {
        HBData repoInfo = grabDataByCollectorId(infoCollector.getCollectorId());
        assertNotNull("Repository info data missing.", repoInfo);

        for (HBData data : this.collectedData)
        {
            assertNotNull(data.getCollectorId());
            assertNotNull(data.getCollectorVersion());
            assertNotNull(data.getSchemaVersion());
            assertNotNull(data.getSystemId());
            assertNotNull(data.getTimestamp());
            assertNotNull(data.getData());
        }
    }

    @Test
    public void testInfoDataIsCollected()
    {
        HBData repoInfo = grabDataByCollectorId(infoCollector.getCollectorId());
        assertNotNull("Repository info data missing.", repoInfo);

        Map<String, Object> data = repoInfo.getData();
        assertEquals("repository", data.get("repoName"));
        assertEquals(1000, data.get("schema"));
        assertEquals("Community", data.get("edition"));
        assertTrue(data.containsKey("version"));
        Map<String, Object> version = (Map<String, Object>) data.get("version");
        assertEquals("5.1.2.4", (version.get("full")));
        assertEquals("5.1.2", version.get("servicePack"));
        assertEquals("5", version.get("major"));
        assertEquals("1", version.get("minor"));
        assertEquals("2", version.get("patch"));
        assertEquals("4", version.get("hotfix"));
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
