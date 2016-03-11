package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.service.cmr.repository.NodeRef;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Delete links capability.
 *
 * @author Roy Wetherall
 */
public class DeleteLinksCapability extends DeclarativeCapability
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.Capability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public int evaluate(NodeRef nodeRef)
    {
        // no way to know ...
        return AccessDecisionVoter.ACCESS_ABSTAIN;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.AbstractCapability#evaluate(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(NodeRef source, NodeRef target)
    {
        if (getFilePlanService().isFilePlanComponent(source) &&
                getFilePlanService().isFilePlanComponent(target))
        {
            if (checkConditions(source) &&
                    checkConditions(target) &&
                    checkPermissions(source) &&
                    checkPermissions(target))
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
            return AccessDecisionVoter.ACCESS_DENIED;
        }
        else
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }
    }
}
