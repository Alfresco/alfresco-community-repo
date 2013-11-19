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
        // Close the record folder
        recordFolderService.closeRecordFolder(recordFolder);

        // Mark the folder as cut off
        dispositionService.cutoffDisposableItem(recordFolder);

        // Mark all the declared children of the folder as cut off
        for (NodeRef record : recordService.getRecords(recordFolder))
        {
            dispositionService.cutoffDisposableItem(record);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordLevelDisposition(Action action, NodeRef record)
    {
        // Mark the record as cut off
        dispositionService.cutoffDisposableItem(record);
    }
 }