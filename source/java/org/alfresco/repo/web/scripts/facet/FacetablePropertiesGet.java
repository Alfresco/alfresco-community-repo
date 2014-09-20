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
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.search.impl.solr.facet.SolrFacetService.FacetablePropertyData;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
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
    
    private static final String TEMPLATE_VAR_CLASSNAME = "classname";
    private static final String QUERY_PARAM_NAMESPACE = "nsp";
    
    private NamespaceService namespaceService;
    
    public void setNamespaceService(NamespaceService service) { this.namespaceService = service; }
    
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
        
        final Set<FacetablePropertyData> facetableProperties;
        if (contentClassQName == null)
        {
            facetableProperties = facetService.getFacetableProperties();
        }
        else
        {
            facetableProperties = facetService.getFacetableProperties(contentClassQName);
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
        
        Set<FacetablePropertyData> filteredFacetableProperties = filter(facetableProperties, filters);
        model.put(PROPERTIES_KEY, filteredFacetableProperties);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieved " + facetableProperties.size() + " available facets; filtered to " + filteredFacetableProperties.size());
        }
        
        return model;
    }
    
    /** This type defines the (inclusion) filtering of {@link FacetablePropertyData} in the response to this webscript. */
    private static interface ResultFilter
    {
        public boolean filter(FacetablePropertyData facetableProperty);
    }
    
    /**
     * This method returns a new Set instance containing only those {@link FacetablePropertyData data} that
     * satisfy all {@link ResultFilter filters}.
     */
    private Set<FacetablePropertyData> filter(Set<FacetablePropertyData> propsData, List<ResultFilter> filters)
    {
        Set<FacetablePropertyData> filteredResult = new TreeSet<>();
        
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
}
