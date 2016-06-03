package org.alfresco.service.cmr.thumbnail;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Thumbnail service exception class
 * 
 * @author Roy Wetherall
 */
public class ThumbnailException extends AlfrescoRuntimeException 
{
	/**
	 * Serial version UID 
	 */
	private static final long serialVersionUID = 3257571685241467958L;

    public ThumbnailException(String msgId)
    {
        super(msgId);
    }

    public ThumbnailException(String msgId, Object[] msgParams)
    {
        super(msgId, msgParams);
    }

    public ThumbnailException(String msgId, Object[] msgParams, Throwable cause)
    {
        super(msgId, msgParams, cause);
    }

    public ThumbnailException(String msgId, Throwable cause)
    {
        super(msgId, cause);
    }
}
