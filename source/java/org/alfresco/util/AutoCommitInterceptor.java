package org.alfresco.util;

import java.sql.Connection;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Can be used to wrap a datasource to ensure that the connections that it returns have auto-commit switched on.
 * 
 * @author dward
 */
public class AutoCommitInterceptor implements MethodInterceptor
{
    public Object invoke(MethodInvocation mi) throws Throwable
    {
        Connection result = (Connection) mi.proceed();
        result.setAutoCommit(true);
        return result;
    }
}
