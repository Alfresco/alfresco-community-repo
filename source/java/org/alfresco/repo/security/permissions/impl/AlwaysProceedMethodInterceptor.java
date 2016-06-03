package org.alfresco.repo.security.permissions.impl;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class AlwaysProceedMethodInterceptor implements MethodInterceptor
{

    public AlwaysProceedMethodInterceptor()
    {
        super();
    }

    public Object invoke(MethodInvocation mi) throws Throwable
    {
        return mi.proceed();
    }

}
