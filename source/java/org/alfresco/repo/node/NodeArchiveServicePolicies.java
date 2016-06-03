package org.alfresco.repo.node;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Node archive service policies
 * 
 * @author Viachaslau Tsikhanovich
 */
public interface NodeArchiveServicePolicies 
{
    public interface BeforePurgeNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforePurgeNode");
        /**
         * Called before a node is purged (deleted from archive).
         * 
         * @param nodeRef   the node reference
         */
        public void beforePurgeNode(NodeRef nodeRef);
    }
}
