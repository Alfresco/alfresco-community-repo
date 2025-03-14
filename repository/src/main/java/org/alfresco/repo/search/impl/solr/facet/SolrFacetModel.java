/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.search.impl.solr.facet;

import org.alfresco.service.namespace.QName;

/**
 * Solr Facet Model Constants
 * 
 * @author Jamal Kaabi-Mofrad
 */
public interface SolrFacetModel
{
    public static final String SOLR_FACET_MODEL_URL = "http://www.alfresco.org/model/solrfacet/1.0";
    public static final String PREFIX = "srft";

    public static final String SOLR_FACET_CUSTOM_PROPERTY_URL = "http://www.alfresco.org/model/solrfacetcustomproperty/1.0";
    public static final String SOLR_FACET_CUSTOM_PROPERTY_PREFIX = "srftcustom";

    public static final QName TYPE_FACET_FIELD = QName.createQName(SOLR_FACET_MODEL_URL, "facetField");

    public static final QName ASPECT_CUSTOM_PROPERTIES = QName.createQName(SOLR_FACET_MODEL_URL, "customProperties");

    public static final QName PROP_FIELD_TYPE = QName.createQName(SOLR_FACET_MODEL_URL, "fieldType");

    public static final QName PROP_FIELD_LABEL = QName.createQName(SOLR_FACET_MODEL_URL, "fieldLabel");

    public static final QName PROP_DISPLAY_CONTROL = QName.createQName(SOLR_FACET_MODEL_URL, "displayControl");

    public static final QName PROP_MAX_FILTERS = QName.createQName(SOLR_FACET_MODEL_URL, "maxFilters");

    public static final QName PROP_HIT_THRESHOLD = QName.createQName(SOLR_FACET_MODEL_URL, "hitThreshold");

    public static final QName PROP_MIN_FILTER_VALUE_LENGTH = QName.createQName(SOLR_FACET_MODEL_URL, "minFilterValueLength");

    public static final QName PROP_SORT_BY = QName.createQName(SOLR_FACET_MODEL_URL, "sortBy");

    public static final QName PROP_SCOPE = QName.createQName(SOLR_FACET_MODEL_URL, "scope");

    public static final QName PROP_SCOPED_SITES = QName.createQName(SOLR_FACET_MODEL_URL, "scopedSites");

    public static final QName PROP_IS_ENABLED = QName.createQName(SOLR_FACET_MODEL_URL, "isEnabled");

    public static final QName PROP_IS_DEFAULT = QName.createQName(SOLR_FACET_MODEL_URL, "isDefault");

    public static final QName PROP_EXTRA_INFORMATION = QName.createQName(SOLR_FACET_CUSTOM_PROPERTY_URL, "extraInformation");

    /** The type of the facet container folder. */
    public static final QName TYPE_FACETS = QName.createQName(SOLR_FACET_MODEL_URL, "facets");

    public static final QName PROP_FACET_ORDER = QName.createQName(SOLR_FACET_MODEL_URL, "facetOrder");
}
