package org.alfresco.repo.virtual.config;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * An expression that resolves to a {@link NodeRef}.<br>
 */
public interface NodeRefExpression
{
    NodeRef resolve();
    
    NodeRef resolve(boolean createIfNotFound);
}
