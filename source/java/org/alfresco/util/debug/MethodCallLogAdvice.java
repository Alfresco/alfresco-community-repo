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
package org.alfresco.util.debug;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Performs writing to DEBUG of incoming arguments and outgoing results for a method call.<br>
 * If the method invocation throws an exception, then the incoming arguments are
 * logged to DEBUG as well.<br>
 * The implementation adds very little overhead to a normal method
 * call by only building log messages when required.
 * <p>
 * The logging is done against the logger retrieved using the names:
 * <p>
 * <pre>
 *      org.alfresco.util.debug.MethodCallLogAdvice
 *         AND
 *      targetClassName
 *      targetClassName.methodName
 *      targetClassName.methodName.exception
 * </pre>
 * <p>
 * The following examples show how to control the log levels:
 * <p>
 * <pre>
 *      org.alfresco.util.debug.MethodCallLogAdvice=DEBUG   # activate method logging
 *          AND
 *      x.y.MyClass=DEBUG                           # log debug for all method calls on MyClass
 *      x.y.MyClass.doSomething=DEBUG               # log debug for all doSomething method calls
 *      x.y.MyClass.doSomething.exception=DEBUG     # only log debug for doSomething() upon exception
 * </pre>
 * <p>
 * 
 * @author Derek Hulley
 */
public class MethodCallLogAdvice implements MethodInterceptor
{
    private static final Log logger = LogFactory.getLog(MethodCallLogAdvice.class);

    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        if (logger.isDebugEnabled())
        {
            return invokeWithLogging(invocation);
        }
        else
        {
            // no logging required
            return invocation.proceed();
        }
    }
    
    /**
     * Only executes logging code if logging is required
     */
    private Object invokeWithLogging(MethodInvocation invocation) throws Throwable
    {
        String methodName = invocation.getMethod().getName();
        String className = invocation.getMethod().getDeclaringClass().getName();
        
        // execute as normal
        try
        {
            Object ret = invocation.proceed();
            // logging
            Log methodLogger = LogFactory.getLog(className + "." + methodName);
            if (methodLogger.isDebugEnabled())
            {
                // log success
                StringBuffer sb = getInvocationInfo(className, methodName, invocation.getArguments()); 
                sb.append("   Result: ").append(ret);
                methodLogger.debug(sb);
            }
            // done
            return ret;
        }
        catch (Throwable e)
        {
            Log exceptionLogger = LogFactory.getLog(className + "." + methodName + ".exception");
            if (exceptionLogger.isDebugEnabled())
            {
                StringBuffer sb = getInvocationInfo(className, methodName, invocation.getArguments()); 
                sb.append("   Failure: ").append(e.getClass().getName()).append(" - ").append(e.getMessage());
                exceptionLogger.debug(sb);
            }
            // rethrow
            throw e;
        }
    }
    
    /**
     * Return format:
     * <pre>
     *      Method: className#methodName
     *         Argument: arg0
     *         Argument: arg1
     *         ...
     *         Argument: argN {newline}
     * </pre>
     * 
     * @param className
     * @param methodName
     * @param args
     * @return Returns a StringBuffer containing the details of a method call
     */
    private StringBuffer getInvocationInfo(String className, String methodName, Object[] args)
    {
        StringBuffer sb = new StringBuffer(250);
        sb.append("\nMethod: ").append(className).append("#").append(methodName).append("\n");
        sb.append("   Transaction: ").append(AlfrescoTransactionSupport.getTransactionId()).append("\n");
        for (Object arg : args)
        {
            sb.append("   Argument: ").append(arg).append("\n");
        }
        return sb;
    }
}
