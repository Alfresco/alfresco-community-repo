 
package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import org.alfresco.module.org_alfresco_module_rm.capability.impl.CreateCapability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;

public class CreatePolicy extends AbstractBasePolicy
{
    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {
        NodeRef linkee = null;
        QName assocType = null;

        // get the destination node
        NodeRef destination = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());

        if (cad.getParameters().size() > 1)
        {
            // get the linkee when present
            linkee = getTestNode(invocation, params, cad.getParameters().get(1), cad.isParent());

            // get the assoc type
            if(cad.getParameters().size() > 2)
            {
                assocType = getType(invocation, params, cad.getParameters().get(2), cad.isParent());
            }
        }

        return ((CreateCapability) getCapabilityService().getCapability("Create")).evaluate(destination, linkee, assocType);
    }

}