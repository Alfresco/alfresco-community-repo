
package org.alfresco.repo.transfer;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A utility base class that simplifies the creation of new node filters.
 * 
 * When used in conjunction with the standard node crawler ({@link StandardNodeCrawlerImpl}),
 * node filters that extend this base class will automatically have the service registry
 * injected into them and their <code>init</code> operations invoked at the appropriate time. 
 * 
 * @author Brian
 * @since 3.4
 */
public abstract class AbstractNodeFilter implements NodeFilter
{
    protected ServiceRegistry serviceRegistry;

    public void init()
    {
        ParameterCheck.mandatory("serviceRegistry", serviceRegistry);
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

}
