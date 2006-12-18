/**
 * 
 */
package org.alfresco.repo.policy.registration;

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
		// Register the class behaviour
		this.policyComponent.bindClassBehaviour(this.policyName, this.className, this.behaviour);
	}

}
