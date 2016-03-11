package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Indicates whether a disposable item currently has a disposition date or not.
 *
 * @author Roy Wetherall
 */
public class HasDispositionDateCapabilityCondition extends AbstractCapabilityCondition
{
    /** Disposition service */
    private DispositionService dispositionService;

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

        DispositionAction dispositionAction = dispositionService.getNextDispositionAction(nodeRef);
        if (dispositionAction != null)
        {
            if (dispositionAction.getAsOfDate() != null)
            {
                result = true;
            }
        }
        else if (filePlanService.isFilePlanComponent(nodeRef) && nodeService.getProperty(nodeRef, PROP_DISPOSITION_AS_OF) != null)
        {
            result = true;
        }

        return result;
    }
}
