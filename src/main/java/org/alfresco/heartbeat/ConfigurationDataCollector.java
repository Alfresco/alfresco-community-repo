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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.management.subsystems.DefaultChildApplicationContextManager;
import org.alfresco.repo.mode.ServerModeProvider;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.usage.RepoUsageComponent;
import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.module.ModuleDetails;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.webdav.WebDavService;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.traitextender.SpringExtensionBundle;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import javax.sql.DataSource;

/**
 * A collector of data related to repository configuration data for HeartBeat.
 * <ul>
 *  <li>Collector ID: <b>acs.repository.configuration</b></li>
 *  <li>Data:
 *      <ul>
 *          <li><b>smartFoldersEnabled:</b> Boolean - Smart folder is registered or not. {@link SpringExtensionBundle#isEnabled()}</li>
 *          <li><b>serverReadOnly:</b> Boolean - Repository server read only mode. {@link RepoUsage#isReadOnly()}</li>
 *          <li><b>serverMode:</b> String - The server mode. {@link ServerModeProvider#getServerMode()}</li>
 *          <li><b>ftpEnabled:</b> Boolean - FTP enabled state as reported by the <code>ftp.enabled</code> property
 *          of the fileServers subsystem {@link ChildApplicationContextFactory#getProperty(String)}</li>
 *          <li><b>webDAVEnabled:</b> Boolean - WebDAV enabled state. {@link WebDavService#getEnabled()}</li>
 *          <li><b>thumbnailsEnabled:</b> Boolean - Thumbnails enabled state. {@link ThumbnailService#getThumbnailsEnabled()}</li>
 *          <li><b>activitiesFeedEnabled:</b> Boolean - Activities feed enabled state as reported by the <code>activities.feed.notifier.enabled</code> property
 *          of the ActivitiesFeed subsystem {@link ChildApplicationContextFactory#getProperty(String)}</li>
 *          <li><b>activitiEngineEnabled:</b> Boolean - Activiti engine enabled state for engine id:
 *          {@link ActivitiConstants#ENGINE_ID} as reported by {@link WorkflowAdminService#isEngineEnabled(String)}</li>
 *          <li><b>inboundServerEnabled:</b> Boolean - Inbound email server enabled state.
 *          The state is calculated as logical AND of the properties <code>email.server.enabled</code> AND <code>email.inbound.enabled</code>
 *          as reported by the InboundSMTP subsystem {@link ChildApplicationContextFactory#getProperty(String)}</li>
 *          <li><b>imapEnabled:</b> Boolean - Imap enabled state as reported by the <code>imap.server.enabled</code> property
 *          of the imap subsystem {@link ChildApplicationContextFactory#getProperty(String)}</li>
 *          <li><b>replication:</b> Replication configuration.
 *              <ul>
 *                  <li>enabled: Boolean - Replication enabled state as reported by the <code>replication.enabled</code> property
 *          of the Replication subsystem {@link ChildApplicationContextFactory#getProperty(String)}</li>
 *                  <li>readOnly: Boolean - Replication transfer readonly as reported by the <code>replication.transfer.readonly</code> property
 *          of the Replication subsystem {@link ChildApplicationContextFactory#getProperty(String)}</li>
 *              </ul>
 *          </li>
 *          <li><b>db:</b> Database configuration
 *              <ul>
 *                  <li>maxConnections: int - The maximum number of active connections. {@link BasicDataSource#getMaxActive()}</li>
 *              </ul>
 *          </li>
 *          <li><b>authentication</b>: Authentication configuration.
 *              <ul>
 *                  <li><b>chain:</b> String - The authentication chain as reported by <code>chain</code> property
 *                  {@link DefaultChildApplicationContextManager#getProperty(String)}
 *                  </li>
 *              </ul>
 *          </li>
 *          <li><b>module:</b> Module configuration.
 *              <ul>
 *                <li><b>installed:</b> Information about the installed modules {@link ModuleService#getAllModules()}
 *                    <ul>
 *                        <li><b>count</b> int - The number of installed modules.</li>
 *                        <li><b>modules:</b> - List of installed modules.
 *                            <ul>
 *                                <li> <b>{@link ModuleDetails#getId()}</b>
 *                                    <ul>
 *                                        <li><b>version</b> - module version {@link ModuleDetails#getModuleVersionNumber()}</li>
 *                                    </ul>
 *                                </li>
 *                                ...
 *                            </ul>
 *                        </li>
 *                    </ul>
 *                </li>
 *
 *                <li><b>missing:</b> Information about the missing modules, omitted if no missing modules, {@link ModuleService#getMissingModules()}
 *                    <ul>
 *                        <li><b>modules:</b> - List of missing modules.
 *                            <ul>
 *                                <li> <b>{@link ModuleDetails#getId()}</b>
 *                                    <ul>
 *                                        <li><b>version</b> String - module version {@link ModuleDetails#getModuleVersionNumber()}</li>
 *                                    </ul>
 *                                </li>
 *                                ...
 *                            </ul>
 *                        </li>
 *                    </ul>
 *                </li>
 *              </ul>
 *          </li>
 *          <li><b>audit</b>: Audit applications configuration.
 *              <ul>
 *                  <li><b>enabled</b> boolean - The audit enabled state {@link AuditService#isAuditEnabled()}</li>
 *                  <li><b>apps:</b> List of audit applications. {@link AuditService#getAuditApplications()}
 *                      <ul>
 *                          <li> <b>map keys from {@link AuditService#getAuditApplications()}</b> Note that spaces are replaces with hyphens
 *                              <ul>
 *                                  <li><b>enabled</b> - Enabled state of this audit application. {@link AuditService.AuditApplication#isEnabled()}</li>
 *                              </ul>
 *                          </li>
 *                          ...
 *                      </ul>
 *                  </li>
 *              </ul>
 *          </li>
 *      </ul>
 *  </li>
 * </ul>

 * @author mpopa
 */
public class ConfigurationDataCollector extends HBBaseDataCollector implements InitializingBean
{
    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    /** The logger. */
    private static final Log logger = LogFactory.getLog(ConfigurationDataCollector.class);

    private SpringExtensionBundle smartFoldersBundle;
    private DataSource dataSource;
    private RepoUsageComponent repoUsageComponent;
    private ModuleService moduleService;
    private AuditService auditService;
    private TransactionService transactionService;
    private ThumbnailService thumbnailService;
    private WebDavService webdavService;
    private WorkflowAdminService workflowAdminService;
    private ChildApplicationContextFactory replicationSubsystem;
    private ChildApplicationContextFactory imapSubsystem;
    private ChildApplicationContextFactory inboundSMTPSubsystem;
    private ChildApplicationContextFactory activitiesFeedSubsystem;
    private ChildApplicationContextFactory fileServersSubsystem;
    private DefaultChildApplicationContextManager authenticationSubsystem;
    private ServerModeProvider serverModeProvider;

    public ConfigurationDataCollector(String collectorId, String collectorVersion, String cronExpression,
                                      HeartBeatJobScheduler hbJobScheduler)
    {
        super(collectorId, collectorVersion, cronExpression, hbJobScheduler);
    }

    public void setReplicationSubsystem(ChildApplicationContextFactory replicationSubsystem)
    {
        this.replicationSubsystem = replicationSubsystem;
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    public void setSmartFoldersBundle(SpringExtensionBundle smartFoldersBundle)
    {
        this.smartFoldersBundle = smartFoldersBundle;
    }

    public void setAuthenticationSubsystem(DefaultChildApplicationContextManager authenticationSubsystem)
    {
        this.authenticationSubsystem = authenticationSubsystem;
    }

    public void setFileServersSubsystem(ChildApplicationContextFactory fileServersSubsystem)
    {
        this.fileServersSubsystem = fileServersSubsystem;
    }

    public void setActivitiesFeedSubsystem(ChildApplicationContextFactory activitiesFeedSubsystem)
    {
        this.activitiesFeedSubsystem = activitiesFeedSubsystem;
    }

    public void setInboundSMTPSubsystem(ChildApplicationContextFactory inboundSMTPSubsystem)
    {
        this.inboundSMTPSubsystem = inboundSMTPSubsystem;
    }

    public void setImapSubsystem(ChildApplicationContextFactory imapSubsystem)
    {
        this.imapSubsystem = imapSubsystem;
    }


    public void setWorkflowAdminService(WorkflowAdminService workflowAdminService)
    {
        this.workflowAdminService = workflowAdminService;
    }

    public void setWebdavService(WebDavService webdavService)
    {
        this.webdavService = webdavService;
    }

    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }

    public void setServerModeProvider(ServerModeProvider serverModeProvider)
    {
        this.serverModeProvider = serverModeProvider;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setRepoUsageComponent(RepoUsageComponent repoUsageComponent)
    {
        this.repoUsageComponent = repoUsageComponent;
    }

    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }

    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "currentRepoDescriptorDAO", currentRepoDescriptorDAO);
        PropertyCheck.mandatory(this, "smartFoldersBundle", smartFoldersBundle);
        PropertyCheck.mandatory(this, "dataSource", dataSource);
        PropertyCheck.mandatory(this, "repoUsageComponent", repoUsageComponent);
        PropertyCheck.mandatory(this, "moduleService", moduleService);
        PropertyCheck.mandatory(this, "auditService", auditService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "thumbnailService", thumbnailService);
        PropertyCheck.mandatory(this, "webdavService", webdavService);
        PropertyCheck.mandatory(this, "workflowAdminService", workflowAdminService);
        PropertyCheck.mandatory(this, "replicationSubsystem", replicationSubsystem);
        PropertyCheck.mandatory(this, "imapSubsystem", imapSubsystem);
        PropertyCheck.mandatory(this, "inboundSMTPSubsystem", inboundSMTPSubsystem);
        PropertyCheck.mandatory(this, "serverModeProvider", serverModeProvider);
        PropertyCheck.mandatory(this, "activitiesFeedSubsystem", activitiesFeedSubsystem);
        PropertyCheck.mandatory(this, "fileServersSubsystem", fileServersSubsystem);
        PropertyCheck.mandatory(this, "authenticationSubsystem", authenticationSubsystem);
    }

    @Override
    public List<HBData> collectData()
    {
        // Collect repository configuration data
        logger.debug("Preparing repository configuration data...");
        Map<String, Object> configurationValues = new HashMap<>();
        configurationValues.put("smartFoldersEnabled", smartFoldersBundle.isEnabled());
        boolean readOnly = transactionService.getRetryingTransactionHelper().doInTransaction(
                () -> repoUsageComponent.getUsage().isReadOnly(), true);
        configurationValues.put("serverReadOnly", readOnly);
        configurationValues.put("serverMode", serverModeProvider.getServerMode().toString());
        boolean ftpEnabled = Boolean.valueOf(fileServersSubsystem.getProperty("ftp.enabled"));
        configurationValues.put("ftpEnabled", ftpEnabled);
        configurationValues.put("webDAVEnabled", webdavService.getEnabled());
        configurationValues.put("thumbnailsEnabled", thumbnailService.getThumbnailsEnabled());
        boolean activitiesFeedEnabled = Boolean.valueOf(activitiesFeedSubsystem.getProperty("activities.feed.notifier.enabled"));
        configurationValues.put("activitiesFeedEnabled", activitiesFeedEnabled);
        configurationValues.put("activitiEngineEnabled", workflowAdminService.isEngineEnabled(ActivitiConstants.ENGINE_ID));
        boolean inboundEnabled = Boolean.valueOf(inboundSMTPSubsystem.getProperty("email.inbound.enabled"));
        boolean emailServerEnabled = Boolean.valueOf(inboundSMTPSubsystem.getProperty("email.server.enabled"));
        boolean inboundServerEnabled = inboundEnabled && emailServerEnabled;
        configurationValues.put("inboundServerEnabled", inboundServerEnabled);
        boolean imapEnabled = Boolean.valueOf(imapSubsystem.getProperty("imap.server.enabled"));
        configurationValues.put("imapEnabled", imapEnabled);

        Map<String, Object> replicationInfo = new HashMap<>();
        replicationInfo.put("enabled", replicationSubsystem.getProperty("replication.enabled"));
        replicationInfo.put("readOnly", replicationSubsystem.getProperty("replication.transfer.readonly"));
        configurationValues.put("replication", replicationInfo);

        if (dataSource instanceof BasicDataSource)
        {
            Map<String, Object> db = new HashMap<>();
            db.put("maxConnections", ((BasicDataSource) dataSource).getMaxActive());
            configurationValues.put("db", db);
        }

        // Modules information
        List<ModuleDetails> rawInstalledModules = moduleService.getAllModules();
        Map<String, Object> modules = new HashMap<>();
        Map<String, Object> installedModules = new HashMap<>();
        installedModules.put("count", rawInstalledModules.size());
        Map<String, Object> installedModulesList = new HashMap<>();
        for (ModuleDetails md : rawInstalledModules)
        {
            Map<String, Object> moduleInfo = new HashMap<>();
            moduleInfo.put("version", md.getModuleVersionNumber().toString());
            installedModulesList.put(md.getId(), moduleInfo);
        }
        if (!installedModulesList.isEmpty())
        {
            installedModules.put("modules", installedModulesList);
        }
        modules.put("installed", installedModules);

        // Missing modules information
        List<ModuleDetails> rawMissingModules = getMissingModules();
        Map<String, Object> missingModules = new HashMap<>();
        Map<String, Object> missingModulesList = new HashMap<>();
        for (ModuleDetails md : rawMissingModules)
        {
            Map<String, Object> moduleInfo = new HashMap<>();
            moduleInfo.put("version", md.getModuleVersionNumber().toString());
            missingModulesList.put(md.getId(), moduleInfo);
        }
        if (!missingModulesList.isEmpty())
        {
            missingModules.put("modules", missingModulesList);
            modules.put("missing", missingModules);
        }
        configurationValues.put("module", modules);

        // Audit information
        Map<String, Object> audit = new HashMap<>();
        audit.put("enabled",auditService.isAuditEnabled());
        Map<String, Object> auditAppList = new HashMap<>();
        Map<String, AuditService.AuditApplication> rawAppList = transactionService.getRetryingTransactionHelper()
                .doInTransaction( () -> auditService.getAuditApplications(), true);

        for (Map.Entry<String, AuditService.AuditApplication> entry : rawAppList.entrySet())
        {
            AuditService.AuditApplication app = entry.getValue();
            Map<String, Object> appInfo = new HashMap<>();
            appInfo.put("enabled", app.isEnabled());
            // replace spaces with hyphens
            String appName = entry.getKey().replace(" ","-");
            auditAppList.put(appName, appInfo);
        }
        if (!auditAppList.isEmpty())
        {
            audit.put("apps", auditAppList);
        }
        configurationValues.put("audit", audit);

        // Authentication chain
        String chainString = authenticationSubsystem.getProperty("chain");
        configurationValues.put("authenticationChain", chainString);

        HBData configurationData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                this.getCollectorId(),
                this.getCollectorVersion(),
                new Date(),
                configurationValues);
        return Arrays.asList(configurationData);
    }

    private List<ModuleDetails> getMissingModules()
    {
        AuthenticationUtil.RunAsWork<List<ModuleDetails>> missingModulesWork = () ->
        {
            try
            {
                return moduleService.getMissingModules();
            }
            catch (Throwable e)
            {
                logger.warn("Heartbeat failed to collect information about missing modules: " + e);
                return Collections.emptyList();
            }
        };
        return AuthenticationUtil.runAs(missingModulesWork, AuthenticationUtil.getSystemUserName());
    }
}
