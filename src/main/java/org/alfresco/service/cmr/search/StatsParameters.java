/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;

/**
 * Defines Stats search criteria
 *
 * @author Gethin James
 * @since 5.0
 */
public class StatsParameters implements BasicSearchParameters
{ 
    public static final String PARAM_FIELD = "field";
    public static final String PARAM_FACET = "facet";
    public static final String FACET_PREFIX = "@";
    
    private final String language;
    private final String query;
    private final String filterQuery;
    private final boolean dateSearch;
    private List<StoreRef> stores = new ArrayList<>();
    private List<Locale> locales = new ArrayList<>();
    private List<SortDefinition> sortDefinitions = new ArrayList<>();
    private Map<String, String> statsParameters = new HashMap<>();
    
    public StatsParameters(String language, String query)
    {
        this(language, query, null, false);
    }
    
    public StatsParameters(String language, String query, boolean isDateSearch)
    {
        this(language, query, null, isDateSearch);
    }
    
    public StatsParameters(String language, String query, String filterQuery, boolean isDateSearch)
    {
        super();
        this.language = language;
        this.query = query;
        this.filterQuery = filterQuery;
        this.dateSearch = isDateSearch;
    }
    
    public String getLanguage()
    {
        return this.language;
    }
    public String getQuery()
    {
        return this.query;
    }    
    public String getFilterQuery()
    {
        return this.filterQuery;
    }
    public List<StoreRef> getStores()
    {
        return this.stores;
    }
    public List<Locale> getLocales()
    {
        return this.locales;
    }
    public List<SortDefinition> getSortDefinitions()
    {
        return this.sortDefinitions;
    }
    public Map<String, String> getStatsParameters()
    {
        return this.statsParameters;
    }
    public boolean isDateSearch()
    {
        return this.dateSearch;
    }

    /**
     * Add a sort definition.
     * 
     * @param sortDefinition - the sort definition to add. 
     */
    public void addSort(SortDefinition sortDefinition)
    {
        sortDefinitions.add(sortDefinition);
    }
    
    /**
     * Add a parameter
     * 
     * @param name String
     * @param value String
     */
    public void addStatsParameter(String name, String value)
    {
        statsParameters.put(name, value);
    }
    
    /**
     * Add a Store ref
     * 
     * @param store StoreRef
     */
    public void addStore(StoreRef store)
    {      
        if (stores.size() != 0)
        {
            throw new IllegalStateException("At the moment, there can only be one stats store set for the search");
        }
        stores.add(store);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("StatsParameters [query=").append(this.query).append(", filterquery=")
                    .append(this.filterQuery).append(", language=")
                    .append(this.language).append(", stores=").append(this.stores)
                    .append(", locales=").append(this.locales).append(", sortDefinitions=")
                    .append(this.sortDefinitions).append(", statsParameters=")
                    .append(this.statsParameters).append(", isDateSearch=")
                    .append(this.dateSearch).append("]");
        return builder.toString();
    }

}
