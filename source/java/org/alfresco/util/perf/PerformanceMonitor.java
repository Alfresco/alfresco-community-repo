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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vladium.utils.timing.ITimer;
import com.vladium.utils.timing.TimerFactory;

/**
 * Enables <b>begin ... end</b> style performance monitoring with summarisation
 * using the <b>performance</b> logging category.  It is designed to only incur
 * a minor cost when performance logging is turned on using the DEBUG logging
 * mechanism.  See base class for details on enabling the <b>performance</b>
 * logging categories.
 * <p>
 * This class is thread safe.
 * <p>
 * Usage:
 * <pre>
 * private PerformanceMonitor somethingTimer = new PerformanceMonitor("mytest", "doSomething");
 * ...
 * ...
 * private void doSomething()
 * {
 *    somethingTimer.start();
 *    ...
 *    ...
 *    somethingTimer.stop();
 * }
 * </pre>
 * 
 * @author Derek Hulley
 */
public class PerformanceMonitor extends AbstractPerformanceMonitor
{
    private String methodName;
    private ThreadLocal<ITimer> threadLocalTimer;
    private boolean log;
    
    /**
     * @param entityName name of the entity, e.g. a test name or a bean name against which to
     *      log the performance
     * @param methodName the method for which the performance will be logged
     */
    public PerformanceMonitor(String entityName, String methodName)
    {
        super(entityName);
        this.methodName = methodName;
        this.threadLocalTimer = new ThreadLocal<ITimer>();
        
        // check if logging can be eliminated
        Log methodLogger = LogFactory.getLog("performance." + entityName + "." + methodName);
        this.log = AbstractPerformanceMonitor.isDebugEnabled() && methodLogger.isDebugEnabled();  
    }
    
    /**
     * Threadsafe method to start the timer.
     * <p>
     * The timer is only started if the logging levels are enabled.
     * 
     * @see #stop()
     */
    public void start()
    {
        if (!log)
        {
            // don't bother timing
            return;
        }
        // overwrite the thread's timer
        ITimer timer = TimerFactory.newTimer();
        threadLocalTimer.set(timer);
        // start the timer
        timer.start();
    }
    
    /**
     * Threadsafe method to stop the timer.
     * 
     * @see #start()
     */
    public void stop()
    {
        if (!log)
        {
            // don't bother timing
            return;
        }
        // get the thread's timer
        ITimer timer = threadLocalTimer.get();
        if (timer == null)
        {
            // begin not called on the thread
            return;
        }
        // time it
        timer.stop();
        recordStats(methodName, timer.getDuration());
        
        // drop the thread's timer
        threadLocalTimer.set(null);
    }
}
