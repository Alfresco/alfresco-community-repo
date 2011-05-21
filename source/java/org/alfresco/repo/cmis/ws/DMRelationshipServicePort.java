/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.cmis.CMISRelationshipDirectionEnum;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Port for relationship service
 * 
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "RelationshipServicePort", serviceName = "RelationshipService", portName = "RelationshipServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.RelationshipServicePort")
public class DMRelationshipServicePort extends DMAbstractServicePort implements RelationshipServicePort
{
    private static final Map<EnumRelationshipDirection, CMISRelationshipDirectionEnum> RELATIONSHIP_DIRECTION_MAPPING;
    static
    {
        RELATIONSHIP_DIRECTION_MAPPING = new HashMap<EnumRelationshipDirection, CMISRelationshipDirectionEnum>(5);
        RELATIONSHIP_DIRECTION_MAPPING.put(EnumRelationshipDirection.SOURCE, CMISRelationshipDirectionEnum.SOURCE);
        RELATIONSHIP_DIRECTION_MAPPING.put(EnumRelationshipDirection.TARGET, CMISRelationshipDirectionEnum.TARGET);
        RELATIONSHIP_DIRECTION_MAPPING.put(EnumRelationshipDirection.EITHER, CMISRelationshipDirectionEnum.EITHER);
    }

    /**
     * Gets a list of relationships associated with the object, optionally of a specified relationship type, and optionally in a specified direction.
     * 
     * @param parameters repositoryId: Repository Id, objectId: The object with which relationships are associated with; direction: source (Default), target, both; typeId:
     *        Relationship Type; includeSubRelationshipTypes: false (Default); filter: property filter; includeAllowableActions: false (default); maxItems: 0 = Unlimited;
     *        skipCount: 0 = start at beginning
     * @return collection of CmisObjectType and boolean hasMoreItems
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public CmisObjectListType getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes, EnumRelationshipDirection relationshipDirection,
            String typeId, String filter, Boolean includeAllowableActions, BigInteger maxItems, BigInteger skipCount, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        if ((null == objectId) || "".equals(objectId) || !NodeRef.isNodeRef(objectId))
        {
            throw ExceptionUtil.createCmisException(("Object with Id='" + objectId + "' is not exist!"), EnumServiceException.OBJECT_NOT_FOUND);
        }
        try
        {
            CMISTypeDefinition relDef = (null != typeId) ? (cmisService.getTypeDefinition(typeId)):(null);
            NodeRef nodeRef = cmisService.getReadableObject(objectId, NodeRef.class);
            AssociationRef[] assocs = cmisService.getRelationships(nodeRef, relDef, includeSubRelationshipTypes != null && includeSubRelationshipTypes,
                    relationshipDirection == null ? CMISRelationshipDirectionEnum.SOURCE : RELATIONSHIP_DIRECTION_MAPPING.get(relationshipDirection));
            skipCount = (null != skipCount) ? skipCount : BigInteger.ZERO;
            maxItems = (null != maxItems) ? maxItems : BigInteger.ZERO;
            PropertyFilter propertyFilter = createPropertyFilter(filter);
            return createResult(propertyFilter, includeAllowableActions, assocs, skipCount, maxItems);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    private CmisObjectListType createResult(PropertyFilter filter, Boolean includeAllowableActions, Object[] sourceArray, BigInteger skipCount, BigInteger maxItems)
            throws CmisException
    {
        Cursor cursor = createCursor(sourceArray.length, skipCount, maxItems);
        CmisObjectListType result = new CmisObjectListType();
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            result.getObjects().add(createCmisObject(sourceArray[i], filter, null, includeAllowableActions, null));
        }
        result.setHasMoreItems(cursor.getEndRow() < sourceArray.length);
        result.setNumItems(BigInteger.valueOf(cursor.getPageSize()));
        return result;
    }
}
