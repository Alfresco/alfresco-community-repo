package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Delete Hold Action
 *
 * @author Tuna Aksoy
 * @since 2.2
 * @version 1.0
 */
public class DeleteHoldAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_DELETE_NOT_HOLD_TYPE = "rm.action.delete-not-hold-type";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (getHoldService().isHold(actionedUponNodeRef))
        {
            getHoldService().deleteHold(actionedUponNodeRef);
        }
        else
        {
            throw new AlfrescoRuntimeException(MSG_DELETE_NOT_HOLD_TYPE, new Object[]{ TYPE_HOLD.toString(), actionedUponNodeRef.toString() });
        }
    }
}
