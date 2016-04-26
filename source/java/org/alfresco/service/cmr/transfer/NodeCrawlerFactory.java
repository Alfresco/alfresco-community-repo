
package org.alfresco.service.cmr.transfer;

public interface NodeCrawlerFactory
{
    /**
     * Get a node crawler from the node crawler factory.
     * 
     * A new instance of a node crawler is returned each time this method is called.
     * 
     * @return a new node crawler.
     */
    NodeCrawler getNodeCrawler();
}
