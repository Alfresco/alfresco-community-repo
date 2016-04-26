package org.alfresco.service.cmr.subscriptions;

import org.alfresco.query.PagingResults;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Response object for subscription paging requests.
 * 
 * @author Florian Mueller
 * @since 4.0
 */
public interface PagingSubscriptionResults extends PagingResults<NodeRef>
{

}
