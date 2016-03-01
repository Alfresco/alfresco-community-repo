 
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * Indicates whether the given node is a record category or not.
 *
 * @author Tuna Aksoy
 * @since 2.1
 */
public class IsRecordCategoryCondition extends AbstractCapabilityCondition
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        return filePlanService.isRecordCategory(nodeRef);
    }

}
