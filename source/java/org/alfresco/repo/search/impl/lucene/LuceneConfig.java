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

import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.node.NodeBulkLoader;
import org.alfresco.repo.search.MLAnalysisMode;
import org.springframework.context.ConfigurableApplicationContext;

public interface LuceneConfig
{
    /**
     * Set the lock dir - just to make sure - this should no longer be used.
     * 
     * @param lockDirectory String
     */
    public void setLockDirectory(String lockDirectory);

    /**
     * The path to the index location
     * 
     * @return String
     */
    public String getIndexRootLocation();

    /**
     * The batch size in which to group flushes of the index.
     * 
     * @return int
     */
    public int getIndexerBatchSize();

    /**
     * The maximum numbr of sub-queries the can be generated out of wild card expansion etc
     * 
     * @return int
     */
    public int getQueryMaxClauses();

    /**
     * The default mode for analysing ML text during index.
     * 
     * @return MLAnalysisMode
     */
    public MLAnalysisMode getDefaultMLIndexAnalysisMode();

    /**
     * The default mode for analysis of ML text during search.
     * 
     * @return MLAnalysisMode
     */
    public MLAnalysisMode getDefaultMLSearchAnalysisMode();

    /**
     * Get the max field length that determine how many tokens are put into the index
     * 
     * @return int
     */
    public int getIndexerMaxFieldLength();

    /**
     * Get the thread pool for index merging etc
     * 
     * @return ThreadPoolExecutor
     */
    public ThreadPoolExecutor getThreadPoolExecutor();

    /**
     * Get preloader - may be null if preloading is not supported
     */
    public NodeBulkLoader getBulkLoader();

    /**
     * Use the nio memory mapping (work arounf for bugs with some JVMs)
     * @return boolean
     */
    public boolean getUseNioMemoryMapping();
    
    /**
     * Max doc number that will merged in memory (and not on disk)
     * 
     * @return int
     */
    public int getMaxDocsForInMemoryMerge();
    
    /**
     * Lucene writer config
     * @return int
     */
    public int getWriterMaxBufferedDocs();
    
    /**
     * Lucene writer config
     * @return int
     */
    public int getWriterMergeFactor();
    
    /**
     * Lucene writer config
     * @return int
     */
    public int getWriterMaxMergeDocs();
    
    /**
     * Lucene merger config
     * @return int
     */
    public int getMergerMaxBufferedDocs();
    
    /**
     * Lucene merger config
     * @return int
     */
    public int getMergerMergeFactor();
    
    /**
     * Lucene merger config
     * @return int
     */
    public int getMergerMaxMergeDocs();
    
    /**
     * Target overlays (will apply deletions and create indexes if over this limit)
     * @return int
     */
    public int getMergerTargetOverlayCount();
    
    /**
     * The factor by which the target overlay count is multiplied to determine the allowable number of overlays before
     * blocking.
     * 
     * @return the factor by which the target overlay count is multiplied to determine the allowable number of overlays
     *         before blocking
     */
    public int getMergerTargetOverlaysBlockingFactor();

    /**
     * Target index count. Over this indexes will be merged together.
     * @return int
     */
    public int getMergerTargetIndexCount();
    
    /**
     * Lucene term index interval
     * @return int
     */
    public int getTermIndexInterval();
    
    /**
     * Is caching enabled for each index fragment?
     * @return boolean
     */
    public boolean isCacheEnabled();
    
    /**
     * How many categories to cache (-ve => unbounded)
     * @return int
     */
    public int getMaxIsCategoryCacheSize();
    
    /**
     * How many documents to cache (-ve => unbounded)
     * @return int
     */
    public int getMaxDocumentCacheSize();
    
    /**
     * How many document ids to cache (-ve => unbounded)
     * @return int
     */
    public int getMaxDocIdCacheSize();
    
    /**
     * How many paths to cache (-ve => unbounded)
     * @return int
     */
    public int getMaxPathCacheSize();
    
    /**
     * How many types to cache (-ve => unbounded)
     * @return int
     */
    public int getMaxTypeCacheSize();
    
    /**
     * How many parents to cache (-ve => unbounded)
     * @return int
     */
    public int getMaxParentCacheSize();
   
    /**
     * How many link aspects to cache (-ve => unbounded)
     * @return int
     */
    public int getMaxLinkAspectCacheSize();

    /**
     * If we are using the DateAnalyser then lucene sort is only to the date, as that is all that is in the index.
     * If this is true, a query that defines a sort on a datetime field will do a post sort in Java.
     * 
     * For the DateTimeAnalyser no post sort is done.
     * (The default config does do a post sort)
     * 
     * In the future, this behaviour may also be set per query on the SearchParameters object.
     * 
     * @return boolean
     */
    public boolean getPostSortDateTime();

    /**
     * Gets the application context through which events can be broadcast
     * @return ConfigurableApplicationContext
     */
    public ConfigurableApplicationContext getApplicationContext();

    /**
     * Ram based limit for in memory merges
     * @return double
     */
    public double getMaxRamInMbForInMemoryMerge();

    /**
     * Ram based limit for in memory portion of writer index.
     * @return double
     */
    public double getWriterRamBufferSizeMb();

    /**
     * Ram based limit for in memory portion of merger index.
     * @return double
     */
    public double getMergerRamBufferSizeMb();

    /**
     * Max docs to allow for in memory indexes (does no apply to merges)
     * @return int
     */
    public int getMaxDocsForInMemoryIndex();

    /**
     * Max Ram to allow for in memory indexes (does not apply to merges)
     * @return double
     */
    public double getMaxRamInMbForInMemoryIndex();

    /**
     * Should we use a 'fair' locking policy, giving queue-like access behaviour to the indexes and avoiding starvation?
     * Default is <code>false</code> since fair locking appears to cause deadlock on old JVMs.
     * 
     * @return <code>true</code> if a fair locking policy should be used
     */
    public boolean getFairLocking();

    /**
     * @param maxAtomicTransformationTime long
     */
    void setMaxAtomicTransformationTime(long maxAtomicTransformationTime);

    /**
     * @return long
     */
    long getMaxTransformationTime();

    /**
     * @return boolean
     */
    public boolean getUseInMemorySort();

    /**
     * @param indexerBatchSize int
     */
    void setIndexerBatchSize(int indexerBatchSize);

    /**
     * @param queryMaxClauses int
     */
    void setQueryMaxClauses(int queryMaxClauses);

    /**
     * @param timeout long
     */
    void setWriteLockTimeout(long timeout);

    /**
     * @param timeout long
     */
    void setCommitLockTimeout(long timeout);

    /**
     * @return long
     */
    long getCommitLockTimeout();

    /**
     * @return long
     */
    long getWriteLockTimeout();

    /**
     * @param time long
     */
    void setLockPollInterval(long time);

    /**
     * @param indexerMaxFieldLength int
     */
    void setIndexerMaxFieldLength(int indexerMaxFieldLength);

    /**
     * @param mode MLAnalysisMode
     */
    void setDefaultMLIndexAnalysisMode(MLAnalysisMode mode);

    /**
     * @param mode MLAnalysisMode
     */
    void setDefaultMLSearchAnalysisMode(MLAnalysisMode mode);

    /**
     * @param maxDocIdCacheSize int
     */
    void setMaxDocIdCacheSize(int maxDocIdCacheSize);

    /**
     * @param maxDocsForInMemoryMerge int
     */
    void setMaxDocsForInMemoryMerge(int maxDocsForInMemoryMerge);

    /**
     * @param maxDocumentCacheSize int
     */
    void setMaxDocumentCacheSize(int maxDocumentCacheSize);

    /**
     * @param maxIsCategoryCacheSize int
     */
    void setMaxIsCategoryCacheSize(int maxIsCategoryCacheSize);

    /**
     * @param maxLinkAspectCacheSize int
     */
    void setMaxLinkAspectCacheSize(int maxLinkAspectCacheSize);

    /**
     * @param maxParentCacheSize int
     */
    void setMaxParentCacheSize(int maxParentCacheSize);

    /**
     * @param maxPathCacheSize int
     */
    void setMaxPathCacheSize(int maxPathCacheSize);

    /**
     * @param maxTypeCacheSize int
     */
    void setMaxTypeCacheSize(int maxTypeCacheSize);

    /**
     * @param mergerMaxMergeDocs int
     */
    void setMergerMaxMergeDocs(int mergerMaxMergeDocs);

    /**
     * @param mergerMergeFactor int
     */
    void setMergerMergeFactor(int mergerMergeFactor);

    /**
     * @param mergerMaxBufferedDocs int
     */
    void setMergerMaxBufferedDocs(int mergerMaxBufferedDocs);

    /**
     * @param mergerTargetIndexCount int
     */
    void setMergerTargetIndexCount(int mergerTargetIndexCount);

    /**
     * @param mergerTargetOverlayCount int
     */
    void setMergerTargetOverlayCount(int mergerTargetOverlayCount);

    /**
     * @param mergerTargetOverlaysBlockingFactor int
     */
    void setMergerTargetOverlaysBlockingFactor(int mergerTargetOverlaysBlockingFactor);

    /**
     * @param fairLocking boolean
     */
    void setFairLocking(boolean fairLocking);

    /**
     * @param termIndexInterval int
     */
    void setTermIndexInterval(int termIndexInterval);

    /**
     * @param useNioMemoryMapping boolean
     */
    void setUseNioMemoryMapping(boolean useNioMemoryMapping);

    /**
     * @param writerMaxMergeDocs int
     */
    void setWriterMaxMergeDocs(int writerMaxMergeDocs);

    /**
     * @param writerMergeFactor int
     */
    void setWriterMergeFactor(int writerMergeFactor);

    /**
     * @return int
     */
    public int getMaxRawResultSetSizeForInMemorySort();

    /**
     * @param writerMaxBufferedDocs int
     */
    void setWriterMaxBufferedDocs(int writerMaxBufferedDocs);

    /**
     * @param cacheEnabled boolean
     */
    void setCacheEnabled(boolean cacheEnabled);

    /**
     * @param postSortDateTime boolean
     */
    void setPostSortDateTime(boolean postSortDateTime);

    /**
     * @param maxDocsForInMemoryIndex int
     */
    void setMaxDocsForInMemoryIndex(int maxDocsForInMemoryIndex);

    /**
     * @param maxRamInMbForInMemoryMerge double
     */
    void setMaxRamInMbForInMemoryMerge(double maxRamInMbForInMemoryMerge);

    /**
     * @param maxRamInMbForInMemoryIndex double
     */
    void setMaxRamInMbForInMemoryIndex(double maxRamInMbForInMemoryIndex);

    /**
     * @param mergerRamBufferSizeMb double
     */
    void setMergerRamBufferSizeMb(double mergerRamBufferSizeMb);

    /**
     * @param writerRamBufferSizeMb double
     */
    void setWriterRamBufferSizeMb(double writerRamBufferSizeMb);

    
    /**
     * 
     * @return if content indexing is enable.
     */
    boolean isContentIndexingEnabled();
    
    /**
     * Enable/Disable the indexing of content 
     * Content is not indexed and FTS disabled
     * When disabled, documents are marked as requiring FTS indexing.
     * When enabled the normal FTS process will catch up with content that was not indexed.
     * @param contentIndexingEnabled boolean
     */
            
    void setContentIndexingEnabled(boolean contentIndexingEnabled);
}
