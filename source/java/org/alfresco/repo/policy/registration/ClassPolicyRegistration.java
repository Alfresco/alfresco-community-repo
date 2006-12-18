/**
 * 
 */
package org.alfresco.repo.policy.registration;

import org.alfresco.service.namespace.QName;

/**
 * Deal with the registration of a class policy
 * 
 * @author Roy Wetherall
 *
 */
public class ClassPolicyRegistration extends PolicyRegistration 
{
	/**
	 * @see org.alfresco.repo.policy.registration.PolicyRegistration#register()
	 */
	@Override
	public void register() 
	{
		for (QName policyName : this.policyNames) 
		{			
			// Register the class behaviour
			this.policyComponent.bindClassBehaviour(policyName, this.className, this.behaviour);
		}
	}

}
