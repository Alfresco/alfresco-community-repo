/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISTypesFilterEnum;
import org.alfresco.cmis.PropertyFilter;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.cmr.model.FileInfo;
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

        try
        {
            NodeRef folderRef = null;
            if ((folderId != null) && !folderId.equals(""))
            {
                folderRef = cmisService.getFolder(folderId);
            }

            NodeRef[] nodeRefs = cmisService.getCheckedOut(AuthenticationUtil.getFullyAuthenticatedUser(), folderRef,
                    (folderRef == null), orderBy);
            Cursor cursor = createCursor(nodeRefs.length, skipCount, maxItems);

            CmisObjectListType result = new CmisObjectListType();
            List<CmisObjectType> resultListing = result.getObjects();

            for (int index = cursor.getStartRow(); index <= cursor.getEndRow(); index++)
            {
                resultListing.add(createCmisObject(nodeRefs[index], propertyFilter, includeRelationships,
                        includeAllowableActions, renditionFilter));
            }
            result.setHasMoreItems(new Boolean(cursor.getEndRow() < (nodeRefs.length - 1)));
            return result;
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
    public CmisObjectInFolderListType getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegments, BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension)
            throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        
        try
        {
            NodeRef folderNodeRef = cmisService.getFolder(folderId);
            
            PagingResults<FileInfo> pageOfNodeInfos = cmisService.getChildren(folderNodeRef, CMISTypesFilterEnum.ANY, maxItems, skipCount, orderBy);
            
            int pageCnt = pageOfNodeInfos.getPage().size();
            NodeRef[] children = new NodeRef[pageCnt];
            
            int idx = 0;
            for (FileInfo child : pageOfNodeInfos.getPage())
            {
                children[idx] = child.getNodeRef();
                idx++;
            }
            
            CmisObjectInFolderListType result = new CmisObjectInFolderListType();
            
            // has more ?
            result.setHasMoreItems(pageOfNodeInfos.hasMoreItems());
            
            // total count ?
            Pair<Integer, Integer> totalCounts = pageOfNodeInfos.getTotalResultCount();
            if (totalCounts != null)
            {
                Integer totalCountLower = totalCounts.getFirst();
                Integer totalCountUpper = totalCounts.getSecond();
                if ((totalCountLower != null) && (totalCountLower.equals(totalCountUpper)))
                {
                    result.setNumItems(BigInteger.valueOf(totalCountLower));
                }
            }
            
            for (int index = 0; index < pageCnt; index++)
            {
                CmisObjectType cmisObject = createCmisObject(children[index], propertyFilter, includeRelationships,
                        includeAllowableActions, renditionFilter);
                CmisObjectInFolderType cmisObjectInFolder = new CmisObjectInFolderType();
                cmisObjectInFolder.setObject(cmisObject);
                if (includePathSegments != null && includePathSegments)
                {
                    cmisObjectInFolder.setPathSegment(propertiesUtil.getProperty(children[index],
                            CMISDictionaryModel.PROP_NAME, ""));
                }
                result.getObjects().add(cmisObjectInFolder);
            }
            
            // TODO: Process includeRelationships, includeACL
            return result;
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
        CmisObjectInFolderContainerType objectInFolderContainerType = getDescendantsTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships,
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
        CmisObjectInFolderContainerType objectInFolderContainerType = getDescendantsTree(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships,
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
        NodeRef parentRef;
        try
        {
            parentRef = cmisService.getFolderParent(folderId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        
        CmisObjectType result = createCmisObject(parentRef, propertyFilter, null, false, null);
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
    public List<CmisObjectParentsType> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);

        try
        {
            NodeRef childNode = (NodeRef) cmisService.getReadableObject(objectId, NodeRef.class);
            List<NodeRef> parents = receiveObjectParents(childNode);

            List<CmisObjectParentsType> result = new ArrayList<CmisObjectParentsType>();
            String relativePathSegment = propertiesUtil.getProperty(childNode, CMISDictionaryModel.PROP_NAME, "");
            for (NodeRef objectNodeRef : parents)
            {
                CmisObjectType cmisObject = createCmisObject(objectNodeRef, propertyFilter, includeRelationships,
                        includeAllowableActions, renditionFilter);
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
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    private CmisObjectInFolderContainerType getDescendantsTree(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegments, CMISTypesFilterEnum types) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        depth = (depth == null) ? (BigInteger.ONE.add(BigInteger.ONE)) : depth;
        depth = depth.equals(BigInteger.valueOf(-1)) ? BigInteger.valueOf(Integer.MAX_VALUE) : depth;
        long maxDepth = depth.longValue();
        checkDepthParameter(depth);

        try
        {
            NodeRef folderNodeRef = cmisService.getFolder(folderId);
            Stack<RecursiveElement> descedantsStack = new Stack<RecursiveElement>();
            CmisObjectInFolderContainerType objectInFolderContainer = createObjectInFolderContainer(folderNodeRef,
                    propertyFilter, includeAllowableActions, includeRelationships, renditionFilter, includePathSegments);
            NodeRef[] children = cmisService.getChildren(folderNodeRef, types, null);
            for (NodeRef childRef : children)
            {
                descedantsStack.push(new RecursiveElement(objectInFolderContainer, 1, childRef));
            }
            while (!descedantsStack.isEmpty())
            {
                RecursiveElement element = descedantsStack.pop();
                CmisObjectInFolderContainerType currentContainer = createObjectInFolderContainer(element
                        .getCurrentNodeRef(), propertyFilter, includeAllowableActions, includeRelationships,
                        renditionFilter, includePathSegments);
                element.getParentContainerType().getChildren().add(currentContainer);
                if (element.getDepth() <= maxDepth)
                {
                    children = cmisService.getChildren(element.getCurrentNodeRef(), types, null);
                    if (children != null)
                    {
                        for (NodeRef childRef : children)
                        {
                            descedantsStack.push(new RecursiveElement(currentContainer, element.getDepth() + 1,
                                    childRef));
                        }
                    }
                }
            }
            return objectInFolderContainer;
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    private CmisObjectInFolderContainerType createObjectInFolderContainer(NodeRef nodeRef, PropertyFilter filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, String renditionFilter, Boolean includePathSegments) throws CmisException
    {
        CmisObjectType cmisObject = createCmisObject(nodeRef, filter, includeRelationships, includeAllowableActions,
                renditionFilter);

        CmisObjectInFolderType objectInFolderType = new CmisObjectInFolderType();
        objectInFolderType.setObject(cmisObject);
        if (includePathSegments != null && includePathSegments)
        {
            String path;
            try
            {
                path = propertiesUtil.getProperty(nodeRef, CMISDictionaryModel.PROP_NAME, "");
            }
            catch (CMISInvalidArgumentException e)
            {
                throw ExceptionUtil.createCmisException(e);
            }
            objectInFolderType.setPathSegment(path);
        }
        CmisObjectInFolderContainerType result = new CmisObjectInFolderContainerType();
        result.setObjectInFolder(objectInFolderType);
        return result;
    }

    private void checkDepthParameter(BigInteger depth) throws CmisException
    {
        if (depth.equals(BigInteger.ZERO) || (depth.compareTo(FULL_DESCENDANTS_HIERARCHY_CONDITION) < EQUALS_CONDITION_VALUE))
        {
            throw ExceptionUtil.createCmisException("The specified descendants depth is not valid. Valid depth values are: -1 (full hierarchy), N > 0",
                    EnumServiceException.INVALID_ARGUMENT);
        }
    }

    private List<NodeRef> receiveObjectParents(NodeRef objectId) throws CmisException
    {
        List<NodeRef> parents = new LinkedList<NodeRef>();
        for (ChildAssociationRef childParentAssociation : nodeService.getParentAssocs(objectId))
        {
            NodeRef parentRef = childParentAssociation.getParentRef();
            if (!parentRef.equals(nodeService.getRootNode(parentRef.getStoreRef())))
            {
                parents.add(parentRef);
            }
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
