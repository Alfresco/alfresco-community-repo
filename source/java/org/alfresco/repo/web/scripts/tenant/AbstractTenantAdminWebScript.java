package org.alfresco.repo.web.scripts.tenant;

import org.alfresco.repo.tenant.TenantAdminService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * @author janv
 * @since 4.2
 */
public abstract class AbstractTenantAdminWebScript extends DeclarativeWebScript
{
    protected static final String TENANT_DOMAIN             = "tenantDomain";
    protected static final String TENANT_ADMIN_PASSWORD     = "tenantAdminPassword";
    protected static final String TENANT_CONTENT_STORE_ROOT = "tenantContentStoreRoot";
    
    protected TenantAdminService tenantAdminService;
    
    public void setTenantAdminService(TenantAdminService tenantAdminService)
    {
        this.tenantAdminService = tenantAdminService;
    }
}
