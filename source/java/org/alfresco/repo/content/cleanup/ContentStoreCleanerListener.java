package org.alfresco.repo.content.cleanup;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.service.cmr.repository.ContentIOException;

/**
 * A listener that can be plugged into a
 * {@link org.alfresco.repo.content.cleanup.ContentStoreCleaner cleaner} to
 * move pre-process any content that is about to be deleted from a store.
 * <p>
 * Implementations may backup the content or even perform scrubbing or obfuscation
 * tasks on the content.  In either case, this interface is called when the content
 * really will disappear i.e. there is no potential rollback of this operation.
 * 
 * @author Derek Hulley
 */
public interface ContentStoreCleanerListener
{
    /**
     * Handle the notification that a store is about to be deleted
     * 
     * @param sourceStore       the store from which the content will be deleted
     * @param contentUrl        the URL of the content to be deleted
     * 
     * @since 3.2
     */
    public void beforeDelete(ContentStore sourceStore, String contentUrl) throws ContentIOException;
}
