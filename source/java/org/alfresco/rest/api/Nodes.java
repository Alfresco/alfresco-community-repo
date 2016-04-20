package org.alfresco.rest.api;

import java.util.Set;

import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.Node;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

public interface Nodes
{
	NodeRef validateNode(StoreRef storeRef, String nodeId);
	NodeRef validateNode(String nodeId);
	NodeRef validateNode(NodeRef nodeRef);
	boolean nodeMatches(NodeRef nodeRef, Set<QName> expectedTypes, Set<QName> excludedTypes);
	
    /**
     * Get the node representation for the given node.
     * @param nodeId String
     * @return Node
     */
    Node getNode(String nodeId);
    
    /**
     * Get the document representation for the given node.
     * @param nodeRef NodeRef
     * @return Document
     */
    Document getDocument(NodeRef nodeRef);
    
    /**
     * Get the folder representation for the given node.
     * @param nodeRef NodeRef
     * @return Folder
     */
    Folder getFolder(NodeRef nodeRef);
}
