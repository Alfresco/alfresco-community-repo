package org.alfresco.repo.content.transform;

import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;   
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Selects a transformer from a supplied list of transformers that appear
 * able to handle a given transformation.
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
public interface TransformerSelector
{
    /**
     * Returns a sorted list of transformers that identifies the order in which transformers
     * should be tried.
     * @param sourceMimetype
     * @param sourceSize
     * @param targetMimetype
     * @param options transformation options
     * @return a sorted list of transformers, with the best one first.
     */
    List<ContentTransformer> selectTransformers(String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options);
}
