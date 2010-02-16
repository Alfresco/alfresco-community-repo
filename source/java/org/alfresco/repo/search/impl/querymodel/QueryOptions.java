/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.querymodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.QueryParameterDefinition;

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

    private int fetchSize = 1000;

    private List<Locale> locales = new ArrayList<Locale>(1);

    private MLAnalysisMode mlAnalaysisMode = MLAnalysisMode.EXACT_LANGUAGE_AND_ALL;

    private List<QueryParameterDefinition> queryParameterDefinitions = new ArrayList<QueryParameterDefinition>(4);

    private boolean includeInTransactionData = true;

    // By default uses the central config
    private int maxPermissionChecks = -1;

    // By default uses the central config
    private long maxPermissionCheckTimeMillis = -1;

    private String defaultFieldName = "TEXT";

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
     * Get the fetch size 0 - no prefetch -1 - prefetch all
     * 
     * @return the fetchSize
     */
    public int getFetchSize()
    {
        return fetchSize;
    }

    /**
     * Set the fetch size 0 - no prefetch -1 - prefetch all
     * 
     * @param fetchSize
     *            the fetchSize to set
     */
    public void setFetchSize(int fetchSize)
    {
        this.fetchSize = fetchSize;
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
}
