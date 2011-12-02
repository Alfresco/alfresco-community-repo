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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISChangeEvent;
import org.alfresco.cmis.CMISChangeLog;
import org.alfresco.cmis.CMISChangeType;
import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISRelationshipDirectionEnum;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISResultSetColumn;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.PropertyFilter;
import org.alfresco.cmis.CMISQueryOptions.CMISQueryMode;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Port for Discovery service.
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "DiscoveryServicePort", serviceName = "DiscoveryService", portName = "DiscoveryServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.DiscoveryServicePort")
public class DMDiscoveryServicePort extends DMAbstractServicePort implements DiscoveryServicePort
{
    private static Map<CMISChangeType, EnumTypeOfChanges> changesTypeMapping = new HashMap<CMISChangeType, EnumTypeOfChanges>();
    static
    {
        changesTypeMapping.put(CMISChangeType.CREATED, EnumTypeOfChanges.CREATED);
        changesTypeMapping.put(CMISChangeType.UPDATED, EnumTypeOfChanges.UPDATED);
        changesTypeMapping.put(CMISChangeType.SECURITY, EnumTypeOfChanges.SECURITY);
        changesTypeMapping.put(CMISChangeType.DELETED, EnumTypeOfChanges.DELETED);
    }

    /**
     * Queries the repository for queryable object based on properties or an optional full-text string. Relationship objects are not queryable. Content-streams are not returned as
     * part of query
     * 
     * ALF-9566 : hasMoreItems was changed to be confirmed with a section (2.2.1.1 Paging) of the specification. 
     * 
     * @param parameters query parameters
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public QueryResponse query(Query parameters) throws CmisException
    {
        checkRepositoryId(parameters.getRepositoryId());

        // TODO: includeRelationships, includeRenditions
        CMISQueryOptions options = new CMISQueryOptions(parameters.getStatement(), cmisService.getDefaultRootStoreRef());

        int skipCount = 0;
        if (parameters.getSkipCount() != null && parameters.getSkipCount().getValue() != null)
        {
            skipCount = parameters.getSkipCount().getValue().intValue();
            options.setSkipCount(skipCount);
        }

        boolean includeAllowableActions = ((null != parameters.getIncludeAllowableActions()) && (null != parameters.getIncludeAllowableActions().getValue())) ? (parameters
                .getIncludeAllowableActions().getValue()) : (false);
        String renditionFilter = (null != parameters.getRenditionFilter()) ? (parameters.getRenditionFilter().getValue()) : null;

        // execute query
        // TODO: If the select clause includes properties from more than a single type reference, then the repository SHOULD throw an exception if includeRelationships or
        // includeAllowableActions is specified as true.
        options.setQueryMode(CMISQueryMode.CMS_WITH_ALFRESCO_EXTENSIONS);
        CMISResultSet resultSet = cmisQueryService.query(options);
        CMISResultSetColumn[] columns = resultSet.getMetaData().getColumns();

        // build query response
        QueryResponse response = new QueryResponse();
        response.setObjects(new CmisObjectListType());

        EnumIncludeRelationships cmisDirection = (null != parameters.getIncludeRelationships()) ? (parameters.getIncludeRelationships().getValue()) : (null);
        CMISRelationshipDirectionEnum includeRelationships = INCLUDE_RELATIONSHIPS_ENUM_MAPPING.get(cmisDirection);

        int maxItems = -1;
        if (parameters.getMaxItems() != null && parameters.getMaxItems().getValue() != null)
        {
            maxItems = parameters.getMaxItems().getValue().intValue();
        }

        // total number of items
        int numItems = resultSet.getLength();
        // for each row...
        int idx = 0;
        for (CMISResultSetRow row : resultSet)
        {
            if (maxItems != -1 && idx == maxItems)
            {
                break;
            }
            
            CmisPropertiesType properties = new CmisPropertiesType();
            Map<String, Serializable> values = row.getValues();

            // for each column...
            for (CMISResultSetColumn column : columns)
            {
                CmisProperty property = propertiesUtil.createProperty(column.getName(), column.getCMISDataType(), values.get(column.getName()));
                if (property != null)
                {
                    property.setQueryName(column.getName());
                    properties.getProperty().add(property);
                }
            }

            CmisObjectType object = new CmisObjectType();
            object.setProperties(properties);
            Object identifier;
            NodeRef nodeRef;
            try
            {
                nodeRef = row.getNodeRef();
                identifier = cmisService.getReadableObject((String) nodeRef.toString(), Object.class);
            }
            catch (CMISServiceException e)
            {
                throw ExceptionUtil.createCmisException(e);
            }
            if (includeAllowableActions)
            {
                object.setAllowableActions(determineObjectAllowableActions(identifier));
            }
            if (null != includeRelationships)
            {
                appendWithRelationships(nodeRef, createPropertyFilter((String) null), includeRelationships, includeAllowableActions, renditionFilter, object);
            }
            if (renditionFilter != null)
            {
                List<CmisRenditionType> renditions = getRenditions(identifier, renditionFilter);
                if (renditions != null && !renditions.isEmpty())
                {
                    object.getRendition().addAll(renditions);
                }
            }
            response.getObjects().getObjects().add(object);
            idx++;
        }
        response.getObjects().setNumItems(BigInteger.valueOf(numItems));
        boolean hasMoreItems = (maxItems != -1 ? (numItems - (skipCount + maxItems)) > 0 : false) || resultSet.hasMore();
        response.getObjects().setHasMoreItems(hasMoreItems);
        return response;
    }

    /**
     * Gets a list of content changes. Targeted for search crawlers or other applications that need to efficiently understand what has changed in the repository. Note: The content
     * stream is NOT returned for any change event.
     * 
     * @param repositoryId {@link String} value that determines Id of the necessary Repository
     * @param changeLogToken generic {@link Holder} class instance with {@link String} type parameter that determines last Change Log Token
     * @param includeProperties {@link Boolean} instance value that specifies whether all allowed by filter properties should be returned for Change Type equal to 'UPDATED' or
     *        Object Id property only
     * @param filter {@link String} value for filtering properties for Change Entry with Change Type equal to 'UPDATED'
     * @param includePolicyIds {@link Boolean} instance value that determines whether Policy Ids must be returned
     * @param includeACL {@link Boolean} instance value that determines whether ACLs must be returned
     * @param maxItems {@link BigInteger} instance value that determines required amount of Change Log Entries
     * @param extension {@link CmisException} instance of unknown assignment
     * @param objects generic {@link Holder} instance with {@link CmisObjectListType} type parameter for storing results of service execution
     * @throws CmisException with next allowable {@link EnumServiceException} enum attribute of exception type values: CONSTRAINT, FILTER_NOT_VALID, INVALID_ARGUMENT,
     *         NOT_SUPPORTED, OBJECT_NOT_FOUND, PERMISSION_DENIED, RUNTIME
     */
    public void getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties, String filter, Boolean includePolicyIds, Boolean includeACL,
            BigInteger maxItems, CmisExtensionType extension, Holder<CmisObjectListType> objects) throws CmisException
    {
        if (!authorityService.hasAdminAuthority())
        {
            throw ExceptionUtil.createCmisException("Cannot retrieve content changes", new AccessDeniedException("Requires admin authority"));
        }
        
        // TODO: includePolicyIds
        checkRepositoryId(repositoryId);
        String changeToken = (null != changeLogToken) ? (changeLogToken.value) : (null);
        Integer maxAmount = (null != maxItems) ? (maxItems.intValue()) : (null);
        boolean propertiesRequsted = (null != includeProperties) ? (includeProperties.booleanValue()) : (false);
        if (propertiesRequsted)
        {
            if ((null != filter) && !"".equals(filter) && !PropertyFilter.MATCH_ALL_FILTER.equals(filter) && !filter.contains(CMISDictionaryModel.PROP_OBJECT_ID))
            {
                filter = CMISDictionaryModel.PROP_OBJECT_ID + PropertyFilter.PROPERTY_NAME_TOKENS_DELIMITER + filter;
            }
        }
        else
        {
            filter = CMISDictionaryModel.PROP_OBJECT_ID;
        }

        CMISChangeLog changeLog = null;
        try
        {
            changeLog = cmisChangeLogService.getChangeLogEvents(changeToken, maxAmount);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        if (null == objects.value)
        {
            objects.value = new CmisObjectListType();
        }
        if ((null == changeLog) || (null == changeLog.getChangeEvents()) || changeLog.getChangeEvents().isEmpty())
        {
            objects.value.setHasMoreItems(false);
            objects.value.setNumItems(BigInteger.valueOf(0));
        }
        else
        {
            formatObjectsResponse(filter, propertiesRequsted, includeACL, changeLog, objects.value.getObjects());
            objects.value.setHasMoreItems(changeLog.hasMoreItems());
            objects.value.setNumItems(BigInteger.valueOf(changeLog.getChangeEvents().size()));
            changeLogToken.value = changeLog.getNextChangeToken();
        }
    }

    /**
     * This method formats response for Get Content Changes service
     * 
     * @param filter {@link String} value that determines user specified properties filter
     * @param propertiesRequsted {@link Boolean} value that determines whether properties another than Object Id should be returned (according to specified properties filter)
     * @param changeLog {@link CMISChangeLog} instance that represents descriptor for some Change Log Token
     * @param result {@link List}&lt;{@link CmisObjectType}&gt; collection instance for storing Change Event entries from Change Log descriptor
     * @throws CmisException
     */
    private void formatObjectsResponse(String filter, boolean propertiesRequsted, boolean includeAce, CMISChangeLog changeLog, List<CmisObjectType> result) throws CmisException
    {
        for (CMISChangeEvent event : changeLog.getChangeEvents())
        {
            CmisObjectType object = new CmisObjectType();
            CmisPropertiesType propertiesType = new CmisPropertiesType();
            object.setProperties(propertiesType);
            propertiesType.getProperty().add(propertiesUtil.createProperty(CMISDictionaryModel.PROP_OBJECT_ID, CMISDataTypeEnum.ID, event.getObjectId()));
            if (nodeService.exists(event.getChangedNode()) && includeAce)
            {
                appendWithAce(event.getChangedNode(), object);
            }
            CmisChangeEventType changeInfo = new CmisChangeEventType();
            XMLGregorianCalendar modificationDate = propertiesUtil.convert(event.getChangeTime());
            changeInfo.setChangeType(changesTypeMapping.get(event.getChangeType()));
            changeInfo.setChangeTime(modificationDate);
            object.setChangeEventInfo(changeInfo);
            result.add(object);
        }
    }
}
