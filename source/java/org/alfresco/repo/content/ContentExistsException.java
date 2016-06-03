package org.alfresco.repo.content;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception produced when a request is made to write content to a location
 * already in use, either by content being written or previously written.
 *
 * @see ContentStore#getWriter(ContentContext)
 * @since 2.1
 * @author Derek Hulley
 */
public class ContentExistsException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 5154068664249490612L;

    private ContentStore contentStore;
    private String contentUrl;
    
    /**
     * @param contentStore      the originating content store
     * @param contentUrl        the offending content URL
     */
    public ContentExistsException(ContentStore contentStore, String contentUrl)
    {
        this(contentStore, contentUrl,
                "Content with the given URL already exists in the store: \n" +
                "   Store:       " + contentStore.getClass().getName() + "\n" +
                "   Content URL: " + contentUrl);
    }

    /**
     * @param contentStore      the originating content store
     * @param contentUrl        the offending content URL
     */
    public ContentExistsException(ContentStore contentStore, String contentUrl, String msg)
    {
        super(msg);
        this.contentStore = contentStore;
        this.contentUrl = contentUrl;
    }

    public ContentStore getContentStore()
    {
        return contentStore;
    }

    public String getContentUrl()
    {
        return contentUrl;
    }
}
