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
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A patch to update the value of a QName.
 * This patch will only succeed if the target QName has not been used i.e. if there is no content
 * that actually references the QName.
 * <P/>
 * A property 'reindexClass' can be optionally injected. If it is not injected then the QName is
 * updated and no reindexing is requested by this patch.
 * If it is set to either 'TYPE' or 'ASPECT' (as appropriate) then that String will be used to
 * locate out-of-date references to the old QName and have them reindexed in a targetted way.
 * <P/>
 * Please refer to the implementation in this class for the details of how this is achieved.
 * 
 * @author Neil McErlean
 */
public class QNamePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.QNamePatch.result";

    /* Injected properties */
    private String qnameStringBefore;
    private String qnameStringAfter;
    private String reindexClass;
    
    /* Injected services */
    private ImporterBootstrap importerBootstrap;
    private IndexerAndSearcher indexerAndSearcher;
    private QNameDAO qnameDAO;

    /**
     * Sets the importerBootstrap.
     * @param importerBootstrap.
     */
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    /**
     * Sets the IndexerAndSearcher.
     * @param indexerAndSearcher
     */
    public void setIndexerAndSearcher(IndexerAndSearcher indexerAndSearcher)
    {
        this.indexerAndSearcher = indexerAndSearcher;
    }
    
    /**
     * Sets the QNameDAO.
     * @param qnameDAO
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * Sets the QName to be patched.
     * @param qnameStringBefore the long-form QName to be patched from. {namespaceURI}localName
     */
    public void setQnameBefore(String qnameStringBefore)
    {
        this.qnameStringBefore = qnameStringBefore;
    }

    /**
     * Sets the new QName value to be used.
     * @param qnameStringAfter the long-form QName to be patched to. {namespaceURI}localName
     */
    public void setQnameAfter(String qnameStringAfter)
    {
        this.qnameStringAfter = qnameStringAfter;
    }
    
    /**
     * Sets a value for the class to reindex. This will be used in the Lucene query below and
     * should be either "TYPE" or "ASPECT" or not set if reindexing is not required.
     * @param reindexClass "TYPE" or "ASPECT" or not set.
     */
    public void setReindexClass(String reindexClass)
    {
        this.reindexClass = reindexClass;
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        // We don't need to catch the potential InvalidQNameException here as it will be caught
        // in AbstractPatch and correctly handled there
        QName qnameBefore = QName.createQName(this.qnameStringBefore);
        QName qnameAfter = QName.createQName(this.qnameStringAfter);

        if (qnameDAO.getQName(qnameBefore) != null)
        {
            qnameDAO.updateQName(qnameBefore, qnameAfter);
        }
        
        // Optionally perform a focussed reindexing of the removed QName.
        if ("TYPE".equals(reindexClass) ||
                "ASPECT".equals(reindexClass))
        {
            reindex(reindexClass + ":" + AbstractLuceneQueryParser.escape(qnameStringBefore), importerBootstrap.getStoreRef());
        }

        return I18NUtil.getMessage(MSG_SUCCESS, qnameBefore, qnameAfter);
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
