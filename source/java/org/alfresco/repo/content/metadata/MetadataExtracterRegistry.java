/*
 * Copyright (C) 2005 Jesper Steen Møller
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

/**
 * Holds and provides the most appropriate metadate extracter for a particular
 * mimetype.
 * <p>
 * The extracters themselves know how well they are able to extract metadata.
 * 
 * @see org.alfresco.repo.content.metadata.MetadataExtracter
 * @author Jesper Steen Møller
 */
public class MetadataExtracterRegistry
{
    private static final Log logger = LogFactory.getLog(MetadataExtracterRegistry.class);

    private List<MetadataExtracter> extracters;
    private Map<String, MetadataExtracter> extracterCache;

    /** Controls read access to the cache */
    private Lock extracterCacheReadLock;
    /** controls write access to the cache */
    private Lock extracterCacheWriteLock;

    public MetadataExtracterRegistry()
    {
        // initialise lists
        extracters = new ArrayList<MetadataExtracter>(10);
        extracterCache = new HashMap<String, MetadataExtracter>(17);

        // create lock objects for access to the cache
        ReadWriteLock extractionCacheLock = new ReentrantReadWriteLock();
        extracterCacheReadLock = extractionCacheLock.readLock();
        extracterCacheWriteLock = extractionCacheLock.writeLock();
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
        MetadataExtracter extracter = null;
        extracterCacheReadLock.lock();
        try
        {
            if (extracterCache.containsKey(sourceMimetype))
            {
                // the translation has been requested before
                // it might have been null
                return extracterCache.get(sourceMimetype);
            }
        }
        finally
        {
            extracterCacheReadLock.unlock();
        }

        // the translation has not been requested before
        // get a write lock on the cache
        // no double check done as it is not an expensive task
        extracterCacheWriteLock.lock();
        try
        {
            // find the most suitable transformer - may be empty list
            extracter = findBestExtracter(sourceMimetype);
            // store the result even if it is null
            extracterCache.put(sourceMimetype, extracter);
            return extracter;
        }
        finally
        {
            extracterCacheWriteLock.unlock();
        }
    }

    /**
     * @param sourceMimetype The MIME type under examination
     * @return The fastest of the most reliable extracters in <code>extracters</code>
     *      for the given MIME type, or null if none is available.
     */
    private MetadataExtracter findBestExtracter(String sourceMimetype)
    {
        double bestReliability = -1;
        long bestTime = Long.MAX_VALUE;
        logger.debug("Finding best extracter for " + sourceMimetype);

        MetadataExtracter bestExtracter = null;

        for (MetadataExtracter ext : extracters)
        {
            double r = ext.getReliability(sourceMimetype);
            if (r <= 0.0)
            {
                // extraction not achievable
                continue;
            }
            else if (r == bestReliability)
            {
                long time = ext.getExtractionTime();
                if (time < bestTime)
                {
                    bestExtracter = ext;
                    bestTime = time;
                }
            }
            else if (r > bestReliability)
            {
                bestExtracter = ext;
                bestReliability = r;
                bestTime = ext.getExtractionTime();
            }
        }
        return bestExtracter;
    }
}