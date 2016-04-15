
package org.alfresco.repo.transfer;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;

/**
 * @author brian
 *
 */
public interface CorrespondingNodeResolver
{
    ResolvedParentChildPair resolveCorrespondingNode(NodeRef sourceNodeRef, ChildAssociationRef primaryAssoc,
            Path parentPath);
    
    static class ResolvedParentChildPair {
        public NodeRef resolvedParent;
        public NodeRef resolvedChild;
        
        public ResolvedParentChildPair(NodeRef parent, NodeRef child) {
            this.resolvedParent = parent;
            this.resolvedChild = child;
        }
    }
}