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
import java.util.Map;

import org.alfresco.cmis.CMISPropertyTypeEnum;
import org.alfresco.cmis.search.CMISQueryOptions;
import org.alfresco.cmis.search.CMISResultSet;
import org.alfresco.cmis.search.CMISResultSetColumn;
import org.alfresco.cmis.search.CMISResultSetMetaData;
import org.alfresco.cmis.search.CMISResultSetRow;
import org.alfresco.repo.cmis.PropertyFilter;

/**
 * Port for Discovery service.
 * 
 * @author Dmitry Lazurkin
 */
@javax.jws.WebService(name = "DiscoveryServicePort", serviceName = "DiscoveryService", portName = "DiscoveryServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.DiscoveryServicePort")
public class DMDiscoveryServicePort extends DMAbstractServicePort implements DiscoveryServicePort
{

    /**
     * Queries the repository for queryable object based on properties or an optional full-text string. Relationship objects are not queryable. Content-streams are not returned as
     * part of query
     * 
     * @param parameters query parameters
     * @return collection of CmisObjectType and boolean hasMoreItems
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public QueryResponse query(CmisQueryType parameters) throws PermissionDeniedException, UpdateConflictException, OperationNotSupportedException, InvalidArgumentException,
            RuntimeException, ConstraintViolationException
    {
        // TODO: searchAllVersions, returnAllowableActions, includeRelationships
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
                CMISPropertyTypeEnum type = column.getPropertyType();
                if (type == CMISPropertyTypeEnum.BOOLEAN)
                {
                    addBooleanProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISPropertyTypeEnum.DATETIME)
                {
                    addDateTimeProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISPropertyTypeEnum.DECIMAL)
                {
                    addDecimalProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISPropertyTypeEnum.ID)
                {
                    addIDProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISPropertyTypeEnum.INTEGER)
                {
                    addIntegerProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISPropertyTypeEnum.STRING)
                {
                    addStringProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISPropertyTypeEnum.URI)
                {
                    addURIProperty(properties, filter, column.getName(), values);
                }
                else if (type == CMISPropertyTypeEnum.XML)
                {
                    // TODO:
                    throw new UnsupportedOperationException();
                }
                else if (type == CMISPropertyTypeEnum.HTML)
                {
                    // TODO:
                    throw new UnsupportedOperationException();
                }
            }
            
            CmisObjectType object = new CmisObjectType();
            object.setProperties(properties);
            response.getObject().add(object);
        }
        
        response.setHasMoreItems(resultSet.hasMore());
        return response;
    }

}
