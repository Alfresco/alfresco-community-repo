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
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 */
public class VitalRecordOrFolderCapabilityCondition extends AbstractCapabilityCondition
{
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;        
        
        if (recordService.isRecord(nodeRef) == true)
        {
            // Check the record for the vital record aspect
            result = nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_VITAL_RECORD);
        }
        else if (rmService.isRecordFolder(nodeRef) == true)
        {
            // Check the folder for the vital record indicator
            Boolean value = (Boolean)nodeService.getProperty(nodeRef, RecordsManagementModel.PROP_VITAL_RECORD_INDICATOR);
            if (value != null)
            {
                result = value.booleanValue();
            }
        }    
        
        return result;
    }
}
