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
package org.alfresco.repo.policy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;


/**
 * Delegate for a Class Feature-level (Property and Association) Policies.  Provides 
 * access to Policy Interface implementations which invoke the appropriate bound behaviours.
 *  
 * @author David Caruana
 *
 * @param <P>  the policy interface
 */
@AlfrescoPublicApi
public class PropertyPolicyDelegate<P extends PropertyPolicy>
{
    private DictionaryService dictionary;
    private CachedPolicyFactory<ClassFeatureBehaviourBinding, P> factory;


    /**
     * Construct.
     * 
     * @param dictionary  the dictionary service
     * @param policyClass  the policy interface class
     * @param index  the behaviour index to query against
     */
    @SuppressWarnings("unchecked")
    /*package*/ PropertyPolicyDelegate(DictionaryService dictionary, Class<P> policyClass, BehaviourIndex<ClassFeatureBehaviourBinding> index, long tryLockTimeout)
    {
        // Get list of all pre-registered behaviours for the policy and
        // ensure they are valid.
        Collection<BehaviourDefinition> definitions = index.getAll();
        for (BehaviourDefinition definition : definitions)
        {
            definition.getBehaviour().getInterface(policyClass);
        }

        // Rely on cached implementation of policy factory
        // Note: Could also use PolicyFactory (without caching)
        this.factory = new CachedPolicyFactory<ClassFeatureBehaviourBinding, P>(policyClass, index);
        this.factory.setTryLockTimeout(tryLockTimeout);
        this.dictionary = dictionary;
    }
    
    /**
     * Ensures the validity of the given property type
     * 
     * @param propertyQName QName
     * @throws IllegalArgumentException
     */
    private void checkPropertyType(QName propertyQName) throws IllegalArgumentException
    {
        PropertyDefinition propertyDef = dictionary.getProperty(propertyQName);
        if (propertyDef == null)
        {
            throw new IllegalArgumentException("Property " + propertyQName + " has not been defined in the data dictionary");
        }
    }


    /**
     * Gets the Policy implementation for the specified Class and Propery
     * 
     * When multiple behaviours are bound to the policy for the class feature, an
     * aggregate policy implementation is returned which invokes each policy
     * in turn.
     * 
     * @param classQName  the class qualified name
     * @param propertyQName  the property qualified name
     * @return  the policy
     */
    public P get(QName classQName, QName propertyQName)
    {
        return get(null, classQName, propertyQName);
    }

    /**
     * Gets the Policy implementation for the specified Class and Propery
     * 
     * When multiple behaviours are bound to the policy for the class feature, an
     * aggregate policy implementation is returned which invokes each policy
     * in turn.
     * 
     * @param nodeRef  the node reference
     * @param classQName  the class qualified name
     * @param propertyQName  the property qualified name
     * @return  the policy
     */
    public P get(NodeRef nodeRef, QName classQName, QName propertyQName)
    {
        checkPropertyType(propertyQName);
        return factory.create(new ClassFeatureBehaviourBinding(dictionary, nodeRef, classQName, propertyQName));
    }
    
    /**
     * Gets the collection of Policy implementations for the specified Class and Property
     * 
     * @param classQName  the class qualified name
     * @param propertyQName  the property qualified name
     * @return  the collection of policies
     */
    public Collection<P> getList(QName classQName, QName propertyQName)
    {
        return getList(null, classQName, propertyQName);
    }

    /**
     * Gets the collection of Policy implementations for the specified Class and Property
     * 
     * @param nodeRef  the node reference 
     * @param classQName  the class qualified name
     * @param propertyQName  the property qualified name
     * @return  the collection of policies
     */
    public Collection<P> getList(NodeRef nodeRef, QName classQName, QName propertyQName)
    {
        checkPropertyType(propertyQName);
        return factory.createList(new ClassFeatureBehaviourBinding(dictionary, nodeRef, classQName, propertyQName));
    }

    /**
     * Gets a <tt>Policy</tt> for all the given Class and Property
     * 
     * @param classQNames the class qualified names
     * @param propertyQName the property qualified name
     * @return Return the policy
     */
    public P get(Set<QName> classQNames, QName propertyQName)
    {
        return get(null, classQNames, propertyQName);
    }
    
    /**
     * Gets a <tt>Policy</tt> for all the given Class and Property
     * 
     * @param nodeRef  the node reference 
     * @param classQNames the class qualified names
     * @param propertyQName the property qualified name
     * @return Return the policy
     */
    public P get(NodeRef nodeRef, Set<QName> classQNames, QName propertyQName)
    {
        checkPropertyType(propertyQName);
        return factory.toPolicy(getList(nodeRef, classQNames, propertyQName));
    }
    
    /**
     * Gets the <tt>Policy</tt> instances for all the given Classes and Properties
     * 
     * @param classQNames the class qualified names
     * @param propertyQName the property qualified name
     * @return Return the policies
     */
    public Collection<P> getList(Set<QName> classQNames, QName propertyQName)
    {
        return getList(null, classQNames, propertyQName);
    }

    /**
     * Gets the <tt>Policy</tt> instances for all the given Classes and Properties
     * 
     * @param nodeRef  the node reference 
     * @param classQNames the class qualified names
     * @param propertyQName the property qualified name
     * @return Return the policies
     */
    public Collection<P> getList(NodeRef nodeRef, Set<QName> classQNames, QName propertyQName)
    {
        checkPropertyType(propertyQName);
        Collection<P> policies = new HashSet<P>();
        for (QName classQName : classQNames)
        {
            P policy = factory.create(new ClassFeatureBehaviourBinding(dictionary, nodeRef, classQName, propertyQName));
            if (policy instanceof PolicyList)
            {
                policies.addAll(((PolicyList<P>)policy).getPolicies());
            }
            else
            {
                policies.add(policy);
            }
        }
        return policies;
    }

}
