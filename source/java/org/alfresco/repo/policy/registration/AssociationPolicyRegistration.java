/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
