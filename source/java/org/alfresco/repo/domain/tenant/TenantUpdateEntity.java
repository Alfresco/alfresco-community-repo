package org.alfresco.repo.domain.tenant;


/**
 * Entity for <b>alf_tenant</b> update.
 * 
 * @author janv
 * @since 4.0 (thor)
 */
public class TenantUpdateEntity extends TenantEntity
{
    public TenantUpdateEntity(String tenantDomain)
    {
        super(tenantDomain);
    }
    
    @Override
    public void  setTenantDomain(String tenantDomain)
    {
        throw new UnsupportedOperationException("Cannot update tenantDomain: "+getTenantDomain());
    }
}
