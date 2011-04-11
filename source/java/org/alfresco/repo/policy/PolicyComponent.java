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
package org.alfresco.repo.policy;

import java.util.Collection;

import org.alfresco.service.namespace.QName;


/**
 * Policy Component for managing Policies and Behaviours.
 *<p>
 * This component provides the ability to:
 * <p>
 * <ul>
 *   <li>a) Register policies</li>
 *   <li>b) Bind behaviours to policies</li>
 *   <li>c) Invoke policy behaviours</li>
 * </ul>
 * <p>
 * A behaviour may be bound to a Policy before the Policy is registered.  In
 * this case, the behaviour is not validated (i.e. checked to determine if it
 * supports the policy interface) until the Policy is registered.  Otherwise,
 * the behaviour is validated at bind-time.
 * 
 * Policies may be selectively "turned off" by the Behaviour Filter.
 * 
 * 
 * 
 * @see org.alfresco.repo.policy.BehaviourFilter
 * 
 * @see org.alfresco.repo.node.NodeServicePolicies
 * 
 *
 * @author David Caruana
 *
 */
public interface PolicyComponent
{
    /**
     * Register a Class-level Policy
     * 
     * @param <P>  the policy interface  
     * @param policy  the policy interface class
     * @return  A delegate for the class-level policy (typed by the policy interface)
     */
    public <P extends ClassPolicy> ClassPolicyDelegate<P> registerClassPolicy(Class<P> policy);

    /**
     * Register a Property-level Policy
     * 
     * @param <P>  the policy interface  
     * @param policy  the policy interface class
     * @return  A delegate for the property-level policy (typed by the policy interface)
     */
    public <P extends PropertyPolicy> PropertyPolicyDelegate<P> registerPropertyPolicy(Class<P> policy); 
    
    /**
     * Register a Association-level Policy
     * 
     * @param <P>  the policy interface  
     * @param policy  the policy interface class
     * @return  A delegate for the association-level policy (typed by the policy interface)
     */
    public <P extends AssociationPolicy> AssociationPolicyDelegate<P> registerAssociationPolicy(Class<P> policy); 
    
    /**
     * Gets all registered Policies
     * 
     * @return  the collection of registered policy definitions
     */
    public Collection<PolicyDefinition> getRegisteredPolicies();

    /**
     * Gets the specified registered Policy
     * 
     * @param policyType  the policy type
     * @param policy  the policy name
     * @return  the policy definition (or null, if it has not been registered)
     */
    public PolicyDefinition<Policy> getRegisteredPolicy(PolicyType policyType, QName policy);

    /**
     * Determine if the specified policy has been registered
     * 
     * @param policyType  the policy type
     * @param policy  the fully qualified name of the policy
     * @return  true => registered, false => not yet
     */
    public boolean isRegisteredPolicy(PolicyType policyType, QName policy);

    /**
     * Bind a Class specific behaviour to a Class-level Policy.   
     * <p>
     * So when the named policy, happens on the specified aspect or type, 
     * the specified behaviour is executed.
     * <p>
     * Example of calling this method
     * <pre>
     *         this.policyComponent.bindClassBehaviour(
     *           NodeServicePolicies.BeforeUpdateNodePolicy.QNAME,
     *           ContentModel.ASPECT_LOCKABLE,
     *           new JavaBehaviour(this, "beforeUpdateNode"));
     * </pre>
     * @param policy  the fully qualified policy name
     * @param className the qualified name of a type or aspect that the policy is bound to 
     * @param behaviour  the behaviour.  What gets executed by the policy
     * @return the registered behaviour definition
     */
    public BehaviourDefinition<ClassBehaviourBinding> bindClassBehaviour(QName policy, QName className, Behaviour behaviour);

    /**
     * Bind a Service behaviour to a Class-level Policy
     * 
     * @param policy the fully qualified policy name
     * @param service the service (any object, in fact)
     * @param behaviour the behaviour.  What gets executed by the policy
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindClassBehaviour(QName policy, Object service, Behaviour behaviour);
    
    /**
     * Bind a Property specific behaviour to a Property-level Policy
     * 
     * @param policy the fully qualified policy name
     * @param className  the qualified name of the class (type or aspect) to bind against
     * @param propertyName  the name of the property to bind against
     * @param behaviour  the behaviour.  What gets executed by the policy
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindPropertyBehaviour(QName policy, QName className, QName propertyName, Behaviour behaviour);

    /**
     * Bind a Property specific behaviour to a Property-level Policy (for all properties of a Class)
     * 
     * @param policy  the fully qualified policy name
     * @param className  the name of the class (type or aspect) to bind against
     * @param behaviour  the behaviour, what gets executed by the policy
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindPropertyBehaviour(QName policy, QName className, Behaviour behaviour);

    /**
     * Bind a Service specific behaviour to a Property-level Policy
     * 
     * @param policy  the fully qualified policy name
     * @param service  the binding service
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindPropertyBehaviour(QName policy, Object service, Behaviour behaviour);

    /**
     * Bind an Association specific behaviour to an Association-level Policy
     * <p>
     * For example, before a rule folder association is created. 
     * <pre>
     *         policyComponent.bindAssociationBehaviour(
     *           NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
     *           RuleModel.ASPECT_RULES,
     *           RuleModel.ASSOC_RULE_FOLDER,
     *           new JavaBehaviour(this, "OnCreateChildAssociation"));
     * </pre>          
     * 
     * @param policy  the policy name
     * @param className  the name of the class (type or aspect) to bind against
     * @param assocName the name of the association to bind against
     * @param behaviour  the behaviour. What gets executed by the policy
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindAssociationBehaviour(QName policy, QName className, QName assocName, Behaviour behaviour);

    /**
     * Bind an Association specific behaviour to an Association-level Policy (for all associations of a Class)
     * 
     * @param policy  the policy name
     * @param className  the name of the class (type or aspect) to bind against
     * @param behaviour  the behaviour. What gets executed by the policy
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindAssociationBehaviour(QName policy, QName className, Behaviour behaviour);

    /**
     * Bind a Service specific behaviour to an Association-level Policy
     * 
     * @param policy  the policy name
     * @param service  the binding service
     * @param behaviour  the behaviour. What gets executed by the policy
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindAssociationBehaviour(QName policy, Object service, Behaviour behaviour);
    
}


