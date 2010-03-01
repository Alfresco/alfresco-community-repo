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
package org.alfresco.util.perf;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;

/**
 * An instance of this class keeps track of timings of method calls on a bean
 * 
 * @author Derek Hulley
 */
public class PerformanceMonitorAdvice extends AbstractPerformanceMonitor implements MethodInterceptor
{
    public PerformanceMonitorAdvice(String beanName)
    {
        super(beanName);
    }
    
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        // bypass all recording if performance logging is not required
        if (AbstractPerformanceMonitor.isDebugEnabled())
        {
            return invokeWithLogging(invocation);
        }
        else
        {
            // no logging required
            return invocation.proceed();
        }
    }
    
    private Object invokeWithLogging(MethodInvocation invocation) throws Throwable
    {
        // get the time prior to call
        ITimer timer = TimerFactory.newTimer ();
        
        timer.start ();

        //long start = System.currentTimeMillis();
        // execute - do not record exceptions
        Object ret = invocation.proceed();
        // get time after call
        //long end = System.currentTimeMillis();
        // record the stats
        timer.stop ();
       
        recordStats(invocation.getMethod().getName(),  timer.getDuration ());
        // done
        return ret;
    }
}
