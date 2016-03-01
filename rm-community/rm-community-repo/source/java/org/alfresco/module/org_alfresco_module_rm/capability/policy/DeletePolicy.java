 
package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.service.cmr.repository.NodeRef;
import org.aopalliance.intercept.MethodInvocation;

public class DeletePolicy extends AbstractBasePolicy
{

    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {
        NodeRef deletee = null;
        if (cad.getParameters().get(0) > -1)
        {
            deletee = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        }
        if (deletee != null)
        {

            return getCapabilityService().getCapability("Delete").evaluate(deletee);

        }
        else
        {
            return AccessDecisionVoter.ACCESS_DENIED;
        }
    }

}