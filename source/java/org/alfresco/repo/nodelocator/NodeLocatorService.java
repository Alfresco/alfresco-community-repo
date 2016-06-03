
package org.alfresco.repo.nodelocator;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This service is responsible for locating {@link NodeRef}s in the repository using {@link NodeLocator} strategies.
 * 
 * @author Nick Smith
 * @since 4.0
 *
 */
@AlfrescoPublicApi
public interface NodeLocatorService
{
    /**
     * Locates and returns a {@link NodeRef} using the specified {@link NodeLocator}.
     * 
     * @param locatorName the name of the {@link NodeLocator} to use.
     * @param source the source node. Can be <code>null</code>.
     * @param params An arbitrary set of parameters. Can be <code>null</code>.
     * @return the node to be found or <code>null</code>.
     */
    NodeRef getNode(String locatorName, NodeRef source, Map<String, Serializable> params);
    
    void register(String locatorName, NodeLocator locator);
}
