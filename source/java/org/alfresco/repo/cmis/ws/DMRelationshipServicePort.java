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

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.repo.cmis.PropertyFilter;
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
@javax.jws.WebService(name = "RelationshipServicePort", serviceName = "RelationshipService", portName = "RelationshipServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901", endpointInterface = "org.alfresco.repo.cmis.ws.RelationshipServicePort")
public class DMRelationshipServicePort extends DMAbstractServicePort implements RelationshipServicePort
{
    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
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
    public void getRelationships(String repositoryId, String objectId, EnumRelationshipDirection direction, String typeId, Boolean includeSubRelationshipTypes, String filter,
            Boolean includeAllowableActions, EnumIncludeRelationships includeRelationships, BigInteger maxItems, BigInteger skipCount, Holder<List<CmisObjectType>> object,
            Holder<Boolean> hasMoreItems) throws CmisException
    {
        checkRepositoryId(repositoryId);

        direction = (null != direction) ? direction : EnumRelationshipDirection.SOURCE;
        if (null == includeSubRelationshipTypes)
        {
            throw cmisObjectsUtils.createCmisException("includeSubRelationshipTypes input parameter is required", EnumServiceException.INVALID_ARGUMENT);
        }
        skipCount = (null != skipCount) ? skipCount : BigInteger.ZERO;
        maxItems = (null != maxItems) ? maxItems : BigInteger.ZERO;

        QName associationType = null;
        if ((null != typeId) && !typeId.equals(""))
        {
            CMISTypeDefinition cmisTypeDef = cmisDictionaryService.findType(typeId);
            associationType = cmisTypeDef.getTypeId().getQName();
        }

        // TODO: process 'includeAllowableActions' param, see DMObjectServicePort->determineObjectAllowableActions
        PropertyFilter propertyFilter = createPropertyFilter(filter);
        NodeRef objectNodeRef = cmisObjectsUtils.getIdentifierInstance(objectId, AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
        List<AssociationRef> assocs = null;
        try
        {
            assocs = cmisObjectsUtils.receiveAssociations(objectNodeRef, new RelationshipTypeFilter(associationType, includeSubRelationshipTypes), direction);
        }
        catch (Exception e)
        {
            throw cmisObjectsUtils.createCmisException("Can't receive associations", e);
        }
        if (null == object)
        {
            object = new Holder<List<CmisObjectType>>();
        }
        object.value = formatResponse(propertyFilter, assocs.toArray(), skipCount, maxItems, hasMoreItems);
    }

    private List<CmisObjectType> formatResponse(PropertyFilter filter, Object[] sourceArray, BigInteger skipCount, BigInteger maxItems, Holder<Boolean> hasMoreItems)
            throws CmisException
    {
        Cursor cursor = createCursor(sourceArray.length, skipCount, maxItems);
        List<CmisObjectType> result = new LinkedList<CmisObjectType>();
        for (int i = cursor.getStartRow(); i < cursor.getEndRow(); i++)
        {
            result.add(createCmisObject(sourceArray[i].toString(), filter));
        }
        if (null == hasMoreItems)
        {
            hasMoreItems = new Holder<Boolean>();
        }
        hasMoreItems.value = cursor.getEndRow() < sourceArray.length;
        return result;
    }

    private class RelationshipTypeFilter implements QNamePattern
    {
        private boolean includeSubtypes;
        private QName relationshipType;

        public RelationshipTypeFilter(QName ralationshipType, boolean includeSubtypes)
        {
            this.relationshipType = ralationshipType;
            this.includeSubtypes = includeSubtypes;
        }

        public boolean isMatch(QName qname)
        {
            if (relationshipType == null)
            {
                return true;
            }
            else if (includeSubtypes)
            {
                // TODO: it is necessary introduce checking on descendants
                return dictionaryService.getAssociation(qname) != null;
            }
            else
            {
                CMISTypeDefinition typeDef = cmisDictionaryService.findTypeForClass(qname, CMISScope.RELATIONSHIP);
                return typeDef != null && relationshipType.equals(qname);
            }
        }
    }
}
