package org.alfresco.repo.tenant;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @since 4.2
 */
public class RunAsTenantInterceptor implements MethodInterceptor
{
    public enum TENANT_TYPE
    {
        Default,
        RealUser
    }
    
    private TENANT_TYPE tenantType;
    
    public RunAsTenantInterceptor(TENANT_TYPE tenantType)
    {
        this.tenantType = tenantType;
    }
    
    @Override
    public Object invoke(final MethodInvocation mi) throws Throwable
    {
        TenantRunAsWork<Object> runAs = new TenantRunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                try
                {
                    return mi.proceed();
                }
                catch(Throwable e)
                {
                    e.printStackTrace();
                    
                    // Re-throw the exception
                    if (e instanceof RuntimeException)
                    {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException("Failed to execute in RunAsTenant context", e);
                }
            }
        };
        
        if (tenantType == TENANT_TYPE.Default)
        {
            return TenantUtil.runAsDefaultTenant(runAs);
        }
        else
        {
            // run as tenant using current tenant context (if no tenant context then it is implied as the primary tenant, based on username)
            return TenantUtil.runAsTenant(runAs, AuthenticationUtil.getUserTenant(AuthenticationUtil.getFullyAuthenticatedUser()).getSecond());
        }
    }
}
