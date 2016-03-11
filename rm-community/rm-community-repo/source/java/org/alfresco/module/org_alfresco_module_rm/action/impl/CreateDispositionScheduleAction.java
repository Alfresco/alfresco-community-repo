package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Create disposition schedule action
 *
 * @author Roy Wetherall
 */
public class CreateDispositionScheduleAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_NODE_NOT_RECORD_CATEGORY = "rm.action.node-not-record-category";

    /** file plan service */
    private FilePlanService filePlanService;

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (eligibleForAction(actionedUponNodeRef))
        {
            // Create the disposition schedule
            getDispositionService().createDispositionSchedule(actionedUponNodeRef, null);
        }
        else
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NODE_NOT_RECORD_CATEGORY, actionedUponNodeRef));
        }
    }

    /**
     * Helper method to check the actioned upon node reference to decide to execute the action
     * The preconditions are:
     *  - The node must exist
     *  - The node must not be a record category
     *
     * @param actionedUponNodeRef node reference
     * @return Return true if the node reference passes all the preconditions for executing the action, false otherwise
     */
    private boolean eligibleForAction(NodeRef actionedUponNodeRef)
    {
        boolean result = false;
        if (getNodeService().exists(actionedUponNodeRef) &&
                filePlanService.isRecordCategory(actionedUponNodeRef))
        {
            result = true;
        }
        return result;
    }
}
