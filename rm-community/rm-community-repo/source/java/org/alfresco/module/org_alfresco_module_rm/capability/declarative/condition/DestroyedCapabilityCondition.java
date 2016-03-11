package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Destroyed capability condition.
 * 
 * @author Roy Wetherall
 */
public class DestroyedCapabilityCondition extends AbstractCapabilityCondition
{
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ASPECT_GHOSTED);
    }
}
