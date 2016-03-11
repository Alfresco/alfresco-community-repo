package org.alfresco.module.org_alfresco_module_rm.capability.declarative;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Capability condition.
 * 
 * @author Roy Wetherall
 * @since 2.0
 */
public interface CapabilityCondition
{
    /**
     * Get capability condition name
     * 
     * @return {@link String}   capability condition name
     */
    String getName();
    
    /**
     * Evaluates capability condition.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if evaluate success, false otherwise
     */
    boolean evaluate(NodeRef nodeRef);
}
