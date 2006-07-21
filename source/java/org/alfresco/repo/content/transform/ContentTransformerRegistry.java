/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.content.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.MimetypeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * Holds and provides the most appropriate content transformer for
 * a particular source and target mimetype transformation request.
 * <p>
 * The transformers themselves are used to determine the applicability
 * of a particular transformation.
 *
 * @see org.alfresco.repo.content.transform.ContentTransformer
 * 
 * @author Derek Hulley
 */
public class ContentTransformerRegistry
{
    private static final Log logger = LogFactory.getLog(ContentTransformerRegistry.class);
    
    private List<ContentTransformer> transformers;
    private MimetypeMap mimetypeMap;
    /** Cache of previously used transactions */
    private Map<TransformationKey, List<ContentTransformer>> transformationCache;
    /** Controls read access to the transformation cache */
    private Lock transformationCacheReadLock;
    /** controls write access to the transformation cache */
    private Lock transformationCacheWriteLock;
    
    /**
     * @param mimetypeMap all the mimetypes available to the system
     */
    public ContentTransformerRegistry(MimetypeMap mimetypeMap)
    {
        Assert.notNull(mimetypeMap, "The MimetypeMap is mandatory");
        this.mimetypeMap = mimetypeMap;
        
        this.transformers = new ArrayList<ContentTransformer>(10);
        transformationCache = new HashMap<TransformationKey, List<ContentTransformer>>(17);
        
        // create lock objects for access to the cache
        ReadWriteLock transformationCacheLock = new ReentrantReadWriteLock();
        transformationCacheReadLock = transformationCacheLock.readLock();
        transformationCacheWriteLock = transformationCacheLock.writeLock();
    }
    
    /**
     * Register an individual transformer against a specific transformation.  This transformation
     * will take precedence over any of the generally-registered transformers.
     * 
     * @param key the mapping from one mimetype to the next
     * @param transformer the content transformer
     */
    public void addExplicitTransformer(TransformationKey key, ContentTransformer transformer)
    {
        transformationCache.put(key, Collections.singletonList(transformer));
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered explicit transformation: \n" +
                    "   key: " + key + "\n" +
                    "   transformer: " + transformer);
        }
    }
    
    /**
     * Registers an individual transformer that can be queried to check for applicability.
     *  
     * @param transformer a content transformer
     */
    public void addTransformer(ContentTransformer transformer)
    {
        transformers.add(transformer);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered general transformer: \n" +
                    "   transformer: " + transformer);
        }
    }

    /**
     * Resets the transformation cache.  This allows a fresh analysis of the best
     * conversions based on actual average performance of the transformers.
     */
    public void resetCache()
    {
        // get a write lock on the cache
        transformationCacheWriteLock.lock();
        try
        {
            transformationCache.clear();
        }
        finally
        {
            transformationCacheWriteLock.unlock();
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Content transformation cache reset");
        }
    }
    
    /**
     * Gets the best transformer possible.  This is a combination of the most reliable
     * and the most performant transformer.
     * <p>
     * The result is cached for quicker access next time.
     * 
     * @param sourceMimetype the source mimetype of the transformation
     * @param targetMimetype the target mimetype of the transformation
     * @return Returns a content transformer that can perform the desired
     *      transformation or null if no transformer could be found that would do it.
     */
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        // check that the mimetypes are valid
        if (!mimetypeMap.getMimetypes().contains(sourceMimetype))
        {
            throw new AlfrescoRuntimeException("Unknown source mimetype: " + sourceMimetype);
        }
        if (!mimetypeMap.getMimetypes().contains(targetMimetype))
        {
            throw new AlfrescoRuntimeException("Unknown target mimetype: " + targetMimetype);
        }
        
        TransformationKey key = new TransformationKey(sourceMimetype, targetMimetype);
        List<ContentTransformer> transformers = null;
        transformationCacheReadLock.lock();
        try
        {
            if (transformationCache.containsKey(key))
            {
                // the translation has been requested before
                // it might have been null
                transformers = transformationCache.get(key);
            }
        }
        finally
        {
            transformationCacheReadLock.unlock();
        }
        
        if (transformers == null)
        {
            // the translation has not been requested before
            // get a write lock on the cache
            // no double check done as it is not an expensive task
            transformationCacheWriteLock.lock();
            try
            {
                // find the most suitable transformer - may be empty list
                transformers = findTransformers(sourceMimetype, targetMimetype);
                // store the result even if it is null
                transformationCache.put(key, transformers);
            }
            finally
            {
                transformationCacheWriteLock.unlock();
            }
        }
        // select the most performant transformer
        long bestTime = -1L;
        ContentTransformer bestTransformer = null;
        for (ContentTransformer transformer : transformers)
        {
            long transformationTime = transformer.getTransformationTime();
            // is it better?
            if (bestTransformer == null || transformationTime < bestTime)
            {
                bestTransformer = transformer;
                bestTime = transformationTime;
            }
        }
        // done
        return bestTransformer;
    }
    
    /**
     * Gets all transformers, of equal reliability, that can perform the requested transformation.
     * 
     * @return Returns best transformer for the translation - null if all
     *      score 0.0 on reliability
     */
    private List<ContentTransformer> findTransformers(String sourceMimetype, String targetMimetype)
    {
        // search for a simple transformer that can do the job
        List<ContentTransformer> transformers = findDirectTransformers(sourceMimetype, targetMimetype);
        // get the complex transformers that can do the job
        List<ContentTransformer> complexTransformers = findComplexTransformer(sourceMimetype, targetMimetype);
        transformers.addAll(complexTransformers);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Searched for transformer: \n" +
                    "   source mimetype: " + sourceMimetype + "\n" +
                    "   target mimetype: " + targetMimetype + "\n" +
                    "   transformers: " + transformers);
        }
        return transformers;
    }
    
    /**
     * Loops through the content transformers and picks the ones with the highest reliabilities.
     * <p>
     * Where there are several transformers that are equally reliable, they are all returned.
     * 
     * @return Returns the most reliable transformers for the translation - empty list if there
     *      are none.
     */
    private List<ContentTransformer> findDirectTransformers(String sourceMimetype, String targetMimetype)
    {
        double maxReliability = 0.0;
        List<ContentTransformer> bestTransformers = new ArrayList<ContentTransformer>(2);
        // loop through transformers
        for (ContentTransformer transformer : this.transformers)
        {
            double reliability = transformer.getReliability(sourceMimetype, targetMimetype);
            if (reliability <= 0.0)
            {
                // it is unusable
                continue;
            }
            else if (reliability < maxReliability)
            {
                // it is not the best one to use
                continue;
            }
            else if (reliability == maxReliability)
            {
                // it is as reliable as a previous transformer
            }
            else
            {
                // it is better than any previous transformer - wipe them
                bestTransformers.clear();
                maxReliability = reliability;
            }
            // add the transformer to the list
            bestTransformers.add(transformer);
        }
        // done
        return bestTransformers;
    }
    
    /**
     * Uses a list of known mimetypes to build transformations from several direct transformations. 
     */
    private List<ContentTransformer> findComplexTransformer(String sourceMimetype, String targetMimetype)
    {
        // get a complete list of mimetypes
        // TODO: Build complex transformers by searching for transformations by mimetype
        return Collections.emptyList();
    }
    
    /**
     * Recursive method to build up a list of content transformers
     */
    @SuppressWarnings("unused")
    private void buildTransformer(List<ContentTransformer> transformers,
            double reliability,
            List<String> touchedMimetypes,
            String currentMimetype,
            String targetMimetype)
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * A key for a combination of a source and target mimetype
     */
    public static class TransformationKey
    {
        private final String sourceMimetype;
        private final String targetMimetype;
        private final String key;
        
        public TransformationKey(String sourceMimetype, String targetMimetype)
        {
            this.key = (sourceMimetype + "_" + targetMimetype);
            this.sourceMimetype = sourceMimetype;
            this.targetMimetype = targetMimetype;
        }
        
        public String getSourceMimetype()
        {
            return sourceMimetype;
        }
        public String getTargetMimetype()
        {
            return targetMimetype;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            else if (this == obj)
            {
                return true;
            }
            else if (!(obj instanceof TransformationKey))
            {
                return false;
            }
            TransformationKey that = (TransformationKey) obj;
            return this.key.equals(that.key);
        }
        @Override
        public int hashCode()
        {
            return key.hashCode();
        }
    }
}
