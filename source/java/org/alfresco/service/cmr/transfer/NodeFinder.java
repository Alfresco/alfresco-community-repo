
package org.alfresco.service.cmr.transfer;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author brian
 * 
 * NodeFinders find nodes related to the current node.
 */
public interface NodeFinder
{

    /**
     * @param thisNode The node to use as the base from which to find other nodes.
     * @return The found nodes
     */
    Set<NodeRef> findFrom(NodeRef thisNode);

}
