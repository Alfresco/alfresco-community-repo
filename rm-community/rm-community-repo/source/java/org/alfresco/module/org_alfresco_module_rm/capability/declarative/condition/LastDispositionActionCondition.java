 
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Last disposition action condition.
 *
 * @author Roy Wetherall
 */
public class LastDispositionActionCondition extends AbstractCapabilityCondition
{
    private DispositionService dispositionService;

    private String dispositionActionName;

    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    public void setDispositionActionName(String dispositionActionName)
    {
        this.dispositionActionName = dispositionActionName;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        DispositionAction dispositionAction = dispositionService.getLastCompletedDispostionAction(nodeRef);
        if (dispositionAction != null &&
            dispositionActionName.equals(dispositionAction.getName()))
        {
            result = true;
        }
        return result;
    }
}
