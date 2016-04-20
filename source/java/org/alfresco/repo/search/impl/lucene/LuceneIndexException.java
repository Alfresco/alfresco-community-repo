package org.alfresco.repo.search.impl.lucene;

import org.alfresco.repo.search.IndexerException;

/**
 * Exceptions relating to indexing within the lucene implementation
 * 
 * @author andyh
 * 
 */
public class LuceneIndexException extends IndexerException
{

    /**
     * 
     */
    private static final long serialVersionUID = 3688505480817422645L;

    public LuceneIndexException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public LuceneIndexException(String message)
    {
        super(message);
    }

}
