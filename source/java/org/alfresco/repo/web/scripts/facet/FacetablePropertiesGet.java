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
        
        final SortedSet<FacetablePropertyFTLModel> facetableProperties;
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
        
        // The webscript allows for some further filtering of results:
        List<ResultFilter> filters = new ArrayList<>();
        
        // Filter by property QName namespace:
        final String namespaceFilter = req.getParameter(QUERY_PARAM_NAMESPACE);
        if (namespaceFilter != null)
        {
            filters.add(new ResultFilter()
            {
                @Override public boolean filter(FacetablePropertyFTLModel facetableProperty)
                {
                    final QName propQName = facetableProperty.getQname();
                    Collection<String> prefixes = namespaceService.getPrefixes(propQName.getNamespaceURI());
                    return prefixes.contains(namespaceFilter);
                }
            });
        }
        
        List<FacetablePropertyFTLModel> filteredFacetableProperties = filter(facetableProperties, filters);
        
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
     * This type defines the (inclusion) filtering of {@link FacetablePropertyFTLModel property data}
     * in the response to this webscript.
     */
    private static interface ResultFilter
    {
        /** @return {@code true} if the specified property should be included. */
        public boolean filter(FacetablePropertyFTLModel facetableProperty);
    }
    
    /**
     * This method returns a new List instance containing only those {@link FacetablePropertyFTLModel property data}
     * that satisfy all {@link ResultFilter filters}.
     */
    private List<FacetablePropertyFTLModel> filter(Collection<FacetablePropertyFTLModel> propsData, List<ResultFilter> filters)
    {
        final List<FacetablePropertyFTLModel> filteredResult = new ArrayList<>();
        
        for (FacetablePropertyFTLModel prop : propsData)
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
    
    /** This method returns a {@link FacetablePropertyFTLModel} for the specified {@link PropertyDefinition}. */
    private FacetablePropertyFTLModel toFacetablePropertyModel(PropertyDefinition propDef, Locale locale)
    {
        String title = propDef.getTitle(messageLookup, locale);
        return new FacetablePropertyFTLModel(propDef, title);
    }
    
    /** This method returns a {@link FacetablePropertyFTLModel} for the specified {@link SyntheticPropertyDefinition}. */
    private FacetablePropertyFTLModel toFacetablePropertyModel(SyntheticPropertyDefinition propDef,
                                                               Locale locale)
    {
        // Note the hard-coded assumption here that all synthetic properties are defined only
        // within the cm:content property type. This code is not designed to be extended.
        // TODO We may need to make this code extensible in a future release.
        //
        // See e.g. content-model.properties for usage of this i18n key.
        final String i18nKeyPrefix = "cm_contentmodel.property.cm_content.cm_content.";
        final String localisedTitle = I18NUtil.getMessage(i18nKeyPrefix + propDef.syntheticPropertyName, locale);
        
        return new SyntheticFacetablePropertyFTLModel(propDef.containingPropertyDef,
                                                      localisedTitle,
                                                      propDef.syntheticPropertyName,
                                                      propDef.dataTypeDefinition);
    }
    
    private SortedSet<FacetablePropertyFTLModel> toFacetablePropertyModel(Collection<PropertyDefinition> propDefs,
                                                                          Locale locale)
    {
        SortedSet<FacetablePropertyFTLModel> result = new TreeSet<>();
        for (PropertyDefinition propDef : propDefs)
        {
            result.add(toFacetablePropertyModel(propDef, locale));
        }
        return result;
    }
    
    // Note: the trailing underscore in this method name is to prevent a clash between this method and the
    // one that takes a Collection<PropertyDefinition> as type erasure means that both methods would have the
    // same signature without the trailing underscore.
    private SortedSet<FacetablePropertyFTLModel> toFacetablePropertyModel_(Collection<SyntheticPropertyDefinition> propDefs,
                                                                           Locale locale)
    {
        SortedSet<FacetablePropertyFTLModel> result = new TreeSet<>();
        for (SyntheticPropertyDefinition propDef : propDefs)
        {
            result.add(toFacetablePropertyModel(propDef, locale));
        }
        return result;
    }
    
    /** A simple POJO/DTO intended primarily for use in an FTL model and rendering in the JSON API. */
    public static class FacetablePropertyFTLModel implements Comparable<FacetablePropertyFTLModel>
    {
        /** The Alfresco property definition which declares this facetable property. */
        protected final PropertyDefinition propDef;
        
        /** The localised title for this property. */
        protected final String localisedTitle;
        
        /** A display name for this property. */
        protected String displayName;
        
        /**
         * @param propDef The {@link PropertyDefinition}.
         * @param localisedTitle The localised title for this property e.g. "Titre".
         */
        public FacetablePropertyFTLModel(PropertyDefinition propDef, String localisedTitle)
        {
            this.propDef        = propDef;
            this.localisedTitle = localisedTitle;
            this.displayName    = getShortQname() + (localisedTitle == null ? "" : " (" + localisedTitle + ")");
        }
        
        // We use "*Qname*" (small 'n') in these accessors to make the FTL less ambiguous.
        public String getShortQname()         { return propDef.getName().getPrefixString(); }
        
        public QName  getQname()              { return propDef.getName(); }
        
        public String getTitle()              { return localisedTitle; }
        
        public String getDisplayName()        { return displayName; }
        
        public QName  getContainerClassType() { return propDef.getContainerClass().getName(); }
        
        public QName  getDataType()           { return propDef.getDataType().getName(); }
        
        public QName  getModelQname()         { return propDef.getModel().getName(); }
        
        @Override public boolean equals(Object obj)
        {
            if (this == obj)                  { return true; }
            if (obj == null)                  { return false; }
            if (getClass() != obj.getClass()) { return false; }
            
            FacetablePropertyFTLModel other = (FacetablePropertyFTLModel) obj;
            if (displayName == null)
            {
                if (other.displayName != null) { return false; }
            } else if (!displayName.equals(other.displayName)) { return false; }
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
        
        @Override public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
            result = prime * result + ((localisedTitle == null) ? 0 : localisedTitle.hashCode());
            result = prime * result + ((propDef == null) ? 0 : propDef.hashCode());
            return result;
        }
        
        @Override public int compareTo(FacetablePropertyFTLModel that)
        {
            final int modelComparison = this.propDef.getModel().getName().compareTo(that.propDef.getModel().getName());
            final int classComparison = this.propDef.getContainerClass().getName().compareTo(that.propDef.getContainerClass().getName());
            final int propComparison  = this.propDef.getName().compareTo(that.propDef.getName());
            // this comparison matters because it incorporates SyntheticProperties like size & mimetype. See below.
            final int propWithSynthetic = this.getShortQname().compareTo(that.getShortQname());
            
            final int result;
            if      (modelComparison != 0) { result = modelComparison; }
            else if (classComparison != 0) { result = classComparison; }
            else if (propComparison != 0)  { result = propComparison; }
            else                           { result = propWithSynthetic; }
            
            return result;
        }
    }
    
    /**
     * This class represents a facetable property, which is not actually an Alfresco
     * content property. Examples are the {@code size} and {@code MIME type} fields
     * within the {@code cm:content} property type.
     */
    public static class SyntheticFacetablePropertyFTLModel extends FacetablePropertyFTLModel
    {
        /** This is the name of the synthetic property e.g. "size". Not localised. */
        private final String syntheticPropertyName;
        
        /** The type of this synthetic data property. */
        private final QName datatype;
        
        /**
         * @param containingPropDef     The {@link PropertyDefinition}.
         * @param localisedTitle        The localised title of this synthetic property e.g. "taille".
         * @param syntheticPropertyName The synthetic property name e.g. "size".
         * @param datatype              The datatype of the synthetic property.
         */
        public SyntheticFacetablePropertyFTLModel(PropertyDefinition containingPropDef,
                                                  String localisedTitle,
                                                  String syntheticPropertyName,
                                                  QName  datatype)
        {
            super(containingPropDef, localisedTitle);
            this.syntheticPropertyName = syntheticPropertyName;
            this.datatype              = datatype;
            this.displayName           = getShortQname() + (localisedTitle == null ? "" : " (" + localisedTitle + ")");
        }
        
        @Override public String getShortQname()
        {
            return super.getShortQname() + "." + this.syntheticPropertyName;
        }
        
        @Override public QName getQname()
        {
            final QName containingPropQName = super.getQname();
            return QName.createQName(containingPropQName.getNamespaceURI(),
                                     containingPropQName.getLocalName() + "." + this.syntheticPropertyName);
        }
        
        @Override public QName getDataType() { return datatype; }
    }
}
