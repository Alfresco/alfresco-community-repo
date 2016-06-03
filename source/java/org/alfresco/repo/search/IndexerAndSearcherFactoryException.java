package org.alfresco.repo.search;

/**
 * Factory related exception
 * 
 * @author andyh
 * 
 */
public class IndexerAndSearcherFactoryException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 3257850969667679025L;

    public IndexerAndSearcherFactoryException()
    {
        super();
    }

    public IndexerAndSearcherFactoryException(String message)
    {
        super(message);
    }

    public IndexerAndSearcherFactoryException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IndexerAndSearcherFactoryException(Throwable cause)
    {
        super(cause);
    }

}
