package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Indicates whether the given disposition action 'may' be scheduled in the future
 *
 * @author Roy Wetherall
 */
public class MayBeScheduledCapabilityCondition extends AbstractCapabilityCondition
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

        DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(nodeRef);
        if (dispositionSchedule != null && checkDispositionLevel(nodeRef, dispositionSchedule))
        {
            for (DispositionActionDefinition dispositionActionDefinition : dispositionSchedule.getDispositionActionDefinitions())
            {
                if (dispositionActionDefinition.getName().equals(dispositionAction))
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Checks the disposition level
     *
     * @param nodeRef
     * @param dispositionSchedule
     * @return
     */
    private boolean checkDispositionLevel(NodeRef nodeRef, DispositionSchedule dispositionSchedule)
    {
        boolean result = false;
        boolean isRecordLevelDisposition = dispositionSchedule.isRecordLevelDisposition();
        if (recordService.isRecord(nodeRef) && isRecordLevelDisposition)
        {
            result = true;
        }
        else if (recordFolderService.isRecordFolder(nodeRef) && !isRecordLevelDisposition)

        {
            result = true;
        }
        return result;
    }
}
