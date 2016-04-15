package org.alfresco.service.cmr.repository;

import java.text.MessageFormat;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thrown when a transformation request cannot be honoured due to
 * no transformers being present for the requested transformation.  
 * 
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public class NoTransformerException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3689067335554183222L;

    private static final MessageFormat MSG =
        new MessageFormat("No transformation exists between mimetypes {0} and {1}");

    private String sourceMimetype;
    private String targetMimetype;
    
    /**
     * @param sourceMimetype the attempted source mimetype
     * @param targetMimetype the attempted target mimetype
     */
    public NoTransformerException(String sourceMimetype, String targetMimetype)
    {
        super(MSG.format(new Object[] {sourceMimetype, targetMimetype}));
        this.sourceMimetype = sourceMimetype;
        this.targetMimetype = targetMimetype;
    }

    public String getSourceMimetype()
    {
        return sourceMimetype;
    }
    
    public String getTargetMimetype()
    {
        return targetMimetype;
    }
}
