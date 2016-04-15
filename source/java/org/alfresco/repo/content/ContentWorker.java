package org.alfresco.repo.content;

import org.alfresco.api.AlfrescoPublicApi;   

/**
 * An interface instances that operate on content.  This is a marker interface
 * for specific <i>worker</i> interfaces such as metadata extractors, content transformers
 * and so forth.
 * 
 * @see org.alfresco.repo.content.selector.ContentWorkerSelector
 * @since 2.1
 * @author Derek Hulley
 */
@AlfrescoPublicApi
public interface ContentWorker
{
}
