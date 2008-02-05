/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.tenant;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.transaction.UserTransaction;

import net.sf.acegisecurity.providers.encoding.PasswordEncoder;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.RepoModelDefinition;
import org.alfresco.repo.attributes.BooleanAttributeValue;
import org.alfresco.repo.attributes.MapAttribute;
import org.alfresco.repo.attributes.MapAttributeValue;
import org.alfresco.repo.attributes.StringAttributeValue;
import org.alfresco.repo.content.TenantRoutingFileContentStore;
import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.node.db.DbNodeServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.usage.UserUsageBootstrapJob;
import org.alfresco.repo.usage.UserUsageTrackingComponent;
import org.alfresco.repo.workflow.WorkflowDeployer;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.RepositoryExporterService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.AbstractLifecycleBean;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * MT Admin Service Implementation.
 * 
 */
    
public class MultiTAdminServiceImpl extends AbstractLifecycleBean implements TenantAdminService, TenantDeployerService
{
    // Logger
    private static Log logger = LogFactory.getLog(MultiTAdminServiceImpl.class);
    
    // Dependencies    
    private DbNodeServiceImpl nodeService; // TODO - replace with NodeService, when deleteStore is exposed via public API
    private DictionaryComponent dictionaryComponent;
    private RepoAdminService repoAdminService;
    private AuthenticationComponent authenticationComponent;    
    private TransactionService transactionService;    
    private MultiTServiceImpl tenantService;    
    private AttributeService attributeService;      
    private PasswordEncoder passwordEncoder;
    private TenantRoutingFileContentStore tenantFileContentStore;
    private WorkflowService workflowService;
    private RepositoryExporterService repositoryExporterService;
    private WorkflowDeployer workflowDeployer;
    
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
    
    public void setAuthenticationComponent(AuthenticationComponent authenticationComponent)
    {
        this.authenticationComponent = authenticationComponent;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
  
    public void setTenantService(MultiTServiceImpl tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public void setTenantFileContentStore(TenantRoutingFileContentStore tenantFileContentStore)
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
    
    public void setWorkflowDeployer(WorkflowDeployer workflowDeployer)
    {
        this.workflowDeployer = workflowDeployer;
    }
    
    
    public static final String PROTOCOL_STORE_USER = "user";
    public static final String PROTOCOL_STORE_WORKSPACE = "workspace";
    public static final String PROTOCOL_STORE_SYSTEM = "system";
    public static final String PROTOCOL_STORE_ARCHIVE = "archive"; 
    public static final String STORE_BASE_ID_USER = "alfrescoUserStore";
    public static final String STORE_BASE_ID_SYSTEM = "system";
    public static final String STORE_BASE_ID_VERSION = "lightWeightVersionStore";
    public static final String STORE_BASE_ID_SPACES = "SpacesStore";
       
    
    private static final String TENANTS_ATTRIBUTE_PATH = "alfresco-tenants";
    private static final String TENANT_ATTRIBUTE_ENABLED = "enabled";
    private static final String TENANT_ROOT_CONTENT_STORE_DIR = "rootContentStoreDir";
    
    private static final String ADMIN_BASENAME = TenantService.ADMIN_BASENAME;

    private List<TenantDeployer> tenantDeployers = new ArrayList<TenantDeployer>();

    
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // initialise the tenant admin service and status of tenants (using attribute service)
        // note: this requires that the repository schema has already been initialised
        
        // register dictionary - to allow enable/disable tenant callbacks
        register(dictionaryComponent);
        
        // register file store - to allow enable/disable tenant callbacks
        register(tenantFileContentStore);
        
        UserTransaction userTransaction = transactionService.getUserTransaction();           
        authenticationComponent.setSystemUserAsCurrentUser();
                                        
        try 
        {
            userTransaction.begin();
            
            // bootstrap Tenant Service internal cache            
            List<Tenant> tenants = getAllTenants();
                    
            int enabledCount = 0;
            int disabledCount = 0;
            
            for (Tenant tenant : tenants)
            {                    
                if (tenant.isEnabled())
                {
                    // this will also call tenant deployers registered so far ...
                    enableTenant(tenant.getTenantDomain(), true);
                    enabledCount++;
                }
                else
                {
                    // explicitly disable, without calling disableTenant callback
                    disableTenant(tenant.getTenantDomain(), false);
                    disabledCount++;
                }
            } 
            
            tenantService.register(this); // callback to refresh tenantStatus cache
            
            userTransaction.commit();
            
            if (logger.isInfoEnabled())
            {
                logger.info(String.format("Alfresco Multi-Tenant startup - %d enabled tenants, %d disabled tenants",
                                          enabledCount, disabledCount));
            }
        }
        catch(Throwable e)
        {
            // rollback the transaction
            try { if (userTransaction != null) {userTransaction.rollback();} } catch (Exception ex) {}
            try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
            throw new AlfrescoRuntimeException("Failed to bootstrap tenants", e);
        }
    }
    
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        tenantDeployers.clear();
        tenantDeployers = null;
    }

    /**
     * @see TenantAdminService.createTenant()
     */
    public void createTenant(final String tenantDomain, final char[] tenantAdminRawPassword)
    {  
        createTenant(tenantDomain, tenantAdminRawPassword, null);
    }
    
    /**
     * @see TenantAdminService.createTenant()
     */
    public void createTenant(final String tenantDomain, final char[] tenantAdminRawPassword, String rootContentStoreDir)
    {             
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        ParameterCheck.mandatory("tenantAdminRawPassword", tenantAdminRawPassword);
        
        validateTenantName(tenantDomain);
        
        if (existsTenant(tenantDomain))
        {
            throw new AlfrescoRuntimeException("Tenant already exists: " + tenantDomain);
        }
        else
        {                      
            authenticationComponent.setSystemUserAsCurrentUser();

            if (rootContentStoreDir == null)
            {
                rootContentStoreDir = tenantFileContentStore.getDefaultRootDir();
            }
            
            // init - need to enable tenant (including tenant service) before stores bootstrap
            Tenant tenant = new Tenant(tenantDomain, true, rootContentStoreDir);
            putTenantAttributes(tenantDomain, tenant);          
            
            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {           
                            dictionaryComponent.init();
                            tenantFileContentStore.init();
                            
                            // create tenant-specific stores
                            ImporterBootstrap userImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("userBootstrap");
                            bootstrapUserTenantStore(userImporterBootstrap, tenantDomain, tenantAdminRawPassword);
                            
                            ImporterBootstrap systemImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("systemBootstrap");
                            bootstrapSystemTenantStore(systemImporterBootstrap, tenantDomain);

                            ImporterBootstrap versionImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("versionBootstrap");
                            bootstrapVersionTenantStore(versionImporterBootstrap, tenantDomain);

                            ImporterBootstrap spacesArchiveImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("spacesArchiveBootstrap");
                            bootstrapSpacesArchiveTenantStore(spacesArchiveImporterBootstrap, tenantDomain);
                            
                            ImporterBootstrap spacesImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("spacesBootstrap");
                            bootstrapSpacesTenantStore(spacesImporterBootstrap, tenantDomain);                           
                            
                            // notify listeners that tenant has been created & hence enabled
                            for (TenantDeployer tenantDeployer : tenantDeployers)
                            {
                                tenantDeployer.onEnableTenant();
                            }
                            
                            return null;
                        }                               
                    }, getSystemUser(tenantDomain));
        }

        logger.info("Tenant created: " + tenantDomain);
    }
    
    /**
     * Export tenant - equivalent to the tenant admin running a 'complete repo' export from the Web Client Admin
     */
    public void exportTenant(final String tenantDomain, final File directoryDestination)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {           
                    	repositoryExporterService.export(directoryDestination, tenantDomain);
                        return null;
                    }                               
                }, getSystemUser(tenantDomain));
        
        logger.info("Tenant exported: " + tenantDomain);
    }
    
    /**
     * Create tenant by restoring from a complete repository export. This is equivalent to a bootstrap import using restore-context.xml.
     */
    public void importTenant(final String tenantDomain, final File directorySource, String rootContentStoreDir)
    {             
       // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        
        validateTenantName(tenantDomain);
        
        if (existsTenant(tenantDomain))
        {
            throw new AlfrescoRuntimeException("Tenant already exists: " + tenantDomain);
        }
        else
        {                      
            authenticationComponent.setSystemUserAsCurrentUser();

            if (rootContentStoreDir == null)
            {
                rootContentStoreDir = tenantFileContentStore.getDefaultRootDir();
            }
            
            // init - need to enable tenant (including tenant service) before stores bootstrap
            Tenant tenant = new Tenant(tenantDomain, true, rootContentStoreDir);
            putTenantAttributes(tenantDomain, tenant);          
            
            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {           
                            dictionaryComponent.init();
                            tenantFileContentStore.init();
                            
                            // import tenant-specific stores
                            importBootstrapUserTenantStore(tenantDomain, directorySource);
                            importBootstrapSystemTenantStore(tenantDomain, directorySource);
                            importBootstrapVersionTenantStore(tenantDomain, directorySource);
                            importBootstrapSpacesArchiveTenantStore(tenantDomain, directorySource);
                            importBootstrapSpacesModelsTenantStore(tenantDomain, directorySource);
                            importBootstrapSpacesTenantStore(tenantDomain, directorySource);
                            
                            // notify listeners that tenant has been created & hence enabled
                            for (TenantDeployer tenantDeployer : tenantDeployers)
                            {
                                tenantDeployer.onEnableTenant();
                            }
                            
                            return null;
                        }                               
                    }, getSystemUser(tenantDomain));
        }

        logger.info("Tenant imported: " + tenantDomain);
    }
    
    public boolean existsTenant(String tenantDomain)
    {
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);

        return (getTenantAttributes(tenantDomain) != null);
    }
    
    private void putTenantAttributes(String tenantDomain, Tenant tenant)
    {
        if (! attributeService.exists(TENANTS_ATTRIBUTE_PATH))
        { 
            // bootstrap
            attributeService.setAttribute("", TENANTS_ATTRIBUTE_PATH, new MapAttributeValue());
        }
        
        MapAttribute tenantProps = new MapAttributeValue();
        tenantProps.put(TENANT_ATTRIBUTE_ENABLED, new BooleanAttributeValue(tenant.isEnabled()));
        tenantProps.put(TENANT_ROOT_CONTENT_STORE_DIR, new StringAttributeValue(tenant.getRootContentStoreDir()));
        
        attributeService.setAttribute(TENANTS_ATTRIBUTE_PATH, tenantDomain, tenantProps);
        
        // update tenant status cache
        ((MultiTServiceImpl)tenantService).putTenant(tenantDomain, tenant);
    }
    
    private Tenant getTenantAttributes(String tenantDomain)
    {
        if (attributeService.exists(TENANTS_ATTRIBUTE_PATH+"/"+tenantDomain))
        {        
            MapAttribute map = (MapAttribute)attributeService.getAttribute(TENANTS_ATTRIBUTE_PATH+"/"+tenantDomain);
            if (map != null)
            {
                return new Tenant(tenantDomain, 
                                  map.get(TENANT_ATTRIBUTE_ENABLED).getBooleanValue(),
                                  map.get(TENANT_ROOT_CONTENT_STORE_DIR).getStringValue());
            }
        }
        
        return null;
    }
    
    public void enableTenant(String tenantDomain)
    { 
        if (isEnabledTenant(tenantDomain))
        {
            logger.warn("Tenant already enabled: " + tenantDomain);
        }
        
        enableTenant(tenantDomain, true);
    }
    
    private void enableTenant(String tenantDomain, boolean notifyTenantDeployers)
    {    
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        
        Tenant tenant = getTenantAttributes(tenantDomain);
        tenant = new Tenant(tenantDomain, true, tenant.getRootContentStoreDir()); // enable
        putTenantAttributes(tenantDomain, tenant);
        
        if (notifyTenantDeployers)
        {
            // notify listeners that tenant has been enabled
            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {
                            for (TenantDeployer tenantDeployer : tenantDeployers)
                            {
                                tenantDeployer.onEnableTenant();
                            }
                            return null;
                        }
                    }, getSystemUser(tenantDomain));
        }
        
        logger.info("Tenant enabled: " + tenantDomain);
    }   
    
    public void disableTenant(String tenantDomain)
    { 
        if (! isEnabledTenant(tenantDomain))
        {
            logger.warn("Tenant already disabled: " + tenantDomain);
        }
        
        disableTenant(tenantDomain, true);
    }
    
    public void disableTenant(String tenantDomain, boolean notifyTenantDeployers)
    {     
        if (notifyTenantDeployers)
        {
            // notify listeners that tenant has been disabled
            AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork()
                        {
                            for (TenantDeployer tenantDeployer : tenantDeployers)
                            {
                                tenantDeployer.onDisableTenant();
                            }
                            return null;
                        }
                    }, getSystemUser(tenantDomain));
        }
        
        // update tenant attributes / tenant cache - need to disable after notifying listeners (else they cannot disable) 
        Tenant tenant = getTenantAttributes(tenantDomain);
        tenant = new Tenant(tenantDomain, false, tenant.getRootContentStoreDir()); // disable
        putTenantAttributes(tenantDomain, tenant);
        
        logger.info("Tenant disabled: " + tenantDomain);
    }
    
    public boolean isEnabledTenant(String tenantDomain)
    {       
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);

        Tenant tenant = getTenantAttributes(tenantDomain);
        if (tenant != null)
        {
            return tenant.isEnabled();
        }

        return false;
    }
    
    protected String getRootContentStoreDir(String tenantDomain)
    {       
        // Check that all the passed values are not null
        ParameterCheck.mandatory("tenantDomain", tenantDomain);
        
        Tenant tenant = getTenantAttributes(tenantDomain);
        if (tenant != null)
        {
            return tenant.getRootContentStoreDir();
        }
        
        return null;
    }
    
    protected void putRootContentStoreDir(String tenantDomain, String rootContentStoreDir)
    {
        Tenant tenant = getTenantAttributes(tenantDomain);
        tenant = new Tenant(tenantDomain, tenant.isEnabled(), rootContentStoreDir);
        putTenantAttributes(tenantDomain, tenant);
    }
    
    public Tenant getTenant(String tenantDomain)
    {
        return new Tenant(tenantDomain, isEnabledTenant(tenantDomain), getRootContentStoreDir(tenantDomain));
    }
        
    public void bootstrapWorkflows(String tenantDomain)
    {
        AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        workflowDeployer.init();
                        return null;
                    }
                }, getSystemUser(tenantDomain));
        
        logger.info("Tenant workflows bootstrapped: " + tenantDomain);
    }
    
    /**
     * @see TenantAdminService.deleteTenant()
     */
    public void deleteTenant(String tenantDomain)
    {
        if (! existsTenant(tenantDomain))
        {
            throw new RuntimeException("Tenant does not exist: " + tenantDomain);
        }
        else
        {                        
            try 
            {
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        List<WorkflowDefinition> workflowDefs = workflowService.getDefinitions();
                        if (workflowDefs != null)
                        {
                            for (WorkflowDefinition workflowDef : workflowDefs)
                            {
                                workflowService.undeployDefinition(workflowDef.getId());
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
                }, getSystemUser(tenantDomain));
                
                final String tenantAdminUser = getTenantAdminUser(tenantDomain);
                
                // delete tenant-specific stores
                nodeService.deleteStore(tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_WORKSPACE, STORE_BASE_ID_SPACES)));
                nodeService.deleteStore(tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_ARCHIVE, STORE_BASE_ID_SPACES)));
                nodeService.deleteStore(tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_WORKSPACE, STORE_BASE_ID_VERSION)));
                nodeService.deleteStore(tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_SYSTEM, STORE_BASE_ID_SYSTEM)));
                nodeService.deleteStore(tenantService.getName(tenantAdminUser, new StoreRef(PROTOCOL_STORE_USER, STORE_BASE_ID_USER)));
                        

                // notify listeners that tenant has been deleted & hence disabled
                AuthenticationUtil.runAs(new RunAsWork<Object>()
                {
                    public Object doWork()
                    {
                        for (TenantDeployer tenantDeployer : tenantDeployers)
                        {
                            tenantDeployer.onDisableTenant();
                        }
                        return null;
                    }
                }, getSystemUser(tenantDomain));
                
                // remove tenant
                attributeService.removeAttribute(TENANTS_ATTRIBUTE_PATH, tenantDomain);
            } 
            catch (Throwable t)
            {
                throw new AlfrescoRuntimeException("Failed to delete tenant: " + tenantDomain, t);
            }   
        }
    }
    
    /**
     * @see TenantAdminService.getAllTenants()
     */
    public List<Tenant> getAllTenants()
    {
        MapAttribute map = (MapAttribute)attributeService.getAttribute(TENANTS_ATTRIBUTE_PATH);
        
        List<Tenant> tenants = new ArrayList<Tenant>();
        
        if (map != null)
        {
            // note: getAllTenants is called first, by TenantDeployer - hence need to initialise the TenantService status cache           
            Set<String> tenantDomains = map.keySet();
                            
            for (String tenantDomain : tenantDomains)
            {        
                Tenant tenant = getTenantAttributes(tenantDomain);
                tenants.add(new Tenant(tenantDomain, tenant.isEnabled(), tenant.getRootContentStoreDir()));
            }        
        }
        
        return tenants; // list of tenants or empty list     
    }

    private void importBootstrapSystemTenantStore(String tenantDomain, File directorySource)
    {     
        // Import Bootstrap (restore) Tenant-Specific Version Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_system.acp");

        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap systemImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("systemBootstrap");
        systemImporterBootstrap.setBootstrapViews(bootstrapViews);
        systemImporterBootstrap.setLog(true);

        bootstrapSystemTenantStore(systemImporterBootstrap, tenantDomain);
    }
    
    private void bootstrapSystemTenantStore(ImporterBootstrap systemImporterBootstrap, String tenantDomain)
    {
        // Bootstrap Tenant-Specific System Store
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_SYSTEM, tenantService.getName(STORE_BASE_ID_SYSTEM, tenantDomain));
        systemImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
    
        // override default property (workspace://SpacesStore)        
        List<String> mustNotExistStoreUrls = new ArrayList<String>();
        mustNotExistStoreUrls.add(new StoreRef(PROTOCOL_STORE_WORKSPACE, tenantService.getName(STORE_BASE_ID_USER, tenantDomain)).toString());
        systemImporterBootstrap.setMustNotExistStoreUrls(mustNotExistStoreUrls);
                
        systemImporterBootstrap.bootstrap();
        
        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }
    
    private void importBootstrapUserTenantStore(String tenantDomain, File directorySource)
    {     
        // Import Bootstrap (restore) Tenant-Specific User Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_users.acp");

        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap userImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("userBootstrap");
        userImporterBootstrap.setBootstrapViews(bootstrapViews);
        userImporterBootstrap.setLog(true);

        bootstrapUserTenantStore(userImporterBootstrap, tenantDomain, null);
    }
    
    private void bootstrapUserTenantStore(ImporterBootstrap userImporterBootstrap, String tenantDomain, char[] tenantAdminRawPassword)
    {     
        // Bootstrap Tenant-Specific User Store
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_USER, tenantService.getName(STORE_BASE_ID_USER, tenantDomain));
        userImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
    
        // override admin username property
        Properties props = userImporterBootstrap.getConfiguration();       
        props.put("alfresco_user_store.adminusername", getTenantAdminUser(tenantDomain));
        
        if (tenantAdminRawPassword != null)
        {
            String salt = null; // GUID.generate();
        	props.put("alfresco_user_store.adminpassword", passwordEncoder.encodePassword(new String(tenantAdminRawPassword), salt));
        }
        
        userImporterBootstrap.bootstrap();
        
        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }

    private void importBootstrapVersionTenantStore(String tenantDomain, File directorySource)
    {     
        // Import Bootstrap (restore) Tenant-Specific Version Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_versions.acp");

        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap versionImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("versionBootstrap");
        versionImporterBootstrap.setBootstrapViews(bootstrapViews);
        versionImporterBootstrap.setLog(true);

        bootstrapVersionTenantStore(versionImporterBootstrap, tenantDomain);
    }
    
    private void bootstrapVersionTenantStore(ImporterBootstrap versionImporterBootstrap, String tenantDomain)
    {
        // Bootstrap Tenant-Specific Version Store
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_WORKSPACE, tenantService.getName(STORE_BASE_ID_VERSION, tenantDomain));
        versionImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
    
        versionImporterBootstrap.bootstrap();
        
        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }
    
    private void importBootstrapSpacesArchiveTenantStore(String tenantDomain, File directorySource)
    {     
        // Import Bootstrap (restore) Tenant-Specific Spaces Archive Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_spaces_archive.acp");

        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap spacesArchiveImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("spacesArchiveBootstrap");
        spacesArchiveImporterBootstrap.setBootstrapViews(bootstrapViews);
        spacesArchiveImporterBootstrap.setLog(true);

        bootstrapSpacesArchiveTenantStore(spacesArchiveImporterBootstrap, tenantDomain);
    }
    
    private void bootstrapSpacesArchiveTenantStore(ImporterBootstrap spacesArchiveImporterBootstrap, String tenantDomain)
    {
        // Bootstrap Tenant-Specific Spaces Archive Store 
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_ARCHIVE, tenantService.getName(STORE_BASE_ID_SPACES, tenantDomain));
        spacesArchiveImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
    
        // override default property (archive://SpacesStore)       
        List<String> mustNotExistStoreUrls = new ArrayList<String>();
        mustNotExistStoreUrls.add(new StoreRef(PROTOCOL_STORE_ARCHIVE, tenantService.getName(STORE_BASE_ID_SPACES, tenantDomain)).toString());
        spacesArchiveImporterBootstrap.setMustNotExistStoreUrls(mustNotExistStoreUrls);        
        
        spacesArchiveImporterBootstrap.bootstrap();
        
        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }

    private void importBootstrapSpacesModelsTenantStore(String tenantDomain, File directorySource)
    {     
        // Import Bootstrap (restore) Tenant-Specific Spaces Store
        Properties bootstrapView = new Properties();
        bootstrapView.put("path", "/");
        bootstrapView.put("location", directorySource.getPath()+"/"+tenantDomain+"_models.acp");

        List<Properties> bootstrapViews = new ArrayList<Properties>(1);
        bootstrapViews.add(bootstrapView);
        
        ImporterBootstrap spacesImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("spacesBootstrap");
        spacesImporterBootstrap.setBootstrapViews(bootstrapViews);
        spacesImporterBootstrap.setLog(true);

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
        
        ImporterBootstrap spacesImporterBootstrap = (ImporterBootstrap)getApplicationContext().getBean("spacesBootstrap");
        spacesImporterBootstrap.setBootstrapViews(bootstrapViews);
        spacesImporterBootstrap.setLog(true);
        
        spacesImporterBootstrap.setUseExistingStore(true);

        bootstrapSpacesTenantStore(spacesImporterBootstrap, tenantDomain);
    }
    
    private void bootstrapSpacesTenantStore(ImporterBootstrap spacesImporterBootstrap, String tenantDomain)
    {
        // Bootstrap Tenant-Specific Spaces Store    
        StoreRef bootstrapStoreRef = new StoreRef(PROTOCOL_STORE_WORKSPACE, tenantService.getName(STORE_BASE_ID_SPACES, tenantDomain));
        spacesImporterBootstrap.setStoreUrl(bootstrapStoreRef.toString());
    
        // override admin username property
        Properties props = spacesImporterBootstrap.getConfiguration();
        props.put("alfresco_user_store.adminusername", getTenantAdminUser(tenantDomain));
        
        // override guest username property
        props.put("alfresco_user_store.guestusername", getTenantGuestUser(tenantDomain));
   
        spacesImporterBootstrap.bootstrap();
        
        // calculate any missing usages
        UserUsageTrackingComponent userUsageTrackingComponent = (UserUsageTrackingComponent)getApplicationContext().getBean(UserUsageBootstrapJob.KEY_COMPONENT);
        userUsageTrackingComponent.bootstrapInternal();
       
        logger.debug("Bootstrapped store: " + tenantService.getBaseName(bootstrapStoreRef));
    }
   
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
            authenticationComponent.setSystemUserAsCurrentUser();
                                    
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
                try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
                throw new AlfrescoRuntimeException("Failed to get tenants", e);
            }
                           
            String currentUser = AuthenticationUtil.getCurrentUserName();
            
            try 
            {
                for (Tenant tenant : tenants)
                { 
                    if (tenant.isEnabled()) 
                    {
                        try
                        {     
                            // switch to admin in order to deploy within context of tenant domain
                            // assumes each tenant has default "admin" user                       
                            AuthenticationUtil.runAs(new RunAsWork<Object>()
                            {
                                public Object doWork()
                                {            
                                    // init the service within tenant context
                                    deployer.init();
                                    return null;
                                }                               
                            }, getSystemUser(tenant.getTenantDomain()));
                        
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
            finally
            {    
                if (currentUser != null) { AuthenticationUtil.setCurrentUser(currentUser); }
            }
        }
    }
    
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
            authenticationComponent.setSystemUserAsCurrentUser();
                                    
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
                try {authenticationComponent.clearCurrentSecurityContext(); } catch (Exception ex) {}
                throw new AlfrescoRuntimeException("Failed to get tenants", e);
            }
                           
            String currentUser = AuthenticationUtil.getCurrentUserName();
            
            try 
            {
                for (Tenant tenant : tenants)
                { 
                    if (tenant.isEnabled()) 
                    {
                        try
                        {
                            // switch to admin in order to deploy within context of tenant domain
                            // assumes each tenant has default "admin" user
                            AuthenticationUtil.runAs(new RunAsWork<Object>()
                            {
                                public Object doWork()
                                {            
                                    // destroy the service within tenant context
                                    deployer.destroy();
                                    return null;
                                }                               
                            }, getSystemUser(tenant.getTenantDomain()));
                        
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
                if (currentUser != null) { AuthenticationUtil.setCurrentUser(currentUser); }
            }
        }
    }   
    
    public void register(TenantDeployer deployer)
    {
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("Deployer must be provided");
        }  
        
        if (! tenantDeployers.contains(deployer))
        {
            tenantDeployers.add(deployer);
        }
    }
    
    public void unregister(TenantDeployer deployer)
    {
        if (deployer == null)
        {
            throw new AlfrescoRuntimeException("Deployer must be provided");
        } 

        if (tenantDeployers != null)
        {
            tenantDeployers.remove(deployer);
        }
    }
    
    public boolean isEnabled()
    {
        return tenantService.isEnabled();
    }
    
    public void resetCache(String tenantDomain)
    {
        if (existsTenant(tenantDomain))
        {
            if (isEnabledTenant(tenantDomain))
            {
                enableTenant(tenantDomain);
            }
            else
            {
                disableTenant(tenantDomain);
            }
        }
        else
        {
            throw new AlfrescoRuntimeException("No such tenant " + tenantDomain);
        }
    }
    
    private void validateTenantName(String tenantDomain)
    {
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
    
    // local helpers
    
    private String getSystemUser(String tenantDomain)
    {
        return tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain);
    }
    
    private String getTenantAdminUser(String tenantDomain)
    {
        return tenantService.getDomainUser(ADMIN_BASENAME, tenantDomain);
    }

    private String getTenantGuestUser(String tenantDomain)
    {
        return tenantService.getDomainUser(authenticationComponent.getGuestUserName(), tenantDomain);
    }
}
