 
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 */
public class IsTransferAccessionCapabilityCondition extends AbstractCapabilityCondition
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;

        FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(nodeRef);
        if (FilePlanComponentKind.TRANSFER.equals(kind))
        {
            Boolean value = (Boolean)nodeService.getProperty(nodeRef, PROP_TRANSFER_ACCESSION_INDICATOR);
            if (value != null)
            {
                result = value.booleanValue();
            }
        }
        return result;
    }

}
