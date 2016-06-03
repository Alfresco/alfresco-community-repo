package org.alfresco.repo.publishing.flickr.springsocial.api;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class FlickrException extends RuntimeException
{
    private static final long serialVersionUID = 7938720115597007302L;
    private String code;
    
    public FlickrException(String errorCode, String message)
    {
        super(message);
        this.code = errorCode;
    }

    public String getCode()
    {
        return code;
    }
    
}
