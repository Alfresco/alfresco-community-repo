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
package org.alfresco.repo.web.scripts.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.util.ModelUtil;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StringUtils;

/**
 * Base class for all workflow REST API implementations.
 * 
 * @author Nick Smith
 * @author Gavin Cornwell
 * @since 3.4
 */
public abstract class AbstractWorkflowWebscript extends DeclarativeWebScript
{
    public static final String NULL = "null";
    public static final String EMPTY = "";
    
    public static final String PARAM_MAX_ITEMS = "maxItems";
    public static final String PARAM_SKIP_COUNT = "skipCount";
    public static final String PARAM_EXCLUDE = "exclude";
    
    // used for results pagination: indicates that all items from list should be returned
    public static final int DEFAULT_MAX_ITEMS = -1;  
    
    // used for results pagination: indicates that no items should be skipped
    public static final int DEFAULT_SKIP_COUNT = 0;

    protected NamespaceService namespaceService;
    protected NodeService nodeService;
    protected PersonService personService;
    protected DictionaryService dictionaryService;
    protected AuthenticationService authenticationService;
    protected AuthorityService authorityService;
    protected WorkflowService workflowService;
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        WorkflowModelBuilder modelBuilder = new WorkflowModelBuilder(namespaceService, nodeService, authenticationService, 
                    personService, workflowService);
        return buildModel(modelBuilder, req, status, cache);
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    public void setWorkflowService(WorkflowService workflowService)
    {
        this.workflowService = workflowService;
    }
    
    /**
     * This method uses a {@link WorkflowModelBuilder} to build up the model to return.
     * @param modelBuilder A {@link WorkflowModelBuilder}.
     * @param req the {@link WebScriptRequest}
     * @param status the {@link Status}
     * @param cache the {@link Cache}
     * @return the data model.
     */
    protected abstract Map<String, Object> buildModel(
            WorkflowModelBuilder modelBuilder,
            WebScriptRequest req,
            Status status, Cache cache);
    
    
    /**
     * Processes the given date filter parameter from the provided webscript request.
     * 
     * If the parameter is present but set to an empty string or to "null" the
     * date is added to the given filters Map as "", if the parameter
     * contains an ISO8601 date it's added as a Date object to the filters.
     * 
     * @param req The WebScript request
     * @param paramName The name of the parameter to look for
     * @param filters Map of filters to add the date to
     */
    protected void processDateFilter(WebScriptRequest req, String paramName, Map<String, Object> filters)
    {
        // TODO: support other keywords i.e. today, tomorrow
        
        String dateParam = req.getParameter(paramName);
        if (dateParam != null)
        {
            Object date = EMPTY;
            
            if (!EMPTY.equals(dateParam) && !NULL.equals(dateParam))
            {
                date = getDateParameter(req, paramName);
            }
            
            filters.put(paramName, date);
        }
    }
    
    /**
     * Retrieves the named paramter as a date.
     * 
     * @param req The WebScript request
     * @param paramName The name of parameter to look for
     * @return The request parameter value or null if the parameter is not present
     */
    protected Date getDateParameter(WebScriptRequest req, String paramName)
    {
        String dateString = req.getParameter(paramName);

        if (dateString != null)
        {
            try
            {
                return ISO8601DateFormat.parse(dateString.replaceAll(" ", "+"));
            }
            catch (Exception e)
            {
                String msg = "Invalid date value: " + dateString;
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
            }
        }
        return null;
    }
    
    /**
     * Retrieves the named parameter as an integer, if the parameter is not present the default value is returned
     * 
     * @param req The WebScript request
     * @param paramName The name of parameter to look for
     * @param defaultValue The default value that should be returned if parameter is not present in request or if it is not positive
     * @return The request parameter or default value
     */
    protected int getIntParameter(WebScriptRequest req, String paramName, int defaultValue)
    {
        String paramString = req.getParameter(paramName);
        
        if (paramString != null)
        {
            try
            {
                int param = Integer.valueOf(paramString);
                
                if (param > 0)
                {
                    return param;
                }
            }
            catch (NumberFormatException e) 
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }
        
        return defaultValue;
    }
    
    /**
     * Builds the results model, applying pagination to the results if necessary.
     * 
     * @param modelBuilder WorkflowModelBuilder instance to use
     * @param req The WebScript request
     * @param dataPropertyName The name of the property to use in the model
     * @param results The full set of results
     * @return List of results to return to the callee
     */
    protected Map<String, Object> createResultModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req, 
                String dataPropertyName, List<Map<String, Object>> results)
    {
        int totalItems = results.size();
        int maxItems = getIntParameter(req, PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS);
        int skipCount = getIntParameter(req, PARAM_SKIP_COUNT, DEFAULT_SKIP_COUNT);
        
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(dataPropertyName, applyPagination(results, maxItems, skipCount));
        
        if (maxItems != DEFAULT_MAX_ITEMS || skipCount != DEFAULT_SKIP_COUNT)
        {
            // maxItems or skipCount parameter was provided so we need to include paging into response
            model.put("paging", ModelUtil.buildPaging(totalItems, maxItems == DEFAULT_MAX_ITEMS ? totalItems : maxItems, skipCount));
        }
        
        return model;
    }
    
    /**
     * Make the pagination for given list of objects
     * 
     * @param results the initial list of objects for pagination
     * @param maxItems maximum count of elements that should be included in paging result 
     * @param skipCount the count of elements that should be skipped
     * @return List of paginated results
     */
    protected List<Map<String, Object>> applyPagination(List<Map<String, Object>> results, int maxItems, int skipCount)
    {
        if (maxItems == DEFAULT_MAX_ITEMS && skipCount == DEFAULT_SKIP_COUNT)
        {
            // no need to make pagination
            return results;
        }
        
        // Do the paging
        return ModelUtil.page(results, maxItems, skipCount);
    }
    
    /**
     * Determines whether the given date is a match for the given filter value.
     * 
     * @param date The date to check against
     * @param filterValue The value of the filter, either an empty String or a Date object
     * @param dateBeforeFilter true to test the date is before the filterValue, 
     *        false to test the date is after the filterValue
     * @return true if the date is a match for the filterValue
     */
    protected boolean isDateMatchForFilter(Date date, Object filterValue, boolean dateBeforeFilter)
    {
        boolean match = true;
        
        if (filterValue.equals(EMPTY))
        {
            if (date != null)
            {
                match = false;
            }
        }
        else
        {
            if (date == null)
            {
                match = false;
            }
            else
            {
                if (dateBeforeFilter)
                {
                    if (date.getTime() >= ((Date)filterValue).getTime())
                    {
                        match = false;
                    }
                }
                else
                {
                    if (date.getTime() <= ((Date)filterValue).getTime())
                    {
                        match = false;
                    }
                }
            }
        }
        
        return match;
    }
    
    /**
     * Helper class to check for excluded items.
     */
    public class ExcludeFilter
    {
        private static final String WILDCARD = "*";
        
        private List<String> exactFilters;
        private List<String> wilcardFilters;
        private boolean containsWildcards = false;
        
        /**
         * Creates a new ExcludeFilter
         * 
         * @param filters Comma separated list of filters which can optionally
         *        contain wildcards
         */
        public ExcludeFilter(String filters)
        {
            // tokenize the filters
            String[] filterArray = StringUtils.tokenizeToStringArray(filters, ",");
            
            // create a list of exact filters and wildcard filters
            this.exactFilters = new ArrayList<String>(filterArray.length);
            this.wilcardFilters = new ArrayList<String>(filterArray.length);
            
            for (String filter : filterArray)
            {
                if (filter.endsWith(WILDCARD))
                {
                    // at least one wildcard is present
                    this.containsWildcards = true;
                    
                    // add the filter without the wildcard
                    this.wilcardFilters.add(filter.substring(0, 
                                (filter.length()-WILDCARD.length())));
                }
                else
                {
                    // add the exact filter
                    this.exactFilters.add(filter);
                }
            }
        }
        
        /**
         * Determines whether the given item matches one of
         * the filters.
         * 
         * @param item The item to check
         * @return true if the item matches one of the filters
         */
        public boolean isMatch(String item)
        {
            // see whether there is an exact match
            boolean match = this.exactFilters.contains(item);
            
            // if there wasn't an exact match and wildcards are present
            if (item != null && !match && this.containsWildcards)
            {
                for (String wildcardFilter : this.wilcardFilters)
                {
                    if (item.startsWith(wildcardFilter))
                    {
                        match = true;
                        break;
                    }
                }
            }
            
            return match;
        }
    }
}