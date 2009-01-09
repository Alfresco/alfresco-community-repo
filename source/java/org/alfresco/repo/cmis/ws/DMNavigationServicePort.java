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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.cmis.CMISTypesFilterEnum;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Port for navigation service
 *
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "NavigationServicePort", serviceName = "NavigationService", portName = "NavigationServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.NavigationServicePort")
public class DMNavigationServicePort extends DMAbstractServicePort implements NavigationServicePort
{
    private static final String POLICIES_LISTING_UNSUPPORTED_EXCEPTION_MESSAGE = "Policies listing isn't supported";

    private static final int EQUALS_CONDITION_VALUE = 0;

    private static final BigInteger FULL_DESCENDANTS_HIERARCHY_CONDITION = BigInteger.valueOf(-1l);

    /**
     * Gets the private working copies of checked-out objects that the user is allowed to update.
     * 
     * @param parameters repositoryId: repository Id; folderID: folder Id; filter: property filter; includeAllowableActions; includeRelationships; maxItems: 0 = Unlimited;
     *        skipCount: 0 = start at beginning
     * @return collection of CmisObjectType and boolean hasMoreItems
     * @throws RuntimeException
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws ConstraintViolationException
     * @throws FilterNotValidException
     * @throws OperationNotSupportedException
     * @throws UpdateConflictException
     * @throws FolderNotValidException
     * @throws PermissionDeniedException
     */
    public GetCheckedoutDocsResponse getCheckedoutDocs(GetCheckedoutDocs parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        checkRepositoryId(parameters.getRepositoryId());

        PropertyFilter propertyFilter = createPropertyFilter(parameters.getFilter());

        NodeRef folderId = (NodeRef) (((parameters.getFolderID() != null) && (parameters.getFolderID().getValue() != null)) ? (this.cmisObjectsUtils.getIdentifierInstance(
                parameters.getFolderID().getValue(), AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier()) : (null));

        NodeRef[] nodeRefs = this.cmisService.getCheckedOut(AuthenticationUtil.getFullyAuthenticatedUser(), folderId, (folderId == null));

        Cursor cursor = createCursor(nodeRefs.length, parameters.getSkipCount() != null ? parameters.getSkipCount().getValue() : null,
                parameters.getMaxItems() != null ? parameters.getMaxItems().getValue() : null);

        GetCheckedoutDocsResponse response = new GetCheckedoutDocsResponse();
        List<CmisObjectType> resultListing = response.getObject();

        for (int index = cursor.getStartRow(); index <= cursor.getEndRow(); index++)
        {
            resultListing.add(convertAlfrescoObjectToCmisObject(nodeRefs[index].toString(), propertyFilter));
        }

        response.setHasMoreItems(cursor.getEndRow() < (nodeRefs.length - 1));

        // TODO: includeAllowableActions, includeRelationships

        return response;
    }

    /**
     * Gets the list of child objects contained in the specified folder. Only the filter-selected properties associated with each object are returned. The content-streams of
     * documents are not returned.For returning a tree of objects of a certain depth, use {@link #getDescendants(GetDescendants parameters)}.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; type: DOCUMENTS, FOLDERS, POLICIES, ANY; filter: property filter; includeAllowableActions;
     *        includeRelationships; maxItems: 0 = Unlimited; skipCount: 0 = start at beginning
     * @return collection of CmisObjectType and boolean hasMoreItems
     * @throws RuntimeException
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws ConstraintViolationException
     * @throws FilterNotValidException
     * @throws OperationNotSupportedException
     * @throws UpdateConflictException
     * @throws FolderNotValidException
     * @throws PermissionDeniedException
     */
    public GetChildrenResponse getChildren(GetChildren parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, ConstraintViolationException,
            FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        PropertyFilter propertyFilter = createPropertyFilter(parameters.getFilter());

        NodeRef folderNodeRef = this.cmisObjectsUtils.getIdentifierInstance(parameters.getFolderId(), AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier();

        NodeRef[] listing = null;

        EnumTypesOfFileableObjects types = EnumTypesOfFileableObjects.ANY;

        if (parameters.getType() != null)
        {
            types = parameters.getType().getValue();
        }

        switch (types)
        {
        case DOCUMENTS:
            listing = cmisService.getChildren(folderNodeRef, CMISTypesFilterEnum.DOCUMENTS);
            break;
        case FOLDERS:
            listing = cmisService.getChildren(folderNodeRef, CMISTypesFilterEnum.FOLDERS);
            break;
        case POLICIES:
            throw new OperationNotSupportedException(POLICIES_LISTING_UNSUPPORTED_EXCEPTION_MESSAGE);
        case ANY:
            listing = cmisService.getChildren(folderNodeRef, CMISTypesFilterEnum.ANY);
            break;
        }

        Cursor cursor = createCursor(listing.length, (parameters.getSkipCount() != null ? parameters.getSkipCount().getValue() : null),
                (parameters.getMaxItems() != null ? parameters.getMaxItems().getValue() : null));

        GetChildrenResponse response = new GetChildrenResponse();
        List<CmisObjectType> resultListing = response.getObject();

        for (int index = cursor.getStartRow(); index <= cursor.getEndRow(); index++)
        {
            resultListing.add(convertAlfrescoObjectToCmisObject(listing[index].toString(), propertyFilter));
        }

        response.setHasMoreItems(cursor.getEndRow() < (listing.length - 1));

        return response;
    }

    /**
     * Gets the list of descendant objects contained at one or more levels in the tree rooted at the specified folder. Only the filter-selected properties associated with each
     * object are returned. The content-stream is not returned. For paging through the children (depth of 1) only use {@link #getChildren(GetChildren parameters)}.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; depth: 1 this folder only (Default), � N folders deep, -1 for all levels; filter: property filter;
     *        includeAllowableActions; includeRelationships;
     * @return collection of CmisObjectType
     * @throws RuntimeException
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws ConstraintViolationException
     * @throws FilterNotValidException
     * @throws OperationNotSupportedException
     * @throws UpdateConflictException
     * @throws FolderNotValidException
     * @throws PermissionDeniedException
     */
    public GetDescendantsResponse getDescendants(GetDescendants parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {

        BigInteger depth = ((parameters.getDepth() != null) && (parameters.getDepth().getValue() != null)) ? (parameters.getDepth().getValue()) : (BigInteger.ONE);

        checkRepositoryId(parameters.getRepositoryId());
        checkDepthParameter(depth);

        GetDescendantsResponse response = new GetDescendantsResponse();

        formatCommonResponse(createPropertyFilter(parameters.getFilter()), createHierarchyReceiver(
                (parameters.getType() != null) ? (parameters.getType()) : (EnumTypesOfFileableObjects.ANY), depth).receiveHierarchy(parameters.getFolderId()), response.getObject());

        // TODO: includeAllowableActions, includeRelationships

        return response;
    }

    /**
     * Returns the parent folder object, and optionally all ancestor folder objects, above a specified folder object.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; filter: property filter; includeAllowableActions; includeRelationships; returnToRoot: If false, return
     *        only the immediate parent of the folder. If true, return an ordered list of all ancestor folders from the specified folder to the root folder
     * @return collection of CmisObjectType
     * @throws RuntimeException
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws ConstraintViolationException
     * @throws FilterNotValidException
     * @throws OperationNotSupportedException
     * @throws UpdateConflictException
     * @throws FolderNotValidException
     * @throws PermissionDeniedException
     */
    public GetFolderParentResponse getFolderParent(GetFolderParent parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {

        checkRepositoryId(parameters.getRepositoryId());

        GetFolderParentResponse response = new GetFolderParentResponse();

        formatCommonResponse(createPropertyFilter(parameters.getFilter()), receiveParentList(parameters.getFolderId(), (((parameters.getReturnToRoot() != null) && (parameters
                .getReturnToRoot().getValue() != null)) ? (parameters.getReturnToRoot().getValue()) : (false))), response.getObject());

        // TODO: includeAllowableActions, includeRelationships

        return response;
    }

    /**
     * Returns the parent folders for the specified non-folder, fileable object.
     * 
     * @param parameters repositoryId: repository Id; objectId: object Id; filter: property filter; includeAllowableActions; includeRelationships;
     * @return collection of CmisObjectType
     * @throws RuntimeException
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws ConstraintViolationException
     * @throws FilterNotValidException
     * @throws OperationNotSupportedException
     * @throws UpdateConflictException
     * @throws FolderNotValidException
     * @throws PermissionDeniedException
     */
    public GetObjectParentsResponse getObjectParents(GetObjectParents parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException,
            ConstraintViolationException, FilterNotValidException, OperationNotSupportedException, UpdateConflictException, FolderNotValidException, PermissionDeniedException
    {
        // TODO: Policy

        checkRepositoryId(parameters.getRepositoryId());

        GetObjectParentsResponse response = new GetObjectParentsResponse();

        formatCommonResponse(createPropertyFilter(parameters.getFilter()), receiveObjectParents((NodeRef) this.cmisObjectsUtils.getIdentifierInstance(parameters.getObjectId(),
                AlfrescoObjectType.DOCUMENT_OBJECT).getConvertedIdentifier()), response.getObject());

        // TODO: includeAllowableActions, includeRelationships

        return response;
    }

    private void checkDepthParameter(BigInteger depth) throws InvalidArgumentException
    {

        if (depth.equals(BigInteger.ZERO) || (depth.compareTo(FULL_DESCENDANTS_HIERARCHY_CONDITION) < EQUALS_CONDITION_VALUE))
        {
            throw new InvalidArgumentException("The specified descendants retriving depth is not valid. Valid depth values are: -1 (full hierarchy), N > 0");
        }
    }

    private List<NodeRef> receiveParentList(String targetChildIdentifier, boolean fullParentsHierarchy) throws InvalidNodeRefException, InvalidArgumentException,
            ObjectNotFoundException
    {

        List<NodeRef> result = new LinkedList<NodeRef>();

        if (targetChildIdentifier.equals(this.cmisService.getDefaultRootNodeRef().toString()))
        {
            return result;
        }

        NodeRef currentParent = receiveNextParentNodeReference((NodeRef) this.cmisObjectsUtils.getIdentifierInstance(targetChildIdentifier, AlfrescoObjectType.FOLDER_OBJECT)
                .getConvertedIdentifier(), result);

        return (fullParentsHierarchy) ? (receiveFullAncestorsHierachy(currentParent, result)) : (result);
    }

    private List<NodeRef> receiveFullAncestorsHierachy(NodeRef currentParent, List<NodeRef> parents)
    {

        String lastAncestorIdentifier = this.cmisService.getDefaultRootNodeRef().toString();

        while ((currentParent != null) && !currentParent.toString().equals(lastAncestorIdentifier))
        {
            currentParent = receiveNextParentNodeReference(currentParent, parents);
        }

        return parents;
    }

    private NodeRef receiveNextParentNodeReference(NodeRef currentParent, List<NodeRef> parents)
    {

        currentParent = this.nodeService.getPrimaryParent(currentParent).getParentRef();

        if (currentParent != null)
        {
            parents.add(currentParent);
        }

        return currentParent;
    }

    private List<NodeRef> receiveObjectParents(NodeRef objectId) throws InvalidArgumentException
    {

        List<NodeRef> parents = new LinkedList<NodeRef>();

        for (ChildAssociationRef childParentAssociation : this.nodeService.getParentAssocs(objectId))
        {
            parents.add(childParentAssociation.getParentRef());
        }

        return parents;
    }

    private HierarchyReceiverStrategy createHierarchyReceiver(EnumTypesOfFileableObjects returnObjectsType, BigInteger finalDepth)
    {

        return (finalDepth.equals(FULL_DESCENDANTS_HIERARCHY_CONDITION)) ? (new FullHierarchyReceiver(returnObjectsType)) : (new LayerConstrainedHierarchyReceiver(
                returnObjectsType, finalDepth));
    }

    private void separateDescendantsObjects(EnumTypesOfFileableObjects returnObjectsType, List<NodeRef> descendantsFolders, List<NodeRef> currentLayerFolders,
            List<NodeRef> currentLayerDocuments)
    {

        for (NodeRef element : descendantsFolders)
        {
            // TODO: OrderBy functionality processing. Instead Arrays.asList() it is necessary to add ordering processing method to store each new element where it should go
            currentLayerFolders.addAll(Arrays.asList(this.cmisService.getChildren(element, CMISTypesFilterEnum.FOLDERS)));

            // TODO: OrderBy functionality processing. Instead Arrays.asList() it is necessary to add ordering processing method to store each new element where it should go
            if ((returnObjectsType == EnumTypesOfFileableObjects.ANY) || (returnObjectsType == EnumTypesOfFileableObjects.DOCUMENTS))
            {
                currentLayerDocuments.addAll(Arrays.asList(this.cmisService.getChildren(element, CMISTypesFilterEnum.DOCUMENTS)));
            }
        }
    }

    private List<NodeRef> performDescendantsResultObjectsStoring(EnumTypesOfFileableObjects returnObjectsType, List<NodeRef> resultList, List<NodeRef> descendantsFolders,
            List<NodeRef> currentLayerFolders, List<NodeRef> currentLayerDocuments)
    {

        separateDescendantsObjects(returnObjectsType, descendantsFolders, currentLayerFolders, currentLayerDocuments);

        if ((returnObjectsType == EnumTypesOfFileableObjects.ANY) || (returnObjectsType == EnumTypesOfFileableObjects.FOLDERS))
        {
            resultList.addAll(currentLayerFolders);
        }

        resultList.addAll(currentLayerDocuments);

        return currentLayerFolders;
    }

    /**
     * This interface introduce common type for Alfresco objects hierarchy receiving
     */
    private interface HierarchyReceiverStrategy
    {
        /**
         * @param rootFolderIdentifier the source folder Id from whose hierarchy bypassing will be started
         * @return <b>List</b> that contains all appropriates layers of Alfresco objects
         * @throws InvalidArgumentException
         */
        public List<NodeRef> receiveHierarchy(String rootFolderIdentifier) throws InvalidArgumentException;
    }

    /**
     * @see HierarchyReceiverStrategy
     */
    private class LayerConstrainedHierarchyReceiver implements HierarchyReceiverStrategy
    {
        private List<NodeRef> descendantsFolders = new LinkedList<NodeRef>();

        private EnumTypesOfFileableObjects returnObjectsType;

        private BigInteger finalDepth;

        private BigInteger currentDepth = BigInteger.ZERO;

        private List<NodeRef> resultList = new LinkedList<NodeRef>();

        /**
         * @param returnObjectsType flag that specifies objects of which type are need to be returned
         * @param finalDepth the number of final Alfresco hierarchy layer: 1 - only children of specified folder; -1 - full descendants hierarchy
         */
        public LayerConstrainedHierarchyReceiver(EnumTypesOfFileableObjects returnObjectsType, BigInteger finalDepth)
        {

            this.returnObjectsType = returnObjectsType;
            this.finalDepth = finalDepth;
        }

        /**
         * This method of this class receives Alfresco objects hierarchy until specified layer number
         */
        public List<NodeRef> receiveHierarchy(String rootFolderIdentifier) throws InvalidArgumentException
        {

            this.descendantsFolders.add((NodeRef) cmisObjectsUtils.getIdentifierInstance(rootFolderIdentifier, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier());

            do
            {
                this.descendantsFolders = performDescendantsResultObjectsStoring(this.returnObjectsType, this.resultList, this.descendantsFolders, new LinkedList<NodeRef>(),
                        new LinkedList<NodeRef>());

                this.currentDepth = this.currentDepth.add(BigInteger.ONE);
            } while (!this.descendantsFolders.isEmpty() && (this.currentDepth.compareTo(this.finalDepth) < EQUALS_CONDITION_VALUE));

            return this.resultList;
    }
    }

    /**
     * @see HierarchyReceiverStrategy
     */
    private class FullHierarchyReceiver implements HierarchyReceiverStrategy
    {
        private EnumTypesOfFileableObjects returnObjectsType;

        private List<NodeRef> descendantsFolders = new LinkedList<NodeRef>();

        private List<NodeRef> resultList = new LinkedList<NodeRef>();

        /**
         * @param returnObjectsType flag that specifies objects of which type are need to be returned
         */
        public FullHierarchyReceiver(EnumTypesOfFileableObjects returnObjectsType)
        {

            this.returnObjectsType = returnObjectsType;
        }

        /**
         * This method of this class bypass Alfresco objects hierarchy until there is some Folder-objects can be found
         */
        public List<NodeRef> receiveHierarchy(String rootFolderIdentifier) throws InvalidArgumentException
        {

            this.descendantsFolders.add((NodeRef) cmisObjectsUtils.getIdentifierInstance(rootFolderIdentifier, AlfrescoObjectType.FOLDER_OBJECT).getConvertedIdentifier());

            while (!this.descendantsFolders.isEmpty())
            {
                this.descendantsFolders = performDescendantsResultObjectsStoring(this.returnObjectsType, this.resultList, this.descendantsFolders, new LinkedList<NodeRef>(),
                        new LinkedList<NodeRef>());
            }

            return this.resultList;
        }
    }
}
