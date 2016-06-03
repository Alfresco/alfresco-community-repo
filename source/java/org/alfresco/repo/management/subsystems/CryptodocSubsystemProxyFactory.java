package org.alfresco.repo.management.subsystems;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.alfresco.repo.content.ContentStoreCaps;
import org.alfresco.repo.tenant.TenantDeployer;
import org.alfresco.repo.tenant.TenantRoutingContentStore;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.DefaultPointcutAdvisor;


public class CryptodocSubsystemProxyFactory extends SubsystemProxyFactory
{
    private static final long serialVersionUID = 1L;

    public CryptodocSubsystemProxyFactory()
    {
        super();
        addAdvisor(0, new DefaultPointcutAdvisor(new MethodInterceptor()
        {
            public Object invoke(MethodInvocation mi) throws Throwable
            {
                Method method = mi.getMethod();
                try
                {
                    switch (method.getName())
                    {
                        case "getTenantRoutingContentStore":
                            return getTenantRoutingContentStore(mi);
                        case "getTenantDeployer":
                            return getTenantDeployer(mi);
                        default:
                            return mi.proceed();
                    }
                }
                catch (InvocationTargetException e)
                {
                    // Unwrap invocation target exceptions
                    throw e.getTargetException();
                }
            }

            private TenantDeployer getTenantDeployer(MethodInvocation mi)
            {
                Object bean = locateBean(mi);
                if (bean instanceof TenantDeployer)
                {
                    return (TenantDeployer) bean;
                }
                else
                {
                    if (bean instanceof ContentStoreCaps)
                    {
                        Object ten = ((ContentStoreCaps)bean).getTenantDeployer();
                        if (ten instanceof TenantDeployer)
                        {
                            return (TenantDeployer) ten;
                        }
                    }
                }
                return null;
            }

            private TenantRoutingContentStore getTenantRoutingContentStore(MethodInvocation mi)
            {
                Object bean = locateBean(mi);
                if (bean instanceof TenantRoutingContentStore)
                {
                    return (TenantRoutingContentStore) bean;
                }
                else
                {
                    if (bean instanceof ContentStoreCaps)
                    {
                        Object ten = ((ContentStoreCaps)bean).getTenantRoutingContentStore();
                        if (ten instanceof TenantRoutingContentStore)
                        {
                            return (TenantRoutingContentStore) ten;
                        }
                    }
                }
                return null;
            }
        }));
    }
}
