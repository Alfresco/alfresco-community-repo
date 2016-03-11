package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.capability.impl.UpdateCapability;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;

public class UpdatePolicy extends AbstractBasePolicy
{

    @SuppressWarnings("rawtypes")
	public int evaluate(
            MethodInvocation invocation,
            Class[] params,
            ConfigAttributeDefinition cad)
    {
        NodeRef updatee = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        QName aspectQName = null;
        if (cad.getParameters().size() > 1 && cad.getParameters().get(1) > -1)
        {
            aspectQName = getQName(invocation, params, cad.getParameters().get(1));
        }
        Map<QName, Serializable> properties = null;
        if (cad.getParameters().size() > 2 && cad.getParameters().get(2) > -1)
        {
            properties = getProperties(invocation, params, cad.getParameters().get(2));
        }

        UpdateCapability updateCapability = (UpdateCapability) getCapabilityService().getCapability("Update");
        return updateCapability.evaluate(updatee, aspectQName, properties);
    }

}
