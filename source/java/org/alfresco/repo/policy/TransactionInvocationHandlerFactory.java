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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
                Map<Integer, Object> executedBehaviours = (Map<Integer, Object>)AlfrescoTransactionSupport.getResource(EXECUTED_KEY);
                if (executedBehaviours == null)
                {
                    executedBehaviours = new HashMap<Integer, Object>();
                    AlfrescoTransactionSupport.bindResource(EXECUTED_KEY, executedBehaviours);
                }
                
                Integer behaviourKey = createInstanceKey(behaviour, definition.getArguments(), args);
                if (executedBehaviours.containsKey(behaviourKey) == false)
                {
                    // Invoke behavior for first time and mark as executed
                    try
                    {
                        result = method.invoke(policyInterface, args);
                        executedBehaviours.put(behaviourKey, result);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw e.getTargetException();
                    }
                }
                else
                {
                    // Return result of previous execution
                    result = executedBehaviours.get(behaviourKey);
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

                
        /**
         * Create an instance key for the behaviour based on the "key" arguments passed in
         * 
         * @param argDefs  definitions of behaviour arguments
         * @param args  the argument values
         * @return  the key
         */
        private Integer createInstanceKey(Behaviour behaviour, Arg[] argDefs, Object[] args)
        {
            int key = behaviour.hashCode();
            for (int i = 0; i < argDefs.length; i++)
            {
                if (argDefs[i].equals(Arg.KEY))
                {
                    key = (37 * key) + args[i].hashCode();
                }
            }
            return new Integer(key);
        }
        
    }

}