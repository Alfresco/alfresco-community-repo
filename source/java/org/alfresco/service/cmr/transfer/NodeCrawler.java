package org.alfresco.service.cmr.transfer;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

public interface NodeCrawler
{

    public abstract Set<NodeRef> crawl(NodeRef... nodes);

    public abstract Set<NodeRef> crawl(Set<NodeRef> startingNodes);

    public abstract void setNodeFinders(NodeFinder... finders);

    public abstract void setNodeFilters(NodeFilter... filters);

}