package org.alfresco.repo.content.transform;

import net.sf.jooreports.converter.DocumentFormatRegistry;

import org.alfresco.api.AlfrescoPublicApi;   
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Makes use of a {@link ContentTransformerWorker} to perform conversions.
 * 
 * @author dward
 */
@AlfrescoPublicApi
public class ProxyContentTransformer extends AbstractContentTransformer2
{
    private ContentTransformerWorker worker;

    public ProxyContentTransformer()
    {
    }

    /**
     * @param worker
     *            the worker that the converter uses
     */
    public void setWorker(ContentTransformerWorker worker)
    {
        this.worker = worker;
    }
    
    /**
     * Returns the worker that the converter uses
     */
    public ContentTransformerWorker getWorker()
    {
        return this.worker;
    }

    /**
     * THIS IS A CUSTOM SPRING INIT METHOD 
     */
    public void register()
    {
        if (worker instanceof ContentTransformerHelper)
        {
            logDeprecatedSetter(((ContentTransformerHelper)worker).deprecatedSetterMessages);
        }
        super.register();
    }

    /**
     * @see DocumentFormatRegistry
     */
    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return this.worker.isTransformable(sourceMimetype, targetMimetype, options);
    }

    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getComments(available));
        sb.append(worker.getComments(false));
        return sb.toString();
    }

    protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options)
            throws Exception
    {
        TransformationOptionLimits original = options.getLimits();
        try
        {
            // Combine the transformer's limit values into the options so they are available to the worker
            options.setLimits(getLimits(reader, writer, options));

            // Perform the transformation
            this.worker.transform(reader, writer, options);
        }
        finally
        {
            options.setLimits(original);
        }
   }
}
