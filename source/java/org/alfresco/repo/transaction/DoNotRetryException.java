package org.alfresco.repo.transaction;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Marker interface for the exceptions that should not trigger retries, regardless of
 * the contained causal exceptions.
 * 
 * @author Derek Hulley
 * @since 3.4.6
 */
@AlfrescoPublicApi
public interface DoNotRetryException
{
}
