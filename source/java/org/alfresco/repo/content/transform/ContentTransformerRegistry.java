/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds and provides the most appropriate content transformer for
 * a particular source and target mimetype transformation request.
 * <p>
 * The transformers themselves are used to determine the applicability
 * of a particular transformation.
 * <p>
 * The actual selection of a transformer is done by the injected
 * {@link TransformerSelector}.
 * 
 * @see org.alfresco.repo.content.transform.ContentTransformer
 * 
 * @author Derek Hulley
 */
public class ContentTransformerRegistry
{
    private static final Log logger = LogFactory.getLog(ContentTransformerRegistry.class);
    
    private final List<ContentTransformer> transformers;
    private final List<ContentTransformer> allTransformers;
    
    private final TransformerSelector transformerSelector;
    
    /**
     * @param mimetypeMap all the mimetypes available to the system
     */
    public ContentTransformerRegistry(TransformerSelector transformerSelector)
    {
        this.transformerSelector = transformerSelector;
        this.transformers = new ArrayList<ContentTransformer>(70);
        this.allTransformers = new ArrayList<ContentTransformer>(70);
    }
    
    /**
     * Registers an individual transformer that can be queried to check for applicability.
     *  
     * @param transformer a content transformer
     */
    public synchronized void addTransformer(ContentTransformer transformer)
    {
        transformers.add(transformer);
        allTransformers.add(transformer);
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Registered general transformer: \n" +
                    "   transformer: " + transformer.getName() + " (" + transformer + ")");
        }
    }
    
    /**
     * Records a transformer that can NOT be queried for applicability, but may be
     * included as a component of complex transformers.
     * @param transformer a content transformer
     */
    public synchronized void addComponentTransformer(ContentTransformer transformer)
    {
        allTransformers.add(transformer);
    }

    /**
     * Removes a dynamically created transformer.
     * @param transformer to be removed.
     */
    public synchronized void removeTransformer(ContentTransformer transformer)
    {
        transformers.remove(transformer);
        allTransformers.remove(transformer);
    }

    /**
     * @return a list of transformers that may be queried to check for applicability.
     */
    public synchronized List<ContentTransformer> getTransformers()
    {
        return Collections.unmodifiableList(transformers);
    }
    
    /**
     * @return a list of all transformers, including those that only exist as a
     *         component of another transformer.
     */
    public synchronized List<ContentTransformer> getAllTransformers()
    {
        return Collections.unmodifiableList(allTransformers);
    }
    
    /**
     * Returns a transformer identified by name.
     * @throws IllegalArgumentException if transformerName is not found.
     */
    public synchronized ContentTransformer getTransformer(String transformerName)
    {
        if (transformerName != null)
        {
            for (ContentTransformer transformer: allTransformers)
            {
                if (transformerName.equals(transformer.getName()))
                {
                    return transformer;
                }
            }
            throw new IllegalArgumentException("Unknown transformer: "+
                    (transformerName.startsWith(TransformerConfig.TRANSFORMER)
                    ? transformerName.substring(TransformerConfig.TRANSFORMER.length())
                    : transformerName));
        }
        return null;
    }
    
    /**
     * @deprecated use overloaded version with sourceSize parameter.
     */
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return getTransformer(sourceMimetype, -1, targetMimetype, options);
    }
    
    /**
     * Gets the best transformer possible.  This is a combination of the most reliable
     * and the most performant transformer.
     */
    public ContentTransformer getTransformer(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        // Get the sorted list of transformers
        List<ContentTransformer> transformers = getActiveTransformers(sourceMimetype, sourceSize, targetMimetype, options);

        // select the most performant transformer
        ContentTransformer bestTransformer = null;
        if(transformers.size() > 0)
        {
            bestTransformer = transformers.get(0);
        }
        // done
        return bestTransformer;
    }
    
    /**
     * @since 3.5
     */
    public List<ContentTransformer> getActiveTransformers(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        // Get the list of transformers
        List<ContentTransformer> transformers = transformerSelector.selectTransformers(sourceMimetype, sourceSize, targetMimetype, options);
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
     * 
     * @Deprecated since 3.0
     */
    @Deprecated
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
