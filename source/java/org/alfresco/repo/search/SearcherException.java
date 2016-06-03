package org.alfresco.repo.search;

/**
 * Searcher related exceptions
 * 
 * @author andyh
 * 
 */
public class SearcherException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 3905522713513899318L;

    public SearcherException()
    {
        super();
    }

    public SearcherException(String message)
    {
        super(message);
    }

    public SearcherException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SearcherException(Throwable cause)
    {
        super(cause);
    }

}
