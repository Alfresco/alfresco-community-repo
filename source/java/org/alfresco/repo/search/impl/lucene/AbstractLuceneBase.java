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
package org.alfresco.repo.search.impl.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.alfresco.repo.search.IndexerException;
import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardAnalyser;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo;
import org.alfresco.repo.search.impl.lucene.index.TransactionStatus;
import org.alfresco.repo.search.impl.lucene.index.IndexInfo.LockWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

/**
 * Common support for abstracting the lucene indexer from its configuration and management requirements.
 * 
 * <p>
 * This class defines where the indexes are stored. This should be via a configurable Bean property in Spring.
 * 
 * <p>
 * The default file structure is
 * <ol>
 * <li><b>"base"/"protocol"/"name"/</b> for the main index
 * <li><b>"base"/"protocol"/"name"/deltas/"id"</b> for transactional updates
 * <li><b>"base"/"protocol"/"name"/undo/"id"</b> undo information
 * </ol>
 * 
 * <p>
 * The IndexWriter and IndexReader for a given index are toggled (one should be used for delete and the other for write). These are reused/closed/initialised as required.
 * 
 * <p>
 * The index deltas are buffered to memory and persisted in the file system as required.
 * 
 * @author Andy Hind
 * 
 */

public abstract class AbstractLuceneBase
{
    private static Log    s_logger = LogFactory.getLog(AbstractLuceneBase.class);

    private IndexInfo indexInfo;

    /**
     * The identifier for the store
     */

    protected StoreRef store;

    /**
     * The identifier for the delta
     */

    protected String deltaId;

    private LuceneConfig config;

    private TransactionStatus status = TransactionStatus.UNKNOWN;

    // "lucene-indexes";

    /**
     * Initialise the configuration elements of the lucene store indexers and searchers.
     * 
     * @param store
     * @param deltaId
     * @throws IOException
     */
    protected void initialise(StoreRef store, String deltaId)
            throws LuceneIndexException
    {
        this.store = store;
        this.deltaId = deltaId;

        String basePath = getBasePath();
        File baseDir = new File(basePath);
        indexInfo = IndexInfo.getIndexInfo(baseDir, config);
        try
        {
            if (this.deltaId != null)
            {
                if (! getStatus().equals(TransactionStatus.ACTIVE))
                {
                    setStatus(TransactionStatus.ACTIVE);
                }
                else
                {
                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("Delta already set as active " + deltaId);
                    }
                }
            }
        }
        catch (IOException e)
        {
            throw new IndexerException("Failed to set delta as active");
        }
    }

    /**
     * Utility method to find the path to the base index
     * 
     * @return - the base path
     */
    private String getBasePath()
    {
        if (config.getIndexRootLocation() == null)
        {
            throw new IndexerException("No configuration for index location");
        }
        String basePath = config.getIndexRootLocation()
                + File.separator + store.getProtocol() + File.separator + store.getIdentifier() + File.separator;
        return basePath;
    }

    /**
     * Get a searcher for the main index TODO: Split out support for the main index. We really only need this if we want to search over the changing index before it is committed
     * 
     * @return - the searcher
     * @throws IOException
     */

    protected IndexSearcher getSearcher() throws LuceneIndexException
    {
        try
        {
            return new ClosingIndexSearcher(indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader());
        }
        catch (IOException e)
        {
            s_logger.error("Error", e);
            throw new LuceneIndexException("Failed to open IndexSarcher for " + getBasePath(), e);
        }
    }

    protected ClosingIndexSearcher getSearcher(LuceneIndexer luceneIndexer) throws LuceneIndexException
    {
        // If we know the delta id we should do better

        try
        {
            if (luceneIndexer == null)
            {
                return new ClosingIndexSearcher(indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader());
            }
            else
            {
                // TODO: Create appropriate reader that lies about deletions
                // from the first
                //
                luceneIndexer.flushPending();
                return new ClosingIndexSearcher(indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader(deltaId,
                        luceneIndexer.getDeletions(), luceneIndexer.getContainerDeletions(), luceneIndexer
                                .getDeleteOnlyNodes()));
            }

        }
        catch (IOException e)
        {
            s_logger.error("Error", e);
            throw new LuceneIndexException("Failed to open IndexSarcher for " + getBasePath(), e);
        }
    }

    /**
     * Get a reader for the on file portion of the delta
     * 
     * @return - the index reader
     * @throws IOException
     * @throws IOException
     */

    protected IndexReader getDeltaReader() throws LuceneIndexException, IOException
    {
        return indexInfo.getDeltaIndexReader(deltaId);
    }

    /**
     * Close the on file reader for the delta if it is open
     * 
     * @throws IOException
     * 
     * @throws IOException
     */

    protected void closeDeltaReader() throws LuceneIndexException, IOException
    {
        indexInfo.closeDeltaIndexReader(deltaId);
    }

    /**
     * Get the on file writer for the delta
     * 
     * @return - the writer for the delta
     * @throws IOException
     * @throws IOException
     */
    protected IndexWriter getDeltaWriter() throws LuceneIndexException, IOException
    {
        return indexInfo.getDeltaIndexWriter(deltaId, new LuceneAnalyser(dictionaryService, config.getDefaultMLIndexAnalysisMode()));
    }

    /**
     * Close the on disk delta writer
     * 
     * @throws IOException
     * 
     * @throws IOException
     */

    protected void closeDeltaWriter() throws LuceneIndexException, IOException
    {
        indexInfo.closeDeltaIndexWriter(deltaId);
    }

    /**
     * Save the in memory delta to the disk, make sure there is nothing held in memory
     * 
     * @throws IOException
     * 
     * @throws IOException
     */
    protected void saveDelta() throws LuceneIndexException, IOException
    {
        // Only one should exist so we do not need error trapping to execute the
        // other
        closeDeltaReader();
        closeDeltaWriter();
    }

    protected void setInfo(long docs, Set<String> deletions, Set<String> containerDeletions, boolean deleteNodesOnly) throws IOException
    {
        indexInfo.setPreparedState(deltaId, deletions, containerDeletions, docs, deleteNodesOnly);
    }

    protected void setStatus(TransactionStatus status) throws IOException
    {
        indexInfo.setStatus(deltaId, status, null, null);
        this.status = status;
    }
    
    protected TransactionStatus getStatus()
    {
        return status;
    }

    
    
    private DictionaryService dictionaryService;

    protected IndexReader getReader() throws LuceneIndexException, IOException
    {
        return indexInfo.getMainIndexReferenceCountingReadOnlyIndexReader();
    }

    /**
     * Set the dictionary service
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Get the dictionary service.
     * 
     * @return - the service
     */
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    /**
     * Set the lucene configuration options
     * 
     * @param config
     */
    public void setLuceneConfig(LuceneConfig config)
    {
        this.config = config;
    }

    /**
     * Get the lucene configuration options.
     * 
     * @return - the config options object.
     */
    public LuceneConfig getLuceneConfig()
    {
        return config;
    }

    /**
     * Get the ID for the delat we are working with.
     * 
     * @return - the id
     */
    public String getDeltaId()
    {
        return deltaId;
    }
    

    /**
     * Execute actions against a read only index (all write ops will block)
     * 
     * @param <R>
     * @param lockWork
     * @return - the result returned by the action.
     */
    public <R> R doReadOnly(LockWork<R> lockWork)
    {
        return indexInfo.doReadOnly(lockWork);
    }

    
    public void deleteIndex()
    {
        indexInfo.delete(deltaId);
    }
   

}
