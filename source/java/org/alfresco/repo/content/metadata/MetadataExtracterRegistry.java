/*
 * Copyright (C) 2005-2012 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.api.AlfrescoPublicApi;  

/**
 * Holds and provides the most appropriate metadate extracter for a particular
 * mimetype.
 * <p>
 * The extracters themselves know how well they are able to extract metadata.
 * 
 * @see org.alfresco.repo.content.metadata.MetadataExtracter
 * @author Jesper Steen Møller
 */
@AlfrescoPublicApi
public class MetadataExtracterRegistry
{
    private static final Log logger = LogFactory.getLog(MetadataExtracterRegistry.class);

    private List<MetadataExtracter> extracters;
    private Map<String, List<MetadataExtracter>> extracterCache;
    private Map<String, List<MetadataEmbedder>> embedderCache;

    /** Controls read access to the cache */
    private Lock extracterCacheReadLock;
    /** controls write access to the cache */
    private Lock extracterCacheWriteLock;

    public MetadataExtracterRegistry()
    {
        // initialise lists
        extracters = new ArrayList<MetadataExtracter>(10);
        extracterCache = new HashMap<String, List<MetadataExtracter>>(17);
        embedderCache = new HashMap<String, List<MetadataEmbedder>>(17);

        // create lock objects for access to the cache
        ReadWriteLock extractionCacheLock = new ReentrantReadWriteLock();
        extracterCacheReadLock = extractionCacheLock.readLock();
        extracterCacheWriteLock = extractionCacheLock.writeLock();
    }
    
    /**
     * Force the registry to drop its cache of extractors.  This is useful for the case where an extractor
     * becomes available only after the registry has initialized the cache.
     */
    public void resetCache()
    {
        extracterCacheWriteLock.lock();
        try
        {
            extracterCache.clear();
            embedderCache.clear();
        }
        finally
        {
            extracterCacheWriteLock.unlock();
        }
    }

    /**
     * Register an instance of an extracter for use
     * 
     * @param extracter an extracter
     */
    public void register(MetadataExtracter extracter)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Registering metadata extracter: " + extracter);
        }

        extracterCacheWriteLock.lock();
        try
        {
            extracters.add(extracter);
            extracterCache.clear();
            embedderCache.clear();
        }
        finally
        {
            extracterCacheWriteLock.unlock();
        }
    }

    /**
     * Gets the best metadata extracter. This is a combination of the most
     * reliable and the most performant extracter.
     * <p>
     * The result is cached for quicker access next time.
     * 
     * @param mimetype the source MIME of the extraction
     * @return Returns a metadata extracter that can extract metadata from the
     *         chosen MIME type.
     */
    public MetadataExtracter getExtracter(String sourceMimetype)
    {
        logger.debug("Get extractors for " + sourceMimetype);
        List<MetadataExtracter> extractors = null;
        extracterCacheReadLock.lock();
        try
        {
            if (extracterCache.containsKey(sourceMimetype))
            {
                // the translation has been requested before
                // it might have been null
                extractors = extracterCache.get(sourceMimetype);
            }
        }
        finally
        {
            extracterCacheReadLock.unlock();
        }

        if (extractors == null)
        {
            // No request has been made before
            // Get a write lock on the cache
            // No double check done as it is not an expensive task
            extracterCacheWriteLock.lock();
            try
            {
                // find the most suitable transformer - may be empty list
                extractors = findBestExtracters(sourceMimetype);
                // store the result even if it is null
                extracterCache.put(sourceMimetype, extractors);
            }
            finally
            {
                extracterCacheWriteLock.unlock();
            }
        }
        
        // We have the list of extractors that supposedly work (as registered).
        // Take the last one that still claims to work
        MetadataExtracter liveExtractor = null;
        for (MetadataExtracter extractor : extractors)
        {
            // An extractor may dynamically become unavailable 
            if (!extractor.isSupported(sourceMimetype))
            {
                logger.debug("Get unsupported: "+getName(extractor));
                continue;
            }
            logger.debug("Get supported:   "+getName(extractor));
            liveExtractor = extractor;
        }
        logger.debug("Get returning:   "+getName(liveExtractor));
        return liveExtractor;
    }
    
    @SuppressWarnings("deprecation")
    private String getName(MetadataExtracter extractor)
    {
        return extractor == null
               ? null
               : extractor instanceof AbstractMetadataExtracter
               ? ((AbstractMetadataExtracter)extractor).getBeanName()
               : extractor instanceof AbstractMappingMetadataExtracter
               ? ((AbstractMappingMetadataExtracter)extractor).getBeanName()
               : extractor.getClass().getSimpleName();
    }

    /**
     * @param       sourceMimetype The MIME type under examination
     * @return      Returns a set of extractors that will work for the given mimetype
     */
    private List<MetadataExtracter> findBestExtracters(String sourceMimetype)
    {
        logger.debug("Finding extractors for " + sourceMimetype);

        List<MetadataExtracter> extractors = new ArrayList<MetadataExtracter>(1);

        for (MetadataExtracter extractor : extracters)
        {
            if (!extractor.isSupported(sourceMimetype))
            {
                // extraction not achievable
                logger.debug("Find unsupported: "+getName(extractor));
                continue;
            }
            logger.debug("Find supported:   "+getName(extractor));
            extractors.add(extractor);
        }
        logger.debug("Find returning:   "+extractors);
        return extractors;
    }
    
    /**
     * Gets the best metadata embedder. This is a combination of the most
     * reliable and the most performant embedder.
     * <p>
     * The result is cached for quicker access next time.
     * 
     * @param mimetype the source MIME of the extraction
     * @return Returns a metadata embedder that can embed metadata in the
     *         chosen MIME type.
     */
    public MetadataEmbedder getEmbedder(String sourceMimetype)
    {
        List<MetadataEmbedder> embedders = null;
        extracterCacheReadLock.lock();
        try
        {
            if (embedderCache.containsKey(sourceMimetype))
            {
                // the translation has been requested before
                // it might have been null
                embedders = embedderCache.get(sourceMimetype);
            }
        }
        finally
        {
            extracterCacheReadLock.unlock();
        }

        if (embedders == null)
        {
            // No request has been made before
            // Get a write lock on the cache
            // No double check done as it is not an expensive task
            extracterCacheWriteLock.lock();
            try
            {
                // find the most suitable transformer - may be empty list
                embedders = findBestEmbedders(sourceMimetype);
                // store the result even if it is null
                embedderCache.put(sourceMimetype, embedders);
            }
            finally
            {
                extracterCacheWriteLock.unlock();
            }
        }
        
        // We have the list of embedders that supposedly work (as registered).
        // Take the last one that still claims to work
        MetadataEmbedder liveEmbedder = null;
        for (MetadataEmbedder embedder : embedders)
        {
            // An extractor may dynamically become unavailable 
            if (!embedder.isEmbeddingSupported(sourceMimetype))
            {
                continue;
            }
            liveEmbedder = embedder;
        }
        return liveEmbedder;
    }
    
    /**
     * @param       sourceMimetype The MIME type under examination
     * @return      Returns a set of embedders that will work for the given mimetype
     */
    private List<MetadataEmbedder> findBestEmbedders(String sourceMimetype)
    {
        logger.debug("Finding embedders for " + sourceMimetype);

        List<MetadataEmbedder> embedders = new ArrayList<MetadataEmbedder>(1);

        for (MetadataExtracter extractor : extracters)
        {
        	if (!(extractor instanceof MetadataEmbedder))
        	{
        		continue;
        	}
            if (!((MetadataEmbedder) extractor).isEmbeddingSupported(sourceMimetype))
            {
                // extraction not achievable
                continue;
            }
            embedders.add((MetadataEmbedder)extractor);
        }
        return embedders;
    }
}
