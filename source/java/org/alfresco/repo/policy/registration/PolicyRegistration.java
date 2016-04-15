/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.policy.registration;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.namespace.QName;

/**
 * Bean that can be configured in spring to register a policy bahaviour
 * 
 * @author Roy Wetherall
 */
public abstract class PolicyRegistration 
{
	/** The policy componenet **/
	protected PolicyComponent policyComponent;
	
	/** The policy names **/
	protected List<QName> policyNames;	
	
	/** The class name **/
	protected QName className;
	
	/** The behaviour **/
	protected Behaviour behaviour;
	
	/**
	 * Set the policy component
	 * 
	 * @param policyComponent	the policy componenet
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) 
	{
		this.policyComponent = policyComponent;
	}
	
	/**
	 * Set the policy name
	 * 
	 * @param policyName	the policy name
	 */
	public void setPolicyName(String policyName)
	{
		this.policyNames = new ArrayList<QName>(1);
		this.policyNames.add(QName.createQName(policyName));
	}
	
	/**
	 * Set the policy names.  The behaviour will be added for each for the policies.
	 * 
	 * @param policyNames	the policy names
	 */
	public void setPolicyNames(List<String> policyNames)
	{
		this.policyNames = new ArrayList<QName>(policyNames.size());
		for (String policyName : policyNames) 
		{
			this.policyNames.add(QName.createQName(policyName));
		}
	}
	
	/**
	 * Set the class name
	 * 
	 * @param className		 the class name
	 */
	public void setClassName(String className)
	{
		this.className = QName.createQName(className);
	}
	
	/**
	 * Set the behaviour
	 * 
	 * @param behaviour	the behaviour
	 */
	public void setBehaviour(Behaviour behaviour) 
	{
		this.behaviour = behaviour;
	}
	
	/**
	 * Registers the behaviour with the policy component for the policy and type specified.  Called
	 * as the init method of the bean.
	 * 
	 * TODO supoort service registration?
	 */
	public abstract void register();
}
