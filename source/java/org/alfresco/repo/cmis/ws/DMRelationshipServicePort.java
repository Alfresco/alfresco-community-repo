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
import java.util.LinkedList;
import java.util.List;

import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.QNamePattern;

/**
 * Port for relationship service
 * 
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "RelationshipServicePort", serviceName = "RelationshipService", portName = "RelationshipServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.RelationshipServicePort")
public class DMRelationshipServicePort extends DMAbstractServicePort implements RelationshipServicePort
{
    private DictionaryService dictionaryService;

    /**
     * Gets a list of relationships associated with the object, optionally of a specified relationship type, and optionally in a specified direction.
     * 
     * @param parameters repositoryId: Repository Id, objectId: The object with which relationships are associated with; direction: source (Default), target, both; typeId:
     *        Relationship Type; includeSubRelationshipTypes: false (Default); filter: property filter; includeAllowableActions: false (default); maxItems: 0 = Unlimited;
     *        skipCount: 0 = start at beginning
     * @return collection of CmisObjectType and boolean hasMoreItems
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws FilterNotValidException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws TypeNotFoundException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public GetRelationshipsResponse getRelationships(GetRelationships parameters) throws PermissionDeniedException, UpdateConflictException, FilterNotValidException,
            ObjectNotFoundException, OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {

        checkRepositoryId(parameters.getRepositoryId());

        EnumRelationshipDirection direction = ((parameters.getDirection() != null) && (parameters.getDirection().getValue() != null)) ? (parameters.getDirection().getValue())
                : (EnumRelationshipDirection.SOURCE);
        Boolean includingSubtypes = ((parameters.getIncludeSubRelationshipTypes() != null) && (parameters.getIncludeSubRelationshipTypes().getValue() != null)) ? (parameters
                .getIncludeSubRelationshipTypes().getValue()) : (false);
        String typeId = ((parameters.getTypeId() != null) && (parameters.getTypeId().getValue() != null)) ? (parameters.getTypeId().getValue()) : (null);
        BigInteger skipCount = ((parameters.getSkipCount() != null) && (parameters.getSkipCount().getValue() != null)) ? (parameters.getSkipCount().getValue()) : (BigInteger.ZERO);
        BigInteger maxItems = ((parameters.getMaxItems() != null) && (parameters.getMaxItems().getValue() != null)) ? (parameters.getMaxItems().getValue()) : (BigInteger.ZERO);

        QName associationType = cmisDictionaryService.getCMISMapping().getAlfrescoType(cmisDictionaryService.getCMISMapping().getCmisTypeId(typeId).getQName());

        return formatResponse(createPropertyFilter(parameters.getFilter()), receiveAssociations(
                (NodeRef) this.cmisObjectsUtils.getIdentifierInstance(parameters.getObjectId(), AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT).getConvertedIdentifier(),
                associationType, direction, includingSubtypes).toArray(), new GetRelationshipsResponse(), skipCount, maxItems);
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {

        this.dictionaryService = dictionaryService;
    }

    private GetRelationshipsResponse formatResponse(PropertyFilter filter, Object[] sourceArray, GetRelationshipsResponse result, BigInteger skipCount, BigInteger maxItems)
            throws InvalidArgumentException, FilterNotValidException
    {

        Cursor cursor = createCursor(sourceArray.length, skipCount, maxItems);

        for (int i = cursor.getStartRow(); i < cursor.getEndRow(); i++)
        {
            result.getObject().add(convertAlfrescoObjectToCmisObject(sourceArray[i].toString(), filter));
        }

        return result;
    }

    private List<AssociationRef> receiveAssociations(NodeRef objectNodeReference, QName necessaryRelationshipType, EnumRelationshipDirection direction, boolean includingSubtypes)
    {

        List<AssociationRef> result = new LinkedList<AssociationRef>();

        QNamePattern matcher = new RelationshipByTypeFilter(necessaryRelationshipType, includingSubtypes);

        if ((direction == EnumRelationshipDirection.BOTH) || (direction == EnumRelationshipDirection.TARGET))
        {
            result.addAll(this.nodeService.getSourceAssocs(objectNodeReference, matcher));
        }

        if ((direction == EnumRelationshipDirection.BOTH) || (direction == EnumRelationshipDirection.SOURCE))
        {
            result.addAll(this.nodeService.getTargetAssocs(objectNodeReference, matcher));
        }

        return result;
    }

    private class RelationshipByTypeFilter implements QNamePattern
    {
        private boolean includingSubtypes;
        private QName necessaryGeneralType;

        public RelationshipByTypeFilter(QName necessaryGeneralType, boolean includingSubtypes)
        {

            this.includingSubtypes = includingSubtypes;
            this.necessaryGeneralType = necessaryGeneralType;
        }

        public boolean isMatch(QName qname)
        {

            if (this.necessaryGeneralType == null)
            {
                return true;
            }

            return ((this.includingSubtypes) ? (dictionaryService.getAssociation(qname) != null)
                    : (cmisDictionaryService.getCMISMapping().isValidCmisRelationship(qname) && this.necessaryGeneralType.equals(qname)));
        }
    }
}
