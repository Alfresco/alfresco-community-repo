 
package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.aopalliance.intercept.MethodInvocation;

public class UpdatePropertiesPolicy extends AbstractBasePolicy
{
    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {
        NodeRef nodeRef = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        return getCapabilityService().getCapability("UpdateProperties").evaluate(nodeRef);
    }
}