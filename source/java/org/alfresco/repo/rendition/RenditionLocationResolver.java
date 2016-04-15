
package org.alfresco.repo.rendition;

import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This interface defines a type which can be used to resolve the location of rendition nodes.
 */
public interface RenditionLocationResolver
{

    /**
     * 
     * @param sourceNode NodeRef
     * @param definition RenditionDefinition
     * @param tempRenditionLocation NodeRef
     * @return RenditionLocation
     */
    RenditionLocation getRenditionLocation(NodeRef sourceNode, RenditionDefinition definition, NodeRef tempRenditionLocation);
}