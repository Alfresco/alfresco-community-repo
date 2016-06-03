package org.alfresco.repo.content;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Exception produced when a content URL is not supported by a particular
 * {@link ContentStore} implementation.
 *
 * @see ContentStore#getWriter(ContentContext)
 * @since 2.1
 * @author Derek Hulley
 */
public class UnsupportedContentUrlException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = 1349903839801739376L;

    private ContentStore contentStore;
    private String contentUrl;
    
    /**
     * @param contentStore      the originating content store
     * @param contentUrl        the offending content URL
     */
    public UnsupportedContentUrlException(ContentStore contentStore, String contentUrl)
    {
        this(contentStore, contentUrl,
                "The content URL is not supported by the content store: \n" +
                "   Store:       " + contentStore.getClass().getName() + "\n" +
                "   Content URL: " + contentUrl);
    }

    /**
     * @param contentStore      the originating content store
     * @param contentUrl        the offending content URL
     */
    public UnsupportedContentUrlException(ContentStore contentStore, String contentUrl, String msg)
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
