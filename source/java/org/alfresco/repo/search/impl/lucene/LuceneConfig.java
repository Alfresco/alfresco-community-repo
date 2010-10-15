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
     * @param lockDirectory
     */
    public void setLockDirectory(String lockDirectory);

    /**
     * The path to the index location
     * 
     * @return
     */
    public String getIndexRootLocation();

    /**
     * The batch size in which to group flushes of the index.
     * 
     * @return
     */
    public int getIndexerBatchSize();

    /**
     * The maximum numbr of sub-queries the can be generated out of wild card expansion etc
     * 
     * @return
     */
    public int getQueryMaxClauses();

    /**
     * The default mode for analysing ML text during index.
     * 
     * @return
     */
    public MLAnalysisMode getDefaultMLIndexAnalysisMode();

    /**
     * The default mode for analysis of ML text during search.
     * 
     * @return
     */
    public MLAnalysisMode getDefaultMLSearchAnalysisMode();

    /**
     * Get the max field length that determine how many tokens are put into the index
     * 
     * @return
     */
    public int getIndexerMaxFieldLength();

    /**
     * Get the thread pool for index merging etc
     * 
     * @return
     */
    public ThreadPoolExecutor getThreadPoolExecutor();

    /**
     * Get preloader - may be null if preloading is not supported
     */
    public NodeBulkLoader getBulkLoader();

    /**
     * Use the nio memory mapping (work arounf for bugs with some JVMs)
     * @return
     */
    public boolean getUseNioMemoryMapping();
    
    /**
     * Max doc number that will merged in memory (and not on disk)
     * 
     * @return
     */
    public int getMaxDocsForInMemoryMerge();
    
    /**
     * Lucene writer config
     * @return
     */
    public int getWriterMaxBufferedDocs();
    
    /**
     * Lucene writer config
     * @return
     */
    public int getWriterMergeFactor();
    
    /**
     * Lucene writer config
     * @return
     */
    public int getWriterMaxMergeDocs();
    
    /**
     * Lucene merger config
     * @return
     */
    public int getMergerMaxBufferedDocs();
    
    /**
     * Lucene merger config
     * @return
     */
    public int getMergerMergeFactor();
    
    /**
     * The factor by which the merge factor is multiplied to determine the allowable number of indexes before blocking.
     * 
     * @return the factor by which the merge factor is multiplied to determine the allowable number of indexes before
     *         blocking
     */
    public int getMergerMergeBlockingFactor();

    /**
     * Lucene merger config
     * @return
     */
    public int getMergerMaxMergeDocs();
    
    /**
     * Target overlays (will apply deletions and create indexes if over this limit)
     * @return
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
     * @return
     */
    public int getMergerTargetIndexCount();
    
    /**
     * Lucene term index interval
     * @return
     */
    public int getTermIndexInterval();
    
    /**
     * Is caching enabled for each index fragment?
     * @return
     */
    public boolean isCacheEnabled();
    
    /**
     * How many categories to cache (-ve => unbounded)
     * @return
     */
    public int getMaxIsCategoryCacheSize();
    
    /**
     * How many documents to cache (-ve => unbounded)
     * @return
     */
    public int getMaxDocumentCacheSize();
    
    /**
     * How many document ids to cache (-ve => unbounded)
     * @return
     */
    public int getMaxDocIdCacheSize();
    
    /**
     * How many paths to cache (-ve => unbounded)
     * @return
     */
    public int getMaxPathCacheSize();
    
    /**
     * How many types to cache (-ve => unbounded)
     * @return
     */
    public int getMaxTypeCacheSize();
    
    /**
     * How many parents to cache (-ve => unbounded)
     * @return
     */
    public int getMaxParentCacheSize();
   
    /**
     * How many link aspects to cache (-ve => unbounded)
     * @return
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
     * @return
     */
    public boolean getPostSortDateTime();

    /**
     * Gets the application context through which events can be broadcast
     * @return
     */
    public ConfigurableApplicationContext getApplicationContext();

    /**
     * Ram based limit for in memory merges
     * @return
     */
    public double getMaxRamInMbForInMemoryMerge();

    /**
     * Ram based limit for in memory portion of writer index.
     * @return
     */
    public double getWriterRamBufferSizeMb();

    /**
     * Ram based limit for in memory portion of merger index.
     * @return
     */
    public double getMergerRamBufferSizeMb();

    /**
     * Max docs to allow for in memory indexes (does no apply to merges)
     * @return
     */
    public int getMaxDocsForInMemoryIndex();

    /**
     * Max Ram to allow for in memory indexes (does not apply to merges)
     * @return
     */
    public double getMaxRamInMbForInMemoryIndex();

    /**
     * Should we use a 'fair' locking policy, giving queue-like access behaviour to the indexes and avoiding starvation?
     * Default is <code>false</code> since fair locking appears to cause deadlock on old JVMs.
     * 
     * @return <code>true</code> if a fair locking policy should be used
     */
    public boolean getFairLocking();

}
