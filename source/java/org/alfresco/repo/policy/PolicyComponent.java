/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.policy;

import java.util.Collection;

import org.alfresco.service.namespace.QName;


/**
 * Policy Component for managing Policies and Behaviours.
 *
 * This component provides the ability to:
 * 
 * a) Register policies
 * b) Bind behaviours to policies
 * c) Invoke policy behaviours
 * 
 * A behaviour may be bound to a Policy before the Policy is registered.  In
 * this case, the behaviour is not validated (i.e. checked to determine if it
 * supports the policy interface) until the Policy is registered.  Otherwise,
 * the behaviour is validated at bind-time.
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
     * @param policy  the policy name
     * @return  true => registered, false => not yet
     */
    public boolean isRegisteredPolicy(PolicyType policyType, QName policy);

    /**
     * Bind a Class specific behaviour to a Class-level Policy
     * 
     * @param policy  the policy name
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassBehaviourBinding> bindClassBehaviour(QName policy, QName classRef, Behaviour behaviour);

    /**
     * Bind a Service behaviour to a Class-level Policy
     * 
     * @param policy  the policy name
     * @param service  the service (any object, in fact)
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindClassBehaviour(QName policy, Object service, Behaviour behaviour);
    
    /**
     * Bind a Property specific behaviour to a Property-level Policy
     * 
     * @param policy  the policy name
     * @param className  the class to bind against
     * @param propertyName  the property to bind against
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindPropertyBehaviour(QName policy, QName className, QName propertyName, Behaviour behaviour);

    /**
     * Bind a Property specific behaviour to a Property-level Policy (for all properties of a Class)
     * 
     * @param policy  the policy name
     * @param className  the class to bind against
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindPropertyBehaviour(QName policy, QName className, Behaviour behaviour);

    /**
     * Bind a Service specific behaviour to a Property-level Policy
     * 
     * @param policy  the policy name
     * @param service  the binding service
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindPropertyBehaviour(QName policy, Object service, Behaviour behaviour);

    /**
     * Bind an Association specific behaviour to an Association-level Policy
     * 
     * @param policy  the policy name
     * @param className  the class to bind against
     * @param assocRef  the association to bind against
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindAssociationBehaviour(QName policy, QName className, QName assocName, Behaviour behaviour);

    /**
     * Bind an Association specific behaviour to an Association-level Policy (for all associations of a Class)
     * 
     * @param policy  the policy name
     * @param className  the class to bind against
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindAssociationBehaviour(QName policy, QName className, Behaviour behaviour);

    /**
     * Bind a Service specific behaviour to an Association-level Policy
     * 
     * @param policy  the policy name
     * @param service  the binding service
     * @param behaviour  the behaviour
     * @return  the registered behaviour definition
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindAssociationBehaviour(QName policy, Object service, Behaviour behaviour);
    
}


