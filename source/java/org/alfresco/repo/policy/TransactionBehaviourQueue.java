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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.policy.Policy.Arg;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.util.GUID;


/**
 * Transaction Behaviour Queue.
 * 
 * Responsible for keeping a record of behaviours to execute at the end of a transaction.
 */
public class TransactionBehaviourQueue implements TransactionListener
{
    /** Id used in equals and hash */
    private String id = GUID.generate();
    
    // Transaction Keys for Behaviour Execution state
    private static final String QUEUE_CONTEXT_KEY = TransactionBehaviourQueue.class.getName() + ".context";
    
    
    /**
     * Queue a behaviour for end-of-transaction execution
     *  
     * @param <P>
     * @param behaviour
     * @param definition
     * @param policyInterface
     * @param method
     * @param args
     */
    @SuppressWarnings("unchecked")
    public <P extends Policy> void queue(Behaviour behaviour, PolicyDefinition<P> definition, P policyInterface, Method method, Object[] args)
    {
        // Construct queue context, if required
        QueueContext queueContext = (QueueContext)AlfrescoTransactionSupport.getResource(QUEUE_CONTEXT_KEY);
        if (queueContext == null)
        {
            queueContext = new QueueContext();
            AlfrescoTransactionSupport.bindResource(QUEUE_CONTEXT_KEY, queueContext);
            AlfrescoTransactionSupport.bindListener(this);
        }
        
        // Determine if behaviour instance has already been queued
        
        // Identity of ExecutionContext is Behaviour + KEY argument(s)
        ExecutionInstanceKey key = new  ExecutionInstanceKey(behaviour, definition.getArguments(), args);
        
        ExecutionContext executionContext = queueContext.index.get(key);
        
        if (executionContext == null)
        {
            // Context does not exist
            // Create execution context for behaviour
            executionContext = new ExecutionContext<P>();
            executionContext.method = method;
            executionContext.args = args;
            executionContext.policyInterface = policyInterface;

            // Defer or execute now?
            if (!queueContext.committed)
            {
                // queue behaviour for deferred execution
                queueContext.queue.offer(executionContext);
            }
            else
            {
                // execute now
                execute(executionContext);
            }
            queueContext.index.put(key, executionContext);
        }
        else
        {
            // Context does already exist
            // Update behaviour instance execution context, in particular, argument state that is marked END_TRANSACTION
            Arg[] argDefs = definition.getArguments();
            for (int i = 0; i < argDefs.length; i++)
            {
                if (argDefs[i].equals(Arg.END_VALUE))
                {
                    executionContext.args[i] = args[i];
                }
            }
        }
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#flush()
     */
    public void flush()
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCommit(boolean)
     */
    @SuppressWarnings("unchecked")
    public void beforeCommit(boolean readOnly)
    {
        QueueContext queueContext = (QueueContext)AlfrescoTransactionSupport.getResource(QUEUE_CONTEXT_KEY);
        ExecutionContext context = queueContext.queue.poll();
        while (context != null)
        {
            execute(context);
            context = queueContext.queue.poll();
        }
        queueContext.committed = true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#beforeCompletion()
     */
    public void beforeCompletion()
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#afterCommit()
     */
    public void afterCommit()
    {
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.transaction.TransactionListener#afterRollback()
     */
    public void afterRollback()
    {
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

    /**
     * Execute behaviour as described in execution context
     * 
     * @param context
     */
    private void execute(ExecutionContext context)
    {
        try
        {
            context.method.invoke(context.policyInterface, context.args);
        }
        catch (IllegalArgumentException e)
        {
            throw new AlfrescoRuntimeException("Failed to execute transaction-level behaviour " + context.method + " in transaction " + AlfrescoTransactionSupport.getTransactionId(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new AlfrescoRuntimeException("Failed to execute transaction-level behaviour " + context.method + " in transaction " + AlfrescoTransactionSupport.getTransactionId(), e);
        }
        catch (InvocationTargetException e)
        {
            throw new AlfrescoRuntimeException("Failed to execute transaction-level behaviour " + context.method + " in transaction " + AlfrescoTransactionSupport.getTransactionId(), e.getTargetException());
        }
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return this.id.hashCode();
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
        if (obj instanceof TransactionBehaviourQueue)
        {
            TransactionBehaviourQueue that = (TransactionBehaviourQueue) obj;
            return (this.id.equals(that.id));
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Behaviour execution Context
     * 
     * @param <P>
     */
    private class ExecutionContext<P extends Policy>
    {
        Method method;
        Object[] args;
        P policyInterface;
    }
    
    
    /**
     * Queue Context
     */
    private class QueueContext
    {
        // TODO: Tune sizes
        Queue<ExecutionContext> queue = new LinkedList<ExecutionContext>();
        Map<ExecutionInstanceKey, ExecutionContext> index = new HashMap<ExecutionInstanceKey, ExecutionContext>();
        boolean committed = false;
    }
        
}
