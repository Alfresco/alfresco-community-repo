/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.web.scripts.facet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.ScriptPagingDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the "facetable-properties.get" web script.
 * 
 * @since 5.0
 * @author Neil Mc Erlean
 */
public class FacetablePropertiesGet extends AbstractSolrFacetConfigAdminWebScript
{
    public static final Log logger = LogFactory.getLog(FacetablePropertiesGet.class);
    public static final String PROPERTIES_KEY = "properties";
    
    private static final String QUERY_PARAM_MAX_ITEMS                  = "maxItems";
    private static final String QUERY_PARAM_SKIP_COUNT                 = "skipCount";
    private static final int    DEFAULT_MAX_ITEMS_PER_PAGE = 50;
    
    private static final String TEMPLATE_VAR_CLASSNAME = "classname";
    private static final String QUERY_PARAM_NAMESPACE  = "nsp";
    
    private DictionaryService dictionaryService;
    private NamespaceService  namespaceService;
    
    public void setDictionaryService(DictionaryService service) { this.dictionaryService = service; }
    public void setNamespaceService (NamespaceService service)  { this.namespaceService  = service; }
    
    @Override protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache)
    {
        // Allow all authenticated users view the filters
        return unprotectedExecuteImpl(req, status, cache);
    }
    
    @Override protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // There are multiple defined URIs for this REST endpoint. Some define a "classname" template var.
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        final String contentClassName = templateVars.get(TEMPLATE_VAR_CLASSNAME);
        
        QName contentClassQName;
        try
        {
            contentClassQName = contentClassName == null ? null : QName.createQName(contentClassName, namespaceService);
        } catch (NamespaceException e)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unrecognised classname: " + contentClassName, e);
        }
        
        final Map<String, Object> model = new HashMap<>();
        
        final SortedSet<FacetablePropertyData> facetableProperties;
        if (contentClassQName == null)
        {
            facetableProperties = toFacetablePropertyDataSet(facetService.getFacetableProperties());
        }
        else
        {
            facetableProperties = toFacetablePropertyDataSet(facetService.getFacetableProperties(contentClassQName));
        }
        
        // The webscript allows for some further filtering of results:
        List<ResultFilter> filters = new ArrayList<>();
        
        // By property QName namespace:
        final String namespaceFilter = req.getParameter(QUERY_PARAM_NAMESPACE);
        if (namespaceFilter != null)
        {
            filters.add(new ResultFilter()
            {
                @Override public boolean filter(FacetablePropertyData facetableProperty)
                {
                    final QName propQName = facetableProperty.getPropertyDefinition().getName();
                    Collection<String> prefixes = namespaceService.getPrefixes(propQName.getNamespaceURI());
                    return prefixes.contains(namespaceFilter);
                }
            });
        }
        
        List<FacetablePropertyData> filteredFacetableProperties = filter(facetableProperties, filters);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved " + facetableProperties.size() + " available facets; filtered to " + filteredFacetableProperties.size());
        }
        
        // Create paging
        ScriptPagingDetails paging = new ScriptPagingDetails(
                                             getNonNegativeIntParameter(req, QUERY_PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS_PER_PAGE),
                                             getNonNegativeIntParameter(req, QUERY_PARAM_SKIP_COUNT, 0));
        
        model.put(PROPERTIES_KEY, ModelUtil.page(filteredFacetableProperties, paging));
        model.put("paging", ModelUtil.buildPaging(paging));
        
        return model;
    }
    
    /** This type defines the (inclusion) filtering of {@link FacetablePropertyData} in the response to this webscript. */
    private static interface ResultFilter
    {
        public boolean filter(FacetablePropertyData facetableProperty);
    }
    
    /**
     * This method returns a new List instance containing only those {@link FacetablePropertyData data} that
     * satisfy all {@link ResultFilter filters}.
     */
    private List<FacetablePropertyData> filter(Collection<FacetablePropertyData> propsData, List<ResultFilter> filters)
    {
        List<FacetablePropertyData> filteredResult = new ArrayList<>();
        
        for (FacetablePropertyData prop : propsData)
        {
            boolean passedAllFilters = true;
            for (ResultFilter filter : filters)
            {
                if (!filter.filter(prop)) { passedAllFilters = false; }
            }
             if (passedAllFilters) { filteredResult.add(prop); }
        }
        
        return filteredResult;
    }
    
    /** This method returns a {@link FacetablePropertyData} for the specified {@link PropertyDefinition}. */
    private FacetablePropertyData toFacetablePropertyData(PropertyDefinition propDef)
    {
        String title = propDef.getTitle(dictionaryService);
        return new FacetablePropertyData(propDef, title);
    }
    
    private SortedSet<FacetablePropertyData> toFacetablePropertyDataSet(Collection<PropertyDefinition> propDefs)
    {
        SortedSet<FacetablePropertyData> result = new TreeSet<>();
        for (PropertyDefinition propDef : propDefs)
        {
            result.add(toFacetablePropertyData(propDef));
        }
        return result;
    }
    
    /** A simple POJO/DTO intended primarily for use in an FTL model and rendering in the JSON API. */
    public static class FacetablePropertyData implements Comparable<FacetablePropertyData>
    {
        private final PropertyDefinition propDef;
        private final String             localisedTitle;
        private final String             displayName;
        
        public FacetablePropertyData(PropertyDefinition propDef, String localisedTitle)
        {
            this.propDef        = propDef;
            this.localisedTitle = localisedTitle;
            this.displayName    = propDef.getName().getPrefixString() +
                                  (localisedTitle == null ? "" : " (" + localisedTitle + ")");
        }
        
        public PropertyDefinition getPropertyDefinition() { return this.propDef; }
        public String             getLocalisedTitle()     { return this.localisedTitle; }
        public String             getDisplayName()        { return this.displayName; }
        
        @Override public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
            result = prime * result + ((localisedTitle == null) ? 0 : localisedTitle.hashCode());
            result = prime * result + ((propDef == null) ? 0 : propDef.hashCode());
            return result;
        }
        
        @Override public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FacetablePropertyData other = (FacetablePropertyData) obj;
            if (displayName == null)
            {
                if (other.displayName != null)
                    return false;
            } else if (!displayName.equals(other.displayName))
                return false;
            if (localisedTitle == null)
            {
                if (other.localisedTitle != null)
                    return false;
            } else if (!localisedTitle.equals(other.localisedTitle))
                return false;
            if (propDef == null)
            {
                if (other.propDef != null)
                    return false;
            } else if (!propDef.equals(other.propDef))
                return false;
            return true;
        }
        
        @Override public int compareTo(FacetablePropertyData that)
        {
            final int modelComparison = this.propDef.getModel().getName().compareTo(that.propDef.getModel().getName());
            final int classComparison = this.propDef.getContainerClass().getName().compareTo(that.propDef.getContainerClass().getName());
            final int propComparison  = this.propDef.getName().compareTo(that.propDef.getName());
            
            final int result;
            if      (modelComparison != 0) { result = modelComparison; }
            else if (classComparison != 0) { result = classComparison; }
            else                           { result = propComparison; }
            
            return result;
        }
    }
}
