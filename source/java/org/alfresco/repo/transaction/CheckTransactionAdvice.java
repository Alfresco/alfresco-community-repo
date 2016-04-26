package org.alfresco.repo.transaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A wrapper that checks that a transaction is present.
 *
 * @author Derek Hulley
 * @since 2.2.1
 */
public class CheckTransactionAdvice implements MethodInterceptor
{
    public CheckTransactionAdvice()
    {
    }
    public Object invoke(final MethodInvocation methodInvocation) throws Throwable
    {
        // Just check for any transaction
        if (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_NONE)
        {
            String methodName = methodInvocation.getMethod().getName();
            String className = methodInvocation.getMethod().getDeclaringClass().getName();
            throw new AlfrescoRuntimeException(
                    "A transaction has not be started for method '" + methodName + "' on " + className);
        }
        return methodInvocation.proceed();
    }
}
