/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.repo.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.alfresco.service.cmr.transfer.NodeFinder;
import org.alfresco.service.cmr.transfer.TransferService;

/**
 * This class can be used to build a set of node references from a given starting point. The caller can provide a list
 * of {@link NodeFinder} objects and a list of {@link NodeFilter} objects. Starting with the nodes supplied by the
 * caller, the crawler uses the NodeFinder objects to find other nodes. Each node that is found is then passed to the
 * NodeFilter objects to determine whether it should be included or ignored. Any included nodes are then fed back into
 * the NodeFinder objects to continue the crawl. This class was originally written to assist users of the
 * {@link TransferService} in combination with the {@link ChildAssociatedNodeFinder} and the {@link ContentClassFilter}.
 * 
 * @author brian
 * 
 */
public class StandardNodeCrawlerImpl implements NodeCrawler
{
    private ServiceRegistry serviceRegistry;
    private List<NodeFinder> nodeFinders = new ArrayList<NodeFinder>();
    private List<NodeFilter> nodeFilters = new ArrayList<NodeFilter>();

    /**
     * 
     */
    public StandardNodeCrawlerImpl()
    {
        super();
    }

    /**
     * @param serviceRegistry ServiceRegistry
     */
    public StandardNodeCrawlerImpl(ServiceRegistry serviceRegistry)
    {
        super();
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * @param serviceRegistry
     *            the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transfer.NodeCrawler#crawl(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Set<NodeRef> crawl(NodeRef... nodes)
    {
        return crawl(new HashSet<NodeRef>(Arrays.asList(nodes)));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transfer.NodeCrawler#crawl(java.util.Set)
     */
    public synchronized Set<NodeRef> crawl(Set<NodeRef> startingNodes)
    {
        init();
        Queue<NodeRef> nodesToProcess = new LinkedList<NodeRef>();
        nodesToProcess.addAll(startingNodes);
        Set<NodeRef> resultingNodeSet = new HashSet<NodeRef>(89);
        Set<NodeRef> processedNodes = new HashSet<NodeRef>(89);

        // Do we have any more nodes to process?
        while (nodesToProcess.peek() != null)
        {
            // Yes, we do. Read the next noderef from the queue.
            NodeRef thisNode = nodesToProcess.poll();
            // Check that we haven't already processed it. Skip it if we have, process it if we haven't
            if (!processedNodes.contains(thisNode))
            {
                // Record the fact that we're processing this node
                processedNodes.add(thisNode);
                // We check this node against any filters that are in place (the nodes
                // that we were given to start with are always processed)
                if (startingNodes.contains(thisNode) || includeNode(thisNode))
                {
                    resultingNodeSet.add(thisNode);
                    Set<NodeRef> subsequentNodes = findSubsequentNodes(thisNode);
                    for (NodeRef node : subsequentNodes)
                    {
                        nodesToProcess.add(node);
                    }
                }
            }
        }
        return resultingNodeSet;
    }

    /**
     * 
     */
    private void init()
    {
        for (NodeFinder nodeFinder : this.nodeFinders)
        {
            if (nodeFinder instanceof AbstractNodeFinder)
            {
                ((AbstractNodeFinder)nodeFinder).setServiceRegistry(serviceRegistry);
                ((AbstractNodeFinder)nodeFinder).init();
            }
        }
        for (NodeFilter nodeFilter : this.nodeFilters)
        {
            if (nodeFilter instanceof AbstractNodeFilter)
            {
                ((AbstractNodeFilter)nodeFilter).setServiceRegistry(serviceRegistry);
                ((AbstractNodeFilter)nodeFilter).init();
            }
        }
    }

    /**
     * @param thisNode NodeRef
     * @return Set<NodeRef>
     */
    private Set<NodeRef> findSubsequentNodes(NodeRef thisNode)
    {
        Set<NodeRef> foundNodes = new HashSet<NodeRef>(89);
        for (NodeFinder finder : nodeFinders)
        {
            foundNodes.addAll(finder.findFrom(thisNode));
        }
        return foundNodes;
    }

    /**
     * @param thisNode NodeRef
     * @return boolean
     */
    private boolean includeNode(NodeRef thisNode)
    {
        boolean include = true;
        for (int i = 0; include && (i < nodeFilters.size()); ++i)
        {
            include &= nodeFilters.get(i).accept(thisNode);
        }
        return include;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transfer.NodeCrawler#setNodeFinders(org.alfresco.service.cmr.transfer.NodeFinder)
     */
    public synchronized void setNodeFinders(NodeFinder... finders)
    {
        nodeFinders = Arrays.asList(finders);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transfer.NodeCrawler#setNodeFilters(org.alfresco.service.cmr.transfer.NodeFilter)
     */
    public synchronized void setNodeFilters(NodeFilter... filters)
    {
        nodeFilters = Arrays.asList(filters);
    }
}
