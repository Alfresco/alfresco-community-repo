/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.policy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.policy.Policy.Arg;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Policy Component Implementation.
 * 
 * @author David Caruana
 *
 */
public class PolicyComponentImpl implements PolicyComponent
{
    // Logger
    private static final Log logger = LogFactory.getLog(PolicyComponentImpl.class);
    
    // Policy interface annotations
    private static String ANNOTATION_NAMESPACE = "NAMESPACE";
    private static String ANNOTATION_ARG ="ARG_";

    // Dictionary Service
    private DictionaryService dictionary;
    
    // Behaviour Filter
    private BehaviourFilter behaviourFilter;
    
    // Map of registered Policies
    private Map<PolicyKey, PolicyDefinition> registeredPolicies;; 

    // Map of Class Behaviours (by policy name)
    private Map<QName, ClassBehaviourIndex<ClassBehaviourBinding>> classBehaviours = new HashMap<QName, ClassBehaviourIndex<ClassBehaviourBinding>>();
    
    // Map of Property Behaviours (by policy name)
    private Map<QName, ClassBehaviourIndex<ClassFeatureBehaviourBinding>> propertyBehaviours = new HashMap<QName, ClassBehaviourIndex<ClassFeatureBehaviourBinding>>();

    // Map of Association Behaviours (by policy name)
    private Map<QName, ClassBehaviourIndex<ClassFeatureBehaviourBinding>> associationBehaviours = new HashMap<QName, ClassBehaviourIndex<ClassFeatureBehaviourBinding>>();

    // Wild Card Feature
    private static final QName FEATURE_WILDCARD = QName.createQName(NamespaceService.DEFAULT_URI, "*"); 
    

    /**
     * Construct
     * 
     * @param dictionary  dictionary service
     * @param behaviourFilter  behaviour filter
     */
    public PolicyComponentImpl(DictionaryService dictionary)
    {
        this.dictionary = dictionary;
        this.registeredPolicies = new HashMap<PolicyKey, PolicyDefinition>();
    }
    

    /**
     * Sets the behaviour filter
     * 
     * @param filter
     */
    public void setBehaviourFilter(BehaviourFilter filter)
    {
        this.behaviourFilter = filter;
    }
    
    
    /**
     * Sets the transaction-based policy invocation handler
     * 
     * @param factory
     */
    public void setTransactionInvocationHandlerFactory(TransactionInvocationHandlerFactory factory)
    {
        PolicyFactory.setTransactionInvocationHandlerFactory(factory);
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#registerClassPolicy()
     */
    @SuppressWarnings("unchecked")
    public <P extends ClassPolicy> ClassPolicyDelegate<P> registerClassPolicy(Class<P> policy)
    {
        ParameterCheck.mandatory("Policy interface class", policy);
        PolicyDefinition definition = createPolicyDefinition(policy);
        registeredPolicies.put(new PolicyKey(definition.getType(), definition.getName()), definition);
        ClassPolicyDelegate<P> delegate = new ClassPolicyDelegate<P>(dictionary, policy, getClassBehaviourIndex(definition.getName()));
        
        if (logger.isInfoEnabled())
            logger.info("Registered class policy " + definition.getName() + " (" + definition.getPolicyInterface() + ")");
        
        return delegate;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#registerPropertyPolicy(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <P extends PropertyPolicy> PropertyPolicyDelegate<P> registerPropertyPolicy(Class<P> policy)
    {
        ParameterCheck.mandatory("Policy interface class", policy);
        PolicyDefinition definition = createPolicyDefinition(policy);
        registeredPolicies.put(new PolicyKey(definition.getType(), definition.getName()), definition);
        PropertyPolicyDelegate<P> delegate = new PropertyPolicyDelegate<P>(dictionary, policy, getPropertyBehaviourIndex(definition.getName()));
        
        if (logger.isInfoEnabled())
            logger.info("Registered property policy " + definition.getName() + " (" + definition.getPolicyInterface() + ")");
        
        return delegate;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#registerAssociationPolicy(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <P extends AssociationPolicy> AssociationPolicyDelegate<P> registerAssociationPolicy(Class<P> policy)
    {
        ParameterCheck.mandatory("Policy interface class", policy);
        PolicyDefinition definition = createPolicyDefinition(policy);
        registeredPolicies.put(new PolicyKey(definition.getType(), definition.getName()), definition);
        AssociationPolicyDelegate<P> delegate = new AssociationPolicyDelegate<P>(dictionary, policy, getAssociationBehaviourIndex(definition.getName()));
        
        if (logger.isInfoEnabled())
            logger.info("Registered association policy " + definition.getName() + " (" + definition.getPolicyInterface() + ")");
        
        return delegate;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#getRegisteredPolicies()
     */
    public Collection<PolicyDefinition> getRegisteredPolicies()
    {
        return Collections.unmodifiableCollection(registeredPolicies.values());
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#getRegisteredPolicy(org.alfresco.repo.policy.PolicyType, org.alfresco.repo.ref.QName)
     */
    public PolicyDefinition getRegisteredPolicy(PolicyType policyType, QName policy)
    {
        return registeredPolicies.get(new PolicyKey(policyType, policy));
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#isRegisteredPolicy(org.alfresco.repo.policy.PolicyType, org.alfresco.repo.ref.QName)
     */
    public boolean isRegisteredPolicy(PolicyType policyType, QName policy)
    {
        return registeredPolicies.containsKey(new PolicyKey(policyType, policy));
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#bindClassBehaviour(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName, org.alfresco.repo.policy.Behaviour)
     */
    public BehaviourDefinition<ClassBehaviourBinding> bindClassBehaviour(QName policy, QName classRef, Behaviour behaviour)
    {
        // Validate arguments
        ParameterCheck.mandatory("Policy", policy);
        ParameterCheck.mandatory("Class Reference", classRef);
        ParameterCheck.mandatory("Behaviour", behaviour);

        // Validate Binding
        ClassDefinition classDefinition = dictionary.getClass(classRef);
        if (classDefinition == null)
        {
            throw new IllegalArgumentException("Class " + classRef + " has not been defined in the data dictionary");
        }
        
        // Create behaviour definition and bind to policy
        ClassBehaviourBinding binding = new ClassBehaviourBinding(dictionary, classRef);
        BehaviourDefinition<ClassBehaviourBinding> definition = createBehaviourDefinition(PolicyType.Class, policy, binding, behaviour);
        getClassBehaviourIndex(policy).putClassBehaviour(definition);
        
        if (logger.isInfoEnabled())
            logger.info("Bound " + behaviour + " to policy " + policy + " for class " + classRef);

        return definition;
    }
    
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#bindClassBehaviour(org.alfresco.repo.ref.QName, java.lang.Object, org.alfresco.repo.policy.Behaviour)
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindClassBehaviour(QName policy, Object service, Behaviour behaviour)
    {
        // Validate arguments
        ParameterCheck.mandatory("Policy", policy);
        ParameterCheck.mandatory("Service", service);
        ParameterCheck.mandatory("Behaviour", behaviour);
        
        // Create behaviour definition and bind to policy
        ServiceBehaviourBinding binding = new ServiceBehaviourBinding(service);
        BehaviourDefinition<ServiceBehaviourBinding> definition = createBehaviourDefinition(PolicyType.Class, policy, binding, behaviour);
        getClassBehaviourIndex(policy).putServiceBehaviour(definition);
        
        if (logger.isInfoEnabled())
            logger.info("Bound " + behaviour + " to policy " + policy + " for service " + service);

        return definition;
    }    

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#bindPropertyBehaviour(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName, org.alfresco.repo.policy.Behaviour)
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindPropertyBehaviour(QName policy, QName className, QName propertyName, Behaviour behaviour)
    {
        // Validate arguments
        ParameterCheck.mandatory("Policy", policy);
        ParameterCheck.mandatory("Class Reference", className);
        ParameterCheck.mandatory("Property Reference", propertyName);
        ParameterCheck.mandatory("Behaviour", behaviour);

        // Validate Binding
        PropertyDefinition propertyDefinition = dictionary.getProperty(className, propertyName);
        if (propertyDefinition == null)
        {
            throw new IllegalArgumentException("Property " + propertyName + " of class " + className + " has not been defined in the data dictionary");
        }
        
        // Create behaviour definition and bind to policy
        ClassFeatureBehaviourBinding binding = new ClassFeatureBehaviourBinding(dictionary, className, propertyName);
        BehaviourDefinition<ClassFeatureBehaviourBinding> definition = createBehaviourDefinition(PolicyType.Property, policy, binding, behaviour);
        getPropertyBehaviourIndex(policy).putClassBehaviour(definition);
        
        if (logger.isInfoEnabled())
            logger.info("Bound " + behaviour + " to policy " + policy + " for property " + propertyName + " of class " + className);

        return definition;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#bindPropertyBehaviour(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName, org.alfresco.repo.policy.Behaviour)
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindPropertyBehaviour(QName policy, QName className, Behaviour behaviour)
    {
        // Validate arguments
        ParameterCheck.mandatory("Policy", policy);
        ParameterCheck.mandatory("Class Reference", className);
        ParameterCheck.mandatory("Behaviour", behaviour);

        // Validate Binding
        ClassDefinition classDefinition = dictionary.getClass(className);
        if (classDefinition == null)
        {
            throw new IllegalArgumentException("Class " + className + " has not been defined in the data dictionary");
        }
        
        // Create behaviour definition and bind to policy
        ClassFeatureBehaviourBinding binding = new ClassFeatureBehaviourBinding(dictionary, className, FEATURE_WILDCARD);
        BehaviourDefinition<ClassFeatureBehaviourBinding> definition = createBehaviourDefinition(PolicyType.Property, policy, binding, behaviour);
        getPropertyBehaviourIndex(policy).putClassBehaviour(definition);
        
        if (logger.isInfoEnabled())
            logger.info("Bound " + behaviour + " to policy " + policy + " for property " + FEATURE_WILDCARD + " of class " + className);

        return definition;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#bindPropertyBehaviour(org.alfresco.repo.ref.QName, java.lang.Object, org.alfresco.repo.policy.Behaviour)
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindPropertyBehaviour(QName policy, Object service, Behaviour behaviour)
    {
        // Validate arguments
        ParameterCheck.mandatory("Policy", policy);
        ParameterCheck.mandatory("Service", service);
        ParameterCheck.mandatory("Behaviour", behaviour);
        
        // Create behaviour definition and bind to policy
        ServiceBehaviourBinding binding = new ServiceBehaviourBinding(service);
        BehaviourDefinition<ServiceBehaviourBinding> definition = createBehaviourDefinition(PolicyType.Property, policy, binding, behaviour);
        getPropertyBehaviourIndex(policy).putServiceBehaviour(definition);
        
        if (logger.isInfoEnabled())
            logger.info("Bound " + behaviour + " to property policy " + policy + " for service " + service);

        return definition;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#bindAssociationBehaviour(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName, org.alfresco.repo.policy.Behaviour)
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindAssociationBehaviour(QName policy, QName className, QName assocName, Behaviour behaviour)
    {
        // Validate arguments
        ParameterCheck.mandatory("Policy", policy);
        ParameterCheck.mandatory("Class Reference", className);
        ParameterCheck.mandatory("Association Reference", assocName);
        ParameterCheck.mandatory("Behaviour", behaviour);

        // Validate Binding
        AssociationDefinition assocDefinition = dictionary.getAssociation(assocName);
        if (assocDefinition == null)
        {
            throw new IllegalArgumentException("Association " + assocName + " of class " + className + " has not been defined in the data dictionary");
        }
        
        // Create behaviour definition and bind to policy
        ClassFeatureBehaviourBinding binding = new ClassFeatureBehaviourBinding(dictionary, className, assocName);
        BehaviourDefinition<ClassFeatureBehaviourBinding> definition = createBehaviourDefinition(PolicyType.Association, policy, binding, behaviour);
        getAssociationBehaviourIndex(policy).putClassBehaviour(definition);
        
        if (logger.isInfoEnabled())
            logger.info("Bound " + behaviour + " to policy " + policy + " for association " + assocName + " of class " + className);

        return definition;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#bindAssociationBehaviour(org.alfresco.repo.ref.QName, org.alfresco.repo.ref.QName, org.alfresco.repo.policy.Behaviour)
     */
    public BehaviourDefinition<ClassFeatureBehaviourBinding> bindAssociationBehaviour(QName policy, QName className, Behaviour behaviour)
    {
        // Validate arguments
        ParameterCheck.mandatory("Policy", policy);
        ParameterCheck.mandatory("Class Reference", className);
        ParameterCheck.mandatory("Behaviour", behaviour);

        // Validate Binding
        ClassDefinition classDefinition = dictionary.getClass(className);
        if (classDefinition == null)
        {
            throw new IllegalArgumentException("Class " + className + " has not been defined in the data dictionary");
        }
        
        // Create behaviour definition and bind to policy
        ClassFeatureBehaviourBinding binding = new ClassFeatureBehaviourBinding(dictionary, className, FEATURE_WILDCARD);
        BehaviourDefinition<ClassFeatureBehaviourBinding> definition = createBehaviourDefinition(PolicyType.Association, policy, binding, behaviour);
        getAssociationBehaviourIndex(policy).putClassBehaviour(definition);
        
        if (logger.isInfoEnabled())
            logger.info("Bound " + behaviour + " to policy " + policy + " for association " + FEATURE_WILDCARD + " of class " + className);

        return definition;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.PolicyComponent#bindAssociationBehaviour(org.alfresco.repo.ref.QName, java.lang.Object, org.alfresco.repo.policy.Behaviour)
     */
    public BehaviourDefinition<ServiceBehaviourBinding> bindAssociationBehaviour(QName policy, Object service, Behaviour behaviour)
    {
        // Validate arguments
        ParameterCheck.mandatory("Policy", policy);
        ParameterCheck.mandatory("Service", service);
        ParameterCheck.mandatory("Behaviour", behaviour);
        
        // Create behaviour definition and bind to policy
        ServiceBehaviourBinding binding = new ServiceBehaviourBinding(service);
        BehaviourDefinition<ServiceBehaviourBinding> definition = createBehaviourDefinition(PolicyType.Association, policy, binding, behaviour);
        getAssociationBehaviourIndex(policy).putServiceBehaviour(definition);
        
        if (logger.isInfoEnabled())
            logger.info("Bound " + behaviour + " to association policy " + policy + " for service " + service);

        return definition;
    }
    
    
    /**
     * Gets the Class behaviour index for the specified Policy
     * 
     * @param policy  the policy
     * @return  the class behaviour index
     */
    private synchronized ClassBehaviourIndex<ClassBehaviourBinding> getClassBehaviourIndex(QName policy)
    {
        ClassBehaviourIndex<ClassBehaviourBinding> index = classBehaviours.get(policy);
        if (index == null)
        {
            index = new ClassBehaviourIndex<ClassBehaviourBinding>(behaviourFilter);
            classBehaviours.put(policy, index);
        }
        return index;
    }

    
    /**
     * Gets the Property behaviour index for the specified Policy
     * 
     * @param policy  the policy
     * @return  the property behaviour index
     */
    private synchronized ClassBehaviourIndex<ClassFeatureBehaviourBinding> getPropertyBehaviourIndex(QName policy)
    {
        ClassBehaviourIndex<ClassFeatureBehaviourBinding> index = propertyBehaviours.get(policy);
        if (index == null)
        {
            index = new ClassBehaviourIndex<ClassFeatureBehaviourBinding>(behaviourFilter);
            propertyBehaviours.put(policy, index);
        }
        return index;
    }
    

    /**
     * Gets the Association behaviour index for the specified Policy
     * 
     * @param policy  the policy
     * @return  the association behaviour index
     */
    private synchronized ClassBehaviourIndex<ClassFeatureBehaviourBinding> getAssociationBehaviourIndex(QName policy)
    {
        ClassBehaviourIndex<ClassFeatureBehaviourBinding> index = associationBehaviours.get(policy);
        if (index == null)
        {
            index = new ClassBehaviourIndex<ClassFeatureBehaviourBinding>(behaviourFilter);
            associationBehaviours.put(policy, index);
        }
        return index;
    }

    
    /**
     * Create a Behaviour Definition
     * 
     * @param <B>  the type of binding
     * @param type  policy type
     * @param policy  policy name
     * @param binding  the binding
     * @param behaviour  the behaviour
     * @return  the behaviour definition
     */
    @SuppressWarnings("unchecked")
    private <B extends BehaviourBinding> BehaviourDefinition<B> createBehaviourDefinition(PolicyType type, QName policy, B binding, Behaviour behaviour)
    {
        // Determine if policy has already been registered
        PolicyDefinition policyDefinition = getRegisteredPolicy(type, policy);
        if (policyDefinition != null)
        {
            // Policy has already been registered, force validation of behaviour now
            behaviour.getInterface(policyDefinition.getPolicyInterface());
        }
        else
        {
            if (logger.isInfoEnabled())
                logger.info("Behaviour " + behaviour + " is binding (" + binding + ") to policy " + policy + " before the policy is registered");
        }
        
        // Construct the definition
        return new BehaviourDefinitionImpl<B>(type, policy, binding, behaviour);
    }
    

    /**
     * Create a Policy Definition
     * 
     * @param policyIF  the policy interface
     * @return  the policy definition
     */
    private PolicyDefinition createPolicyDefinition(Class policyIF)
    {
        // Extract Policy Namespace
        String namespaceURI = NamespaceService.DEFAULT_URI;
        try
        {
            Field metadata = policyIF.getField(ANNOTATION_NAMESPACE);
            if (!String.class.isAssignableFrom(metadata.getType()))
            {
                throw new PolicyException("NAMESPACE metadata incorrectly specified in policy " + policyIF.getCanonicalName());
            }
            namespaceURI = (String)metadata.get(null);
        }
        catch(NoSuchFieldException e)
        {
            // Assume default namespace
        }
        catch(IllegalAccessException e)
        {
            // Shouldn't get here (interface definitions must be accessible)
        }

        // Extract Policy Name
        Method[] methods = policyIF.getMethods();
        if (methods.length != 1)
        {
            throw new PolicyException("Policy " + policyIF.getCanonicalName() + " must declare only one method");
        }
        String name = methods[0].getName();

        // Extract Policy Arguments
        Class[] paramTypes = methods[0].getParameterTypes();
        Arg[] args = new Arg[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++)
        {
            // Extract Policy Arg
            args[i] = (i == 0) ? Arg.KEY : Arg.START_VALUE; 
            try
            {
                Field argMetadata = policyIF.getField(ANNOTATION_ARG + i);
                if (!Arg.class.isAssignableFrom(argMetadata.getType()))
                {
                    throw new PolicyException("ARG_" + i + " metadata incorrectly specified in policy " + policyIF.getCanonicalName());
                }
                args[i] = (Arg)argMetadata.get(null);
                if (i == 0 && (!args[i].equals(Arg.KEY)))
                {
                    throw new PolicyException("ARG_" + i + " specified in policy " + policyIF.getCanonicalName() + " must be a key");
                }
            }
            catch(NoSuchFieldException e)
            {
                // Assume default ARG configuration
            }
            catch(IllegalAccessException e)
            {
                // Shouldn't get here (interface definitions must be accessible)
            }
        }
        
        // Create Policy Definition
        return new PolicyDefinitionImpl(QName.createQName(namespaceURI, name), policyIF, args);
    }

    
    /**
     * Policy Key (composite of policy type and name)
     * 
     * @author David Caruana
     *
     */
    private static class PolicyKey
    {
        private PolicyType type;
        private QName policy;
        
        private PolicyKey(PolicyType type, QName policy)
        {
            this.type = type;
            this.policy = policy;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            else if (obj == null || !(obj instanceof PolicyKey))
            {
                return false;
            }
            PolicyKey other = (PolicyKey)obj;
            return type.equals(other.type) && policy.equals(other.policy);
        }

        @Override
        public int hashCode()
        {
            return 37 * type.hashCode() + policy.hashCode();
        }
    }
    
    
    /**
     * Policy Definition implementation.
     * 
     * @author David Caruana
     *
     */
    /*package*/ class PolicyDefinitionImpl implements PolicyDefinition
    {
        private QName policy;
        private Class policyIF;
        private Arg[] args;

        /*package*/ PolicyDefinitionImpl(QName policy, Class policyIF, Arg[] args)
        {
            this.policy = policy;
            this.policyIF = policyIF;
            this.args = args;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.PolicyDefinition#getName()
         */
        public QName getName()
        {
            return policy;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.PolicyDefinition#getPolicyInterface()
         */
        public Class getPolicyInterface()
        {
            return policyIF;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.PolicyDefinition#getType()
         */
        public PolicyType getType()
        {
            if (ClassPolicy.class.isAssignableFrom(policyIF))
            {
                return PolicyType.Class;
            }
            else if (PropertyPolicy.class.isAssignableFrom(policyIF))
            {
                return PolicyType.Property;
            }
            else
            {
                return PolicyType.Association; 
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.PolicyDefinition#getArgument(int)
         */
        public Arg getArgument(int index)
        {
            if (index < 0 || index > args.length -1)
            {
                throw new IllegalArgumentException("Argument index " + index + " is invalid");
            }
            return args[index];
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.PolicyDefinition#getArguments()
         */
        public Arg[] getArguments()
        {
            return args;
        }
        
    }
    

    /**
     * Behaviour Definition implementation.
     * 
     * @author David Caruana
     *
     * @param <B>  the type of binding
     */
    /*package*/ class BehaviourDefinitionImpl<B extends BehaviourBinding> implements BehaviourDefinition<B>
    {
        private PolicyType type;
        private QName policy;
        private B binding;
        private Behaviour behaviour;
        
        /*package*/ BehaviourDefinitionImpl(PolicyType type, QName policy, B binding, Behaviour behaviour)
        {
            this.type = type;
            this.policy = policy;
            this.binding = binding;
            this.behaviour = behaviour;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.BehaviourDefinition#getPolicy()
         */
        public QName getPolicy()
        {
            return policy;
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.BehaviourDefinition#getPolicyDefinition()
         */
        public PolicyDefinition getPolicyDefinition()
        {
            return getRegisteredPolicy(type, policy);
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.BehaviourDefinition#getBinding()
         */
        public B getBinding()
        {
            return binding;
        }
        
        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.BehaviourDefinition#getBehaviour()
         */
        public Behaviour getBehaviour()
        {
            return behaviour;
        }
    }

}
