/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.cmis;

import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * The options for a CMIS query
 * 
 * @author andyh
 */
public class CMISQueryOptions extends QueryOptions
{
    public enum CMISQueryMode
    {
        CMS_STRICT, CMS_WITH_ALFRESCO_EXTENSIONS ;
    }
    
   
    private CMISQueryMode queryMode = CMISQueryMode.CMS_STRICT;

    
    public static CMISQueryOptions create(SearchParameters searchParameters)
    {
        String sql = searchParameters.getQuery();

        CMISQueryOptions options = new CMISQueryOptions(sql, searchParameters.getStores().get(0));
        options.setIncludeInTransactionData(!searchParameters.excludeDataInTheCurrentTransaction());
        options.setDefaultFTSConnective(searchParameters.getDefaultOperator() == SearchParameters.Operator.OR ? Connective.OR : Connective.AND);
        options.setDefaultFTSFieldConnective(searchParameters.getDefaultOperator() == SearchParameters.Operator.OR ? Connective.OR : Connective.AND);
        options.setSkipCount(searchParameters.getSkipCount());
        options.setMaxPermissionChecks(searchParameters.getMaxPermissionChecks());
        options.setMaxPermissionCheckTimeMillis(searchParameters.getMaxPermissionCheckTimeMillis());
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
        //options.setQuery(); Done on conbstruction
        //options.setQueryMode(); Should set afterwards
        options.setQueryParameterDefinitions(searchParameters.getQueryParameterDefinitions());
        options.setDefaultFieldName(searchParameters.getDefaultFieldName());
        return options;
    }
    /**
     * Create a CMISQueryOptions instance with the default options other than the query and store ref.
     * The query will be run using the locale returned by I18NUtil.getLocale()
     * 
     * @param query - the query to run
     * @param storeRef - the store against which to run the query
     */
    public CMISQueryOptions(String query, StoreRef storeRef)
    {
        this(query, storeRef, I18NUtil.getLocale());
    }
    
    /**
     * Create a CMISQueryOptions instance with the default options other than the query, store ref and locale.
     * 
     * @param query - the query to run
     * @param storeRef - the store against which to run the query
     */
    public CMISQueryOptions(String query, StoreRef storeRef, Locale locale)
    {
        super(query, storeRef, locale);
    }

    

    
    /**
     * Get the query mode.
     * 
     * @return the queryMode
     */
    public CMISQueryMode getQueryMode()
    {
        return queryMode;
    }

    /**
     * Set the query mode.
     * 
     * @param queryMode the queryMode to set
     */
    public void setQueryMode(CMISQueryMode queryMode)
    {
        this.queryMode = queryMode;
    }
    /**
     * @return
     */
    public SearchParameters getAsSearchParmeters()
    {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setDefaultFieldName(this.getDefaultFieldName());
        searchParameters.setDefaultFTSFieldConnective(this.getDefaultFTSFieldConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setDefaultFTSOperator(this.getDefaultFTSConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setDefaultOperator(this.getDefaultFTSConnective() == Connective.OR ? SearchParameters.Operator.OR : SearchParameters.Operator.AND);
        searchParameters.setLanguage(SearchService.LANGUAGE_CMIS_ALFRESCO);
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
        return searchParameters;
    }
}


