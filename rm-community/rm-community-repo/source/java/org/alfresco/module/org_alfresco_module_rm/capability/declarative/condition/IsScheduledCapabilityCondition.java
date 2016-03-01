 
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Indicates whether the given disposition action is scheduled next
 *
 * @author Roy Wetherall
 */
public class IsScheduledCapabilityCondition extends AbstractCapabilityCondition
{
    /** Disposition action */
    private String dispositionAction;

    /** Disposition service */
    private DispositionService dispositionService;

    /**
     * @param dispositionAction     disposition action
     */
    public void setDispositionAction(String dispositionAction)
    {
        this.dispositionAction = dispositionAction;
    }

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;

        DispositionAction nextDispositionAction = dispositionService.getNextDispositionAction(nodeRef);
        if (nextDispositionAction != null)
        {
            // Get the disposition actions name
            String actionName = nextDispositionAction.getName();
            if (actionName.equals(dispositionAction) &&
                dispositionService.isNextDispositionActionEligible(nodeRef))
            {
                result = true;
            }
        }

        return result;
    }
}
