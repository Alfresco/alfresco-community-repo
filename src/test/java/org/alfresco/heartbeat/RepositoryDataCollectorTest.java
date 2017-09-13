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
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.dictionary.CustomModelsInfo;
import org.alfresco.repo.usage.RepoUsageComponent;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author eknizat
 */
public class RepositoryDataCollectorTest
{

    private ApplicationContext context;
    private RepositoryDataCollector repoCollector;
    private List<HBData> collectedData;

    @Before
    public void setUp()
    {
        context = ApplicationContextHelper.getApplicationContext();

        TransactionService transactionService = (TransactionService) context.getBean("transactionService");
        HBDataCollectorService mockCollectorService = mock(HBDataCollectorService.class);
        AuthorityService authorityService = mock(AuthorityService.class);

        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getId()).thenReturn("mock_id");
        DescriptorDAO descriptorDAO = mock(DescriptorDAO.class);
        when(descriptorDAO.getDescriptor()).thenReturn(mockDescriptor);

        RepoUsage mockRepoUsage = mock(RepoUsage.class);
        RepoUsageComponent repoUsageComponent = mock(RepoUsageComponent.class);
        when(repoUsageComponent.getUsage()).thenReturn(mockRepoUsage);

        CustomModelsInfo mockCustomModelsInfo = mock(CustomModelsInfo.class);
        CustomModelService customModelService = mock(CustomModelService.class);
        when(customModelService.getCustomModelsInfo()).thenReturn(mockCustomModelsInfo);

        repoCollector = new RepositoryDataCollector();
        repoCollector.setAuthorityService(authorityService);
        repoCollector.setCurrentRepoDescriptorDAO(descriptorDAO);
        repoCollector.setCustomModelService(customModelService);
        repoCollector.setRepoUsageComponent(repoUsageComponent);
        repoCollector.setServerDescriptorDAO(descriptorDAO);
        repoCollector.setTransactionService(transactionService);
        repoCollector.setHbDataCollectorService(mockCollectorService);
        collectedData = repoCollector.collectData();
    }

    @Test
    public void testHBDataFields()
    {
        for(HBData data : this.collectedData)
        {
            System.out.println(data.getCollectorId());
            assertNotNull(data.getCollectorId());
            assertNotNull(data.getCollectorVersion());
            assertNotNull(data.getSchemaVersion());
            assertNotNull(data.getSystemId());
            assertNotNull(data.getTimestamp());
        }

    }

    @Test
    public void testInfoDataIsCollected()
    {
        HBData repoInfo = grabDataByCollectorId("acs.repository.info");
        assertNotNull("Repository info data missing.", repoInfo);

        Map<String,Object> data = repoInfo.getData();
        assertTrue(data.containsKey("repoName"));
        assertTrue(data.containsKey("edition"));
        assertTrue(data.containsKey("versionMajor"));
        assertTrue(data.containsKey("versionMinor"));
        assertTrue(data.containsKey("schema"));
    }

    @Test
    public void testSystemUsageDataIsCollected()
    {
        HBData systemUsage = grabDataByCollectorId("acs.repository.usage.system");
        assertNotNull("Repository usage data missing.", systemUsage);

        Map<String,Object> data = systemUsage.getData();
        assertTrue(data.containsKey("memFree"));
        assertTrue(data.containsKey("memMax"));
        assertTrue(data.containsKey("memTotal"));
    }

    @Test
    public void testModelUsageDataIsCollected()
    {
        HBData modelUsage = grabDataByCollectorId("acs.repository.usage.model");
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
