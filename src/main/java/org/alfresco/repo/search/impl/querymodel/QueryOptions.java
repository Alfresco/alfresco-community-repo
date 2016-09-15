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
package org.alfresco.repo.search.impl.querymodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryConsistency;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * The options for a query
 * 
 * @author andyh
 */
public class QueryOptions
{
    public enum Connective
    {
        AND, OR;
    }

    private String query;

    private List<StoreRef> stores = new ArrayList<StoreRef>(1);

    private int maxItems = -1;

    private int skipCount = 0;

    private Connective defaultFTSConnective = Connective.AND;

    private Connective defaultFTSFieldConnective = Connective.AND;

    private List<Locale> locales = new ArrayList<Locale>(1);

    private MLAnalysisMode mlAnalaysisMode = MLAnalysisMode.EXACT_LANGUAGE_AND_ALL;

    private List<QueryParameterDefinition> queryParameterDefinitions = new ArrayList<QueryParameterDefinition>(4);

    private boolean includeInTransactionData = true;

    // By default uses the central config
    private int maxPermissionChecks = -1;

    // By default uses the central config
    private long maxPermissionCheckTimeMillis = -1;

    private String defaultFieldName = "TEXT";

    private Boolean useInMemorySort;

    private Integer maxRawResultSetSizeForInMemorySort;
    
    private boolean excludeTenantFilter = false;
    
    private boolean isBulkFetchEnabled = true;

    private QueryConsistency queryConsistency = QueryConsistency.DEFAULT;
    
    private Long sinceTxId;

    public static QueryOptions create(SearchParameters searchParameters)
    {
        QueryOptions options = new QueryOptions(searchParameters.getQuery(), null);
        options.setIncludeInTransactionData(!searchParameters.excludeDataInTheCurrentTransaction());
        options.setDefaultFTSConnective(searchParameters.getDefaultOperator() == SearchParameters.Operator.OR ? Connective.OR : Connective.AND);
        options.setDefaultFTSFieldConnective(searchParameters.getDefaultOperator() == SearchParameters.Operator.OR ? Connective.OR : Connective.AND);
        options.setSkipCount(searchParameters.getSkipCount());
        options.setMaxPermissionChecks(searchParameters.getMaxPermissionChecks());
        options.setMaxPermissionCheckTimeMillis(searchParameters.getMaxPermissionCheckTimeMillis());
        options.setDefaultFieldName(searchParameters.getDefaultFieldName());
        if (searchParameters.getLimitBy() == LimitBy.FINAL_SIZE)
        {
            options.setMaxItems(searchParameters.getLimit());
        }
        else
        {
            options.setMaxItems(searchParameters.getMaxItems());
        }
        options.setMlAnalaysisMode(searchParameters.getMlAnalaysisMode());
        options.setLocales(searchParameters.getLocales());
        options.setStores(searchParameters.getStores());
        options.setQueryParameterDefinitions(searchParameters.getQueryParameterDefinitions());
        ///options.setQuery(query); Done on construction.
        options.setUseInMemorySort(searchParameters.getUseInMemorySort());
        options.setMaxRawResultSetSizeForInMemorySort(searchParameters.getMaxRawResultSetSizeForInMemorySort());
        options.setBulkFetchEnabled(searchParameters.isBulkFetchEnabled());
        options.setExcludeTenantFilter(searchParameters.getExcludeTenantFilter());
        options.setQueryConsistency(searchParameters.getQueryConsistency());
        options.setSinceTxId(searchParameters.getSinceTxId());
        return options;
    }
    /**
     * Create a CMISQueryOptions instance with the default options other than the query and store ref. The query will be
     * run using the locale returned by I18NUtil.getLocale()
     * 
     * @param query -
     *            the query to run
     * @param storeRef -
     *            the store against which to run the query
     */
    public QueryOptions(String query, StoreRef storeRef)
    {
        this(query, storeRef, I18NUtil.getLocale());
    }

    /**
     * Create a CMISQueryOptions instance with the default options other than the query, store ref and locale.
     * 
     * @param query -
     *            the query to run
     * @param storeRef -
     *            the store against which to run the query
     */
    public QueryOptions(String query, StoreRef storeRef, Locale locale)
    {
        this.query = query;
        this.stores.add(storeRef);
        this.locales.add(locale);
    }

    /**
     * Get the query string
     * 
     * @return the query
     */
    public String getQuery()
    {
        return query;
    }

    /**
     * Set the query string
     * 
     * @param query
     *            the query to set
     */
    public void setQuery(String query)
    {
        this.query = query;
    }

    /**
     * Get the list of stores in which to run the query. Only one store is supported at the momentOnly one store is
     * supported at the moment
     * 
     * @return the stores
     */
    public List<StoreRef> getStores()
    {
        return stores;
    }

    /**
     * Set the stores against which to run the query. Only one store is supported at the moment.
     * 
     * @param stores
     *            the stores to set
     */
    public void setStores(List<StoreRef> stores)
    {
        this.stores = stores;
    }

    /**
     * Get the max number of rows for the result set 0 or less is unlimited
     * 
     * @return the maxItems
     */
    public int getMaxItems()
    {
        return maxItems;
    }

    /**
     * Set the max number of rows for the result set 0 or less is unlimited
     * 
     * @param maxItems
     *            the maxItems to set
     */
    public void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems;
    }

    /**
     * Get the skip count - the number of rows to skip at the start of the query.
     * 
     * @return the skipCount
     */
    public int getSkipCount()
    {
        return skipCount;
    }

    /**
     * Set the skip count - the number of rows to skip at the start of the query.
     * 
     * @param skipCount
     *            the skipCount to set
     */
    public void setSkipCount(int skipCount)
    {
        this.skipCount = skipCount;
    }

    /**
     * Get the default connective used when OR and AND are not specified for the FTS contains() function.
     * 
     * @return the defaultFTSConnective
     */
    public Connective getDefaultFTSConnective()
    {
        return defaultFTSConnective;
    }

    /**
     * Set the default connective used when OR and AND are not specified for the FTS contains() function.
     * 
     * @param defaultFTSConnective
     *            the defaultFTSConnective to set
     */
    public void setDefaultFTSConnective(Connective defaultFTSConnective)
    {
        this.defaultFTSConnective = defaultFTSConnective;
    }

    /**
     * As getDefaultFTSConnective() but for field groups
     * 
     * @return the defaultFTSFieldConnective
     */
    public Connective getDefaultFTSFieldConnective()
    {
        return defaultFTSFieldConnective;
    }

    /**
     * As setDefaultFTSConnective() but for field groups
     * 
     * @param defaultFTSFieldConnective
     *            the defaultFTSFieldConnective to set
     */
    public void setDefaultFTSFieldConnective(Connective defaultFTSFieldConnective)
    {
        this.defaultFTSFieldConnective = defaultFTSFieldConnective;
    }

    /**
     * Get the list of locales to use for the query
     * 
     * @return the locales
     */
    public List<Locale> getLocales()
    {
        return locales;
    }

    /**
     * sSet the list of locales to use for the query
     * 
     * @param locales
     *            the locales to set
     */
    public void setLocales(List<Locale> locales)
    {
        this.locales = locales;
    }

    /**
     * Get the mode for multi-lingual text analaysis
     * 
     * @return the mlAnalaysisMode
     */
    public MLAnalysisMode getMlAnalaysisMode()
    {
        return mlAnalaysisMode;
    }

    /**
     * Set the mode for multi-lingual text analaysis
     * 
     * @param mlAnalaysisMode
     *            the mlAnalaysisMode to set
     */
    public void setMlAnalaysisMode(MLAnalysisMode mlAnalaysisMode)
    {
        this.mlAnalaysisMode = mlAnalaysisMode;
    }

    /**
     * Get the query parameters
     * 
     * @return the queryParameterDefinitions
     */
    public List<QueryParameterDefinition> getQueryParameterDefinitions()
    {
        return queryParameterDefinitions;
    }

    /**
     * Set the query parameters
     * 
     * @param queryParameterDefinitions
     *            the queryParameterDefinitions to set
     */
    public void setQueryParameterDefinitions(List<QueryParameterDefinition> queryParameterDefinitions)
    {
        this.queryParameterDefinitions = queryParameterDefinitions;
    }

    /**
     * Does the search include any changes made in the current transaction?
     * 
     * @return the includeInTransactionData
     */
    public boolean isIncludeInTransactionData()
    {
        return includeInTransactionData;
    }

    /**
     * Set to true if the search include any changes made in the current transaction.
     * 
     * @param includeInTransactionData
     *            the includeInTransactionData to set
     */
    public void setIncludeInTransactionData(boolean includeInTransactionData)
    {
        this.includeInTransactionData = includeInTransactionData;
    }

    /**
     * @return the timeout in millis for permission checks
     */
    public long getMaxPermissionCheckTimeMillis()
    {
        return maxPermissionCheckTimeMillis;
    }

    /**
     * @param maxPermissionCheckTimeMillis -
     *            the timeout in millis for permission checks
     */
    public void setMaxPermissionCheckTimeMillis(long maxPermissionCheckTimeMillis)
    {
        this.maxPermissionCheckTimeMillis = maxPermissionCheckTimeMillis;
    }

    /**
     * @return the max number of permission checks to carry out
     */
    public int getMaxPermissionChecks()
    {
        return maxPermissionChecks;
    }

    /**
     * @param maxPermissionChecks -
     *            the max number of permission checks to carry out
     */
    public void setMaxPermissionChecks(int maxPermissionChecks)
    {
        this.maxPermissionChecks = maxPermissionChecks;
    }

    /**
     * @return the default field name
     */
    public String getDefaultFieldName()
    {
        return defaultFieldName;
    }
    
    /**
     * @param defaultFieldName - the default field name to use
     */
    public void setDefaultFieldName(String defaultFieldName)
    {
       this.defaultFieldName = defaultFieldName;
    }
    
    /**
     * @return the useInMemorySort
     */
    public Boolean getUseInMemorySort()
    {
        return useInMemorySort;
    }

    /**
     * @param useInMemorySort the useInMemorySort to set
     */
    public void setUseInMemorySort(Boolean useInMemorySort)
    {
        this.useInMemorySort = useInMemorySort;
    }

    /**
     * @return the maxRawResultSetSizeForInMemorySort
     */
    public Integer getMaxRawResultSetSizeForInMemorySort()
    {
        return maxRawResultSetSizeForInMemorySort;
    }

    /**
     * @param maxRawResultSetSizeForInMemorySort the maxRawResultSetSizeForInMemorySort to set
     */
    public void setMaxRawResultSetSizeForInMemorySort(Integer maxRawResultSetSizeForInMemorySort)
    {
        this.maxRawResultSetSizeForInMemorySort = maxRawResultSetSizeForInMemorySort;
    }
    
    /**
     * @return true if bulk fetch is enabled
     */
    public boolean isBulkFetchEnabled()
    {
        return isBulkFetchEnabled;
    }

    /**
     * @param isBulkFetchEnabled boolean
     */
    public void setBulkFetchEnabled(boolean isBulkFetchEnabled)
    {
        this.isBulkFetchEnabled = isBulkFetchEnabled;
    }  
    
    /**
     * @return the tenants
     */
    public boolean getExcludeTenantFilter()
    {
        return excludeTenantFilter;
    }
    
    /**
     * @param excludeTenantFilter boolean
     */
    public void setExcludeTenantFilter(boolean excludeTenantFilter)
    {
        this.excludeTenantFilter = excludeTenantFilter;
    }
  
    /**
     * @return the queryConsistency
     */
    public QueryConsistency getQueryConsistency()
    {
        return queryConsistency;
    }
    /**
     * @param queryConsistency the queryConsistency to set
     */
    public void setQueryConsistency(QueryConsistency queryConsistency)
    {
        this.queryConsistency = queryConsistency;
    }
    
    /**
     * @return the sinceTxId
     */
    public Long getSinceTxId()
    {
        return this.sinceTxId;
    }

    /**
     * @param sinceTxId the sinceTxId to set
     */
    public void setSinceTxId(Long sinceTxId)
    {
        this.sinceTxId = sinceTxId;
    }

    /**
     * @return SearchParameters
     */
    public SearchParameters getAsSearchParmeters()
    {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setDefaultFieldName(this.getDefaultFieldName());
        searchParameters.setDefaultFTSFieldConnective(this.getDefaultFTSFieldConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setDefaultFTSOperator(this.getDefaultFTSConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setDefaultOperator(this.getDefaultFTSConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        if(this.getMaxItems() > 0)
        {
            searchParameters.setLimit(this.getMaxItems());
            searchParameters.setLimitBy(LimitBy.FINAL_SIZE);
            searchParameters.setMaxItems(this.getMaxItems());
        }
        searchParameters.setMaxPermissionChecks(this.getMaxPermissionChecks());
        searchParameters.setMaxPermissionCheckTimeMillis(this.getMaxPermissionCheckTimeMillis());
        searchParameters.setMlAnalaysisMode(this.getMlAnalaysisMode());
        //searchParameters.setNamespace()   TODO: Fix
        //searchParameters.setPermissionEvaluation()
        searchParameters.setQuery(this.getQuery());
        searchParameters.setSkipCount(this.getSkipCount());
        //searchParameters.addAllAttribute()
        for(Locale locale : this.getLocales())
        {
            searchParameters.addLocale(locale);
        }
        for(QueryParameterDefinition queryParameterDefinition: this.getQueryParameterDefinitions())
        {
            searchParameters.addQueryParameterDefinition(queryParameterDefinition);
        }
        //searchParameters.addQueryTemplate(name, template)
        //searchParameters.addSort()
        for(StoreRef storeRef : this.getStores())
        {
            searchParameters.addStore(storeRef);
        }
        //searchParameters.addTextAttribute()
        searchParameters.setQueryConsistency(this.getQueryConsistency());
        searchParameters.setSinceTxId(getSinceTxId());
        return searchParameters;
    }
    
}
