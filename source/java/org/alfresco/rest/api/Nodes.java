/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
     * @param nodeRef
     * @return
     */
    Node getNode(String nodeId);
    
    /**
     * Get the document representation for the given node.
     * @param nodeRef
     * @return
     */
    Document getDocument(NodeRef nodeRef);
    
    /**
     * Get the folder representation for the given node.
     * @param nodeRef
     * @return
     */
    Folder getFolder(NodeRef nodeRef);
}
