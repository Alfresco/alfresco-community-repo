
package org.alfresco.repo.nodelocator;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This {@link NodeLocator} returns the source node.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class SelfNodeLocator extends AbstractNodeLocator
{
    public static final String NAME = "self";
    /**
    * {@inheritDoc}
    */
    @Override
    public NodeRef getNode(NodeRef source, Map<String, Serializable> params)
    {
        return source;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public String getName()
    {
        return NAME;
    }
}
