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

package org.alfresco.repo.admin.patch.impl;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch usr:user and cm:person objects so that the user name properties are in the 
 * index in untokenized form. If not authentication may fail in mixed language use. 
 * 
 * @author andyh
 *
 */
public class CalendarModelUriPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.calendarModelNamespacePatch.result";
    
    private static final String URI_BEFORE = "com.infoaxon.alfresco.calendar";
    private static final String URI_AFTER  = "http://www.alfresco.org/model/calendar";
    
    private ImporterBootstrap importerBootstrap;
    private IndexerAndSearcher indexerAndSearcher;
    private QNameDAO qnameDAO;
    
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }
    
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // Make sure the old name spaces exists before we update it ...
        qnameDAO.getOrCreateNamespace(URI_BEFORE);
        // modify namespace for all calendar entries
        qnameDAO.updateNamespace(URI_BEFORE, URI_AFTER);
        
        // reindex the calendar entries
        int count = reindex("TYPE:\\{" + AbstractLuceneQueryParser.escape(URI_BEFORE) + "\\}*", importerBootstrap.getStoreRef());
        return I18NUtil.getMessage(MSG_SUCCESS, count);
    }
    
    private int reindex(String query, StoreRef store)
    {
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(store);
        Indexer indexer = indexerAndSearcher.getIndexer(store);
        ResultSet rs = null;
        int count = 0;
        try
        {
            rs = searchService.query(sp);
            count = rs.length();
            for (ResultSetRow row : rs)
            {
                indexer.updateNode(row.getNodeRef());
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        return count;
    }
}
