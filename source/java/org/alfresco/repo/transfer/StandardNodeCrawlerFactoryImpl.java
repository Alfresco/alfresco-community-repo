
package org.alfresco.repo.transfer;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.cmr.transfer.NodeCrawlerFactory;

public class StandardNodeCrawlerFactoryImpl implements NodeCrawlerFactory
{
    private ServiceRegistry serviceRegistry;
    
    public StandardNodeCrawlerFactoryImpl(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    public NodeCrawler getNodeCrawler()
    {
        return new StandardNodeCrawlerImpl(serviceRegistry);
    }
}
