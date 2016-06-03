
package org.alfresco.repo.workflow;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public interface NodeConverter
{
    List<?> convertNodes(Collection<NodeRef> nodes);

    Object convertNode(NodeRef node);
}
