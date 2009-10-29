/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISTypesFilterEnum;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * Port for navigation service
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "NavigationServicePort", serviceName = "NavigationService", portName = "NavigationServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.NavigationServicePort")
public class DMNavigationServicePort extends DMAbstractServicePort implements NavigationServicePort
{
    private static final int EQUALS_CONDITION_VALUE = 0;
    private static final BigInteger FULL_DESCENDANTS_HIERARCHY_CONDITION = BigInteger.valueOf(-1l);

    private static final String FILTER_TOKENS_DELIMETER = ", ";

    private static final Pattern ORDER_BY_CLAUSE_MASK = Pattern.compile("^( )*([\\p{Alnum}_]+(( )+((ASC)|(DESC)))?){1}((,){1}( )*[\\p{Alnum}_]+(( )+((ASC)|(DESC)))?)*( )*$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Gets the private working copies of checked-out objects that the user is allowed to update.
     * 
     * @param parameters repositoryId: repository Id; folderID: folder Id; filter: property filter; includeAllowableActions; includeRelationships; maxItems: 0 = Unlimited;
     *        skipCount: 0 = start at beginning
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public CmisObjectListType getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);

        NodeRef folderRef = null;
        if ((folderId != null) && !folderId.equals(""))
        {
            folderRef = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);
        }

        @SuppressWarnings("unused")
        List<Pair<String, Boolean>> orderingFields = null;
        if ((orderBy != null) && !orderBy.equals(""))
        {
            orderingFields = checkAndParseOrderByClause(orderBy);
        }

        // TODO: Ordering functionality SHOULD be moved to getChildren service method
        NodeRef[] nodeRefs = cmisService.getCheckedOut(AuthenticationUtil.getFullyAuthenticatedUser(), folderRef, (folderRef == null));
        Cursor cursor = createCursor(nodeRefs.length, skipCount, maxItems);

        CmisObjectListType result = new CmisObjectListType();
        List<CmisObjectType> resultListing = result.getObjects();

        for (int index = cursor.getStartRow(); index <= cursor.getEndRow(); index++)
        {
            resultListing.add(createCmisObject(nodeRefs[index].toString(), propertyFilter, includeAllowableActions));
        }
        result.setHasMoreItems(new Boolean(cursor.getEndRow() < (nodeRefs.length - 1)));

        // TODO: includeAllowableActions, includeRelationships, renditions
        
        return result;
    }

    /**
     * Gets the list of child objects contained in the specified folder. Only the filter-selected properties associated with each object are returned. The content-streams of
     * documents are not returned.For returning a tree of objects of a certain depth, use {@link #getDescendants(GetDescendants parameters)}.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; type: DOCUMENTS, FOLDERS, POLICIES, ANY; filter: property filter; includeAllowableActions;
     *        includeRelationships; maxItems: 0 = Unlimited; skipCount: 0 = start at beginning
     * @return collection of CmisObjectType and boolean hasMoreItems
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public CmisObjectInFolderListType getChildren(String repositoryId, String folderId, String filter, String orderBy, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegments, BigInteger maxItems, BigInteger skipCount,
            CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);

        NodeRef folderNodeRef = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);

        @SuppressWarnings("unused")
        List<Pair<String, Boolean>> orderingFields = null;
        if ((orderBy != null) && !orderBy.equals(""))
        {
            orderingFields = checkAndParseOrderByClause(orderBy);
        }
      
        // TODO: Ordering functionality SHOULD be moved to getChildren service method
        NodeRef[] listing = cmisService.getChildren(folderNodeRef, CMISTypesFilterEnum.ANY);        

        CmisObjectInFolderListType result = new CmisObjectInFolderListType();        
        
        Cursor cursor = createCursor(listing.length, skipCount, maxItems);

        for (int index = cursor.getStartRow(); index <= cursor.getEndRow(); index++)
        {
            CmisObjectType cmisObject = createCmisObject(listing[index].toString(), propertyFilter, includeAllowableActions);
            CmisObjectInFolderType cmisObjectInFolder = new CmisObjectInFolderType();
            cmisObjectInFolder.setObject(cmisObject);
            if (includePathSegments != null && includePathSegments)
            {
                cmisObjectInFolder.setPathSegment(propertiesUtil.getProperty(listing[index], CMISDictionaryModel.PROP_NAME, ""));
            }
            result.getObjects().add(cmisObjectInFolder);
        }

        result.setHasMoreItems(cursor.getEndRow() < (listing.length - 1));
        result.setNumItems(BigInteger.valueOf(listing.length));

        // TODO: Process includeAllowableActions, includeRelationships, includeRenditions, includeACL
        return result;
    }

    /**
     * Gets the list of descendant objects contained at one or more levels in the tree rooted at the specified folder. Only the filter-selected properties associated with each
     * object are returned. The content-stream is not returned. For paging through the children (depth of 1) only use {@link #getChildren(GetChildren parameters)}.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; depth: 1 this folder only (Default), N folders deep, -1 for all levels; filter: property filter;
     *        includeAllowableActions; includeRelationships;
     * @return collection of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public List<CmisObjectInFolderContainerType> getDescendants(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegments, CmisExtensionType extension) throws CmisException
    {
        CmisObjectInFolderContainerType objectInFolderContainerType = getDescedantsTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePathSegments, CMISTypesFilterEnum.ANY);
        return objectInFolderContainerType.getChildren();
    }

    /**
     * Gets the list of descendant objects contained at one or more levels in the tree rooted at the specified folder. Only the filter-selected properties associated with each
     * object are returned. The content-stream is not returned. For paging through the children (depth of 1) only use {@link #getChildren(GetChildren parameters)}.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; depth: 1 this folder only (Default), N folders deep, -1 for all levels; filter: property filter;
     *        includeAllowableActions; includeRelationships;
     * @return collection of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public List<CmisObjectInFolderContainerType> getFolderTree(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegments, CmisExtensionType extension) throws CmisException
    {
        CmisObjectInFolderContainerType objectInFolderContainerType = getDescedantsTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePathSegments, CMISTypesFilterEnum.FOLDERS);
        return objectInFolderContainerType.getChildren();
    }

    /**
     * Returns the parent folder object, and optionally all ancestor folder objects, above a specified folder object.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; filter: property filter; includeAllowableActions; includeRelationships; returnToRoot: If false, return
     *        only the immediate parent of the folder. If true, return an ordered list of all ancestor folders from the specified folder to the root folder
     * @return collection of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public CmisObjectType getFolderParent(String repositoryId, String folderId, String filter, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);

        if ((filter != null) && !filter.equals("") && !filter.equals("*"))
        {
            if (!filter.contains(CMISDictionaryModel.PROP_PARENT_ID))
            {
                filter = CMISDictionaryModel.PROP_PARENT_ID + FILTER_TOKENS_DELIMETER + filter;
            }

            if (!filter.contains(CMISDictionaryModel.PROP_OBJECT_ID))
            {
                filter = CMISDictionaryModel.PROP_OBJECT_ID + FILTER_TOKENS_DELIMETER + filter;
            }
        }

        PropertyFilter propertyFilter = createPropertyFilter(filter);

        NodeRef parentRef = receiveParent(folderId);
        CmisObjectType result = createCmisObject(parentRef, propertyFilter, false);

        return result;
    }

    /**
     * Returns the parent folders for the specified non-folder, fileable object.
     * 
     * @param parameters repositoryId: repository Id; objectId: object Id; filter: property filter; includeAllowableActions; includeRelationships;
     * @return collection of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT,
     *         FILTER_NOT_VALID)
     */
    public List<CmisObjectParentsType> getObjectParents(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includeRelativePathSegment, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);

        NodeRef childNode = (NodeRef) cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT);
        List<NodeRef> parents = receiveObjectParents(childNode);

        List<CmisObjectParentsType> result = new ArrayList<CmisObjectParentsType>();
        String relativePathSegment = propertiesUtil.getProperty(childNode, CMISDictionaryModel.PROP_NAME, "");
        for (NodeRef objectNodeRef : parents)
        {
            CmisObjectType cmisObject = createCmisObject(objectNodeRef, propertyFilter, includeAllowableActions);
            //TODO: includeRelationship, renditions
            CmisObjectParentsType cmisObjectParentsType = new CmisObjectParentsType();
            cmisObjectParentsType.setObject(cmisObject);
            if (includeRelativePathSegment != null && includeRelativePathSegment)
            {
                cmisObjectParentsType.setRelativePathSegment(relativePathSegment);
            }
            result.add(cmisObjectParentsType);

        }
        return result;
    }
    
    private CmisObjectInFolderContainerType getDescedantsTree(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegments,
            CMISTypesFilterEnum types) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        depth = (depth == null) ? (BigInteger.ONE.add(BigInteger.ONE)) : depth;
        depth = depth.equals(BigInteger.valueOf(-1)) ? BigInteger.valueOf(Integer.MAX_VALUE) : depth;
        long maxDepth = depth.longValue(); 
        checkDepthParameter(depth);
        
        NodeRef folderNodeRef = cmisObjectsUtils.getIdentifierInstance(folderId, AlfrescoObjectType.FOLDER_OBJECT);
        Stack<RecursiveElement> descedantsStack = new Stack<RecursiveElement>();        
        CmisObjectInFolderContainerType objectInFolderContainer = createObjectInFolderContainer(folderNodeRef, propertyFilter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegments);
        NodeRef[] children = cmisService.getChildren(folderNodeRef, types);
        for (NodeRef childRef : children)
        {
            descedantsStack.push(new RecursiveElement(objectInFolderContainer, 1, childRef));
        }        
        while (!descedantsStack.isEmpty())
        {
            RecursiveElement element = descedantsStack.pop();
            CmisObjectInFolderContainerType currentContainer = createObjectInFolderContainer(element.getCurrentNodeRef(), propertyFilter, includeAllowableActions, includeRelationships,
                    renditionFilter, includePathSegments);
            element.getParentContainerType().getChildren().add(currentContainer);
            if (element.getDepth() <= maxDepth)
            {
                children = cmisService.getChildren(element.getCurrentNodeRef(), types);
                if (children != null)
                {
                    for (NodeRef childRef : children)
                    {
                        descedantsStack.push(new RecursiveElement(currentContainer, element.getDepth() + 1, childRef));
                    }
                }
            }
        }
        return objectInFolderContainer;
    }
    
    private CmisObjectInFolderContainerType createObjectInFolderContainer(NodeRef nodeRef, PropertyFilter filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegments) throws CmisException
    {
        includeAllowableActions = includeAllowableActions == null ? Boolean.FALSE : includeAllowableActions;
        CmisObjectType cmisObject = createCmisObject(nodeRef, filter, includeAllowableActions);
        //TODO: add relationships and renditions
        
        CmisObjectInFolderType objectInFolderType = new CmisObjectInFolderType();
        objectInFolderType.setObject(cmisObject);
        if (includePathSegments != null && includePathSegments)
        {
            String path = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_NAME, "");
            objectInFolderType.setPathSegment(path);
        }
        CmisObjectInFolderContainerType result = new CmisObjectInFolderContainerType();
        result.setObjectInFolder(objectInFolderType);
        return result;
    }

    private List<Pair<String, Boolean>> checkAndParseOrderByClause(String orderByClause) throws CmisException
    {
        List<Pair<String, Boolean>> result = new LinkedList<Pair<String, Boolean>>();

        if (!ORDER_BY_CLAUSE_MASK.matcher(orderByClause).matches())
        {
            throw cmisObjectsUtils.createCmisException(("\"" + orderByClause + "\" Order By Clause is invalid!"), EnumServiceException.INVALID_ARGUMENT);
        }

        for (String token : orderByClause.split(","))
        {
            token = token.trim();

            String[] direction = token.split(" ");
            String fieldName = direction[0];

            result.add(new Pair<String, Boolean>(fieldName, ((direction.length == 1) ? (true) : (direction[direction.length - 1].toLowerCase().equals("asc")))));
        }

        return result;
    }

    private NodeRef receiveParent(String targetChildIdentifier) throws CmisException
    {
        if (targetChildIdentifier.equals(cmisService.getDefaultRootNodeRef().toString()))
        {
            return null;
        }
        return receiveNextParentNodeReference((NodeRef) cmisObjectsUtils.getIdentifierInstance(targetChildIdentifier, AlfrescoObjectType.FOLDER_OBJECT), new ArrayList<NodeRef>());
    }
        
    private void checkDepthParameter(BigInteger depth) throws CmisException
    {
        if (depth.equals(BigInteger.ZERO) || (depth.compareTo(FULL_DESCENDANTS_HIERARCHY_CONDITION) < EQUALS_CONDITION_VALUE))
        {
            throw cmisObjectsUtils.createCmisException("The specified descendants depth is not valid. Valid depth values are: -1 (full hierarchy), N > 0",
                    EnumServiceException.INVALID_ARGUMENT);
        }
    }

    private NodeRef receiveNextParentNodeReference(NodeRef currentParent, List<NodeRef> parents)
    {
        currentParent = nodeService.getPrimaryParent(currentParent).getParentRef();
        if (currentParent != null)
        {
            parents.add(currentParent);
        }
        return currentParent;
    }

    private List<NodeRef> receiveObjectParents(NodeRef objectId) throws CmisException
    {
        List<NodeRef> parents = new LinkedList<NodeRef>();
        for (ChildAssociationRef childParentAssociation : nodeService.getParentAssocs(objectId))
        {
            parents.add(childParentAssociation.getParentRef());
        }
        return parents;
    }
    
    private class RecursiveElement
    {
        private CmisObjectInFolderContainerType parentContainerType;
        private int depth;
        private NodeRef currentNodeRef;
        
        public RecursiveElement(CmisObjectInFolderContainerType parentContainerType, int depth, NodeRef currentNodeRef)
        {
            this.parentContainerType = parentContainerType;
            this.depth = depth;
            this.currentNodeRef = currentNodeRef;
        }

        public CmisObjectInFolderContainerType getParentContainerType()
        {
            return parentContainerType;
        }

        public int getDepth()
        {
            return depth;
        }

        public NodeRef getCurrentNodeRef()
        {
            return currentNodeRef;
        }
    }
}
