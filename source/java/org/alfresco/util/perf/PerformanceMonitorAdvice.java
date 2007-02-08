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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
