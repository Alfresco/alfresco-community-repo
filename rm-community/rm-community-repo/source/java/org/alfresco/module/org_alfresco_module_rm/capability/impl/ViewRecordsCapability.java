 
package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.service.cmr.repository.NodeRef;

public final class ViewRecordsCapability extends DeclarativeCapability
{
    /** capability name */
    public static final String NAME = "ViewRecords";

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(NodeRef nodeRef)
    {
        if (nodeRef != null)
        {
            if (getFilePlanService().isFilePlanComponent(nodeRef))
            {
                return checkRmRead(nodeRef);
            }
            else
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("View Records capability abstains, because node is not a file plan component. (nodeRef=" + nodeRef.toString() + ")");
                }
            }
        }

        return AccessDecisionVoter.ACCESS_ABSTAIN;
    }
}