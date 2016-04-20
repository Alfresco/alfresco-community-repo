package org.alfresco.repo.search;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Indexer related exceptions
 * 
 * @author andyh
 */
public class IndexerException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 3257286911646447666L;

    public IndexerException(String message)
    {
        super(message);
    }

    public IndexerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
