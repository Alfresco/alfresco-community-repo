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
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetService.SyntheticPropertyDefinition;
import org.alfresco.repo.web.scripts.facet.FacetablePropertyFTL.FacetablePropertyFTLComparator;
import org.alfresco.repo.web.scripts.facet.FacetablePropertyFTL.SpecialFacetablePropertyFTL;
import org.alfresco.repo.web.scripts.facet.FacetablePropertyFTL.StandardFacetablePropertyFTL;
import org.alfresco.repo.web.scripts.facet.FacetablePropertyFTL.SyntheticFacetablePropertyFTL;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.ScriptPagingDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
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
    private static final String QUERY_PARAM_LOCALE  = "locale";
    
    private NamespaceService  namespaceService;
    private MessageLookup     messageLookup;
    
    public FacetablePropertiesGet() { messageLookup = new StaticMessageLookup(); }
    
    public void setNamespaceService (NamespaceService service)  { this.namespaceService  = service; }
    
    @Override protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache)
    {
        // Allow all authenticated users in
        return unprotectedExecuteImpl(req, status, cache);
    }
    
    @Override protected Map<String, Object> unprotectedExecuteImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // We use any provided locale to localise some elements of the webscript response, but not all.
        final String userLocaleString = req.getParameter(QUERY_PARAM_LOCALE);
        final Locale userLocale = (userLocaleString == null) ? Locale.getDefault() : new Locale(userLocaleString);
        
        // There are multiple defined URIs for this REST endpoint. Some define a "classname" template var.
        final Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        final String contentClassName = templateVars.get(TEMPLATE_VAR_CLASSNAME);
        
        final QName contentClassQName;
        try
        {
            contentClassQName = contentClassName == null ? null : QName.createQName(contentClassName, namespaceService);
        } catch (NamespaceException e)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Unrecognised classname: " + contentClassName, e);
        }
        
        // Build an FTL model of facetable properties - this includes normal Alfresco properties and also
        // 'synthetic' properties like size and mimetype. See below for more details.
        final Map<String, Object> model = new HashMap<>();
        
        final SortedSet<FacetablePropertyFTL<?>> facetableProperties;
        if (contentClassQName == null)
        {
            facetableProperties = toFacetablePropertyModel(facetService.getFacetableProperties(), userLocale);
            
            final List<SyntheticPropertyDefinition> facetableSyntheticProperties = facetService.getFacetableSyntheticProperties();
            facetableProperties.addAll(toFacetablePropertyModel_(facetableSyntheticProperties, userLocale));
        }
        else
        {
            facetableProperties = toFacetablePropertyModel(facetService.getFacetableProperties(contentClassQName), userLocale);
            
            final List<SyntheticPropertyDefinition> facetableSyntheticProperties = facetService.getFacetableSyntheticProperties(contentClassQName);
            facetableProperties.addAll(toFacetablePropertyModel_(facetableSyntheticProperties, userLocale));
        }
        
        // Always add some hard-coded facetable "properties"
        facetableProperties.add(new SpecialFacetablePropertyFTL("TAG", "Tag"));
        facetableProperties.add(new SpecialFacetablePropertyFTL("SITE", "Site"));
        
        // The webscript allows for some further filtering of results:
        List<ResultFilter> filters = new ArrayList<>();
        
        // Filter by property QName namespace:
        final String namespaceFilter = req.getParameter(QUERY_PARAM_NAMESPACE);
        if (namespaceFilter != null)
        {
            filters.add(new ResultFilter()
            {
                @Override public boolean filter(FacetablePropertyFTL<?> facetableProperty)
                {
                    final QName propQName = facetableProperty.getQname();
                    Collection<String> prefixes = namespaceService.getPrefixes(propQName.getNamespaceURI());
                    return prefixes.contains(namespaceFilter);
                }
            });
        }
        
        List<FacetablePropertyFTL<?>> filteredFacetableProperties = filter(facetableProperties, filters);
        
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
    
    /**
     * This type defines the (inclusion) filtering of {@link FacetablePropertyFTL property data}
     * in the response to this webscript.
     */
    private static interface ResultFilter
    {
        /** @return {@code true} if the specified property should be included. */
        public boolean filter(FacetablePropertyFTL<?> facetableProperty);
    }
    
    /**
     * This method returns a new List instance containing only those {@link FacetablePropertyFTL property data}
     * that satisfy all {@link ResultFilter filters}.
     */
    private List<FacetablePropertyFTL<?>> filter(Collection<FacetablePropertyFTL<?>> propsData, List<ResultFilter> filters)
    {
        final List<FacetablePropertyFTL<?>> filteredResult = new ArrayList<>();
        
        for (FacetablePropertyFTL<?> prop : propsData)
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
    
    /** This method returns a {@link FacetablePropertyFTL} for the specified {@link PropertyDefinition}. */
    private FacetablePropertyFTL<?> toFacetablePropertyModel(PropertyDefinition propDef, Locale locale)
    {
        String title = propDef.getTitle(messageLookup, locale);
        return new StandardFacetablePropertyFTL(propDef, title);
    }
    
    /** This method returns a {@link FacetablePropertyFTL} for the specified {@link SyntheticPropertyDefinition}. */
    private FacetablePropertyFTL<?> toFacetablePropertyModel(SyntheticPropertyDefinition propDef,
                                                          Locale locale)
    {
        // Note the hard-coded assumption here that all synthetic properties are defined only
        // within the cm:content property type. This code is not designed to be extended.
        // TODO We may need to make this code extensible in a future release.
        //
        // See e.g. content-model.properties for usage of this i18n key.
        final String i18nKeyPrefix = "cm_contentmodel.property.cm_content.cm_content.";
        final String localisedTitle = I18NUtil.getMessage(i18nKeyPrefix + propDef.syntheticPropertyName, locale);
        
        return new SyntheticFacetablePropertyFTL(propDef.containingPropertyDef,
                                                 localisedTitle,
                                                 propDef.syntheticPropertyName,
                                                 propDef.dataTypeDefinition);
    }
    
    private SortedSet<FacetablePropertyFTL<?>> toFacetablePropertyModel(Collection<PropertyDefinition> propDefs,
                                                                     Locale locale)
    {
        SortedSet<FacetablePropertyFTL<?>> result = new TreeSet<>(new FacetablePropertyFTLComparator());
        for (PropertyDefinition propDef : propDefs)
        {
            result.add(toFacetablePropertyModel(propDef, locale));
        }
        return result;
    }
    
    // Note: the trailing underscore in this method name is to prevent a clash between this method and the
    // one that takes a Collection<PropertyDefinition> as Java's type erasure means that both methods would have the
    // same signature, without the trailing underscore.
    private SortedSet<FacetablePropertyFTL<?>> toFacetablePropertyModel_(Collection<SyntheticPropertyDefinition> propDefs,
                                                                      Locale locale)
    {
        SortedSet<FacetablePropertyFTL<?>> result = new TreeSet<>(new FacetablePropertyFTLComparator());
        for (SyntheticPropertyDefinition propDef : propDefs)
        {
            result.add(toFacetablePropertyModel(propDef, locale));
        }
        return result;
    }
}
