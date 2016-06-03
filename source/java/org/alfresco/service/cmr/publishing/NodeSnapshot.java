
package org.alfresco.service.cmr.publishing;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public interface NodeSnapshot
{
    /**
     * Retrieve the identifier of the node of which this is a snapshot
     * @return The NodeRef object that identifies the node
     */
    NodeRef getNodeRef();
    
    /**
     * The property values assigned to the node at the moment the snapshot was taken.
     * @return A map that associates property names to property values for the node.
     */
    Map<QName, Serializable> getProperties();

    /**
     * Retrieve the type of the node at the moment the snapshot was taken.
     * @return The QName that identifies the type of the node
     */
    QName getType();
    
    /**
     * Retrieve all the aspects that were applied to the node at the moment the snapshot was taken
     * @return A set of QName objects, each identifying an aspect that is applied to the node
     */
    Set<QName> getAspects();
    
    /**
     * @return the version of the node when the snapshot was taken.
     */
    String getVersion();
}
