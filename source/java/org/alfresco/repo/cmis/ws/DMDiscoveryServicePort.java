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
import java.util.List;
import java.util.Map;

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISResultSet;
import org.alfresco.cmis.CMISResultSetColumn;
import org.alfresco.cmis.CMISResultSetMetaData;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.repo.cmis.PropertyFilter;

/**
 * Port for Discovery service.
 * 
 * @author Dmitry Lazurkin
 */
@javax.jws.WebService(name = "DiscoveryServicePort", serviceName = "DiscoveryService", portName = "DiscoveryServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901", endpointInterface = "org.alfresco.repo.cmis.ws.DiscoveryServicePort")
public class DMDiscoveryServicePort extends DMAbstractServicePort implements DiscoveryServicePort
{

    /**
     * Queries the repository for queryable object based on properties or an optional full-text string. Relationship objects are not queryable. Content-streams are not returned as
     * part of query
     * 
     * @param parameters query parameters
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public QueryResponse query(CmisQueryType parameters) throws CmisException
    {
        checkRepositoryId(parameters.getRepositoryId());

        // TODO: searchAllVersions, returnAllowableActions
        CMISQueryOptions options = new CMISQueryOptions(parameters.getStatement(), cmisService.getDefaultRootStoreRef());

        if (parameters.getSkipCount() != null)
        {
            options.setSkipCount(parameters.getSkipCount().intValue());
        }

        if (parameters.getPageSize() != null)
        {
            options.setMaxItems(parameters.getPageSize().intValue());
        }

        // execute query
        CMISResultSet resultSet = cmisQueryService.query(options);
        CMISResultSetMetaData metaData = resultSet.getMetaData();
        CMISResultSetColumn[] columns = metaData.getColumns();
        PropertyFilter filter = new PropertyFilter();

        // build query response
        QueryResponse response = new QueryResponse();

        // for each row...
        for (CMISResultSetRow row : resultSet)
        {
            CmisPropertiesType properties = new CmisPropertiesType();
            Map<String, Serializable> values = row.getValues();

            // for each column...
            for (CMISResultSetColumn column : columns)
            {
                CMISDataTypeEnum type = column.getCMISDataType();
                if (type == CMISDataTypeEnum.BOOLEAN)
                {
                    addBooleanProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISDataTypeEnum.DATETIME)
                {
                    addDateTimeProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISDataTypeEnum.DECIMAL)
                {
                    addDecimalProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISDataTypeEnum.ID)
                {
                    addIDProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISDataTypeEnum.INTEGER)
                {
                    addIntegerProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISDataTypeEnum.STRING)
                {
                    addStringProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISDataTypeEnum.URI)
                {
                    addURIProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISDataTypeEnum.XML)
                {
                    // TODO:
                    throw cmisObjectsUtils.createCmisException("", EnumServiceException.NOT_SUPPORTED);
                }
                else if (type == CMISDataTypeEnum.HTML)
                {
                    // TODO:
                    throw cmisObjectsUtils.createCmisException("", EnumServiceException.NOT_SUPPORTED);
                }
            }

            CmisObjectType object = new CmisObjectType();
            object.setProperties(properties);
            response.getObject().add(object);
        }

        response.setHasMoreItems(resultSet.hasMore());
        return response;
    }

    public void getContentChanges(String repositoryId, Holder<String> changeToken, BigInteger maxItems, Boolean includeACL, Boolean includeProperties, String filter,
            Holder<List<CmisObjectType>> changedObject) throws CmisException
    {
        // TODO
    }
}
