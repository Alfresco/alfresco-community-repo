
package org.alfresco.repo.nodelocator;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A strategy for locating a {@link NodeRef} in the repository, given a source node and an arbitrary set of parameters.
 * 
 * @author Nick Smith
 * @since 4.0
 *
 */
public interface NodeLocator
{
    /**
     * Finds a {@link NodeRef} given a starting {@link NodeRef} and a
     * {@link Map} of parameters.
     * Returns <code>null</code> if the specified node could not be found.
     * 
     * @param source the starting point for locating a new node. The source node. Can be <code>null</code>.
     * @param params an arbitrary {@link Map} of parameters.Can be <code>null</code>.
     * @return the node to be found or <code>null</code>.
     */
    NodeRef getNode(NodeRef source, Map<String, Serializable> params);
    
    /**
     * A list containing the parmameter defintions for this {@link NodeLocator}.
     * 
     * @return  a list of parameter definitions
     */
    public List<ParameterDefinition> getParameterDefinitions();
}
