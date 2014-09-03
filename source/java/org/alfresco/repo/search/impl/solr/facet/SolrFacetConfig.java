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

package org.alfresco.repo.search.impl.solr.facet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.search.impl.solr.facet.SolrFacetProperties.CustomProperties;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * This class picks up all the loaded properties passed to it and uses a naming
 * convention to isolate the default and custom facets and related values.
 * <p/>
 * So, if a new facet <b>filter_abc</b> is required for the
 * <b>cm:content.mimetype</b>, then the following needs to be put into a
 * properties file. The search for the additional custom properties file is <b>
 * tomcat/shared/classes/alfresco/extension/solr-facets-config-custom.properties</b>:
 * <ul>
 * <li>custom.cm\:content.mimetype.filterID=filter_abc</li>
 * <li>custom.cm\:content.mimetype.displayName=faceted-search.facet-menu.facet.formats</li>
 * <li>custom.cm\:content.mimetype.displayControl=alfresco/search/FacetFilters</li>
 * <li>custom.cm\:content.mimetype.maxFilters=5</li>
 * <li>custom.cm\:content.mimetype.hitThreshold=1</li>
 * <li>custom.cm\:content.mimetype.minFilterValueLength=4</li>
 * <li>custom.cm\:content.mimetype.sortBy=DESCENDING</li>
 * <li>custom.cm\:content.mimetype.scope=SCOPED_SITES</li>
 * <li>custom.cm\:content.mimetype.scopedSites=site1,site2,site3</li>
 * <li>custom.cm\:content.mimetype.isEnabled=true</li>
 * </ul>
 * Also, if there is a need to add additional properties, the following needs to be
 * put into a properties file:
 * <ul>
 * <li>custom.cm\:content.mimetype<b>.EXTRA-PROP.</b>blockIncludeFacetRequest=true</li>
 * <li>custom.cm\:content.mimetype<b>.EXTRA-PROP.</b>moreProp=additionalInfo</li>
 * </ul>
 * The inheritance order is strictly defined using property:<br/>
 * <b>${solr_facets.inheritanceHierarchy}</b><br/>
 * The default inheritance orders are:<br/>
 * <b>solr_facets.inheritanceHierarchy=default,custom</b> i.e. the default
 * facet's values are retrieved first and then overlayed with values from the
 * custom facet.
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class SolrFacetConfig extends AbstractLifecycleBean
{
    private static final Log logger = LogFactory.getLog(SolrFacetConfig.class);

    private static final String KEY_EXTRA_INFO = ".EXTRA-PROP.";
    private static final int KEY_EXTRA_INFO_LENGTH = KEY_EXTRA_INFO.length();
    
    private final Properties rawProperties;
    private final Set<String> propInheritanceOrder;
 
    private Map<String, SolrFacetProperties> defaultFacets;
    private NamespaceService namespaceService;

    public SolrFacetConfig(Properties rawProperties, String inheritanceOrder)
    {
        ParameterCheck.mandatory("rawProperties", rawProperties);
        ParameterCheck.mandatory("inheritanceOrder", inheritanceOrder);
        
        this.rawProperties = rawProperties;
        
        String[] order = inheritanceOrder.split(",");
        this.propInheritanceOrder = new LinkedHashSet<>(order.length);
        for (String ord : order)
        {
            if (ord.length() > 0)
            {
                this.propInheritanceOrder.add(ord);
            }
        }
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public synchronized Map<String, SolrFacetProperties> getDefaultFacets()
    {
        if (defaultFacets == null)
        {
            throw new IllegalStateException("SolrFacetConfig has not been started.");
        }
        return defaultFacets;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        
        defaultFacets = ConfigHelper.getFacetPropertiesMap(rawProperties, propInheritanceOrder, namespaceService);
        if(logger.isDebugEnabled())
        {
            logger.debug("All bootstrapped facets: " + defaultFacets);
        }
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // nothing to do
    }

    public static class ConfigHelper
    {
        /**
         * Convert Java properties into instances of {@link SolrFacetProperties}
         * .
         * 
         * @param properties Java properties with values defining defaults or custom values
         * @param inheritanceOrder the overriding order. E.g {default, custom} means, the custom will override the default values
         * @param namespaceService the Namespace service 
         * @return Map of {@code SolrFacetProperties} with the
         *         {@code SolrFacetProperties.filterID} as the key or an empty map if none exists
         */
        public static Map<String, SolrFacetProperties> getFacetPropertiesMap(Properties properties, Set<String> inheritanceOrder, NamespaceService namespaceService)
        {
            Set<String> allPropNames = new HashSet<String>(100);
            Set<String> propOrderControl = new HashSet<String>(2);
            getPropertyAndPropOderControl(properties, allPropNames, propOrderControl);
            
            // Get property inheritance order
            Set<String> inheritance = getInheritanceOrder(inheritanceOrder, propOrderControl);

            Map<String, String> propValues = new HashMap<>(allPropNames.size());
            for (String order : inheritance)
            {
                for (String propName : allPropNames)
                {
                    String value = properties.getProperty(order + '.' + propName);
                    if (value != null)
                    {
                        // Override the values based on the inheritance order
                        propValues.put(propName, value);
                    }
                }
            }

            Map<String, Set<String>> facetFields = new HashMap<>();
            for(String key : propValues.keySet())
            {
                String facetQName = null;
                Set<String> extraProp = null;
                int index = key.indexOf(KEY_EXTRA_INFO);
                if (index > 0)
                {
                    String extraInfo = key.substring(index + KEY_EXTRA_INFO_LENGTH);
                    facetQName = key.substring(0, index);

                    extraProp = facetFields.get(facetQName);
                    if (extraProp == null)
                    {
                        extraProp = new HashSet<>();
                    }
                    if (extraInfo.length() > 0)
                    {
                        extraProp.add(extraInfo);
                    }
                }
                else
                {
                    index = key.lastIndexOf('.');
                    facetQName = key.substring(0, index);
                    extraProp = facetFields.get(facetQName);
                }
                facetFields.put(facetQName, extraProp);
            }

            // Build the facet config objects
            Map<String, SolrFacetProperties> facetProperties = new HashMap<>(100);
            for (String field : facetFields.keySet())
            {
                // FacetProperty attributes
                // Resolve facet field into QName
                QName fieldQName = resolveToQName(namespaceService, field);
                String filterID = propValues.get(ValueName.PROP_FILTER_ID.getPropValueName(field));
                String displayName = propValues.get(ValueName.PROP_DISPLAY_NAME.getPropValueName(field));
                String displayControl = propValues.get(ValueName.PROP_DISPLAY_CONTROL.getPropValueName(field));
                int maxFilters = getIntegerValue(propValues.get(ValueName.PROP_MAX_FILTERS.getPropValueName(field)));
                int hitThreshold = getIntegerValue(propValues.get(ValueName.PROP_HIT_THRESHOLD.getPropValueName(field)));
                int minFilterValueLength = getIntegerValue(propValues.get(ValueName.PROP_MIN_FILTER_VALUE_LENGTH.getPropValueName(field)));
                String sortBy = propValues.get(ValueName.PROP_SORTBY.getPropValueName(field));
                String scope = propValues.get(ValueName.PROP_SCOPE.getPropValueName(field));
                Set<String> scopedSites = getScopedSites(propValues.get(ValueName.PROP_SCOPED_SITES.getPropValueName(field)));
                Boolean isEnabled = Boolean.valueOf(propValues.get(ValueName.PROP_IS_ENABLED.getPropValueName(field)));
                Set<CustomProperties> customProps = getCustomProps(facetFields.get(field), field, propValues);

                // Construct the FacetProperty object
                SolrFacetProperties fp = new SolrFacetProperties.Builder()
                            .filterID(filterID)
                            .facetQName(fieldQName)
                            .displayName(displayName)
                            .displayControl(displayControl)
                            .maxFilters(maxFilters)
                            .hitThreshold(hitThreshold)
                            .minFilterValueLength(minFilterValueLength)
                            .sortBy(sortBy)
                            .scope(scope)
                            .isEnabled(isEnabled)
                            .isDefault(true)
                            .scopedSites(scopedSites)
                            .customProperties(customProps).build();

                facetProperties.put(filterID, fp);
            }

            // Done
            return facetProperties;
        }

        private static Set<String> getInheritanceOrder(Set<String> definedOrder, Set<String> foundOrder)
        {
            // 1 => it means we have only default properties.
            if (foundOrder.size() == 1)
            {
                return foundOrder;
            }

            boolean result = definedOrder.addAll(foundOrder);
            if (result)
            {
                logger.warn("The property inheritance order has not been explicitly defined.  Orders are: " + definedOrder);
            }
            return definedOrder;
        }

        private static void getPropertyAndPropOderControl(Properties properties, Set<String> propNames, Set<String> propOrderControl)
        {

            for (Object propKeyObj : properties.keySet())
            {
                String propKey = (String) propKeyObj;

                // Find end of property control (default/custom) flag
                int propOrderControlEndDot = propKey.indexOf('.');
                if (propOrderControlEndDot < 1)
                {
                    logger.debug("Ignoring property: " + propKey);
                    continue;
                }
                int propKeyLength = propKey.length();

                int propNameLength = (propKeyLength - propOrderControlEndDot) -1; // Length of characters between dots
                if (propNameLength  < 1)
                {
                    logger.debug("Ignoring property: " + propKey);
                    continue;
                }
                String orderControl = propKey.substring(0, propOrderControlEndDot);
                String propName = propKey.substring((propOrderControlEndDot + 1));

                // Add them
                propOrderControl.add(orderControl);
                propNames.add(propName);
            }
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("All property order controls: " + propOrderControl);
                logger.debug("All properties: " + propNames);
            }
        }

        private static Set<String> getScopedSites(String propValue)
        {
            if (propValue == null)
            {
                return Collections.emptySet();
            }

            String[] sites = propValue.split(",");
            Set<String> set = new LinkedHashSet<>(sites.length);

            for (String site : sites)
            {
                if (site.length() > 0)
                {
                    set.add(site);
                }
            }
            return set;
        }

        private static Set<CustomProperties> getCustomProps(Set<String> additionalProps, String field, Map<String, String> propValues)
        {
            if (additionalProps == null)
            {
                return null;
            }

            Set<CustomProperties> customProps = new HashSet<>();
            for (String extraInfo : additionalProps)
            {
                String value = propValues.get(field + KEY_EXTRA_INFO + extraInfo);
                if (value != null)
                {
                    QName qName = QName.createQName(SolrFacetModel.SOLR_FACET_CUSTOM_PROPERTY_URL, extraInfo);
                    String[] extra = value.split(",");
                    if (extra.length == 1)
                    {
                        customProps.add(new CustomProperties(qName, extra[0]));
                    }
                    else
                    {
                        List<String> list = Arrays.asList(extra);
                        customProps.add(new CustomProperties(qName, (Serializable) list));
                    }
                }
            }
            return customProps;
        }

        private static int getIntegerValue(String propValue)
        {
            try
            {
                return Integer.parseInt(propValue);
            }
            catch (NumberFormatException ne)
            {
                throw new AlfrescoRuntimeException("Invalid property value. Cannot convert [" + propValue + " ] into an Integer.", ne);
            }
        }

        private static QName resolveToQName(NamespaceService namespaceService, String qnameStr)
        {
            QName typeQName = QName.resolveToQName(namespaceService, qnameStr);
            if (logger.isDebugEnabled())
            {
                logger.debug("Resolved facet field [" + qnameStr + "] into [" + typeQName + "]");
            }
            return typeQName;
        }

    }

    public static enum ValueName
    {
        PROP_FILTER_ID("filterID"), PROP_DISPLAY_NAME("displayName"), PROP_MAX_FILTERS("maxFilters"), PROP_HIT_THRESHOLD("hitThreshold"),
        PROP_MIN_FILTER_VALUE_LENGTH("minFilterValueLength"), PROP_SORTBY("sortBy"), PROP_SCOPE("scope"), PROP_SCOPED_SITES("scopedSites"),
        PROP_IS_ENABLED("isEnabled"), PROP_DISPLAY_CONTROL("displayControl");

        private ValueName(String propValueName)
        {
            this.propValueName = propValueName;
        }

        private String propValueName;

        public String getPropValueName(String facetField)
        {
            return facetField + '.' + this.propValueName;
        }
    }
}
