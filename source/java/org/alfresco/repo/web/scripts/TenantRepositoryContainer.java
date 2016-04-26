package org.alfresco.repo.web.scripts;

import org.alfresco.repo.cache.AsynchronouslyRefreshedCache;
import org.alfresco.repo.tenant.TenantAdminService;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Registry;

/**
 * Tenant-aware Repository (server-tier) container for Web Scripts
 * 
 * @author davidc
 */
public class TenantRepositoryContainer extends RepositoryContainer implements TenantDeployer
{
    // Logger
    protected static final Log logger = LogFactory.getLog(TenantRepositoryContainer.class);

    /* Component Dependencies */
    protected TenantAdminService tenantAdminService;
    protected TransactionService transactionService;
    private AsynchronouslyRefreshedCache<Registry> registryCache;

    /**
     * @param registryCache                 asynchronously maintained cache for script registries
     */
    public void setWebScriptsRegistryCache(AsynchronouslyRefreshedCache<Registry> registryCache)
    {
        this.registryCache = registryCache;
    }
    
    /**
     * @param tenantAdminService            service to sort out tenant context
     */
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
    
    /**
     * @param transactionService            service to give transactions when reading from the container
     */
    public void setTransactionService(TransactionService transactionService)
    {
        super.setTransactionService(transactionService);
        this.transactionService = transactionService;
    }

    @Override
    public Registry getRegistry()
    {
        Registry registry = registryCache.get();
        boolean isUpToDate = registryCache.isUpToDate();
        if (!isUpToDate && logger.isDebugEnabled())
        {
            logger.debug("Retrieved out of date web script registry for tenant " + tenantAdminService.getCurrentUserDomain());
        }
        return registry;
    }
    
    @Override
    public void onEnableTenant()
    {
        init();
    }
    
    @Override
    public void onDisableTenant()
    {
        destroy();
    }
    
    @Override
    public void init()
    {
        tenantAdminService.register(this);
        registryCache.refresh();
        
        super.reset();
    }
    
    @Override
    public void destroy()
    {
        registryCache.refresh();
    }
    
    @Override
    public void reset()
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Exception
            {
                destroy();
                init();

                return null;
            }
        }, true, false);
    }
}
