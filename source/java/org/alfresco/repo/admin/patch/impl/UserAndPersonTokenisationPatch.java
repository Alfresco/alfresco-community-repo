/*
 * Copyright (C) 2005 Alfresco, Inc.
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

package org.alfresco.repo.admin.patch.impl;

import org.alfresco.i18n.I18NUtil;
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
public class UserAndPersonTokenisationPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.userAndPersonUserNamesAsIdentifiers.result";
    
    private ImporterBootstrap spacesImporterBootstrap;
    private ImporterBootstrap userImporterBootstrap;
    private IndexerAndSearcher indexerAndSearcher;
    

    public UserAndPersonTokenisationPatch()
    {
        
    }

    public void setSpacesImporterBootstrap(ImporterBootstrap spacesImporterBootstrap)
    {
        this.spacesImporterBootstrap = spacesImporterBootstrap;
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
        reindex("TYPE:\"usr:user\"", userImporterBootstrap.getStoreRef());
        reindex("TYPE:\"cm:person\"", spacesImporterBootstrap.getStoreRef());
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
