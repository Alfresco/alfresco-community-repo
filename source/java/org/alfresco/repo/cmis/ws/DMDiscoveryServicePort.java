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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISResultSetColumn;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.repo.cmis.ws.utils.AlfrescoObjectType;

/**
 * Port for Discovery service.
 * 
 * @author Dmitry Lazurkin
 */
@javax.jws.WebService(name = "DiscoveryServicePort", serviceName = "DiscoveryService", portName = "DiscoveryServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.DiscoveryServicePort")
public class DMDiscoveryServicePort extends DMAbstractServicePort implements DiscoveryServicePort
{
    /**
     * Queries the repository for queryable object based on properties or an optional full-text string. Relationship objects are not queryable. Content-streams are not returned as
     * part of query
     * 
     * @param parameters query parameters
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public QueryResponse query(Query parameters) throws CmisException
    {
        checkRepositoryId(parameters.getRepositoryId());

        // TODO: searchAllVersions, includeRelationships, includeAllowableActions, includeRenditions
        CMISQueryOptions options = new CMISQueryOptions(parameters.getStatement(), cmisService.getDefaultRootStoreRef());

        if (parameters.getSkipCount() != null && parameters.getSkipCount().getValue() != null)
        {
            options.setSkipCount(parameters.getSkipCount().getValue().intValue());
        }

        if (parameters.getMaxItems() != null && parameters.getMaxItems().getValue() != null)
        {
            options.setMaxItems(parameters.getMaxItems().getValue().intValue());
        }
        boolean includeAllowableActions = (null != parameters.getIncludeAllowableActions()) ? (parameters.getIncludeAllowableActions().getValue()) : (false);

        // execute query
        // TODO: If the select clause includes properties from more than a single type reference, then the repository SHOULD throw an exception if includeRelationships or
        // includeAllowableActions is specified as true.
        CMISResultSet resultSet = cmisQueryService.query(options);
        CMISResultSetColumn[] columns = resultSet.getMetaData().getColumns();

        // build query response
        QueryResponse response = new QueryResponse();
        response.setObjects(new CmisObjectListType());

        // for each row...
        for (CMISResultSetRow row : resultSet)
        {
            CmisPropertiesType properties = new CmisPropertiesType();
            Map<String, Serializable> values = row.getValues();

            // for each column...
            for (CMISResultSetColumn column : columns)
            {
                CmisProperty property = propertiesUtil.createProperty(column.getName(), column.getCMISDataType(), values.get(column.getName()));
                if (property != null)
                {
                    properties.getProperty().add(property);
                }
            }

            CmisObjectType object = new CmisObjectType();
            object.setProperties(properties);
            if (includeAllowableActions)
            {
                Object identifier = cmisObjectsUtils.getIdentifierInstance((String) values.get(CMISDictionaryModel.PROP_OBJECT_ID), AlfrescoObjectType.DOCUMENT_OR_FOLDER_OBJECT);
                object.setAllowableActions(determineObjectAllowableActions(identifier));
            }
            response.getObjects().getObjects().add(object);
        }
        // TODO: response.getObjects().setNumItems(value);
        response.getObjects().setHasMoreItems(resultSet.hasMore());
        return response;
    }

    public void getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties, String filter, Boolean includePolicyIds, Boolean includeACL,
            BigInteger maxItems, CmisExtensionType extension, Holder<CmisObjectListType> objects) throws CmisException
    {
        // TODO: implement me
        throw cmisObjectsUtils.createCmisException("Not implemented", EnumServiceException.RUNTIME);
    }

}
