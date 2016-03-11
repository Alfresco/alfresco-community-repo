package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.service.cmr.repository.NodeRef;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Change or delete references capability
 *
 * @author Roy Wetherall
 */
public class ChangeOrDeleteReferencesCapability extends DeclarativeCapability
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.AbstractCapability#evaluate(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(NodeRef source, NodeRef target)
    {
        if (getFilePlanService().isFilePlanComponent(source))
        {
            if (target != null)
            {
                if (getFilePlanService().isFilePlanComponent(target) &&
                        checkConditions(source) &&
                        checkConditions(target) &&
                        checkPermissions(source) &&
                        checkPermissions(target))
                {
                    return AccessDecisionVoter.ACCESS_GRANTED;
                }
            }
            else
            {
                if (checkConditions(source) &&
                    checkPermissions(source))
                {
                    return AccessDecisionVoter.ACCESS_GRANTED;
                }
            }

            return AccessDecisionVoter.ACCESS_DENIED;
        }
        else
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }
    }
}
