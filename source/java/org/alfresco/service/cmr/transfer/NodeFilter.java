
package org.alfresco.service.cmr.transfer;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author brian
 *
 * Examines the supplied node and indicates whether it has been accepted by the filter.
 */
public interface NodeFilter
{

    /**
     * Examines the supplied node and indicates whether it has been accepted by the filter.
     * @param thisNode NodeRef
     * @return true if the supplied node matches the criteria specified on this filter, and false
     * otherwise.
     */
    boolean accept(NodeRef thisNode);
}
