package org.alfresco.repo.transaction;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A this advice wrapper around the {@link RetryingTransactionHelper}.
 *
 * @author Derek Hulley
 */
public class RetryingTransactionAdvice implements MethodInterceptor
{
    private RetryingTransactionHelper txnHelper;
    private boolean readOnly;
    private boolean requiresNew;

    public RetryingTransactionAdvice()
    {
        readOnly = false;
        requiresNew = false;
    }

    public void setTxnHelper(RetryingTransactionHelper txnHelper)
    {
        this.txnHelper = txnHelper;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public void setRequiresNew(boolean requiresNew)
    {
        this.requiresNew = requiresNew;
    }

    public Object invoke(final MethodInvocation methodInvocation) throws Throwable
    {
        // Just call the helper
        RetryingTransactionCallback<Object> txnCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                return methodInvocation.proceed();
            }
        };
        return txnHelper.doInTransaction(txnCallback, readOnly, requiresNew);
    }
}
