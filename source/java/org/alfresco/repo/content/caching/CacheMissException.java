package org.alfresco.repo.content.caching;

/**
 * CacheMissException will be thrown if an attempt is made to read
 * content from the ContentCache when it is not in the cache.
 * 
 * @author Matt Ward
 */
public class CacheMissException extends RuntimeException
{
    private static final long serialVersionUID = -410818899455752655L; 

    /**
     * @param contentUrl URL of content that was attempted to be retrieved.
     */
    public CacheMissException(String contentUrl)
    {
        super("Content not found in cache [URL=" + contentUrl + "]");
    }
}
