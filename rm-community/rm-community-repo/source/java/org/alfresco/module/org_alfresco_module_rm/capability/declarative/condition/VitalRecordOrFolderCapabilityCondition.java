 
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

        if (recordService.isRecord(nodeRef))
        {
            // Check the record for the vital record aspect
            result = nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_VITAL_RECORD);
        }
        else if (recordFolderService.isRecordFolder(nodeRef))
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
