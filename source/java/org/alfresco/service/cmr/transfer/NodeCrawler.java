package org.alfresco.service.cmr.transfer;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The NodeCrawler finds nodes related to an initial group of nodes for the 
 * transfer service.
 * <p>
 * During the crawl method the node finders find nodes related to the staring nodes 
 * and then the filters can exclude unwanted nodes.   For example you could use the finders 
 * to walk down a tree of nodes and exclude nodes of a certain type.  
 * 
 * @see org.alfresco.repo.transfer.StandardNodeCrawlerImpl
 *
 * @author Brian 
 * @since 3.3
 */
public interface NodeCrawler
{
    public abstract Set<NodeRef> crawl(NodeRef... nodes);

    public abstract Set<NodeRef> crawl(Set<NodeRef> startingNodes);

    public abstract void setNodeFinders(NodeFinder... finders);

    public abstract void setNodeFilters(NodeFilter... filters);

}