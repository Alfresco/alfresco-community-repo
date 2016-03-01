 
package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.repo.action.RuntimeActionService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.aop.framework.ProxyFactoryBean;

/**
 * RM action proxy factory bean.
 *
 * @author Roy Wetherall
 */
public class RMActionProxyFactoryBean extends ProxyFactoryBean
{
    private static final long serialVersionUID = 539749542853266449L;

    /** Runtime action service */
    protected RuntimeActionService runtimeActionService;

    /** Records management action service */
    protected RecordsManagementActionService recordsManagementActionService;

    /** Records management audit service */
    protected RecordsManagementAuditService recordsManagementAuditService;

    /** transaction service */
    private TransactionService transactionService;

    /**
     * Set action service
     *
     * @param actionService
     */
    public void setRuntimeActionService(RuntimeActionService runtimeActionService)
    {
        this.runtimeActionService = runtimeActionService;
    }

    /**
     * Set records management service
     *
     * @param recordsManagementActionService
     */
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
    }

    /**
     * Set records management service
     *
     * @param recordsManagementAuditService
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService recordsManagementAuditService)
    {
        this.recordsManagementAuditService = recordsManagementAuditService;
    }

    /**
     * @param transactionService    transaction service
     * @since 2.4.a
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Register the action
     */
    public void registerAction()
    {
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
                {
                    public Void execute() throws Throwable
                    {
                        RecordsManagementAction action = (RecordsManagementAction)getObject();
                        recordsManagementActionService.register(action);

                        return null;
                    }
                });

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());

    }
}
