/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.framework.resource.parameters;

import java.util.List;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Parameters passed into a request.
 *
 * @author Gethin James
 */
public interface Parameters
{   
    /**
     * Gets a single request query parameter passed in by the user.
     * Currently doesn't support multiple values.
     * @param parameterName String
     * @return String The Parameter value
     */
    String getParameter(String parameterName);
    
    /**
     * Gets a single request parameter passed in by the user.
     * Attempts to convert the parameter to the specified type.
     * If unable to convert the parameter to the specified type then throws an InvalidArgumentException.
     * Currently doesn't support multiple values.
     * @param parameterName String
     * @param clazz - type to use for conversion.
     * @return The Parameter value
     * @throws InvalidArgumentException
     */
    T getParameter(String parameterName, Class<T> clazz) throws InvalidArgumentException;
    
    /**
     * Returns a representation of the Paging of collections of resources, with skip count and max items. 
     * See {@link Paging}
     * Specified by the "skipCount" and "maxItems" request parameters.
     * @return Paging Paging information
     */
    Paging getPaging();
    
    /**
     * Returns a List of {@link SortColumn} for sorting properties.
     * Specified by the "orderBy" request parameter.
     * @return List of {@link SortColumn}
     */
    List<SortColumn> getSorting();
    
    /**
     * Returns a {@link BeanPropertiesFilter} for filtering out properties.
     * Specified by the "properties" request parameter.
     * @return BeanPropertiesFilter {@link BeanPropertiesFilter}
     */
    BeanPropertiesFilter getFilter();
    
    /**
     * Indicates if the specified property was requested.
     * @param propertyName the property
     * Specified as part of the url request.
     * @return true if the propertyName was specified as part of the url request
     */
    boolean hasBinaryProperty(String propertyName);
    
    /**
     * Gets the name of the property that was requested.
     * @return String the propertyName
     */
    String getBinaryProperty();

    /**
     * Indicates if the source entity should be includes in the request.
     * This will normally be done by the framework but implentations may prefer
     * to do it themselves.
     * @return true if the source should be included.
     */
    boolean includeSource();

    /**
     * Represents a Query specified by the client.
     * Specified by the "WHERE" request parameter.
     * @return Query {@link Query}
     */
    Query getQuery();
    
    /**
     * A list of property names passed in the request using the json pointer syntax
     * Specified by the "SELECT" request parameter.
     * @return List<String> the propertyNames
     */
    @Deprecated
    List<String> getSelectedProperties();

    /**
     * A list of property names passed in the request using the json pointer syntax
     * Specified by the "INCLUDE" request parameter.
     * @return List<String> the propertyNames
     */
    List<String> getInclude();

    /**
     * Gets the basic information about content, typically taken from a HTTPServletRequest.
     * @return BasicContentInfo the content info
     */
    BasicContentInfo getContentInfo();

    /**
     * Gets access to the entire webscript request.
     * @return WebScriptRequest
     */
    WebScriptRequest getRequest();
}
