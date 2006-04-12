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