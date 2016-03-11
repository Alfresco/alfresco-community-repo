package org.alfresco.module.org_alfresco_module_rm.job.publish;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Publish executor interface.
 * 
 * @author Roy Wetherall
 */
public interface PublishExecutor
{
    /**
     * @return  publish exector name
     */
    String getName();
    
    /**
     * Publish changes to node.
     * 
     * @param nodeRef   node reference
     */
    void publish(NodeRef nodeRef);
}
