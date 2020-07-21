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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.deployment.DeploymentMethod;
import org.alfresco.repo.deployment.DeploymentMethodProvider;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.descriptor.DescriptorServiceImpl.BaseDescriptor;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

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
    private BaseDescriptor spyDescriptor;
    private DeploymentMethodProvider mockDeploymentMethodProvider;
    private HeartBeatJobScheduler mockScheduler;
    private DatabaseMetaData mockDatabaseMetaData;
    private ServletContext mockServletContext;

    @Before
    public void setUp() throws SQLException
    {
        spyDescriptor = spy(BaseDescriptor.class);
        mockDescriptorDAO = mock(DescriptorDAO.class);
        mockServerDescriptorDAO = mock(DescriptorDAO.class);
        mockCollectorService = mock(HBDataCollectorService.class);
        mockDeploymentMethodProvider = mock(DeploymentMethodProvider.class);
        mockScheduler = mock(HeartBeatJobScheduler.class);
        mockDatabaseMetaData = mock(DatabaseMetaData.class);
        mockServletContext = mock(ServletContext.class);

        DataSource mockDataSource = mock(DataSource.class);
        Connection mockCon = mock(Connection.class);
        when(mockDataSource.getConnection()).thenReturn(mockCon);
        when(mockCon.getMetaData()).thenReturn(mockDatabaseMetaData);


        when(spyDescriptor.getId()).thenReturn("mock_id");
        when(mockServerDescriptorDAO.getDescriptor()).thenReturn(spyDescriptor);
        when(mockDescriptorDAO.getDescriptor()).thenReturn(spyDescriptor);
        when(mockDeploymentMethodProvider.getDeploymentMethod()).thenReturn(DeploymentMethod.DEFAULT);
        when(mockServletContext.getServerInfo()).thenReturn("Apache Tomcat/7.0.47");

        infoCollector = new InfoDataCollector("acs.repository.info", "1.0", "0 0 0 ? * *", mockScheduler);
        infoCollector.setHbDataCollectorService(mockCollectorService);
        infoCollector.setCurrentRepoDescriptorDAO(mockDescriptorDAO);
        infoCollector.setServerDescriptorDAO(mockServerDescriptorDAO);
        infoCollector.setDeploymentMethodProvider(mockDeploymentMethodProvider);
        infoCollector.setDataSource(mockDataSource);
        infoCollector.setServletContext(mockServletContext);
    }

    @Test
    public void testHBDataFields()
    {
        mockVersionDetails("6","0","0","");
        collectedData = infoCollector.collectData();
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
        mockVersionDetails("5","1","2",".4");
        mockDatabaseMetaData("PostgreSQL","10.1","PostgreSQL JDBC Driver","42.2.5");
        collectedData = infoCollector.collectData();

        HBData repoInfo = grabDataByCollectorId(infoCollector.getCollectorId());
        assertNotNull("Repository info data missing.", repoInfo);

        Map<String, Object> data = repoInfo.getData();
        assertEquals("repository", data.get("repoName"));
        assertEquals(1000, data.get("schema"));
        assertEquals("Community", data.get("edition"));
        assertEquals(DeploymentMethod.DEFAULT.toString(), data.get("deploymentMethod"));
        assertTrue(data.containsKey("version"));
        Map<String, Object> version = (Map<String, Object>) data.get("version");
        assertEquals("5.1.2 (.4 rc08e1b5a-b192)", version.get("full"));
        assertEquals("5.1", version.get("servicePack"));
        assertEquals("5", version.get("major"));
        assertEquals("1", version.get("minor"));
        assertEquals("2", version.get("patch"));
        assertEquals("4", version.get("hotfix"));

        // No need to mock the system properties, just check if they are collected 
        assertNotNull("Check if data is collected", data.get("osVendor") );
        assertNotNull("Check if data is collected", data.get("osVersion") );
        assertNotNull("Check if data is collected", data.get("osArch") );
        assertNotNull("Check if data is collected", data.get("javaVendor") );
        assertNotNull("Check if data is collected", data.get("javaVersion") );
        assertNotNull("Check if data is collected", data.get("userLanguage") );
        assertNotNull("Check if data is collected", data.get("userTimezone") );
        assertNotNull("Check if data is collected", data.get("userUTCOffset") );
        
        assertEquals("Apache Tomcat/7.0.47", data.get("serverInfo") );
                
        assertTrue(data.containsKey("db"));
        Map<String, Object> db = (Map<String, Object>) data.get("db");
        assertEquals("PostgreSQL", db.get("vendor"));
        assertEquals("10.1", db.get("version"));
        assertEquals("PostgreSQL JDBC Driver", db.get("driverName"));
        assertEquals("42.2.5", db.get("driverVersion"));
        
    }
    
    @Test
    public void testInfoDataIsCollectedHotfixNoDot()
    {
        mockVersionDetails("5","1","2","4");
        collectedData = infoCollector.collectData();
        
        HBData repoInfo = grabDataByCollectorId(infoCollector.getCollectorId());
        assertNotNull("Repository info data missing.", repoInfo);

        Map<String, Object> data = repoInfo.getData();
        assertEquals("repository", data.get("repoName"));
        assertEquals(1000, data.get("schema"));
        assertEquals("Community", data.get("edition"));
        assertTrue(data.containsKey("version"));
        Map<String, Object> version = (Map<String, Object>) data.get("version");
        assertEquals("5.1.2 (4 rc08e1b5a-b192)", version.get("full"));
        assertEquals("5.1", version.get("servicePack"));
        assertEquals("5", version.get("major"));
        assertEquals("1", version.get("minor"));
        assertEquals("2", version.get("patch"));
        assertEquals("4", version.get("hotfix"));
    }

    @Test
    public void testInfoDataIsCollectedNoHotfix()
    {
        mockVersionDetails("5","1","2","");
        collectedData = infoCollector.collectData();
        
        HBData repoInfo = grabDataByCollectorId(infoCollector.getCollectorId());
        assertNotNull("Repository info data missing.", repoInfo);

        Map<String, Object> data = repoInfo.getData();
        assertEquals("repository", data.get("repoName"));
        assertEquals(1000, data.get("schema"));
        assertEquals("Community", data.get("edition"));
        assertTrue(data.containsKey("version"));
        Map<String, Object> version = (Map<String, Object>) data.get("version");
        assertEquals("5.1.2 (rc08e1b5a-b192)", version.get("full"));
        assertEquals("5.1", version.get("servicePack"));
        assertEquals("5", version.get("major"));
        assertEquals("1", version.get("minor"));
        assertEquals("2", version.get("patch"));
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

    private void mockVersionDetails(String major, String minor, String patch, String hotfix)
    {
        when(spyDescriptor.getName()).thenReturn("repository");
        when(spyDescriptor.getVersionMajor()).thenReturn(major);
        when(spyDescriptor.getVersionMinor()).thenReturn(minor);
        when(spyDescriptor.getVersionRevision()).thenReturn(patch);
        when(spyDescriptor.getVersionLabel()).thenReturn(hotfix);
        when(spyDescriptor.getSchema()).thenReturn(1000);
        when(spyDescriptor.getEdition()).thenReturn("Community");
        when(spyDescriptor.getVersionBuild()).thenReturn("rc08e1b5a-b192");
    }

    private void mockDatabaseMetaData(String vendor, String version, String driverName, String driverVersion)
    {
        try
        {
            when(mockDatabaseMetaData.getDatabaseProductName()).thenReturn(vendor);
            when(mockDatabaseMetaData.getDatabaseProductVersion()).thenReturn(version);
            when(mockDatabaseMetaData.getDriverName()).thenReturn(driverName);
            when(mockDatabaseMetaData.getDriverVersion()).thenReturn(driverVersion);
        }
        catch (SQLException e)
        {
            // No need to log exception if the data cannot be retrieved
        }
    }
}
