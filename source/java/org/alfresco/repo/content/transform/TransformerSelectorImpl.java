/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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

/**
 * Default transformer selector implementation, which sorts by priority and then
 * by average transform time. The transform time is only used once a threshold
 * (number of transforms) has been reached. This average is maintained for each
 * source target mimetype pair.<p>
 * 
 * Prior to the introduction of this class the transformation time was only kept
 * for each transformer. There was no threshold and there was a concept of
 * 'Explicit' transformers, which would cause all other transformers to be discarded.
 * It is still possible to disable transformers by giving adding unsupported mappings
 * as has been done for transformers that would not have been used in the past as
 * there existed one or more 'explicit' transformers (a concept not used by this
 * TransformerSelector). By default a transformer has a priority of {@code 10}.
 * Old 'Explicit' transformers have been given a priority of {@code 5}.
 * 
 * @author Alan Davis
 */
public class TransformerSelectorImpl implements TransformerSelector
{
    private TransformerConfig transformerConfig;
    private ContentTransformerRegistry contentTransformerRegistry;

    public void setTransformerConfig(TransformerConfig transformerConfig)
    {
        this.transformerConfig = transformerConfig;
    }

    public void setContentTransformerRegistry(ContentTransformerRegistry contentTransformerRegistry)
    {
        this.contentTransformerRegistry = contentTransformerRegistry;
    }

    @Override
    public List<ContentTransformer> selectTransformers(String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options)
    {
        // TODO cache results for reuse. This was a heavy operation in the past and still is.
        
        // TODO cache results of last few successful transforms as we tend to repeat some of them as part of compound transforms.
        
        List<ContentTransformer> transformers = contentTransformerRegistry.getTransformers();
        List<TransformerSortData> possibleTransformers = findTransformers(transformers, sourceMimetype, sourceSize, targetMimetype, options);
        return sortTransformers(possibleTransformers);
    }

    /**
     * Returns the list of possible transformers for the transformation.
     */
    private List<TransformerSortData> findTransformers(List<ContentTransformer> allTransformers, String sourceMimetype,
            long sourceSize, String targetMimetype, TransformationOptions options)
    {
        List<TransformerSortData> transformers = new ArrayList<TransformerSortData>(8);
        for (ContentTransformer transformer : allTransformers)
        {
            int priority = transformerConfig.getPriority(transformer, sourceMimetype, targetMimetype);
            if (priority > 0 &&
                transformer.isTransformable(sourceMimetype, sourceSize, targetMimetype, options) == true)
                
            {
                transformers.add(new TransformerSortData(transformer, sourceMimetype, targetMimetype, priority));
            }
        }
        return transformers;
    }
    
    /**
     * Returns a sorted list of transformers by priority and then average time (ignored if the threshold
     * has not been reached).
     */
    private List<ContentTransformer> sortTransformers(List<TransformerSortData> possibleTransformers)
    {
        Collections.sort(possibleTransformers);
        
        List<ContentTransformer> transformers = new ArrayList<ContentTransformer>(possibleTransformers.size());
        for (TransformerSortData possibleTransformer: possibleTransformers)
        {
            transformers.add(possibleTransformer.transformer);
        }
        return transformers;
    }
    
    private class TransformerSortData implements Comparable<TransformerSortData>
    {
        private final ContentTransformer transformer;
        private final int priority;
        private final long averageTime;
        private final long count;
        
        TransformerSortData(ContentTransformer transformer, String sourceMimetype, String targetMimetype, int priority)
        {
            this.transformer = transformer;
            this.priority = priority;
            
            TransformerStatistics stats = transformerConfig.getStatistics(transformer, sourceMimetype, targetMimetype, true);
            int threashold = transformerConfig.getThresholdCount(transformer, sourceMimetype, targetMimetype);
            count = stats.getCount();
            averageTime = (count < threashold) ? 0 : stats.getAverageTime();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (averageTime ^ (averageTime >>> 32));
            result = prime * result + priority;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            return transformer == ((TransformerSortData) obj).transformer;
        }

        @Override
        public int compareTo(TransformerSortData that)
        {
            int relativeInt = priority - that.priority;
            if (relativeInt != 0)
            {
                return relativeInt;
            }
            
            long relativeLong = averageTime - that.averageTime;
            relativeInt = relativeLong > 0L ? 1 : relativeLong < 0L ? -1 : 0;
            if (relativeInt != 0)
            {
                return relativeInt;
            }

            relativeLong = count - that.count;
            relativeInt = relativeLong > 0L ? 1 : relativeLong < 0L ? -1 : 0;
            return relativeInt;
        }
    }
}
