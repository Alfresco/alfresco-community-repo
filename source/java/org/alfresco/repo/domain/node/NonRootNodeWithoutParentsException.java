package org.alfresco.repo.domain.node;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * For internal use only: see ALF-13066 / ALF-12358
 */
/* package */ class NonRootNodeWithoutParentsException extends ConcurrencyFailureException
{
    private static final long serialVersionUID = 5920138218201628243L;
    
    private final Pair<Long, NodeRef> nodePair;
    
    public NonRootNodeWithoutParentsException(Pair<Long, NodeRef> nodePair)
    {
        super("Node without parents does not have root aspect: " + nodePair);
        this.nodePair = nodePair;
    }
    
    public Pair<Long, NodeRef> getNodePair()
    {
        return nodePair;
    }
}
