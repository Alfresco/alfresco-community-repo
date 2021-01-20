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
package org.alfresco.repo.tenant;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.transaction.UserTransaction;

import net.sf.acegisecurity.providers.encoding.PasswordEncoder;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.ContentStoreCaps;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.domain.tenant.TenantAdminDAO;
import org.alfresco.repo.domain.tenant.TenantEntity;
import org.alfresco.repo.domain.tenant.TenantUpdateEntity;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.ShaPasswordEncoderImpl;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.usage.UserUsageTrackingComponent;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.RepositoryExporterService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * MT Admin Service Implementation.
 * 
 */
public class MultiTAdminServiceImpl implements TenantAdminService, ApplicationContextAware, InitializingBean
{
    // Logger
    private static Log logger = LogFactory.getLog(MultiTAdminServiceImpl.class);
    
    // Keep hold of the app context
    protected ApplicationContext ctx;
    
    // Dependencies    
    private NodeService nodeService;
    private RepoAdminService repoAdminService;
    private AuthenticationContext authenticationContext;
    private MultiTServiceImpl tenantService;
    private BehaviourFilter behaviourFilter;
    
    protected TransactionService transactionService;
    protected DictionaryComponent dictionaryComponent;
    protected TenantAdminDAO tenantAdminDAO;
    protected PasswordEncoder passwordEncoder;
    protected ContentStore tenantFileContentStore;
    
    private ThumbnailRegistry thumbnailRegistry;
    private String contentRootContainerPath = null;
    
    private WorkflowService workflowService;
    private RepositoryExporterService repositoryExporterService;
    private ModuleService moduleService;
    private List<WorkflowDeployer> workflowDeployers = new ArrayList<WorkflowDeployer>();
    
    private String baseAdminUsername = null; 

    // Experimental: Thor
    private TenantRoutingDataSource trds;

    /*
     * Tenant domain/ids are unique strings that are case-insensitive. Tenant ids must be valid filenames. 
     * They may also map onto domains and hence should allow valid FQDN.
     *
     *       The following PCRE-style
     *       regex defines a valid label within a FQDN:
     *
     *          ^[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]$
     *
     *       Less formally:
     *
     *          o  Case insensitive
     *          o  First/last character:  alphanumeric
     *          o  Interior characters:   alphanumeric plus hyphen
     *          o  Minimum length:        2  characters
     *          o  Maximum length:        63 characters
     *
     *       The FQDN (fully qualified domain name) has the following constraints:
     *
     *          o  Maximum 255 characters (***)
     *          o  Must contain at least one alpha
     *          
     *  Note: (***) Due to various internal restrictions (such as store identifier) we restrict tenant ids to 75 characters.
     */

    protected final static String REGEX_VALID_DNS_LABEL = "^[a-zA-Z0-9][a-zA-Z0-9-]{0,61}[a-zA-Z0-9]$";
    
    protected final static String REGEX_CONTAINS_ALPHA = "^(.*)[a-zA-Z](.*)$";
    
    protected final static int MAX_LEN = 75;
    	
    public void setNodeService(DbNodeServiceImpl dbNodeService)
    {
        this.nodeService = dbNodeService;
    }
    
    public void setDictionaryComponent(DictionaryComponent dictionaryComponent)
    {
        this.dictionaryComponent = dictionaryComponent;
    }
    
    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }
    
    public void setAuthenticationContext(AuthenticationContext authenticationContext)
    {
        this.authenticationContext = authenticationContext;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
  
    public void setTenantService(MultiTServiceImpl tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setTenantAdminDAO(TenantAdminDAO tenantAdminDAO)
    {
        this.tenantAdminDAO = tenantAdminDAO;
    }
    
    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }
    
    public void setTenantFileContentStore(ContentStore tenantFileContentStore)
    {
        this.tenantFileContentStore = tenantFileContentStore;
    }
    
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    public void setRepositoryExporterService(RepositoryExporterService repositoryExporterService)
    {
        this.repositoryExporterService = repositoryExporterService;
    }
    
    /**
     * @deprecated see setWorkflowDeployers
     */
    public void setWorkflowDeployer(WorkflowDeployer workflowDeployer)
    {
        // NOOP
        logger.warn(WARN_MSG);
    }
    
    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }
    
    public void setThumbnailRegistry(ThumbnailRegistry thumbnailRegistry)
    {
        this.thumbnailRegistry = thumbnailRegistry;
    }
    
    public void setBaseAdminUsername(String baseAdminUsername)
    {
        this.baseAdminUsername = baseAdminUsername;
    }
    
    public void setTenantRoutingDataSource(TenantRoutingDataSource trds)
    {
        this.trds = trds;
    }
    
    // if set then tenant are not co-mingled and all content roots will appear below this container (in <tenantdomain> sub-folder)
    public void setContentRootContainerPath(String contentRootContainerPath)
    {
        this.contentRootContainerPath = contentRootContainerPath;
    }
    
    public static final String PROTOCOL_STORE_USER = "user";
    public static final String PROTOCOL_STORE_WORKSPACE = "workspace";
    public static final String PROTOCOL_STORE_SYSTEM = "system";
    public static final String PROTOCOL_STORE_ARCHIVE = "archive"; 
    public static final String STORE_BASE_ID_USER = "alfrescoUserStore";
    public static final String STORE_BASE_ID_SYSTEM = "system";
    public static final String STORE_BASE_ID_VERSION1 = "lightWeightVersionStore"; // deprecated
    public static final String STORE_BASE_ID_VERSION2 = "version2Store";
    public static final String STORE_BASE_ID_SPACES = "SpacesStore";
    
    public static final String TENANTS_ATTRIBUTE_PATH = "alfresco-tenants";
    public static final String TENANT_ATTRIBUTE_ENABLED = "enabled";
    public static final String TENANT_ATTRIBUTE_ROOT_CONTENT_STORE_DIR = "rootContentStoreDir";
    public static final String TENANT_ATTRIBUTE_DB_URL = "dbUrl"; // if not co-mingled
    
    private List<TenantDeployer> tenantDeployers = new ArrayList<TenantDeployer>();
    
    private static final String WARN_MSG = "system.mt.warn.upgrade_mt_admin_context";
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        // for upgrade/backwards compatibility with 3.0.x (mt-admin-context.xml)
        if (baseAdminUsername == null)
        {
            logger.warn(I18NUtil.getMessage(WARN_MSG));
        }
        
        PropertyCheck.mandatory(this, "NodeService", nodeService);
        PropertyCheck.mandatory(this, "DictionaryComponent", dictionaryComponent);
        PropertyCheck.mandatory(this, "RepoAdminService", repoAdminService);
        PropertyCheck.mandatory(this, "TransactionService", transactionService);
        PropertyCheck.mandatory(this, "TenantService", tenantService);
        PropertyCheck.mandatory(this, "TenantAdminDAO", tenantAdminDAO);
        PropertyCheck.mandatory(this, "PasswordEncoder", passwordEncoder);
        PropertyCheck.mandatory(this, "TenantFileContentStore", tenantFileContentStore);
        PropertyCheck.mandatory(this, "WorkflowService", workflowService);
        PropertyCheck.mandatory(this, "RepositoryExporterService", repositoryExporterService);
        PropertyCheck.mandatory(this, "moduleService", moduleService);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.ctx = applicationContext;
    }

    @Override
    public void startTenants()
    {
        AuthenticationUtil.setMtEnabled(true);
        
        // initialise the tenant admin service and status of tenants (using attribute service)
        // note: this requires that the repository schema has already been initialised
        
        // register dictionary - to allow enable/disable tenant callbacks
        register(dictionaryComponent);
        
        if (isTenantDeployer(tenantFileContentStore))
        {
            // register file store - to allow enable/disable tenant callbacks
            // note: tenantFileContentStore must be registed before dictionaryRepositoryBootstrap
            register(tenantDeployer(tenantFileContentStore), 0);
        }
        
        UserTransaction userTransaction = transactionService.getUserTransaction();
        
        try
        {
            authenticationContext.setSystemUserAsCurrentUser();
            userTransaction.begin();
            
            // bootstrap Tenant Service internal cache
            List<Tenant> tenants = getAllTenants();
            
            int enabledCount = 0;
            int disabledCount = 0;
            
            for (Tenant tenant : tenants)
            {
                if ((! (isTenantRoutingContentStore(tenantFileContentStore))) && (! tenantFileContentStore.getRootLocation().equals(tenant.getRootContentStoreDir())))
                {
                    // eg. ALF-14121 - MT will not work with replicating-content-services-context.sample if tenants are not co-mingled
                    throw new AlfrescoRuntimeException("MT: cannot start tenants - TenantRoutingContentStore is not configured AND not all tenants use co-mingled content store");
                }
                
                String tenantDomain = tenant.getTenantDomain();
                
                if (tenant.isEnabled())
                {
                    // notify tenant deployers registered so far ...
                    notifyAfterEnableTenant(tenantDomain);
                    enabledCount++;
                }
                else
                {
                    disabledCount++;
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Tenant disabled: " + tenantDomain);
                    }
                }
            }
            
            userTransaction.commit();
            
            if ((enabledCount+disabledCount) == 0)
            {
                AuthenticationUtil.setMtEnabled(false); // explicitly disable if there are no tenants
            }
            
            if (logger.isInfoEnabled() && ((enabledCount+disabledCount) > 0))
            {
                logger.info(String.format("Alfresco Multi-Tenant startup - %d enabled tenants, %d disabled tenants",
                                          enabledCount, disabledCount));
            }
            else if (logger.isDebugEnabled())
            {
                logger.debug(String.format("Alfresco Multi-Tenant startup - %d enabled tenants, %d disabled tenants",
                             enabledCount, disabledCount));
            }
        }
        catch(Throwable e)
        {
            // rollback the transaction
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("Failed to bootstrap tenants", e);
        }
        finally
        {
            authenticationContext.clearCurrentSecurityContext();
        }
    }
    
    @Override
    public void stopTenants()
    {
        tenantDeployers.clear();
        tenantDeployers = null;
        AuthenticationUtil.setMtEnabled(false);
    }

    @Override
    public void createTenant(final String tenantDomain, final char[] tenantAdminRawPassword)
    {  
        createTenant(tenantDomain, tenantAdminRawPassword, null);
    }
    
    @Override
    public void createTenant(final String tenantDomain, final char[] tenantAdminRawPassword, String contentRoot)
    {
        createTenant(tenantDomain, tenantAdminRawPassword, contentRoot, null);
    }
    
    @Override
    public void createTenant(final String tenantDomainIn, final char[] tenantAdminRawPassword, String contentRootPath, final String dbUrl)
    {
        ParameterCheck.mandatory("tenantAdminRawPassword", tenantAdminRawPassword);
        
        final String tenantDomain = getTenantDomain(tenantDomainIn);

        AuthenticationUtil.setMtEnabled(true); // in case this is the 1st tenant
        
        long start = System.currentTimeMillis();
        
        if ((contentRootContainerPath != null) && (! contentRootContainerPath.isEmpty()))
        {
            String defaultContentRoot = null;
            
            if (! contentRootContainerPath.endsWith("/"))
            {
                defaultContentRoot = contentRootContainerPath + "/" + tenantDomain;
            }
            else
            {
                defaultContentRoot = contentRootContainerPath + tenantDomain;
            }
            
            if ((contentRootPath != null) && (! contentRootPath.isEmpty()))
            {
                logger.warn("Use default content root path: "+defaultContentRoot+" (ignoring: "+contentRootPath+")");
            }
            
            contentRootPath = defaultContentRoot;
        }

        initTenant(tenantDomain, contentRootPath, dbUrl);
        
        if ((dbUrl != null) && (trds != null))
        {
            try
            {
                // note: experimental - currently assumes a bootstrapped DB schema exists for this dbUrl !
                trds.addTenantDataSource(tenantDomain, dbUrl);
            }
            catch (SQLException se)
            {
                throw new AlfrescoRuntimeException("Failed to create tenant '"+tenantDomain+"' for dbUrl '"+dbUrl+"'", se);
            }
        }
        
        try
        {
            // note: runAs would cause auditable property "creator" to be "admin" instead of "System@xxx"
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(getSystemUser(tenantDomain));
            
            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
            {
                public Object doWork()
                {
                    dictionaryComponent.init();
                    
                    if (isTenantDeployer(tenantFileContentStore))
                    {
                        TenantDeployer deployer = tenantDeployer(tenantFileContentStore);
                        deployer.init();
                    }
                    
                    // callback
                    RetryingTransactionCallback<Object> doImportCallback = new RetryingTransactionCallback<Object>()
                    {
                        public Object execute() throws Throwable
                        {
                            // create tenant-specific stores
                            ImporterBootstrap userImporterBootstrap = (ImporterBootstrap)ctx.getBean("userBootstrap-mt");
                            bootstrapUserTenantStore(userImporterBootstrap, tenantDomain, tenantAdminRawPassword);
                            
                            ImporterBootstrap systemImporterBootstrap = (ImporterBootstrap)ctx.getBean("systemBootstrap-mt");
                            bootstrapSystemTenantStore(systemImporterBootstrap, tenantDomain);
                            
                            // deprecated
                            ImporterBootstrap versionImporterBootstrap = (ImporterBootstrap)ctx.getBean("versionBootstrap-mt");
                            bootstrapVersionTenantStore(versionImporterBootstrap, tenantDomain);
                            
                            ImporterBootstrap version2ImporterBootstrap = (ImporterBootstrap)ctx.getBean("version2Bootstrap-mt");
                            bootstrapVersionTenantStore(version2ImporterBootstrap, tenantDomain);
                            
                            ImporterBootstrap spacesArchiveImporterBootstrap = (ImporterBootstrap)ctx.getBean("spacesArchiveBootstrap-mt");
                            bootstrapSpacesArchiveTenantStore(spacesArchiveImporterBootstrap, tenantDomain);
                            
                            ImporterBootstrap spacesImporterBootstrap = (ImporterBootstrap)ctx.getBean("spacesBootstrap-mt");
                            bootstrapSpacesTenantStore(spacesImporterBootstrap, tenantDomain);

                            thumbnailRegistry.initThumbnailDefinitions();
                    
                            // TODO janv - resolve this conflict later
                            /* Note: assume for now that all tenant deployers can lazily init
                            
                            // notify listeners that tenant has been created & hence enabled
                            for (TenantDeployer tenantDeployer : tenantDeployers)
                            {
                                tenantDeployer.onEnableTenant();
                            }
                            */
                            
                            // bootstrap workflows
                            for (WorkflowDeployer workflowDeployer : workflowDeployers)
                            {
                                workflowDeployer.init();
                            }                            
                            
                            // bootstrap modules (if any)
                            moduleService.startModules();
                            
                            return null;
                        }
                    };
                    
                    // if not default DB (ie. dbUrl != null) then run in new Spring managed txn (to ensure datasource is switched)
                    transactionService.getRetryingTransactionHelper().doInTransaction(doImportCallback, transactionService.isReadOnly(), (dbUrl != null));
                    return null;
                }
            }, tenantDomain);
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        
        if (logger.isInfoEnabled())
        {
            logger.info("Tenant created: " + tenantDomain + " in "+(System.currentTimeMillis()-start)+ " ms");
        }
    }
    
    /**
     * Export tenant - equivalent to the tenant admin running a 'complete repo' export from the Web Client Admin
     */
    @Override
    public void exportTenant(String tenantDomainIn, final File directoryDestination)
    {
        final String tenantDomain = getTenantDomain(tenantDomainIn);
        
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork()
            {
                repositoryExporterService.export(directoryDestination, tenantDomain);
                return null;
            }
        }, tenantDomain);
        
        if (logger.isInfoEnabled())
        {
            logger.info("Tenant exported: " + tenantDomain);
        }
    }
    
    /**
     * Create tenant by restoring from a complete repository export. This is equivalent to a bootstrap import using restore-context.xml.
     */
    @Override
    public void importTenant(final String tenantDomainIn, final File directorySource, String contentRoot)
    {
        final String tenantDomain = getTenantDomain(tenantDomainIn);
        
        AuthenticationUtil.setMtEnabled(true); // in case this is the 1st tenant
        
        initTenant(tenantDomain, contentRoot, null);
        
        try
        {
            // note: runAs would cause auditable property "creator" to be "admin" instead of "System@xxx"
            AuthenticationUtil.pushAuthentication();
            AuthenticationUtil.setFullyAuthenticatedUser(getSystemUser(tenantDomain));
            
            dictionaryComponent.init();
            
            if (isTenantDeployer(tenantFileContentStore))
            {
                TenantDeployer deployer = tenantDeployer(tenantFileContentStore);
                deployer.init();
            }
            
            // import tenant-specific stores
            importBootstrapUserTenantStore(tenantDomain, directorySource);
            importBootstrapSystemTenantStore(tenantDomain, directorySource);
            importBootstrapVersionTenantStore(tenantDomain, directorySource);
            importBootstrapSpacesArchiveTenantStore(tenantDomain, directorySource);
            importBootstrapSpacesModelsTenantStore(tenantDomain, directorySource);
            importBootstrapSpacesTenantStore(tenantDomain, directorySource);
            
            thumbnailRegistry.initThumbnailDefinitions();
            
            // notify listeners that tenant has been created & hence enabled
            for (TenantDeployer tenantDeployer : tenantDeployers)
            {
                tenantDeployer.onEnableTenant();
            }
            
            // bootstrap workflows, if needed
            if(workflowService.isMultiTenantWorkflowDeploymentEnabled()) 
            {
            	for (WorkflowDeployer workflowDeployer : workflowDeployers)
            	{
            		workflowDeployer.init();
            	}
            }
            
            // bootstrap modules (if any)
            moduleService.startModules();
        }
        finally
        {
            AuthenticationUtil.popAuthentication();
        }
        
        if (logger.isInfoEnabled())
        {
            logger.info("Tenant imported: " + tenantDomain);
        }
    }
    
    @Override
    public boolean existsTenant(String tenantDomain)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        
        tenantDomain = getTenantDomain(tenantDomain);
        
        return (getTenantAttributes(tenantDomain) != null);
    }
    
    private Tenant getTenantAttributes(String tenantDomain)
    {
        TenantEntity tenantEntity = tenantAdminDAO.getTenant(tenantDomain);
        if (tenantEntity == null)
        {
            return null;
        }
        else
        {
            Tenant tenant = new Tenant(tenantEntity.getTenantDomain(), tenantEntity.getEnabled(), tenantEntity.getContentRoot(), null);
            return tenant;
        }
    }
    
    @Override
    public void enableTenant(String tenantDomain)
    { 
        tenantDomain = getTenantDomain(tenantDomain);
        
        if (! existsTenant(tenantDomain))
        {
            throw new AuthenticationException("Tenant does not exist: " + tenantDomain);
        }
        
        if (isEnabledTenant(tenantDomain))
        {
            logger.warn("Tenant already enabled: " + tenantDomain);
        }
        
        TenantUpdateEntity tenantUpdateEntity = tenantAdminDAO.getTenantForUpdate(tenantDomain);
        tenantUpdateEntity.setEnabled(true);
        tenantAdminDAO.updateTenant(tenantUpdateEntity);
        
        notifyAfterEnableTenant(tenantDomain);
    }
    
    /**
     * Call all {@link TenantDeployer#onEnableTenant() TenantDeployers} as the system tenant.
     */
    protected void notifyAfterEnableTenant(String tenantDomain)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        
        // notify listeners that tenant has been enabled
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork()
            {
                for (TenantDeployer tenantDeployer : tenantDeployers)
                {
                    tenantDeployer.onEnableTenant();
                }
                return null;
            }
        }, tenantDomain);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Tenant enabled: " + tenantDomain);
        }
    }
    
    @Override
    public void disableTenant(String tenantDomain)
    { 
        tenantDomain = getTenantDomain(tenantDomain);
        
        if (! existsTenant(tenantDomain))
        {
            throw new AuthenticationException("Tenant does not exist: " + tenantDomain);
        }
        
        if (! isEnabledTenant(tenantDomain))
        {
            logger.warn("Tenant already disabled: " + tenantDomain);
        }
        
        notifyBeforeDisableTenant(tenantDomain);
        
        // update tenant attributes / tenant cache - need to disable after notifying listeners (else they cannot disable) 
        TenantUpdateEntity tenantUpdateEntity = tenantAdminDAO.getTenantForUpdate(tenantDomain);
        tenantUpdateEntity.setEnabled(false);
        tenantAdminDAO.updateTenant(tenantUpdateEntity);
    }
    
    private void notifyBeforeDisableTenant(String tenantDomain)
    {
        tenantDomain = getTenantDomain(tenantDomain);
        
        // notify listeners that tenant has been disabled
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
        {
            public Object doWork()
            {
                for (TenantDeployer tenantDeployer : tenantDeployers)
                {
                    tenantDeployer.onDisableTenant();
                }
                return null;
            }
        }, tenantDomain);
        
        if (logger.isInfoEnabled())
        {
            logger.info("Tenant disabled: " + tenantDomain);
        }
    }
    
    @Override
    public boolean isEnabledTenant(String tenantDomain)
    {       
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        
        tenantDomain = getTenantDomain(tenantDomain);
        
        Tenant tenant = getTenantAttributes(tenantDomain);
        if (tenant != null)
        {
            return tenant.isEnabled();
        }
        
        return false;
    }
    
    @Override
    public Tenant getTenant(String tenantDomain)
    {
        tenantDomain = getTenantDomain(tenantDomain);
        if (! existsTenant(tenantDomain))
        {
            throw new AuthenticationException("Tenant does not exist: " + tenantDomain);
        }
        
        return getTenantAttributes(tenantDomain);
    }
    
    @Override
    public void deleteTenant(String tenantDomain)
    {
        tenantDomain = getTenantDomain(tenantDomain);
        
        if (! existsTenant(tenantDomain))
        {
            throw new AuthenticationException("Tenant does not exist: " + tenantDomain);
        }
        else
        {
            try
            {
                TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork()
                    {
                    	// Only undeploy tenant-workflows when MT-workflow deployment is enabled
                    	if(workflowService.isMultiTenantWorkflowDeploymentEnabled()) {
                    		List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
                    		if (workflowDefs != null)
                    		{
                    			for (WorkflowDefinition workflowDef : workflowDefs)
                    			{
                    				workflowService.undeployDefinition(workflowDef.getId());
                    			}
                    		}
                    	}
                        
                        List<String> messageResourceBundles = repoAdminService.getMessageBundles();
                        if (messageResourceBundles != null)
                        {
                            for (String messageResourceBundle : messageResourceBundles)
                            {
                                repoAdminService.undeployMessageBundle(messageResourceBundle);
                            }
                        }   
                        
                        List<RepoModelDefinition> models = repoAdminService.getModels();
                        if (models != null)
                        {
                            for (RepoModelDefinition model : models)
                            {
                                repoAdminService.undeployModel(model.getRepoName());
                            }
                        }
                       
                        return null;
                    }
                }, tenantDomain);
                
                // delete tenant-specific stores
                behaviourFilter.disableBehaviour(ContentModel.ASPECT_UNDELETABLE);
                try
                {
                    nodeService.deleteStore(tenantService.getName(new StoreRef(PROTOCOL_STORE_WORKSPACE, STORE_BASE_ID_SPACES), tenantDomain, false));
                    nodeService.deleteStore(tenantService.getName(new StoreRef(PROTOCOL_STORE_ARCHIVE, STORE_BASE_ID_SPACES), tenantDomain, false));
                    nodeService.deleteStore(tenantService.getName(new StoreRef(PROTOCOL_STORE_WORKSPACE, STORE_BASE_ID_VERSION1), tenantDomain, false));
                    nodeService.deleteStore(tenantService.getName(new StoreRef(PROTOCOL_STORE_WORKSPACE, STORE_BASE_ID_VERSION2), tenantDomain, false));
                    nodeService.deleteStore(tenantService.getName(new StoreRef(PROTOCOL_STORE_SYSTEM, STORE_BASE_ID_SYSTEM), tenantDomain, false));
                    nodeService.deleteStore(tenantService.getName(new StoreRef(PROTOCOL_STORE_USER, STORE_BASE_ID_USER), tenantDomain, false));
                }
                finally
                {
                    behaviourFilter.enableBehaviour(ContentModel.ASPECT_UNDELETABLE);                    
                }
                
                TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        // shutdown modules (if any)
                        moduleService.shutdownModules();
                        
                        // notify listeners that tenant has been deleted & hence disabled
                        for (TenantDeployer tenantDeployer : tenantDeployers)
                        {
                            tenantDeployer.onDisableTenant();
                        }
                        
                        return null;
                    }
                }, tenantDomain);
                
                // remove tenant
                tenantAdminDAO.deleteTenant(tenantDomain);
            } 
            catch (Throwable t)
            {
                throw new AlfrescoRuntimeException("Failed to delete tenant: " + tenantDomain, t);
            }
            
            if (logger.isInfoEnabled())
            {
                logger.info("Tenant deleted: " + tenantDomain);
            }
        }
    }
    
    @Override
    @Deprecated
    public List<Tenant> getAllTenants()
    {
        return getTenants(false);
    }
    
    @Override
    @Deprecated
    public List<Tenant> getTenants(boolean enabledOnly)
    {
        List<TenantEntity> tenantEntities = tenantAdminDAO.listTenants(enabledOnly);
        List<Tenant> tenants = new ArrayList<Tenant>(tenantEntities.size());
        for (TenantEntity tenantEntity : tenantEntities)
        {
            tenants.add(new Tenant(tenantEntity.getTenantDomain(), tenantEntity.getEnabled(), tenantEntity.getContentRoot(), null));
        }
        return tenants;
    }

    private void importBootstrapSystemTenantStore(String tenantDomain, File directorySource)
    {
        // Import Bootstrap (restore) Tenant-Specific Version Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_system.acp");
        
        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap systemImporterBootstrap = (ImporterBootstrap)ctx.getBean("systemBootstrap");
        systemImporterBootstrap.setBootstrapViews(bootstrapViews);
        
        bootstrapSystemTenantStore(systemImporterBootstrap, tenantDomain);
    }
    
    private void bootstrapSystemTenantStore(ImporterBootstrap systemImporterBootstrap, String tenantDomain)
    {
        // Bootstrap Tenant-Specific System Store
        StoreRef bootstrapStoreRef = systemImporterBootstrap.getStoreRef();
        StoreRef tenantBootstrapStoreRef = new StoreRef(bootstrapStoreRef.getProtocol(), tenantService.getName(bootstrapStoreRef.getIdentifier(), tenantDomain));
        systemImporterBootstrap.setStoreUrl(tenantBootstrapStoreRef.toString());
        
        // override default property (workspace://SpacesStore)
        List<String> mustNotExistStoreUrls = new ArrayList<String>();
        mustNotExistStoreUrls.add(new StoreRef(PROTOCOL_STORE_WORKSPACE, tenantService.getName(STORE_BASE_ID_USER, tenantDomain)).toString());
        systemImporterBootstrap.setMustNotExistStoreUrls(mustNotExistStoreUrls);
        
        systemImporterBootstrap.bootstrap();
        
        // reset since systemImporter is singleton (hence reused)
        systemImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Bootstrapped store: "+tenantService.getBaseName(tenantBootstrapStoreRef)+" (Tenant: "+tenantDomain+")");
        }
    }
    
    private void importBootstrapUserTenantStore(String tenantDomain, File directorySource)
    {
        // Import Bootstrap (restore) Tenant-Specific User Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_users.acp");

        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap userImporterBootstrap = (ImporterBootstrap)ctx.getBean("userBootstrap");
        userImporterBootstrap.setBootstrapViews(bootstrapViews);
        
        bootstrapUserTenantStore(userImporterBootstrap, tenantDomain, null);
    }
    
    private void bootstrapUserTenantStore(ImporterBootstrap userImporterBootstrap, String tenantDomain, char[] tenantAdminRawPassword)
    {
        // Bootstrap Tenant-Specific User Store
        StoreRef bootstrapStoreRef = userImporterBootstrap.getStoreRef();
        bootstrapStoreRef = new StoreRef(bootstrapStoreRef.getProtocol(), tenantService.getName(bootstrapStoreRef.getIdentifier(), tenantDomain));
        userImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
        
        // override admin username property
        Properties props = userImporterBootstrap.getConfiguration();
        props.put("alfresco_user_store.adminusername", getTenantAdminUser(tenantDomain));
        
        if (tenantAdminRawPassword != null)
        {
            // TODO: This needs to be replaced with the composite password encoder
            //       and use the new passwordHash property but some further thought
            //       is required how to handle the case where no passsword is provided
            //       as we can no longer presume what hash encoding is being used.
            //       Or maybe this is fine as the background job will upgrade the user object?
            
            // generate the new MD4 password hash
            props.put("alfresco_user_store.adminpassword", passwordEncoder.encodePassword(new String(tenantAdminRawPassword), null));
            
            // generate the new SHA256 password hash
            String salt = props.getProperty("alfresco_user_store.adminsalt");
            ShaPasswordEncoderImpl sha256PasswordEncoder = new ShaPasswordEncoderImpl(256);
            String sha256Password = sha256PasswordEncoder.encodePassword(new String(tenantAdminRawPassword), salt);
            props.put("alfresco_user_store.adminpassword2", sha256Password);
        }
        
        userImporterBootstrap.bootstrap();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Bootstrapped store: "+tenantService.getBaseName(bootstrapStoreRef)+" (Tenant: "+tenantDomain+")");
        }
    }
    
    private void importBootstrapVersionTenantStore(String tenantDomain, File directorySource)
    {
        // Import Bootstrap (restore) Tenant-Specific Version Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_versions2.acp");
        
        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap versionImporterBootstrap = (ImporterBootstrap)ctx.getBean("versionBootstrap");
        versionImporterBootstrap.setBootstrapViews(bootstrapViews);
        
        bootstrapVersionTenantStore(versionImporterBootstrap, tenantDomain);
    }
    
    private void bootstrapVersionTenantStore(ImporterBootstrap versionImporterBootstrap, String tenantDomain)
    {
        // Bootstrap Tenant-Specific Version Store
        StoreRef bootstrapStoreRef = versionImporterBootstrap.getStoreRef();
        bootstrapStoreRef = new StoreRef(bootstrapStoreRef.getProtocol(), tenantService.getName(bootstrapStoreRef.getIdentifier(), tenantDomain));
        versionImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
        
        versionImporterBootstrap.bootstrap();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Bootstrapped store: "+tenantService.getBaseName(bootstrapStoreRef)+" (Tenant: "+tenantDomain+")");
        }
    }
    
    private void importBootstrapSpacesArchiveTenantStore(String tenantDomain, File directorySource)
    {
        // Import Bootstrap (restore) Tenant-Specific Spaces Archive Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_spaces_archive.acp");
        
        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap spacesArchiveImporterBootstrap = (ImporterBootstrap)ctx.getBean("spacesArchiveBootstrap");
        spacesArchiveImporterBootstrap.setBootstrapViews(bootstrapViews);
        
        bootstrapSpacesArchiveTenantStore(spacesArchiveImporterBootstrap, tenantDomain);
    }
    
    private void bootstrapSpacesArchiveTenantStore(ImporterBootstrap spacesArchiveImporterBootstrap, String tenantDomain)
    {
        // Bootstrap Tenant-Specific Spaces Archive Store
        StoreRef bootstrapStoreRef = spacesArchiveImporterBootstrap.getStoreRef();
        bootstrapStoreRef = new StoreRef(bootstrapStoreRef.getProtocol(), tenantService.getName(bootstrapStoreRef.getIdentifier(), tenantDomain));
        spacesArchiveImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
    
        // override default property (archive://SpacesStore)       
        List<String> mustNotExistStoreUrls = new ArrayList<String>();
        mustNotExistStoreUrls.add(bootstrapStoreRef.toString());
        spacesArchiveImporterBootstrap.setMustNotExistStoreUrls(mustNotExistStoreUrls);
        
        spacesArchiveImporterBootstrap.bootstrap();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Bootstrapped store: "+tenantService.getBaseName(bootstrapStoreRef)+" (Tenant: "+tenantDomain+")");
        }
    }

    private void importBootstrapSpacesModelsTenantStore(String tenantDomain, File directorySource)
    {
        // Import Bootstrap (restore) Tenant-Specific Spaces Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_models.acp");

        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap spacesImporterBootstrap = (ImporterBootstrap)ctx.getBean("spacesBootstrap");
        spacesImporterBootstrap.setBootstrapViews(bootstrapViews);

        bootstrapSpacesTenantStore(spacesImporterBootstrap, tenantDomain);
    }
    
    private void importBootstrapSpacesTenantStore(String tenantDomain, File directorySource)
    {
        // Import Bootstrap (restore) Tenant-Specific Spaces Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_spaces.acp");
        bootstrapView.put("uuidBinding", "UPDATE_EXISTING");

        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap spacesImporterBootstrap = (ImporterBootstrap)ctx.getBean("spacesBootstrap");
        spacesImporterBootstrap.setBootstrapViews(bootstrapViews);
        
        spacesImporterBootstrap.setUseExistingStore(true);

        bootstrapSpacesTenantStore(spacesImporterBootstrap, tenantDomain);
    }
    
    private void bootstrapSpacesTenantStore(ImporterBootstrap spacesImporterBootstrap, String tenantDomain)
    {
        // Bootstrap Tenant-Specific Spaces Store
        StoreRef bootstrapStoreRef = spacesImporterBootstrap.getStoreRef();
        StoreRef tenantBootstrapStoreRef = new StoreRef(bootstrapStoreRef.getProtocol(), tenantService.getName(bootstrapStoreRef.getIdentifier(), tenantDomain));
        spacesImporterBootstrap.setStoreUrl(tenantBootstrapStoreRef.toString());
    
        // override admin username property
        Properties props = spacesImporterBootstrap.getConfiguration();
        props.put("alfresco_user_store.adminusername", getTenantAdminUser(tenantDomain));
        
        // override guest username property
        props.put("alfresco_user_store.guestusername", getTenantGuestUser(tenantDomain));
        
        spacesImporterBootstrap.bootstrap();
        
        // reset since spacesImporterBootstrap is singleton (hence reused)
        spacesImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
        
        // calculate any missing usages
        UserUsageTrackingComponent userUsageTrackingComponent = (UserUsageTrackingComponent)ctx.getBean("userUsageTrackingComponent");
        userUsageTrackingComponent.bootstrapInternal();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Bootstrapped store: "+tenantService.getBaseName(tenantBootstrapStoreRef)+" (Tenant: "+tenantDomain+")");
        }
    }
   
    @Override
    public void deployTenants(final TenantDeployer deployer, Log logger)
    {
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("Deployer must be provided");
        }
        if (logger == null)
        {
            throw new AlfrescoRuntimeException("Logger must be provided");
        }
        
        if (tenantService.isEnabled())
        {
            UserTransaction userTransaction = transactionService.getUserTransaction();
            authenticationContext.setSystemUserAsCurrentUser();
            
            List<Tenant> tenants = null;
            try 
            {
                userTransaction.begin();
                tenants = getAllTenants();
                userTransaction.commit();
            }
            catch(Throwable e)
            {
                // rollback the transaction
                try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
                throw new AlfrescoRuntimeException("Failed to get tenants", e);
            }
            finally
            {
                authenticationContext.clearCurrentSecurityContext();
            }
            
            for (Tenant tenant : tenants)
            {
                if (tenant.isEnabled())
                {
                    try
                    {
                        // deploy within context of tenant domain
                        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                        {
                            public Object doWork()
                            {
                                // init the service within tenant context
                                deployer.init();
                                return null;
                            }
                        }, tenant.getTenantDomain());
                    
                    }
                    catch (Throwable e)
                    {
                        logger.error("Deployment failed" + e);
                        
                        StringWriter stringWriter = new StringWriter();
                        e.printStackTrace(new PrintWriter(stringWriter));
                        logger.error(stringWriter.toString());
                        
                        // tenant deploy failure should not necessarily affect other tenants
                    }
                }
            }
        }
    }
    
    @Override
    public void undeployTenants(final TenantDeployer deployer, Log logger)
    {
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("Deployer must be provided");
        }
        if (logger == null)
        {
            throw new AlfrescoRuntimeException("Logger must be provided");
        }
        
        if (tenantService.isEnabled())
        {
            UserTransaction userTransaction = transactionService.getUserTransaction();
            authenticationContext.setSystemUserAsCurrentUser();
            
            List<Tenant> tenants = null;
            try
            {
                userTransaction.begin();
                tenants = getAllTenants();
                userTransaction.commit();
            }
            catch(Throwable e)
            {
                // rollback the transaction
                try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
                try {authenticationContext.clearCurrentSecurityContext(); } catch (Exception ex) {}
                throw new AlfrescoRuntimeException("Failed to get tenants", e);
            }
            
            try
            {
                AuthenticationUtil.pushAuthentication();
                for (Tenant tenant : tenants)
                {
                    if (tenant.isEnabled())
                    {
                        try
                        {
                            // undeploy within context of tenant domain
                            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Object>()
                            {
                                public Object doWork()
                                {
                                    // destroy the service within tenant context
                                    deployer.destroy();
                                    return null;
                                }
                            }, tenant.getTenantDomain());
                        }
                        catch (Throwable e)
                        {
                            logger.error("Undeployment failed" + e);
                            
                            StringWriter stringWriter = new StringWriter();
                            e.printStackTrace(new PrintWriter(stringWriter));
                            logger.error(stringWriter.toString());
                            
                            // tenant undeploy failure should not necessarily affect other tenants
                        }
                    }
                }
            }
            finally
            {
                AuthenticationUtil.popAuthentication();
            }
        }
    }
    
    @Override
    public void register(TenantDeployer deployer)
    {
        register(deployer, -1);
    }
    
    private void register(TenantDeployer deployer, int position)
    {
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("Deployer must be provided");
        }
       
        if (! tenantDeployers.contains(deployer))
        {
            if (position == -1)
            {
                tenantDeployers.add(deployer);
            }
            else
            {
                tenantDeployers.add(position, deployer);
            }
        }
    }
    
    @Override
    public void unregister(TenantDeployer deployer)
    {
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("TenantDeployer must be provided");
        }
        
        if (tenantDeployers != null)
        {
            tenantDeployers.remove(deployer);
        }
    }
    
    @Override
    public void register(WorkflowDeployer workflowDeployer)
    {
        if (workflowDeployer == null)
        {
            throw new AlfrescoRuntimeException("WorkflowDeployer must be provided");
        }
        
        if (! workflowDeployers.contains(workflowDeployer))
        {
            workflowDeployers.add(workflowDeployer);
        }
    }
     
    protected TenantRoutingContentStore tenantRoutingContentStore(ContentStore contentStore)
    {
        if (contentStore instanceof TenantRoutingContentStore)
        {
            return (TenantRoutingContentStore) contentStore;
        }
        else if (contentStore instanceof ContentStoreCaps)
        {
            ContentStoreCaps capabilities = (ContentStoreCaps) contentStore;
            return (TenantRoutingContentStore) capabilities.getTenantRoutingContentStore();
        }
        return null;
    }
    
    protected boolean isTenantRoutingContentStore(ContentStore contentStore)
    {
        boolean router = tenantRoutingContentStore(contentStore) != null;
        return router;
    }
    
    protected TenantDeployer tenantDeployer(ContentStore contentStore)
    {
        if (contentStore instanceof TenantDeployer)
        {
            return (TenantDeployer) contentStore;
        }
        else if (contentStore instanceof ContentStoreCaps)
        {
            ContentStoreCaps capabilities = (ContentStoreCaps) contentStore;
            return (TenantDeployer) capabilities.getTenantDeployer();
        }
        return null;
    }
    
    protected boolean isTenantDeployer(ContentStore contentStore)
    {
        boolean deployer = tenantDeployer(contentStore) != null;
        return deployer;
    }
    
    
    private void initTenant(String tenantDomain, String contentRoot, String dbUrl)
    {
        validateTenantName(tenantDomain);
        
        if (existsTenant(tenantDomain))
        {
            throw new AlfrescoRuntimeException("Tenant already exists: " + tenantDomain);
        }
        
        if (contentRoot == null)
        {
            contentRoot = tenantFileContentStore.getRootLocation();
        }
        else
        {
            if (! isTenantRoutingContentStore(tenantFileContentStore))
            {
                // eg. ALF-14121 - MT will not work with replicating-content-services-context.sample
                throw new AlfrescoRuntimeException("MT: cannot initialse tenant - TenantRoutingContentStore is not configured AND tenant is not using co-mingled content store (ie. default root location)");
            }
            
            File tenantRootDir = new File(contentRoot);
            if ((tenantRootDir.exists()) && (tenantRootDir.list().length != 0))
            {
                logger.warn("Tenant root directory is not empty: " + contentRoot);
            }
        }
        
        // init - need to enable tenant (including tenant service) before stores bootstrap
        TenantEntity tenantEntity = new TenantEntity(tenantDomain);
        tenantEntity.setEnabled(true);
        tenantEntity.setContentRoot(contentRoot);
        tenantEntity.setDbUrl(dbUrl);
        
        tenantAdminDAO.createTenant(tenantEntity);
    }
    
    /**
     * @see #MAX_LEN
     * @see #REGEX_CONTAINS_ALPHA
     * @see #REGEX_VALID_DNS_LABEL
     */
    protected void validateTenantName(String tenantDomain)
    {
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        
        if (tenantDomain.length() > MAX_LEN)
        {
        	throw new IllegalArgumentException(tenantDomain + " is not a valid tenant name (must be less than " + MAX_LEN + " characters)");
        }
        
        if (! Pattern.matches(REGEX_CONTAINS_ALPHA, tenantDomain))
        {
        	throw new IllegalArgumentException(tenantDomain + " is not a valid tenant name (must contain at least one alpha character)");
        }
        
        String[] dnsLabels = tenantDomain.split("\\.");
        if (dnsLabels.length != 0)
        {
	        for (int i = 0; i < dnsLabels.length; i++)
	        {
		        if (! Pattern.matches(REGEX_VALID_DNS_LABEL, dnsLabels[i]))
		        {
		        	throw new IllegalArgumentException(dnsLabels[i] + " is not a valid DNS label (must match " + REGEX_VALID_DNS_LABEL + ")");
		        }
	        }
        }
        else
        {
	        if (! Pattern.matches(REGEX_VALID_DNS_LABEL, tenantDomain))
	        {
	        	throw new IllegalArgumentException(tenantDomain + " is not a valid DNS label (must match " + REGEX_VALID_DNS_LABEL + ")");
	        }
        }
    }
    
    // tenant deployer/user services delegated to tenant service
    
    @Override
    public boolean isEnabled()
    {
        return tenantService.isEnabled();
    }
    
    @Override
    public String getCurrentUserDomain()
    {
        return tenantService.getCurrentUserDomain();
    }

    @Override
    public String getUserDomain(String username)
    {
        return tenantService.getUserDomain(username);
    }
    
    @Override
    public String getBaseNameUser(String username)
    {
        return tenantService.getBaseNameUser(username);
    }
    
    @Override
    public String getDomainUser(String baseUsername, String tenantDomain)
    {
        tenantDomain = getTenantDomain(tenantDomain);
        return tenantService.getDomainUser(baseUsername, tenantDomain);
    }
    
    // local helpers
    
    private String getBaseAdminUser()
    {
        // default for backwards compatibility only - eg. upgrade of existing MT instance (mt-admin-context.xml.sample)
        if (baseAdminUsername != null)
        {
            return baseAdminUsername;
        }
        return getBaseNameUser(AuthenticationUtil.getAdminUserName());
    }
    
    private String getSystemUser(String tenantDomain)
    {
        return tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
    }
    
    private String getTenantAdminUser(String tenantDomain)
    {
        
        return tenantService.getDomainUser(getBaseAdminUser(), tenantDomain);
    }

    private String getTenantGuestUser(String tenantDomain)
    {
        return authenticationContext.getGuestUserName(tenantDomain);
    }
    
    /**
     * Do a null check and convert tenant domain to lowercase
     */
    protected String getTenantDomain(String tenantDomain)
    {
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        return tenantDomain.toLowerCase(I18NUtil.getLocale());
    }
}
