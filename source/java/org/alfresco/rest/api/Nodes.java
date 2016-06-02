/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.UserInfo;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.servlet.FormData;

/**
 * File Folder (Nodes) API
 *
 * @author janv
 * @author Jamal Kaabi-Mofrad
 * @author Gethin James
 * @author steveglover
 */
public interface Nodes
{
    /**
     * Get the node representation for the given node.
     *
     * @param nodeId String
     * @return Node
     */
    Node getNode(String nodeId);
    
    /**
     * Get the document representation for the given node.
     *
     * @param nodeRef NodeRef
     * @return Document
     */
    Document getDocument(NodeRef nodeRef);
    
    /**
     * Get the folder representation for the given node.
     *
     * @param nodeRef NodeRef
     * @return Folder
     */
    Folder getFolder(NodeRef nodeRef);
    
    /**
     * Get the folder or document representation (as appropriate) for the given node.
     *
     * @param nodeId String nodeId or well-known alias, eg. "-root-" or "-my-"
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - incPrimaryParent
     * @return
     */
    Node getFolderOrDocument(String nodeId, Parameters parameters);

    Node getFolderOrDocumentFullInfo(NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, Parameters parameters, Map<String, UserInfo> mapUserInfo);

    /**
     * Get the folder or document representation (as appropriate) for the given node.
     *
     * @param nodeRef A real Node
     * @param parentNodeRef
     * @param nodeTypeQName
     * @param includeParam
     * @param mapUserInfo
     * @return
     */
    Node getFolderOrDocument(NodeRef nodeRef, NodeRef parentNodeRef, QName nodeTypeQName, List<String> includeParam, Map<String, UserInfo> mapUserInfo);

    /**
     * Get list of children of a parent folder.
     *
     * @param parentFolderNodeId String id of parent folder node or well-known alias, eg. "-root-" or "-my-"
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *        including:
     *        - filter, sort & paging params (where, orderBy, skipCount, maxItems)
     *        - incFiles, incFolders (both true by default)
     * @return a paged list of {@code org.alfresco.rest.api.model.Node} objects
     */
    CollectionWithPagingInfo<Node> listChildren(String parentFolderNodeId, Parameters parameters);
    
    /**
     * Delete the given node. Note: will cascade delete for a folder.
     *
     * @param nodeId String id of node (folder or document)
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     *                   - permanent (default false)
     */
    void deleteNode(String nodeId, Parameters parameters);

    /**
     * Create node - folder or (empty) file.
     *
     * @param parentFolderNodeId
     * @param nodeInfo
     * @param parameters
     * @return
     */
    Node createNode(String parentFolderNodeId, Node nodeInfo, Parameters parameters);

    /**
     * Move or Copy node
     *
     * @param sourceNodeId
     * @param parentFolderNodeId
     * @param name
     * @param parameters
     * @return
     */
    Node moveOrCopyNode(String sourceNodeId, String parentFolderNodeId, String name, Parameters parameters, boolean isCopy);

    /**
     * Update node meta-data.
     *
     * @param nodeId
     * @param entity
     * @param parameters
     * @return
     */
    Node updateNode(String nodeId, Node entity, Parameters parameters);

    /**
     * Download file content.
     *
     * @param fileNodeId
     * @param parameters
     * @param recordActivity true, if an activity post is required.
     * @return
     */
    BinaryResource getContent(String fileNodeId, Parameters parameters, boolean recordActivity);

    /**
     * Download file content.
     *
     * @param nodeRef        the content nodeRef
     * @param parameters
     * @param recordActivity true, if an activity post is required.
     * @return
     */
    BinaryResource getContent(NodeRef nodeRef, Parameters parameters, boolean recordActivity);

    /**
     * Uploads file content (updates existing node with new content).
     *
     * Note: may create a new version, depending on versioning behaviour.
     *
     * @param fileNodeId
     * @param contentInfo
     * @param stream
     * @param parameters
     * @return
     */
    Node updateContent(String fileNodeId, BasicContentInfo contentInfo, InputStream stream, Parameters parameters);

    /**
     * Uploads file content and meta-data into the repository.
     *
     * @param parentFolderNodeId String id of parent folder node or well-known alias, eg. "-root-" or "-my-"
     * @param formData           the {@link FormData}
     * @param parameters         the {@link Parameters} object to get the parameters passed into the request
     * @return {@code Node} if successful
     */
    Node upload(String parentFolderNodeId, FormData formData, Parameters parameters);
    

    NodeRef validateNode(StoreRef storeRef, String nodeId);
    NodeRef validateNode(String nodeId);
    NodeRef validateNode(NodeRef nodeRef);
    NodeRef validateOrLookupNode(String nodeId, String path);

    boolean nodeMatches(NodeRef nodeRef, Set<QName> expectedTypes, Set<QName> excludedTypes);

    /**
     * Determines whether the type of the given nodeRef is a sub-class of another class or not.
     *
     * @param nodeRef         source nodeRef
     * @param ofClassQName    the class to test against
     * @param validateNodeRef whether to validate the given source node or not
     * @return true if the type of the given nodeRef is a sub-class of another class, otherwise false
     */
    boolean isSubClass(NodeRef nodeRef, QName ofClassQName, boolean validateNodeRef);

    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     *
     * @param qnameStr Fully qualified or short-name QName string
     * @return QName
     */
    QName createQName(String qnameStr);

    /**
     * API Constants - query parameters, etc
     */

    String PATH_ROOT = "-root-";
    String PATH_MY = "-my-";
    String PATH_SHARED = "-shared-";

    String OP_CREATE = "create";
    String OP_DELETE = "delete";
    String OP_UPDATE = "update";

    String PARAM_RELATIVE_PATH = "relativePath";
    String PARAM_AUTO_RENAME = "autoRename";
    String PARAM_PERMANENT = "permanent";

    String PARAM_INCLUDE_PROPERTIES = "properties";
    String PARAM_INCLUDE_PATH = "path";
    String PARAM_INCLUDE_ASPECTNAMES = "aspectNames";
    String PARAM_INCLUDE_ISLINK = "isLink";
    String PARAM_INCLUDE_ALLOWABLEOPERATIONS = "allowableOperations";

    String PARAM_INCLUDE_ASSOCIATION = "association";

    String PARAM_ISFOLDER = "isFolder";
    String PARAM_ISFILE = "isFile";

    String PARAM_INCLUDE_SUBTYPES = "INCLUDESUBTYPES";

    String PARAM_NAME = "name";
    String PARAM_CREATEDAT = "createdAt";
    String PARAM_MODIFIEDAT = "modifiedAt";
    String PARAM_CREATEBYUSER = "createdByUser";
    String PARAM_MODIFIEDBYUSER = "modifiedByUser";
    String PARAM_MIMETYPE = "mimeType";
    String PARAM_SIZEINBYTES = "sizeInBytes";
    String PARAM_NODETYPE = "nodeType";

    String PARAM_VERSION_MAJOR = "majorVersion"; // true if major, false if minor
    String PARAM_VERSION_COMMENT = "comment";

    String PARAM_ISPRIMARY = "isPrimary";
}
