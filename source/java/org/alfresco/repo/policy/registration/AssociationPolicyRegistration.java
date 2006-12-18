/**
 * 
 */
package org.alfresco.repo.policy.registration;

import org.alfresco.service.namespace.QName;

/**
 * Deals with the registration of an association policy
 * 
 * @author Roy Wetherall
 *
 */
public class AssociationPolicyRegistration extends PolicyRegistration 
{
	/** The association type **/
	private QName associationType;
	
	/**
	 * Set the association type
	 * 
	 * @param associationType	the association type
	 */
	public void setAssociationType(String associationType) 
	{
		this.associationType = QName.createQName(associationType);
	}
	
	/**
	 * @see org.alfresco.repo.policy.registration.PolicyRegistration#register()
	 */
	@Override
	public void register() 
	{
		for (QName policyName : this.policyNames) 
		{
			// Register the association behaviour
			if (this.associationType == null)
			{		
				this.policyComponent.bindAssociationBehaviour(policyName, this.className, this.behaviour);
			}
			else
			{
				this.policyComponent.bindAssociationBehaviour(policyName, this.className, this.associationType, this.behaviour);
			}
		}
	}

}
