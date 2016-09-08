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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

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
        // Close folder
        Boolean isClosed = (Boolean)this.nodeService.getProperty(recordFolder, PROP_IS_CLOSED);
        if (Boolean.FALSE.equals(isClosed) == true)
        {
            this.nodeService.setProperty(recordFolder, PROP_IS_CLOSED, true);
        }
        
        // Mark the folder as cut off
        doCutOff(recordFolder);
        
        // Mark all the declared children of the folder as cut off
        List<NodeRef> records = this.recordsManagementService.getRecords(recordFolder);
        for (NodeRef record : records)
        {
            doCutOff(record);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordLevelDisposition(Action action, NodeRef record)
    {
        // Mark the record as cut off
        doCutOff(record);       
    }
    
    /**
     * Marks the record or record folder as cut off, calculating the cut off date.
     * 
     * @param nodeRef   node reference
     */
    private void doCutOff(NodeRef nodeRef)
    {
        if (this.nodeService.hasAspect(nodeRef, ASPECT_CUT_OFF) == false)
        {
            // Apply the cut off aspect and set cut off date
            Map<QName, Serializable> cutOffProps = new HashMap<QName, Serializable>(1);
            cutOffProps.put(PROP_CUT_OFF_DATE, new Date());
            this.nodeService.addAspect(nodeRef, ASPECT_CUT_OFF, cutOffProps);
        }
    }
 }