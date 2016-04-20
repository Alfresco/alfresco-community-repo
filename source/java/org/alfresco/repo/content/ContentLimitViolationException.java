package org.alfresco.repo.content;

import org.alfresco.service.cmr.repository.ContentIOException;

/**
 * This exception represents a violation of the defined content limit.
 * 
 * @author Neil Mc Erlean
 * @since Thor
 */
public class ContentLimitViolationException extends ContentIOException
{
    private static final long serialVersionUID = -640491905977728606L;

    public ContentLimitViolationException(String msg)
    {
        super(msg);
    }
    
    public ContentLimitViolationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
