package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * UnCutoff action implementation
 *
 * @author Roy Wetherall
 */
public class UnCutoffAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_UNDO_NOT_LAST = "rm.action.undo-not-last";

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (getNodeService().hasAspect(actionedUponNodeRef, ASPECT_DISPOSITION_LIFECYCLE) &&
                getNodeService().hasAspect(actionedUponNodeRef, ASPECT_CUT_OFF))
        {
            // Get the last disposition action
            DispositionAction da = getDispositionService().getLastCompletedDispostionAction(actionedUponNodeRef);

            // Check that the last disposition action was a cutoff
            if (da == null || !da.getName().equals("cutoff"))
            {
                // Can not undo cut off since cut off was not the last thing done
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UNDO_NOT_LAST));
            }

            // Remove the cutoff aspect and add the uncutoff aspect
            getNodeService().removeAspect(actionedUponNodeRef, ASPECT_CUT_OFF);
            getNodeService().addAspect(actionedUponNodeRef, ASPECT_UNCUT_OFF, null);
            if (getRecordFolderService().isRecordFolder(actionedUponNodeRef))
            {
                List<NodeRef> records = getRecordService().getRecords(actionedUponNodeRef);
                for (NodeRef record : records)
                {
                    getNodeService().removeAspect(record, ASPECT_CUT_OFF);
                    getNodeService().addAspect(record, ASPECT_UNCUT_OFF, null);
                }
            }

            // Delete the current disposition action
            DispositionAction currentDa = getDispositionService().getNextDispositionAction(actionedUponNodeRef);
            if (currentDa != null)
            {
                getNodeService().deleteNode(currentDa.getNodeRef());
            }

            // Move the previous (cutoff) disposition back to be current
            getNodeService().moveNode(da.getNodeRef(), actionedUponNodeRef, ASSOC_NEXT_DISPOSITION_ACTION, ASSOC_NEXT_DISPOSITION_ACTION);

            // Reset the started and completed property values
            getNodeService().setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_STARTED_AT, null);
            getNodeService().setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_STARTED_BY, null);
            getNodeService().setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_COMPLETED_AT, null);
            getNodeService().setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_COMPLETED_BY, null);
        }
    }
}
