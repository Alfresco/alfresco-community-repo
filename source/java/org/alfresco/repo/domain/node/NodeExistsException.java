package org.alfresco.repo.domain.node;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Exception generated when a live node already exists for a given {@link NodeRef node reference}.
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class NodeExistsException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -2122408334209855947L;
    
    private final Pair<Long, NodeRef> nodePair;

    public NodeExistsException(Pair<Long, NodeRef> nodePair, Throwable e)
    {
        super("Node already exists: " + nodePair, e);
        this.nodePair = nodePair;
    }

    public Pair<Long, NodeRef> getNodePair()
    {
        return nodePair;
    }
}
