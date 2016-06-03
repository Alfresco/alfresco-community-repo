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
