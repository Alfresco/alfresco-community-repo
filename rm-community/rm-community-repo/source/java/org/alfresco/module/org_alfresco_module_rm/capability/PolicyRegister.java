package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.module.org_alfresco_module_rm.capability.policy.Policy;

/**
 * @author Roy Wetherall
 */
public interface PolicyRegister 
{
	void registerPolicy(Policy policy);
}
