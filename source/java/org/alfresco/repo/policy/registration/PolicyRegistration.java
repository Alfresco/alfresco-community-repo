/**
 * 
 */
package org.alfresco.repo.policy.registration;

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
	
	/** The policy name **/
	protected QName policyName;
	
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
		this.policyName = QName.createQName(policyName);
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
