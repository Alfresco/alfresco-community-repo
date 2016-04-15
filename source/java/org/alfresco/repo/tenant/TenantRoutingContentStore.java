package org.alfresco.repo.tenant;


/**
 * Content Store that supports tenant routing, if multi-tenancy is enabled.
 */
public interface TenantRoutingContentStore extends TenantDeployer
{
    public String getRootLocation();
}
