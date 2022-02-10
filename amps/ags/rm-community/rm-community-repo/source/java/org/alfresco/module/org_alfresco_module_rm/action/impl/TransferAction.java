/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.transfer.TransferService;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Transfer action
 *
 * @author Roy Wetherall
 */
public class TransferAction extends RMDispositionActionExecuterAbstractBase
{
    /** Action name */
    public static final String NAME = "transfer";

    /** Indicates whether the transfer is an accession or not */
    private boolean isAccession = false;

    /** transfer service */
    private TransferService transferService;

    /**
     * Indicates whether this transfer is an accession or not
     *
     * @param isAccession Is the transfer an accession or not
     */
    public void setIsAccession(boolean isAccession)
    {
        this.isAccession = isAccession;
    }

    /**
     * Sets the transfer service
     *
     * @param transferService transfer service
     */
    public void setTransferService(TransferService transferService)
    {
        this.transferService = transferService;
    }

    /**
     * Do not set the transfer action to auto-complete
     *
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#getSetDispositionActionComplete()
     */
    @Override
    public boolean getSetDispositionActionComplete()
    {
        return false;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordFolderLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordFolderLevelDisposition(Action action, NodeRef recordFolder)
    {
        doTransfer(action, recordFolder);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordLevelDisposition(Action action, NodeRef record)
    {
        doTransfer(action, record);
    }

    /**
     * Create the transfer node and link the disposition lifecycle node beneath it
     *
     * @param action action
     * @param dispositionLifeCycleNodeRef disposition lifecycle node
     */
    private void doTransfer(Action action, NodeRef dispositionLifeCycleNodeRef)
    {
        NodeRef transferNodeRef = transferService.transfer(dispositionLifeCycleNodeRef, isAccession);

        // Set the return value of the action
        action.setParameterValue(ActionExecuter.PARAM_RESULT, transferNodeRef);

        // Cut off the disposable item if it's not cut off already
        if (!getDispositionService().isDisposableItemCutoff(dispositionLifeCycleNodeRef))
        {
            getDispositionService().cutoffDisposableItem(dispositionLifeCycleNodeRef);
        }
    }
}
