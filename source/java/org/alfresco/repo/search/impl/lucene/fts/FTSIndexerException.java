package org.alfresco.repo.search.impl.lucene.fts;

/**
 * FTS indexer exception
 * 
 * @author andyh
 *
 */
public class FTSIndexerException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 3258134635127912754L;

    /**
     * 
     */
    public FTSIndexerException()
    {
        super();
    }

    /**
     * @param message String
     */
    public FTSIndexerException(String message)
    {
        super(message);
    }

    /**
     * @param message String
     * @param cause Throwable
     */
    public FTSIndexerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param cause Throwable
     */
    public FTSIndexerException(Throwable cause)
    {
        super(cause);
    }

}
