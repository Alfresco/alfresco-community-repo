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
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;

/**
 * This class provides the search parameters to construct a search query.
 * @author Michael Suzuki
 *
 */
public class SearchSQLParameters implements BasicSearchParameters
{
    private final String language;
    private final String query;
    private List<StoreRef> stores = new ArrayList<>();
    private List<Locale> locales = new ArrayList<>();
    
    public SearchSQLParameters(String query, String language, List<Locale> locales)
    {
        this.query = query;
        this.language = language;
        this.locales = locales;
    }
    
    @Override
    public String getLanguage()
    {
        return language;
    }

    @Override
    public String getQuery()
    {
        return query;
    }

    @Override
    public List<StoreRef> getStores()
    {
        return stores;
    }

    @Override
    public List<Locale> getLocales()
    {
        return locales;
    }

    @Override
    public List<SortDefinition> getSortDefinitions()
    {
        return null;
    }
    /**
     * Set the stores to be supported - currently there can be only one. Searching across multiple stores is on the todo
     * list.
     * 
     * @param store StoreRef
     */
    public void addStore(StoreRef store)
    {
        stores.add(store);
    }
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SearchParameters [language=").append(this.language)
               .append(", query=").append(this.query).append(", stores=").append(this.stores)
               .append(", locales=").append(this.locales).append("]");
        return builder.toString();
        
    }

}
