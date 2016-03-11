package org.alfresco.module.org_alfresco_module_rm.job;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;

/**
 * Records management job executer base class.
 *
 * @author Roy Wetherall
 */
public abstract class RecordsManagementJobExecuter implements RecordsManagementModel
{
    /** Retrying transaction helper */
    protected RetryingTransactionHelper retryingTransactionHelper;

    /** Repository state helper */
    protected RepositoryState repositoryState;

    /**
     * @param retryingTransactionHelper retrying transaction helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * @param repositoryState   repository state helper component
     */
    public void setRepositoryState(RepositoryState repositoryState)
    {
        this.repositoryState = repositoryState;
    }

    /**
     * Executes the jobs work.
     */
    public void execute()
    {
        // jobs not allowed to execute unless bootstrap is complete
        if (!repositoryState.isBootstrapping())
        {
            retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute()
                {
                    executeImpl();

                    return null;
                }
            }, false, true);
        }
    }

    /**
     * Jobs work implementation.
     */
    public abstract void executeImpl();
}
