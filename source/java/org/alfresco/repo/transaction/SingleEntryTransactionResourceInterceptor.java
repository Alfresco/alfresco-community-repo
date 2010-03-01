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
package org.alfresco.repo.transaction;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.util.resource.MethodResourceManager;
import org.alfresco.util.resource.MethodResourceManager.MethodStatistics;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * This interceptor gathers basic method call statistics and calls the
 * {@link org.alfresco.util.resource.MethodResourceManager resource managers} for
 * further processing.  The resource managers are called <i>after</i> the method
 * invocations, but it doesn't matter too much since they are called on a time
 * frequency basis.
 * <p>
 * It acts as a sampling tool, but doesn't make any decisions or take any action
 * with regard system or transaction-related resources.  All samples are stored
 * against the current transaction.  If there is no current transaction then no
 * action will be taken with respect to the resource management.
 * <p>
 * The default is to activate after 10s and call through every 5s.
 * <p>
 * This class supports both interceptor-based calling as well as manual calling.
 * Long-running processes can call an this manually on every iteration and
 * get the same behaviour of regular calls to the resouce managers.
 * <p>
 * The current thread is marked on first entry and all subsequent nested re-entries
 * will just get passed down to the underlying delegate invocation method.
 * 
 * @see org.alfresco.util.resource.MethodResourceManager
 * 
 * @author Derek Hulley
 * @since 2.1.3
 */
public class SingleEntryTransactionResourceInterceptor implements MethodInterceptor
{
    private ThreadLocal<Boolean> threadLocalReentryCheck;
    private List<MethodResourceManager> methodResourceManagers;
    /** Default 10000ms (10s) */
    private long elapsedTimeBeforeActivationMillis = 10000L;
    /** Default 5000ms  (5s) */
    private long resourceManagerCallFrequencyMillis = 5000L;
    /** When the last call to the resource managers was made by this instance */
    private volatile long lastCallMillis;
    
    /**
     * A resource key unique to this interceptor.  This avoids clashes with other instances up
     * and down the stack operating in the same transaction.
     */ 
    private String resourceKey;

    public SingleEntryTransactionResourceInterceptor()
    {
        resourceKey = "MethodStats" + super.toString();
        threadLocalReentryCheck = new ThreadLocal<Boolean>();
    }
    
    /**
     * Set the method-based resource managers that will be notified of the statistics.
     * 
     * @param methodResourceManagers    a list of resource managers - may be null or empty
     */
    public void setMethodResourceManagers(List<MethodResourceManager> methodResourceManagers)
    {
        this.methodResourceManagers = methodResourceManagers;
    }

    /**
     * Set the minimum number of seconds that a transaction must have been running for
     * before method sampling begins.  The interceptor does nothing prior to this.
     * 
     * @param elapsedTimeBeforeActivationMillis    an initial idle time in milliseconds
     */
    public void setElapsedTimeBeforeActivationMillis(long elapsedTimeBeforeActivationMillis)
    {
        this.elapsedTimeBeforeActivationMillis = elapsedTimeBeforeActivationMillis;
    }

    /**
     * Set the approximate time between calls to the
     * {@link #setMethodResourceManagers(List) registered resource managers}.  This applies to this instance
     * of the interceptor and <u>not to the transaction</u>.  If a single instance of this
     * class is used in multiple places, then the resource managers will still only get called at a steady
     * rate.  This is mainly in order to streamline the interception prior to the activation phase, but suits
     * the resource managers since they get given the exact methods that were called anyway.
     * 
     * @param resourceManagerCallFrequencyMillis an approximate time between calls to the resource managers
     */
    public void setResourceManagerCallFrequencyMillis(long resourceManagerCallFrequencyMillis)
    {
        this.resourceManagerCallFrequencyMillis = resourceManagerCallFrequencyMillis;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        if (threadLocalReentryCheck.get() == Boolean.TRUE)
        {
            // We're already in a wrapped resource, so avoid doing anything
            return invocation.proceed();
        }
        else if (methodResourceManagers == null || methodResourceManagers.size() == 0)
        {
            // We just ignore everything
            return invocation.proceed();
        }
        try
        {
            // Mark this thread
            threadLocalReentryCheck.set(Boolean.TRUE);
            return invokeInternal(invocation);
        }
        finally
        {
            // Unmark this thread
            threadLocalReentryCheck.set(null);
        }
    }

    private Object invokeInternal(MethodInvocation invocation) throws Throwable
    {
        // Get the txn start time
        long txnStartTime = AlfrescoTransactionSupport.getTransactionStartTime();
        if (txnStartTime < 0)
        {
            // There is no transaction
            return invocation.proceed();
        }
        
        // Check if the required time has passed
        long now = System.currentTimeMillis();
        long txnElapsedTime = (now - txnStartTime);
        if (txnElapsedTime < elapsedTimeBeforeActivationMillis)
        {
            // It's not been long enough
            return invocation.proceed();
        }

        // We need to start timing the method calls
        Method calledMethod = invocation.getMethod();
        long beforeNs = System.nanoTime();
        Object ret = invocation.proceed();
        long deltaNs = System.nanoTime() - beforeNs;
        
        // Get the method stats
        @SuppressWarnings("unchecked")
        Map<Method, MethodStatistics> methodStatsByMethod =
            (Map<Method, MethodStatistics>) AlfrescoTransactionSupport.getResource(resourceKey);
        if (methodStatsByMethod == null)
        {
            methodStatsByMethod = new HashMap<Method, MethodStatistics>(11);
            AlfrescoTransactionSupport.bindResource(resourceKey, methodStatsByMethod);
        }
        
        // Update method stats
        MethodStatistics calledMethodStats = methodStatsByMethod.get(calledMethod);
        if (calledMethodStats == null)
        {
            calledMethodStats = new MethodStatistics();
            methodStatsByMethod.put(calledMethod, calledMethodStats);
        }
        calledMethodStats.accumulateNs(deltaNs);
        
        // Check if we need to call the resource managers to clean up 
        if ((now - lastCallMillis) >= resourceManagerCallFrequencyMillis)
        {
            for (MethodResourceManager resourceManager : methodResourceManagers)
            {
                resourceManager.manageResources(methodStatsByMethod, txnElapsedTime, calledMethod);
            }
            lastCallMillis = now;
        }
        
        // Done
        return ret;
    }
    
    /**
     * An alternative method allowing a manual call to check the resources.  This is useful
     * in the cases where long running iterations don't necessarily pass through the
     * necessary API stack, or where the specific resources in hand can't be dealt with
     * blindly before and after resource management.  The elapsed time should be that of the
     * iteration within the method (it is assumed that there won't be more than one).
     * <p>
     * If you have a loop in a method that doesn't call anything that can be intercepted
     * and handle safely, then get a pre-configured instance (usually from the application context)
     * and mimic the interceptor call.
     * <p>
     * You should get the <code>Method</code>, which is used for informational purposes, in a
     * single call when you calling code is loaded by the classloader.  Introspecting every time
     * you wish to call this method is unnecessary.
     * 
     * @param calledMethod  the method that this check applies to
     * @param deltaNs       the time in milliseconds that the repeated operation took
     */
    public void performManualCheck(Method calledMethod, long deltaNs)
    {
        /*
         * This is mainly duplicated code, but it can be heavily used so nice patterns
         * are not preferable to speed of execution.
         */
        
        if (methodResourceManagers == null || methodResourceManagers.size() == 0)
        {
            // We just ignore everything
            return;
        }
        
        // Get the txn start time
        long txnStartTime = AlfrescoTransactionSupport.getTransactionStartTime();
        if (txnStartTime < 0)
        {
            // There is no transaction
            return;
        }
        
        // Check if the required time has passed
        long now = System.currentTimeMillis();
        long txnElapsedTime = (now - txnStartTime);
        if (txnElapsedTime < elapsedTimeBeforeActivationMillis)
        {
            // It's not been long enough
            return;
        }

        // Get the method stats
        @SuppressWarnings("unchecked")
        Map<Method, MethodStatistics> methodStatsByMethod =
            (Map<Method, MethodStatistics>) AlfrescoTransactionSupport.getResource(resourceKey);
        if (methodStatsByMethod == null)
        {
            methodStatsByMethod = new HashMap<Method, MethodStatistics>(11);
            AlfrescoTransactionSupport.bindResource(resourceKey, methodStatsByMethod);
        }
        
        // Update method stats
        MethodStatistics calledMethodStats = methodStatsByMethod.get(calledMethod);
        if (calledMethodStats == null)
        {
            calledMethodStats = new MethodStatistics();
            methodStatsByMethod.put(calledMethod, calledMethodStats);
        }
        calledMethodStats.accumulateNs(deltaNs);
        
        // Check if we need to call the resource managers to clean up 
        if ((now - lastCallMillis) >= resourceManagerCallFrequencyMillis)
        {
            for (MethodResourceManager resourceManager : methodResourceManagers)
            {
                resourceManager.manageResources(methodStatsByMethod, txnElapsedTime, calledMethod);
            }
            lastCallMillis = now;
        }
        // Done
        return;
    }
}
