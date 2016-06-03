package org.alfresco.repo.tenant;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception thrown when an operation that requires 
 *  two things to be in the same tenant domain discovers
 *  that they belong to different ones.
 */
@SuppressWarnings("serial")
public class TenantDomainMismatchException extends AlfrescoRuntimeException
{
    private String tenantA;
    private String tenantB;
    
    public TenantDomainMismatchException(String tenantA, String tenantB)
    {
        super(
                "domain mismatch: expected = " + renderTenent(tenantA) + 
                ", actual = " + renderTenent(tenantB)
        );
                
        this.tenantA = tenantA;
        this.tenantB = tenantB;
    }
    private static String renderTenent(String tenant)
    {
        if(tenant == null)
            return "<none>";
        return tenant;
    }
    
    public String getTenantA()
    {
        return tenantA;
    }
    public String getTenantB()
    {
        return tenantB;
    }
}
