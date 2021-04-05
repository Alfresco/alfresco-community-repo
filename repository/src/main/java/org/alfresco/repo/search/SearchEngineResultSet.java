/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.search;

import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.Metric;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Supertype layer interface for all resultset coming from a search engine (e.g. Elasticsearch, Solr)
 * This interface has been originally extracted from the Apache Solr ResultSet implementation,
 * that's the reason why the naming used for denoting some things (e.g. facets) is tied to the Solr world.
 */
public interface SearchEngineResultSet extends ResultSet, SearchEngineResultMetadata
{
    Map<String, List<Pair<String, Integer>>> getFieldFacets();

    Map<String, List<Pair<String, Integer>>> getFacetIntervals();

    Map<String, List<Map<String, String>>> getFacetRanges();

    List<GenericFacetResponse> getPivotFacets();

    Map<String, Set<Metric>> getStats();

    long getLastIndexedTxId();

    boolean getProcessedDenies();
}
