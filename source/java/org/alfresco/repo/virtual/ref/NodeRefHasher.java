
package org.alfresco.repo.virtual.ref;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Creates and looks up string-pair hash codes of {@link NodeRef}s.<br>
 */
public interface NodeRefHasher
{

    NodeRef lookup(Pair<String, String> hash);

    Pair<String, String> hash(NodeRef nodeRef);
}
