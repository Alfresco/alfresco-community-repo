/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * UnCutoff action implementation
 *
 * @author Roy Wetherall
 */
public class UnCutoffAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_UNDO_NOT_LAST = "rm.action.undo-not-last";

    /** Transaction resource key used to track nodes whose cut off is being undone in the current transaction */
    private static final String KEY_UNDO_CUT_OFF_NODE_REFS = "UnCutoffAction.undoCutOffNodeRefs";

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
            markUndoCutOffInProgress(actionedUponNodeRef);
            getNodeService().removeAspect(actionedUponNodeRef, ASPECT_CUT_OFF);
            getNodeService().addAspect(actionedUponNodeRef, ASPECT_UNCUT_OFF, null);
            if (getRecordFolderService().isRecordFolder(actionedUponNodeRef))
            {
                List<NodeRef> records = getRecordService().getRecords(actionedUponNodeRef);
                for (NodeRef record : records)
                {
                    markUndoCutOffInProgress(record);
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

    /**
     * Marks the given node as having its cut off undone in the current transaction, so that {@link #isUndoCutOffInProgress(NodeRef)} can identify the resulting property change as legitimate.
     *
     * @param nodeRef
     *            the node whose cut off is being undone
     */
    @SuppressWarnings("unchecked")
    private static void markUndoCutOffInProgress(NodeRef nodeRef)
    {
        Set<NodeRef> nodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_UNDO_CUT_OFF_NODE_REFS);
        if (nodeRefs == null)
        {
            nodeRefs = new HashSet<>();
            AlfrescoTransactionSupport.bindResource(KEY_UNDO_CUT_OFF_NODE_REFS, nodeRefs);
        }
        nodeRefs.add(nodeRef);
    }

    /**
     * Indicates whether the given node's cut off is currently being undone by this action, in the current transaction. Unlike checking for the presence of the uncut off aspect, this is only true for the exact operation that clears the cut off date, and not for any node that has been undone in the past.
     *
     * @param nodeRef
     *            the node to check
     *
     * @return true if the node's cut off is being undone in the current transaction, false otherwise
     */
    @SuppressWarnings("unchecked")
    public static boolean isUndoCutOffInProgress(NodeRef nodeRef)
    {
        Set<NodeRef> nodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_UNDO_CUT_OFF_NODE_REFS);
        return nodeRefs != null && nodeRefs.contains(nodeRef);
    }
}
