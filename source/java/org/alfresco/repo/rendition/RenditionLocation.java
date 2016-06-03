package org.alfresco.repo.rendition;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This simple interface defines a data class which identifies a rendition node, its parent and its name.
 */

public interface RenditionLocation
{
    /**
     * Gets the parent node of the rendition.
     * @return NodeRef
     */
    NodeRef getParentRef();

    /**
     * Gets the rendition node itself.
     * @return NodeRef
     */
    NodeRef getChildRef();

    /**
     * Gets the name of the rendition.
     * @return String
     */
    String getChildName();
}
