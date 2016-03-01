 
package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import org.alfresco.module.org_alfresco_module_rm.capability.impl.ViewRecordsCapability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Read method security policy.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ReadPolicy extends AbstractBasePolicy
{

    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {
        NodeRef testNodeRef = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        return getCapabilityService().getCapability(ViewRecordsCapability.NAME).evaluate(testNodeRef);
    }
}