package org.alfresco.repo.content.transform;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * An interface that allows separation between the content transformer registry and the various third party subsystems
 * performing the transformation.
 * 
 * @author dward
 */
// TODO Modify ContentTransformerWorker to understand transformer limits. At the moment no workers use them
@AlfrescoPublicApi
public interface ContentTransformerWorker
{
    /**
     * Checks if this worker is available.
     * 
     * @return true if it is available
     */
    public boolean isAvailable();

    /**
     * Gets a string returning product and version information.
     * 
     * @return the version string
     */
    public String getVersionString();

    /**
     * Unlike {@link ContentTransformer#isTransformable(String, String, TransformationOptions)} 
     * should not include the transformer name, as that is added by the ContentTransformer in
     * the parent context.
     */
    public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options);    

    /**
     * @see ContentTransformer#getComments(boolean)
     */
    public String getComments(boolean available);

    /**
     * @see ContentTransformer#transform(ContentReader, ContentWriter, TransformationOptions)
     */
    public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception;
}
