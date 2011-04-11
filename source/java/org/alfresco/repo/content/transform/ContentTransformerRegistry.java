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
package org.alfresco.repo.content.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    
    /**
     * @param mimetypeMap all the mimetypes available to the system
     */
    public ContentTransformerRegistry()
    {
        this.transformers = new ArrayList<ContentTransformer>(10);
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
     * Gets the best transformer possible.  This is a combination of the most reliable
     * and the most performant transformer.
     */
    public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // Get the sorted list of transformers
        List<ContentTransformer> transformers = getActiveTransformers(sourceMimetype, targetMimetype, options);

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
    public List<ContentTransformer> getActiveTransformers(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // Get the list of transformers
        List<ContentTransformer> transformers = findTransformers(sourceMimetype, targetMimetype, options);

        final Map<ContentTransformer,Long> activeTransformers = new HashMap<ContentTransformer, Long>();
        
        // identify the performance of all the transformers
         for (ContentTransformer transformer : transformers)
        {
            // Transformability can be dynamic, i.e. it may have become unusable
            if (transformer.isTransformable(sourceMimetype, targetMimetype, options) == false)
            {
                // It is unreliable now.
                continue;
            }
            
            long transformationTime = transformer.getTransformationTime();
            activeTransformers.put(transformer, transformationTime);
        }
         
        // sort by performance (quicker is "better")
        List<ContentTransformer> sorted = new ArrayList<ContentTransformer>(activeTransformers.keySet());
        Collections.sort(sorted, new Comparator<ContentTransformer>() {

            @Override
            public int compare(ContentTransformer a, ContentTransformer b)
            {
                return activeTransformers.get(a).compareTo(activeTransformers.get(b));
            }
            
        });
        
        // All done
        return sorted;
    }
    
    /**
     * Gets all transformers, of equal reliability, that can perform the requested transformation.
     * 
     * @return Returns best transformer for the translation - null if all
     *      score 0.0 on reliability
     */
    private List<ContentTransformer> findTransformers(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        // search for a simple transformer that can do the job
        List<ContentTransformer> transformers = findDirectTransformers(sourceMimetype, targetMimetype, options);
        // get the complex transformers that can do the job
        List<ContentTransformer> complexTransformers = findComplexTransformer(sourceMimetype, targetMimetype, options);
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
    private List<ContentTransformer> findDirectTransformers(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        //double maxReliability = 0.0;
        
        List<ContentTransformer> transformers = new ArrayList<ContentTransformer>(2);
        boolean foundExplicit = false;
        
        // loop through transformers
        for (ContentTransformer transformer : this.transformers)
        {
            if (transformer.isTransformable(sourceMimetype, targetMimetype, options) == true)
            {
                if (transformer.isExplicitTransformation(sourceMimetype, targetMimetype, options) == true)
                {
                    if (foundExplicit == false)
                    {
                        transformers.clear();
                        foundExplicit = true;
                    }
                    transformers.add(transformer);
                }
                else
                {
                    if (foundExplicit == false)
                    {
                        transformers.add(transformer);
                    }
                }
            }
        }
        // done
        return transformers;
    }
    
    /**
     * Uses a list of known mimetypes to build transformations from several direct transformations. 
     */
    private List<ContentTransformer> findComplexTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options)
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
