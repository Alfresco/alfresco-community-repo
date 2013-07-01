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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * A Policy Factory is responsible for creating Policy implementations.
 * 
 * @author David Caruana
 *
 * @param <B>  the type of binding
 * @param <P>  the policy interface
 */
/*package*/ class PolicyFactory<B extends BehaviourBinding, P extends Policy>
{
    // Behaviour Index to query
    private BehaviourIndex<B> index;
    
    // The policy interface class
    private Class<P> policyClass;

    // NOOP Invocation Handler
    private static InvocationHandler NOOPHandler = new NOOPHandler();
    
    // Transaction Invocation Handler Factory
    private static TransactionInvocationHandlerFactory transactionHandlerFactory = null;
    
    // Tenant Service
    private static TenantService tenantService = null;
    
    
    /**
     * Construct.
     * 
     * @param policyClass  the policy class
     * @param index  the behaviour index to query
     */
    /*package*/ PolicyFactory(Class<P> policyClass, BehaviourIndex<B> index)
    {
        this.policyClass = policyClass;
        this.index = index;
    }
    

    /**
     * Sets the Transaction Invocation Handler
     * 
     * @param handlerFactory
     */
    protected static void setTransactionInvocationHandlerFactory(TransactionInvocationHandlerFactory factory)
    {
        transactionHandlerFactory = factory;
    }
    
    /**
     * Sets the Tenant Service
     * 
     * @param service
     */
    protected static void setTenantService(TenantService service)
    {
        tenantService = service;
    }

    
    /**
     * Gets the Policy class created by this factory
     * 
     * @return  the policy class
     */
    protected Class<P> getPolicyClass()
    {
        return policyClass;
    }
    

    /**
     * Construct a Policy implementation for the specified binding
     * 
     * @param binding  the binding
     * @return  the policy implementation
     */
    public P create(B binding)
    {
        Collection<P> policyInterfaces = createList(binding);
        return toPolicy(policyInterfaces);
    }
    

    /**
     * Construct a collection of Policy implementations for the specified binding
     * 
     * @param binding  the binding
     * @return  the collection of policy implementations
     */
    @SuppressWarnings("unchecked")
    public Collection<P> createList(B binding)
    {
        Collection<BehaviourDefinition> behaviourDefs = index.find(binding);
        List<P> policyInterfaces = new ArrayList<P>(behaviourDefs.size());
        for (BehaviourDefinition behaviourDef : behaviourDefs)
        {
            Behaviour behaviour = behaviourDef.getBehaviour();
            P policyIF = behaviour.getInterface(policyClass);
            if (!(behaviour.getNotificationFrequency().equals(NotificationFrequency.EVERY_EVENT)))
            {
                // wrap behaviour in transaction proxy which deals with delaying invocation until necessary
                if (transactionHandlerFactory == null)
                {
                    throw new PolicyException("Transaction-level policies not supported as transaction support for the Policy Component has not been initialised.");
                }
                InvocationHandler trxHandler = transactionHandlerFactory.createHandler(behaviour, behaviourDef.getPolicyDefinition(), policyIF);
                policyIF = (P)Proxy.newProxyInstance(policyClass.getClassLoader(), new Class[]{policyClass}, trxHandler);
            }
            policyInterfaces.add(policyIF);
        }
        
        return policyInterfaces;
    }
    
    
    /**
     * Construct a single aggregate policy implementation for the specified 
     * collection of policy implementations.
     * 
     * @param policyList  the policy implementations to aggregate
     * @return  the aggregate policy implementation
     */
    @SuppressWarnings("unchecked")
    public P toPolicy(Collection<P> policyList)
    {
        if (policyList.size() == 1)
        {
        	P policy = (policyList.iterator()).next();
        	return (P)Proxy.newProxyInstance(policyClass.getClassLoader(), 
   					new Class[]{policyClass}, new SingleHandler<P>(policy));
        }
        else if (policyList.size() == 0)
        {
            return (P)Proxy.newProxyInstance(policyClass.getClassLoader(), 
					new Class[]{policyClass}, NOOPHandler);
        }
        else
        {
            return (P)Proxy.newProxyInstance(policyClass.getClassLoader(), 
					new Class[]{policyClass, PolicyList.class}, new MultiHandler<P>(policyList));
        }
    }
    

    /**
     * NOOP Invocation Handler.
     * 
     * @author David Caruana
     *
     */
    private static class NOOPHandler implements InvocationHandler
    {
        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (method.getName().equals("toString"))
            {
                return toString();
            }
            else if (method.getName().equals("hashCode"))
            {
                return hashCode();
            }
            else if (method.getName().equals("equals"))
            {
                return equals(args[0]);
            }
            return null;
        }
    }
    
    /**
     * @author mrogers
     *
     * @param <P>
     */
    private static class SingleHandler<P extends Policy> implements InvocationHandler
    {
        private P policyInterface;
        
        /**
         * Construct
         * 
         * @param policyInterfaces  the collection of policy implementations
         */
        public SingleHandler(P policyInterface)
        {
            this.policyInterface = policyInterface;
        }
        
        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if ((tenantService != null) && (tenantService.isEnabled()) && (args != null))
            {
                // Convert each of the arguments to the spoofed (no tenant prefix) reference
            	convertMTArgs(args);
            }
            
            // Handle PolicyList level methods
            if (method.getDeclaringClass().equals(PolicyList.class))
            {
                return method.invoke(this, args);
            }
            
            // Handle Object level methods
            if (method.getName().equals("toString"))
            {
                return toString() + ": wrapped " + 1 + " policy";
            }
            else if (method.getName().equals("hashCode"))
            {
                return hashCode();
            }
            else if (method.getName().equals("equals"))
            {
                return equals(args[0]);
            }

            // Invoke each wrapped policy in turn
            try
            {
                Object result = null;
                result = method.invoke(policyInterface, args);
                return result;
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }
    }
    

    /**
     * Multi-policy Invocation Handler.
     * 
     * @author David Caruana
     *
     * @param <P>  policy interface
     */
    private static class MultiHandler<P extends Policy> implements InvocationHandler, PolicyList
    {
        private Collection<P> policyInterfaces;
       
        /**
         * Construct
         * 
         * @param policyInterfaces  the collection of policy implementations
         */
        public MultiHandler(Collection<P> policyInterfaces)
        {
            this.policyInterfaces = Collections.unmodifiableCollection(policyInterfaces);
        }
        
        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if ((tenantService != null) && (tenantService.isEnabled()) && (args != null))
            {
                // Convert each of the arguments to the spoofed (no tenant prefix) reference
            	convertMTArgs(args);
            }
            
            // Handle PolicyList level methods
            if (method.getDeclaringClass().equals(PolicyList.class))
            {
                return method.invoke(this, args);
            }
            
            // Handle Object level methods
            if (method.getName().equals("toString"))
            {
                return toString() + ": wrapped " + policyInterfaces.size() + " policies";
            }
            else if (method.getName().equals("hashCode"))
            {
                return hashCode();
            }
            else if (method.getName().equals("equals"))
            {
                return equals(args[0]);
            }

            // Invoke each wrapped policy in turn
            try
            {
                Object result = null;
                for (P policyInterface : policyInterfaces)
                {
                    result = method.invoke(policyInterface, args);
                }
                return result;
            }
            catch (InvocationTargetException e)
            {
                throw e.getTargetException();
            }
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.policy.PolicyList#getPolicies()
         */
        public Collection getPolicies()
        {
            return policyInterfaces;
        }
    }
    
    /**
     * Convert each of the arguments to the spoofed (no tenant prefix) reference.
     * 
     * Converts arguments of Type NodeRef to base name etc.
     * 
     * @param args list of non final arguments - the arguments are updated in place
     */
    protected static void convertMTArgs (Object[] args)
    {
        // Convert each of the arguments to the spoofed (no tenant prefix) reference
        for (int i = 0; i < args.length; i++)
        {
            Object arg = args[i];
            Object newArg = arg;
            if (arg == null)
            {
                // No conversion possible
            }
            if (arg instanceof StoreRef)
            {
                StoreRef ref = (StoreRef) arg;
                newArg = tenantService.getBaseName(ref);
            }
            else if (arg instanceof NodeRef)
            {
                NodeRef ref = (NodeRef) arg;
                newArg = tenantService.getBaseName(ref);
            }
            else if (arg instanceof ChildAssociationRef)
            {
                ChildAssociationRef ref = (ChildAssociationRef) arg;
                newArg = tenantService.getBaseName(ref);
            }
            else if (arg instanceof AssociationRef)
            {
                AssociationRef ref = (AssociationRef) arg;
                newArg = tenantService.getBaseName(ref);
            }
            
            // Substitute the new value
            args[i] = newArg;
        }    
    }
}
