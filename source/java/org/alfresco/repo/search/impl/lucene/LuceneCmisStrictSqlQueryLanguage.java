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
package org.alfresco.repo.search.impl.lucene;

import java.util.List;

import org.alfresco.cmis.CMISQueryOptions;
import org.alfresco.cmis.CMISQueryService;
import org.alfresco.cmis.CMISResultSetMetaData;
import org.alfresco.cmis.CMISResultSetRow;
import org.alfresco.cmis.CMISQueryOptions.CMISQueryMode;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.results.ResultSetSPIWrapper;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Support for Alfresco SQL in the search service 
 * @author andyh
 *
 */
public class LuceneCmisStrictSqlQueryLanguage implements LuceneQueryLanguageSPI
{
    private CMISQueryService cmisQueryService;

    /**
     * Set the search service
     * 
     * @param cmisQueryService
     */
    public void setCmisQueryService(CMISQueryService cmisQueryService)
    {
        this.cmisQueryService = cmisQueryService;
    }

    public ResultSet executQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        String sql = searchParameters.getQuery();

        CMISQueryOptions options = new CMISQueryOptions(sql, searchParameters.getStores().get(0));
        options.setFetchSize(searchParameters.getBulkFecthSize());
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
        
        options.setQueryMode(CMISQueryMode.CMS_STRICT);

        return new ResultSetSPIWrapper<CMISResultSetRow, CMISResultSetMetaData>(cmisQueryService.query(options));
    }

    public String getName()
    {
        return SearchService.LANGUAGE_CMIS_STRICT;
    }

    public void setFactories(List<AbstractLuceneIndexerAndSearcherFactory> factories)
    {
        for (AbstractLuceneIndexerAndSearcherFactory factory : factories)
        {
            factory.registerQueryLanguage(this);
        }
    }

}
