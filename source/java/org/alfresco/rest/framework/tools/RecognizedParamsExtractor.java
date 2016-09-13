/*-
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

package org.alfresco.rest.framework.tools;

import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.parameters.InvalidSelectException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryImpl;
import org.alfresco.rest.framework.resource.parameters.where.WhereCompiler;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.RewriteCardinalityException;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/*
 * Extracts recognized parameters from the HTTP request.
 *
 * @author Gethin James
 */
public interface RecognizedParamsExtractor
{
    public static final String PARAM_RELATIONS = "relations";
    public static final String PARAM_FILTER_FIELDS = "fields";

    @Deprecated
    public static final String PARAM_FILTER_PROPERTIES = "properties";

    public static final String PARAM_PAGING_SKIP = "skipCount";
    public static final String PARAM_PAGING_MAX = "maxItems";
    public static final String PARAM_ORDERBY = "orderBy";
    public static final String PARAM_WHERE = "where";
    public static final String PARAM_SELECT = "select";
    public static final String PARAM_INCLUDE = "include";
    public static final String PARAM_INCLUDE_SOURCE_ENTITY = "includeSource";
    public static final List<String> KNOWN_PARAMS = Arrays
                .asList(PARAM_RELATIONS, PARAM_FILTER_PROPERTIES, PARAM_FILTER_FIELDS, PARAM_PAGING_SKIP, PARAM_PAGING_MAX, PARAM_ORDERBY,
                            PARAM_WHERE, PARAM_SELECT, PARAM_INCLUDE_SOURCE_ENTITY);

    default Log rpeLogger()
    {
        return LogFactory.getLog(this.getClass());
    }

    /**
     * Finds the formal set of params that any rest service could potentially have passed in as request params
     *
     * @param req WebScriptRequest
     * @return RecognizedParams a POJO containing the params for use with the Params objects
     */
    default Params.RecognizedParams getRecognizedParams(WebScriptRequest req)
    {
        Paging paging = findPaging(req);
        List<SortColumn> sorting = getSort(req.getParameter(PARAM_ORDERBY));
        Map<String, BeanPropertiesFilter> relationFilter = getRelationFilter(req.getParameter(PARAM_RELATIONS));
        Query whereQuery = getWhereClause(req.getParameter(PARAM_WHERE));
        Map<String, String[]> requestParams = getRequestParameters(req);
        boolean includeSource = Boolean.valueOf(req.getParameter(PARAM_INCLUDE_SOURCE_ENTITY));

        List<String> includedFields = getIncludeClause(req.getParameter(PARAM_INCLUDE));
        List<String> selectFields = getSelectClause(req.getParameter(PARAM_SELECT));

        String fields = req.getParameter(PARAM_FILTER_FIELDS);
        String properties = req.getParameter(PARAM_FILTER_PROPERTIES);

        if ((fields != null) && (properties != null))
        {
            if (rpeLogger().isWarnEnabled())
            {
                rpeLogger().warn("Taking 'fields' param [" + fields + "] and ignoring deprecated 'properties' param [" + properties + "]");
            }
        }

        BeanPropertiesFilter filter = getFilter((fields != null ? fields : properties), includedFields);

        return new Params.RecognizedParams(requestParams, paging, filter, relationFilter, includedFields, selectFields, whereQuery, sorting,
                    includeSource);
    }

    /**
     * Takes the web request and looks for a "fields" parameter (otherwise deprecated "properties" parameter).
     * Parses the parameter and produces a list of bean properties to use as a filter A
     * SimpleBeanPropertyFilter it returned that uses the bean properties. If no
     * filter param is set then a default BeanFilter is returned that will never
     * filter fields (ie. Returns all bean properties).
     * If selectList is provided then it will take precedence (ie. be included) over the fields/properties filter
     * for top-level entries (bean properties).
     * For example, this will return entries from both select & properties, eg.
     * select=abc,def&properties=id,name,ghi
     * Note: it should be noted that API-generic "fields" clause does not currently work for sub-entries.
     * Hence, even if the API-specific "select" clause allows selection of a sub-entries this cannot be used
     * with "fields" filtering. For example, an API-specific method may implement and return "abc/blah", eg.
     * select=abc/blah
     * However the following will not return "abc/blah" if used with fields filtering, eg.
     * select=abc/blah&fields=id,name,ghi
     * If fields filtering is desired then it would require "abc" to be selected and returned as a whole, eg.
     * select=abc&fields=id,name,ghi
     *
     * @param filterParams
     * @param selectList
     * @return
     */
    default BeanPropertiesFilter getFilter(String filterParams, List<String> selectList)
    {
        if (filterParams != null)
        {
            StringTokenizer st = new StringTokenizer(filterParams, ",");
            Set<String> filteredProperties = new HashSet<String>(st.countTokens());
            while (st.hasMoreTokens())
            {
                filteredProperties.add(st.nextToken());
            }

            // if supplied, the select takes precedence over the filter (fields/properties) for top-level bean properties
            if (selectList != null)
            {
                for (String select : selectList)
                {
                    String[] split = select.split("/");
                    filteredProperties.add(split[0]);
                }
            }

            rpeLogger().debug("Filtering using the following properties: " + filteredProperties);
            BeanPropertiesFilter filter = new BeanPropertiesFilter(filteredProperties);
            return filter;
        }
        return BeanPropertiesFilter.ALLOW_ALL;
    }

    /**
     * Takes the "select" parameter and turns it into a List<String> property names
     *
     * @param selectParam String
     * @return bean property names potentially using JSON Pointer syntax
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    default List<String> getSelectClause(String selectParam) throws InvalidArgumentException
    {
        return getClause(selectParam, "SELECT");
    }

    /**
     * Takes the "include" parameter and turns it into a List<String> property names
     *
     * @param includeParam String
     * @return bean property names potentially using JSON Pointer syntax
     */
    @SuppressWarnings("unchecked")
    default List<String> getIncludeClause(String includeParam) throws InvalidArgumentException
    {
        return getClause(includeParam, "INCLUDE");
    }

    /**
     * Gets the clause specificed in paramName
     *
     * @param param
     * @param paramName
     * @return bean property names potentially using JSON Pointer syntax
     */
    default List<String> getClause(String param, String paramName)
    {
        if (param == null)
            return Collections.emptyList();

        try
        {
            CommonTree selectedPropsTree = WhereCompiler.compileSelectClause(param);
            if (selectedPropsTree instanceof CommonErrorNode)
            {
                rpeLogger().debug("Error parsing the " + paramName + " clause " + selectedPropsTree);
                throw new InvalidSelectException(paramName, selectedPropsTree);
            }
            if (selectedPropsTree.getChildCount() == 0 && !selectedPropsTree.getText().isEmpty())
            {
                return Arrays.asList(selectedPropsTree.getText());
            }
            List<Tree> children = (List<Tree>) selectedPropsTree.getChildren();
            if (children != null && !children.isEmpty())
            {
                List<String> properties = new ArrayList<String>(children.size());
                for (Tree child : children)
                {
                    properties.add(child.getText());
                }
                return properties;
            }
        }
        catch (RewriteCardinalityException re)
        {
            //Catch any error so it doesn't get thrown up the stack
            rpeLogger().debug("Unhandled Error parsing the " + paramName + " clause: " + re);
        }
        catch (RecognitionException e)
        {
            rpeLogger().debug("Error parsing the \"+paramName+\" clause: " + param);
        }
        catch (InvalidQueryException iqe)
        {
            throw new InvalidSelectException(paramName, iqe.getQueryParam());
        }
        //Default to throw out an invalid query
        throw new InvalidSelectException(paramName, param);
    }

    /**
     * Takes the "where" parameter and turns it into a Java Object that can be used for querying
     *
     * @param whereParam String
     * @return Query a parsed version of the where clause, represented in Java
     */
    default Query getWhereClause(String whereParam) throws InvalidQueryException
    {
        if (whereParam == null)
            return QueryImpl.EMPTY;

        try
        {
            CommonTree whereTree = WhereCompiler.compileWhereClause(whereParam);
            if (whereTree instanceof CommonErrorNode)
            {
                rpeLogger().debug("Error parsing the WHERE clause " + whereTree);
                throw new InvalidQueryException(whereTree);
            }
            return new QueryImpl(whereTree);
        }
        catch (RewriteCardinalityException re)
        {  //Catch any error so it doesn't get thrown up the stack
            rpeLogger().info("Unhandled Error parsing the WHERE clause: " + re);
        }
        catch (RecognitionException e)
        {
            whereParam += ", " + WhereCompiler.resolveMessage(e);
            rpeLogger().info("Error parsing the WHERE clause: " + whereParam);
        }
        //Default to throw out an invalid query
        throw new InvalidQueryException(whereParam);
    }

    /**
     * Takes the Sort parameter as a String and parses it into a List of SortColumn objects.
     * The format is a comma seperated list of "columnName sortDirection",
     * e.g. "name DESC, age ASC".  It is not case sensitive and the sort direction is optional
     * It default to sort ASCENDING.
     *
     * @param sortParams - String passed in on the request
     * @return - the sort columns or an empty list if the params were invalid.
     */
    default List<SortColumn> getSort(String sortParams)
    {
        if (sortParams != null)
        {
            StringTokenizer st = new StringTokenizer(sortParams, ",");
            List<SortColumn> sortedColumns = new ArrayList<SortColumn>(st.countTokens());
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                StringTokenizer columnDesc = new StringTokenizer(token, " ");
                if (columnDesc.countTokens() <= 2)
                {
                    String columnName = columnDesc.nextToken();
                    String sortOrder = SortColumn.ASCENDING;
                    if (columnDesc.hasMoreTokens())
                    {
                        String sortDef = columnDesc.nextToken().toUpperCase();
                        if (SortColumn.ASCENDING.equals(sortDef) || SortColumn.DESCENDING.equals(sortDef))
                        {
                            sortOrder = sortDef;
                        }
                        else
                        {
                            rpeLogger().debug("Invalid sort order direction (" + sortDef + ").  Valid values are " + SortColumn.ASCENDING + " or "
                                        + SortColumn.DESCENDING + ".");
                            throw new InvalidArgumentException("Unknown sort order direction '"+sortDef+"', expected asc or desc");
                        }
                    }
                    sortedColumns.add(new SortColumn(columnName, SortColumn.ASCENDING.equals(sortOrder)));
                }
                else
                {
                    rpeLogger().debug("Invalid sort order definition (" + token + ")");
                    throw new InvalidArgumentException("Unknown sort order definition '" + token + "', expected 'field1,field2' or 'field1 asc,field2 desc' or similar");
                }
                // filteredProperties.add();
            }
            //            logger.debug("Filtering using the following properties: " + filteredProperties);
            //            BeanPropertiesFilter filter = new BeanPropertiesFilter(filteredProperties);
            return sortedColumns;
        }
        return Collections.emptyList();
    }

    /**
     * Find paging setings based on the request parameters.
     *
     * @param req
     * @return Paging
     */
    default Paging findPaging(WebScriptRequest req)
    {
        int skipped = Paging.DEFAULT_SKIP_COUNT;
        int max = Paging.DEFAULT_MAX_ITEMS;
        String skip = req.getParameter(PARAM_PAGING_SKIP);
        String maxItems = req.getParameter(PARAM_PAGING_MAX);

        try
        {
            if (skip != null) { skipped = Integer.parseInt(skip);}
            if (maxItems != null) { max = Integer.parseInt(maxItems); }
            if (skipped < 0)
            {
                throw new InvalidArgumentException("Negative values not supported for skipCount.");
            }
            if (max < 1)
            {
                throw new InvalidArgumentException("Only positive values supported for maxItems.");
            }
        }
        catch (NumberFormatException error)
        {
            String errorMsg = "Invalid paging parameters skipCount: " + skip + ", maxItems:" + maxItems;
            if (rpeLogger().isDebugEnabled())
            {
                rpeLogger().debug(errorMsg);
            }
            if (skip == null)
            {
                errorMsg = "Invalid paging parameter maxItems:" + maxItems;
            }
            if (maxItems == null)
            {
                errorMsg = "Invalid paging parameter skipCount:" + skip;
            }
            throw new InvalidArgumentException(errorMsg);
        }

        return Paging.valueOf(skipped, max);
    }

    /**
     * Takes the web request and looks for a "fields" parameter  (otherwise deprecated "properties" parameter).
     * Parses the parameter and produces a list of bean properties to use as a filter A
     * SimpleBeanPropertyFilter it returned that uses the bean properties. If no
     * filter param is set then a default BeanFilter is returned that will never
     * filter fields (ie. Returns all bean properties).
     *
     * @param filterParams String
     * @return BeanPropertyFilter - if no parameter then returns a new
     * ReturnAllBeanProperties class
     */
    default BeanPropertiesFilter getFilter(String filterParams)
    {
        return getFilter(filterParams, null);
    }

    /**
     * Takes the web request and looks for a "relations" parameter Parses the
     * parameter and produces a list of bean properties to use as a filter A
     * SimpleBeanPropertiesFilter it returned that uses the properties If no
     * filter param is set then a default BeanFilter is returned that will never
     * filter properties (ie. Returns all bean properties).
     *
     * @param filterParams String
     * @return BeanPropertiesFilter - if no parameter then returns a new
     * ReturnAllBeanProperties class
     */
    default Map<String, BeanPropertiesFilter> getRelationFilter(String filterParams)
    {
        if (filterParams != null)
        {
            // Split by a comma when not in a bracket
            String[] relations = filterParams.split(",(?![^()]*+\\))");
            Map<String, BeanPropertiesFilter> filterMap = new HashMap<String, BeanPropertiesFilter>(relations.length);

            for (String relation : relations)
            {
                int bracketLocation = relation.indexOf("(");
                if (bracketLocation != -1)
                {
                    // We have properties
                    String relationKey = relation.substring(0, bracketLocation);
                    String props = relation.substring(bracketLocation + 1, relation.length() - 1);
                    filterMap.put(relationKey, getFilter(props));
                }
                else
                {
                    // no properties so just get the String
                    filterMap.put(relation, getFilter(null));
                }
            }
            return filterMap;
        }
        return Collections.emptyMap();
    }

    /**
     * Finds all request parameters that aren't already know about (eg. not paging or filter params)
     * and returns them for use.
     *
     * @param req - the WebScriptRequest object
     * @return the request parameters
     */
    default Map<String, String[]> getRequestParameters(WebScriptRequest req)
    {
        if (req != null)
        {
            String[] paramNames = req.getParameterNames();
            if (paramNames != null)
            {
                Map<String, String[]> requestParameteters = new HashMap<String, String[]>(paramNames.length);

                for (int i = 0; i < paramNames.length; i++)
                {
                    String paramName = paramNames[i];
                    if (!KNOWN_PARAMS.contains(paramName))
                    {
                        String[] vals = req.getParameterValues(paramName);
                        requestParameteters.put(paramName, vals);
                    }
                }
                return requestParameteters;
            }
        }

        return Collections.emptyMap();
    }

}
