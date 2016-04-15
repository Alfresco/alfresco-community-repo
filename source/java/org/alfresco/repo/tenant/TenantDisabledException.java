package org.alfresco.repo.tenant;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when an attempt is made to use a disabled tenant.
 * 
 * @author Matt Ward
 */
public class TenantDisabledException extends AlfrescoRuntimeException
{
    public static final String DISABLED_TENANT_MSG = "system.mt.disabled";
    private static final long serialVersionUID = 1L;
    private String tenantDomain;
    
    public TenantDisabledException(String tenantDomain)
    {
        super(DISABLED_TENANT_MSG, new Object[] { tenantDomain });
        this.tenantDomain = tenantDomain;
    }

    /**
     * @return the tenantDomain
     */
    public String getTenantDomain()
    {
        return tenantDomain;
    }
}
