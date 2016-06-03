package org.alfresco.repo.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A content service that determines at runtime the store that the
 * content associated with a node should be routed to.
 * 
 * @deprecated  Replaced by {@link ContentServiceImpl}
 * @author Derek Hulley
 */
public class RoutingContentService extends ContentServiceImpl
{
    private static Log logger = LogFactory.getLog(RoutingContentService.class);
    
    public RoutingContentService()
    {
        logger.warn("Class 'RoutingContentService' has been deprecated and replaced by 'ContentServiceImpl'.");
    }
}