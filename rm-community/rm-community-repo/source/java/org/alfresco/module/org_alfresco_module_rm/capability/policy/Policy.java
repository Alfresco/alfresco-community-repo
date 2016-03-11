package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Policy interface 
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface Policy
{
    /**
     * @return   policy name
     */
    String getName();
    
    /**
     * Evaluate the policy
     * 
     * @param invocation
     * @param params
     * @param cad
     * @return
     */
    @SuppressWarnings("rawtypes")
	int evaluate(
            MethodInvocation invocation, 
            Class[] params, 
            ConfigAttributeDefinition cad);
}
