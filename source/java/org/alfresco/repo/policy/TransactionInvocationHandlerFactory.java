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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.Policy.Arg;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;

/**
 * Factory for creating transaction-aware behaviour invocation handlers.
 */
public class TransactionInvocationHandlerFactory
{
    /** Transaction Key for Behaviour Execution state */
    private final static String EXECUTED_KEY = TransactionHandler.class.getName() + ".executed";
    
    /** Transaction behaviour Queue */
    private TransactionBehaviourQueue queue;
    
    /**
     * Construct
     * 
     * @param queue  behaviour queue
     */
    public TransactionInvocationHandlerFactory(TransactionBehaviourQueue queue)
    {
        this.queue = queue;
    }
    

    /**
     * Create Invocation Handler
     * 
     * @param <P>
     * @param behaviour
     * @param definition
     * @param policyInterface
     * @return  invocation handler
     */
    public <P extends Policy> InvocationHandler createHandler(Behaviour behaviour, PolicyDefinition<P> definition, P policyInterface)
    {
        return new TransactionHandler<P>(behaviour, definition, policyInterface);
    }
    

    /**
     * Transaction Invocation Handler.
     *
     * @param <P>  policy interface
     */
    private class TransactionHandler<P extends Policy> implements InvocationHandler
    {
        private Behaviour behaviour;
        private PolicyDefinition<P> definition;
        private P policyInterface;
       
        /**
         * Construct
         */
        public TransactionHandler(Behaviour behaviour, PolicyDefinition<P> definition, P policyInterface)
        {
            this.behaviour = behaviour;
            this.definition = definition;
            this.policyInterface = policyInterface;
        }
        
        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        @SuppressWarnings("unchecked")
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            // Handle Object level methods
            if (method.getName().equals("toString"))
            {
                return policyInterface.toString();
            }
            else if (method.getName().equals("hashCode"))
            {
                return policyInterface.hashCode();
            }
            else if (method.getName().equals("equals"))
            {
                return policyInterface.equals(args[0]);
            }

            // Invoke policy based on its notification frequency
            Object result = null;
            if (behaviour.getNotificationFrequency().equals(NotificationFrequency.FIRST_EVENT))
            {
                Map<ExecutionInstanceKey, Object> executedBehaviours = (Map<ExecutionInstanceKey, Object>)AlfrescoTransactionSupport.getResource(EXECUTED_KEY);
                if (executedBehaviours == null)
                {
                    executedBehaviours = new HashMap<ExecutionInstanceKey, Object>();
                    AlfrescoTransactionSupport.bindResource(EXECUTED_KEY, executedBehaviours);
                }
                                
                ExecutionInstanceKey key = new  ExecutionInstanceKey(behaviour, definition.getArguments(), args);
                
                if (executedBehaviours.containsKey(key) == false)
                {
                    // Invoke behavior for first time and mark as executed
                    try
                    {
                        result = method.invoke(policyInterface, args);
                        executedBehaviours.put(key, result);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw e.getTargetException();
                    }
                }
                else
                {
                    // Return result of previous execution
                    result = executedBehaviours.get(key);
                }
            }
            else if (behaviour.getNotificationFrequency().equals(NotificationFrequency.TRANSACTION_COMMIT))
            {
                // queue policy invocation for end of transaction
                queue.queue(behaviour, definition, policyInterface, method, args);
            }
            else
            {
                // Note: shouldn't get here
                throw new PolicyException("Invalid Notification frequency " + behaviour.getNotificationFrequency());
            }
            
            return result;
        }        
    }
    
    /**
     * Execution Instance Key - to uniquely identify an ExecutionContext
     * 
     * @param <P>
     */
    private class ExecutionInstanceKey
    {
        public ExecutionInstanceKey(Behaviour behaviour, Arg[] argDefs, Object[] args)
        {
            this.behaviour = behaviour;
            
            for (int i = 0; i < argDefs.length; i++)
            {
                if (argDefs[i].equals(Arg.KEY))
                {
                    keys.add(args[i]);
                }
            }
        }
        
        Behaviour behaviour;
        ArrayList<Object> keys = new ArrayList<Object>();
        
        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {   
            int key = behaviour.hashCode();
            for (int i = 0; i < keys.size(); i++)
            {
                key = (37 * key) + keys.get(i).hashCode();
            }
            return key;
        }
        
        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof ExecutionInstanceKey)
            {
                ExecutionInstanceKey that = (ExecutionInstanceKey) obj;
                if(this.behaviour.equals(that.behaviour))
                {
                    if(keys.size() != that.keys.size())
                    {
                        // different number of keys
                        return false;
                    }
                    if(keys.containsAll(that.keys))
                    {
                        // yes keys are equal
                        return true;
                    }
                }

                // behavior is different
                return false;
            }
            else
            {
                // Object is wrong type
                return false;
            }
        } // equals
    } // ExecutionInstanceKey


}