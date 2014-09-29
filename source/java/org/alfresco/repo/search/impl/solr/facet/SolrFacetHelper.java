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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper class for facet queries.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SolrFacetHelper
{
    private static Log logger = LogFactory.getLog(SolrFacetHelper.class);

    private final Map<String, List<String>> facetQueries;


    /** These facet IDs are recognised by SOLR and can be used directly within faceted searches. */
    private Set<String> specialFacetIds = Collections.emptySet();

    /**
     * Constructor
     * 
     * @param serviceRegistry
     */
    public SolrFacetHelper(List<FacetQueryProvider> facetQueryProviders)
    {
        PropertyCheck.mandatory(this, "facetQueryProviders", facetQueryProviders);

        facetQueries = new LinkedHashMap<>();
        for (FacetQueryProvider queryProvider : facetQueryProviders)
        {
            for (Entry<String, List<String>> entry : queryProvider.getFacetQueries().entrySet())
            {
                facetQueries.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setSpecialFacetIds(Set<String> ids)
    {
        this.specialFacetIds = ids;
    }

    /**
     * Gets the predefined set of facet queries. Currently the facet queries
     * are: <li>Created date buckets</li> <li>Modified date buckets</li> <li>
     * Content size buckets</li>
     * 
     * @return list of facet queries
     */
    public List<String> getDefaultFacetQueries()
    {
        Collection<List<String>> queries = facetQueries.values();
        List<String> list = new ArrayList<String>();
        for (List<String> q : queries)
        {
            list.addAll(q);
        }
        return list;
    }

    /**
     * Whether the specified field is defined as a <i>facet.query</i> or not
     * 
     * @param facetField
     * @return true if the facet is <i>facet.query</i>, false otherwise
     */
    public boolean hasFacetQueries(String facetField)
    {
        return facetQueries.containsKey(facetField);
    }

    /**
     * Gets all the defined facet queries for the specified field
     * @param facetField the requested field
     * @return  an unmodifiable list of facet queries, or null if none found
     */
    public List<String> getFacetQueries(String facetField)
    {
        List<String> queries = facetQueries.get(facetField);
        if (queries == null)
        {
            return null;
        }
        return Collections.unmodifiableList(queries);
    }

    /**
     * Gets predefined set of field facets which are used to construct bucketing
     * 
     * @return an unmodifiable view of the set of predefined field facets
     */
    public Set<String> getBucketedFieldFacets()
    {
        return Collections.unmodifiableSet(facetQueries.keySet());
    }

    /**
     * Creates a facet query by trying to extract the date range from the the
     * search query.
     * 
     * @return the facet query, or null if the date range cannot be extracted
     */
    // workaround for https://issues.alfresco.com/jira/browse/ACE-1605
    public String createFacetQueriesFromSearchQuery(String field, String searchQuery)
    {
        if (field == null)
        {
            return null;
        }
        try
        {
            if (field.startsWith("@"))
            {
                field = field.substring(1);
            }
            String escapedField = searchQuery.substring(searchQuery.indexOf(field));

            String dateRange = escapedField.substring(field.length() + 2, escapedField.indexOf(")")).trim();
            // E.g. dateRange => "NOW/DAY-7DAYS".."NOW/DAY+1DAY"

            dateRange = dateRange.replace("\"..\"", " TO ");
            // remove the date-range quotations marks
            dateRange = dateRange.replace("\"", "");

            // the processed dateRange will be, for example, NOW/DAY-7DAY TO NOW/DAY+1DAY
            dateRange = (dateRange == null) ? null : field + ":[" + dateRange + "]";

            return "@" + dateRange;
        }
        catch (Exception e)
        {
            logger.warn("Couldn’t extract " + field + " date range from the search query." + e);
            return null;
        }
    }
    

    /**
     * Is the specified facet ID part of the list of "specials" which are
     * handled by our SOLR service as is?
     */
    public boolean isSpecialFacetId(String facetId)
    {
        return specialFacetIds.contains(facetId);
    }
}
