/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.mode.ServerMode;
import org.alfresco.repo.mode.ServerModeProvider;
import org.alfresco.repo.module.ModuleVersionNumber;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.usage.RepoUsageComponent;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.webdav.WebDavService;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.traitextender.SpringExtensionBundle;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mpopa
 */
public class ConfigurationDataCollectorTest
{
    private ConfigurationDataCollector configurationCollector;
    private HBDataCollectorService mockCollectorService;
    private SpringExtensionBundle smartFoldersBundle;
    private DescriptorDAO mockDescriptorDAO;
    private DescriptorDAO mockServerDescriptorDAO;
    private List<HBData> collectedData;
    private HeartBeatJobScheduler mockScheduler;
    private static final ServerMode SERVER_MODE = ServerMode.PRODUCTION;
    private static final String INSTALLED_MODULE_ID_1 = "installedID1";
    private static final String INSTALLED_MODULE_ID_2 = "installedID2";
    private static final ModuleVersionNumber INSTALLED_MODULE_VERSION_1 = new ModuleVersionNumber("1.0");
    private static final ModuleVersionNumber INSTALLED_MODULE_VERSION_2 = new ModuleVersionNumber("2.0");
    private static final String MISSING_MODULE_ID_1 = "missingID1";
    private static final ModuleVersionNumber MISSING_MODULE_VERSION_1 = new ModuleVersionNumber("3.0");
    private static final String AUDIT_APP_NAME = "auditapp1";
    private static final boolean AUDIT_APP_ENABLED = true;

    @Before
    public void setUp()
    {
        smartFoldersBundle = mock(SpringExtensionBundle.class);
        mockDescriptorDAO = mock(DescriptorDAO.class);
        mockServerDescriptorDAO = mock(DescriptorDAO.class);
        mockCollectorService = mock(HBDataCollectorService.class);
        mockScheduler = mock(HeartBeatJobScheduler.class);

        Descriptor mockDescriptor = mock(Descriptor.class);
        when(mockDescriptor.getId()).thenReturn("mock_id");
        when(mockServerDescriptorDAO.getDescriptor()).thenReturn(mockDescriptor);
        when(mockDescriptorDAO.getDescriptor()).thenReturn(mockDescriptor);

        BasicDataSource mockBasicDataSource = mock(BasicDataSource.class);
        RepoUsageComponent mockRepoUsageComponent = mock(RepoUsageComponent.class);
        ServerModeProvider mockServerModeProvider = mock(ServerModeProvider.class);
        when(mockServerModeProvider.getServerMode()).thenReturn(SERVER_MODE);
        ChildApplicationContextFactory mockFileServerSubsystem = mock(ChildApplicationContextFactory.class);
        WebDavService mockWebDavService = mock(WebDavService.class);
        ThumbnailService mockThumbnailService = mock(ThumbnailService.class);
        ChildApplicationContextFactory mockActivitiesFeedSubsystem = mock(ChildApplicationContextFactory.class);
        WorkflowAdminService mockWorkflowAdminService = mock(WorkflowAdminService.class);
        ChildApplicationContextFactory mockInboundSMTPSubsystem = mock(ChildApplicationContextFactory.class);
        ChildApplicationContextFactory mockImapSubsystem = mock(ChildApplicationContextFactory.class);
        ChildApplicationContextFactory mockReplication = mock(ChildApplicationContextFactory.class);

        // mock modules and module service
        ModuleService mockModuleService = mock(ModuleService.class);
        ModuleDetails mockInstalledModule1 = mock(ModuleDetails.class);
        when(mockInstalledModule1.getId()).thenReturn(INSTALLED_MODULE_ID_1);
        when(mockInstalledModule1.getModuleVersionNumber()).thenReturn(INSTALLED_MODULE_VERSION_1);
        ModuleDetails mockInstalledModule2 = mock(ModuleDetails.class);
        when(mockInstalledModule2.getId()).thenReturn(INSTALLED_MODULE_ID_2);
        when(mockInstalledModule2.getModuleVersionNumber()).thenReturn(INSTALLED_MODULE_VERSION_2);
        ModuleDetails mockMissingModule = mock(ModuleDetails.class);
        when(mockMissingModule.getId()).thenReturn(MISSING_MODULE_ID_1);
        when(mockMissingModule.getModuleVersionNumber()).thenReturn(MISSING_MODULE_VERSION_1);
        when(mockModuleService.getAllModules()).thenReturn(Arrays.asList(mockInstalledModule1, mockInstalledModule2));
        when(mockModuleService.getMissingModules()).thenReturn(Arrays.asList(mockMissingModule));

        // mock audit applications and audit service
        AuditService mockAuditService = mock(AuditService.class);
        AuditService.AuditApplication mockAuditApp = mock(AuditService.AuditApplication.class);
        when(mockAuditApp.isEnabled()).thenReturn(AUDIT_APP_ENABLED);
        Map<String, AuditService.AuditApplication> auditApps = new HashMap<>();
        auditApps.put(AUDIT_APP_NAME, mockAuditApp);

        TransactionService mockTransactionService = mock(TransactionService.class);
        RetryingTransactionHelper mockRetryingTransactionHelper = mock(RetryingTransactionHelper.class);
        // Mock transaction service calls
        when(mockRetryingTransactionHelper
                .doInTransaction(any(RetryingTransactionHelper.RetryingTransactionCallback.class), anyBoolean()))
                .thenReturn(true) // First call made by the collector to get the server readOnly value via transformation service
                .thenReturn(auditApps); // Second call to get the audit applications
        when(mockTransactionService.getRetryingTransactionHelper()).thenReturn(mockRetryingTransactionHelper);

        // mock authentication chain
        DefaultChildApplicationContextManager mockAuthenticationSubsystem = mock(DefaultChildApplicationContextManager.class);

        configurationCollector = new ConfigurationDataCollector("acs.repository.configuration", "1.0", "0 0 0 ? * SUN", mockScheduler);
        configurationCollector.setHbDataCollectorService(mockCollectorService);
        configurationCollector.setCurrentRepoDescriptorDAO(mockDescriptorDAO);
        configurationCollector.setSmartFoldersBundle(smartFoldersBundle);

        configurationCollector.setDataSource(mockBasicDataSource);
        configurationCollector.setTransactionService(mockTransactionService);
        configurationCollector.setRepoUsageComponent(mockRepoUsageComponent);
        configurationCollector.setServerModeProvider(mockServerModeProvider);
        configurationCollector.setFileServersSubsystem(mockFileServerSubsystem);
        configurationCollector.setWebdavService(mockWebDavService);
        configurationCollector.setThumbnailService(mockThumbnailService);
        configurationCollector.setActivitiesFeedSubsystem(mockActivitiesFeedSubsystem);
        configurationCollector.setWorkflowAdminService(mockWorkflowAdminService);
        configurationCollector.setInboundSMTPSubsystem(mockInboundSMTPSubsystem);
        configurationCollector.setImapSubsystem(mockImapSubsystem);
        configurationCollector.setReplicationSubsystem(mockReplication);
        configurationCollector.setModuleService(mockModuleService);
        configurationCollector.setAuditService(mockAuditService);
        configurationCollector.setAuthenticationSubsystem(mockAuthenticationSubsystem);

        collectedData = configurationCollector.collectData();
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
            assertNotNull(data.getData());
        }
    }

    @Test
    public void testConfigurationDataIsCollected()
    {
        HBData confData = grabDataByCollectorId(configurationCollector.getCollectorId());
        assertNotNull("Repository configuration data missing.", confData);

        Map<String,Object> data = confData.getData();
        assertTrue(data.containsKey("smartFoldersEnabled"));

        assertTrue(data.containsKey("db"));
        assertTrue(((Map<String, Object>) data.get("db")).containsKey("maxConnections"));
        assertTrue(data.containsKey("serverReadOnly"));
        assertTrue(data.containsKey("serverMode"));
        assertTrue(data.containsKey("ftpEnabled"));
        assertTrue(data.containsKey("webDAVEnabled"));
        assertTrue(data.containsKey("thumbnailsEnabled"));
        assertTrue(data.containsKey("activitiesFeedEnabled"));
        assertTrue(data.containsKey("activitiEngineEnabled"));
        assertTrue(data.containsKey("inboundServerEnabled"));
        assertTrue(data.containsKey("imapEnabled"));
        assertTrue(data.containsKey("authenticationChain"));

        assertTrue(data.containsKey("replication"));
        Map<String, Object> replication = (Map<String, Object>)((Map<String, Object>)data.get("replication"));
        assertTrue(replication.containsKey("enabled"));
        assertTrue(replication.containsKey("readOnly"));

        assertTrue(data.containsKey("module"));
        Map<String, Object> installedModules = (Map<String, Object>)((Map<String, Object>)data.get("module")).get("installed");
        assertTrue(installedModules != null);
        assertEquals(2, installedModules.get("count"));
        Map<String, Object> installedModulesList = (Map<String, Object>) installedModules.get("modules");
        assertTrue(installedModulesList.containsKey(INSTALLED_MODULE_ID_1));
        Map<String, Object> installedModulesInfo = (Map<String, Object>) installedModulesList.get(INSTALLED_MODULE_ID_1);
        assertTrue(installedModulesInfo.containsKey("version"));
        assertEquals(INSTALLED_MODULE_VERSION_1.toString(), installedModulesInfo.get("version"));

        assertTrue(installedModulesList.containsKey(INSTALLED_MODULE_ID_2));
        installedModulesInfo = (Map<String, Object>) installedModulesList.get(INSTALLED_MODULE_ID_2);
        assertTrue(installedModulesInfo.containsKey("version"));
        assertEquals(INSTALLED_MODULE_VERSION_2.toString(), installedModulesInfo.get("version"));

        Map<String, Object> missingModules = (Map<String, Object>)((Map<String, Object>)data.get("module")).get("missing");
        assertTrue(missingModules != null);
        Map<String, Object> missingModulesList = (Map<String, Object>) missingModules.get("modules");
        assertTrue(missingModulesList.containsKey(MISSING_MODULE_ID_1));
        Map<String, Object> missingModulesInfo = (Map<String, Object>) missingModulesList.get(MISSING_MODULE_ID_1);
        assertTrue(missingModulesInfo.containsKey("version"));
        assertEquals(MISSING_MODULE_VERSION_1.toString(), missingModulesInfo.get("version"));

        assertTrue(data.containsKey("audit"));
        assertTrue(((Map<String, Object>) data.get("audit")).containsKey("enabled"));
        Map<String, Object> auditApps = (Map<String, Object>)((Map<String, Object>)data.get("audit")).get("apps");
        assertTrue(auditApps != null);
        assertTrue(auditApps.containsKey(AUDIT_APP_NAME));
        Map<String, Object> auditAppInfo = (Map<String, Object>) auditApps.get(AUDIT_APP_NAME);
        assertEquals(AUDIT_APP_ENABLED, auditAppInfo.get("enabled"));
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
