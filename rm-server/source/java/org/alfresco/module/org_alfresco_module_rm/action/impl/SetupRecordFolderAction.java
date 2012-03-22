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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.vital.VitalRecordDefinition;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Action to close the records folder
 * 
 * @author Roy Wetherall
 */
public class SetupRecordFolderAction extends RMActionExecuterAbstractBase
{
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (this.recordsManagementService.isRecordFolder(actionedUponNodeRef) == true)
        {
            // Inherit the vital record details
            VitalRecordDefinition vrDef = vitalRecordService.getVitalRecordDefinition(actionedUponNodeRef);
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
            if (vrDef != null)
            {
                props.put(PROP_VITAL_RECORD_INDICATOR, vrDef.isEnabled());
                props.put(PROP_REVIEW_PERIOD, vrDef.getReviewPeriod());
            }
            this.nodeService.addAspect(actionedUponNodeRef, ASPECT_VITAL_RECORD_DEFINITION, props);
                
            // Set up the disposition schedule if the dispositions are being managed at the folder level
            DispositionSchedule di = this.dispositionService.getDispositionSchedule(actionedUponNodeRef);
            if (di != null && di.isRecordLevelDisposition() == false)
            {
                // Setup the next disposition action
                updateNextDispositionAction(actionedUponNodeRef);                
            }
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }

    @Override
    protected boolean isExecutableImpl(NodeRef filePlanComponent, Map<String, Serializable> parameters, boolean throwException)
    {
        return true;
    }

}
