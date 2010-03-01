/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.admin.patch.impl;

import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Patch usr:user and cm:person objects so that the user name properties are in the 
 * index in untokenized form. If not authentication may fail in mixed language use. 
 * 
 * @author andyh
 *
 */
public class GroupTokenisationPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.groupNamesAsIdentifiers.result";
    
    private ImporterBootstrap userImporterBootstrap;
    private IndexerAndSearcher indexerAndSearcher;
    

    public GroupTokenisationPatch()
    {
        
    }
    
    public void setUserImporterBootstrap(ImporterBootstrap userImporterBootstrap)
    {
        this.userImporterBootstrap = userImporterBootstrap;
    }
    
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        reindex("TYPE:\"usr:authorityContainer\"", userImporterBootstrap.getStoreRef());
        return I18NUtil.getMessage(MSG_SUCCESS);
    }
    
    private void reindex(String query, StoreRef store)
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(store);
        ResultSet rs = null;
        try
        {
            rs = searchService.query(sp);
            for(ResultSetRow row : rs)
            {
                Indexer indexer = indexerAndSearcher.getIndexer(row.getNodeRef().getStoreRef());
                indexer.updateNode(row.getNodeRef());
            }
        }
        finally
        {
          if(rs != null)
          {
              rs.close();
          }
        }
    }
}
