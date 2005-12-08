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
