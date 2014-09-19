/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.io.File;
import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class is a transformer which contains a fixed sequence of delegate transformers.
 * Requests to transform a document will be passed to the first transformer in the sequence.
 * If that transformer successfully transforms the document then the process is complete. However
 * should it fail, the transformation will be passed on to the next transformer in the sequence and
 * so on.
 * <P/>Transformers are considered to have failed of they throw an exception.
 * 
 * @author Neil McErlean
 */
@AlfrescoPublicApi
public class FailoverContentTransformer extends AbstractContentTransformer2 implements InitializingBean
{
    private static Log logger = LogFactory.getLog(FailoverContentTransformer.class);
    private List<ContentTransformer> transformers;

    public FailoverContentTransformer()
    {
        // Intentionally empty
    }

    /**
     * The list of transformers to use. There must be at least one, but for failover behaviour to work
     * there should be at least two.
     * 
     * @param transformers list of transformers.
     */
    public void setTransformers(List<ContentTransformer> transformers)
    {
        this.transformers = transformers;
    }

    /**
     * Ensures that required properties have been set
     */
    public void afterPropertiesSet() throws Exception
    {
        if (transformers == null || transformers.size() == 0)
        {
            throw new AlfrescoRuntimeException("At least one inner transformer must be supplied: " + this);
        }
        if (getMimetypeService() == null)
        {
            throw new AlfrescoRuntimeException("'mimetypeService' is a required property");
        }
    }
    
    /**
     * Overrides super class method to avoid calling
     * {@link #isTransformableMimetype(String, String, TransformationOptions)}
     * twice on each transformer in the list, as
     * {@link #isTransformableSize(String, long, String, TransformationOptions)}
     * in this class must check the mimetype too.
     */
    @Override
    public boolean isTransformable(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        return
            isSupportedTransformation(sourceMimetype, targetMimetype, options) &&
            // isTransformableSize must check the mimetype anyway
            (((sourceSize >= 0) && isTransformableSize(sourceMimetype, sourceSize, targetMimetype, options)) ||
             ((sourceSize < 0) && isTransformableMimetype(sourceMimetype,targetMimetype, options)));
    }

    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
        return isTransformableMimetypeAndSize(sourceMimetype, -1, targetMimetype, options);
    }

    @Override
    public boolean isTransformableSize(String sourceMimetype, long sourceSize, String targetMimetype, TransformationOptions options)
    {
        return (sourceSize < 0) ||
            super.isTransformableSize(sourceMimetype, sourceSize, targetMimetype, options) &&
            isTransformableMimetypeAndSize(sourceMimetype, sourceSize, targetMimetype, options);
    }

    private boolean isTransformableMimetypeAndSize(String sourceMimetype, long sourceSize,
            String targetMimetype, TransformationOptions options)
    {
        boolean result = false;
        for (ContentTransformer ct : this.transformers)
        {
            if (ct.isTransformableMimetype(sourceMimetype, targetMimetype, options))
            {
                if (sourceSize < 0)
                {
                    result = true;
                    break;
                }
                else
                {
                    try
                    {
                        transformerDebug.pushIsTransformableSize(this);
                        if (ct.isTransformableSize(sourceMimetype, sourceSize, targetMimetype, options))
                        {
                            result = true;
                            break;
                        }
                    }
                    finally
                    {
                        transformerDebug.popIsTransformableSize();
                    }
                }
            }
        }
        return result;
    }
    
    @SuppressWarnings("deprecation")
    public boolean isExplicitTransformation(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
    	boolean result = true;
    	for (ContentTransformer ct : this.transformers)
    	{
    		if (ct.isExplicitTransformation(sourceMimetype, targetMimetype, options) == false)
    		{
    			result = false;
    		}
    	}
    	return result;
    }


    /**
     * @see org.alfresco.repo.content.transform.AbstractContentTransformer2#transformInternal(org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ContentWriter, org.alfresco.service.cmr.repository.TransformationOptions)
     */
    @Override
    public void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            TransformationOptions options) throws Exception
    {
        final String outputMimetype = writer.getMimetype();
        final String outputFileExt = getMimetypeService().getExtension(outputMimetype);
        
        // We need to keep a reference to thrown exceptions as we're going to catch them and
        // then move on to the next transformer. In the event that they all fail, we will throw
        // the final exception.
        Exception transformationException = null;
        
        for (int i = 0; i < transformers.size(); i++)
        {
        	int oneBasedCount = i + 1;
            ContentTransformer transf = transformers.get(i);
            ContentWriter currentWriter = null;
            File tempFile = null;
            try
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Transformation attempt " + oneBasedCount + " of " + transformers.size() +  ": " + transf);
                }
                
                if (!transf.isTransformable(reader.getMimetype(), reader.getSize(), outputMimetype, options))
                {
                    throw new UnsupportedTransformationException("Unsupported transformation: " +reader.getMimetype()+" to "+outputMimetype);
                }
                
                // We can't know in advance which transformer in the sequence will work - if any.
                // Therefore we can't write into the ContentWriter stream.
                // So make a temporary file writer with the current transformer name.
                tempFile = TempFileProvider.createTempFile(
                        "FailoverTransformer_intermediate_" + transf.getClass().getSimpleName() + "_",
                        "." + outputFileExt);
                currentWriter = new FileContentWriter(tempFile);
                currentWriter.setMimetype(outputMimetype);
                currentWriter.setEncoding(writer.getEncoding());

                // attempt to transform
                transf.transform(reader, currentWriter, options);
                
                // TODO Could add a check for zero-length output and treat that as a failure
                // final long writtenSize = currentWriter.getSize();
            }
            catch (Exception are)
            {
                if (transformationException == null)
                {
                    transformationException = are;
                }
                
                if (logger.isDebugEnabled())
                {
                	logger.debug("Transformation " + oneBasedCount + " was unsuccessful.");
                	if (i != transformers.size() - 1)
                	{
                		// We don't log the last exception as we're going to throw it.
                		logger.debug("The below exception is provided for information purposes only.", are);
                	}
                }
                
                // Set a new reader to refresh the input stream.
                reader = reader.getReader();
                // and move to the next transformer
                continue;
            }
            // No need to close input or output streams
            
            // At this point the current transformation was successful i.e. it did not throw an exception.

            // Now we must copy the content from the temporary file into the ContentWriter stream.
            if (tempFile != null)
            {
                writer.putContent(tempFile);
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Transformation was successful");
            }
            return;
        }
        // At this point we have tried all transformers in the sequence without apparent success.
        if (transformationException != null)
        {
            transformerDebug.debug("          No more transformations to failover to");
            if (logger.isDebugEnabled())
            {
                logger.debug("All transformations were unsuccessful. Throwing first exception.", transformationException);
            }
            throw transformationException;
        }
    }

    /**
     * Returns the transformer properties predefined (hard coded or implied) by this transformer.
     */
    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getComments(available));
        sb.append("# ");
        sb.append(TransformerConfig.CONTENT);
        sb.append(getName());
        sb.append(TransformerConfig.FAILOVER);
        sb.append('=');
        boolean first = true;
        for (ContentTransformer transformer: transformers)
        {
            if (!first)
            {
                sb.append(TransformerConfig.PIPE);
            }
            first = false;
            sb.append(transformer != null ? getSimpleName(transformer) : TransformerConfig.ANY);
        }
        sb.append('\n');
        return sb.toString();
    }
}
