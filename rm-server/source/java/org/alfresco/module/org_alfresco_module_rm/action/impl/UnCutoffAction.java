/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
        if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_DISPOSITION_LIFECYCLE) == true &&
            nodeService.hasAspect(actionedUponNodeRef, ASPECT_CUT_OFF) == true)
        {
            // Get the last disposition action
            DispositionAction da = dispositionService.getLastCompletedDispostionAction(actionedUponNodeRef);

            // Check that the last disposition action was a cutoff
            if (da == null || da.getName().equals("cutoff") == false)
            {
                // Can not undo cut off since cut off was not the last thing done
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UNDO_NOT_LAST));
            }

            // Remove the cutoff aspect
            nodeService.removeAspect(actionedUponNodeRef, ASPECT_CUT_OFF);
            if (recordsManagementService.isRecordFolder(actionedUponNodeRef) == true)
            {
                List<NodeRef> records = this.recordsManagementService.getRecords(actionedUponNodeRef);
                for (NodeRef record : records)
                {
                    nodeService.removeAspect(record, ASPECT_CUT_OFF);
                }
            }

            // Delete the current disposition action
            DispositionAction currentDa = dispositionService.getNextDispositionAction(actionedUponNodeRef);
            if (currentDa != null)
            {
                nodeService.deleteNode(currentDa.getNodeRef());
            }

            // Move the previous (cutoff) disposition back to be current
            nodeService.moveNode(da.getNodeRef(), actionedUponNodeRef, ASSOC_NEXT_DISPOSITION_ACTION, ASSOC_NEXT_DISPOSITION_ACTION);

            // Reset the started and completed property values
            nodeService.setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_STARTED_AT, null);
            nodeService.setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_STARTED_BY, null);
            nodeService.setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_COMPLETED_AT, null);
            nodeService.setProperty(da.getNodeRef(), PROP_DISPOSITION_ACTION_COMPLETED_BY, null);
        }
    }
}