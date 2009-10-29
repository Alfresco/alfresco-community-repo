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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISTypesFilterEnum;
import org.alfresco.repo.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

import com.sun.star.auth.InvalidArgumentException;

/**
 * Port for navigation service
 *
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "NavigationServicePort", serviceName = "NavigationService", portName = "NavigationServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901", endpointInterface = "org.alfresco.repo.cmis.ws.NavigationServicePort")
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
    public void getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, BigInteger maxItems, BigInteger skipCount, Holder<List<CmisObjectType>> object, Holder<Boolean> hasMoreItems)
            throws CmisException
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

        object.value = new ArrayList<CmisObjectType>();
        List<CmisObjectType> resultListing = object.value;

        for (int index = cursor.getStartRow(); index <= cursor.getEndRow(); index++)
        {
            resultListing.add(createCmisObject(nodeRefs[index].toString(), propertyFilter));
        }

        hasMoreItems.value = new Boolean(cursor.getEndRow() < (nodeRefs.length - 1));

        // TODO: includeAllowableActions, includeRelationships

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
    public void getChildren(String repositoryId, String folderId, String filter, Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships,
            Boolean includeRenditions, Boolean includeACL, BigInteger maxItems, BigInteger skipCount, String orderBy, Holder<List<CmisObjectType>> object,
            Holder<Boolean> hasMoreItems) throws CmisException
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

        Cursor cursor = createCursor(listing.length, skipCount, maxItems);

        object.value = new ArrayList<CmisObjectType>();
        List<CmisObjectType> resultListing = object.value;

        for (int index = cursor.getStartRow(); index <= cursor.getEndRow(); index++)
        {
            resultListing.add(createCmisObject(listing[index].toString(), propertyFilter));
        }

        hasMoreItems.value = new Boolean(cursor.getEndRow() < (listing.length - 1));

        // TODO: Process includeAllowableActions, includeRelationships, includeRenditions, includeACL 

    }

    // TODO: This method will create appropriate Ordering fields
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

    /**
     * Gets the list of descendant objects contained at one or more levels in the tree rooted at the specified folder. Only the filter-selected properties associated with each
     * object are returned. The content-stream is not returned. For paging through the children (depth of 1) only use {@link #getChildren(GetChildren parameters)}.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; depth: 1 this folder only (Default), N folders deep, -1 for all levels; filter: property filter;
     *        includeAllowableActions; includeRelationships;
     * @return collection of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public List<CmisObjectType> getDescendants(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, Boolean includeRenditions, String orderBy) throws CmisException
    {
        return getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, includeRenditions, orderBy, null);
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
    public List<CmisObjectType> getFolderTree(String repositoryId, String folderId, String filter, BigInteger depth, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships) throws CmisException
    {
        return getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions, includeRelationships, null, null, CMISTypesFilterEnum.FOLDERS);
    }

    /**
     * Returns the parent folder object, and optionally all ancestor folder objects, above a specified folder object.
     * 
     * @param parameters repositoryId: repository Id; folderId: folder Id; filter: property filter; includeAllowableActions; includeRelationships; returnToRoot: If false, return
     *        only the immediate parent of the folder. If true, return an ordered list of all ancestor folders from the specified folder to the root folder
     * @return collection of CmisObjectType
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public CmisObjectType getFolderParent(String repositoryId, String folderId, String filter) throws CmisException
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
        CmisObjectType result = createCmisObject(parentRef, propertyFilter);

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
    public List<CmisObjectType> getObjectParents(String repositoryId, String objectId, String filter) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);

        List<NodeRef> parents = receiveObjectParents((NodeRef) cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OBJECT));
        List<CmisObjectType> result = new ArrayList<CmisObjectType>();
        createCmisObjectList(propertyFilter, parents, result);
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

    private List<CmisObjectType> getDescendants(String repositoryId, String folderId, BigInteger depth, String filter, Boolean includeAllowableActions,
            EnumIncludeRelationships includeRelationships, Boolean includeRenditions, String orderBy, CMISTypesFilterEnum type) throws CmisException
    {
        checkRepositoryId(repositoryId);
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        depth = (depth == null) ? (BigInteger.ONE) : depth;
        checkDepthParameter(depth);

        HierarchyReceiverStrategy receiver = createHierarchyReceiver(type, depth);

        List<Pair<String, Boolean>> orderingFields = null;
        if ((orderBy != null) && !orderBy.equals(""))
        {
            orderingFields = checkAndParseOrderByClause(orderBy);
        }

        List<CmisObjectType> result = new ArrayList<CmisObjectType>();
        // TODO: Ordering functionality SHOULD be moved to getChildren service method
        createCmisObjectList(propertyFilter, receiver.receiveHierarchy(folderId, orderingFields), result);

        // TODO: includeAllowableActions, includeRelationships, includeRenditions

        return result;
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

    private HierarchyReceiverStrategy createHierarchyReceiver(CMISTypesFilterEnum type, BigInteger finalDepth)
    {
        if (finalDepth.equals(FULL_DESCENDANTS_HIERARCHY_CONDITION))
        {
            return new FullHierarchyReceiver(type);
        }
        else
        {
            return new LayerConstrainedHierarchyReceiver(type, finalDepth);
        }
    }

    private void separateDescendantsObjects(List<NodeRef> descendantsFolders, List<NodeRef> currentLayerFolders, List<NodeRef> currentLayerDocuments,
            List<Pair<String, Boolean>> orderingFields)
    {
        for (NodeRef element : descendantsFolders)
        {
            // TODO: Ordering functionality SHOULD be moved to getChildren service method
            currentLayerFolders.addAll(Arrays.asList(cmisService.getChildren(element, CMISTypesFilterEnum.FOLDERS)));

            // TODO: Ordering functionality SHOULD be moved to getChildren service method
            currentLayerDocuments.addAll(Arrays.asList(cmisService.getChildren(element, CMISTypesFilterEnum.DOCUMENTS)));
        }
    }

    private List<NodeRef> performDescendantsResultObjectsStoring(List<NodeRef> resultList, List<NodeRef> descendantsFolders, List<NodeRef> currentLayerFolders,
            List<NodeRef> currentLayerDocuments, List<Pair<String, Boolean>> orderingFields, CMISTypesFilterEnum type)
    {
        separateDescendantsObjects(descendantsFolders, currentLayerFolders, currentLayerDocuments, orderingFields);
        if (CMISTypesFilterEnum.ANY.equals(type) || CMISTypesFilterEnum.FOLDERS.equals(type))
        {
            resultList.addAll(currentLayerFolders);
        }
        if (CMISTypesFilterEnum.ANY.equals(type) || CMISTypesFilterEnum.DOCUMENTS.equals(type))
        {
            resultList.addAll(currentLayerDocuments);
        }
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
        public List<NodeRef> receiveHierarchy(String rootFolderIdentifier, List<Pair<String, Boolean>> orderFields) throws CmisException;
    }

    /**
     * @see HierarchyReceiverStrategy
     */
    private class FullHierarchyReceiver implements HierarchyReceiverStrategy
    {
        private List<NodeRef> descendantsFolders = new LinkedList<NodeRef>();
        private List<NodeRef> resultList = new LinkedList<NodeRef>();
        private CMISTypesFilterEnum type = CMISTypesFilterEnum.ANY;

        public FullHierarchyReceiver(CMISTypesFilterEnum type)
        {
            if (type != null)
            {
                this.type = type;
            }
        }

        /**
         * Traverse Alfresco objects hierarchy until there is some Folder-objects can be found
         */
        public List<NodeRef> receiveHierarchy(String rootFolderIdentifier, List<Pair<String, Boolean>> orderingFields) throws CmisException
        {
            descendantsFolders.add((NodeRef) cmisObjectsUtils.getIdentifierInstance(rootFolderIdentifier, AlfrescoObjectType.FOLDER_OBJECT));
            while (!descendantsFolders.isEmpty())
            {
                descendantsFolders = performDescendantsResultObjectsStoring(resultList, descendantsFolders, new LinkedList<NodeRef>(), new LinkedList<NodeRef>(), orderingFields,
                        type);
            }

            return resultList;
        }
    }

    /**
     * @see HierarchyReceiverStrategy
     */
    private class LayerConstrainedHierarchyReceiver implements HierarchyReceiverStrategy
    {
        private List<NodeRef> descendantsFolders = new LinkedList<NodeRef>();
        private BigInteger finalDepth;
        private BigInteger currentDepth = BigInteger.ZERO;
        private List<NodeRef> resultList = new LinkedList<NodeRef>();
        private CMISTypesFilterEnum type = CMISTypesFilterEnum.ANY;

        /**
         * @param returnObjectsType flag that specifies objects of which type are need to be returned
         * @param finalDepth the number of final Alfresco hierarchy layer: 1 - only children of specified folder; -1 - full descendants hierarchy
         */
        public LayerConstrainedHierarchyReceiver(CMISTypesFilterEnum type, BigInteger finalDepth)
        {
            this.finalDepth = finalDepth;
            if (type != null)
            {
                this.type = type;
            }
        }

        /**
         * This method of this class receives Alfresco objects hierarchy until specified layer number
         */
        public List<NodeRef> receiveHierarchy(String rootFolderIdentifier, List<Pair<String, Boolean>> orderingFields) throws CmisException
        {
            descendantsFolders.add((NodeRef) cmisObjectsUtils.getIdentifierInstance(rootFolderIdentifier, AlfrescoObjectType.FOLDER_OBJECT));

            do
            {
                descendantsFolders = performDescendantsResultObjectsStoring(this.resultList, this.descendantsFolders, new LinkedList<NodeRef>(), new LinkedList<NodeRef>(),
                        orderingFields, type);
                currentDepth = currentDepth.add(BigInteger.ONE);
            } while (!descendantsFolders.isEmpty() && (currentDepth.compareTo(this.finalDepth) < EQUALS_CONDITION_VALUE));

            return this.resultList;
        }
    }

}
