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
package org.alfresco.ibatis;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper that runs a unit of work, transparently retrying the unit of work if
 * an error occurs.
 * <p>
 * Defaults:
 * <ul>
 *   <li><b>maxRetries: 5</b></li>
 *   <li><b>retryWaitMs: 10</b></li>
 * </ul>
 *
 * @author Derek Hulley
 * @since 3.4
 */
public class RetryingCallbackHelper
{
    private static final Log logger = LogFactory.getLog(RetryingCallbackHelper.class);
    
    /** The maximum number of retries. -1 for infinity. */
    private int maxRetries;
    /** How much time to wait with each retry. */
    private int retryWaitMs;
    
    /**
     * Callback interface
     * @author Derek Hulley
     */
    public interface RetryingCallback<Result>
    {
        /**
         * Perform a unit of work.
         *
         * @return              Return the result of the unit of work
         * @throws Throwable    This can be anything and will guarantee either a retry or a rollback
         */
        public Result execute() throws Throwable;
    };

    /**
     * Default constructor.
     */
    public RetryingCallbackHelper()
    {
        this.maxRetries = 5;
        this.retryWaitMs = 10;
    }

    /**
     * Set the maximimum number of retries. -1 for infinity.
     */
    public void setMaxRetries(int maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    public void setRetryWaitMs(int retryWaitMs)
    {
        this.retryWaitMs = retryWaitMs;
    }

    /**
     * Execute a callback until it succeeds, fails or until a maximum number of retries have
     * been attempted.
     *
     * @param callback          The callback containing the unit of work.
     * @return                  Returns the result of the unit of work.
     * @throws                  RuntimeException  all checked exceptions are converted
     */
    public <R> R doWithRetry(RetryingCallback<R> callback)
    {
        // Track the last exception caught, so that we can throw it if we run out of retries.
        RuntimeException lastException = null;
        for (int count = 0; count == 0 || count < maxRetries; count++)
        {
            try
            {
                // Do the work.
                R result = callback.execute();
                if (logger.isDebugEnabled())
                {
                    if (count != 0)
                    {
                        logger.debug("\n" +
                                "Retrying work succeeded: \n" +
                                "   Thread: " + Thread.currentThread().getName() + "\n" +
                                "   Iteration: " + count);
                    }
                }
                return result;
            }
            catch (Throwable e)
            {
                lastException = (e instanceof RuntimeException) ?
                     (RuntimeException) e :
                         new AlfrescoRuntimeException("Exception in Transaction.", e);
                if (logger.isDebugEnabled())
                {
                    logger.debug("\n" +
                            "Retrying work failed: \n" +
                            "   Thread: " + Thread.currentThread().getName() + "\n" +
                            "   Iteration: " + count + "\n" +
                            "   Exception follows:",
                            e);
                }
                else if (logger.isInfoEnabled())
                {
                    String msg = String.format(
                            "Retrying %s: count %2d; wait: %3dms; msg: \"%s\"; exception: (%s)",
                            Thread.currentThread().getName(),
                            count, retryWaitMs,
                            e.getMessage(),
                            e.getClass().getName());
                    logger.info(msg);
                }
                try
                {
                    Thread.sleep(retryWaitMs);
                }
                catch (InterruptedException ie)
                {
                    // Do nothing.
                }
                // Try again
                continue;
            }
        }
        // We've worn out our welcome and retried the maximum number of times.
        // So, fail.
        throw lastException;
    }
}
