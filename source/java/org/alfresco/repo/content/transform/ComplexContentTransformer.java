package org.alfresco.repo.content.transform;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.springframework.beans.factory.InitializingBean;

/**
 * Transformer that passes a document through several nested transformations
 * in order to accomplish its goal.
 * 
 * @author Derek Hulley
 */
public class ComplexContentTransformer extends AbstractContentTransformer implements InitializingBean
{
    private List<ContentTransformer> transformers;
    private List<String> intermediateMimetypes;
    
    public ComplexContentTransformer()
    {
    }

    /**
     * The list of transformers to use.
     * <p>
     * If a single transformer is supplied, then it will still be used.
     * 
     * @param transformers list of <b>at least one</b> transformer
     */
    public void setTransformers(List<ContentTransformer> transformers)
    {
        this.transformers = transformers;
    }

    /**
     * Set the intermediate mimetypes that the transformer must take the content
     * through.  If the transformation <b>A..B..C</b> is performed in order to
     * simulate <b>A..C</b>, then <b>B</b> is the intermediate mimetype.  There
     * must always be <b>n-1</b> intermediate mimetypes, where <b>n</b> is the
     * number of {@link #setTransformers(List) transformers} taking part in the
     * transformation.
     * 
     * @param intermediateMimetypes intermediate mimetypes to transition the content
     *      through.
     */
    public void setIntermediateMimetypes(List<String> intermediateMimetypes)
    {
        this.intermediateMimetypes = intermediateMimetypes;
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
        if (intermediateMimetypes == null || intermediateMimetypes.size() != transformers.size() - 1)
        {
            throw new AlfrescoRuntimeException(
                    "There must be n-1 intermediate mimetypes, where n is the number of transformers");
        }
        if (getMimetypeService() == null)
        {
            throw new AlfrescoRuntimeException("'mimetypeService' is a required property");
        }
    }

    /**
     * @return Returns the multiple of the reliabilities of the chain of transformers
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        double reliability = 1.0;
        String currentSourceMimetype = sourceMimetype;
        
        Iterator<ContentTransformer> transformerIterator = transformers.iterator();
        Iterator<String> intermediateMimetypeIterator = intermediateMimetypes.iterator();
        while (transformerIterator.hasNext())
        {
            ContentTransformer transformer = transformerIterator.next();
            // determine the target mimetype.  This is the final target if we are on the last transformation
            String currentTargetMimetype = null;
            if (!transformerIterator.hasNext())
            {
                currentTargetMimetype = targetMimetype;
            }
            else
            {
                // use an intermediate transformation mimetype
                currentTargetMimetype = intermediateMimetypeIterator.next();
            }
            // the reliability is a multiple
            reliability *= transformer.getReliability(currentSourceMimetype, currentTargetMimetype);
            // move the source on
            currentSourceMimetype = currentTargetMimetype;
        }
        // done
        return reliability;
    }

    @Override
    public void transformInternal(
            ContentReader reader,
            ContentWriter writer,
            Map<String, Object> options) throws Exception
    {
        ContentReader currentReader = reader;
        
        Iterator<ContentTransformer> transformerIterator = transformers.iterator();
        Iterator<String> intermediateMimetypeIterator = intermediateMimetypes.iterator();
        while (transformerIterator.hasNext())
        {
            ContentTransformer transformer = transformerIterator.next();
            // determine the target mimetype.  This is the final target if we are on the last transformation
            ContentWriter currentWriter = null;
            if (!transformerIterator.hasNext())
            {
                currentWriter = writer;
            }
            else
            {
                String nextMimetype = intermediateMimetypeIterator.next();
                // make a temp file writer with the correct extension
                String sourceExt = getMimetypeService().getExtension(currentReader.getMimetype());
                String targetExt = getMimetypeService().getExtension(nextMimetype);
                File tempFile = TempFileProvider.createTempFile(
                        "ComplextTransformer_intermediate_" + sourceExt + "_",
                        "." + targetExt);
                currentWriter = new FileContentWriter(tempFile);
                currentWriter.setMimetype(nextMimetype);
            }
            // transform
            transformer.transform(currentReader, currentWriter, options);
            // move the source on
            currentReader = currentWriter.getReader();
        }
        // done
    }
}
