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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Cut off disposition action
 *
 * @author Roy Wetherall
 */
public class CutOffAction extends RMDispositionActionExecuterAbstractBase
{
    /** Action name */
    public static final String NAME = "cutoff";

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordFolderLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordFolderLevelDisposition(Action action, NodeRef recordFolder)
    {
        if(checkUncutOffStatus(action, recordFolder))
        {
            // Mark the folder as cut off
            getDispositionService().cutoffDisposableItem(recordFolder);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordLevelDisposition(Action action, NodeRef record)
    {
        if(checkUncutOffStatus(action, record))
        {
            // Mark the record as cut off
            getDispositionService().cutoffDisposableItem(record);
        }
    }

    /**
     * Check if the record or folder has been uncut off. If it has and this cut off action is an
     * automated disposition action then the cut off isn't run. If it has and this is a manual
     * cut off action then the uncut off aspect is removed prior to the uncut action.
     *
     * @param action The cut off action
     * @param recordOrFolder The record or folder to be cut off
     * @return True if the record or folder can be cut off
     */
    private boolean checkUncutOffStatus(Action action, NodeRef recordOrFolder)
    {
        boolean okToCutOff = true;
        if(getNodeService().hasAspect(recordOrFolder, ASPECT_UNCUT_OFF))
        {
            if(action.getParameterValue(PARAM_NO_ERROR_CHECK) != null)
            {
                // this exception stops the cut off disposition schedule action taking place and because we're
                // running from the schedule (PARAM_NO_ERROR_CHECK is set) then the exception will not be reported
                throw new AlfrescoRuntimeException("Cannot cut off from schedule when uncut off aspect is present");
            }
            else
            {
                getNodeService().removeAspect(recordOrFolder, ASPECT_UNCUT_OFF);
            }
        }
        return okToCutOff;
    }
 }
