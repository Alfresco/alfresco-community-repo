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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Implementation of a transformer selector that matches the code that was in place
 * before a selector was introduced. Class is not used but exists to allow customers
 * to maintain the previous approach if they really wish.
 * 
 * @author Alan Davis
 */
public class TransformerSelectorImplOriginal implements TransformerSelector
{
    private ContentTransformerRegistry contentTransformerRegistry;

    public void setContentTransformerRegistry(ContentTransformerRegistry contentTransformerRegistry)
    {
        this.contentTransformerRegistry = contentTransformerRegistry;
    }

    @Override
    public List<ContentTransformer> selectTransformers( String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options)
    {
        List<ContentTransformer> transformers = contentTransformerRegistry.getTransformers();
        transformers = findTransformers(transformers, sourceMimetype, sourceSize, targetMimetype, options);
        transformers = discardNonExplicitTransformers(transformers, sourceMimetype, sourceSize, targetMimetype, options);
        transformers = sortTransformers(transformers, sourceMimetype, sourceSize, targetMimetype, options);
        return transformers;
    }

    /**
     * Reduces the list of transformers down to only those capable of doing the transformation.
     */
    private List<ContentTransformer> findTransformers(List<ContentTransformer> allTransformers, String sourceMimetype,
            long sourceSize, String targetMimetype, TransformationOptions options)
    {
        List<ContentTransformer> transformers = new ArrayList<ContentTransformer>(2);
        
        for (ContentTransformer transformer : allTransformers)
        {
            if (transformer.isTransformable(sourceMimetype, sourceSize, targetMimetype, options) == true)
            {
                transformers.add(transformer);
            }
        }
        return transformers;
    }
    
    /**
     * Discards non explicit transformers if there are any explicit ones.
     */
    private List<ContentTransformer> discardNonExplicitTransformers(List<ContentTransformer> allTransformers, String sourceMimetype,
            long sourceSize, String targetMimetype, TransformationOptions options)
    {
        List<ContentTransformer> transformers = new ArrayList<ContentTransformer>(2);
        boolean foundExplicit = false;
        
        for (ContentTransformer transformer : allTransformers)
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
        return transformers;
    }

    // sort by performance (quicker is "better")
    private List<ContentTransformer> sortTransformers(List<ContentTransformer> transformers,
            String sourceMimetype, long sourceSize, String targetMimetype,
            TransformationOptions options)
    {
        final Map<ContentTransformer,Long> activeTransformers = new HashMap<ContentTransformer, Long>();
        for (ContentTransformer transformer : transformers)
        {
            long transformationTime = transformer.getTransformationTime(sourceMimetype, targetMimetype);
            activeTransformers.put(transformer, transformationTime);
        }
         
        List<ContentTransformer> sorted = new ArrayList<ContentTransformer>(activeTransformers.keySet());
        Collections.sort(sorted, new Comparator<ContentTransformer>() {
            @Override
            public int compare(ContentTransformer a, ContentTransformer b)
            {
                return activeTransformers.get(a).compareTo(activeTransformers.get(b));
            }
        });
        return sorted;
    }
}
