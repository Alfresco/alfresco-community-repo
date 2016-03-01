 
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Roy Wetherall
 */
public class DeclaredCapabilityCondition extends AbstractCapabilityCondition
{
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        if (FilePlanComponentKind.RECORD.equals(filePlanService.getFilePlanComponentKind(nodeRef)))
        {
            result = recordService.isDeclared(nodeRef);
        }
        return result;
    }
}
