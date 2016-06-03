package org.alfresco.repo.security.person;

import org.alfresco.repo.node.NodeServicePolicies;

/**
 * Interface for home folder providers.
 * 
 * @deprecated 
 * Depreciated since 4.0. {@link HomeFolderProvider2} should now be used.
 * 
 * @author Andy Hind
 */
public interface HomeFolderProvider extends NodeServicePolicies.OnCreateNodePolicy
{
    /**
     * Get the name of the provider.
     * 
     * @return String
     */
    public String getName();
}
