/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

import java.io.InputStream;
import java.util.Set;

import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * @author steveglover
 * @author janv
 */
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
    
    /**
     * Get the folder or document representation (as appropriate) for the given node.
     * @param nodeId String nodeId or well-known alias, eg. "-root-" or "-my-"
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - incPrimaryParent
     * @return
     */
    Node getFolderOrDocument(String nodeId, Parameters parameters);
    
    /**
     * Get list of children of a parent folder.
     * @param parentFolderNodeId String id of parent folder node or well-known alias, eg. "-root-" or "-my-"
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort & paging params (where, orderBy, skipCount, maxItems)
     *        - incFiles, incFolders (both true by default)
     * @return a paged list of {@code org.alfresco.rest.api.model.Node} objects
     */
    CollectionWithPagingInfo<Node> getChildren(String parentFolderNodeId, Parameters parameters);
    
    /**
     * Delete the given node. Note: will cascade delete for a folder.
     * @param nodeId String id of node (folder or document)
     */
    void deleteNode(String nodeId);

    /**
     * Create node(s) - folder or (empty) file
     *
     * @param parentFolderNodeId
     * @param nodeInfo
     * @param parameters
     * @return
     */
    Node createNode(String parentFolderNodeId, Node nodeInfo, Parameters parameters);

    /**
     *
     * @param nodeId
     * @param entity
     * @param parameters
     * @return
     */
    Node updateNode(String nodeId, Node entity, Parameters parameters);

    // TODO update REST fwk - to optionally support "attachment" (Content-Disposition) header
    BinaryResource getContent(String fileNodeId, Parameters parameters);

    // TODO update REST fwk - to optionally support return of json
    void updateContent(String fileNodeId, BasicContentInfo contentInfo, InputStream stream, Parameters parameters);
}
