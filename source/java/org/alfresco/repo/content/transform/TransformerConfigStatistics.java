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

import static org.alfresco.repo.content.transform.TransformerConfig.ANY;
import static org.alfresco.repo.content.transform.TransformerConfig.SUMMARY_TRANSFORMER_NAME;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.MimetypeService;

/**
 * Provides a place to store statistics about:
 * a) the combination of transformer, source and target mimetype;
 * b) a summary for each transformer;
 * c) a summary of top level transformations (++) for each combination of
 *    source and target mimetype;
 * d) a summary of all top level transformations.
 * These values are not shared across the cluster but are node specific.<p>
 * 
 * ++ Top level transformations don't include transformations performed as part
 * of another transformation.
 *
 * @author Alan Davis
 */
public class TransformerConfigStatistics
{
    private TransformerConfigImpl transformerConfigImpl;
    private MimetypeService mimetypeService;

    // Holds statistics about each transformer, sourceMimeType and targetMimetype combination.
    // A null transformer is the system wide value. Null sourceMimeType and targetMimetype values are
    // transformer wide summaries.
    private Map<String, DoubleMap<String, String, TransformerStatistics>> statistics =
            new HashMap<String, DoubleMap<String, String, TransformerStatistics>>();
            
    public TransformerConfigStatistics(TransformerConfigImpl transformerConfigImpl,
            MimetypeService mimetypeService)
    {
        this.transformerConfigImpl = transformerConfigImpl;
        this.mimetypeService = mimetypeService;
    }

    public TransformerStatistics getStatistics(ContentTransformer transformer, String sourceMimetype, String targetMimetype, boolean createNew)
    {
        if (sourceMimetype == null)
        {
            sourceMimetype = ANY;
        }
        
        if (targetMimetype == null)
        {
            targetMimetype = ANY;
        }

        TransformerStatistics transformerStatistics;
        
        String name = (transformer == null) ? SUMMARY_TRANSFORMER_NAME : transformer.getName();
        DoubleMap<String, String, TransformerStatistics> mimetypeStatistics = statistics.get(name);

        if (!createNew)
        {
            transformerStatistics = (mimetypeStatistics == null)
                    ? null
                    : mimetypeStatistics.getNoWildcards(sourceMimetype, targetMimetype);
            return transformerStatistics;
        }
        
        if (mimetypeStatistics == null)
        {
            // Create the summary for the transformer as a whole
            mimetypeStatistics = new DoubleMap<String, String, TransformerStatistics>(ANY, ANY);
            statistics.put(name, mimetypeStatistics);
            transformerStatistics = newTransformerStatistics(transformer, ANY, ANY, null);
            mimetypeStatistics.put(ANY, ANY, transformerStatistics);
        }

        if (ANY.equals(sourceMimetype) && ANY.equals(targetMimetype))
        {
            transformerStatistics = mimetypeStatistics.get(ANY, ANY);
        }
        else
        {
            // Not looking for the summary, so will have to create it if not found or the summary is returned 
            transformerStatistics = mimetypeStatistics.get(sourceMimetype, targetMimetype);
            if (transformerStatistics == null || transformerStatistics.isSummary())
            {
                // Create individual mimetype to mimetype transformation by this transformer
                transformerStatistics = newTransformerStatistics(transformer, sourceMimetype, targetMimetype, mimetypeStatistics.get(ANY, ANY));
                mimetypeStatistics.put(sourceMimetype, targetMimetype, transformerStatistics);
            }
        }
        
        return transformerStatistics;
    }

    private TransformerStatistics newTransformerStatistics(ContentTransformer transformer,
            String sourceMimetype, String targetMimetype, TransformerStatistics parent)
    {
        long initialAverageTime = transformerConfigImpl.getInitialAverageTime(transformer, sourceMimetype, targetMimetype);
        long initialCount = initialAverageTime <= 0
           ? 0
           : transformerConfigImpl.getInitialCount(transformer, sourceMimetype, targetMimetype);
        long errorTime = transformerConfigImpl.getErrorTime(transformer, sourceMimetype, targetMimetype);

        TransformerStatistics transformerStatistics = new TransformerStatisticsImpl(mimetypeService, sourceMimetype, targetMimetype,
                transformer, parent, errorTime, initialAverageTime, initialCount);

        return transformerStatistics;
    }
}
