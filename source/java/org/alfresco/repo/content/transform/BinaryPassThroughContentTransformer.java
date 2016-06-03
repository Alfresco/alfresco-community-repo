package org.alfresco.repo.content.transform;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Allows direct streaming from source to target when the respective mimetypes
 * are identical, except where the mimetype is text.
 * <p>
 * Text has to be transformed based on the encoding even if the mimetypes don't
 * reflect it. 
 * 
 * @see org.alfresco.repo.content.transform.StringExtractingContentTransformer
 * 
 * @author Derek Hulley
 */
public class BinaryPassThroughContentTransformer extends AbstractContentTransformer2
{
    @SuppressWarnings("unused")
    private static final Log logger = LogFactory.getLog(BinaryPassThroughContentTransformer.class);

    @Override
    protected void transformInternal(ContentReader reader,
            ContentWriter writer, TransformationOptions options)
            throws Exception
    {
        // just stream it
        writer.putContent(reader.getContentInputStream());
        
    }

    @Override
    public boolean isTransformableMimetype(String sourceMimetype,
            String targetMimetype, TransformationOptions options)
    {
        if (sourceMimetype.startsWith(StringExtractingContentTransformer.PREFIX_TEXT))
        {
            // we can only stream binary content through
            return false;
        }
        else if (!sourceMimetype.equals(targetMimetype))
        {
            // no transformation is possible so formats must be exact
            return false;
        }
        else
        {
            if (options == null || TransformationOptions.class.equals(options.getClass()) == true)
            {
                // formats are the same and are not text
                return true;
            }
            else
            {
                // If it has meaningful options then we assume there is another transformer better equiped
                // to deal with it
                return false;
            }
        }
    }
    
    @Override
    public String getComments(boolean available)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getComments(available));
        sb.append("# Only supports streaming to the same type but excludes txt\n");
        return sb.toString();
    }
}
