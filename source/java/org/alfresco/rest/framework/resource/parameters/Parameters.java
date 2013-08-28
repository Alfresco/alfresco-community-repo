package org.alfresco.rest.framework.resource.parameters;

import java.util.List;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.apache.poi.ss.formula.functions.T;


/**
 * Parameters passed into a request.
 *
 * @author Gethin James
 */
public interface Parameters
{   
    /**
     * Gets a single request parameter passed in by the user.
     * Currently doesn't support multiple values.
     * @param parameterName
     * @return String The Parameter value
     */
    public String getParameter(String parameterName);
    
    /**
     * Gets a single request parameter passed in by the user.
     * Attempts to convert the parameter to the specified type.
     * If unable to convert the parameter to the specified type then throws an InvalidArgumentException.
     * Currently doesn't support multiple values.
     * @param parameterName
     * @param clazz - type to use for conversion.
     * @return The Parameter value
     * @throws InvalidArgumentException
     */
    public T getParameter(String parameterName, Class<T> clazz) throws InvalidArgumentException;
    
    /**
     * Returns a representation of the Paging of collections of resources, with skip count and max items. 
     * See {@link Paging}
     * Specified by the "skipCount" and "maxItems" request parameters.
     * @return Paging Paging information
     */
    public Paging getPaging();
    
    /**
     * Returns a List of {@link SortColumn} for sorting properties.
     * Specified by the "orderBy" request parameter.
     * @return List of {@link SortColumn}
     */
    public List<SortColumn> getSorting();
    
    /**
     * Returns a {@link BeanPropertiesFilter} for filtering out properties.
     * Specified by the "properties" request parameter.
     * @return BeanPropertiesFilter {@link BeanPropertiesFilter}
     */
    public BeanPropertiesFilter getFilter();
    
    /**
     * Indicates if the specified property was requested.
     * @param propertyName the property
     * Specified as part of the url request.
     * @return true if the propertyName was specified as part of the url request
     */
    public boolean hasBinaryProperty(String propertyName);
    
    /**
     * Gets the name of the property that was requested.
     * @return String the propertyName
     */
    public String getBinaryProperty();
    
    /**
     * Represents a Query specified by the client.
     * Specified by the "WHERE" request parameter.
     * @return Query {@link Query}
     */
    public Query getQuery();
    
    /**
     * A list of property names passed in the request using the json pointer syntax
     * Specified by the "SELECT" request parameter.
     * @return List<String> the propertyNames
     */
    public List<String> getSelectedProperties();

    /**
     * Gets the basic information about content, typically taken from a HTTPServletRequest.
     * @return BasicContentInfo the content info
     */
	BasicContentInfo getContentInfo();
}
