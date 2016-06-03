package org.alfresco.repo.module;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.module.ModuleService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.util.PropertyCheck;
import org.springframework.context.ApplicationEvent;

/**
 * This component is responsible for ensuring that patches are applied
 * at the appropriate time.
 * 
 * @author Derek Hulley
 */
public class ModuleStarter extends AbstractLifecycleBean
{
    private TransactionService transactionService;
    private ModuleService moduleService;

    /**
     * 
     * @param transactionService        provides the retrying transaction
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param moduleService the service that will do the actual work.
     */
    public void setModuleService(ModuleService moduleService)
    {
        this.moduleService = moduleService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        PropertyCheck.mandatory(this, "moduleService", moduleService);
        final RetryingTransactionCallback<Object> startModulesCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                moduleService.startModules();
                return null;
            }
        };
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            @Override
            public Object doWork() throws Exception 
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(startModulesCallback, transactionService.isReadOnly());
                return null;
            }
        	
        }, AuthenticationUtil.getSystemUserName());       
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // NOOP
    }
}
